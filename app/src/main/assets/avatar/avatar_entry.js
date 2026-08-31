// Yap & Render — native 3D ISL avatar engine.
//
// This is a framework-free port of components/avatar/AvatarPlayer.tsx. The React
// component and this file share the exact same procedural sign engine and the
// same animation tables (avatar/animations/**, avatar/textToGloss.js), so a sign
// that plays on the web plays identically inside the Android WebView. The
// "Human" avatar is a procedural face + warm armour layered on the ybot rig at
// runtime (avatar/humanLayer.js), matching the web build.
//
// Everything it needs is bundled as a local asset (see ./vendor). There is no
// CDN / network dependency: the previous build fetched three.js from a CDN and
// rendered a black frame whenever that request failed.

import * as THREE from './vendor/three.module.js'
import { GLTFLoader } from './vendor/GLTFLoader.js'
import * as words from './avatar/animations/words.js'
import * as alphabets from './avatar/animations/alphabets.js'
import { defaultPose } from './avatar/animations/defaultPose.js'
import { textToGloss } from './avatar/textToGloss.js'
import { createFaceLayer, applyWarmArmorColors } from './avatar/humanLayer.js'

const wordAnimations = words
const alphabetAnimations = alphabets

try { console.log('[avatar] engine module evaluated, three r' + THREE.REVISION) } catch (_) {}

// --- Native bridge -----------------------------------------------------------

function notifyState(state) {
  try {
    if (window.AndroidAvatarBridge && window.AndroidAvatarBridge.onStateChange) {
      window.AndroidAvatarBridge.onStateChange(state)
    }
  } catch (_) { /* bridge not attached (e.g. plain browser preview) */ }
}

function log() {
  try { console.log.apply(console, ['[avatar]'].concat([].slice.call(arguments))) } catch (_) {}
}

// --- Engine state -----------------------------------------------------------

const runtime = {
  animations: [],
  characters: [],
  pending: true,
  model: 'default',
  avatar: undefined,
  // Legacy hook referenced by a few animation tables. The queue is drained by
  // the render loop, so this only needs to exist.
  animate() {},
}

let mount = null
let scene = null
let camera = null
let renderer = null
let loader = null
let resizeObserver = null

let currentModel = 'default'
let currentAvatarObject = null
let speed = 1
let ready = false
let loadingModel = null // model name currently being fetched, or null
let disposed = false

let rafId = 0
let nextFrameAt = 0
let wasSigning = false

let lastPhrase = ''
let pendingPhrase = null // { phrase } queued while a model is still loading

// --- Bone resolution (ported) ---------------------------------------------

function getAvatarBone(avatar, boneName) {
  const compact = boneName.replace(/^mixamorig:?/, '')
  const candidates = [boneName, `mixamorig:${compact}`, compact]
  for (const candidate of candidates) {
    const bone = avatar.getObjectByName(candidate)
    if (bone) return bone
  }
  let resolved
  avatar.traverse((child) => {
    if (resolved || child.type !== 'Bone') return
    const normalized = (child.name.split('|').pop() || '').replace(/^mixamorig:?/, '')
    if (normalized === compact) resolved = child
  })
  return resolved
}

function captureRestQuaternions(avatar, model) {
  // Both avatars run on the same ybot rig, which ships a pure identity T-pose,
  // so composing each authored delta onto the captured rest quaternion
  // reproduces the pose. `model` kept for call-site compatibility.
  void model
  avatar.traverse((child) => {
    if (child.type !== 'Bone') return
    const userData = child.userData
    userData.restQuaternion = child.quaternion.clone()
    userData.gestureRestQuaternion = userData.restQuaternion.clone()
    userData.restPosition = child.position.clone()
    userData.restScale = child.scale.clone()
  })
}

function resolveInstructionForModel(boneName, axis, target, direction, model) {
  // Both avatars now run on the same ybot rig, so the authored YBot-coordinate
  // tables apply verbatim. Args kept as the single hook for translating an
  // authored instruction if another rig is ever added.
  void boneName; void axis; void model
  return [target, direction]
}

function resetAvatar(rt, model) {
  const avatar = rt.avatar
  if (!avatar) return

  rt.animations = []
  rt.characters = []
  avatar.traverse((child) => {
    if (child.type !== 'Bone') return
    const userData = child.userData
    if (userData.gestureRestQuaternion) child.quaternion.copy(userData.gestureRestQuaternion)
    if (userData.restPosition) child.position.copy(userData.restPosition)
    if (userData.restScale) child.scale.copy(userData.restScale)
    userData.delta = { x: 0, y: 0, z: 0 }
  })

  // Every word table is authored from defaultPose(). Snap that proven starting
  // pose before playback begins so a switch/reset never shows a partial pose.
  defaultPose(rt)
  for (const frame of rt.animations) {
    for (const [boneName, , axis, target, direction] of frame) {
      const bone = getAvatarBone(avatar, boneName)
      if (!bone) continue
      const userData = bone.userData
      const delta = userData.delta || (userData.delta = { x: 0, y: 0, z: 0 })
      const [resolvedTarget] = resolveInstructionForModel(boneName, axis, target, direction, model)
      delta[axis] = resolvedTarget
      const deltaQuaternion = new THREE.Quaternion().setFromEuler(new THREE.Euler(delta.x, delta.y, delta.z))
      const gestureRest = userData.gestureRestQuaternion || userData.restQuaternion
      bone.quaternion.copy(gestureRest ? gestureRest.clone().multiply(deltaQuaternion) : deltaQuaternion)
    }
  }
  rt.animations = []
  rt.characters = []
}

// True when every whitespace-separated token is already something the engine
// can play directly: a known word animation or a single A-Z fingerspell letter.
// Chips, quick-words and a future AI gloss layer hand us tokens like this, so we
// play them verbatim instead of round-tripping through textToGloss.
function looksPreTranslated(input) {
  const tokens = String(input || '').trim().toUpperCase().split(/\s+/).filter(Boolean)
  if (!tokens.length) return false
  return tokens.every((token) => (token in wordAnimations) || /^[A-Z]$/.test(token))
}

function enqueuePhrase(input, rt, append = false) {
  if (!append) {
    // Snap the rig back to neutral first. A sign the user interrupted mid-move
    // leaves bones partway through a gesture, and every sign is authored as a
    // delta from rest - without this the next sign starts from a half-finished
    // pose and can freeze on an unreachable target.
    if (rt.avatar) resetAvatar(rt, currentModel)
    rt.animations = []
  }

  const tokens = looksPreTranslated(input)
    ? input.trim().toUpperCase().split(/\s+/).filter(Boolean)
    : ((textToGloss(input) || {}).tokens || [])

  log('input:', input)
  log('gloss:', tokens.join(' + ') || '(none)')

  for (const token of tokens) {
    const word = String(token).toUpperCase()
    const wordAnimation = wordAnimations[word]
    if (typeof wordAnimation === 'function') {
      wordAnimation(rt)
      continue
    }
    for (const letter of word) {
      const letterAnimation = alphabetAnimations[letter]
      if (typeof letterAnimation === 'function') letterAnimation(rt)
    }
  }
  return rt.animations.length
}

// --- Scene ----------------------------------------------------------------

function sizeOf(el) {
  const rect = el.getBoundingClientRect()
  const width = rect.width || el.clientWidth || window.innerWidth
  const height = rect.height || el.clientHeight || window.innerHeight
  return { width, height }
}

function handleResize() {
  if (!mount || !renderer || !camera) return
  const { width, height } = sizeOf(mount)
  if (!width || !height) return
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height, false)
}

function buildScene() {
  scene = new THREE.Scene()
  scene.background = new THREE.Color('#faf8f2')

  // Frame the avatar from head through hips, holding steady as the canvas
  // widens on larger screens.
  camera = new THREE.PerspectiveCamera(31, 1, 0.1, 1000)
  camera.position.set(0, 0.62, 3)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, powerPreference: 'high-performance' })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFShadowMap
  renderer.outputColorSpace = THREE.SRGBColorSpace
  mount.appendChild(renderer.domElement)

  scene.add(new THREE.HemisphereLight('#fff9ed', '#8f9e82', 2.4))
  const keyLight = new THREE.DirectionalLight('#ffffff', 3.6)
  keyLight.position.set(3, 5, 4)
  keyLight.castShadow = true
  scene.add(keyLight)
  const rimLight = new THREE.PointLight('#ef714f', 15, 8)
  rimLight.position.set(-2.8, 2, 2)
  scene.add(rimLight)

  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(handleResize)
    resizeObserver.observe(mount)
  }
  window.addEventListener('resize', handleResize)
  handleResize()
}

function disposeObject(object) {
  object.traverse((child) => {
    if (child.geometry) child.geometry.dispose()
    const material = child.material
    if (Array.isArray(material)) material.forEach((m) => m && m.dispose())
    else if (material) material.dispose()
  })
}

function loadAvatar(model) {
  const target = model === 'human' ? 'human' : 'default'
  // Ignore duplicate requests for a load that is already in flight. Kotlin fires
  // setAvatar() from several places (onPageFinished, LaunchedEffect) on startup.
  if (loadingModel === target) return

  loadingModel = target
  currentModel = target
  runtime.model = currentModel
  runtime.animations = []
  runtime.characters = []
  runtime.avatar = undefined
  ready = false
  notifyState('loading')

  // Both avatars are the same ybot rig. "Human" layers a procedural face and
  // warm-toned armour on top at runtime; the model file itself is untouched, so
  // every ISL sign plays identically on both.
  const modelPath = 'models/ybot.glb'
  loader.load(
    modelPath,
    (gltf) => {
      if (disposed) return
      loadingModel = null

      if (currentAvatarObject) {
        scene.remove(currentAvatarObject)
        disposeObject(currentAvatarObject)
        currentAvatarObject = null
      }

      const avatar = gltf.scene
      // Keep the GLB's own materials for "default" — YBot ships the two-tone
      // teal body + dark joints look. "human" recolours them below.
      let meshCount = 0
      avatar.traverse((child) => {
        child.castShadow = true
        child.frustumCulled = false
        if (child.isMesh) {
          meshCount++
          child.visible = true
          if (child.material) {
            child.material.side = THREE.FrontSide
            if ('envMapIntensity' in child.material) child.material.envMapIntensity = 0.6
          }
        }
      })

      runtime.avatar = avatar
      currentAvatarObject = avatar
      avatar.position.set(0, -0.85, 0)
      avatar.rotation.set(0, 0, 0)
      avatar.scale.setScalar(1)
      scene.add(avatar)

      if (currentModel === 'human') {
        // Non-destructive: recolours the existing robot mesh and parents a
        // procedural face onto the head bone. No vertices, bones or animation
        // instructions change, so ISL playback is identical to the default rig.
        applyWarmArmorColors(avatar)
        const headBone = getAvatarBone(avatar, 'mixamorig:Head')
        if (headBone) {
          const faceLayer = createFaceLayer()
          // Compensate for ybot's 0.01 rig scale and sit the head on the neck.
          faceLayer.scale.set(13.5, 13.5, 13.5)
          faceLayer.position.set(0, 2, 1)
          headBone.add(faceLayer)
        } else {
          log('human face layer skipped: head bone not found')
        }
      }

      const box = new THREE.Box3().setFromObject(avatar)
      const size = box.getSize(new THREE.Vector3())
      log('added', currentModel, 'meshes=', meshCount, 'size=',
        size.x.toFixed(2), size.y.toFixed(2), size.z.toFixed(2),
        'sceneChildren=', scene.children.length)

      captureRestQuaternions(avatar, currentModel)
      resetAvatar(runtime, currentModel)

      ready = true
      hideBoot()
      log('avatar ready:', currentModel)

      const queued = pendingPhrase
      if (queued) {
        pendingPhrase = null
        const frames = enqueuePhrase(queued.phrase, runtime, false)
        notifyState(frames > 0 ? 'signing' : 'ready')
      } else {
        notifyState('ready')
      }
    },
    undefined,
    (error) => {
      loadingModel = null
      log('GLB load failed for', modelPath, error && (error.message || error))
      hideBoot()
      notifyState('ready')
    },
  )
}

let bootHidden = false
function hideBoot() {
  if (bootHidden) return
  bootHidden = true
  const boot = document.getElementById('avatar-boot')
  if (boot) {
    boot.classList.add('hidden')
    setTimeout(() => boot && boot.parentNode && boot.parentNode.removeChild(boot), 600)
  }
}

// --- Render loop --------------------------------------------------------------

function tick(now) {
  rafId = requestAnimationFrame(tick)
  if (!renderer || !scene || !camera) return

  const queue = runtime.animations
  if (runtime.avatar && queue.length && now >= nextFrameAt) {
    wasSigning = true
    const frame = queue[0]
    for (let index = 0; index < frame.length; ) {
      const [boneName, , axis, target, direction] = frame[index]
      const bone = getAvatarBone(runtime.avatar, boneName)
      if (!bone) {
        frame.splice(index, 1)
        continue
      }
      const userData = bone.userData
      const delta = userData.delta || (userData.delta = { x: 0, y: 0, z: 0 })
      const [resolvedTarget, resolvedDirection] = resolveInstructionForModel(boneName, axis, target, direction, currentModel)
      const value = delta[axis]
      // Step size drives how fast a bone eases toward its authored target. Too
      // small and long multi-stage signs (HELLO, SORRY, the meal compounds)
      // drag; too large and the shared raised-hand prep pose makes different
      // signs look identical before their distinct movement is visible. 0.05
      // keeps each pose readable while roughly halving the big signs' runtime.
      const step = 0.05 * speed
      const inProgress = resolvedDirection === '+' ? value < resolvedTarget : value > resolvedTarget
      if (inProgress) {
        delta[axis] = resolvedDirection === '+'
          ? Math.min(value + step, resolvedTarget)
          : Math.max(value - step, resolvedTarget)
        const deltaQuaternion = new THREE.Quaternion().setFromEuler(new THREE.Euler(delta.x, delta.y, delta.z))
        const gestureRest = userData.gestureRestQuaternion || userData.restQuaternion
        bone.quaternion.copy(gestureRest ? gestureRest.clone().multiply(deltaQuaternion) : deltaQuaternion)
        index += 1
      } else {
        frame.splice(index, 1)
      }
    }
    if (!frame.length) {
      queue.shift()
      // Brief hold on each authored key pose so the movement still reads,
      // without the dead air that made every sign feel sluggish (was 360).
      nextFrameAt = now + 170 / speed
    }
  }

  if (wasSigning && !queue.length) {
    wasSigning = false
    notifyState('ready')
  }

  renderer.render(scene, camera)
}

// --- Public API (called from Avatar3DWebView.kt) ---------------------------

window.setAvatar = function setAvatar(name) {
  const model = name === 'human' ? 'human' : 'default'
  if (model === currentModel && ready) return
  // Preserve whatever is on screen: replay it once the new rig is ready.
  if (lastPhrase) pendingPhrase = { phrase: lastPhrase }
  loadAvatar(model)
}

window.setSpeed = function setSpeed(value) {
  const parsed = Number(value)
  if (Number.isFinite(parsed) && parsed > 0) speed = parsed
}

window.playPhrase = function playPhrase(phrase) {
  const text = String(phrase == null ? '' : phrase).trim()
  if (!text) return
  lastPhrase = text
  if (!ready) {
    pendingPhrase = { phrase: text }
    notifyState('loading')
    return
  }
  const frames = enqueuePhrase(text, runtime, false)
  notifyState(frames > 0 ? 'signing' : 'ready')
}

window.replayAnimation = function replayAnimation() {
  if (!lastPhrase) return
  window.playPhrase(lastPhrase)
}

window.stopAnimation = function stopAnimation() {
  pendingPhrase = null
  runtime.animations = []
  runtime.characters = []
  wasSigning = false
  if (ready) resetAvatar(runtime, currentModel)
  notifyState('ready')
}

// --- Boot -----------------------------------------------------------------

function boot() {
  mount = document.getElementById('avatar-container')
  if (!mount) return

  try {
    loader = new GLTFLoader()
    buildScene()
    log('scene built, renderer =', renderer && renderer.domElement ? 'ok' : 'missing')
    rafId = requestAnimationFrame(tick)
    // First model. Default = the teal YBot robot.
    loadAvatar('default')
  } catch (error) {
    log('fatal init error', error && (error.stack || error.message || error))
    hideBoot()
    notifyState('ready')
  }
}

window.addEventListener('beforeunload', () => {
  disposed = true
  cancelAnimationFrame(rafId)
  if (resizeObserver) resizeObserver.disconnect()
  window.removeEventListener('resize', handleResize)
  if (renderer) renderer.dispose()
})

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', boot)
} else {
  boot()
}
