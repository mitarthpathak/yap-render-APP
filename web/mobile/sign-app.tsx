// Entry point of the camera Sign → Text app inside the Android WebView.
// Renders the web app's own components for each route the web app serves:
//   /?mode=sign          live camera recognition (SignStage)
//   /sign/train          letters & numbers Training Studio
//   /sign/train/words    word-motion Training Studio
//   /sign/validate       signer validation sessions
import '@fontsource/dm-sans/latin-400.css'
import '@fontsource/dm-sans/latin-500.css'
import '@fontsource/dm-sans/latin-600.css'
import '@fontsource/dm-sans/latin-700.css'
import '@fontsource/dm-serif-display/latin-400.css'
import '@fontsource/dm-serif-display/latin-400-italic.css'
import '@/app/globals.css'
import '@/app/sign/train/training.css'
import '@/app/sign/validate/validation.css'
import './sign-mobile.css'
import { Suspense, lazy, useEffect } from 'react'
import { createRoot } from 'react-dom/client'
import { useSignMode } from '@/lib/sign/use-sign-mode'
import { SignStage } from '@/components/sign/sign-stage'
import { closeSignScreen, installAndroidShims } from './android-bridge'

installAndroidShims()

const TrainingStudio = lazy(() => import('@/components/sign/training-studio').then((module) => ({ default: module.TrainingStudio })))
const WordTrainingStudio = lazy(() => import('@/components/sign/word-training-studio').then((module) => ({ default: module.WordTrainingStudio })))
const ValidationStudio = lazy(() => import('@/components/sign/validation-studio').then((module) => ({ default: module.ValidationStudio })))

function CameraPage() {
  const sign = useSignMode()
  // Opening this screen from the app is the deliberate tap that may start video.
  useEffect(() => { sign.open({ x: innerWidth / 2, y: innerHeight - 48 }) }, []) // eslint-disable-line react-hooks/exhaustive-deps
  return <main className="sign-mobile-shell">
    <SignStage active={sign.active} status={sign.status} origin={sign.origin} attachVideo={sign.attachVideo}
      onAccept={sign.acceptPrivacy} onRetry={sign.requestCamera} onPause={sign.pauseCamera}
      onClose={() => { sign.close(); closeSignScreen() }} />
  </main>
}

function Route() {
  const path = location.pathname.replace(/\/+$/, '') || '/'
  if (path === '/sign/train/words') { document.title = 'Word Training — Yap & Render'; return <WordTrainingStudio /> }
  if (path === '/sign/train') { document.title = 'Training Studio — Yap & Render'; return <TrainingStudio /> }
  if (path === '/sign/validate') { document.title = 'Signer Validation — Yap & Render'; return <ValidationStudio /> }
  document.title = 'Sign to Text — Yap & Render'
  return <CameraPage />
}

// No StrictMode: its double-run effects would ask for the camera twice.
createRoot(document.getElementById('root')!).render(
  <Suspense fallback={<div className="sign-mobile-loading" role="status">Loading…</div>}><Route /></Suspense>,
)
