#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$ROOT_DIR/out"
UI_DIR="$ROOT_DIR/ui"
BACKEND_PORT=8080
FRONTEND_PORT=3000

cleanup() {
  echo
  echo "Stopping services..."
  if [[ -n "${BACKEND_PID:-}" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
  if [[ -n "${FRONTEND_PID:-}" ]] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
  wait 2>/dev/null || true
  echo "Stopped."
}

trap cleanup EXIT INT TERM

if ! command -v javac >/dev/null 2>&1; then
  echo "Error: javac not found. Install JDK first."
  exit 1
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Error: java not found. Install JRE/JDK first."
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "Error: python3 not found. Install Python 3 first."
  exit 1
fi

echo "Compiling Java backend..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$ROOT_DIR/src/main/java" -name "*.java" -print0 | xargs -0 javac -d "$OUT_DIR"

echo "Starting Java backend on http://localhost:$BACKEND_PORT ..."
(
  cd "$ROOT_DIR"
  java -cp "$OUT_DIR" com.lld.elevator.app.Main
) &
BACKEND_PID=$!

sleep 1
if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
  echo "Error: backend failed to start."
  exit 1
fi

echo "Starting frontend on http://localhost:$FRONTEND_PORT ..."
(
  cd "$UI_DIR"
  python3 -m http.server "$FRONTEND_PORT"
) &
FRONTEND_PID=$!

sleep 1
if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
  echo "Error: frontend failed to start."
  exit 1
fi

echo
echo "Backend:  http://localhost:$BACKEND_PORT"
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Press Ctrl+C to stop both."

wait "$BACKEND_PID" "$FRONTEND_PID"
