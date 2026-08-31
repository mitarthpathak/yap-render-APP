# Avatar attribution

`ybot.glb` is the "Y Bot" rig from [Adobe Mixamo](https://www.mixamo.com/),
used under the Mixamo license (free for commercial and non-commercial use).

Both avatars in the app ("Default" and "Human") load this single file. The
"Human" look is produced entirely at runtime: a procedural face built from
Three.js primitives (`avatar/humanLayer.js`) parented to the head bone, plus
warm per-vertex armour colours (`applyWarmArmorColors` in the same file). The
model file is never modified, so every ISL sign plays identically on both.

The previous "brunette" Ready Player Me avatar (`brunette.glb`, CC BY-NC 4.0)
was removed on 2026-08-28.
