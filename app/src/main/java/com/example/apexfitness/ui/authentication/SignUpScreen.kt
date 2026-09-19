package com.example.apexfitness.ui.authentication

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.data.FirestoreRepository
import com.example.apexfitness.data.UserProfile
import com.example.apexfitness.ui.theme.ApexFitnessTheme
import com.example.apexfitness.ui.theme.ApexPrimaryButton
import com.example.apexfitness.ui.theme.ApexTextField
import com.example.apexfitness.ui.theme.Dimens
import com.example.apexfitness.ui.theme.LocalMotionEnabled
import com.example.apexfitness.ui.theme.Motion
import com.example.apexfitness.ui.theme.PillShape
import com.example.apexfitness.ui.theme.apex
import com.example.apexfitness.ui.theme.apexClickable
import com.example.apexfitness.ui.theme.apexSpring
import com.example.apexfitness.ui.theme.apexTween
import com.example.apexfitness.ui.theme.glassScreenBackground
import com.example.apexfitness.ui.theme.rememberGlassState
import com.example.apexfitness.ui.theme.staggeredEntrance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

// Sign up screen

@Composable
fun SignUpScreen(navController: NavController, formState: OnboardingFormState) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val motionEnabled = LocalMotionEnabled.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        when {
            name.isBlank() -> errorMessage = "Enter your name"
            email.isBlank() -> errorMessage = "Enter your email"
            password.length < 6 -> errorMessage = "Password must be at least 6 characters"
            password != confirmPassword -> errorMessage = "Passwords don't match"
            else -> {
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authService.signUpWithEmail(email, password)
                    result.onSuccess { user ->
                        formState.name = name
                        formState.email = email
                        val profile = UserProfile(
                            uid = user.uid,
                            name = name,
                            email = email,
                            onboardingComplete = false
                        )
                        runCatching { FirestoreRepository.saveProfile(profile) }
                        isLoading = false
                        navController.navigate("getStarted") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                    result.onFailure {
                        isLoading = false
                        errorMessage = it.localizedMessage ?: "Couldn't create your account"
                    }
                }
            }
        }
    }

    fun goToOnboardingAfterGoogleSignUp(uid: String, displayName: String?, googleEmail: String?) {
        val resolvedName = displayName.orEmpty()
        val resolvedEmail = googleEmail.orEmpty()
        formState.name = resolvedName
        formState.email = resolvedEmail
        val profile = UserProfile(
            uid = uid,
            name = resolvedName,
            email = resolvedEmail,
            onboardingComplete = false
        )
        coroutineScope.launch {
            runCatching { FirestoreRepository.saveProfile(profile) }
            isLoading = false
            navController.navigate("getStarted") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authService.signInWithGoogle(idToken)
                    result.onSuccess { user ->
                        goToOnboardingAfterGoogleSignUp(user.uid, account.displayName, account.email)
                    }
                    result.onFailure {
                        isLoading = false
                        errorMessage = it.localizedMessage ?: "Google sign-up failed"
                    }
                }
            } else {
                errorMessage = "Google sign-up failed"
            }
        } catch (e: ApiException) {
            errorMessage = "Google sign-up failed"
        }
    }

    val glassState = rememberGlassState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = Dimens.ScreenEdge)
                .padding(top = Dimens.Space3)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(Dimens.MinTouchTarget)
                        .apexClickable { navController.popBackStack() }
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
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimens.Space2))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.staggeredEntrance(index = 0, key = "signup-title")
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.apex.accentSoft)
                            .border(Dimens.Hairline, MaterialTheme.apex.accent.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space3))
                    Text(
                        text = "Create Your Account",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Start building your best self today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 1, key = "signup-form"),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
                    ApexTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = "Full Name",
                        leadingIcon = Icons.Outlined.Person,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ApexTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = "Email Address",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ApexTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ApexTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = "Confirm Password",
                        leadingIcon = Icons.Outlined.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(apexTween(motionEnabled, Motion.Fade)) + expandVertically(apexSpring(motionEnabled)),
                        exit = fadeOut(apexTween(motionEnabled, Motion.Micro)) + shrinkVertically(apexSpring(motionEnabled))
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.apex.errorText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Space3))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 2, key = "signup-divider"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.Hairline)
                            .background(MaterialTheme.apex.hairline)
                    )
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.apex.mutedText,
                        modifier = Modifier.padding(horizontal = Dimens.Space2)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.Hairline)
                            .background(MaterialTheme.apex.hairline)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space3))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 3, key = "signup-google")
                        .apexClickable {
                            errorMessage = null
                            googleLauncher.launch(authService.getGoogleSignInClient().signInIntent)
                        }
                        .heightIn(min = 52.dp)
                        .clip(PillShape)
                        .border(Dimens.Hairline, MaterialTheme.apex.mutedText.copy(alpha = 0.5f), PillShape)
                        .padding(horizontal = Dimens.Space2),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = com.example.apexfitness.R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.Space1 + 4.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space2))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.Space3),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApexPrimaryButton(
                    text = if (isLoading) "Creating account" else "Create Account",
                    onClick = { submit() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                    Box(
                        modifier = Modifier
                            .apexClickable { navController.popBackStack() }
                            .heightIn(min = 56.dp)
                            .padding(horizontal = Dimens.Space1),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.apex.accentText
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sign up light")
@Composable
fun SignUpScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        SignUpScreen(navController = rememberNavController(), formState = OnboardingFormState())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sign up dark")
@Composable
private fun SignUpScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        SignUpScreen(navController = rememberNavController(), formState = OnboardingFormState())
    }
}
