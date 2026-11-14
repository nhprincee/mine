package com.example.ui.security

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.KharcheeArchitecturalAMark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.SecurityLockManager

/**
 * Full-screen Glassmorphic Obsidian App-Lock Overlay with:
 * - Glowing "A" Kharchee architectural emblem
 * - Automatic system BiometricPrompt trigger (Fingerprint / Face)
 * - Tactile 4-digit PIN pad with haptic feedback on every tap
 */
@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val headingFont = LocalAppFontFamily.current

    var enteredPin by remember { mutableStateOf("") }
    var statusText by remember {
        mutableStateOf("Touch fingerprint sensor or enter your 4-digit PIN")
    }
    var hasError by remember { mutableStateOf(false) }

    // Block back press while vault is locked
    BackHandler(enabled = true) {
        // Keep lock screen active until user authenticates
    }

    fun triggerKeypadHaptic(isErrorPulse: Boolean = false) {
        runCatching {
            haptic.performHapticFeedback(
                if (isErrorPulse) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            if (isErrorPulse) 65L else 18L,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isErrorPulse) 65L else 18L)
                }
            }
        }
    }

    fun promptBiometrics() {
        val activity = context as? Activity ?: return
        SecurityLockManager.authenticateWithBiometrics(
            activity = activity,
            onAuthenticated = {
                triggerKeypadHaptic(false)
                onUnlocked()
            },
            onStatusOrFallback = { msg ->
                statusText = msg
            }
        )
    }

    // Automatically trigger the system BiometricPrompt when the lock screen appears
    LaunchedEffect(Unit) {
        promptBiometrics()
    }

    fun onDigitTapped(digit: String) {
        if (enteredPin.length >= 4) return
        triggerKeypadHaptic(false)
        hasError = false
        val nextPin = enteredPin + digit
        enteredPin = nextPin

        if (nextPin.length == 4) {
            val verified = SecurityLockManager.verifyMasterPin(context, nextPin)
            if (verified) {
                statusText = "Vault Unlocked"
                onUnlocked()
            } else {
                triggerKeypadHaptic(isErrorPulse = true)
                hasError = true
                statusText = "Incorrect 4-digit PIN. Try again or use Fingerprint."
                enteredPin = ""
            }
        }
    }

    fun onBackspaceTapped() {
        if (enteredPin.isNotEmpty()) {
            triggerKeypadHaptic(false)
            hasError = false
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04040A),
                        Color(0xFF0B0719),
                        Color(0xFF05050B)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("kharchee_app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic radial obsidian mesh glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.26f),
                        Color(0xFF06B6D4).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.24f),
                    radius = (w * 0.65f).coerceAtLeast(1f)
                ),
                center = Offset(w * 0.5f, h * 0.24f),
                radius = (w * 0.65f).coerceAtLeast(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Glowing "A" Kharchee Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(28.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFA855F7))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A1038), Color(0xFF090714))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFD8B4FE), Color(0xFF7C3AED), Color(0xFF38BDF8))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                KharcheeArchitecturalAMark(size = 42.dp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "KHARCHEE SOVEREIGN VAULT LOCKED",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                        color = Color(0xFFD8B4FE)
                    )
                }

                Text(
                    text = "Biometric & PIN Shield",
                    fontFamily = headingFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = if (hasError) Color(0xFFFF4D8D) else Color(0xFFB8B0D4),
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.testTag("app_lock_status_text")
                )
            }

            // 4-Digit PIN Indicator Dots inside Glassmorphic Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF100C20).copy(alpha = 0.88f))
                    .border(
                        width = 1.dp,
                        color = if (hasError) Color(0xFFFF4D8D) else Color(0xFF3B2B63),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 28.dp, vertical = 14.dp)
                    .testTag("app_lock_pin_dots_row"),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            hasError -> Color(0xFFFF4D8D)
                            isFilled -> Color(0xFF2EE59D)
                            else -> Color(0xFF332752)
                        },
                        animationSpec = tween(durationMillis = 150),
                        label = "pinDotColor_$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isFilled) 15.dp else 13.dp)
                            .shadow(if (isFilled) 10.dp else 0.dp, CircleShape, spotColor = dotColor)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = 1.dp,
                                color = if (isFilled) Color.White.copy(alpha = 0.7f) else Color(0xFF564485),
                                shape = CircleShape
                            )
                    )
                }
            }

            // 4-Digit Tactile Keypad (Rows: 1-2-3, 4-5-6, 7-8-9, Biometric-0-Backspace)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                ).forEach { rowDigits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowDigits.forEach { digit ->
                            PinKeypadDigitButton(
                                digit = digit,
                                onClick = { onDigitTapped(digit) }
                            )
                        }
                    }
                }

                // Bottom Row: Fingerprint | 0 | Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric Trigger Key
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF140E29))
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.7f), CircleShape)
                            .clickable {
                                triggerKeypadHaptic(false)
                                promptBiometrics()
                            }
                            .testTag("app_lock_biometric_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Unlock with Fingerprint",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    PinKeypadDigitButton(
                        digit = "0",
                        onClick = { onDigitTapped("0") }
                    )

                    // Backspace Key
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF140E29))
                            .border(1.dp, Color(0xFF3A2D5C), CircleShape)
                            .clickable { onBackspaceTapped() }
                            .testTag("app_lock_backspace_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete Digit",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Re-trigger Biometric Pill
            Surface(
                color = Color(0xFF140E2A),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, Color(0xFF7C3AED).copy(alpha = 0.65f)),
                modifier = Modifier
                    .clickable {
                        triggerKeypadHaptic(false)
                        promptBiometrics()
                    }
                    .testTag("app_lock_use_fingerprint_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF2EE59D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap to Authenticate with Fingerprint / Face",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeypadDigitButton(
    digit: String,
    onClick: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Box(
        modifier = Modifier
            .size(68.dp)
            .shadow(10.dp, CircleShape, spotColor = Color(0xFF7C3AED).copy(alpha = 0.35f))
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B1336), Color(0xFF0F0B1F))
                )
            )
            .border(1.dp, Color(0xFF4C3785), CircleShape)
            .clickable { onClick() }
            .testTag("app_lock_key_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            fontFamily = headingFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

/**
 * Dialog shown when the user toggles ON "Biometric & App PIN Lock" for the first time
 * (or taps "Change 4-Digit PIN") in Settings (`MyProfileSheet.kt`).
 */
@Composable
fun SetFourDigitPinDialog(
    onDismiss: () -> Unit,
    onPinConfigured: (pin: String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0E0B1C))
                .border(1.4.dp, Color(0xFFA855F7), RoundedCornerShape(24.dp))
                .padding(22.dp)
                .testTag("set_four_digit_pin_dialog"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Set 4-Digit Security PIN",
                        fontFamily = headingFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "PBKDF2/SHA-256 Encrypted + Fingerprint Lock",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFD8B4FE)
                    )
                }
            }

            Text(
                text = "Create your 4-digit master backup PIN. You can unlock KHARCHEE using either your Fingerprint/Face or this 4-digit PIN.",
                fontSize = 12.sp,
                color = Color(0xFFB8B0D4),
                lineHeight = 17.sp
            )

            OutlinedTextField(
                value = firstPin,
                onValueChange = {
                    firstPin = it.filter { ch -> ch.isDigit() }.take(4)
                    errorMessage = null
                },
                label = { Text("Enter 4-Digit PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF3A2D5C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_pin_first_input")
            )

            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    confirmPin = it.filter { ch -> ch.isDigit() }.take(4)
                    errorMessage = null
                },
                label = { Text("Confirm 4-Digit PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF3A2D5C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_pin_confirm_input")
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF4D8D)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, Color(0xFF3A2D5C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color(0xFFB8B0D4))
                }

                Button(
                    onClick = {
                        if (firstPin.length != 4) {
                            errorMessage = "Please enter a 4-digit PIN (e.g. 2580)"
                            return@Button
                        }
                        if (firstPin != confirmPin) {
                            errorMessage = "PINs do not match. Please re-enter."
                            return@Button
                        }
                        val saved = SecurityLockManager.setMasterPinAndEnableLock(
                            context = context,
                            pin4Digits = firstPin,
                            enableAppLock = true
                        )
                        if (saved) {
                            // Optionally prompt fingerprint confirmation if Activity is available
                            (context as? Activity)?.let { act ->
                                SecurityLockManager.authenticateWithBiometrics(
                                    activity = act,
                                    title = "Confirm Fingerprint for KHARCHEE",
                                    subtitle = "Biometric & 4-Digit PIN Lock Activated",
                                    onAuthenticated = {}
                                )
                            }
                            onPinConfigured(firstPin)
                        } else {
                            errorMessage = "Could not save PIN. Try again."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2EE59D),
                        contentColor = Color(0xFF040508)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("setup_pin_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enable Lock", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
