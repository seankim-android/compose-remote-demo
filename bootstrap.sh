#!/usr/bin/env bash
# compose-remote-starter bootstrap
# Walks you through the first-time setup. Doesn't pretend to be magic:
# Ktor and Android Studio both have their own generators, and pinning
# versions in this repo would just rot. So we drive those, then verify.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$ROOT/.bootstrap.env"

cmd="${1:-init}"

ask() {
  local prompt="$1" default="$2" var
  read -r -p "$prompt [$default]: " var || true
  echo "${var:-$default}"
}

have() { command -v "$1" >/dev/null 2>&1; }

# Look for Android Studio in the usual macOS spots, including JetBrains Toolbox.
find_studio() {
  local candidates=(
    "$HOME/Applications/Android Studio.app"
    "/Applications/Android Studio.app"
    "$HOME/Applications/JetBrains Toolbox/Android Studio.app"
  )
  # Toolbox versioned installs: ~/Library/Application Support/JetBrains/Toolbox/apps/AndroidStudio/...
  local toolbox="$HOME/Library/Application Support/JetBrains/Toolbox/apps"
  if [ -d "$toolbox" ]; then
    while IFS= read -r p; do candidates+=("$p"); done < <(find "$toolbox" -maxdepth 5 -name "Android Studio*.app" 2>/dev/null)
  fi
  for c in "${candidates[@]}"; do
    [ -d "$c" ] && { echo "$c"; return 0; }
  done
  return 1
}

# adb usually lives under the Android SDK, not on PATH.
find_adb() {
  have adb && { command -v adb; return 0; }
  local sdk_candidates=(
    "${ANDROID_HOME:-}"
    "${ANDROID_SDK_ROOT:-}"
    "$HOME/Library/Android/sdk"
    "$HOME/Android/Sdk"
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
    echo "Install those first, then re-run."
    exit 1
  fi
}

cmd_init() {
  check_prereqs

  echo "Let's pick a few defaults. You can change them later."
  pkg=$(ask "Android/server package" "com.example.composeremote")
  port=$(ask "Server port" "8080")
  emu_host=$(ask "Server host as seen from the emulator" "10.0.2.2")

  cat > "$ENV_FILE" <<EOF
PACKAGE=$pkg
PORT=$port
EMU_HOST=$emu_host
EOF
  echo "Wrote $ENV_FILE"

  echo
  echo "Next, two things this script can't do for you:"
  echo
  echo "1) Generate the Ktor server."
  echo "   Open: https://start.ktor.io/?name=server&package=$pkg"
  echo "   Plugins: Routing, ContentNegotiation, kotlinx.serialization"
  echo "   Unzip into ./server/ (so server/build.gradle.kts exists)."
  echo
  echo "2) Generate the Android app."
  if studio=$(find_studio); then
    echo "   Found Android Studio: $studio"
    echo "   New Project → Empty Activity (Compose) → location: $ROOT/android/  (package: $pkg)"
    reply=$(ask "   Open Android Studio now? (y/N)" "n")
    case "$reply" in
      y|Y|yes) open -a "$studio" "$ROOT/android" ;;
    esac
  else
    echo "   Android Studio → New Project → Empty Activity (Compose) → location: ./android/"
    echo "   Use package: $pkg"
  fi

  if adb_path=$(find_adb); then
    echo
    echo "(adb found at $adb_path)"
  fi

  echo
  echo "When both are in place, run: ./bootstrap.sh verify"
}

cmd_verify() {
  [ -f "$ENV_FILE" ] || { echo "run ./bootstrap.sh init first"; exit 1; }
  # shellcheck disable=SC1090
  . "$ENV_FILE"

  fail=0
  if [ ! -f "$ROOT/server/build.gradle.kts" ] && [ ! -f "$ROOT/server/build.gradle" ]; then
    echo "✗ server/ has no Gradle build. Drop the Ktor generator output here."
    fail=1
  else
    echo "✓ server/ looks scaffolded"
  fi

  if [ ! -d "$ROOT/android/app" ] && [ ! -f "$ROOT/android/settings.gradle.kts" ] && [ ! -f "$ROOT/android/settings.gradle" ]; then
    echo "✗ android/ doesn't look like an Android Studio project yet."
    fail=1
  else
    echo "✓ android/ looks scaffolded"
  fi

  [ $fail -eq 0 ] || exit 1

  echo
  echo "All set. To run the server:"
  echo "  cd server && ./gradlew run"
  echo
  echo "Then open ./android in Android Studio and hit Run."
  echo "The app will hit http://$EMU_HOST:$PORT from the emulator."
}

cmd_run_server() {
  [ -f "$ROOT/server/build.gradle.kts" ] || { echo "server/ not scaffolded yet"; exit 1; }
  cd "$ROOT/server" && ./gradlew run
}

case "$cmd" in
  init)        cmd_init ;;
  verify)      cmd_verify ;;
  run-server)  cmd_run_server ;;
  *) echo "usage: $0 [init|verify|run-server]"; exit 1 ;;
esac
