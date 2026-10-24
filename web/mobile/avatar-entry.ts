// Yap & Render — 3D ISL avatar engine for the Android WebView.
//
// A framework-free port of the web app's components/avatar/AvatarPlayer.tsx.
// It imports the web's own sign tables, gloss engine (English, Hindi and
// digits), human layer and sign queue from web/src, so a sign plays exactly as
// it does on the web. `npm run build` bundles this file and three.js into
// app/src/main/assets/avatar/avatar_bundle.js; there is no CDN dependency.
//
// Avatar3DWebView.kt drives it through the window.* functions at the bottom
// and listens for state changes on window.AndroidAvatarBridge.

import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import * as words from '@/components/avatar/animations/words'
import * as alphabets from '@/components/avatar/animations/alphabets'
import { NUMBER_ANIMATIONS } from '@/components/avatar/animations/numbers'
import { defaultPose } from '@/components/avatar/animations/defaultPose'
import { textToGloss, trailingPhrasePrefixLength } from '@/components/avatar/textToGloss'
import { createFaceLayer } from '@/components/avatar/humanLayer'
import { advanceFrame, appendSignFrames, stepForElapsed, type SignRuntimeLike } from '@/components/avatar/signQueue'
import { looksPreTranslated } from '@/lib/islVocabulary'
import { planLiveCommit } from '@/lib/live-speech'

type AvatarModel = 'default' | 'human'
type AvatarState = 'loading' | 'ready' | 'signing' | 'error'
type SignSegment = { token: string; frames: number }
type SignRuntime = SignRuntimeLike & { segments: SignSegment[]; avatar?: THREE.Object3D }
type PendingTokens = { tokens: string[]; append: boolean }
type BoneUserData = {
  restQuaternion?: THREE.Quaternion
  gestureRestQuaternion?: THREE.Quaternion
  restPosition?: THREE.Vector3
  restScale?: THREE.Vector3
  delta?: { x: number; y: number; z: number }
}

declare global {
  interface Window {
    AndroidAvatarBridge?: { onStateChange?: (state: string) => void }
    setAvatar: (name: string) => void
    setSpeed: (value: number | string) => void
    playPhrase: (phrase: string) => void
    appendPhrase: (phrase: string) => void
    replayAnimation: () => void
    stopAnimation: () => void
    resetAvatar: () => void
    liveSpeech: (transcript: string, isFinal: boolean) => void
    resetLive: () => void
  }
}

const wordAnimations = words as unknown as Record<string, (runtime: SignRuntimeLike) => void>
const alphabetAnimations = alphabets as unknown as Record<string, (runtime: SignRuntimeLike) => void>
const numberAnimations = NUMBER_ANIMATIONS as unknown as Record<string, (runtime: SignRuntimeLike) => void>
// Live speech can arrive faster than signs play. Beyond this many queued signs
// the oldest waiting ones are dropped so the avatar stays close to the speaker.
const MAX_LIVE_BACKLOG = 8

function log(...args: unknown[]) {
  try { console.log('[avatar]', ...args) } catch { /* console unavailable */ }
}

function notifyState(state: AvatarState) {
  try { window.AndroidAvatarBridge?.onStateChange?.(state) } catch { /* bridge not attached (plain browser preview) */ }
}

// --- Engine state ------------------------------------------------------------

const runtime: SignRuntime = { animations: [], segments: [], characters: [], pending: true }
let mount: HTMLElement | null = null
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer | null = null
const loader = new GLTFLoader()

let model: AvatarModel = 'default'
let loadingModel: AvatarModel | null = null
let loadGeneration = 0
let speed = 1
let ready = false
let needsRender = true
let pending: PendingTokens[] = []
let lastTokens: string[] = []
let liveCommitted: string[] = []
const bones = new Map<string, THREE.Object3D | undefined>()

// --- Bones (same as AvatarPlayer.tsx) ------------------------------------------

function getAvatarBone(avatar: THREE.Object3D, boneName: string) {
  const compact = boneName.replace(/^mixamorig:?/, '')
  for (const candidate of [boneName, `mixamorig:${compact}`, compact]) {
    const bone = avatar.getObjectByName(candidate)
    if (bone) return bone
  }
  let resolved: THREE.Object3D | undefined
  avatar.traverse((child) => {
    if (resolved || child.type !== 'Bone') return
    const normalized = child.name.split('|').pop()?.replace(/^mixamorig:?/, '')
    if (normalized === compact) resolved = child
  })
  return resolved
}

function boneFor(boneName: string) {
  if (!bones.has(boneName)) bones.set(boneName, runtime.avatar && getAvatarBone(runtime.avatar, boneName))
  return bones.get(boneName)
}

function deltaFor(boneName: string) {
  const bone = boneFor(boneName)
  if (!bone) return undefined
  const userData = bone.userData as BoneUserData
  return userData.delta ?? (userData.delta = { x: 0, y: 0, z: 0 })
}

const scratchEuler = new THREE.Euler()
const scratchDelta = new THREE.Quaternion()

function applyBoneDelta(bone: THREE.Object3D) {
  const userData = bone.userData as BoneUserData
  const delta = userData.delta ?? (userData.delta = { x: 0, y: 0, z: 0 })
  scratchDelta.setFromEuler(scratchEuler.set(delta.x, delta.y, delta.z))
  const rest = userData.gestureRestQuaternion || userData.restQuaternion
  bone.quaternion.copy(rest ? rest.clone().multiply(scratchDelta) : scratchDelta)
}

function captureRestQuaternions(avatar: THREE.Object3D) {
  avatar.traverse((child) => {
    if (child.type !== 'Bone') return
    const userData = child.userData as BoneUserData
    userData.restQuaternion = child.quaternion.clone()
    userData.gestureRestQuaternion = userData.restQuaternion.clone()
    userData.restPosition = child.position.clone()
    userData.restScale = child.scale.clone()
  })
}

function resetPose() {
  const avatar = runtime.avatar
  if (!avatar) return
  runtime.animations = []
  runtime.segments = []
  runtime.characters = []
  avatar.traverse((child) => {
    if (child.type !== 'Bone') return
    const userData = child.userData as BoneUserData
    if (userData.gestureRestQuaternion) child.quaternion.copy(userData.gestureRestQuaternion)
    if (userData.restPosition) child.position.copy(userData.restPosition)
    if (userData.restScale) child.scale.copy(userData.restScale)
    userData.delta = { x: 0, y: 0, z: 0 }
  })
  // Every word table is authored from defaultPose(); snap it before playback.
  defaultPose(runtime)
  for (const frame of runtime.animations) {
    for (const [boneName, , axis, target] of frame) {
      const bone = getAvatarBone(avatar, boneName)
      if (!bone) continue
      const userData = bone.userData as BoneUserData
      const delta = userData.delta ?? (userData.delta = { x: 0, y: 0, z: 0 })
      delta[axis] = target
      applyBoneDelta(bone)
    }
  }
  runtime.animations = []
  runtime.characters = []
  needsRender = true
}

// --- Gloss → frames (same as AvatarPlayer.tsx) ---------------------------------

function phraseTokens(input: string): string[] {
  return looksPreTranslated(input)
    ? input.trim().toUpperCase().split(/\s+/).filter(Boolean)
    : [...(textToGloss(input) as { tokens: string[] }).tokens]
}

function enqueueTokens(tokens: string[], append = false) {
  if (!append) {
    if (runtime.avatar) resetPose()
    runtime.animations = []
    runtime.segments = []
  }
  for (const token of tokens) {
    const word = token.toUpperCase()
    // Hyphenated tokens are always spelled; a plain token plays its word sign
    // when one exists, otherwise it is spelled letter by letter / digit by digit.
    const wordAnimation = word.includes('-') ? undefined : wordAnimations[word]
    let added = 0
    if (wordAnimation) added = appendSignFrames(runtime, wordAnimation)
    else for (const character of word.replace(/-/g, '')) {
      const play = alphabetAnimations[character] ?? numberAnimations[character]
      if (play) added += appendSignFrames(runtime, play)
    }
    if (added > 0) runtime.segments.push({ token: word, frames: added })
  }
  runtime.characters = []
  if (append) {
    while (runtime.segments.length > MAX_LIVE_BACKLOG) {
      runtime.animations.splice(runtime.segments[0].frames, runtime.segments[1].frames)
      runtime.segments.splice(1, 1)
    }
  }
  needsRender = true
}

function request(phrase: string, append: boolean) {
  const text = String(phrase ?? '').trim()
  if (!text) return
  const tokens = phraseTokens(text)
  log('input:', text)
  log('gloss:', tokens.join(' + ') || '(none)')
  lastTokens = tokens
  if (ready) {
    enqueueTokens(tokens, append)
    notifyState(runtime.animations.length ? 'signing' : 'ready')
    return
  }
  if (!append) pending = [{ tokens, append: false }]
  else {
    pending.push({ tokens, append: true })
    while (pending.length > MAX_LIVE_BACKLOG) pending.splice(pending[0].append ? 0 : 1, 1)
  }
  notifyState('loading')
}

// --- Scene -----------------------------------------------------------------------

function resize() {
  if (!mount || !renderer) return
  const width = mount.clientWidth || innerWidth, height = mount.clientHeight || innerHeight
  if (!width || !height) return
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height, false)
  needsRender = true
}

function disposeMaterials(material: THREE.Material | THREE.Material[] | undefined) {
  for (const entry of Array.isArray(material) ? material : material ? [material] : []) {
    for (const value of Object.values(entry)) if (value instanceof THREE.Texture) value.dispose()
    entry.dispose()
  }
}

function disposeObject(root: THREE.Object3D) {
  root.traverse((child) => {
    const mesh = child as THREE.Mesh
    if (!mesh.isMesh) return
    mesh.geometry?.dispose()
    disposeMaterials(mesh.material)
  })
}

// Copied from AvatarPlayer.tsx: recolours the existing skinned robot mesh only,
// so ISL movements stay identical while the body takes the warm palette.
function applyWarmArmorColors(avatar: THREE.Object3D) {
  const chestCream = new THREE.Color('#e2d3bc')
  const warmBeige = new THREE.Color('#d4bea1')
  const limbBeige = new THREE.Color('#c8b093')
  const lowerBeige = new THREE.Color('#bfa88c')
  const neckCream = new THREE.Color('#d9c7ae')
  const jointCharcoal = new THREE.Color('#4a4641')
  avatar.traverse((child) => {
    const mesh = child as THREE.Mesh
    if (!mesh.isMesh) return
    const positions = mesh.geometry?.attributes.position
    if (!positions) return
    if (mesh.name === 'Alpha_Surface') {
      const colors = new Float32Array(positions.count * 3)
      for (let index = 0; index < positions.count; index += 1) {
        const x = positions.getX(index), y = positions.getY(index), z = positions.getZ(index)
        const isNeck = y > 1.48 && Math.abs(x) < 0.12 && z > -0.1
        const isChest = y >= 1.18 && Math.abs(x) <= 0.38
        const isWaistOrHip = y >= 0.78 && y < 1.18 && Math.abs(x) <= 0.36
        const isArm = Math.abs(x) > 0.34 && y > 0.82
        const isCalfOrFoot = y < 0.42
        const color = isNeck ? neckCream : isChest ? chestCream : isWaistOrHip ? warmBeige : isArm ? limbBeige : isCalfOrFoot ? lowerBeige : warmBeige
        colors[index * 3] = color.r
        colors[index * 3 + 1] = color.g
        colors[index * 3 + 2] = color.b
      }
      mesh.geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
      disposeMaterials(mesh.material)
      mesh.material = new THREE.MeshStandardMaterial({ vertexColors: true, roughness: 0.63, metalness: 0.04 })
    }
    if (mesh.name === 'Alpha_Joints') {
      const colors = new Float32Array(positions.count * 3)
      for (let index = 0; index < positions.count; index += 1) {
        colors[index * 3] = jointCharcoal.r
        colors[index * 3 + 1] = jointCharcoal.g
        colors[index * 3 + 2] = jointCharcoal.b
      }
      mesh.geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
      disposeMaterials(mesh.material)
      mesh.material = new THREE.MeshStandardMaterial({ vertexColors: true, roughness: 0.72, metalness: 0.07 })
    }
  })
}

function loadAvatar(target: AvatarModel) {
  // Kotlin calls setAvatar() from several places on startup; ignore repeats.
  if (loadingModel === target) return
  const generation = ++loadGeneration
  loadingModel = target
  model = target
  ready = false
  notifyState('loading')

  // A switch replays what had not finished (or else the latest phrase).
  const previous = runtime.avatar
  if (previous) {
    const remaining = runtime.segments.map((segment) => segment.token)
    const replay = remaining.length ? remaining : lastTokens
    if (replay.length) pending = [{ tokens: replay, append: false }, ...pending]
  }

  loader.load('models/ybot.glb', (gltf) => {
    if (generation !== loadGeneration) { disposeObject(gltf.scene); return }
    loadingModel = null
    if (runtime.avatar) {
      scene.remove(runtime.avatar)
      disposeObject(runtime.avatar)
    }
    bones.clear()
    runtime.animations = []
    runtime.segments = []
    const avatar = gltf.scene
    avatar.traverse((child) => {
      child.castShadow = true
      child.frustumCulled = false
    })
    runtime.avatar = avatar
    avatar.position.y = -0.85
    scene.add(avatar)
    if (target === 'human') {
      applyWarmArmorColors(avatar)
      const headBone = getAvatarBone(avatar, 'mixamorig:Head')
      if (headBone) {
        const faceLayer = createFaceLayer()
        // Compensate for ybot's 0.01 rig scale and sit the head on the neck.
        faceLayer.scale.set(13.5, 13.5, 13.5)
        faceLayer.position.set(0, 2, 1)
        headBone.add(faceLayer)
      }
    }
    captureRestQuaternions(avatar)
    resetPose()
    ready = true
    hideBoot()
    log('avatar ready:', target)
    const waiting = pending
    pending = []
    for (const { tokens, append } of waiting) enqueueTokens(tokens, append)
    notifyState(runtime.animations.length ? 'signing' : 'ready')
  }, undefined, (error) => {
    if (generation !== loadGeneration) return
    loadingModel = null
    log('GLB load failed', error instanceof Error ? error.message : error)
    hideBoot()
    notifyState('error')
  })
}

let bootHidden = false
function hideBoot() {
  if (bootHidden) return
  bootHidden = true
  const boot = document.getElementById('avatar-boot')
  if (!boot) return
  boot.classList.add('hidden')
  setTimeout(() => boot.remove(), 600)
}

// --- Render loop (same timing rules as AvatarPlayer.tsx) -------------------------

let nextFrameAt = 0
let lastStepAt = performance.now()
let wasSigning = false

function animate(now: number) {
  requestAnimationFrame(animate)
  if (!renderer || document.hidden) return
  // A resting rig needs no repeated WebGL draws.
  if (!runtime.animations.length && !wasSigning && !needsRender) { lastStepAt = now; return }
  // Movement scales with elapsed time, so speed is the same at any refresh rate.
  const elapsed = now - lastStepAt
  lastStepAt = now
  const queue = runtime.animations
  if (runtime.avatar && queue.length && now >= nextFrameAt) {
    wasSigning = true
    const moved = new Set<string>()
    const done = advanceFrame(queue[0], deltaFor, stepForElapsed(elapsed, speed), moved)
    for (const boneName of moved) {
      const bone = boneFor(boneName)
      if (bone) applyBoneDelta(bone)
    }
    if (done) {
      queue.shift()
      const segment = runtime.segments[0]
      if (segment && --segment.frames <= 0) runtime.segments.shift()
      // Brief hold on each authored key pose so the movement still reads.
      nextFrameAt = now + 170 / speed
    }
  }
  if (wasSigning && !queue.length) {
    wasSigning = false
    notifyState('ready')
  }
  renderer.render(scene, camera)
  needsRender = false
}

// --- Public API (called from Avatar3DWebView.kt) ---------------------------------

window.setAvatar = (name) => {
  const target: AvatarModel = name === 'human' ? 'human' : 'default'
  if (target === model && (ready || loadingModel === target)) return
  loadAvatar(target)
}
window.setSpeed = (value) => {
  const parsed = Number(value)
  if (Number.isFinite(parsed) && parsed > 0) speed = parsed
}
window.playPhrase = (phrase) => request(phrase, false)
window.appendPhrase = (phrase) => request(phrase, true)
window.replayAnimation = () => { if (lastTokens.length) window.playPhrase(lastTokens.join(' ')) }
window.stopAnimation = () => {
  pending = []
  runtime.animations = []
  runtime.segments = []
  wasSigning = false
  if (ready) resetPose()
  notifyState('ready')
}
window.resetAvatar = () => {
  // Reset reloads the rig and plays nothing that was waiting or unfinished.
  pending = []
  lastTokens = []
  runtime.segments = []
  loadingModel = null
  loadAvatar(model)
  pending = []
}

// Live speech: the app forwards each partial / final recognizer hypothesis of
// one listening session. The web's live commit rules decide which new words
// to sign now and which to hold back (a word that may still become THANK YOU,
// HOW MUCH…); revised words are signed again from the first difference.
window.liveSpeech = (transcript, isFinal) => {
  const words = String(transcript ?? '').replace(/\s+/g, ' ').trim().split(' ').filter(Boolean)
  const plan = planLiveCommit(liveCommitted, { words, finalWordCount: isFinal ? words.length : 0, allFinal: !!isFinal }, trailingPhrasePrefixLength)
  liveCommitted = isFinal ? [] : plan.committed
  if (plan.chunk) request(plan.chunk, true)
}
window.resetLive = () => { liveCommitted = [] }

// --- Boot ------------------------------------------------------------------------

function boot() {
  mount = document.getElementById('avatar-container')
  if (!mount) return
  try {
    scene = new THREE.Scene()
    scene.background = new THREE.Color('#faf8f2')
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
    if (typeof ResizeObserver !== 'undefined') new ResizeObserver(resize).observe(mount)
    window.addEventListener('resize', resize)
    resize()
    requestAnimationFrame(animate)
    log('engine ready, three r' + THREE.REVISION)
    loadAvatar('default')
  } catch (error) {
    log('fatal init error', error instanceof Error ? error.stack || error.message : error)
    hideBoot()
    notifyState('error')
  }
}

window.addEventListener('beforeunload', () => renderer?.dispose())
if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot)
else boot()
