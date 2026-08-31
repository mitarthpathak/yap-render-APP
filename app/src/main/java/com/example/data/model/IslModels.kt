package com.example.data.model

enum class AvatarType(val label: String) {
    DEFAULT("Default"),
    HUMAN("Human")
}

data class JointPose(
    val leftArmPitch: Float = 0f,      // Shoulder rotation forward/back (degrees)
    val leftArmRoll: Float = 0f,       // Shoulder abduction outwards (degrees)
    val leftArmYaw: Float = 0f,        // Shoulder twist
    val leftElbowBend: Float = 20f,    // Elbow bend (0: straight down, 90: right angle, 140: touch face)
    val leftWristPitch: Float = 0f,    // Wrist angle
    val leftHandOpen: Float = 0.5f,    // 0: tight fist, 0.5: relaxed, 1.0: wide open palm
    val leftHandGesture: HandGesture = HandGesture.RELAXED,

    val rightArmPitch: Float = 0f,
    val rightArmRoll: Float = 0f,
    val rightArmYaw: Float = 0f,
    val rightElbowBend: Float = 20f,
    val rightWristPitch: Float = 0f,
    val rightHandOpen: Float = 0.5f,
    val rightHandGesture: HandGesture = HandGesture.RELAXED,

    val headPitch: Float = 0f,         // Head tilt up/down
    val headYaw: Float = 0f,           // Head turn left/right
    val headRoll: Float = 0f,          // Head tilt sideways
    val chestLean: Float = 0f,         // Torso forward lean
    val chestTwist: Float = 0f         // Torso rotation
)

enum class HandGesture {
    RELAXED,
    OPEN_PALM,
    FLAT_PALM,
    FIST,
    THUMBS_UP,
    POINT_INDEX,
    PINCH_TWO,
    ROOF_SHAPE,
    HEART_TOUCH,
    W_SHAPE,
    V_PEACE,
    CLAP_TOUCH
}

data class SignKeyframe(
    val pose: JointPose,
    val durationMs: Long = 400L,
    val label: String = ""
)

data class IslSign(
    val word: String,
    val category: String,
    val description: String,
    val keyframes: List<SignKeyframe>
)
