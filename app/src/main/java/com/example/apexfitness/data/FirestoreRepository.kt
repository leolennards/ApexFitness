package com.example.apexfitness.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Everything is stored in Firestore under users/{uid}:
//   users/{uid}                         -> UserProfile
//   users/{uid}/routines/{id}           -> Routine
//   users/{uid}/workoutLogs/{id}        -> WorkoutLog (one per session)
//   users/{uid}/personalRecords/{slug}  -> PersonalRecord
//   users/{uid}/challenges/{id}         -> UserChallenge
//   users/{uid}/waterLogs/{yyyy-MM-dd}  -> WaterLog
//   users/{uid}/cardioLogs/{id}         -> CardioLog
//
// Everything sits under the signed-in user's id, so the security rules can simply check
// request.auth.uid == uid.
object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // Firestore saves a write on the phone straight away and sends it when there is signal, but
    // the Task only finishes once the server confirms. At the gym the signal can be bad, so I wait
    // a few seconds and then carry on (the write is already queued and will sync later).
    private suspend fun Task<Void>.awaitWrite() {
        withTimeoutOrNull(4000) { await() }
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun routinesCol(uid: String) = userDoc(uid).collection("routines")
    private fun logsCol(uid: String) = userDoc(uid).collection("workoutLogs")
    private fun prCol(uid: String) = userDoc(uid).collection("personalRecords")
    private fun challengesCol(uid: String) = userDoc(uid).collection("challenges")
    private fun waterLogsCol(uid: String) = userDoc(uid).collection("waterLogs")
    private fun cardioLogsCol(uid: String) = userDoc(uid).collection("cardioLogs")

    private fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // Deletes everything stored for this user (used by "Delete account")
    suspend fun deleteAllUserData(uid: String): Unit = withTimeout(30000) {
        val collections = listOf(
            routinesCol(uid), logsCol(uid), prCol(uid),
            challengesCol(uid), waterLogsCol(uid), cardioLogsCol(uid)
        )
        for (col in collections) {
            val docs = col.get().await().documents
            // A batch can hold 500 writes, so I delete in chunks
            docs.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        }
        userDoc(uid).delete().await()
    }

    // Profile

    suspend fun getProfile(uid: String): UserProfile? {
        val snap = userDoc(uid).get().await()
        return snap.toObject(UserProfile::class.java)
    }

    suspend fun saveProfile(profile: UserProfile) {
        userDoc(profile.uid).set(profile).awaitWrite()
    }

    suspend fun updateProfileFields(uid: String, fields: Map<String, Any?>) {
        userDoc(uid).set(fields, SetOptions.merge()).awaitWrite()
    }

    fun observeProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = userDoc(uid).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { registration.remove() }
    }

    // Routines

    fun observeRoutines(uid: String): Flow<List<Routine>> = callbackFlow {
        val registration = routinesCol(uid).addSnapshotListener { snapshot, _ ->
            val routines = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Routine::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(routines)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getRoutines(uid: String): List<Routine> {
        val snap = routinesCol(uid).get().await()
        return snap.documents.mapNotNull { doc -> doc.toObject(Routine::class.java)?.copy(id = doc.id) }
    }

    suspend fun getRoutine(uid: String, routineId: String): Routine? {
        val doc = routinesCol(uid).document(routineId).get().await()
        return doc.toObject(Routine::class.java)?.copy(id = doc.id)
    }

    // Saves a routine and returns its id (a new id is made if it does not have one yet)
    suspend fun saveRoutine(uid: String, routine: Routine): String {
        val docRef = if (routine.id.isBlank()) routinesCol(uid).document() else routinesCol(uid).document(routine.id)
        val toSave = routine.copy(id = docRef.id, updatedAtMillis = System.currentTimeMillis())
        docRef.set(toSave).awaitWrite()
        return docRef.id
    }

    suspend fun deleteRoutine(uid: String, routineId: String) {
        routinesCol(uid).document(routineId).delete().awaitWrite()
    }

    // Workout logs

    fun observeWorkoutLogs(uid: String, limit: Long = 90): Flow<List<WorkoutLog>> = callbackFlow {
        val registration = logsCol(uid)
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, _ ->
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WorkoutLog::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(logs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addWorkoutLog(uid: String, log: WorkoutLog): String {
        val docRef = logsCol(uid).document()
        docRef.set(log.copy(id = docRef.id)).awaitWrite()
        return docRef.id
    }

    // Personal records

    fun observePersonalRecords(uid: String): Flow<List<PersonalRecord>> = callbackFlow {
        val registration = prCol(uid).addSnapshotListener { snapshot, _ ->
            val records = snapshot?.documents?.mapNotNull { it.toObject(PersonalRecord::class.java) }.orEmpty()
            trySend(records.sortedByDescending { it.updatedAtMillis })
        }
        awaitClose { registration.remove() }
    }

    // Only updates the record if the new result beats the old one (weight first, then reps)
    suspend fun upsertPersonalRecordIfBetter(uid: String, exerciseName: String, weight: Double, reps: Int) {
        val slug = slugify(exerciseName)
        val ref = prCol(uid).document(slug)
        val existing = ref.get().await().toObject(PersonalRecord::class.java)
        val isBetter = existing == null || weight > existing.bestWeight ||
            (weight == existing.bestWeight && reps > existing.bestReps)
        if (isBetter) {
            ref.set(
                PersonalRecord(
                    exerciseName = exerciseName,
                    bestWeight = weight,
                    bestReps = reps,
                    updatedAtMillis = System.currentTimeMillis()
                )
            ).awaitWrite()
        }
    }

    // Challenges

    fun observeChallenges(uid: String): Flow<List<UserChallenge>> = callbackFlow {
        val registration = challengesCol(uid).addSnapshotListener { snapshot, _ ->
            val challenges = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserChallenge::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(challenges)
        }
        awaitClose { registration.remove() }
    }

    // Starts a challenge, or restarts it with a fresh window from now
    suspend fun startChallenge(uid: String, templateId: String, targetValue: Int, durationDays: Int) {
        val challenge = UserChallenge(
            id = templateId,
            templateId = templateId,
            startDateMillis = System.currentTimeMillis(),
            targetValue = targetValue,
            durationDays = durationDays,
            completed = false,
            completedAtMillis = 0L
        )
        challengesCol(uid).document(templateId).set(challenge).awaitWrite()
    }

    suspend fun markChallengeCompleted(uid: String, challengeId: String) {
        challengesCol(uid).document(challengeId).set(
            mapOf("completed" to true, "completedAtMillis" to System.currentTimeMillis()),
            SetOptions.merge()
        ).awaitWrite()
    }

    suspend fun abandonChallenge(uid: String, challengeId: String) {
        challengesCol(uid).document(challengeId).delete().awaitWrite()
    }

    // Water

    fun observeTodayWaterLog(uid: String): Flow<WaterLog?> = callbackFlow {
        val registration = waterLogsCol(uid).document(todayDateKey()).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toObject(WaterLog::class.java))
        }
        awaitClose { registration.remove() }
    }

    // Water logs for the last few days, newest first, for the small weekly chart
    fun observeRecentWaterLogs(uid: String, days: Long = 7): Flow<List<WaterLog>> = callbackFlow {
        val registration = waterLogsCol(uid)
            .orderBy("dateKey", Query.Direction.DESCENDING)
            .limit(days)
            .addSnapshotListener { snapshot, _ ->
                val logs = snapshot?.documents?.mapNotNull { it.toObject(WaterLog::class.java) }.orEmpty()
                trySend(logs)
            }
        awaitClose { registration.remove() }
    }

    // Adds to today's total. A negative number undoes a wrong tap, and the total never goes below 0.
    suspend fun addWater(uid: String, deltaMl: Int) {
        val docRef = waterLogsCol(uid).document(todayDateKey())
        val existing = docRef.get().await().toObject(WaterLog::class.java)
        val newTotal = ((existing?.millilitersConsumed ?: 0) + deltaMl).coerceAtLeast(0)
        docRef.set(WaterLog(dateKey = todayDateKey(), millilitersConsumed = newTotal)).awaitWrite()
    }

    // Cardio logs

    fun observeCardioLogs(uid: String, limit: Long = 90): Flow<List<CardioLog>> = callbackFlow {
        val registration = cardioLogsCol(uid)
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, _ ->
                val logs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CardioLog::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(logs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addCardioLog(uid: String, log: CardioLog): String {
        val docRef = cardioLogsCol(uid).document()
        docRef.set(log.copy(id = docRef.id)).awaitWrite()
        return docRef.id
    }

    suspend fun deleteCardioLog(uid: String, logId: String) {
        cardioLogsCol(uid).document(logId).delete().awaitWrite()
    }
}
