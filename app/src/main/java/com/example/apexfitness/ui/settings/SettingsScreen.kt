package com.example.apexfitness.ui.settings

import androidx.compose.runtime.collectAsState
import android.Manifest
import android.content.Context
import java.util.Calendar
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.apexfitness.ui.export.CsvExport
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.widthIn
import com.example.apexfitness.ui.theme.ApexScreenHeader
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.motionTween
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.notifications.NotificationScheduler
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexShapes
import com.example.apexfitness.ui.theme.ApexTextField
import com.example.apexfitness.ui.theme.CardShape
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.LocalMotionEnabled
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.apexSpring
import com.example.apexfitness.ui.theme.apexTween
import com.example.apexfitness.ui.theme.glassPanel
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.rememberHaptics
import com.example.apexfitness.ui.theme.staggeredEntrance

// Settings screen

// Where the privacy policy is hosted (the page is in the docs folder of the repo, served with GitHub Pages)
private const val PRIVACY_POLICY_URL = "https://leolennards.github.io/ApexFitness/privacy.html"
private const val FEEDBACK_EMAIL = "leo.lukelennards@gmail.com"

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onToggleNotifications: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit
) {
    val glassState = rememberGlassState()
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleNotifications(true)
        }
    }

    // Keeps the reminder alarms in sync with the switch, the time and the days routines are assigned to
    LaunchedEffect(notificationsEnabled, reminderHour, reminderMinute) {
        val appContext = context.applicationContext
        if (notificationsEnabled) {
            val uid = authService.getCurrentUser()?.uid
            if (uid != null) {
                val routines = FirestoreRepository.getRoutines(uid)
                val days = routines
                    .flatMap { it.days }
                    .mapNotNull { NotificationScheduler.dayCodeToCalendarDay(it) }
                    .toSet()
                NotificationScheduler.scheduleAll(appContext, days, reminderHour, reminderMinute)
            }
        } else {
            NotificationScheduler.cancelAll(appContext)
        }
    }

    val useLbs = UnitPreferences.useLbs.collectAsState().value

    val scope = rememberCoroutineScope()

    // Export workouts as a CSV file
    var exporting by remember { mutableStateOf(false) }
    fun exportWorkouts() {
        val id = authService.getCurrentUser()?.uid ?: return
        if (exporting) return
        exporting = true
        scope.launch {
            try {
                val file = CsvExport.createWorkoutsFile(context.applicationContext, id, useLbs)
                if (file == null) {
                    Toast.makeText(context, "No workouts to export yet", Toast.LENGTH_SHORT).show()
                } else {
                    CsvExport.share(context, file)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not export your workouts", Toast.LENGTH_SHORT).show()
            }
            exporting = false
        }
    }

    // Delete account
    val usesPassword = remember { authService.usesPassword() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var passwordInput by remember { mutableStateOf("") }

    // Confirms who is asking, wipes the data, then removes the account and goes back to Welcome
    fun runDelete(reauth: suspend () -> Result<Unit>) {
        scope.launch {
            deleting = true
            deleteError = null
            if (reauth().isFailure) {
                deleteError = if (usesPassword) "That password is not right, please try again." else "I could not confirm your Google account."
                deleting = false
                return@launch
            }
            val uid = authService.getCurrentUser()?.uid
            try {
                if (uid != null) FirestoreRepository.deleteAllUserData(uid)
            } catch (e: Exception) {
                deleteError = "Could not delete your data. Check your connection and try again."
                deleting = false
                return@launch
            }
            if (authService.deleteCurrentUser().isFailure) {
                deleteError = "Your data was removed but the account could not be deleted. Please try again."
                deleting = false
                return@launch
            }
            NotificationScheduler.cancelAll(context.applicationContext)
            // Progress photos only live on this phone, so they go too
            com.example.apexfitness.ui.body.ProgressPhotoStore.deleteAll(context.applicationContext)
            authService.signOut()
            navController.navigate("welcome") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val googleConfirmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                runDelete { authService.reauthWithGoogle(idToken) }
            } else {
                deleteError = "I could not confirm your Google account."
            }
        } catch (e: ApiException) {
            deleteError = "Google sign in was cancelled."
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleting) showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = ApexShapes.large,
            title = {
                Text(
                    text = "Delete account?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
                    Text(
                        text = "This permanently deletes your account and everything in it: routines, workouts, records, challenges and progress. It cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                    if (usesPassword) {
                        ApexTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = "Enter your password to confirm",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = deleteError != null,
                            container = MaterialTheme.colorScheme.background
                        )
                    } else {
                        Text(
                            text = "You will be asked to sign in with Google once more to confirm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.apex.mutedText
                        )
                    }
                    deleteError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting && (!usesPassword || passwordInput.isNotEmpty()),
                    onClick = {
                        if (usesPassword) {
                            runDelete { authService.reauthWithPassword(passwordInput) }
                        } else {
                            googleConfirmLauncher.launch(authService.getGoogleSignInClient().signInIntent)
                        }
                    },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(
                        text = if (deleting) "Deleting..." else "Delete",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
                ) {
                    Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Reminder time picker (Material 3, so it matches the rest of the app)
    var showTimePicker by remember { mutableStateOf(false) }
    val use24Hour = remember { android.text.format.DateFormat.is24HourFormat(context) }
    if (showTimePicker) {
        ReminderTimeDialog(
            hour = reminderHour,
            minute = reminderMinute,
            is24Hour = use24Hour,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onReminderTimeChange(hour, minute)
            }
        )
    }

    // Real version name from the build, so I never have to update it by hand
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }

    val motionEnabled = LocalMotionEnabled.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ApexScreenHeader(title = "Settings", onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenEdge)
        ) {
            Spacer(modifier = Modifier.height(Dimens.Space1))

            SettingsSection(
                label = "Appearance",
                modifier = Modifier.staggeredEntrance(index = 0, key = "settings-appearance")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsSegmentedRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Theme",
                        options = listOf("Light", "Dark"),
                        selectedIndex = if (isDarkMode) 1 else 0,
                        onSelect = { index -> onToggleDarkMode(index == 1) }
                    )
                }
            }

            SettingsSection(
                label = "Units",
                modifier = Modifier.staggeredEntrance(index = 1, key = "settings-units")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsSegmentedRow(
                        icon = Icons.Outlined.MonitorWeight,
                        title = "Weight unit",
                        options = listOf("kg", "lb"),
                        selectedIndex = if (useLbs) 1 else 0,
                        onSelect = { index -> UnitPreferences.setUseLbs(context.applicationContext, index == 1) }
                    )
                }
            }

            SettingsSection(
                label = "Notifications",
                modifier = Modifier.staggeredEntrance(index = 2, key = "settings-notifications")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Workout Reminders",
                        subtitle = if (notificationsEnabled) {
                            "On · ${formatReminderTime(context, reminderHour, reminderMinute)}"
                        } else {
                            "Off"
                        },
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            val needsPermission = checked &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onToggleNotifications(checked)
                            }
                        }
                    )
                    AnimatedVisibility(
                        visible = notificationsEnabled,
                        enter = fadeIn(apexTween(motionEnabled, Motion.Fade)) +
                            expandVertically(apexSpring(motionEnabled)),
                        exit = fadeOut(apexTween(motionEnabled, Motion.Micro)) +
                            shrinkVertically(apexSpring(motionEnabled))
                    ) {
                        Column {
                            RowDivider()
                            SettingsNavRow(
                                icon = Icons.Outlined.AccessTime,
                                title = "Reminder time",
                                value = formatReminderTime(context, reminderHour, reminderMinute),
                                onClick = { showTimePicker = true }
                            )
                        }
                    }
                }
            }

            SettingsSection(
                label = "Account",
                modifier = Modifier.staggeredEntrance(index = 3, key = "settings-account")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.Person,
                        title = "Edit profile",
                        onClick = { navController.navigate("editProfile") }
                    )
                    RowDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Delete account",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = {
                            passwordInput = ""
                            deleteError = null
                            showDeleteDialog = true
                        }
                    )
                }
            }

            SettingsSection(
                label = "Data",
                modifier = Modifier.staggeredEntrance(index = 4, key = "settings-data")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.FileDownload,
                        title = if (exporting) "Preparing file..." else "Export workout history (CSV)",
                        onClick = { exportWorkouts() }
                    )
                }
            }

            SettingsSection(
                label = "About",
                modifier = Modifier.staggeredEntrance(index = 5, key = "settings-about")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy policy",
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))) }
                    )
                    RowDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.StarOutline,
                        title = "Rate the app",
                        onClick = {
                            val pkg = context.packageName
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
                            }
                        }
                    )
                    RowDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.MailOutline,
                        title = "Send feedback",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                                putExtra(Intent.EXTRA_SUBJECT, "ApexFitness feedback")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // No email app installed, nothing to do
                            }
                        }
                    )
                    RowDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Share,
                        title = "Share ApexFitness",
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out ApexFitness: https://play.google.com/store/apps/details?id=${context.packageName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(send, "Share ApexFitness"))
                        }
                    )
                    RowDivider()
                    SettingsInfoRow(
                        icon = Icons.Outlined.Info,
                        title = "Version",
                        value = versionName
                    )
                }
            }

            Text(
                text = "ApexFitness. Built to help you plan, train and track, all in one place.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier
                    .padding(vertical = Dimens.Space2)
                    .staggeredEntrance(index = 6, key = "settings-footer")
            )

            Spacer(modifier = Modifier.height(Dimens.Space3))
        }
    }
}

// Formats the time the way the phone is set up, e.g. "6:00 PM" or "18:00"
private fun formatReminderTime(context: Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return android.text.format.DateFormat.getTimeFormat(context).format(calendar.time)
}

// Material 3 time picker in a dialog, styled with the app colours
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = is24Hour)
    val apex = MaterialTheme.apex
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = ApexShapes.large,
        title = {
            Text(
                text = "Reminder time",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    clockDialColor = MaterialTheme.colorScheme.background,
                    clockDialSelectedContentColor = apex.onAccent,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    selectorColor = apex.accent,
                    periodSelectorBorderColor = apex.hairline,
                    periodSelectorSelectedContainerColor = apex.accentSoft,
                    periodSelectorSelectedContentColor = apex.accentText,
                    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                    periodSelectorUnselectedContentColor = apex.mutedText,
                    timeSelectorSelectedContainerColor = apex.accentSoft,
                    timeSelectorSelectedContentColor = apex.accentText,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(state.hour, state.minute) },
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = apex.accentText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)
            ) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

// Two or three choices side by side in a pill, e.g. Light / Dark or kg / lb.
// The chosen one is filled with the accent, like the active state of a switch.
@Composable
private fun SettingsSegmentedRow(
    icon: ImageVector,
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val haptics = rememberHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.background)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, PillShape)
                .padding(3.dp)
        ) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val fill by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.apex.accent else Color.Transparent,
                    animationSpec = motionTween(Motion.Fade),
                    label = "segmentFill"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.apex.onAccent else MaterialTheme.apex.mutedText,
                    animationSpec = motionTween(Motion.Fade),
                    label = "segmentText"
                )
                Box(
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .widthIn(min = 60.dp)
                        .clip(PillShape)
                        .background(fill)
                        .apexClickable(haptic = false, pressedScale = 1f) {
                            if (!selected) {
                                haptics.toggle(true)
                                onSelect(index)
                            }
                        }
                        .semantics {
                            role = Role.RadioButton
                            this.selected = selected
                        }
                        .padding(horizontal = Dimens.Space2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = option, style = MaterialTheme.typography.labelLarge, color = textColor)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = Dimens.Space3)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText,
            modifier = Modifier.padding(start = 4.dp, bottom = Dimens.Space1)
        )
        content()
    }
}

@Composable
private fun SettingsCard(glassState: com.example.apexfitness.ui.theme.GlassState, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape),
        content = content
    )
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space2)
            .height(Dimens.Hairline)
            .background(MaterialTheme.apex.hairline)
    )
}

@Composable
private fun RowIcon(icon: ImageVector, tint: Color = MaterialTheme.apex.mutedText) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptics = rememberHaptics()
    val toggle = {
        haptics.toggle(!checked)
        onCheckedChange(!checked)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(haptic = false, onClick = toggle)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,  // the whole row is tappable
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.apex.mutedText,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedBorderColor = MaterialTheme.apex.mutedText.copy(alpha = 0.6f)
            ),
            modifier = Modifier.semantics { role = Role.Switch }
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color? = null,
    value: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon, tint ?: MaterialTheme.apex.mutedText)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.apex.mutedText)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.apex.mutedText
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Settings light")
@Composable
fun SettingsScreenPreview() {
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var reminderHour by remember { mutableStateOf(18) }
    var reminderMinute by remember { mutableStateOf(0) }
    ApexFitnessTheme(darkTheme = darkMode) {
        SettingsScreen(
            navController = rememberNavController(),
            isDarkMode = darkMode,
            onToggleDarkMode = { darkMode = it },
            notificationsEnabled = notificationsEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            onToggleNotifications = { notificationsEnabled = it },
            onReminderTimeChange = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Settings dark")
@Composable
private fun SettingsScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        SettingsScreen(
            navController = rememberNavController(),
            isDarkMode = true,
            onToggleDarkMode = {},
            notificationsEnabled = true,
            reminderHour = 6,
            reminderMinute = 30,
            onToggleNotifications = {},
            onReminderTimeChange = { _, _ -> }
        )
    }
}
