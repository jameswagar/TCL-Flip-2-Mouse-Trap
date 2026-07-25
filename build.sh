#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="$HOME/Library/Android/sdk"
BT="$SDK/build-tools/35.0.0"
ANDROID_JAR="/opt/homebrew/share/android-commandlinetools/platforms/android-34/android.jar"
JAVA_HOME="/opt/homebrew/opt/openjdk"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
OUT="$ROOT/build"
KEYSTORE="${MOUSE_TRAP_KEYSTORE:-$ROOT/mousetrap-release.jks}"
KEY_ALIAS="${MOUSE_TRAP_KEY_ALIAS:-mousetrap}"
: "${MOUSE_TRAP_STOREPASS:?Set MOUSE_TRAP_STOREPASS to build or sign the APK}"
MOUSE_TRAP_KEYPASS="${MOUSE_TRAP_KEYPASS:-$MOUSE_TRAP_STOREPASS}"

[[ -f "$KEYSTORE" ]] || {
  echo "Signing keystore not found: $KEYSTORE" >&2
  echo "Set MOUSE_TRAP_KEYSTORE to the established release key or a separately created development key." >&2
  exit 2
}

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

"$BT/aapt2" compile --dir "$ROOT/res" -o "$OUT/resources.zip"

JAVA_FILES=()
while IFS= read -r file; do
  JAVA_FILES+=("$file")
done < <(find "$ROOT/stubs" "$ROOT/src" -name '*.java' -print)

"$JAVA_HOME/bin/javac" -source 8 -target 8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$OUT/classes" \
  "${JAVA_FILES[@]}"

# DEX only Mouse Trap's classes. Xposed stubs are compile-only and must never enter the APK.
CLASS_FILES=()
while IFS= read -r file; do
  CLASS_FILES+=("$file")
done < <(find "$OUT/classes/com/dumbphone/mousetrap" -name '*.class' -print)

"$BT/d8" --min-api 23 --lib "$ANDROID_JAR" --output "$OUT/dex" \
  "${CLASS_FILES[@]}"

"$BT/aapt2" link -o "$OUT/mousetrap-unsigned.apk" \
  -I "$ANDROID_JAR" --manifest "$ROOT/AndroidManifest.xml" \
  -A "$ROOT/assets" --min-sdk-version 23 --target-sdk-version 30 \
  "$OUT/resources.zip"

(
  cd "$OUT/dex"
  zip -q "$OUT/mousetrap-unsigned.apk" classes.dex
)

"$BT/zipalign" -f 4 "$OUT/mousetrap-unsigned.apk" "$OUT/mousetrap-aligned.apk"

MOUSE_TRAP_STOREPASS="$MOUSE_TRAP_STOREPASS" \
MOUSE_TRAP_KEYPASS="$MOUSE_TRAP_KEYPASS" \
  "$BT/apksigner" sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" \
  --ks-pass env:MOUSE_TRAP_STOREPASS --key-pass env:MOUSE_TRAP_KEYPASS \
  --out "$OUT/Mouse-Trap-v1.0.1.apk" "$OUT/mousetrap-aligned.apk"

"$BT/apksigner" verify --verbose --print-certs "$OUT/Mouse-Trap-v1.0.1.apk"
shasum -a 256 "$OUT/Mouse-Trap-v1.0.1.apk"
