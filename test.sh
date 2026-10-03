#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ANDROID_JAR="/opt/homebrew/share/android-commandlinetools/platforms/android-34/android.jar"
JAVA_HOME="/opt/homebrew/opt/openjdk"
OUT="$ROOT/build/test-classes"
rm -rf "$OUT"
mkdir -p "$OUT"
"$JAVA_HOME/bin/javac" -source 8 -target 8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$OUT" \
  "$ROOT/src/com/dumbphone/mousetrap/HookTargetResolver.java" \
  "$ROOT/tests/com/dumbphone/mousetrap/HookTargetResolverTest.java"
"$JAVA_HOME/bin/java" -cp "$OUT:$ANDROID_JAR" com.dumbphone.mousetrap.HookTargetResolverTest
