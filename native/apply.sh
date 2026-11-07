#!/usr/bin/env bash
# Copies the native app features into the generated Android project.
set -euo pipefail
cd "$(dirname "$0")/.."

APP=android/app
JAVA_DIR="$APP/src/main/java/com/chaloje/app"

cp native/MainActivity.java "$JAVA_DIR/MainActivity.java"
mkdir -p "$APP/src/main/res/drawable"
cp native/drawable/*.xml "$APP/src/main/res/drawable/"

# Pull-to-refresh library
if ! grep -q swiperefreshlayout "$APP/build.gradle"; then
  sed -i '/^dependencies {/a\    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"' "$APP/build.gradle"
fi

echo "Native features applied"
grep -n "swiperefreshlayout" "$APP/build.gradle"
