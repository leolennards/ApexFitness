package com.example.apexfitness.data

// Built-in list of common exercises, grouped by muscle group, so building a routine is quicker
object ExerciseLibrary {
    val exercisesByCategory: Map<String, List<String>> = linkedMapOf(
        "Chest" to listOf("Bench Press", "Incline Bench Press", "Dumbbell Flyes", "Push-ups", "Cable Crossover", "Chest Dips"),
        "Back" to listOf("Deadlift", "Pull-ups", "Lat Pulldown", "Barbell Row", "Seated Cable Row", "T-Bar Row"),
        "Legs" to listOf("Squats", "Leg Press", "Lunges", "Romanian Deadlift", "Leg Curl", "Leg Extension", "Calf Raises"),
        "Shoulders" to listOf("Overhead Press", "Lateral Raises", "Front Raises", "Face Pulls", "Arnold Press", "Shrugs"),
        "Arms" to listOf("Bicep Curls", "Hammer Curls", "Tricep Pushdown", "Skull Crushers", "Preacher Curl", "Tricep Dips"),
        "Core" to listOf("Plank", "Russian Twists", "Hanging Leg Raises", "Cable Crunch", "Ab Wheel Rollout", "Sit-ups"),
        "Cardio" to listOf("Treadmill Run", "Cycling", "Rowing Machine", "Jump Rope", "Stair Climber", "Elliptical")
    )
}
