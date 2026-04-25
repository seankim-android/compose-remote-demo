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

step_server() {
  cat <<EOF

──────────────────────────────────────────────
Step 1 of 2: generate the Ktor server
──────────────────────────────────────────────
On the page that just opened (start.ktor.io):

  1. Project name: server
  2. Package:      $PACKAGE
  3. Add plugins:  Routing, ContentNegotiation, kotlinx.serialization
  4. Click "Download" → you get a zip.
  5. Unzip its contents into:
       $ROOT/server/
     so that this file exists:
       $ROOT/server/build.gradle.kts

EOF
}

step_android() {
  cat <<EOF

──────────────────────────────────────────────
Step 2 of 2: generate the Android app
──────────────────────────────────────────────
In Android Studio:

  1. New Project → "Empty Activity" (the Compose one)
  2. Package:  $PACKAGE
  3. Save location:
       $ROOT/android
     (overwrite/use existing folder if prompted)
  4. Let Gradle sync finish.

EOF
}

wait_for() {
  local label="$1" check="$2"
  echo "Waiting for $label to appear (Ctrl-C to stop)..."
  while ! eval "$check"; do sleep 3; done
  echo "✓ $label ready"
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

# One-shot: init + walk both generator steps in order + verify.
cmd_all() {
  cmd_init
  # shellcheck disable=SC1090
  . "$ENV_FILE"

  if server_ready; then
    echo "✓ server/ already scaffolded, skipping Ktor step"
  else
    step_server
    echo "Opening start.ktor.io in your browser..."
    open_url "$(ktor_url "$PACKAGE")"
    [ "${BOOTSTRAP_NOWAIT:-}" = "1" ] || wait_for "server/" "server_ready"
  fi

  if android_ready; then
    echo "✓ android/ already scaffolded, skipping Studio step"
  else
    step_android
    if studio=$(find_studio); then
      reply=$(ask "Open Android Studio now? (Y/n)" "y")
      case "$reply" in n|N|no) ;; *) mkdir -p "$ROOT/android" && open -a "$studio" "$ROOT/android" ;; esac
    else
      echo "(Android Studio not auto-detected. Open it and create the project at the path above.)"
    fi
    [ "${BOOTSTRAP_NOWAIT:-}" = "1" ] || wait_for "android/" "android_ready"
  fi

  echo
  cmd_verify
}

case "$cmd" in
  all)         cmd_all ;;
  init)        cmd_init; . "$ENV_FILE"; step_server; step_android ;;
  verify)      cmd_verify ;;
  run-server)  cmd_run_server ;;
  *) echo "usage: $0 [all|init|verify|run-server]"; exit 1 ;;
esac
