@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.apexfitness.ui.theme

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// Motion helpers. Springs for movement, fade tweens for fades, never linear.
// Everything turns instant when the system animation scale is 0.

// Durations in milliseconds
object Motion {
    // Button and toggle feedback
    const val Micro = 120
    // Fades
    const val Fade = 250
    // Most transitions
    const val Standard = 300
    // Big screen transitions
    const val Screen = 360
    // Rings and bars filling up
    const val Progress = 700
    // Numbers counting up
    const val CountUp = 800
    // Light and dark theme fade
    const val ThemeCrossfade = 300
    // Delay between list items
    const val Stagger = 50

    // How far screens slide
    val ScreenSlide: Dp = 24.dp
    // How far list items rise
    val ListRise: Dp = 12.dp
    // Press scale for buttons and cards
    const val PressedScale = 0.97f
}

// ---- Reduced motion ----

// False when the user turned system animations off
val LocalMotionEnabled = staticCompositionLocalOf { true }

private fun readMotionEnabled(context: Context): Boolean = runCatching {
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
}.getOrDefault(true)

// Reads the animation scale from the system
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(readMotionEnabled(context)) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled = readMotionEnabled(context)
            }
        }
        runCatching {
            context.contentResolver.registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
                false,
                observer
            )
        }
        onDispose { runCatching { context.contentResolver.unregisterContentObserver(observer) } }
    }
    return enabled
}

// ---- Animation specs ----

// Spring for movement and size
fun <T> apexSpring(
    enabled: Boolean = true,
    dampingRatio: Float = 0.85f,
    stiffness: Float = Spring.StiffnessMediumLow
): FiniteAnimationSpec<T> =
    if (enabled) spring(dampingRatio = dampingRatio, stiffness = stiffness) else snap()

// Fade tween, never linear
fun <T> apexTween(
    enabled: Boolean = true,
    durationMillis: Int = Motion.Fade,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing
): FiniteAnimationSpec<T> =
    if (enabled) tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = easing) else snap()

// Spring that respects the reduced motion setting
@Composable
fun <T> motionSpring(
    dampingRatio: Float = 0.85f,
    stiffness: Float = Spring.StiffnessMediumLow
): FiniteAnimationSpec<T> = apexSpring(LocalMotionEnabled.current, dampingRatio, stiffness)

// Tween that respects the reduced motion setting
@Composable
fun <T> motionTween(
    durationMillis: Int = Motion.Fade,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing
): FiniteAnimationSpec<T> = apexTween(LocalMotionEnabled.current, durationMillis, delayMillis, easing)

// ---- Haptics ----

// Light vibrations. It uses the view's haptic feedback, so the system touch feedback setting is respected.
class Haptics internal constructor(private val view: View) {
    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun toggle(on: Boolean) {
        val constant = when {
            Build.VERSION.SDK_INT >= 34 ->
                if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
            else -> HapticFeedbackConstants.CLOCK_TICK
        }
        view.performHapticFeedback(constant)
    }

    fun confirm() {
        val constant = if (Build.VERSION.SDK_INT >= 30) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        view.performHapticFeedback(constant)
    }

    fun soft() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

// ---- Press feedback ----

// Shrinks the element slightly while it is pressed
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = Motion.PressedScale
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = motionSpring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// Clickable card or row that shrinks when pressed and gives a small vibration. Keep the tap area at least 48dp.
// Put it before clip and background so the whole card scales.
@Composable
fun Modifier.apexClickable(
    enabled: Boolean = true,
    haptic: Boolean = true,
    pressedScale: Float = Motion.PressedScale,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberHaptics()
    return this
        .pressScale(interactionSource, pressedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button
        ) {
            if (haptic) haptics.tick()
            onClick()
        }
}

// ---- Numbers, progress and list animations ----

// Counts up from 0 to the target the first time, then moves smoothly to new values
@Composable
fun rememberCountUp(target: Float, durationMillis: Int = Motion.CountUp): State<Float> {
    val enabled = LocalMotionEnabled.current
    val animatable = remember { Animatable(if (enabled) 0f else target) }
    LaunchedEffect(target, enabled) {
        if (enabled) {
            animatable.animateTo(target, tween(durationMillis, easing = FastOutSlowInEasing))
        } else {
            animatable.snapTo(target)
        }
    }
    return animatable.asState()
}

// Same as above but for whole numbers
@Composable
fun rememberCountUpInt(target: Int, durationMillis: Int = Motion.CountUp): State<Int> {
    val animated = rememberCountUp(target.toFloat(), durationMillis)
    return remember(animated) { derivedStateOf { animated.value.roundToInt() } }
}

// Progress from 0 to 1 for bars and rings. It fills the first time, then moves smoothly.
@Composable
fun rememberAnimatedProgress(target: Float): State<Float> {
    val enabled = LocalMotionEnabled.current
    val animatable = remember { Animatable(if (enabled) 0f else target.coerceIn(0f, 1f)) }
    var firstDone by remember { mutableStateOf(!enabled) }
    LaunchedEffect(target, enabled) {
        val clamped = target.coerceIn(0f, 1f)
        when {
            !enabled -> animatable.snapTo(clamped)
            !firstDone -> {
                firstDone = true
                animatable.animateTo(clamped, tween(Motion.Progress, easing = EaseOutCubic))
            }
            else -> animatable.animateTo(clamped, spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow))
        }
    }
    return animatable.asState()
}

// Remembers which list animations already played this session
object EntranceMemory {
    val played = HashSet<String>()
}

// Fades a list item in and moves it up 12dp, with a small delay per item. It only plays once,
// so pass a stable key.
@Composable
fun Modifier.staggeredEntrance(index: Int, key: String? = null, rise: Dp = Motion.ListRise): Modifier {
    val enabled = LocalMotionEnabled.current
    var playedLocal by rememberSaveable { mutableStateOf(false) }
    val alreadyPlayed = playedLocal || (key != null && key in EntranceMemory.played)
    val progress = remember { Animatable(if (alreadyPlayed || !enabled) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!alreadyPlayed && enabled) {
            delay(index.coerceIn(0, 8) * Motion.Stagger.toLong())
            progress.animateTo(1f, tween(Motion.Standard, easing = FastOutSlowInEasing))
        }
        playedLocal = true
        if (key != null) EntranceMemory.played.add(key)
    }
    val risePx = with(LocalDensity.current) { rise.toPx() }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * risePx
    }
}

// ---- Shared element transitions ----

// Provided by the SharedTransitionLayout around the NavHost (null in previews)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

// Animation scope of the current screen (null in previews)
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Remembers which routine card was tapped, so the session screen can grow out of it. Set it just before navigating.
object SharedKeys {
    var lastRoutine: String = ""
    // Same thing for the routine list to editor
    var lastEditor: String = ""
}

// Makes a card grow into the detail screen. Give both the same key.
// Put it before clip and background.
@Composable
fun Modifier.sharedCardBounds(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalNavAnimatedScope.current
    val enabled = LocalMotionEnabled.current
    if (sharedScope == null || visibilityScope == null || !enabled || key.isBlank()) return this
    return with(sharedScope) {
        this@sharedCardBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ ->
                spring<Rect>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            }
        )
    }
}

// Preview of a pressable card with a counting number
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MotionPreview() {
    ApexFitnessTheme(darkTheme = false) {
        val count by rememberCountUpInt(1240)
        Column(modifier = Modifier.padding(Dimens.ScreenEdge)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 0)
                    .apexClickable(onClick = {})
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Dimens.Space3)
            ) {
                Text(text = "$count", style = ApexText.Numeral, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "XP THIS WEEK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
