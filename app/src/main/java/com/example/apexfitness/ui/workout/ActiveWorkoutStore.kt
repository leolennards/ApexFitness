package com.example.apexfitness.ui.workout

import android.content.Context
import androidx.core.content.edit
import com.example.apexfitness.data.RoutineExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

// A workout that is still going, saved on the device after every change.
// If Android closes the app mid-workout (music, a call, low memory) nothing is lost,
// and Home offers to pick it up again. Nothing here goes to Firestore until Finish.
data class SavedSet(val reps: String, val weight: String, val completed: Boolean)

data class SavedExercise(val exercise: RoutineExercise, val sets: List<SavedSet>)

data class ActiveWorkout(
    val routineId: String,
    val routineName: String,
    val startTimeMillis: Long,
    val exercises: List<SavedExercise>,
    // When the rest timer ends (0 = no rest running)
    val restEndMillis: Long = 0L,
    val restTotalSeconds: Int = 0,
    // Seconds left while the rest is paused (0 = not paused)
    val restPausedSecondsLeft: Int = 0
) {
    val doneSets: Int get() = exercises.sumOf { ex -> ex.sets.count { it.completed } }
    val totalSets: Int get() = exercises.sumOf { it.sets.size }
}

object ActiveWorkoutStore {
    private const val PREFS_NAME = "apex_active_workout"
    private const val KEY_WORKOUT = "workout"

    // Workouts older than this are treated as forgotten and dropped
    private const val MAX_AGE_MILLIS = 12L * 60 * 60 * 1000

    private val _active = MutableStateFlow<ActiveWorkout?>(null)
    val active: StateFlow<ActiveWorkout?> = _active

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val json = prefs(context).getString(KEY_WORKOUT, null) ?: return
        val workout = runCatching { fromJson(JSONObject(json)) }.getOrNull()
        if (workout == null || System.currentTimeMillis() - workout.startTimeMillis > MAX_AGE_MILLIS) {
            clear(context)
        } else {
            _active.value = workout
        }
    }

    fun save(context: Context, workout: ActiveWorkout) {
        _active.value = workout
        prefs(context).edit { putString(KEY_WORKOUT, toJson(workout).toString()) }
    }

    fun clear(context: Context) {
        _active.value = null
        prefs(context).edit { remove(KEY_WORKOUT) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun toJson(workout: ActiveWorkout): JSONObject = JSONObject().apply {
        put("routineId", workout.routineId)
        put("routineName", workout.routineName)
        put("startTimeMillis", workout.startTimeMillis)
        put("restEndMillis", workout.restEndMillis)
        put("restTotalSeconds", workout.restTotalSeconds)
        put("restPausedSecondsLeft", workout.restPausedSecondsLeft)
        put("exercises", JSONArray().apply {
            workout.exercises.forEach { saved ->
                val ex = saved.exercise
                put(JSONObject().apply {
                    put("id", ex.id)
                    put("name", ex.name)
                    put("sets", ex.sets)
                    put("reps", ex.reps)
                    put("targetWeight", ex.targetWeight)
                    put("restSeconds", ex.restSeconds)
                    put("notes", ex.notes)
                    put("order", ex.order)
                    put("loggedSets", JSONArray().apply {
                        saved.sets.forEach { set ->
                            put(JSONObject().apply {
                                put("reps", set.reps)
                                put("weight", set.weight)
                                put("completed", set.completed)
                            })
                        }
                    })
                })
            }
        })
    }

    private fun fromJson(json: JSONObject): ActiveWorkout {
        val exercisesJson = json.getJSONArray("exercises")
        val exercises = (0 until exercisesJson.length()).map { i ->
            val ex = exercisesJson.getJSONObject(i)
            val setsJson = ex.getJSONArray("loggedSets")
            SavedExercise(
                exercise = RoutineExercise(
                    id = ex.optString("id"),
                    name = ex.optString("name"),
                    sets = ex.optInt("sets", 3),
                    reps = ex.optString("reps", "10"),
                    targetWeight = ex.optString("targetWeight"),
                    restSeconds = ex.optInt("restSeconds", 60),
                    notes = ex.optString("notes"),
                    order = ex.optInt("order", i)
                ),
                sets = (0 until setsJson.length()).map { j ->
                    val set = setsJson.getJSONObject(j)
                    SavedSet(set.optString("reps"), set.optString("weight"), set.optBoolean("completed"))
                }
            )
        }
        return ActiveWorkout(
            routineId = json.getString("routineId"),
            routineName = json.optString("routineName"),
            startTimeMillis = json.getLong("startTimeMillis"),
            exercises = exercises,
            restEndMillis = json.optLong("restEndMillis", 0L),
            restTotalSeconds = json.optInt("restTotalSeconds", 0),
            restPausedSecondsLeft = json.optInt("restPausedSecondsLeft", 0)
        )
    }
}
