package com.example.apexfitness.ui.settings

import androidx.compose.runtime.collectAsState
import android.Manifest
import android.app.TimePickerDialog
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

    val motionEnabled = LocalMotionEnabled.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingsHeader(onBack = { navController.popBackStack() })

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
                    SettingsToggleRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        subtitle = if (isDarkMode) "On" else "Off",
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }

            SettingsSection(
                label = "Units",
                modifier = Modifier.staggeredEntrance(index = 1, key = "settings-units")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.MonitorWeight,
                        title = "Use pounds (lb)",
                        subtitle = if (useLbs) "Weights are shown in lb" else "Weights are shown in kg",
                        checked = useLbs,
                        onCheckedChange = { UnitPreferences.setUseLbs(context.applicationContext, it) }
                    )
                }
            }

            SettingsSection(
                label = "Notifications",
                modifier = Modifier.staggeredEntrance(index = 1, key = "settings-notifications")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Workout Reminders",
                        subtitle = if (notificationsEnabled) {
                            "On · ${formatReminderTime(reminderHour, reminderMinute)}"
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
                                title = "Reminder Time · ${formatReminderTime(reminderHour, reminderMinute)}",
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute -> onReminderTimeChange(hour, minute) },
                                        reminderHour,
                                        reminderMinute,
                                        false
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(
                label = "Account",
                modifier = Modifier.staggeredEntrance(index = 2, key = "settings-account")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.Person,
                        title = "Edit Profile",
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
                modifier = Modifier.staggeredEntrance(index = 3, key = "settings-data")
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
                modifier = Modifier.staggeredEntrance(index = 3, key = "settings-about")
            ) {
                SettingsCard(glassState = glassState) {
                    SettingsNavRow(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy Policy",
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
                        value = "1.0.0"
                    )
                }
            }

            Text(
                text = "ApexFitness - built to help you plan, train and track, all in one place.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.apex.mutedText,
                modifier = Modifier
                    .padding(vertical = Dimens.Space2)
                    .staggeredEntrance(index = 4, key = "settings-footer")
            )

            Spacer(modifier = Modifier.height(Dimens.Space3))
        }
    }
}

// Formats the time for display, e.g. "6:00 PM"
private fun formatReminderTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space1, vertical = Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .apexClickable(onClick = onBack)
                .clip(CircleShape)
                .border(Dimens.Hairline, MaterialTheme.apex.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
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
private fun RowIcon(icon: ImageVector, tint: Color = MaterialTheme.apex.accentText) {
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
    tint: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon, tint ?: MaterialTheme.apex.accentText)
        Spacer(modifier = Modifier.width(Dimens.Space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
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
