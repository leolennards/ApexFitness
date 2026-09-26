package com.example.apexfitness.data

// Built-in list of common exercises, grouped by muscle group, so building a routine is quicker
object ExerciseLibrary {
    val exercisesByCategory: Map<String, List<String>> = linkedMapOf(
        "Chest" to listOf("Bench Press", "Incline Bench Press", "Decline Bench Press", "Dumbbell Flyes", "Push-ups", "Cable Crossover", "Chest Dips", "Pec Deck"),
        "Back" to listOf("Deadlift", "Pull-ups", "Chin-ups", "Lat Pulldown", "Barbell Row", "Single-Arm Dumbbell Row", "Seated Cable Row", "T-Bar Row"),
        "Legs" to listOf("Squats", "Leg Press", "Lunges", "Bulgarian Split Squat", "Romanian Deadlift", "Hip Thrust", "Leg Curl", "Leg Extension", "Calf Raises"),
        "Shoulders" to listOf("Overhead Press", "Lateral Raises", "Cable Lateral Raise", "Front Raises", "Upright Row", "Face Pulls", "Arnold Press", "Shrugs"),
        "Arms" to listOf("Bicep Curls", "Hammer Curls", "Concentration Curl", "Tricep Pushdown", "Cable Kickback", "Skull Crushers", "Preacher Curl", "Tricep Dips"),
        "Core" to listOf("Plank", "Russian Twists", "Bicycle Crunches", "Mountain Climbers", "Hanging Leg Raises", "Cable Crunch", "Ab Wheel Rollout", "Sit-ups"),
        "Cardio" to listOf("Treadmill Run", "Cycling", "Rowing Machine", "Jump Rope", "Stair Climber", "Elliptical", "Sled Push", "Battle Ropes"),
        "Bodyweight" to listOf("Burpees", "Wall Sit", "Step-ups", "Glute Bridges", "Bodyweight Squats", "Inverted Rows", "Diamond Push-ups")
    )
}
