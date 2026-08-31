package com.example.data.repository

import com.example.data.model.*

object IslSignDictionary {

    val IDLE_POSE = JointPose(
        leftArmPitch = 15f,
        leftArmRoll = 18f,
        leftElbowBend = 35f,
        leftHandOpen = 0.5f,
        leftHandGesture = HandGesture.RELAXED,

        rightArmPitch = 15f,
        rightArmRoll = 18f,
        rightElbowBend = 35f,
        rightHandOpen = 0.5f,
        rightHandGesture = HandGesture.RELAXED,

        headPitch = 0f,
        headYaw = 0f,
        headRoll = 0f,
        chestLean = 0f,
        chestTwist = 0f
    )

    private val READY_POSE = JointPose(
        leftArmPitch = 25f,
        leftArmRoll = 22f,
        leftElbowBend = 55f,
        leftHandOpen = 0.7f,
        leftHandGesture = HandGesture.OPEN_PALM,

        rightArmPitch = 25f,
        rightArmRoll = 22f,
        rightElbowBend = 55f,
        rightHandOpen = 0.7f,
        rightHandGesture = HandGesture.OPEN_PALM,

        headPitch = 2f
    )

    val SIGN_DICTIONARY: Map<String, IslSign> = mapOf(
        "YOU" to IslSign(
            word = "YOU",
            category = "Pronoun",
            description = "Point directly towards the viewer with right index finger",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 60f,
                        rightArmRoll = 10f,
                        rightElbowBend = 30f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 25f,
                        headPitch = -2f,
                        chestTwist = -3f
                    ),
                    durationMs = 500L,
                    label = "Point forward at viewer"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 65f,
                        rightArmRoll = 12f,
                        rightElbowBend = 20f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 25f
                    ),
                    durationMs = 400L,
                    label = "Hold point"
                )
            )
        ),
        "HOME" to IslSign(
            word = "HOME",
            category = "Noun",
            description = "Fingertips meet to form a roof /\\ shape in front of upper chest",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 45f,
                        leftArmRoll = 25f,
                        leftElbowBend = 85f,
                        leftWristPitch = -20f,
                        leftHandGesture = HandGesture.ROOF_SHAPE,
                        rightArmPitch = 45f,
                        rightArmRoll = 25f,
                        rightElbowBend = 85f,
                        rightWristPitch = -20f,
                        rightHandGesture = HandGesture.ROOF_SHAPE,
                        headPitch = 5f
                    ),
                    durationMs = 450L,
                    label = "Form roof shape"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 40f,
                        leftArmRoll = 28f,
                        leftElbowBend = 80f,
                        leftWristPitch = -15f,
                        leftHandGesture = HandGesture.ROOF_SHAPE,
                        rightArmPitch = 40f,
                        rightArmRoll = 28f,
                        rightElbowBend = 80f,
                        rightWristPitch = -15f,
                        rightHandGesture = HandGesture.ROOF_SHAPE,
                        headPitch = 2f
                    ),
                    durationMs = 450L,
                    label = "Tap roof apex"
                )
            )
        ),
        "TIME" to IslSign(
            word = "TIME",
            category = "Concept",
            description = "Right index finger taps the wrist of the left arm",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 40f,
                        leftArmRoll = 15f,
                        leftElbowBend = 90f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 50f,
                        rightArmRoll = 20f,
                        rightElbowBend = 95f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        headPitch = 8f,
                        headYaw = -5f
                    ),
                    durationMs = 400L,
                    label = "Raise watch arm"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 40f,
                        leftArmRoll = 15f,
                        leftElbowBend = 90f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 46f,
                        rightArmRoll = 12f,
                        rightElbowBend = 100f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        headPitch = 10f,
                        headYaw = -5f
                    ),
                    durationMs = 300L,
                    label = "Tap wrist"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 40f,
                        leftArmRoll = 15f,
                        leftElbowBend = 90f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 48f,
                        rightArmRoll = 18f,
                        rightElbowBend = 92f,
                        rightHandGesture = HandGesture.POINT_INDEX
                    ),
                    durationMs = 300L,
                    label = "Tap wrist again"
                )
            )
        ),
        "PERSON" to IslSign(
            word = "PERSON",
            category = "Noun",
            description = "Both hands flat palms trace down the sides of body",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 50f,
                        leftArmRoll = 35f,
                        leftElbowBend = 70f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 50f,
                        rightArmRoll = 35f,
                        rightElbowBend = 70f,
                        rightHandGesture = HandGesture.FLAT_PALM
                    ),
                    durationMs = 400L,
                    label = "Hands high at ribs"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 15f,
                        leftArmRoll = 25f,
                        leftElbowBend = 30f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 15f,
                        rightArmRoll = 25f,
                        rightElbowBend = 30f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        chestLean = 2f
                    ),
                    durationMs = 500L,
                    label = "Slide downwards"
                )
            )
        ),
        "HELLO" to IslSign(
            word = "HELLO",
            category = "Greeting",
            description = "Open flat hand from forehead / temple extends outward in a salute-wave",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 70f,
                        rightArmRoll = 30f,
                        rightElbowBend = 120f,
                        rightHandGesture = HandGesture.OPEN_PALM,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 25f,
                        headPitch = 5f,
                        headRoll = 3f
                    ),
                    durationMs = 400L,
                    label = "Hand at temple"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 55f,
                        rightArmRoll = 45f,
                        rightElbowBend = 50f,
                        rightHandGesture = HandGesture.OPEN_PALM,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 25f,
                        headPitch = -2f
                    ),
                    durationMs = 500L,
                    label = "Wave outward"
                )
            )
        ),
        "HELP" to IslSign(
            word = "HELP",
            category = "Action",
            description = "Right fist with thumb up is lifted upwards by the flat left palm",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 30f,
                        leftArmRoll = 15f,
                        leftElbowBend = 80f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 30f,
                        rightArmRoll = 12f,
                        rightElbowBend = 85f,
                        rightHandGesture = HandGesture.THUMBS_UP,
                        headPitch = 5f
                    ),
                    durationMs = 400L,
                    label = "Fist resting on palm"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 55f,
                        leftArmRoll = 15f,
                        leftElbowBend = 95f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 55f,
                        rightArmRoll = 12f,
                        rightElbowBend = 100f,
                        rightHandGesture = HandGesture.THUMBS_UP,
                        headPitch = 0f,
                        chestLean = 3f
                    ),
                    durationMs = 500L,
                    label = "Lift both together"
                )
            )
        ),
        "THANK YOU" to IslSign(
            word = "THANK YOU",
            category = "Polite",
            description = "Right flat hand touches chin and extends forward gracefully towards viewer",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 60f,
                        rightArmRoll = 15f,
                        rightElbowBend = 115f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 20f,
                        headPitch = 8f
                    ),
                    durationMs = 400L,
                    label = "Fingertips to chin"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 45f,
                        rightArmRoll = 15f,
                        rightElbowBend = 40f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        leftArmPitch = 10f,
                        leftArmRoll = 15f,
                        leftElbowBend = 20f,
                        headPitch = 2f,
                        chestLean = 4f
                    ),
                    durationMs = 550L,
                    label = "Extend outward"
                )
            )
        ),
        "YES" to IslSign(
            word = "YES",
            category = "Response",
            description = "Right fist nods up and down at chest height with head nod",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 40f,
                        rightArmRoll = 20f,
                        rightElbowBend = 85f,
                        rightHandGesture = HandGesture.FIST,
                        headPitch = -5f
                    ),
                    durationMs = 300L,
                    label = "Fist up"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 35f,
                        rightArmRoll = 20f,
                        rightElbowBend = 70f,
                        rightHandGesture = HandGesture.FIST,
                        headPitch = 8f
                    ),
                    durationMs = 300L,
                    label = "Fist down / nod"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 40f,
                        rightArmRoll = 20f,
                        rightElbowBend = 85f,
                        rightHandGesture = HandGesture.FIST,
                        headPitch = -2f
                    ),
                    durationMs = 300L,
                    label = "Fist up again"
                )
            )
        ),
        "NO" to IslSign(
            word = "NO",
            category = "Response",
            description = "Right index and middle fingers snap together against thumb with head shake",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 45f,
                        rightArmRoll = 25f,
                        rightElbowBend = 75f,
                        rightHandGesture = HandGesture.V_PEACE,
                        headYaw = -8f
                    ),
                    durationMs = 350L,
                    label = "Fingers apart"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 45f,
                        rightArmRoll = 25f,
                        rightElbowBend = 75f,
                        rightHandGesture = HandGesture.PINCH_TWO,
                        headYaw = 8f
                    ),
                    durationMs = 350L,
                    label = "Snap closed"
                )
            )
        ),
        "PLEASE" to IslSign(
            word = "PLEASE",
            category = "Polite",
            description = "Right flat hand rubs in a gentle circle over center chest",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 40f,
                        rightArmRoll = 10f,
                        rightElbowBend = 100f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        chestLean = 3f,
                        headPitch = 5f
                    ),
                    durationMs = 350L,
                    label = "Hand on chest left"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 45f,
                        rightArmRoll = 15f,
                        rightElbowBend = 90f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        chestLean = 3f,
                        headPitch = 5f
                    ),
                    durationMs = 350L,
                    label = "Hand circling right"
                ),
                SignKeyframe(
                    pose = JointPose(
                        rightArmPitch = 40f,
                        rightArmRoll = 10f,
                        rightElbowBend = 100f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        chestLean = 2f
                    ),
                    durationMs = 350L,
                    label = "Circle complete"
                )
            )
        ),
        "FRIEND" to IslSign(
            word = "FRIEND",
            category = "Relation",
            description = "Both index fingers hook together across chest",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 40f,
                        leftArmRoll = 18f,
                        leftElbowBend = 85f,
                        leftHandGesture = HandGesture.POINT_INDEX,
                        rightArmPitch = 40f,
                        rightArmRoll = 18f,
                        rightElbowBend = 85f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        headPitch = 4f
                    ),
                    durationMs = 400L,
                    label = "Hook fingers right over left"
                ),
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 42f,
                        leftArmRoll = 18f,
                        leftElbowBend = 88f,
                        leftHandGesture = HandGesture.POINT_INDEX,
                        rightArmPitch = 42f,
                        rightArmRoll = 18f,
                        rightElbowBend = 88f,
                        rightHandGesture = HandGesture.POINT_INDEX,
                        headPitch = 2f
                    ),
                    durationMs = 400L,
                    label = "Switch hook left over right"
                )
            )
        ),
        "LOVE" to IslSign(
            word = "LOVE",
            category = "Emotion",
            description = "Both arms cross in an 'X' over the heart/chest",
            keyframes = listOf(
                SignKeyframe(
                    pose = JointPose(
                        leftArmPitch = 45f,
                        leftArmRoll = 10f,
                        leftElbowBend = 105f,
                        leftHandGesture = HandGesture.FLAT_PALM,
                        rightArmPitch = 45f,
                        rightArmRoll = 10f,
                        rightElbowBend = 105f,
                        rightHandGesture = HandGesture.FLAT_PALM,
                        headPitch = 5f,
                        headRoll = 4f,
                        chestLean = 2f
                    ),
                    durationMs = 600L,
                    label = "Cross arms over heart"
                )
            )
        )
    )

    val QUICK_SUGGESTION_WORDS = listOf(
        "YOU", "HOME", "TIME", "PERSON", "HELLO", "HELP",
        "THANK YOU", "YES", "NO", "PLEASE", "FRIEND", "LOVE"
    )

    // Fingerspelling pose generator for A-Z
    fun getLetterPose(char: Char): JointPose {
        val upper = char.uppercaseChar()
        return when (upper) {
            'A' -> JointPose(
                rightArmPitch = 48f, rightArmRoll = 20f, rightElbowBend = 75f,
                rightHandGesture = HandGesture.FIST, headPitch = 2f
            )
            'B' -> JointPose(
                rightArmPitch = 52f, rightArmRoll = 20f, rightElbowBend = 80f,
                rightHandGesture = HandGesture.FLAT_PALM, headPitch = 2f
            )
            'C' -> JointPose(
                rightArmPitch = 50f, rightArmRoll = 22f, rightElbowBend = 78f,
                rightHandGesture = HandGesture.OPEN_PALM, headPitch = 2f
            )
            'D', 'I', 'P', 'Z' -> JointPose(
                rightArmPitch = 50f, rightArmRoll = 18f, rightElbowBend = 75f,
                rightHandGesture = HandGesture.POINT_INDEX, headPitch = 2f
            )
            'V', 'U', 'R', 'K' -> JointPose(
                rightArmPitch = 50f, rightArmRoll = 20f, rightElbowBend = 78f,
                rightHandGesture = HandGesture.V_PEACE, headPitch = 2f
            )
            'W' -> JointPose(
                rightArmPitch = 50f, rightArmRoll = 20f, rightElbowBend = 80f,
                rightHandGesture = HandGesture.W_SHAPE, headPitch = 2f
            )
            'L' -> JointPose(
                rightArmPitch = 50f, rightArmRoll = 22f, rightElbowBend = 76f,
                rightHandGesture = HandGesture.POINT_INDEX, headPitch = 2f
            )
            'O', 'S', 'T', 'M', 'N' -> JointPose(
                rightArmPitch = 46f, rightArmRoll = 18f, rightElbowBend = 74f,
                rightHandGesture = HandGesture.FIST, headPitch = 2f
            )
            else -> JointPose(
                rightArmPitch = 48f, rightArmRoll = 20f, rightElbowBend = 75f,
                rightHandGesture = HandGesture.OPEN_PALM, headPitch = 2f
            )
        }
    }
}
