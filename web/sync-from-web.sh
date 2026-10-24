#!/usr/bin/env bash
# Copies the web app (github.com/mitarthpathak/yap-render) into this Android
# repo: source into web/src, trained models and MediaPipe runtime straight into
# the Android assets. Run `npm run build` afterwards to rebuild the bundles.
#
#   bash web/sync-from-web.sh [path-to-yap-render-checkout]   (default: ../yap-render)
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
WEB="${1:-$ROOT/../yap-render}"
WEB="$(cd "$WEB" && pwd)"
[ -f "$WEB/components/avatar/AvatarPlayer.tsx" ] || { echo "Not a yap-render checkout: $WEB" >&2; exit 1; }

SRC="$HERE/src"
ASSETS="$ROOT/app/src/main/assets"

rm -rf "$SRC"
mkdir -p "$SRC/app/sign/train" "$SRC/app/sign/validate"
cp -R "$WEB/components" "$WEB/lib" "$SRC/"
cp "$WEB/app/globals.css" "$WEB/app/sign.css" "$WEB/app/sign-story.css" "$SRC/app/"
cp "$WEB/app/sign/train/training.css" "$SRC/app/sign/train/"
cp "$WEB/app/sign/validate/validation.css" "$SRC/app/sign/validate/"

# Sign recognition data and the avatar rig.
rm -rf "$ASSETS/sign/models" "$ASSETS/sign/mediapipe"
mkdir -p "$ASSETS/sign/models" "$ASSETS/sign/mediapipe/wasm" "$ASSETS/avatar/models"
cp "$WEB"/public/models/*.json "$WEB"/public/models/*.bin "$WEB"/public/models/*.md "$ASSETS/sign/models/"
cp "$WEB/public/models/ybot.glb" "$WEB/public/models/ATTRIBUTION.md" "$ASSETS/avatar/models/"
# Hand, pose and face models. Only the classic-script WASM builds are copied:
# the trackers never request the ES-module variant.
cp "$WEB"/public/mediapipe/*.task "$WEB"/public/mediapipe/*.md "$ASSETS/sign/mediapipe/"
cp "$WEB"/public/mediapipe/wasm/vision_wasm_internal.* "$WEB"/public/mediapipe/wasm/vision_wasm_nosimd_internal.* "$ASSETS/sign/mediapipe/wasm/"

git -C "$WEB" rev-parse HEAD > "$HERE/UPSTREAM_COMMIT"
echo "Synced from $WEB @ $(cat "$HERE/UPSTREAM_COMMIT")"
