# Elevator LLD

Minimal elevator system with:
- Java backend (dispatch + state)
- HTML/CSS/JS frontend (interactive UI)

## Architecture

Java (port 8080) <-> HTTP/REST <-> UI (port 3000)

Backend: Java API server (current implementation uses `ElevatorHttpServer` in `Main`)

Frontend: Browser UI calls API and animates elevator state

## Java Backend Working Flow

1. User clicks UP/DOWN in UI.
2. Frontend sends request to Java API.
3. Java controller/service applies SCAN dispatch logic.
4. API returns latest elevator statuses as JSON.
5. UI updates and animates cars/floor states.

Example request flow:

- Call floor request -> `GET /call?floor=5&dir=UP`
- Internal request -> `GET /internal?elevId=0&floor=7`
- Step simulation -> `GET /step`
- Poll status -> `GET /status`

## Run (One Command)

```bash
./start.sh
```

This script:
- compiles Java
- starts backend on 8080
- starts frontend on 3000

## URLs

- Frontend: http://localhost:3000
- Backend health/status: http://localhost:8080/status

## Run (Manual)

Backend:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.lld.elevator.app.Main
```

Frontend:

```bash
cd ui
python3 -m http.server 3000
```

## API Endpoints

- `GET /status` -> list all elevator states
- `GET /call?floor=<n>&dir=UP|DOWN` -> external floor call
- `GET /internal?elevId=<id>&floor=<n>` -> internal cab button
- `GET /step` -> advance one simulation step
- `GET /reset` -> reset elevators to initial state
