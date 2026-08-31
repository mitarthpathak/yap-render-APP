package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * Shared motion primitives so the landing surface reads as one animated system:
 * sections rise into place on first appearance, tappable tiles give a spring
 * press response.
 */

/**
 * Fade + rise a block into view the first time it is composed. [delayMillis]
 * staggers sibling sections so the page assembles top-to-bottom.
 */
@Composable
fun Reveal(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        state.targetState = true
    }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(animationSpec = tween(420)) +
            slideInVertically(
                animationSpec = tween(460),
                initialOffsetY = { it / 6 }
            )
    ) {
        content()
    }
}

/**
 * Press-scale feedback that does NOT consume the gesture, so an underlying
 * `clickable` still fires. Place near the start of the modifier chain so the
 * scale wraps the element's background and border.
 */
fun Modifier.tapBounce(pressedScale: Float = 0.955f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tapBounce"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    pressed = event.changes.any { it.pressed }
                }
            }
        }
}
