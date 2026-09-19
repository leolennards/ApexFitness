package com.example.apexfitness.ui.authentication.login

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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.authentication.AuthService
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

// Sign in screen

@Composable
fun SignInScreen(navController: NavController) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val motionEnabled = LocalMotionEnabled.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    fun goToMain() {
        navController.navigate("main") {
            popUpTo("welcome") { inclusive = true }
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
                    isLoading = false
                    result.onSuccess { goToMain() }
                    result.onFailure { errorMessage = it.localizedMessage ?: "Google sign-in failed" }
                }
            } else {
                errorMessage = "Google sign-in failed"
            }
        } catch (e: ApiException) {
            errorMessage = "Google sign-in failed"
        }
    }

    fun signInWithEmail() {
        when {
            email.isBlank() || password.isBlank() -> {
                errorMessage = "Enter your email and password"
            }
            else -> {
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authService.signInWithEmail(email, password)
                    isLoading = false
                    result.onSuccess { goToMain() }
                    result.onFailure {
                        errorMessage = it.localizedMessage ?: "Couldn't sign in"
                    }
                }
            }
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
                .padding(top = Dimens.Space3, bottom = Dimens.Space3)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Box(
                    modifier = Modifier
                        .apexClickable { navController.navigate("signup") }
                        .heightIn(min = Dimens.MinTouchTarget)
                        .padding(horizontal = Dimens.Space1),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.apex.accentText
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(Dimens.Space3))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.staggeredEntrance(index = 0, key = "signin-title")
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
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.apex.accentText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Space3))
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Space1))
                    Text(
                        text = "Sign in to continue your fitness journey",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.apex.mutedText,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Space4))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 1, key = "signin-form"),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
                ) {
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
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.MinTouchTarget)
                                    .apexClickable { passwordVisible = !passwordVisible },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = MaterialTheme.apex.mutedText
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    AnimatedVisibility(
                        visible = infoMessage != null,
                        enter = fadeIn(apexTween(motionEnabled, Motion.Fade)) + expandVertically(apexSpring(motionEnabled)),
                        exit = fadeOut(apexTween(motionEnabled, Motion.Micro)) + shrinkVertically(apexSpring(motionEnabled))
                    ) {
                        Text(
                            text = infoMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.apex.accentText
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .apexClickable {
                                    if (email.isBlank()) {
                                        errorMessage = "Enter your email above first"
                                    } else {
                                        coroutineScope.launch {
                                            val result = authService.sendPasswordReset(email)
                                            result.onSuccess { infoMessage = "Password reset email sent" }
                                            result.onFailure { errorMessage = it.localizedMessage ?: "Couldn't send reset email" }
                                        }
                                    }
                                }
                                .heightIn(min = Dimens.MinTouchTarget)
                                .padding(horizontal = Dimens.Space1),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.apex.accentText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Space2))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(index = 2, key = "signin-social"),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1 + 4.dp)
                ) {
                    SocialButton(
                        text = "Continue with Google",
                        iconRes = com.example.apexfitness.R.drawable.ic_google_logo,
                        onClick = {
                            errorMessage = null
                            googleLauncher.launch(authService.getGoogleSignInClient().signInIntent)
                        }
                    )

                    // Apple sign-in is not built yet, so it is greyed out and says "Coming Soon"
                    SocialButton(
                        text = "Continue with Apple",
                        iconRes = com.example.apexfitness.R.drawable.ic_apple_logo,
                        muted = true,
                        badge = "COMING SOON",
                        onClick = { infoMessage = "Apple sign-in is coming soon" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Space2))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApexPrimaryButton(
                    text = if (isLoading) "Signing in" else "Sign In",
                    onClick = { signInWithEmail() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.apex.mutedText
                    )
                    Box(
                        modifier = Modifier
                            .apexClickable { navController.navigate("signup") }
                            .heightIn(min = 56.dp)
                            .padding(horizontal = Dimens.Space1),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.apex.accentText
                        )
                    }
                }
            }
        }
    }
}

// Outlined pill for a social sign-in. muted greys it out for options that are not live yet.
@Composable
private fun SocialButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    muted: Boolean = false,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .apexClickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .clip(PillShape)
            .border(
                Dimens.Hairline,
                MaterialTheme.apex.mutedText.copy(alpha = if (muted) 0.3f else 0.5f),
                PillShape
            )
            .padding(horizontal = Dimens.Space2),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .alpha(if (muted) 0.5f else 1f)
        )
        Spacer(modifier = Modifier.width(Dimens.Space1 + 4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (muted) MaterialTheme.apex.mutedText else MaterialTheme.colorScheme.onSurface
        )
        if (badge != null) {
            Spacer(modifier = Modifier.width(Dimens.Space1))
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.apex.mutedText
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sign in light")
@Composable
fun SignInScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        SignInScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sign in dark")
@Composable
private fun SignInScreenDarkPreview() {
    ApexFitnessTheme(darkTheme = true) {
        SignInScreen(navController = rememberNavController())
    }
}
