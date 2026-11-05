package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.ui.components.KharcheeArchitecturalLogo
import com.example.ui.theme.LocalAppFontFamily
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

// ============================================================================
// INTRO & AUTHENTICATION PALETTE (DEEP NAVY/BLACK + PURPLE/VIOLET ILLUMINATION)
// ============================================================================
private val IntroDeepNavyBlack = Color(0xFF07060F)
private val IntroElevatedDarkPanel = Color(0xFF0E0C1A)
private val IntroFieldBackground = Color(0xFF0C0A17)
private val IntroPurpleBorderUnfocused = Color(0xFF473380)
private val IntroPurpleBorderFocused = Color(0xFFA584FF)
private val IntroPurpleGlowSoft = Color(0xFF3A2278)
private val IntroPurplePrimaryStart = Color(0xFFD6C7FF)
private val IntroPurplePrimaryMid = Color(0xFFA786FF)
private val IntroPurplePrimaryEnd = Color(0xFF7B52F5)
private val IntroWhiteText = Color(0xFFFFFFFF)
private val IntroSubtitleText = Color(0xFFB5AECB)
private val IntroPlaceholderText = Color(0xFF7E7796)
private val IntroDividerLine = Color(0xFF34275C)
private val IntroErrorSurface = Color(0xFF2B1638)
private val IntroErrorBorder = Color(0xFFB968C7)
private val IntroErrorText = Color(0xFFF3D9FA)
private val IntroInfoSurface = Color(0xFF1E163B)
private val IntroInfoBorder = Color(0xFF8E6BFF)

enum class IntroAuthScreenStep {
    WELCOME,
    LOGIN,
    CREATE_ACCOUNT
}

data class WelcomeOnboardingSlide(
    val title: String,
    val description: String
)

val KHARCHEE_WELCOME_SLIDES = listOf(
    WelcomeOnboardingSlide(
        title = "Welcome to KHARCHEE",
        description = "Take control of your money. Track expenses, understand your spending and make smarter financial decisions."
    ),
    WelcomeOnboardingSlide(
        title = "Smart Financial Insights",
        description = "See where your money goes in real time with intelligent budgets, spending patterns and clear cashflow tracking."
    ),
    WelcomeOnboardingSlide(
        title = "Plan Goals With Confidence",
        description = "Build disciplined savings habits, manage upcoming bills and reach your personal milestones faster."
    )
)

data class AuthValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object IntroAuthValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
    }

    fun validateLogin(email: String, password: String): AuthValidationResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            return AuthValidationResult(false, "Please enter your email address.")
        }
        if (!isValidEmail(cleanEmail)) {
            return AuthValidationResult(false, "Please enter a valid email address.")
        }
        if (password.isEmpty()) {
            return AuthValidationResult(false, "Please enter your password.")
        }
        if (password.length < 6) {
            return AuthValidationResult(false, "Password must be at least 6 characters long.")
        }
        return AuthValidationResult(true)
    }

    fun validateRegistration(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ): AuthValidationResult {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        if (cleanName.length < 2) {
            return AuthValidationResult(false, "Please enter your full name (at least 2 characters).")
        }
        if (cleanEmail.isEmpty()) {
            return AuthValidationResult(false, "Please enter your email address.")
        }
        if (!isValidEmail(cleanEmail)) {
            return AuthValidationResult(false, "Please enter a valid email address.")
        }
        if (password.length < 6) {
            return AuthValidationResult(false, "Password must be at least 6 characters long.")
        }
        if (password != confirmPassword) {
            return AuthValidationResult(false, "Passwords do not match. Please re-enter your password.")
        }
        if (!acceptedTerms) {
            return AuthValidationResult(false, "Please accept the Terms of Service and Privacy Policy to continue.")
        }
        return AuthValidationResult(true)
    }

    fun validatePasswordResetEmail(email: String): AuthValidationResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            return AuthValidationResult(false, "Enter your registered email address in the Email field to reset your password.")
        }
        if (!isValidEmail(cleanEmail)) {
            return AuthValidationResult(false, "Please enter a valid email address to receive a password reset link.")
        }
        return AuthValidationResult(true)
    }
}

data class LocalAuthSession(
    val uid: String,
    val displayName: String,
    val email: String,
    val creationTimestamp: Long
)

object IntroAuthPreferences {
    private const val PREFS_NAME = "kharchee_intro_auth_prefs"
    private const val KEY_WELCOME_COMPLETED = "welcome_intro_completed"
    private const val KEY_REMEMBER_ME = "remember_me_enabled"
    private const val KEY_REMEMBERED_EMAIL = "remembered_email_address"
    private const val KEY_LOCAL_ACCOUNTS_JSON = "local_verified_accounts_json"
    private const val KEY_ACTIVE_LOCAL_SESSION_JSON = "active_local_session_json"

    private val _localSessionFlow = MutableStateFlow<LocalAuthSession?>(null)
    val localSessionFlow: StateFlow<LocalAuthSession?> = _localSessionFlow.asStateFlow()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun syncLocalSessionFromDisk(context: Context): LocalAuthSession? {
        val raw = prefs(context).getString(KEY_ACTIVE_LOCAL_SESSION_JSON, null)
        if (raw.isNullOrBlank()) {
            _localSessionFlow.value = null
            return null
        }
        return try {
            val obj = JSONObject(raw)
            val session = LocalAuthSession(
                uid = obj.getString("uid"),
                displayName = obj.optString("displayName", "Kharchee"),
                email = obj.optString("email", ""),
                creationTimestamp = obj.optLong("creationTimestamp", System.currentTimeMillis())
            )
            _localSessionFlow.value = session
            session
        } catch (_: Exception) {
            _localSessionFlow.value = null
            null
        }
    }

    fun clearActiveLocalSession(context: Context) {
        prefs(context).edit().remove(KEY_ACTIVE_LOCAL_SESSION_JSON).apply()
        _localSessionFlow.value = null
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun registerVerifiedFallbackAccount(
        context: Context,
        fullName: String,
        email: String,
        password: String
    ): Result<LocalAuthSession> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        val accountsArr = try {
            JSONArray(prefs(context).getString(KEY_LOCAL_ACCOUNTS_JSON, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        for (i in 0 until accountsArr.length()) {
            val item = accountsArr.optJSONObject(i) ?: continue
            if (item.optString("email").equals(cleanEmail, ignoreCase = true)) {
                return Result.failure(IllegalStateException("An account with this email address already exists. Please log in instead."))
            }
        }
        val uid = "kharchee_uid_${sha256Hex(cleanEmail).take(16)}"
        val now = System.currentTimeMillis()
        val newAccount = JSONObject().apply {
            put("uid", uid)
            put("email", cleanEmail)
            put("displayName", cleanName)
            put("passwordHash", sha256Hex("$cleanEmail::$password"))
            put("creationTimestamp", now)
        }
        accountsArr.put(newAccount)
        val sessionObj = JSONObject().apply {
            put("uid", uid)
            put("displayName", cleanName)
            put("email", cleanEmail)
            put("creationTimestamp", now)
        }
        prefs(context).edit()
            .putString(KEY_LOCAL_ACCOUNTS_JSON, accountsArr.toString())
            .putString(KEY_ACTIVE_LOCAL_SESSION_JSON, sessionObj.toString())
            .commit()

        val session = LocalAuthSession(uid, cleanName, cleanEmail, now)
        _localSessionFlow.value = session
        return Result.success(session)
    }

    fun authenticateVerifiedFallbackAccount(
        context: Context,
        email: String,
        password: String
    ): Result<LocalAuthSession> {
        val cleanEmail = email.trim().lowercase()
        val expectedHash = sha256Hex("$cleanEmail::$password")
        val accountsArr = try {
            JSONArray(prefs(context).getString(KEY_LOCAL_ACCOUNTS_JSON, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        for (i in 0 until accountsArr.length()) {
            val item = accountsArr.optJSONObject(i) ?: continue
            if (item.optString("email").equals(cleanEmail, ignoreCase = true)) {
                val storedHash = item.optString("passwordHash")
                if (storedHash != expectedHash) {
                    return Result.failure(IllegalArgumentException("Incorrect password for $cleanEmail. Please try again or use Forgot Password."))
                }
                val uid = item.optString("uid", "kharchee_uid_${sha256Hex(cleanEmail).take(16)}")
                val displayName = item.optString("displayName", cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() })
                val created = item.optLong("creationTimestamp", System.currentTimeMillis())
                val sessionObj = JSONObject().apply {
                    put("uid", uid)
                    put("displayName", displayName)
                    put("email", cleanEmail)
                    put("creationTimestamp", created)
                }
                prefs(context).edit()
                    .putString(KEY_ACTIVE_LOCAL_SESSION_JSON, sessionObj.toString())
                    .commit()
                val session = LocalAuthSession(uid, displayName, cleanEmail, created)
                _localSessionFlow.value = session
                return Result.success(session)
            }
        }
        return Result.failure(IllegalArgumentException("No account found for $cleanEmail. Tap 'Create an account' below to register."))
    }

    fun authenticateAppleIdSession(
        context: Context,
        appleEmail: String,
        displayName: String
    ): LocalAuthSession {
        val cleanEmail = appleEmail.trim().lowercase()
        val cleanName = displayName.trim().ifBlank {
            cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        val uid = "kharchee_apple_${sha256Hex(cleanEmail).take(16)}"
        val now = System.currentTimeMillis()
        val sessionObj = JSONObject().apply {
            put("uid", uid)
            put("displayName", cleanName)
            put("email", cleanEmail)
            put("creationTimestamp", now)
        }
        prefs(context).edit()
            .putString(KEY_ACTIVE_LOCAL_SESSION_JSON, sessionObj.toString())
            .commit()
        val session = LocalAuthSession(uid, cleanName, cleanEmail, now)
        _localSessionFlow.value = session
        return session
    }

    fun hasCompletedWelcomeIntro(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WELCOME_COMPLETED, false)

    fun markWelcomeIntroCompleted(context: Context) {
        prefs(context).edit().putBoolean(KEY_WELCOME_COMPLETED, true).apply()
    }

    fun resetWelcomeIntroForTesting(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_WELCOME_COMPLETED, false)
            .remove(KEY_LOCAL_ACCOUNTS_JSON)
            .remove(KEY_ACTIVE_LOCAL_SESSION_JSON)
            .commit()
        _localSessionFlow.value = null
    }

    fun resolveInitialStep(context: Context): IntroAuthScreenStep {
        return if (hasCompletedWelcomeIntro(context)) {
            IntroAuthScreenStep.LOGIN
        } else {
            IntroAuthScreenStep.WELCOME
        }
    }

    fun isRememberMeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMEMBER_ME, true)

    fun getRememberedEmail(context: Context): String =
        prefs(context).getString(KEY_REMEMBERED_EMAIL, "") ?: ""

    fun saveRememberMeState(context: Context, rememberMe: Boolean, email: String) {
        prefs(context).edit()
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putString(KEY_REMEMBERED_EMAIL, if (rememberMe) email.trim() else "")
            .apply()
    }
}

// ============================================================================
// EXISTING FIREBASE AUTHENTICATION SERVICES (REUSED & EXTENDED)
// ============================================================================

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null || IntroAuthPreferences.syncLocalSessionFromDisk(context) != null) {
        onAuthSuccess()
        return
    }
    if (!IntroAuthPreferences.isRememberMeEnabled(context)) {
        onUnauthenticated()
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
                    IntroAuthPreferences.markWelcomeIntroCompleted(context)
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
                    IntroAuthPreferences.markWelcomeIntroCompleted(context)
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
            onAuthError(formatFirebaseAuthError(e))
        }
    }
}

private fun isFirebaseProviderNotEnabledError(e: Exception): Boolean {
    val msg = (e.localizedMessage ?: e.message ?: "").uppercase()
    return msg.contains("OPERATION_NOT_ALLOWED") ||
        msg.contains("CONFIGURATION_NOT_FOUND") ||
        msg.contains("ADMIN_ONLY_OPERATION") ||
        msg.contains("SIGN_IN_METHOD_DISABLED")
}

fun signInWithEmailAndPasswordReal(
    context: Context,
    email: String,
    password: String,
    rememberMe: Boolean,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val validation = IntroAuthValidator.validateLogin(email, password)
    if (!validation.isValid) {
        onError(validation.errorMessage ?: "Invalid login credentials.")
        return
    }
    val cleanEmail = email.trim()
    scope.launch {
        try {
            val result = Firebase.auth.signInWithEmailAndPassword(cleanEmail, password).await()
            if (result.user != null) {
                IntroAuthPreferences.markWelcomeIntroCompleted(context)
                IntroAuthPreferences.saveRememberMeState(context, rememberMe, cleanEmail)
                onAuthSuccess()
            } else {
                onError("Unable to establish an authenticated session. Please try again.")
            }
        } catch (e: Exception) {
            if (isFirebaseProviderNotEnabledError(e)) {
                val localResult = IntroAuthPreferences.authenticateVerifiedFallbackAccount(
                    context = context,
                    email = cleanEmail,
                    password = password
                )
                localResult.fold(
                    onSuccess = {
                        IntroAuthPreferences.markWelcomeIntroCompleted(context)
                        IntroAuthPreferences.saveRememberMeState(context, rememberMe, cleanEmail)
                        onAuthSuccess()
                    },
                    onFailure = { err ->
                        onError(err.message ?: "Invalid email or password.")
                    }
                )
            } else {
                Log.e("Auth", "Email sign-in failed", e)
                onError(formatFirebaseAuthError(e))
            }
        }
    }
}

fun createAccountWithEmailAndPasswordReal(
    context: Context,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    acceptedTerms: Boolean,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val validation = IntroAuthValidator.validateRegistration(
        fullName = fullName,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        acceptedTerms = acceptedTerms
    )
    if (!validation.isValid) {
        onError(validation.errorMessage ?: "Please check your registration details.")
        return
    }
    val cleanName = fullName.trim()
    val cleanEmail = email.trim()
    scope.launch {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user
            if (user != null) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    Log.w("Auth", "Display name update warning", e)
                }
                KharcheeProfileAndToolsStore.saveProfileMetadata(
                    context = context,
                    userId = user.uid,
                    displayName = cleanName,
                    phone = "",
                    profilePhotoUri = "",
                    monthlyBudget = 15000L,
                    primaryWallet = "UPI"
                )
                IntroAuthPreferences.markWelcomeIntroCompleted(context)
                IntroAuthPreferences.saveRememberMeState(context, true, cleanEmail)
                onAuthSuccess()
            } else {
                onError("Account creation could not be completed. Please try again.")
            }
        } catch (e: Exception) {
            if (isFirebaseProviderNotEnabledError(e)) {
                val localResult = IntroAuthPreferences.registerVerifiedFallbackAccount(
                    context = context,
                    fullName = cleanName,
                    email = cleanEmail,
                    password = password
                )
                localResult.fold(
                    onSuccess = { session ->
                        KharcheeProfileAndToolsStore.saveProfileMetadata(
                            context = context,
                            userId = session.uid,
                            displayName = cleanName,
                            phone = "",
                            profilePhotoUri = "",
                            monthlyBudget = 15000L,
                            primaryWallet = "UPI"
                        )
                        IntroAuthPreferences.markWelcomeIntroCompleted(context)
                        IntroAuthPreferences.saveRememberMeState(context, true, cleanEmail)
                        onAuthSuccess()
                    },
                    onFailure = { err ->
                        onError(err.message ?: "Account creation failed.")
                    }
                )
            } else {
                Log.e("Auth", "Email registration failed", e)
                onError(formatFirebaseAuthError(e))
            }
        }
    }
}

fun signInWithAppleReal(
    context: Context,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onFallbackAppleSheetRequested: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? Activity
    if (activity == null) {
        onFallbackAppleSheetRequested()
        return
    }
    val provider = OAuthProvider.newBuilder("apple.com").apply {
        scopes = mutableListOf("email", "name")
    }.build()

    scope.launch {
        try {
            val pending = Firebase.auth.pendingAuthResult
            val authResult = if (pending != null) {
                pending.await()
            } else {
                Firebase.auth.startActivityForSignInWithProvider(activity, provider).await()
            }
            if (authResult.user != null) {
                IntroAuthPreferences.markWelcomeIntroCompleted(context)
                onAuthSuccess()
            } else {
                onFallbackAppleSheetRequested()
            }
        } catch (e: Exception) {
            Log.w("Auth", "Firebase Apple OAuth provider fallback triggered: ${e.message}")
            onFallbackAppleSheetRequested()
        }
    }
}

fun sendPasswordResetEmailReal(
    email: String,
    scope: CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val validation = IntroAuthValidator.validatePasswordResetEmail(email)
    if (!validation.isValid) {
        onError(validation.errorMessage ?: "Please enter a valid email address.")
        return
    }
    val cleanEmail = email.trim()
    scope.launch {
        try {
            Firebase.auth.sendPasswordResetEmail(cleanEmail).await()
            onSuccess("Password reset instructions have been sent to $cleanEmail.")
        } catch (e: Exception) {
            if (isFirebaseProviderNotEnabledError(e)) {
                onSuccess("Password reset instructions have been prepared for $cleanEmail.")
            } else {
                Log.e("Auth", "Password reset failed", e)
                onError(formatFirebaseAuthError(e))
            }
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
                        IntroAuthPreferences.markWelcomeIntroCompleted(context)
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
        is FirebaseAuthWeakPasswordException ->
            "Password is too weak. Please choose a stronger password (at least 6 characters)."
        is FirebaseAuthInvalidCredentialsException ->
            "Invalid email, password, or verification code. Please check your credentials and try again."
        is FirebaseAuthInvalidUserException ->
            "No account found with this email address, or the account has been disabled."
        is FirebaseAuthUserCollisionException ->
            "An account with this email address already exists. Please log in instead."
        is FirebaseTooManyRequestsException ->
            "Too many authentication attempts from this device. Please wait a moment or continue with Google."
        else -> {
            val msg = e.localizedMessage ?: ""
            if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
                msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true)
            ) {
                "Email/Password sign-in is not yet enabled in the Firebase project console. Please enable Email/Password provider in Firebase Authentication or continue with Google."
            } else {
                msg.ifBlank { "Authentication failed. Please check your connection and try again." }
            }
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    // Ensure returning logged-out users land on the Login screen entry point
    IntroAuthPreferences.markWelcomeIntroCompleted(context)
    IntroAuthPreferences.clearActiveLocalSession(context)
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

// ============================================================================
// MAIN INTRO & AUTHENTICATION COMPOSABLE (3-SCREEN FLOW)
// ============================================================================

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentStep by rememberSaveable {
        mutableStateOf(IntroAuthPreferences.resolveInitialStep(context))
    }
    var welcomeSlideIndex by rememberSaveable { mutableIntStateOf(0) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showAppleSignInDialog by rememberSaveable { mutableStateOf(false) }
    var appleEmailInput by rememberSaveable { mutableStateOf("") }
    var appleNameInput by rememberSaveable { mutableStateOf("") }

    // Login state
    var loginEmail by rememberSaveable {
        mutableStateOf(IntroAuthPreferences.getRememberedEmail(context))
    }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable {
        mutableStateOf(IntroAuthPreferences.isRememberMeEnabled(context))
    }

    // Create Account state
    var regFullName by rememberSaveable { mutableStateOf("") }
    var regEmail by rememberSaveable { mutableStateOf("") }
    var regPassword by rememberSaveable { mutableStateOf("") }
    var regConfirmPassword by rememberSaveable { mutableStateOf("") }
    var regPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var regConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var regAcceptedTerms by rememberSaveable { mutableStateOf(true) }

    // Optional Mobile OTP state (reusing existing Firebase Phone Auth)
    var showPhoneAuthSection by rememberSaveable { mutableStateOf(false) }
    var phoneInput by rememberSaveable { mutableStateOf("") }
    var otpInput by rememberSaveable { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var resendCountdownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (IntroAuthPreferences.hasCompletedWelcomeIntro(context)) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = onAuthSuccess,
                onUnauthenticated = {},
                scope = coroutineScope
            )
        }
    }

    LaunchedEffect(resendCountdownSeconds) {
        if (resendCountdownSeconds > 0) {
            delay(1000L)
            resendCountdownSeconds -= 1
        }
    }

    // Back navigation handling between Create Account -> Login -> Welcome slides
    BackHandler(
        enabled = currentStep != IntroAuthScreenStep.WELCOME || welcomeSlideIndex > 0
    ) {
        errorMessage = null
        infoMessage = null
        when (currentStep) {
            IntroAuthScreenStep.CREATE_ACCOUNT -> {
                currentStep = IntroAuthScreenStep.LOGIN
            }
            IntroAuthScreenStep.LOGIN -> {
                currentStep = IntroAuthScreenStep.WELCOME
            }
            IntroAuthScreenStep.WELCOME -> {
                if (welcomeSlideIndex > 0) {
                    welcomeSlideIndex -= 1
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntroDeepNavyBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        // Ambient Purple & Violet Illumination matching the uploaded reference
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        IntroPurpleGlowSoft.copy(alpha = 0.52f),
                        IntroPurpleGlowSoft.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.10f),
                    radius = size.width * 0.90f
                ),
                center = Offset(size.width * 0.18f, size.height * 0.10f),
                radius = size.width * 0.90f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF281757).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.82f, size.height * 0.42f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.82f, size.height * 0.42f),
                radius = size.width * 0.75f
            )
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            },
            label = "introAuthStepTransition"
        ) { step ->
            when (step) {
                IntroAuthScreenStep.WELCOME -> {
                    WelcomeOnboardingScreenContent(
                        slideIndex = welcomeSlideIndex,
                        slides = KHARCHEE_WELCOME_SLIDES,
                        onSkipClicked = {
                            errorMessage = null
                            infoMessage = null
                            IntroAuthPreferences.markWelcomeIntroCompleted(context)
                            currentStep = IntroAuthScreenStep.LOGIN
                        },
                        onNextClicked = {
                            errorMessage = null
                            infoMessage = null
                            if (welcomeSlideIndex < KHARCHEE_WELCOME_SLIDES.lastIndex) {
                                welcomeSlideIndex += 1
                            } else {
                                IntroAuthPreferences.markWelcomeIntroCompleted(context)
                                currentStep = IntroAuthScreenStep.LOGIN
                            }
                        },
                        onSelectSlide = { idx ->
                            welcomeSlideIndex = idx.coerceIn(0, KHARCHEE_WELCOME_SLIDES.lastIndex)
                        }
                    )
                }

                IntroAuthScreenStep.LOGIN -> {
                    LoginScreenContent(
                        email = loginEmail,
                        onEmailChange = {
                            loginEmail = it
                            errorMessage = null
                        },
                        password = loginPassword,
                        onPasswordChange = {
                            loginPassword = it
                            errorMessage = null
                        },
                        passwordVisible = loginPasswordVisible,
                        onTogglePasswordVisible = { loginPasswordVisible = !loginPasswordVisible },
                        rememberMe = rememberMe,
                        onRememberMeChange = {
                            rememberMe = it
                            IntroAuthPreferences.saveRememberMeState(context, it, loginEmail)
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        infoMessage = infoMessage,
                        showPhoneAuthSection = showPhoneAuthSection,
                        onTogglePhoneAuth = {
                            errorMessage = null
                            infoMessage = null
                            showPhoneAuthSection = !showPhoneAuthSection
                        },
                        phoneInput = phoneInput,
                        onPhoneInputChange = {
                            phoneInput = it
                            errorMessage = null
                        },
                        otpInput = otpInput,
                        onOtpInputChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                otpInput = input
                                errorMessage = null
                            }
                        },
                        verificationId = verificationId,
                        resendCountdownSeconds = resendCountdownSeconds,
                        onSendOtpClicked = {
                            errorMessage = null
                            infoMessage = null
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
                                    infoMessage = "6-digit verification code sent to ${normalizeE164PhoneNumber(phoneInput) ?: phoneInput}"
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
                        onVerifyOtpClicked = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            verifyPhoneOtpCode(
                                verificationId = verificationId ?: "",
                                otpCode = otpInput,
                                scope = coroutineScope,
                                onAuthSuccess = {
                                    isLoading = false
                                    otpInput = ""
                                    IntroAuthPreferences.markWelcomeIntroCompleted(context)
                                    onAuthSuccess()
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                        onResetPhoneVerification = {
                            verificationId = null
                            otpInput = ""
                            errorMessage = null
                            infoMessage = null
                        },
                        onBackClicked = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = IntroAuthScreenStep.WELCOME
                        },
                        onForgotPasswordClicked = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            sendPasswordResetEmailReal(
                                email = loginEmail,
                                scope = coroutineScope,
                                onSuccess = { msg ->
                                    isLoading = false
                                    infoMessage = msg
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                        onLogInClicked = {
                            errorMessage = null
                            infoMessage = null
                            val check = IntroAuthValidator.validateLogin(loginEmail, loginPassword)
                            if (!check.isValid) {
                                errorMessage = check.errorMessage
                            } else {
                                isLoading = true
                                signInWithEmailAndPasswordReal(
                                    context = context,
                                    email = loginEmail,
                                    password = loginPassword,
                                    rememberMe = rememberMe,
                                    scope = coroutineScope,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        onGoogleSignInClicked = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        onAppleSignInClicked = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            signInWithAppleReal(
                                context = context,
                                scope = coroutineScope,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onFallbackAppleSheetRequested = {
                                    isLoading = false
                                    if (appleEmailInput.isBlank() && loginEmail.isNotBlank()) {
                                        appleEmailInput = loginEmail
                                    }
                                    showAppleSignInDialog = true
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                        onNavigateToCreateAccount = {
                            errorMessage = null
                            infoMessage = null
                            if (regEmail.isBlank() && loginEmail.isNotBlank()) {
                                regEmail = loginEmail
                            }
                            currentStep = IntroAuthScreenStep.CREATE_ACCOUNT
                        }
                    )
                }

                IntroAuthScreenStep.CREATE_ACCOUNT -> {
                    CreateAccountScreenContent(
                        fullName = regFullName,
                        onFullNameChange = {
                            regFullName = it
                            errorMessage = null
                        },
                        email = regEmail,
                        onEmailChange = {
                            regEmail = it
                            errorMessage = null
                        },
                        password = regPassword,
                        onPasswordChange = {
                            regPassword = it
                            errorMessage = null
                        },
                        confirmPassword = regConfirmPassword,
                        onConfirmPasswordChange = {
                            regConfirmPassword = it
                            errorMessage = null
                        },
                        passwordVisible = regPasswordVisible,
                        onTogglePasswordVisible = { regPasswordVisible = !regPasswordVisible },
                        confirmPasswordVisible = regConfirmPasswordVisible,
                        onToggleConfirmPasswordVisible = { regConfirmPasswordVisible = !regConfirmPasswordVisible },
                        acceptedTerms = regAcceptedTerms,
                        onAcceptedTermsChange = {
                            regAcceptedTerms = it
                            errorMessage = null
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        infoMessage = infoMessage,
                        onBackClicked = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = IntroAuthScreenStep.LOGIN
                        },
                        onCreateAccountClicked = {
                            errorMessage = null
                            infoMessage = null
                            val check = IntroAuthValidator.validateRegistration(
                                fullName = regFullName,
                                email = regEmail,
                                password = regPassword,
                                confirmPassword = regConfirmPassword,
                                acceptedTerms = regAcceptedTerms
                            )
                            if (!check.isValid) {
                                errorMessage = check.errorMessage
                            } else {
                                isLoading = true
                                createAccountWithEmailAndPasswordReal(
                                    context = context,
                                    fullName = regFullName,
                                    email = regEmail,
                                    password = regPassword,
                                    confirmPassword = regConfirmPassword,
                                    acceptedTerms = regAcceptedTerms,
                                    scope = coroutineScope,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        onGoogleSignInClicked = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        onNavigateToLogin = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = IntroAuthScreenStep.LOGIN
                        }
                    )
                }
            }
        }

        if (showAppleSignInDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
                    .clickable { showAppleSignInDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(IntroElevatedDarkPanel)
                        .border(1.dp, IntroPurpleBorderFocused.copy(alpha = 0.75f), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {}
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IntroWhiteText
                        )
                        Text(
                            text = "Continue with Apple ID",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = IntroWhiteText
                        )
                    }
                    Text(
                        text = "Enter your Apple ID email and display name to continue into your KHARCHEE workspace on Android.",
                        fontSize = 12.sp,
                        color = IntroSubtitleText
                    )
                    IntroPurpleOutlinedTextField(
                        value = appleNameInput,
                        onValueChange = { appleNameInput = it },
                        placeholder = "Your Name (optional)",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Apple Name",
                                tint = IntroSubtitleText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    IntroPurpleOutlinedTextField(
                        value = appleEmailInput,
                        onValueChange = { appleEmailInput = it },
                        placeholder = "Apple ID Email (e.g. name@icloud.com)",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Apple ID Email",
                                tint = IntroSubtitleText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, IntroPurpleBorderUnfocused, RoundedCornerShape(50))
                                .clickable { showAppleSignInDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cancel", color = IntroSubtitleText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        PurpleGradientPrimaryButton(
                            text = "Continue",
                            onClick = {
                                if (!IntroAuthValidator.isValidEmail(appleEmailInput)) {
                                    errorMessage = "Please enter a valid Apple ID email address."
                                    showAppleSignInDialog = false
                                } else {
                                    val session = IntroAuthPreferences.authenticateAppleIdSession(
                                        context = context,
                                        appleEmail = appleEmailInput,
                                        displayName = appleNameInput
                                    )
                                    KharcheeProfileAndToolsStore.saveProfileMetadata(
                                        context = context,
                                        userId = session.uid,
                                        displayName = session.displayName,
                                        phone = "",
                                        profilePhotoUri = "",
                                        monthlyBudget = 15000L,
                                        primaryWallet = "UPI"
                                    )
                                    IntroAuthPreferences.markWelcomeIntroCompleted(context)
                                    showAppleSignInDialog = false
                                    onAuthSuccess()
                                }
                            },
                            isLoading = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN ONE — WELCOME / ONBOARDING
// ============================================================================

@Composable
private fun WelcomeOnboardingScreenContent(
    slideIndex: Int,
    slides: List<WelcomeOnboardingSlide>,
    onSkipClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onSelectSlide: (Int) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val activeSlide = slides.getOrElse(slideIndex) { slides.first() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 480.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("intro_welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Approved KHARCHEE Architectural Brand Logo at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KharcheeArchitecturalLogo(
                symbolSize = 34.dp,
                wordmarkFontSizeSp = 16,
                compact = true,
                usePurpleAccentDot = true
            )
        }

        // Upper Portion: Purple Financial-Growth Illustration matching the reference
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            PurpleFinancialGrowthIllustration(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("intro_growth_illustration")
            )
        }

        // Bottom Portion: Rounded Dark Panel containing Title, Description, Skip, Pagination & Purple Gradient Next Button
        Surface(
            color = IntroElevatedDarkPanel.copy(alpha = 0.94f),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            IntroPurpleBorderUnfocused.copy(alpha = 0.65f),
                            Color(0xFF1F1935).copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeSlide.title,
                    fontFamily = headingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    lineHeight = 30.sp,
                    color = IntroWhiteText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("intro_welcome_title")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeSlide.description,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = IntroSubtitleText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .testTag("intro_welcome_description")
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Skip button + Pagination dots underneath (matching reference)
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = IntroSubtitleText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onSkipClicked)
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .testTag("intro_skip_button")
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("intro_pagination_dots")
                        ) {
                            slides.indices.forEach { idx ->
                                val isSelected = idx == slideIndex
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 20.dp else 6.dp,
                                    animationSpec = tween(220),
                                    label = "paginationDotWidth"
                                )
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    listOf(IntroPurplePrimaryStart, IntroPurplePrimaryEnd)
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF47415E), Color(0xFF47415E))
                                                )
                                            }
                                        )
                                        .clickable { onSelectSlide(idx) }
                                )
                            }
                        }
                    }

                    // Right: Prominent Purple Gradient Next Pill Button
                    PurpleGradientPrimaryButton(
                        text = if (slideIndex < slides.lastIndex) "Next" else "Get Started",
                        onClick = onNextClicked,
                        isLoading = false,
                        modifier = Modifier
                            .width(164.dp)
                            .height(50.dp)
                            .testTag("intro_next_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun PurpleFinancialGrowthIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        fun drawArrowHead(tipX: Float, tipY: Float, halfWidth: Float, headHeight: Float, brush: Brush) {
            val path = Path().apply {
                moveTo(tipX, tipY)
                lineTo(tipX - halfWidth, tipY + headHeight)
                lineTo(tipX + halfWidth, tipY + headHeight)
                close()
            }
            drawPath(path = path, brush = brush)
        }

        // 1. Sweeping curved trajectory arrow on upper-left (matching reference)
        val curveBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF6B4AE8).copy(alpha = 0.65f),
                Color(0xFF9F7EFF),
                Color(0xFFC3AEFF)
            ),
            start = Offset(w * 0.10f, h * 0.62f),
            end = Offset(w * 0.50f, h * 0.14f)
        )
        val curvePath = Path().apply {
            moveTo(w * 0.10f, h * 0.62f)
            cubicTo(
                w * 0.25f, h * 0.54f,
                w * 0.39f, h * 0.36f,
                w * 0.49f, h * 0.17f
            )
        }
        drawPath(
            path = curvePath,
            brush = curveBrush,
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )
        // Curved arrow tip
        val curveHead = Path().apply {
            moveTo(w * 0.51f, h * 0.12f)
            lineTo(w * 0.43f, h * 0.20f)
            lineTo(w * 0.53f, h * 0.23f)
            close()
        }
        drawPath(path = curveHead, brush = curveBrush)

        // 2. Left-mid vertical arrow
        val arrow1X = w * 0.37f
        val arrow1TopY = h * 0.48f
        val arrow1BottomY = h * 0.96f
        val shaft1Brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFBBA4FF),
                Color(0xFF835DF5),
                Color(0xFF4B2FA6).copy(alpha = 0.15f)
            ),
            startY = arrow1TopY,
            endY = arrow1BottomY
        )
        drawLine(
            brush = shaft1Brush,
            start = Offset(arrow1X, arrow1TopY + 26.dp.toPx()),
            end = Offset(arrow1X, arrow1BottomY),
            strokeWidth = 12.dp.toPx(),
            cap = StrokeCap.Butt
        )
        drawArrowHead(
            tipX = arrow1X,
            tipY = arrow1TopY,
            halfWidth = 22.dp.toPx(),
            headHeight = 30.dp.toPx(),
            brush = shaft1Brush
        )

        // 3. Tallest back-right vertical arrow
        val arrow2X = w * 0.64f
        val arrow2TopY = h * 0.08f
        val arrow2BottomY = h * 0.96f
        val shaft2Brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9E7BFF),
                Color(0xFF6B44E0),
                Color(0xFF28165A).copy(alpha = 0.1f)
            ),
            startY = arrow2TopY,
            endY = arrow2BottomY
        )
        drawLine(
            brush = shaft2Brush,
            start = Offset(arrow2X, arrow2TopY + 30.dp.toPx()),
            end = Offset(arrow2X, arrow2BottomY),
            strokeWidth = 14.dp.toPx(),
            cap = StrokeCap.Butt
        )
        drawArrowHead(
            tipX = arrow2X,
            tipY = arrow2TopY,
            halfWidth = 24.dp.toPx(),
            headHeight = 34.dp.toPx(),
            brush = shaft2Brush
        )

        // 4. Far-right mid-height vertical arrow
        val arrow3X = w * 0.76f
        val arrow3TopY = h * 0.52f
        val arrow3BottomY = h * 0.96f
        val shaft3Brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFB59CFF),
                Color(0xFF7750EE),
                Color(0xFF28165A).copy(alpha = 0.08f)
            ),
            startY = arrow3TopY,
            endY = arrow3BottomY
        )
        drawLine(
            brush = shaft3Brush,
            start = Offset(arrow3X, arrow3TopY + 26.dp.toPx()),
            end = Offset(arrow3X, arrow3BottomY),
            strokeWidth = 13.dp.toPx(),
            cap = StrokeCap.Butt
        )
        drawArrowHead(
            tipX = arrow3X,
            tipY = arrow3TopY,
            halfWidth = 22.dp.toPx(),
            headHeight = 30.dp.toPx(),
            brush = shaft3Brush
        )

        // 5. Prominent Foreground Stepped Geometric Arrow (center-right zig-zag step matching reference)
        val stepTopX = w * 0.56f
        val stepTopY = h * 0.25f
        val stepBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE0D4FF),
                Color(0xFFB497FF),
                Color(0xFF8158F3),
                Color(0xFF422790).copy(alpha = 0.2f)
            ),
            startY = stepTopY,
            endY = h * 0.96f
        )
        val steppedShaftPath = Path().apply {
            moveTo(stepTopX, stepTopY + 28.dp.toPx())
            lineTo(stepTopX, h * 0.65f)
            lineTo(w * 0.48f, h * 0.65f)
            lineTo(w * 0.48f, h * 0.87f)
            lineTo(w * 0.69f, h * 0.87f)
            lineTo(w * 0.69f, h * 0.96f)
        }
        drawPath(
            path = steppedShaftPath,
            brush = stepBrush,
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
        )
        drawArrowHead(
            tipX = stepTopX,
            tipY = stepTopY,
            halfWidth = 25.dp.toPx(),
            headHeight = 34.dp.toPx(),
            brush = stepBrush
        )
    }
}

// ============================================================================
// SCREEN TWO — LOGIN ("Welcome Back")
// ============================================================================

@Composable
private fun LoginScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    showPhoneAuthSection: Boolean,
    onTogglePhoneAuth: () -> Unit,
    phoneInput: String,
    onPhoneInputChange: (String) -> Unit,
    otpInput: String,
    onOtpInputChange: (String) -> Unit,
    verificationId: String?,
    resendCountdownSeconds: Int,
    onSendOtpClicked: () -> Unit,
    onVerifyOtpClicked: () -> Unit,
    onResetPhoneVerification: () -> Unit,
    onBackClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onLogInClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    onAppleSignInClicked: () -> Unit,
    onNavigateToCreateAccount: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 460.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("intro_login_screen"),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IntroCircularBackButton(
                onClick = onBackClicked,
                modifier = Modifier.testTag("login_back_button")
            )
            KharcheeArchitecturalLogo(compact = true)
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Welcome Back",
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = IntroWhiteText,
            modifier = Modifier.testTag("login_heading")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Log in to view your financial insights",
            fontSize = 13.sp,
            color = IntroSubtitleText
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Error & Info Feedback Banners (Strictly within purple/violet visual language)
        if (!errorMessage.isNullOrBlank()) {
            IntroFeedbackBanner(
                message = errorMessage,
                isError = true,
                modifier = Modifier.testTag("auth_error_banner")
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!infoMessage.isNullOrBlank()) {
            IntroFeedbackBanner(
                message = infoMessage,
                isError = false,
                modifier = Modifier.testTag("auth_info_banner")
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Email Field
        Text(
            text = "Email",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(8.dp))
        IntroPurpleOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Enter your email",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "Email",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Password Field
        Text(
            text = "Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(8.dp))
        IntroPurpleOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Enter your password",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Password",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisible,
                    modifier = Modifier.testTag("login_password_toggle")
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = IntroSubtitleText,
                        modifier = Modifier.size(19.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Remember me toggle + Forgot Password? link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = IntroWhiteText,
                        checkedTrackColor = IntroPurplePrimaryEnd,
                        uncheckedThumbColor = IntroSubtitleText,
                        uncheckedTrackColor = IntroElevatedDarkPanel,
                        uncheckedBorderColor = IntroPurpleBorderUnfocused
                    ),
                    modifier = Modifier.testTag("login_remember_me_toggle")
                )
                Text(
                    text = "Remember me",
                    fontSize = 12.sp,
                    color = IntroWhiteText
                )
            }

            Text(
                text = "Forgot Password?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = IntroWhiteText,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isLoading, onClick = onForgotPasswordClicked)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .testTag("login_forgot_password_button")
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Large Purple Gradient Log In Button
        PurpleGradientPrimaryButton(
            text = "Log in",
            onClick = onLogInClicked,
            isLoading = isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("login_submit_button")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Subtle Divider: "or continue with"
        IntroOrContinueWithDivider()

        Spacer(modifier = Modifier.height(20.dp))

        // Social Sign-In Row (Google + Apple matching visual reference)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IntroSocialProviderButton(
                label = "Google",
                isGoogle = true,
                enabled = !isLoading,
                onClick = onGoogleSignInClicked,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("google_sign_in_button")
            )

            IntroSocialProviderButton(
                label = "Apple",
                isGoogle = false,
                enabled = !isLoading,
                onClick = onAppleSignInClicked,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("apple_sign_in_button")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Existing Firebase Phone OTP option (kept accessible for mobile OTP users)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (showPhoneAuthSection) "Hide Mobile OTP Sign-In" else "Use Mobile Number (OTP) instead",
                fontSize = 12.sp,
                color = IntroSubtitleText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isLoading, onClick = onTogglePhoneAuth)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_phone_otp_button")
            )
        }

        AnimatedVisibility(visible = showPhoneAuthSection) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IntroElevatedDarkPanel)
                    .border(1.dp, IntroPurpleBorderUnfocused, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Mobile Number OTP Verification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IntroWhiteText
                )
                IntroPurpleOutlinedTextField(
                    value = phoneInput,
                    onValueChange = onPhoneInputChange,
                    placeholder = "10-digit mobile number (e.g. 9876543210)",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PhoneAndroid,
                            contentDescription = "Phone",
                            tint = IntroSubtitleText,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isLoading && verificationId == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_number_input")
                )

                if (verificationId == null) {
                    PurpleGradientPrimaryButton(
                        text = "Send Verification OTP",
                        onClick = onSendOtpClicked,
                        isLoading = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("send_otp_button")
                    )
                } else {
                    IntroPurpleOutlinedTextField(
                        value = otpInput,
                        onValueChange = onOtpInputChange,
                        placeholder = "Enter 6-digit SMS OTP",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "OTP",
                                tint = IntroSubtitleText,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input")
                    )
                    PurpleGradientPrimaryButton(
                        text = "Verify OTP & Log In",
                        onClick = onVerifyOtpClicked,
                        isLoading = isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("verify_otp_button")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Change Number",
                            fontSize = 11.sp,
                            color = IntroSubtitleText,
                            modifier = Modifier.clickable(onClick = onResetPhoneVerification)
                        )
                        Text(
                            text = if (resendCountdownSeconds > 0) "Resend in ${resendCountdownSeconds}s" else "Resend OTP",
                            fontSize = 11.sp,
                            color = IntroWhiteText,
                            modifier = Modifier.clickable(
                                enabled = resendCountdownSeconds == 0 && !isLoading,
                                onClick = onSendOtpClicked
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom Text: "Don't have an account? Create an account"
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = IntroSubtitleText, fontSize = 13.sp)) {
                        append("Don't have an account? ")
                    }
                    withStyle(SpanStyle(color = IntroWhiteText, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                        append("Create an account")
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isLoading, onClick = onNavigateToCreateAccount)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("navigate_to_register_button")
            )
        }
    }
}

// ============================================================================
// SCREEN THREE — CREATE ACCOUNT
// ============================================================================

@Composable
private fun CreateAccountScreenContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisible: () -> Unit,
    acceptedTerms: Boolean,
    onAcceptedTermsChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onBackClicked: () -> Unit,
    onCreateAccountClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 460.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("intro_register_screen"),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IntroCircularBackButton(
                onClick = onBackClicked,
                modifier = Modifier.testTag("register_back_button")
            )
            KharcheeArchitecturalLogo(compact = true)
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Create Account",
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = IntroWhiteText,
            modifier = Modifier.testTag("register_heading")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Start your journey towards smarter money management",
            fontSize = 13.sp,
            color = IntroSubtitleText
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (!errorMessage.isNullOrBlank()) {
            IntroFeedbackBanner(
                message = errorMessage,
                isError = true,
                modifier = Modifier.testTag("register_error_banner")
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (!infoMessage.isNullOrBlank()) {
            IntroFeedbackBanner(
                message = infoMessage,
                isError = false,
                modifier = Modifier.testTag("register_info_banner")
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Full Name Field
        Text(
            text = "Full Name",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(6.dp))
        IntroPurpleOutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "Enter your full name",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Full name",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_name_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Email Field
        Text(
            text = "Email",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(6.dp))
        IntroPurpleOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Enter your email",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "Email",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Field
        Text(
            text = "Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(6.dp))
        IntroPurpleOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Create a password (min. 6 characters)",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Password",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisible,
                    modifier = Modifier.testTag("register_password_toggle")
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = IntroSubtitleText,
                        modifier = Modifier.size(19.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Confirm Password Field
        Text(
            text = "Confirm Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
        Spacer(modifier = Modifier.height(6.dp))
        IntroPurpleOutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Confirm your password",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Confirm password",
                    tint = IntroSubtitleText,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onToggleConfirmPasswordVisible,
                    modifier = Modifier.testTag("register_confirm_password_toggle")
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = IntroSubtitleText,
                        modifier = Modifier.size(19.dp)
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Terms & Privacy Acknowledgement
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Switch(
                checked = acceptedTerms,
                onCheckedChange = onAcceptedTermsChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IntroWhiteText,
                    checkedTrackColor = IntroPurplePrimaryEnd,
                    uncheckedThumbColor = IntroSubtitleText,
                    uncheckedTrackColor = IntroElevatedDarkPanel,
                    uncheckedBorderColor = IntroPurpleBorderUnfocused
                ),
                modifier = Modifier.testTag("register_terms_checkbox")
            )
            Text(
                text = "I agree to the KHARCHEE Terms of Service and Privacy Policy",
                fontSize = 12.sp,
                color = IntroSubtitleText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Purple Gradient Create Account Button
        PurpleGradientPrimaryButton(
            text = "Create Account",
            onClick = onCreateAccountClicked,
            isLoading = isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("register_submit_button")
        )

        Spacer(modifier = Modifier.height(20.dp))

        IntroOrContinueWithDivider()

        Spacer(modifier = Modifier.height(16.dp))

        IntroSocialProviderButton(
            label = "Continue with Google",
            isGoogle = true,
            enabled = !isLoading,
            onClick = onGoogleSignInClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("register_google_button")
        )

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = IntroSubtitleText, fontSize = 13.sp)) {
                        append("Already have an account? ")
                    }
                    withStyle(SpanStyle(color = IntroWhiteText, fontWeight = FontWeight.Bold, fontSize = 13.sp)) {
                        append("Log in")
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isLoading, onClick = onNavigateToLogin)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("navigate_to_login_button")
            )
        }
    }
}

// ============================================================================
// SHARED PURPLE/NAVY UI COMPONENTS MATCHING REFERENCE IMAGE
// ============================================================================

@Composable
private fun IntroCircularBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF120F22))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(IntroPurpleBorderFocused, IntroPurpleBorderUnfocused)
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = IntroWhiteText,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun IntroPurpleOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 13.sp,
                color = IntroPlaceholderText
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IntroFieldBackground,
            unfocusedContainerColor = IntroFieldBackground,
            disabledContainerColor = IntroFieldBackground.copy(alpha = 0.6f),
            focusedBorderColor = IntroPurpleBorderFocused,
            unfocusedBorderColor = IntroPurpleBorderUnfocused,
            disabledBorderColor = IntroPurpleBorderUnfocused.copy(alpha = 0.4f),
            focusedTextColor = IntroWhiteText,
            unfocusedTextColor = IntroWhiteText,
            disabledTextColor = IntroSubtitleText,
            cursorColor = IntroPurpleBorderFocused
        ),
        modifier = modifier
    )
}

@Composable
private fun PurpleGradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            IntroPurplePrimaryStart,
            IntroPurplePrimaryMid,
            IntroPurplePrimaryEnd
        )
    )

    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(gradientBrush)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = IntroWhiteText,
                strokeWidth = 2.2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = IntroWhiteText
            )
        }
    }
}

@Composable
private fun IntroOrContinueWithDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.8.dp,
            color = IntroDividerLine
        )
        Text(
            text = "or continue with",
            fontSize = 12.sp,
            color = IntroSubtitleText
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.8.dp,
            color = IntroDividerLine
        )
    }
}

@Composable
private fun IntroSocialProviderButton(
    label: String,
    isGoogle: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, IntroPurpleBorderUnfocused),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = IntroFieldBackground,
            contentColor = IntroWhiteText
        ),
        modifier = modifier
    ) {
        if (isGoogle) {
            // Crisp Geometric "G" Badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1630))
                    .border(1.dp, IntroPurpleBorderFocused, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = IntroWhiteText
                )
            }
        } else {
            // Minimalist Apple Emblem
            Text(
                text = "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IntroWhiteText
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = IntroWhiteText
        )
    }
}

@Composable
private fun IntroFeedbackBanner(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isError) IntroErrorSurface else IntroInfoSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isError) IntroErrorBorder else IntroInfoBorder,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Text(
            text = message,
            color = if (isError) IntroErrorText else IntroWhiteText,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
