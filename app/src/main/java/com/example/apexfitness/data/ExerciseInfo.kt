package com.example.apexfitness.data

// Short guidance for an exercise: which muscles it works and one form tip
data class ExerciseTip(val muscles: String, val tip: String)

// What the user did last time and their best, for one exercise (weights in kg)
data class ExerciseHistory(
    val lastSet: LoggedSet? = null,
    val best: PersonalRecord? = null
)

// Tips for the exercises in ExerciseLibrary. Exercises the user typed in themselves have no tip.
object ExerciseInfo {
    private val tips: Map<String, ExerciseTip> = mapOf(
        // Chest
        "Bench Press" to ExerciseTip("Chest, front shoulders, triceps", "Pull your shoulder blades back and plant your feet. Lower the bar to mid-chest, then press up without bouncing."),
        "Incline Bench Press" to ExerciseTip("Upper chest, front shoulders, triceps", "Set the bench at about 30 degrees. Lower the bar to your upper chest and keep your elbows about 45 degrees from your body."),
        "Dumbbell Flyes" to ExerciseTip("Chest", "Keep a slight bend in your elbows and lower the weights in a wide arc until you feel a stretch. Squeeze them back together."),
        "Push-ups" to ExerciseTip("Chest, shoulders, triceps, core", "Keep your body in one straight line from head to heels. Lower your chest to just above the floor and press back up."),
        "Cable Crossover" to ExerciseTip("Chest", "Step forward with a slight lean and bring the handles together in a hugging arc. Squeeze your chest at the centre."),
        "Chest Dips" to ExerciseTip("Lower chest, triceps", "Lean your torso forward and lower until your shoulders are just below your elbows. Press up without locking out hard."),
        // Back
        "Deadlift" to ExerciseTip("Back, glutes, hamstrings", "Keep the bar close to your legs and your back flat. Push the floor away, then stand tall without leaning back."),
        "Pull-ups" to ExerciseTip("Lats, biceps", "Start from a full hang and pull your chest towards the bar by driving your elbows down. Lower slowly."),
        "Lat Pulldown" to ExerciseTip("Lats, biceps", "Lean back slightly and pull the bar to your upper chest, leading with your elbows. Do not swing."),
        "Barbell Row" to ExerciseTip("Upper back, lats, biceps", "Hinge forward with a flat back and pull the bar to your lower ribs. Keep your elbows close and your torso still."),
        "Seated Cable Row" to ExerciseTip("Mid back, lats, biceps", "Sit tall, pull the handle to your stomach and squeeze your shoulder blades together. Let your arms stretch forward under control."),
        "T-Bar Row" to ExerciseTip("Mid back, lats", "Keep your chest up and your back flat, and pull the weight towards your chest. Avoid jerking with your lower back."),
        // Legs
        "Squats" to ExerciseTip("Quads, glutes, hamstrings", "Keep your chest up and your knees over your toes. Sit down until your thighs are at least parallel, then drive up."),
        "Leg Press" to ExerciseTip("Quads, glutes", "Place your feet shoulder-width apart and lower until your knees reach about 90 degrees. Do not lock your knees at the top."),
        "Lunges" to ExerciseTip("Quads, glutes", "Take a long step, drop your back knee towards the floor and keep your front heel down. Push through the front foot to return."),
        "Romanian Deadlift" to ExerciseTip("Hamstrings, glutes", "Keep a soft bend in your knees and push your hips back with a flat back. Lower until you feel a hamstring stretch, then squeeze your glutes to stand."),
        "Leg Curl" to ExerciseTip("Hamstrings", "Curl the weight towards your glutes without lifting your hips, and lower it slowly."),
        "Leg Extension" to ExerciseTip("Quads", "Extend your legs until they are almost straight and pause briefly. Lower under control and avoid swinging."),
        "Calf Raises" to ExerciseTip("Calves", "Rise onto your toes as high as you can, pause, then lower until you feel a full stretch."),
        // Shoulders
        "Overhead Press" to ExerciseTip("Shoulders, triceps", "Brace your core and squeeze your glutes. Press the bar straight up past your face and finish with it over your mid-foot."),
        "Lateral Raises" to ExerciseTip("Side shoulders", "Lift the weights out to your sides to shoulder height with a slight bend in your elbows. Go light and do not swing."),
        "Front Raises" to ExerciseTip("Front shoulders", "Raise the weights in front of you to shoulder height with control, then lower slowly."),
        "Face Pulls" to ExerciseTip("Rear shoulders, upper back", "Pull the rope towards your face with your elbows high and pull the ends apart. Squeeze your shoulder blades."),
        "Arnold Press" to ExerciseTip("Shoulders", "Start with your palms facing you and rotate them outward as you press overhead. Reverse the movement on the way down."),
        "Shrugs" to ExerciseTip("Upper traps", "Lift your shoulders straight up towards your ears and hold for a moment. Do not roll them."),
        // Arms
        "Bicep Curls" to ExerciseTip("Biceps", "Keep your elbows next to your sides and curl the weight up without swinging your body. Lower slowly."),
        "Hammer Curls" to ExerciseTip("Biceps, forearms", "Hold the dumbbells with your palms facing each other and curl up while keeping your wrists straight."),
        "Tricep Pushdown" to ExerciseTip("Triceps", "Keep your elbows tucked at your sides and push down until your arms are straight. Let the bar rise slowly."),
        "Skull Crushers" to ExerciseTip("Triceps", "Lower the bar towards your forehead by bending only at the elbows, then press back up. Keep your upper arms still."),
        "Preacher Curl" to ExerciseTip("Biceps", "Rest your upper arms on the pad and curl up, then lower until your arms are almost straight."),
        "Tricep Dips" to ExerciseTip("Triceps, chest", "Keep your body upright and lower until your elbows reach about 90 degrees, then press up."),
        // Core
        "Plank" to ExerciseTip("Core", "Keep your body in a straight line from head to heels, with your core tight and your hips level. Breathe steadily."),
        "Russian Twists" to ExerciseTip("Obliques", "Lean back slightly with your chest up and rotate your torso from side to side. Keep your feet steady."),
        "Hanging Leg Raises" to ExerciseTip("Lower abs, hip flexors", "Hang with your shoulders engaged and raise your legs without swinging. Lower them slowly."),
        "Cable Crunch" to ExerciseTip("Abs", "Kneel under the cable and curl your ribs towards your hips. Keep your hips still."),
        "Ab Wheel Rollout" to ExerciseTip("Abs, shoulders", "Roll out only as far as you can keep your lower back flat. Pull back using your abs."),
        "Sit-ups" to ExerciseTip("Abs, hip flexors", "Keep your feet anchored and curl your torso up. Lower under control and do not pull on your neck."),
        // Cardio
        "Treadmill Run" to ExerciseTip("Legs, heart", "Land softly under your hips and keep your shoulders relaxed. Start with an easy warm-up pace."),
        "Cycling" to ExerciseTip("Legs, heart", "Set the seat so your knee is slightly bent at the bottom of the pedal stroke. Keep a steady rhythm."),
        "Rowing Machine" to ExerciseTip("Legs, back, arms", "Drive with your legs first, then lean back, then pull the handle to your chest. Reverse the order on the way back."),
        "Jump Rope" to ExerciseTip("Calves, shoulders, heart", "Stay on the balls of your feet and turn the rope with your wrists. Jump only high enough to clear it."),
        "Stair Climber" to ExerciseTip("Legs, glutes", "Stand tall and place your whole foot on each step. Avoid leaning on the rails."),
        "Elliptical" to ExerciseTip("Legs, heart", "Keep your posture upright and push and pull the handles evenly. Adjust the resistance to keep a steady effort.")
    ).mapKeys { it.key.lowercase() }

    fun find(exerciseName: String): ExerciseTip? = tips[exerciseName.trim().lowercase()]
}
