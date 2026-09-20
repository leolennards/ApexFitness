package com.example.apexfitness.data

// Ready-made routines a new user can add with one tap. The exercise names match ExerciseLibrary.
data class RoutineTemplate(
    val name: String,
    val description: String,
    val iconKey: String,
    // Exercise name, sets, reps
    val exercises: List<Triple<String, Int, String>>
) {
    // Builds a real routine from the template. It has no days, so the user picks those later.
    fun toRoutine(): Routine = Routine(
        name = name,
        iconKey = iconKey,
        days = emptyList(),
        exercises = exercises.mapIndexed { index, (exerciseName, sets, reps) ->
            RoutineExercise(name = exerciseName, sets = sets, reps = reps, order = index)
        }
    )
}

object RoutineTemplates {
    val all: List<RoutineTemplate> = listOf(
        RoutineTemplate(
            name = "Push Day",
            description = "Chest, shoulders and triceps",
            iconKey = "dumbbell",
            exercises = listOf(
                Triple("Bench Press", 4, "8"),
                Triple("Incline Bench Press", 3, "10"),
                Triple("Overhead Press", 3, "8"),
                Triple("Lateral Raises", 3, "12"),
                Triple("Tricep Pushdown", 3, "12")
            )
        ),
        RoutineTemplate(
            name = "Pull Day",
            description = "Back and biceps",
            iconKey = "dumbbell",
            exercises = listOf(
                Triple("Deadlift", 3, "5"),
                Triple("Pull-ups", 3, "8"),
                Triple("Barbell Row", 3, "10"),
                Triple("Face Pulls", 3, "15"),
                Triple("Bicep Curls", 3, "12")
            )
        ),
        RoutineTemplate(
            name = "Leg Day",
            description = "Quads, hamstrings, glutes and calves",
            iconKey = "dumbbell",
            exercises = listOf(
                Triple("Squats", 4, "8"),
                Triple("Romanian Deadlift", 3, "10"),
                Triple("Leg Press", 3, "12"),
                Triple("Leg Curl", 3, "12"),
                Triple("Calf Raises", 4, "15")
            )
        ),
        RoutineTemplate(
            name = "Full Body",
            description = "One session that covers everything",
            iconKey = "dumbbell",
            exercises = listOf(
                Triple("Squats", 3, "8"),
                Triple("Bench Press", 3, "8"),
                Triple("Barbell Row", 3, "8"),
                Triple("Overhead Press", 3, "10"),
                Triple("Plank", 3, "45")
            )
        ),
        RoutineTemplate(
            name = "Upper Body",
            description = "Chest, back, shoulders and arms",
            iconKey = "dumbbell",
            exercises = listOf(
                Triple("Bench Press", 3, "8"),
                Triple("Lat Pulldown", 3, "10"),
                Triple("Overhead Press", 3, "10"),
                Triple("Seated Cable Row", 3, "10"),
                Triple("Bicep Curls", 2, "12"),
                Triple("Tricep Pushdown", 2, "12")
            )
        ),
        RoutineTemplate(
            name = "Core Blast",
            description = "A short session for your abs",
            iconKey = "hiit",
            exercises = listOf(
                Triple("Plank", 3, "45"),
                Triple("Russian Twists", 3, "20"),
                Triple("Hanging Leg Raises", 3, "10"),
                Triple("Cable Crunch", 3, "15")
            )
        )
    )
}
