#!/usr/bin/env bash
# compose-remote-starter bootstrap
# Walks you through the first-time setup. Doesn't pretend to be magic:
# Ktor and Android Studio both have their own generators, and pinning
# versions in this repo would just rot. So we drive those, then verify.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$ROOT/.bootstrap.env"

cmd="${1:-all}"

ask() {
  local prompt="$1" default="$2" var=""
  if [ -t 0 ]; then
    read -r -p "$prompt [$default]: " var || true
  else
    { read -r -p "$prompt [$default]: " var </dev/tty; } 2>/dev/null || true
  fi
  echo "${var:-$default}"
}

have() { command -v "$1" >/dev/null 2>&1; }

find_studio() {
  local candidates=(
    "$HOME/Applications/Android Studio.app"
    "/Applications/Android Studio.app"
    "$HOME/Applications/JetBrains Toolbox/Android Studio.app"
  )
  local toolbox="$HOME/Library/Application Support/JetBrains/Toolbox/apps"
  if [ -d "$toolbox" ]; then
    while IFS= read -r p; do candidates+=("$p"); done < <(find "$toolbox" -maxdepth 5 -name "Android Studio*.app" 2>/dev/null)
  fi
  for c in "${candidates[@]}"; do
    [ -d "$c" ] && { echo "$c"; return 0; }
  done
  return 1
}

find_adb() {
  have adb && { command -v adb; return 0; }
  local sdk_candidates=(
    "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}"
    "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"
  )
  for s in "${sdk_candidates[@]}"; do
    [ -n "$s" ] && [ -x "$s/platform-tools/adb" ] && { echo "$s/platform-tools/adb"; return 0; }
  done
  return 1
}

check_prereqs() {
  local missing=()
  have java || missing+=("java (JDK 17+)")
  have curl || missing+=("curl")
  if [ ${#missing[@]} -gt 0 ]; then
    echo "Missing: ${missing[*]}"
    exit 1
  fi
}

server_ready() { [ -f "$ROOT/server/build.gradle.kts" ] || [ -f "$ROOT/server/build.gradle" ]; }
android_ready() { [ -d "$ROOT/android/app" ] || [ -f "$ROOT/android/settings.gradle.kts" ] || [ -f "$ROOT/android/settings.gradle" ]; }

ktor_url() { echo "https://start.ktor.io/?name=server&package=$1"; }

open_url() {
  if have open; then open "$1"
  elif have xdg-open; then xdg-open "$1" >/dev/null 2>&1 || true
  fi
}

cmd_init() {
  check_prereqs

  echo "Pick a few defaults (Enter to accept):"
  pkg=$(ask "  package" "com.example.composeremote")
  port=$(ask "  server port" "8080")
  emu_host=$(ask "  server host from emulator" "10.0.2.2")

  cat > "$ENV_FILE" <<EOF
PACKAGE=$pkg
PORT=$port
EMU_HOST=$emu_host
EOF
  echo "wrote $ENV_FILE"
}

print_steps() {
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  cat <<EOF

Step 1 — generate the Ktor server (Routing, ContentNegotiation, kotlinx.serialization):

    open "$(ktor_url "$PACKAGE")"
    # download → unzip into ./server/

Step 2 — generate the Android app:

    Android Studio → New Project → Empty Activity (Compose)
      Location: $ROOT/android
      Package:  $PACKAGE

Step 3 — verify and run:

    ./bootstrap.sh verify
    ./bootstrap.sh run-server

EOF
}

cmd_verify() {
  [ -f "$ENV_FILE" ] || { echo "run ./bootstrap.sh init first"; exit 1; }
  # shellcheck disable=SC1090
  . "$ENV_FILE"

  local fail=0
  if server_ready; then echo "✓ server/ scaffolded"; else echo "✗ server/ missing Gradle build (drop Ktor output here)"; fail=1; fi
  if android_ready; then echo "✓ android/ scaffolded"; else echo "✗ android/ not an Android Studio project yet"; fail=1; fi
  [ $fail -eq 0 ] || exit 1

  cat <<EOF

All set. App will hit http://$EMU_HOST:$PORT from the emulator.
Run the server:  ./bootstrap.sh run-server
EOF
}

cmd_run_server() {
  server_ready || { echo "server/ not scaffolded yet — run ./bootstrap.sh verify"; exit 1; }
  cd "$ROOT/server" && ./gradlew run
}

# One-shot: init + open generators + poll until both sides are scaffolded.
cmd_all() {
  cmd_init
  # shellcheck disable=SC1090
  . "$ENV_FILE"

  echo
  if server_ready; then
    echo "✓ server/ already scaffolded, skipping Ktor generator"
  else
    echo "Opening Ktor generator..."
    open_url "$(ktor_url "$PACKAGE")"
  fi

  if android_ready; then
    echo "✓ android/ already scaffolded, skipping Studio"
  else
    if studio=$(find_studio); then
      reply=$(ask "Open Android Studio on $ROOT/android now? (Y/n)" "y")
      case "$reply" in n|N|no) ;; *) mkdir -p "$ROOT/android" && open -a "$studio" "$ROOT/android" ;; esac
    else
      echo "Android Studio not found. Open it manually and create an Empty Activity (Compose) project at:"
      echo "  $ROOT/android  (package: $PACKAGE)"
    fi
  fi

  print_steps

  if [ "${BOOTSTRAP_NOWAIT:-}" = "1" ]; then return 0; fi

  echo "Watching for scaffolding to land (Ctrl-C to stop)..."
  local s_done=0 a_done=0
  while [ $s_done -eq 0 ] || [ $a_done -eq 0 ]; do
    if [ $s_done -eq 0 ] && server_ready; then echo "✓ server/ ready"; s_done=1; fi
    if [ $a_done -eq 0 ] && android_ready; then echo "✓ android/ ready"; a_done=1; fi
    [ $s_done -eq 1 ] && [ $a_done -eq 1 ] && break
    sleep 3
  done

  echo
  cmd_verify
}

case "$cmd" in
  all)         cmd_all ;;
  init)        cmd_init && print_steps ;;
  verify)      cmd_verify ;;
  run-server)  cmd_run_server ;;
  *) echo "usage: $0 [all|init|verify|run-server]"; exit 1 ;;
esac
