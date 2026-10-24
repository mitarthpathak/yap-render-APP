package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.example.data.model.AvatarType
import java.io.InputStream

private const val TAG = "Avatar3DWebView"

class AvatarJsBridge(
    private val onState: (String) -> Unit
) {
    // The unqualified name inside this method resolves to the method itself, so
    // the callback parameter must have a different name — otherwise every state
    // event recurses into StackOverflowError.
    @JavascriptInterface
    fun onStateChange(state: String) {
        onState(state)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Avatar3DWebView(
    avatarType: AvatarType,
    phraseToPlay: String,
    playTrigger: Long,
    stopTrigger: Long,
    replayTrigger: Long,
    speed: Float,
    onAvatarStateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    liveTranscript: String = "",
    liveIsFinal: Boolean = false,
    liveTrigger: Long = 0L
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    // Handle avatar model change
    LaunchedEffect(avatarType, isLoaded) {
        if (isLoaded) {
            val modelName = if (avatarType == AvatarType.HUMAN) "human" else "default"
            webViewRef?.evaluateJavascript("window.setAvatar && window.setAvatar('$modelName')", null)
        }
    }

    // Handle speed changes
    LaunchedEffect(speed, isLoaded) {
        if (isLoaded) {
            webViewRef?.evaluateJavascript("window.setSpeed && window.setSpeed($speed)", null)
        }
    }

    // Handle play triggers
    LaunchedEffect(playTrigger) {
        if (playTrigger > 0 && isLoaded) {
            val phrase = org.json.JSONObject.quote(phraseToPlay.replace("\n", " ").trim())
            webViewRef?.evaluateJavascript("window.playPhrase && window.playPhrase($phrase)", null)
        }
    }

    // Handle stop triggers
    LaunchedEffect(stopTrigger) {
        if (stopTrigger > 0 && isLoaded) {
            webViewRef?.evaluateJavascript("window.stopAnimation && window.stopAnimation()", null)
        }
    }

    // Live speech: forward each recognizer hypothesis; the engine signs only
    // the words that are new and stable (web lib/live-speech.ts rules).
    LaunchedEffect(liveTrigger) {
        if (liveTrigger > 0 && isLoaded) {
            val text = org.json.JSONObject.quote(liveTranscript.replace("\n", " "))
            webViewRef?.evaluateJavascript("window.liveSpeech && window.liveSpeech($text, $liveIsFinal)", null)
        }
    }

    // Handle replay triggers
    LaunchedEffect(replayTrigger) {
        if (replayTrigger > 0 && isLoaded) {
            webViewRef?.evaluateJavascript("window.replayAnimation && window.replayAnimation()", null)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.parseColor("#FAF8F2"))
                    
                    // Hardware Acceleration is essential for WebGL on real devices
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    addJavascriptInterface(
                        AvatarJsBridge { state ->
                            post { onAvatarStateChange(state) }
                        },
                        "AndroidAvatarBridge"
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d(TAG, "[Avatar WebGL] ${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()}")
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url ?: return null
                            val path = url.path ?: ""

                            // Serve the bundled avatar runtime ourselves so ES module
                            // scripts always get a valid JS mime type and the .glb
                            // models stream reliably on physical devices. The three.js
                            // engine is a local asset now — there is no CDN fallback.
                            if (path.contains("/assets/avatar/")) {
                                val assetPath = path.substringAfter("/assets/")
                                try {
                                    val stream: InputStream = ctx.assets.open(assetPath)
                                    val isText: Boolean
                                    val mime = when {
                                        assetPath.endsWith(".js") || assetPath.endsWith(".mjs") -> { isText = true; "text/javascript" }
                                        assetPath.endsWith(".html") -> { isText = true; "text/html" }
                                        assetPath.endsWith(".css") -> { isText = true; "text/css" }
                                        assetPath.endsWith(".json") -> { isText = true; "application/json" }
                                        assetPath.endsWith(".wasm") -> { isText = false; "application/wasm" }
                                        assetPath.endsWith(".glb") -> { isText = false; "model/gltf-binary" }
                                        assetPath.endsWith(".gltf") -> { isText = true; "model/gltf+json" }
                                        assetPath.endsWith(".bin") -> { isText = false; "application/octet-stream" }
                                        assetPath.endsWith(".png") -> { isText = false; "image/png" }
                                        assetPath.endsWith(".jpg") || assetPath.endsWith(".jpeg") -> { isText = false; "image/jpeg" }
                                        else -> { isText = false; "application/octet-stream" }
                                    }
                                    val headers = mapOf(
                                        "Access-Control-Allow-Origin" to "*",
                                        "Access-Control-Allow-Methods" to "GET, OPTIONS",
                                        "Cache-Control" to "public, max-age=31536000, immutable"
                                    )
                                    return WebResourceResponse(
                                        mime,
                                        if (isText) "utf-8" else null,
                                        200,
                                        "OK",
                                        headers,
                                        stream
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to load asset stream: $assetPath", e)
                                }
                            }

                            return assetLoader.shouldInterceptRequest(url)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoaded = true
                            val modelName = if (avatarType == AvatarType.HUMAN) "human" else "default"
                            evaluateJavascript(
                                "window.setAvatar && window.setAvatar('$modelName'); window.setSpeed && window.setSpeed($speed);",
                                null
                            )
                        }
                    }

                    loadUrl("https://appassets.androidplatform.net/assets/avatar/index.html")
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            }
        )
    }
}
