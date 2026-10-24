// Glue between the web camera app and the native Android screen
// (SignCameraWebView.kt). In a plain browser the bridge is missing and every
// shim falls back to the web app's own behaviour.

type SignBridge = {
  close?: () => void
  speak?: (id: string, text: string, language: string) => void
  stopSpeaking?: () => void
  sentenceAvailable?: () => boolean
  requestSentence?: (id: string, body: string) => void
}

declare global {
  interface Window {
    AndroidSignBridge?: SignBridge
    __yapSpeechEvent?: (id: string, state: string) => void
    __yapSentenceResult?: (id: string, status: number, body: string) => void
  }
}

const bridge = () => (typeof window === 'undefined' ? undefined : window.AndroidSignBridge)

export function closeSignScreen() {
  const native = bridge()
  if (native?.close) native.close()
  else location.assign('/')
}

let serial = 0
const nextId = () => `${Date.now().toString(36)}-${(serial++).toString(36)}`

// --- speechSynthesis --------------------------------------------------------
// Android WebView has no working Web Speech synthesis (no voices, no audio).
// The transcript's Speak button uses speechSynthesis + SpeechSynthesisUtterance,
// so inside the app both are replaced with the device's TextToSpeech engine.
function installSpeech() {
  const native = bridge()
  if (!native?.speak) return
  const voice = (lang: string, name: string) => ({ voiceURI: `android-tts-${lang}`, name, lang, localService: true, default: lang === 'en-IN' }) as SpeechSynthesisVoice
  const voices = [voice('en-IN', 'Device voice (English)'), voice('hi-IN', 'Device voice (Hindi)')]
  const active = new Map<string, Utterance>()

  class Utterance extends EventTarget {
    text: string; lang = 'en-IN'; voice: SpeechSynthesisVoice | null = null
    volume = 1; rate = 1; pitch = 1
    onstart: ((event: Event) => void) | null = null
    onend: ((event: Event) => void) | null = null
    onerror: ((event: Event) => void) | null = null
    constructor(text = '') { super(); this.text = text }
  }
  const emit = (utterance: Utterance, type: 'start' | 'end' | 'error') => {
    const event = new Event(type)
    const handler = type === 'start' ? utterance.onstart : type === 'end' ? utterance.onend : utterance.onerror
    handler?.call(utterance, event)
    utterance.dispatchEvent(event)
  }
  window.__yapSpeechEvent = (id, state) => {
    const utterance = active.get(id)
    if (!utterance) return
    if (state !== 'start') active.delete(id)
    emit(utterance, state === 'start' ? 'start' : state === 'end' ? 'end' : 'error')
  }
  const synthesis = Object.assign(new EventTarget(), {
    pending: false, paused: false,
    get speaking() { return active.size > 0 },
    getVoices: () => voices.slice(),
    speak(utterance: Utterance) {
      const id = nextId()
      active.set(id, utterance)
      native.speak!(id, utterance.text, utterance.voice?.lang || utterance.lang || 'en-IN')
    },
    cancel() {
      // Cancelled speech reports no further events, like the browser API.
      active.clear()
      native.stopSpeaking?.()
    },
    pause() {}, resume() {},
    onvoiceschanged: null,
  })
  Object.defineProperty(window, 'speechSynthesis', { value: synthesis, configurable: true })
  Object.defineProperty(window, 'SpeechSynthesisUtterance', { value: Utterance, configurable: true })
}

// --- /api/sign/sentence -----------------------------------------------------
// The web server's Gemini route does not exist on the phone. The request is
// checked here with the route's own validator; then, with a Gemini key in the
// app build, the native side makes the route's Gemini call. Without one the
// route's no-key answer is returned: basic formatting, no translation.
function installSentenceApi() {
  const original = window.fetch.bind(window)
  const waiting = new Map<string, (response: Response) => void>()
  window.__yapSentenceResult = (id, status, body) => {
    const resolve = waiting.get(id)
    waiting.delete(id)
    resolve?.(new Response(body, { status, headers: { 'Content-Type': 'application/json' } }))
  }
  window.fetch = async (input, init) => {
    const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url
    if (new URL(url, location.href).pathname !== '/api/sign/sentence') return original(input, init)
    const { formatTokens, sentenceRequest } = await import('@/lib/sign/sentence')
    let request
    try {
      request = sentenceRequest(JSON.parse(typeof init?.body === 'string' ? init.body : ''))
    } catch (error) {
      return Response.json({ error: error instanceof Error && !(error instanceof SyntaxError) ? error.message : 'Invalid text request.' }, { status: 400 })
    }
    const native = bridge()
    if (native?.requestSentence && native.sentenceAvailable?.()) {
      const id = nextId()
      return await new Promise<Response>((resolve, reject) => {
        waiting.set(id, resolve)
        init?.signal?.addEventListener('abort', () => { waiting.delete(id); reject(new DOMException('Aborted', 'AbortError')) }, { once: true })
        native.requestSentence!(id, JSON.stringify(request))
      })
    }
    return Response.json({
      text: formatTokens(request.tokens), raw: request.tokens.join(''), engine: 'formatted',
      notice: 'Sentence service unavailable. Showing basic text formatting; no translation was performed.',
    })
  }
}

export function installAndroidShims() {
  installSpeech()
  installSentenceApi()
}
