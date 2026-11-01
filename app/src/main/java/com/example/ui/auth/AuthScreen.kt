package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                if (authResult.user != null) {
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                if (authResult.user != null) {
                    onAuthSuccess()
                } else {
                    onAuthError("Authentication could not be verified by server.")
                }
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun normalizeE164PhoneNumber(rawInput: String): String? {
    val cleaned = rawInput.trim().replace(Regex("[\\s\\-()]"), "")
    if (cleaned.isEmpty()) return null
    return when {
        cleaned.startsWith("+") && cleaned.drop(1).all { it.isDigit() } && cleaned.length in 11..16 -> cleaned
        cleaned.length == 10 && cleaned.all { it.isDigit() } && cleaned.first() in '6'..'9' -> "+91$cleaned"
        cleaned.length == 11 && cleaned.startsWith("0") && cleaned.all { it.isDigit() } && cleaned[1] in '6'..'9' -> "+91${cleaned.drop(1)}"
        cleaned.length == 12 && cleaned.startsWith("91") && cleaned.all { it.isDigit() } -> "+$cleaned"
        else -> null
    }
}

fun startPhoneNumberOtpVerification(
    context: Context,
    rawPhoneNumber: String,
    resendingToken: PhoneAuthProvider.ForceResendingToken?,
    scope: CoroutineScope,
    onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
    onInstantVerified: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? Activity
    if (activity == null) {
        onError("Unable to start phone verification: Activity context required.")
        return
    }
    val e164Phone = normalizeE164PhoneNumber(rawPhoneNumber)
    if (e164Phone == null) {
        onError("Please enter a valid 10-digit Indian mobile number (e.g., 9876543210) or full +country code.")
        return
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            scope.launch {
                try {
                    val result = Firebase.auth.signInWithCredential(credential).await()
                    if (result.user != null) {
                        onInstantVerified()
                    } else {
                        onError("Verification completed but session could not be established.")
                    }
                } catch (e: Exception) {
                    onError(formatFirebaseAuthError(e))
                }
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("Auth", "Phone OTP verification failed", e)
            onError(formatFirebaseAuthError(e))
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            onCodeSent(verificationId, token)
        }
    }

    val optionsBuilder = PhoneAuthOptions.newBuilder(Firebase.auth)
        .setPhoneNumber(e164Phone)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)

    if (resendingToken != null) {
        optionsBuilder.setForceResendingToken(resendingToken)
    }

    PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
}

fun verifyPhoneOtpCode(
    verificationId: String,
    otpCode: String,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val trimmedOtp = otpCode.trim()
    if (trimmedOtp.length != 6 || !trimmedOtp.all { it.isDigit() }) {
        onError("Please enter the complete 6-digit numeric OTP sent to your phone.")
        return
    }
    if (verificationId.isBlank()) {
        onError("Verification session expired. Please request a new OTP.")
        return
    }

    val credential = PhoneAuthProvider.getCredential(verificationId, trimmedOtp)
    scope.launch {
        try {
            val authResult = Firebase.auth.signInWithCredential(credential).await()
            if (authResult.user != null) {
                onAuthSuccess()
            } else {
                onError("OTP verification failed to establish an authenticated session.")
            }
        } catch (e: Exception) {
            Log.e("Auth", "OTP code verification failed", e)
            onError(formatFirebaseAuthError(e))
        }
    }
}

private fun formatFirebaseAuthError(e: Exception): String {
    return when (e) {
        is FirebaseAuthInvalidCredentialsException ->
            "Invalid phone number or verification OTP code. Please double-check and try again."
        is FirebaseTooManyRequestsException ->
            "Too many verification requests from this device. Please wait a few minutes or sign in with Google."
        else -> e.localizedMessage ?: "Authentication failed. Please check your connection and try again."
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val headingFont = LocalAppFontFamily.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    // Auth Mode: "google" or "phone"
    var showPhoneAuthSection by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var resendCountdownSeconds by remember { mutableIntStateOf(0) }
    var showRecoveryHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    LaunchedEffect(resendCountdownSeconds) {
        if (resendCountdownSeconds > 0) {
            delay(1000L)
            resendCountdownSeconds -= 1
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = ChampagneGold,
        unfocusedLabelColor = MutedIvory
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient sovereign champagne glow at top
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ChampagneGold.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.14f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.14f),
                radius = size.width * 0.75f
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Luxury Sovereign Emblem
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val outerStroke = 1.5.dp.toPx()
                    val arcStroke = 3.5.dp.toPx()
                    drawCircle(
                        color = MutedPlatinum.copy(alpha = 0.18f),
                        style = Stroke(width = outerStroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                ChampagneGold.copy(alpha = 0.15f),
                                ChampagneGold,
                                MutedPlatinum,
                                ChampagneGold.copy(alpha = 0.15f)
                            )
                        ),
                        startAngle = -110f,
                        sweepAngle = 290f,
                        useCenter = false,
                        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ElevatedGraphite,
                                    RichGraphite,
                                    ObsidianBlack
                                )
                            )
                        )
                        .border(1.dp, HairlineGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        fontFamily = headingFont,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChampagneGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = RichGraphite,
                shape = RoundedCornerShape(50),
                modifier = Modifier.border(0.8.dp, HairlineGold, RoundedCornerShape(50))
            ) {
                Text(
                    text = "SOVEREIGN PRIVATE WEALTH OS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.8.sp,
                    color = ChampagneGold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "KHARCHEE",
                fontFamily = headingFont,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Private Banking Discipline, J.A.R.V.I.S. Voice Intelligence & Night Guard Impulse Shield.",
                style = MaterialTheme.typography.bodyLarge,
                color = MutedPlatinum,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (errorMessage != null) {
                Surface(
                    color = CrimsonRuby.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, CrimsonRuby.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("auth_error_banner")
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = WarmIvory,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (statusBanner != null) {
                Surface(
                    color = SophisticatedEmerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = statusBanner ?: "",
                        color = SophisticatedEmerald,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Primary Google Sign-In Button
            Button(
                onClick = {
                    errorMessage = null
                    statusBanner = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = coroutineScope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack,
                    disabledContainerColor = ChampagneGold.copy(alpha = 0.45f),
                    disabledContentColor = ObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(14.dp, RoundedCornerShape(16.dp))
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading && !showPhoneAuthSection) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ObsidianBlack,
                        strokeWidth = 2.2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enter Sovereign Vault with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Toggle for Phone Number OTP Authentication
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    statusBanner = null
                    showPhoneAuthSection = !showPhoneAuthSection
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    0.8.dp,
                    if (showPhoneAuthSection) SophisticatedEmerald else HairlinePlatinum
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = RichGraphite,
                    contentColor = WarmIvory
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("toggle_phone_otp_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Phone OTP Authentication",
                    tint = SophisticatedEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showPhoneAuthSection) "Hide Mobile OTP Verification" else "Sign In with Mobile Number (OTP)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // Expandable Real Firebase Phone OTP Card
            AnimatedVisibility(visible = showPhoneAuthSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "MOBILE OTP AUTHENTICATION · FIREBASE IDENTITY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = SophisticatedEmerald
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it
                            errorMessage = null
                        },
                        enabled = !isLoading && verificationId == null,
                        label = { Text("Mobile Number (+91 or 10-digit)") },
                        placeholder = { Text("9876543210") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input")
                    )

                    if (verificationId == null) {
                        Button(
                            onClick = {
                                errorMessage = null
                                statusBanner = null
                                isLoading = true
                                startPhoneNumberOtpVerification(
                                    context = context,
                                    rawPhoneNumber = phoneInput,
                                    resendingToken = resendToken,
                                    scope = coroutineScope,
                                    onCodeSent = { vId, token ->
                                        isLoading = false
                                        verificationId = vId
                                        resendToken = token
                                        resendCountdownSeconds = 60
                                        statusBanner = "6-digit OTP dispatched to ${normalizeE164PhoneNumber(phoneInput) ?: phoneInput}"
                                    },
                                    onInstantVerified = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isLoading && phoneInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedEmerald,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = ObsidianBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Send Verification OTP", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpInput = input
                                    errorMessage = null
                                }
                            },
                            enabled = !isLoading,
                            label = { Text("Enter 6-Digit SMS OTP") },
                            placeholder = { Text("••••••") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input")
                        )

                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                verifyPhoneOtpCode(
                                    verificationId = verificationId ?: "",
                                    otpCode = otpInput,
                                    scope = coroutineScope,
                                    onAuthSuccess = {
                                        isLoading = false
                                        otpInput = ""
                                        onAuthSuccess()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isLoading && otpInput.length == 6,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedEmerald,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = ObsidianBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Verify OTP & Enter Vault", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change Number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedPlatinum,
                                modifier = Modifier
                                    .clickable {
                                        verificationId = null
                                        otpInput = ""
                                        statusBanner = null
                                        errorMessage = null
                                    }
                                    .padding(vertical = 4.dp)
                            )

                            Text(
                                text = if (resendCountdownSeconds > 0) {
                                    "Resend OTP in ${resendCountdownSeconds}s"
                                } else {
                                    "Resend OTP"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (resendCountdownSeconds > 0) MutedIvory else ChampagneGold,
                                modifier = Modifier
                                    .clickable(enabled = resendCountdownSeconds == 0 && !isLoading) {
                                        errorMessage = null
                                        isLoading = true
                                        startPhoneNumberOtpVerification(
                                            context = context,
                                            rawPhoneNumber = phoneInput,
                                            resendingToken = resendToken,
                                            scope = coroutineScope,
                                            onCodeSent = { vId, token ->
                                                isLoading = false
                                                verificationId = vId
                                                resendToken = token
                                                resendCountdownSeconds = 60
                                                statusBanner = "Fresh 6-digit OTP sent."
                                            },
                                            onInstantVerified = {
                                                isLoading = false
                                                onAuthSuccess()
                                            },
                                            onError = { err ->
                                                isLoading = false
                                                errorMessage = err
                                            }
                                        )
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Luxury Feature Highlights Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuthFeatureRow(
                    icon = Icons.Default.Bolt,
                    tint = ChampagneGold,
                    title = "Instant UPI & SMS Telemetry",
                    subtitle = "1-tap quick logging & on-device Indian bank SMS intelligence"
                )
                HorizontalDivider(color = HairlinePlatinum, thickness = 0.5.dp)
                AuthFeatureRow(
                    icon = Icons.Default.Shield,
                    tint = SophisticatedEmerald,
                    title = "Night Guard & Pre-Spend Reflection",
                    subtitle = "Late-night impulse shield with brain diversion challenges & 5m cooldown"
                )
                HorizontalDivider(color = HairlinePlatinum, thickness = 0.5.dp)
                AuthFeatureRow(
                    icon = Icons.Default.Groups,
                    tint = MutedPlatinum,
                    title = "Private Khata & Smart Bill Reminders",
                    subtitle = "Lena/Dena settlements, equal splits & scheduled payment alerts"
                )
                HorizontalDivider(color = HairlinePlatinum, thickness = 0.5.dp)
                AuthFeatureRow(
                    icon = Icons.Default.Lock,
                    tint = ChampagneGold,
                    title = "Zero-Trust Cloud Vault & J.A.R.V.I.S. AI",
                    subtitle = "Strict per-user Firestore isolation & bilingual voice wealth concierge"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Recovery & Security Help
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { showRecoveryHelp = !showRecoveryHelp }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("account_recovery_help_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Account Recovery",
                    tint = ChampagneGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Trouble signing in? Account Recovery & Security Info",
                    style = MaterialTheme.typography.labelMedium,
                    color = ChampagneGold
                )
            }

            AnimatedVisibility(visible = showRecoveryHelp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElevatedGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ACCOUNT RECOVERY & SESSION SECURITY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold
                    )
                    Text(
                        text = "• Google Account Recovery: If you signed in with Google previously, tap 'Enter Sovereign Vault with Google' to restore your encrypted cloud ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory
                    )
                    Text(
                        text = "• Mobile OTP Recovery: Enter your registered 10-digit mobile number to receive a fresh 6-digit SMS verification code via Firebase Authentication.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory
                    )
                    Text(
                        text = "• Zero-Plaintext Policy: Kharchee never stores passwords or OTP codes on device or in cloud storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPlatinum
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Protected by Google Identity, Firebase Phone Auth & Zero-Trust Cloud Security",
                style = MaterialTheme.typography.labelSmall,
                color = MutedIvory,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ElevatedGraphite)
                .border(0.8.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WarmIvory,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedIvory,
                fontSize = 12.sp
            )
        }
    }
}
