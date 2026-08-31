package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.data.model.AvatarType
import com.example.data.model.HandGesture
import com.example.data.model.JointPose
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun Mannequin3DCanvas(
    pose: JointPose,
    avatarType: AvatarType,
    modifier: Modifier = Modifier,
    isIdle: Boolean = true
) {
    // Breathing & subtle life animation
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_life")
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        // Scaling factor relative to reference 400x400 canvas
        val scale = min(w / 380f, h / 380f)
        val centerX = w / 2f
        val centerY = h * 0.46f

        // Subtle idle breath offset
        val idleBreath = if (isIdle) (breathProgress - 0.5f) * 4f * scale else 0f
        val idleSway = if (isIdle) sin(breathProgress * Math.PI.toFloat()) * 1.5f * scale else 0f

        val palette = if (avatarType == AvatarType.DEFAULT) {
            MannequinPalette(
                highlight = AvatarTealLight,
                base = AvatarTealBase,
                mid = AvatarTealMid,
                shadow = AvatarTealShadow,
                deep = AvatarTealDeep,
                joint = Color(0xFF163E42),
                visor = Color(0xFFE0FCFF)
            )
        } else {
            MannequinPalette(
                highlight = AvatarHumanLight,
                base = AvatarHumanBase,
                mid = AvatarHumanMid,
                shadow = AvatarHumanShadow,
                deep = AvatarHumanDeep,
                joint = Color(0xFF6B422B),
                visor = Color(0xFF332014)
            )
        }

        // --- Kinematic Body Anchor Coordinates ---
        val torsoCenterX = centerX + pose.chestTwist * 0.8f * scale + idleSway
        val torsoCenterY = centerY + idleBreath

        // Head Anchor
        val neckY = torsoCenterY - 70f * scale
        val headY = neckY - 45f * scale + pose.headPitch * 0.5f * scale
        val headX = torsoCenterX + pose.headYaw * 0.8f * scale

        // Shoulders
        val shoulderHalfWidth = 58f * scale
        val leftShoulder = Offset(torsoCenterX - shoulderHalfWidth, torsoCenterY - 48f * scale)
        val rightShoulder = Offset(torsoCenterX + shoulderHalfWidth, torsoCenterY - 48f * scale)

        // Arms Kinematics (Left & Right)
        val leftArm = calculateArmJoints(
            shoulder = leftShoulder,
            isRight = false,
            pitch = pose.leftArmPitch,
            roll = pose.leftArmRoll,
            elbowBend = pose.leftElbowBend,
            scale = scale
        )

        val rightArm = calculateArmJoints(
            shoulder = rightShoulder,
            isRight = true,
            pitch = pose.rightArmPitch,
            roll = pose.rightArmRoll,
            elbowBend = pose.rightElbowBend,
            scale = scale
        )

        // 1. Draw Lower Torso / Spine Core / Pelvis
        drawPelvisAndSpine(
            center = Offset(torsoCenterX, torsoCenterY + 45f * scale),
            scale = scale,
            palette = palette
        )

        // 2. Draw Upper Torso Chest Plate
        drawChestPlate(
            center = Offset(torsoCenterX, torsoCenterY - 15f * scale),
            scale = scale,
            lean = pose.chestLean,
            palette = palette
        )

        // 3. Draw Neck & Head
        drawNeck(
            start = Offset(torsoCenterX, torsoCenterY - 45f * scale),
            end = Offset(headX, headY + 22f * scale),
            scale = scale,
            palette = palette
        )
        drawFacetedHead(
            center = Offset(headX, headY),
            pitch = pose.headPitch,
            yaw = pose.headYaw,
            roll = pose.headRoll,
            scale = scale,
            palette = palette
        )

        // 4. Draw Arms & Hands (Draw back arm first based on depth, or both)
        drawArmAndHand(
            shoulder = leftShoulder,
            elbow = leftArm.elbow,
            wrist = leftArm.wrist,
            handEnd = leftArm.hand,
            gesture = pose.leftHandGesture,
            isRight = false,
            scale = scale,
            palette = palette
        )

        drawArmAndHand(
            shoulder = rightShoulder,
            elbow = rightArm.elbow,
            wrist = rightArm.wrist,
            handEnd = rightArm.hand,
            gesture = pose.rightHandGesture,
            isRight = true,
            scale = scale,
            palette = palette
        )
    }
}

private data class MannequinPalette(
    val highlight: Color,
    val base: Color,
    val mid: Color,
    val shadow: Color,
    val deep: Color,
    val joint: Color,
    val visor: Color
)

private data class ArmJoints(
    val elbow: Offset,
    val wrist: Offset,
    val hand: Offset
)

private fun calculateArmJoints(
    shoulder: Offset,
    isRight: Boolean,
    pitch: Float,
    roll: Float,
    elbowBend: Float,
    scale: Float
): ArmJoints {
    val upperArmLength = 48f * scale
    val forearmLength = 46f * scale
    val handLength = 22f * scale

    val sideMult = if (isRight) 1f else -1f

    // Natural resting angle downwards and outwards
    val baseAngleDeg = 80f * sideMult
    // Pitch brings arm forward/upwards, Roll pushes arm outwards
    val upperArmAngleRad = Math.toRadians((baseAngleDeg + (roll * 0.7f * sideMult) - (pitch * 0.8f * sideMult)).toDouble()).toFloat()

    val elbowX = shoulder.x + sin(upperArmAngleRad) * upperArmLength * sideMult
    val elbowY = shoulder.y + cos(upperArmAngleRad) * upperArmLength

    // Forearm bends inwards / upwards towards chest/head based on elbowBend
    val bendAngleRad = Math.toRadians((Math.toDegrees(upperArmAngleRad.toDouble()) - (elbowBend * 1.05f * sideMult) - (pitch * 0.4f * sideMult))).toFloat()

    val wristX = elbowX + sin(bendAngleRad) * forearmLength * sideMult
    val wristY = elbowY + cos(bendAngleRad) * forearmLength

    // Hand extension
    val handAngleRad = bendAngleRad - Math.toRadians((15.0 * sideMult)).toFloat()
    val handX = wristX + sin(handAngleRad) * handLength * sideMult
    val handY = wristY + cos(handAngleRad) * handLength

    return ArmJoints(
        elbow = Offset(elbowX, elbowY),
        wrist = Offset(wristX, wristY),
        hand = Offset(handX, handY)
    )
}

private fun DrawScope.drawFacetedHead(
    center: Offset,
    pitch: Float,
    yaw: Float,
    roll: Float,
    scale: Float,
    palette: MannequinPalette
) {
    val hw = 26f * scale
    val hh = 34f * scale

    // Faceted geometric head polygon path
    val headPath = Path().apply {
        // Top dome
        moveTo(center.x - hw * 0.7f, center.y - hh * 0.75f)
        lineTo(center.x, center.y - hh)
        lineTo(center.x + hw * 0.7f, center.y - hh * 0.75f)
        // Temple
        lineTo(center.x + hw, center.y - hh * 0.2f)
        // Cheek / Jaw angle
        lineTo(center.x + hw * 0.8f, center.y + hh * 0.45f)
        // Chin
        lineTo(center.x + hw * 0.35f, center.y + hh * 0.9f)
        lineTo(center.x - hw * 0.35f, center.y + hh * 0.9f)
        // Left Jaw / Temple
        lineTo(center.x - hw * 0.8f, center.y + hh * 0.45f)
        lineTo(center.x - hw, center.y - hh * 0.2f)
        close()
    }

    // Shaded face gradient (light from top-left)
    drawPath(
        path = headPath,
        brush = Brush.radialGradient(
            colors = listOf(palette.highlight, palette.base, palette.mid, palette.shadow),
            center = Offset(center.x - hw * 0.3f, center.y - hh * 0.3f),
            radius = hw * 2.2f
        )
    )

    // Center facial ridge facet (gives crisp 3D geometric form)
    val facetPath = Path().apply {
        moveTo(center.x, center.y - hh * 0.9f)
        lineTo(center.x + hw * 0.6f, center.y - hh * 0.2f)
        lineTo(center.x + hw * 0.35f, center.y + hh * 0.9f)
        lineTo(center.x, center.y + hh * 0.7f)
        lineTo(center.x - hw * 0.35f, center.y + hh * 0.9f)
        lineTo(center.x - hw * 0.6f, center.y - hh * 0.2f)
        close()
    }
    drawPath(
        path = facetPath,
        color = palette.mid.copy(alpha = 0.45f)
    )

    // Sleek geometric eye visor / brow bar
    val visorY = center.y - hh * 0.15f + pitch * 0.3f * scale
    val visorPath = Path().apply {
        moveTo(center.x - hw * 0.65f, visorY)
        lineTo(center.x, visorY + 2f * scale)
        lineTo(center.x + hw * 0.65f, visorY)
        lineTo(center.x + hw * 0.55f, visorY + 5f * scale)
        lineTo(center.x, visorY + 7f * scale)
        lineTo(center.x - hw * 0.55f, visorY + 5f * scale)
        close()
    }
    drawPath(path = visorPath, color = palette.deep)
    drawPath(path = visorPath, color = palette.visor.copy(alpha = 0.85f), style = Stroke(width = 1.5f * scale))

    // Ear node discs on side of head
    drawCircle(
        color = palette.joint,
        radius = 4.5f * scale,
        center = Offset(center.x - hw * 0.92f, center.y - hh * 0.05f)
    )
    drawCircle(
        color = palette.joint,
        radius = 4.5f * scale,
        center = Offset(center.x + hw * 0.92f, center.y - hh * 0.05f)
    )
}

private fun DrawScope.drawNeck(
    start: Offset,
    end: Offset,
    scale: Float,
    palette: MannequinPalette
) {
    val neckRadius = 9f * scale
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(palette.shadow, palette.mid, palette.highlight, palette.shadow),
            startX = start.x - neckRadius,
            endX = start.x + neckRadius
        ),
        start = start,
        end = end,
        strokeWidth = neckRadius * 2f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawChestPlate(
    center: Offset,
    scale: Float,
    lean: Float,
    palette: MannequinPalette
) {
    val cw = 56f * scale
    val ch = 38f * scale

    // Angular chest armor plate
    val chestPath = Path().apply {
        // Neck line
        moveTo(center.x - cw * 0.35f, center.y - ch)
        lineTo(center.x + cw * 0.35f, center.y - ch)
        // Clavicle to shoulders
        lineTo(center.x + cw, center.y - ch * 0.65f)
        // Rib slope down
        lineTo(center.x + cw * 0.65f, center.y + ch * 0.7f)
        // Center sternum apex
        lineTo(center.x, center.y + ch)
        // Left rib slope
        lineTo(center.x - cw * 0.65f, center.y + ch * 0.7f)
        lineTo(center.x - cw, center.y - ch * 0.65f)
        close()
    }

    drawPath(
        path = chestPath,
        brush = Brush.radialGradient(
            colors = listOf(palette.highlight, palette.base, palette.mid, palette.shadow),
            center = Offset(center.x - cw * 0.25f, center.y - ch * 0.3f),
            radius = cw * 1.8f
        )
    )

    // Center Sternum Specular Seam
    drawLine(
        color = palette.highlight.copy(alpha = 0.8f),
        start = Offset(center.x, center.y - ch * 0.8f),
        end = Offset(center.x, center.y + ch * 0.85f),
        strokeWidth = 2.5f * scale,
        cap = StrokeCap.Round
    )

    // Left & Right Shoulder Ball Joints
    drawCircle(
        color = palette.joint,
        radius = 11f * scale,
        center = Offset(center.x - cw * 0.95f, center.y - ch * 0.6f)
    )
    drawCircle(
        color = palette.highlight.copy(alpha = 0.5f),
        radius = 4f * scale,
        center = Offset(center.x - cw * 0.98f, center.y - ch * 0.65f)
    )

    drawCircle(
        color = palette.joint,
        radius = 11f * scale,
        center = Offset(center.x + cw * 0.95f, center.y - ch * 0.6f)
    )
    drawCircle(
        color = palette.highlight.copy(alpha = 0.5f),
        radius = 4f * scale,
        center = Offset(center.x + cw * 0.92f, center.y - ch * 0.65f)
    )
}

private fun DrawScope.drawPelvisAndSpine(
    center: Offset,
    scale: Float,
    palette: MannequinPalette
) {
    // Spine discs (interlocked vertebrae)
    for (i in 0..2) {
        val dy = (i - 1) * 11f * scale
        val discWidth = (32f - i * 4f) * scale
        drawRoundRect(
            color = palette.deep,
            topLeft = Offset(center.x - discWidth / 2f, center.y - 30f * scale + dy),
            size = Size(discWidth, 8f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
        )
    }

    // Lower abdomen / Pelvic cradle
    val pw = 42f * scale
    val ph = 26f * scale
    val pelvisPath = Path().apply {
        moveTo(center.x - pw * 0.7f, center.y + 2f * scale)
        lineTo(center.x + pw * 0.7f, center.y + 2f * scale)
        lineTo(center.x + pw * 0.9f, center.y + ph * 0.5f)
        lineTo(center.x + pw * 0.4f, center.y + ph)
        lineTo(center.x - pw * 0.4f, center.y + ph)
        lineTo(center.x - pw * 0.9f, center.y + ph * 0.5f)
        close()
    }
    drawPath(
        path = pelvisPath,
        brush = Brush.verticalGradient(
            colors = listOf(palette.mid, palette.shadow, palette.deep),
            startY = center.y,
            endY = center.y + ph
        )
    )
}

private fun DrawScope.drawArmAndHand(
    shoulder: Offset,
    elbow: Offset,
    wrist: Offset,
    handEnd: Offset,
    gesture: HandGesture,
    isRight: Boolean,
    scale: Float,
    palette: MannequinPalette
) {
    val upperArmThickness = 14f * scale
    val forearmThickness = 12f * scale

    // 1. Upper Arm Cylinder Segment
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(palette.highlight, palette.base, palette.mid, palette.shadow),
            start = shoulder,
            end = elbow
        ),
        start = shoulder,
        end = elbow,
        strokeWidth = upperArmThickness,
        cap = StrokeCap.Round
    )

    // 2. Elbow Joint Disc
    drawCircle(
        color = palette.joint,
        radius = 8.5f * scale,
        center = elbow
    )
    drawCircle(
        color = palette.highlight.copy(alpha = 0.4f),
        radius = 3f * scale,
        center = Offset(elbow.x - 2f * scale, elbow.y - 2f * scale)
    )

    // 3. Forearm Cylinder Segment
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(palette.highlight, palette.base, palette.shadow),
            start = elbow,
            end = wrist
        ),
        start = elbow,
        end = wrist,
        strokeWidth = forearmThickness,
        cap = StrokeCap.Round
    )

    // 4. Wrist Joint
    drawCircle(
        color = palette.joint,
        radius = 6.5f * scale,
        center = wrist
    )

    // 5. Hand / Palm & Fingers Gestures (Accurate ISL Pose Rendering)
    drawArticulatedHand(
        wrist = wrist,
        handEnd = handEnd,
        gesture = gesture,
        isRight = isRight,
        scale = scale,
        palette = palette
    )
}

private fun DrawScope.drawArticulatedHand(
    wrist: Offset,
    handEnd: Offset,
    gesture: HandGesture,
    isRight: Boolean,
    scale: Float,
    palette: MannequinPalette
) {
    val side = if (isRight) 1f else -1f
    val palmRadius = 7.5f * scale

    // Base Palm
    drawCircle(
        color = palette.base,
        radius = palmRadius,
        center = wrist
    )

    val dirX = handEnd.x - wrist.x
    val dirY = handEnd.y - wrist.y
    val len = hypot(dirX, dirY).coerceAtLeast(1f)
    val nx = dirX / len
    val ny = dirY / len
    // Perpendicular vector
    val px = -ny * side
    val py = nx * side

    when (gesture) {
        HandGesture.POINT_INDEX -> {
            // Index finger points far out
            val indexEnd = Offset(wrist.x + nx * 22f * scale, wrist.y + ny * 22f * scale)
            drawLine(color = palette.highlight, start = wrist, end = indexEnd, strokeWidth = 4f * scale, cap = StrokeCap.Round)
            // Curled other fingers
            drawCircle(color = palette.mid, radius = 5.5f * scale, center = Offset(wrist.x + px * 4f * scale, wrist.y + py * 4f * scale))
            // Thumb
            drawLine(color = palette.base, start = wrist, end = Offset(wrist.x + px * 7f * scale + nx * 4f * scale, wrist.y + py * 7f * scale + ny * 4f * scale), strokeWidth = 3.5f * scale, cap = StrokeCap.Round)
        }
        HandGesture.ROOF_SHAPE -> {
            // Flat angled sloped fingers forming roof edge
            val roofTip = Offset(wrist.x + nx * 18f * scale - px * 6f * scale, wrist.y + ny * 18f * scale - py * 6f * scale)
            drawLine(color = palette.highlight, start = wrist, end = roofTip, strokeWidth = 5f * scale, cap = StrokeCap.Round)
            drawLine(color = palette.mid, start = Offset(wrist.x + px * 3f * scale, wrist.y + py * 3f * scale), end = roofTip, strokeWidth = 4f * scale, cap = StrokeCap.Round)
        }
        HandGesture.THUMBS_UP -> {
            // Fist body
            drawCircle(color = palette.mid, radius = 7f * scale, center = wrist)
            // Upward vertical thumb
            val thumbEnd = Offset(wrist.x, wrist.y - 18f * scale)
            drawLine(color = palette.highlight, start = wrist, end = thumbEnd, strokeWidth = 4.5f * scale, cap = StrokeCap.Round)
        }
        HandGesture.FIST -> {
            // Tight fist sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.highlight, palette.mid, palette.deep),
                    center = Offset(wrist.x - 2f * scale, wrist.y - 2f * scale),
                    radius = 9f * scale
                ),
                radius = 8f * scale,
                center = wrist
            )
        }
        HandGesture.V_PEACE -> {
            // Two open fingers spread
            val indexEnd = Offset(wrist.x + nx * 18f * scale - px * 5f * scale, wrist.y + ny * 18f * scale - py * 5f * scale)
            val middleEnd = Offset(wrist.x + nx * 19f * scale + px * 4f * scale, wrist.y + ny * 19f * scale + py * 4f * scale)
            drawLine(color = palette.highlight, start = wrist, end = indexEnd, strokeWidth = 3.5f * scale, cap = StrokeCap.Round)
            drawLine(color = palette.highlight, start = wrist, end = middleEnd, strokeWidth = 3.5f * scale, cap = StrokeCap.Round)
            drawCircle(color = palette.shadow, radius = 5f * scale, center = wrist)
        }
        HandGesture.W_SHAPE -> {
            val f1 = Offset(wrist.x + nx * 17f * scale - px * 6f * scale, wrist.y + ny * 17f * scale - py * 6f * scale)
            val f2 = Offset(wrist.x + nx * 19f * scale, wrist.y + ny * 19f * scale)
            val f3 = Offset(wrist.x + nx * 17f * scale + px * 6f * scale, wrist.y + ny * 17f * scale + py * 6f * scale)
            drawLine(color = palette.highlight, start = wrist, end = f1, strokeWidth = 3.2f * scale, cap = StrokeCap.Round)
            drawLine(color = palette.highlight, start = wrist, end = f2, strokeWidth = 3.2f * scale, cap = StrokeCap.Round)
            drawLine(color = palette.highlight, start = wrist, end = f3, strokeWidth = 3.2f * scale, cap = StrokeCap.Round)
        }
        HandGesture.PINCH_TWO -> {
            val pinchTip = Offset(wrist.x + nx * 12f * scale, wrist.y + ny * 12f * scale)
            drawLine(color = palette.highlight, start = wrist, end = pinchTip, strokeWidth = 5f * scale, cap = StrokeCap.Round)
            drawCircle(color = palette.mid, radius = 4f * scale, center = pinchTip)
        }
        HandGesture.OPEN_PALM, HandGesture.FLAT_PALM -> {
            // 4 fingers spread or aligned + thumb
            for (i in 0..3) {
                val spread = (i - 1.5f) * 4f * scale
                val fLength = (16f - abs(i - 1.5f) * 2f) * scale
                val fingerEnd = Offset(
                    wrist.x + nx * fLength + px * spread,
                    wrist.y + ny * fLength + py * spread
                )
                drawLine(
                    color = if (i % 2 == 0) palette.highlight else palette.base,
                    start = Offset(wrist.x + px * spread * 0.4f, wrist.y + py * spread * 0.4f),
                    end = fingerEnd,
                    strokeWidth = 3.2f * scale,
                    cap = StrokeCap.Round
                )
            }
            // Thumb
            val thumbEnd = Offset(wrist.x + px * 10f * scale + nx * 6f * scale, wrist.y + py * 10f * scale + ny * 6f * scale)
            drawLine(color = palette.base, start = wrist, end = thumbEnd, strokeWidth = 3.5f * scale, cap = StrokeCap.Round)
        }
        else -> {
            // Relaxed hand
            drawCircle(color = palette.base, radius = 6.5f * scale, center = wrist)
            val tip = Offset(wrist.x + nx * 12f * scale, wrist.y + ny * 12f * scale)
            drawLine(color = palette.highlight, start = wrist, end = tip, strokeWidth = 4f * scale, cap = StrokeCap.Round)
        }
    }
}
