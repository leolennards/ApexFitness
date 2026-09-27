package com.example.apexfitness.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.PersonalRecord
import com.example.apexfitness.data.Routine
import com.example.apexfitness.data.UserProfile
import com.example.apexfitness.data.WorkoutLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Holds the data the four main tabs share. It lives as long as the main screen is on the back stack,
// so coming back from Settings or a workout does not reload everything and flash the skeleton again.
class MainViewModel : ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
        private set
    var routines by mutableStateOf<List<Routine>>(emptyList())
        private set
    var logs by mutableStateOf<List<WorkoutLog>>(emptyList())
        private set
    var personalRecords by mutableStateOf<List<PersonalRecord>>(emptyList())
        private set
    var todayWaterMl by mutableStateOf(0)
        private set

    private var hasLoadedProfile by mutableStateOf(false)
    private var hasLoadedRoutines by mutableStateOf(false)
    private var hasLoadedLogs by mutableStateOf(false)
    private var hasLoadedPersonalRecords by mutableStateOf(false)
    private var hasLoadedWater by mutableStateOf(false)

    // Popups the user already closed this session, so they do not come back before Firestore catches up
    var dismissedAchievements by mutableStateOf<Set<String>>(emptySet())
    var dismissedLevelUp by mutableStateOf(1)

    // Set once, before the first frame reads isInitialLoading
    private var startedFor: String? = null

    // True until every listener has sent its first result
    val isInitialLoading: Boolean
        get() = startedFor != null &&
            !(hasLoadedProfile && hasLoadedRoutines && hasLoadedLogs && hasLoadedPersonalRecords && hasLoadedWater)

    // Starts the Firestore listeners once per signed in user
    fun start(uid: String) {
        if (startedFor == uid) return
        startedFor = uid
        viewModelScope.launch { FirestoreRepository.observeProfile(uid).collect { profile = it; hasLoadedProfile = true } }
        viewModelScope.launch { FirestoreRepository.observeRoutines(uid).collect { routines = it; hasLoadedRoutines = true } }
        viewModelScope.launch { FirestoreRepository.observeWorkoutLogs(uid).collect { logs = it; hasLoadedLogs = true } }
        viewModelScope.launch { FirestoreRepository.observePersonalRecords(uid).collect { personalRecords = it; hasLoadedPersonalRecords = true } }
        startWater(uid)
    }

    // Today's water is one Firestore document per day, so after midnight I switch to the new day's document.
    // MainScreen calls this every time the app comes back to the front.
    fun refreshWaterIfNewDay() {
        val uid = startedFor ?: return
        if (waterDay != dayKey()) startWater(uid)
    }

    private var waterJob: Job? = null
    private var waterDay: String? = null

    private fun dayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun startWater(uid: String) {
        waterJob?.cancel()
        waterDay = dayKey()
        waterJob = viewModelScope.launch {
            FirestoreRepository.observeTodayWaterLog(uid).collect {
                todayWaterMl = it?.millilitersConsumed ?: 0
                hasLoadedWater = true
            }
        }
    }
}
