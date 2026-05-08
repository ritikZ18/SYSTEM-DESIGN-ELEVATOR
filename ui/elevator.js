const NF = 8;
const FH = 52;
const NE = 3;
const SX = [44, 116, 188];
const SW = 62;
const EL = ["A", "B", "C"];
const CAR_THEMES = ["car-theme-a", "car-theme-b", "car-theme-c"];
const STATUS_THEMES = ["status-theme-a", "status-theme-b", "status-theme-c"];
const API = "http://localhost:8080";
let healthTimer = null;

let S = {
  elevs: [
    { id: 0, fl: 1, dir: "IDLE", req: new Set() },
    { id: 1, fl: 4, dir: "IDLE", req: new Set() },
    { id: 2, fl: 8, dir: "IDLE", req: new Set() }
  ],
  ext: new Set(),
  selE: null,
  logs: [],
  step: 0,
  intv: null,
  running: false,
  useAPI: false
};

const f2y = (f) => (NF - f) * FH;

function init() {
  const building = document.getElementById("bld");
  building.innerHTML = "";

  for (let i = 0; i < NE; i++) {
    const shaft = document.createElement("div");
    shaft.className = "shaft-column";
    shaft.style.left = SX[i] + "px";

    const shaftLabel = document.createElement("div");
    shaftLabel.style.cssText = "position:absolute;bottom:3px;width:100%;text-align:center;font-size:8px;font-weight:500;color:#aaa;letter-spacing:1px";
    shaftLabel.textContent = "LIFT " + EL[i];

    shaft.appendChild(shaftLabel);
    building.appendChild(shaft);
  }

  for (let f = NF; f >= 1; f--) {
    const y = f2y(f);

    const floorRow = document.createElement("div");
    floorRow.className = "floor-row";
    floorRow.style.top = y + "px";
    building.appendChild(floorRow);

    const floorLabel = document.createElement("div");
    floorLabel.className = "floor-label";
    floorLabel.style.top = y + "px";
    floorLabel.textContent = "F" + f;
    building.appendChild(floorLabel);

    const floorButtons = document.createElement("div");
    floorButtons.className = "floor-button-stack";
    floorButtons.style.top = y + "px";
    building.appendChild(floorButtons);

    if (f < NF) {
      const upButton = document.createElement("button");
      upButton.className = "hall-button";
      upButton.textContent = "▲";
      upButton.id = "bu" + f;
      upButton.onclick = () => extReq(f, "UP");
      floorButtons.appendChild(upButton);
    }

    if (f > 1) {
      const downButton = document.createElement("button");
      downButton.className = "hall-button";
      downButton.textContent = "▼";
      downButton.id = "bd" + f;
      downButton.onclick = () => extReq(f, "DOWN");
      floorButtons.appendChild(downButton);
    }
  }

  for (let i = 0; i < NE; i++) {
    const car = document.createElement("div");
    car.className = "elevator-car " + CAR_THEMES[i];
    car.id = "c" + i;
    car.style.left = SX[i] + 3 + "px";
    car.style.width = SW - 6 + "px";
    car.innerHTML = '<div class="car-name">LIFT ' + EL[i] + '</div><div class="car-floor" id="cf' + i + '">' + S.elevs[i].fl + '</div><div class="car-direction" id="cd' + i + '">IDLE</div>';
    car.onclick = () => selElev(i);
    building.appendChild(car);
  }

  addLog("System ready - Lift A:F1  Lift B:F4  Lift C:F8", "info");

  checkApiHealth(false);
  if (!healthTimer) {
    healthTimer = setInterval(() => checkApiHealth(false), 5000);
  }

  render();
}

function setApiHealth(status, text) {
  const dot = document.getElementById("api-dot");
  const label = document.getElementById("api-text");
  if (!dot || !label) {
    return;
  }

  dot.classList.remove("api-healthy", "api-unhealthy", "api-unknown");
  if (status === "up") {
    dot.classList.add("api-healthy");
  } else if (status === "down") {
    dot.classList.add("api-unhealthy");
  } else {
    dot.classList.add("api-unknown");
  }
  label.textContent = text;
}

async function checkApiHealth(manual) {
  try {
    const res = await fetch(API + "/status");
    if (!res.ok) {
      throw new Error("HTTP " + res.status);
    }
    const data = await res.json();
    S.useAPI = true;
    setApiHealth("up", "Backend connected");
    expl("Java API connected. Controls now sync with your Java backend.");
    if (Array.isArray(data)) {
      syncFromAPI(data);
    }
    if (manual) {
      addLog("API health check success", "success");
      render();
    }
  } catch (err) {
    S.useAPI = false;
    setApiHealth("down", "Backend unavailable (running local simulation)");
    if (manual) {
      addLog("API health check failed", "info");
      render();
    }
  }
}

function render() {
  for (const e of S.elevs) {
    const car = document.getElementById("c" + e.id);
    if (!car) {
      continue;
    }

    car.style.top = f2y(e.fl) + 4 + "px";
    document.getElementById("cf" + e.id).textContent = e.fl;
    document.getElementById("cd" + e.id).textContent = e.dir === "IDLE" ? "idle" : e.dir === "UP" ? "going up" : "going dn";
    car.classList.toggle("selected", S.selE === e.id);

    let pending = car.querySelector(".pending-indicator");
    if (e.req.size > 0) {
      if (!pending) {
        pending = document.createElement("div");
        pending.className = "pending-indicator";
        car.appendChild(pending);
      }
      pending.title = "Stops: " + [...e.req].sort((a, b) => a - b).join(", ");
    } else if (pending) {
      pending.remove();
    }
  }

  for (let f = 1; f <= NF; f++) {
    const upButton = document.getElementById("bu" + f);
    const downButton = document.getElementById("bd" + f);
    if (upButton) {
      upButton.classList.toggle("call-up-active", S.ext.has(f + "-UP"));
    }
    if (downButton) {
      downButton.classList.toggle("call-down-active", S.ext.has(f + "-DOWN"));
    }
  }

  document.getElementById("stat").innerHTML = S.elevs
    .map((e) => {
      const icon = e.dir === "IDLE" ? "●" : e.dir === "UP" ? "▲" : "▼";
      const req = e.req.size > 0 ? " [" + [...e.req].sort((a, b) => a - b).join(",") + "]" : "";
      return '<div class="status-chip ' + STATUS_THEMES[e.id] + '">Lift ' + EL[e.id] + " F" + e.fl + " " + icon + req + "</div>";
    })
    .join("");

  const insidePanel = document.getElementById("inside-panel");
  if (S.selE !== null) {
    insidePanel.style.display = "block";

    const liftLabel = document.getElementById("il");
    liftLabel.textContent = EL[S.selE];
    liftLabel.style.color = ["#3C3489", "#085041", "#712B13"][S.selE];

    const e = S.elevs[S.selE];
    const floorGrid = document.getElementById("fg");
    floorGrid.innerHTML = "";

    for (let f = NF; f >= 1; f--) {
      const floorButton = document.createElement("div");
      floorButton.className =
        "inside-floor-button" +
        (e.req.has(f) ? " requested" : "") +
        (e.fl === f ? " current-floor" : "");
      floorButton.textContent = "F" + f;
      if (e.fl !== f) {
        floorButton.onclick = () => intReq(f);
      }
      floorGrid.appendChild(floorButton);
    }
  } else {
    insidePanel.style.display = "none";
  }

  document.getElementById("lg").innerHTML = S.logs
    .slice(0, 25)
    .map((l) => '<div class="log-line ' + l.t + '">[' + l.s + "] " + l.m + "</div>")
    .join("");
}

function syncFromAPI(data) {
  if (!Array.isArray(data)) {
    return;
  }

  for (const ev of data) {
    const idx = EL.indexOf(ev.label);
    if (idx === -1) {
      continue;
    }
    S.elevs[idx].fl = ev.floor;
    S.elevs[idx].dir = ev.direction;
    S.elevs[idx].req = new Set();
  }
  render();
}

async function extReq(fl, dir) {
  const key = fl + "-" + dir;
  if (S.ext.has(key)) {
    return;
  }

  S.ext.add(key);

  if (S.useAPI) {
    try {
      const res = await fetch(API + "/call?floor=" + fl + "&dir=" + dir);
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      const data = await res.json();
      setApiHealth("up", "Backend connected");
      syncFromAPI(data.status || []);
    } catch (err) {
      S.useAPI = false;
      setApiHealth("down", "Backend unavailable (running local simulation)");
      const e = bestLift(fl, dir);
      e.req.add(fl);
      updDir(e);
    }
  } else {
    const e = bestLift(fl, dir);
    e.req.add(fl);
    updDir(e);
  }

  addLog("Floor " + fl + " [" + dir + "] dispatched", "info");
  expl("Dispatch: floor " + fl + " [" + dir + "] request sent. Press Step to move.");
  render();
}

async function intReq(fl) {
  if (S.selE === null) {
    return;
  }

  const e = S.elevs[S.selE];
  if (e.req.has(fl)) {
    return;
  }

  if (S.useAPI) {
    try {
      const res = await fetch(API + "/internal?elevId=" + S.selE + "&floor=" + fl);
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      setApiHealth("up", "Backend connected");
    } catch (err) {
      S.useAPI = false;
      setApiHealth("down", "Backend unavailable (running local simulation)");
    }
  }

  e.req.add(fl);
  updDir(e);
  addLog("Inside Lift " + EL[S.selE] + ": floor " + fl + " pressed", "info");
  expl("Floor " + fl + " added to Lift " + EL[S.selE] + " stops.");
  render();
}

function selElev(id) {
  S.selE = S.selE === id ? null : id;
  expl(S.selE !== null ? "Lift " + EL[id] + " selected. Pick destination floor below." : "Press floor buttons to call a lift.");
  render();
}

async function doStep() {
  S.step++;

  if (S.useAPI) {
    try {
      const res = await fetch(API + "/step");
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      const data = await res.json();
      setApiHealth("up", "Backend connected");
      if (Array.isArray(data)) {
        syncFromAPI(data);
      }
      addLog("Step - synced from Java", "");
    } catch (err) {
      S.useAPI = false;
      setApiHealth("down", "Backend unavailable (running local simulation)");
      localStep();
    }
  } else {
    localStep();
  }

  render();
}

function localStep() {
  for (const e of S.elevs) {
    if (e.req.has(e.fl)) {
      e.req.delete(e.fl);
      S.ext.delete(e.fl + "-UP");
      S.ext.delete(e.fl + "-DOWN");
      addLog("Lift " + EL[e.id] + ": STOP at F" + e.fl, "success");
      expl("Lift " + EL[e.id] + " arrived at floor " + e.fl + ". Doors open.");
    }

    updDir(e);
    if (!e.req.size) {
      continue;
    }

    const previousFloor = e.fl;
    if (e.dir === "UP" && e.fl < NF) {
      e.fl++;
    } else if (e.dir === "DOWN" && e.fl > 1) {
      e.fl--;
    }

    if (e.fl !== previousFloor) {
      addLog("Lift " + EL[e.id] + ": F" + previousFloor + "->F" + e.fl, "");
    }
  }

  if (S.elevs.every((e) => !e.req.size)) {
    expl("All lifts idle.");
  }
}

function scoreFn(e, fl, dir) {
  const d = Math.abs(e.fl - fl);
  if (e.dir === "IDLE") {
    return d;
  }
  if (e.dir === dir) {
    if (dir === "UP" && e.fl <= fl) {
      return d;
    }
    if (dir === "DOWN" && e.fl >= fl) {
      return d;
    }
  }
  return d + NF;
}

function bestLift(fl, dir) {
  return S.elevs.reduce((best, e) => (scoreFn(e, fl, dir) < scoreFn(best, fl, dir) ? e : best), S.elevs[0]);
}

function updDir(e) {
  if (!e.req.size) {
    e.dir = "IDLE";
    return;
  }

  const targets = [...e.req];
  const above = targets.filter((f) => f > e.fl);
  const below = targets.filter((f) => f < e.fl);

  if (e.dir === "UP") {
    e.dir = above.length ? "UP" : below.length ? "DOWN" : "IDLE";
  } else if (e.dir === "DOWN") {
    e.dir = below.length ? "DOWN" : above.length ? "UP" : "IDLE";
  } else {
    e.dir = above.length ? "UP" : below.length ? "DOWN" : "IDLE";
  }
}

function doAuto() {
  S.running = !S.running;
  const autoButton = document.getElementById("abtn");

  if (S.running) {
    autoButton.textContent = "Pause";
    autoButton.classList.add("action-primary");
    S.intv = setInterval(async () => {
      if (S.elevs.every((e) => !e.req.size)) {
        doAuto();
        return;
      }
      await doStep();
    }, 700);
  } else {
    autoButton.textContent = "Auto run";
    autoButton.classList.remove("action-primary");
    clearInterval(S.intv);
  }
}

async function doReset() {
  if (S.running) {
    doAuto();
  }

  if (S.useAPI) {
    try {
      const res = await fetch(API + "/reset");
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      setApiHealth("up", "Backend connected");
    } catch (err) {
      S.useAPI = false;
      setApiHealth("down", "Backend unavailable (running local simulation)");
    }
  }

  S = {
    elevs: [
      { id: 0, fl: 1, dir: "IDLE", req: new Set() },
      { id: 1, fl: 4, dir: "IDLE", req: new Set() },
      { id: 2, fl: 8, dir: "IDLE", req: new Set() }
    ],
    ext: new Set(),
    selE: null,
    logs: [],
    step: 0,
    intv: null,
    running: false,
    useAPI: S.useAPI
  };

  expl("Reset! Lifts back to floors 1, 4, 8.");
  addLog("System reset", "info");
  render();
}

function addLog(m, t) {
  S.logs.unshift({ m, t, s: S.step });
  if (S.logs.length > 60) {
    S.logs.pop();
  }
}

function expl(text) {
  document.getElementById("expl").textContent = text;
}
