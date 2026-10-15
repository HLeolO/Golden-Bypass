#!/usr/bin/env bash
set -euo pipefail

# Gera keystore de debug se nao existir
if [ ! -f debug.keystore ]; then
  keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
    -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

# Build release
./gradlew assembleRelease

APK="app/build/outputs/apk/release/app-release.apk"
OUT="GoldenBypass-signed.apk"

# Assina
apksigner sign --ks debug.keystore --ks-pass pass:android \
  --out "$OUT" "$APK"

echo "APK assinado: $OUT"
echo "Copie $OUT e gh-pages/index.html para o repositorio GitHub Pages."
