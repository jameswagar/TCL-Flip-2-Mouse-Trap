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
KEYSTORE="$ROOT/mousetrap-release.jks"

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

"$BT/aapt2" compile --dir "$ROOT/res" -o "$OUT/resources.zip"

"$JAVA_HOME/bin/javac" -source 8 -target 8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$OUT/classes" \
  $(find "$ROOT/stubs" "$ROOT/src" -name '*.java' -print)

# DEX only Mouse Trap's classes. Xposed stubs are compile-only and must never enter the APK.
"$BT/d8" --min-api 23 --lib "$ANDROID_JAR" --output "$OUT/dex" \
  $(find "$OUT/classes/com/dumbphone/mousetrap" -name '*.class' -print)

"$BT/aapt2" link -o "$OUT/mousetrap-unsigned.apk" \
  -I "$ANDROID_JAR" --manifest "$ROOT/AndroidManifest.xml" \
  -A "$ROOT/assets" --min-sdk-version 23 --target-sdk-version 30 \
  "$OUT/resources.zip"

(
  cd "$OUT/dex"
  zip -q "$OUT/mousetrap-unsigned.apk" classes.dex
)

"$BT/zipalign" -f 4 "$OUT/mousetrap-unsigned.apk" "$OUT/mousetrap-aligned.apk"

if [[ ! -f "$KEYSTORE" ]]; then
  "$JAVA_HOME/bin/keytool" -genkeypair -noprompt \
    -keystore "$KEYSTORE" -storepass:env MOUSE_TRAP_STOREPASS -keypass:env MOUSE_TRAP_KEYPASS \
    -alias mousetrap -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Mouse Trap, OU=Dumbphone, O=Local, C=US"
fi

"$BT/apksigner" sign --ks "$KEYSTORE" \
  --ks-pass env:MOUSE_TRAP_STOREPASS --key-pass env:MOUSE_TRAP_KEYPASS \
  --out "$OUT/Mouse-Trap-v1.2.0.apk" "$OUT/mousetrap-aligned.apk"

"$BT/apksigner" verify --verbose --print-certs "$OUT/Mouse-Trap-v1.2.0.apk"
shasum -a 256 "$OUT/Mouse-Trap-v1.2.0.apk"
