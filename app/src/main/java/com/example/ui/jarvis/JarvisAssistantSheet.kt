package com.example.ui.jarvis

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.JarvisChatMessage
import com.example.data.model.ProactiveFinancialInsight
import com.example.ui.components.KharcheeSymbolLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.viewmodel.JarvisErrorState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class JarvisQuickPrompt(
    val id: String,
    val label: String,
    val prompt: String
)

private val JARVIS_QUICK_PROMPTS = listOf(
    JarvisQuickPrompt(
        id = "daily_summary",
        label = "📊 Daily Summary",
        prompt = "Provide a comprehensive daily financial summary for today: total spent, percentage of daily limit used, top category outlays, and remaining allowance."
    ),
    JarvisQuickPrompt(
        id = "spending_analysis",
        label = "◆ Expense Analysis",
        prompt = "Analyze my actual expenses this month by category, highlight my biggest spending leaks, and suggest where I can cut back."
    ),
    JarvisQuickPrompt(
        id = "hinglish_roast",
        label = "◆ Hinglish Roast",
        prompt = "Jarvis bhai, mera aaj aur is mahine ka kharcha dekh ke sach batao—paisa kahan udd raha hai? Roast me with real numbers!"
    ),
    JarvisQuickPrompt(
        id = "hindi_summary",
        label = "◆ हिंदी बजट रिपोर्ट",
        prompt = "मेरे अब तक के खर्च, दैनिक बजट सीमा और बचत लक्ष्यों का हिंदी में पूरा विश्लेषण और सुझाव दें।"
    ),
    JarvisQuickPrompt(
        id = "budget_plan",
        label = "◆ Smart Budget Plan",
        prompt = "Based on my current income, daily limit, and savings goals, give me a practical daily and weekly budget plan to boost my savings."
    ),
    JarvisQuickPrompt(
        id = "khata_check",
        label = "◆ Khata Recovery",
        prompt = "Who owes me money in my Khata split ledger and who do I owe? Give me a quick recovery summary."
    )
)

private fun formatInr(amount: Long): String {
    return try {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        "₹${formatter.format(amount)}"
    } catch (_: Exception) {
        "₹$amount"
    }
}

@Composable
fun JarvisAssistantOverlay(
    userDisplayName: String,
    todaySpent: Long,
    monthSpent: Long,
    dailyLimit: Long,
    disciplineScore: Int,
    messages: List<JarvisChatMessage>,
    streamingReply: String,
    isGenerating: Boolean,
    activeMode: String,
    activeLanguage: String,
    errorState: JarvisErrorState?,
    latestSpokenText: String?,
    onConsumeLatestSpokenText: () -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    proactiveInsights: List<ProactiveFinancialInsight> = emptyList(),
    pendingProposedAction: JarvisProposedAction? = null,
    onConfirmProposedAction: ((JarvisProposedAction) -> Unit)? = null,
    onDismissProposedAction: (() -> Unit)? = null,
    initialPrompt: String? = null,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf(initialPrompt.orEmpty()) }
    var isVoiceMuted by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var showQuickPromptsStrip by remember { mutableStateOf(true) }
    var showProfileCommandDialog by remember { mutableStateOf(false) }
    var showVoiceAnalysisStage by remember { mutableStateOf(false) }
    var voiceStatusBanner by remember { mutableStateOf<String?>(null) }
    var lastSpokenMessage by remember {
        mutableStateOf(messages.lastOrNull { it.role == "jarvis" }?.text.orEmpty())
    }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = initialPrompt
        }
    }

    BackHandler {
        if (showVoiceAnalysisStage) {
            showVoiceAnalysisStage = false
        } else {
            onClose()
        }
    }

    LaunchedEffect(messages) {
        val latestJarvis = messages.lastOrNull { it.role == "jarvis" }?.text
        if (!latestJarvis.isNullOrBlank()) {
            lastSpokenMessage = latestJarvis
        }
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(streamingReply) {
        if (streamingReply.isNotEmpty() && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var ttsRef: TextToSpeech? = null
        val tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = ttsRef
                if (engine != null) {
                    val preferredLocale = Locale("en", "IN")
                    val res = engine.setLanguage(preferredLocale)
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.language = Locale.US
                    }
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                        }

                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                        }
                    })
                    ttsReady = true
                }
            }
        }
        ttsRef = tts
        ttsInstance = tts

        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {
            }
        }
    }

    fun sanitizeForSpeech(raw: String): String {
        return raw
            .replace(Regex("[*_#`~>|]"), " ")
            .replace("₹", " rupees ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speakText(textToSpeak: String) {
        val clean = sanitizeForSpeech(textToSpeak)
        if (clean.isEmpty()) return
        lastSpokenMessage = textToSpeak
        val engine = ttsInstance
        if (engine == null || !ttsReady) {
            voiceStatusBanner = "Text-to-Speech engine initializing on device..."
            return
        }
        val targetLocale = if (activeLanguage == "hindi" || textToSpeak.any { it in '\u0900'..'\u097F' }) {
            Locale("hi", "IN")
        } else {
            Locale("en", "IN")
        }
        val langResult = engine.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.US
        }
        isSpeaking = true
        engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utt_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        try {
            ttsInstance?.stop()
        } catch (_: Exception) {
        }
        isSpeaking = false
    }

    LaunchedEffect(latestSpokenText, isVoiceMuted, ttsReady) {
        val text = latestSpokenText
        if (!text.isNullOrBlank()) {
            lastSpokenMessage = text
            if (!isVoiceMuted && ttsReady) {
                speakText(text)
            }
            onConsumeLatestSpokenText()
        }
    }

    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim().orEmpty()
            if (spoken.isNotEmpty()) {
                inputText = spoken
                voiceStatusBanner = null
                showVoiceAnalysisStage = false
                onSendMessage(spoken)
                inputText = ""
            }
        }
    }

    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    DisposableEffect(context) {
        val recognizer = if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
        speechRecognizer = recognizer
        onDispose {
            try {
                recognizer?.destroy()
            } catch (_: Exception) {
            }
        }
    }

    fun startVoiceRecognition() {
        stopSpeaking()
        val speechLocale = if (activeLanguage == "hindi") "hi-IN" else "en-IN"
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to J.A.R.V.I.S. (Hindi, Hinglish, or English)")
        }

        val recognizer = speechRecognizer
        if (recognizer != null) {
            try {
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        voiceStatusBanner = "🎙️ Listening (${if (activeLanguage == "hindi") "Hindi" else "Hinglish / English"})... Speak now"
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        try {
                            speechIntentLauncher.launch(recognizerIntent)
                        } catch (_: Exception) {
                            voiceStatusBanner = "Voice recognition unavailable on this device/emulator. Type below or tap a quick prompt chip."
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val transcript = matches?.firstOrNull()?.trim().orEmpty()
                        if (transcript.isNotEmpty()) {
                            voiceStatusBanner = null
                            inputText = ""
                            showVoiceAnalysisStage = false
                            onSendMessage(transcript)
                        } else {
                            voiceStatusBanner = "Didn't catch that. Tap the mic to try again or type your message."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (partial.isNotBlank()) {
                            inputText = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                isListening = true
                recognizer.startListening(recognizerIntent)
                return
            } catch (_: Exception) {
                isListening = false
            }
        }

        try {
            speechIntentLauncher.launch(recognizerIntent)
        } catch (_: Exception) {
            isListening = false
            voiceStatusBanner = "Speech recognition service is not installed on this device/emulator. Use text input or quick chips below."
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceStatusBanner = null
            startVoiceRecognition()
        } else {
            isListening = false
            voiceStatusBanner = "Microphone permission was denied. Grant RECORD_AUDIO permission to use voice input, or type your query below."
        }
    }

    fun handleMicButtonClick() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
            }
            isListening = false
            voiceStatusBanner = null
            return
        }

        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            startVoiceRecognition()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val coreStatus = resolveJarvisCoreStatus(
        isListening = isListening,
        isThinking = isGenerating,
        isSpeaking = isSpeaking
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisDeepNavyTop,
                        JarvisDeepNavyMid,
                        JarvisAbyssBlack
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .testTag("jarvis_assistant_overlay"),
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight
        val isWideScreen = maxWidth >= 640.dp
        // Compact circular JARVIS visual in the main conversation layout
        val compactReactorSize = when {
            availableHeight < 560.dp -> 84.dp
            availableHeight < 720.dp -> 96.dp
            isWideScreen -> 114.dp
            else -> 104.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = if (isWideScreen) 18.dp else 8.dp, vertical = 6.dp)
                .shadow(32.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisDeepNavyTop,
                            JarvisDeepNavyMid,
                            JarvisAbyssBlack
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            JarvisCyanBright.copy(alpha = 0.55f),
                            JarvisHairlineCyan,
                            JarvisCyanPrimary.copy(alpha = 0.50f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // --- 1. AI COMMAND CENTER HEADER WITH JARVIS BRANDING & PROFILE ACCESS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                JarvisDeepNavyTop,
                                JarvisPanelBlack
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular Glowing Back / Close Button (matches center & right screens of reference)
                    IconButton(
                        onClick = {
                            stopSpeaking()
                            onClose()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisElevatedSurface)
                            .border(1.dp, JarvisCyanPrimary.copy(alpha = 0.65f), CircleShape)
                            .testTag("jarvis_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close J.A.R.V.I.S.",
                            tint = JarvisTextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Profile & Branding Access Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showProfileCommandDialog = true }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                            .testTag("jarvis_profile_access_button")
                    ) {
                        JarvisCircularReferenceBadge(
                            size = 38.dp,
                            isAnimated = isListening || isGenerating || isSpeaking
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                KharcheeSymbolLogo(height = 13.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "J.A.R.V.I.S.",
                                    fontFamily = headingFont,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JarvisTextWhite,
                                    letterSpacing = 1.4.sp,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = JarvisCyanPrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.border(
                                        0.8.dp,
                                        JarvisCyanBright.copy(alpha = 0.6f),
                                        RoundedCornerShape(50)
                                    )
                                ) {
                                    Text(
                                        text = coreStatus.label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = JarvisCyanBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Hi there, $userDisplayName · AI Command Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Voice, Replay, Clear & Profile Menu Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val nextMute = !isVoiceMuted
                            isVoiceMuted = nextMute
                            if (nextMute) stopSpeaking()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisElevatedSurface.copy(alpha = 0.7f))
                            .border(0.8.dp, JarvisHairlineCyan, CircleShape)
                            .testTag("jarvis_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isVoiceMuted) "Unmute Spoken Voice" else "Mute Spoken Voice",
                            tint = if (isVoiceMuted) JarvisAlertCrimson else JarvisCyanBright,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (lastSpokenMessage.isNotBlank()) {
                                isVoiceMuted = false
                                speakText(lastSpokenMessage)
                            }
                        },
                        enabled = lastSpokenMessage.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisElevatedSurface.copy(alpha = 0.7f))
                            .border(0.8.dp, JarvisHairlineCyan, CircleShape)
                            .testTag("jarvis_replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay Last Spoken Response",
                            tint = if (lastSpokenMessage.isNotBlank()) JarvisCyanBright else JarvisTextSecondary.copy(alpha = 0.35f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (isSpeaking) {
                        IconButton(
                            onClick = { stopSpeaking() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(JarvisAlertCrimson.copy(alpha = 0.16f))
                                .border(0.8.dp, JarvisAlertCrimson, CircleShape)
                                .testTag("jarvis_stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop Speaking",
                                tint = JarvisAlertCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                stopSpeaking()
                                onClearHistory()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(JarvisElevatedSurface.copy(alpha = 0.7f))
                                .border(0.8.dp, JarvisHairlineCyan, CircleShape)
                                .testTag("jarvis_clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Conversation History",
                                tint = JarvisTextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = JarvisHairlineCyan, thickness = 0.8.dp)

            // --- 2. TELEMETRY + PERSONA MODE + LANGUAGE + VOICE STAGE BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisPanelBlack.copy(alpha = 0.90f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryMiniBadge(
                        label = "TODAY",
                        value = "${formatInr(todaySpent)} / ${formatInr(dailyLimit)}",
                        color = JarvisTextWhite
                    )
                    TelemetryMiniBadge(
                        label = "MONTH",
                        value = formatInr(monthSpent),
                        color = JarvisCyanBright
                    )
                    TelemetryMiniBadge(
                        label = "DISCIPLINE",
                        value = "$disciplineScore/100",
                        color = JarvisEmeraldAccent
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle between Chat Command Center and Full Voice Analysis Stage
                    Surface(
                        color = if (showVoiceAnalysisStage) JarvisCyanPrimary.copy(alpha = 0.28f) else JarvisElevatedSurface,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(
                                0.9.dp,
                                if (showVoiceAnalysisStage) JarvisCyanBright else JarvisCyanPrimary.copy(alpha = 0.55f),
                                RoundedCornerShape(50)
                            )
                            .clickable { showVoiceAnalysisStage = !showVoiceAnalysisStage }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Voice Analysis Mode",
                                tint = JarvisCyanBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (showVoiceAnalysisStage) "Chat View" else "Voice Analysis",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JarvisTextWhite
                            )
                        }
                    }

                    listOf(
                        "roast" to "◆ Roast Mode",
                        "advice" to "❖ Wealth Advisor"
                    ).forEach { (modeKey, modeLabel) ->
                        val selected = activeMode == modeKey
                        Surface(
                            color = if (selected) JarvisCyanPrimary.copy(alpha = 0.22f) else JarvisElevatedSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    0.8.dp,
                                    if (selected) JarvisCyanBright else JarvisHairlineCyan,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelectMode(modeKey) }
                                .testTag("jarvis_mode_$modeKey")
                        ) {
                            Text(
                                text = modeLabel,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) JarvisCyanBright else JarvisTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    listOf(
                        "hinglish" to "Hinglish",
                        "hindi" to "हिंदी (Hindi)",
                        "english" to "English"
                    ).forEach { (langKey, langLabel) ->
                        val selected = activeLanguage == langKey
                        Surface(
                            color = if (selected) JarvisCyanAccent.copy(alpha = 0.25f) else JarvisElevatedSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    0.8.dp,
                                    if (selected) JarvisCyanBright else JarvisHairlineCyan,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelectLanguage(langKey) }
                                .testTag("jarvis_lang_$langKey")
                        ) {
                            Text(
                                text = langLabel,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) JarvisTextWhite else JarvisTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (voiceStatusBanner != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisElevatedSurface)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Status",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = voiceStatusBanner!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = JarvisTextWhite,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "Dismiss",
                        color = JarvisCyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { voiceStatusBanner = null }
                            .padding(start = 8.dp)
                    )
                }
            }

            if (showVoiceAnalysisStage) {
                // --- FULL VOICE ANALYSIS STAGE (Matches right screen of reference image) ---
                JarvisVoiceAnalysisFullStage(
                    isListening = isListening,
                    isThinking = isGenerating,
                    isSpeaking = isSpeaking,
                    activeMode = activeMode,
                    activeLanguage = activeLanguage,
                    lastSpokenPreview = lastSpokenMessage,
                    onToggleMic = { handleMicButtonClick() },
                    onReplayVoice = {
                        if (lastSpokenMessage.isNotBlank()) {
                            isVoiceMuted = false
                            speakText(lastSpokenMessage)
                        }
                    },
                    onStopSpeaking = { stopSpeaking() },
                    onCloseStage = { showVoiceAnalysisStage = false },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                // --- 3. COMPACT CIRCULAR JARVIS VISUAL (Pinterest Reference Core) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JarvisHolographicOrbVisualizer(
                        isListening = isListening,
                        isThinking = isGenerating,
                        isSpeaking = isSpeaking,
                        activeMode = activeMode,
                        activeLanguage = activeLanguage,
                        lastSpokenPreview = lastSpokenMessage,
                        onToggleMic = { handleMicButtonClick() },
                        onStopSpeaking = { stopSpeaking() },
                        reactorSize = compactReactorSize
                    )
                }

                HorizontalDivider(color = JarvisHairlineCyan, thickness = 0.8.dp)

                // --- 4. MAIN CONVERSATION AREA WITH BENTO COMMANDS, MESSAGES & INSIGHT CARDS ---
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("jarvis_message_list"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (pendingProposedAction != null) {
                        item {
                            JarvisActionConfirmationCard(
                                proposedAction = pendingProposedAction,
                                onConfirm = { action ->
                                    onConfirmProposedAction?.invoke(action)
                                },
                                onCancel = {
                                    onDismissProposedAction?.invoke()
                                }
                            )
                        }
                    }

                    item {
                        JarvisCommandCenterWelcomeSection(
                            userDisplayName = userDisplayName,
                            todaySpent = todaySpent,
                            dailyLimit = dailyLimit,
                            monthSpent = monthSpent,
                            disciplineScore = disciplineScore,
                            onSelectQuickPrompt = { prompt ->
                                onSendMessage(prompt)
                            }
                        )
                    }

                    items(messages, key = { it.id.ifEmpty { "${it.txEpochMillis}_${it.role}" } }) { message ->
                        JarvisMessageBubble(
                            message = message,
                            todaySpent = todaySpent,
                            dailyLimit = dailyLimit,
                            monthSpent = monthSpent,
                            disciplineScore = disciplineScore,
                            onSpeakMessage = { text ->
                                isVoiceMuted = false
                                speakText(text)
                            }
                        )
                    }

                    if (isGenerating) {
                        item {
                            JarvisStreamingBubble(
                                streamingText = streamingReply,
                                todaySpent = todaySpent,
                                dailyLimit = dailyLimit,
                                monthSpent = monthSpent
                            )
                        }
                    }

                    if (errorState != null) {
                        item {
                            JarvisErrorBanner(
                                errorState = errorState,
                                onRetry = {
                                    val retryPrompt = errorState.lastFailedPrompt
                                    onDismissError()
                                    if (retryPrompt.isNotBlank()) {
                                        onSendMessage(retryPrompt)
                                    }
                                },
                                onDismiss = onDismissError
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = JarvisHairlineCyan, thickness = 0.8.dp)

            // --- 5. QUICK SUGGESTION & PROACTIVE CHIPS ROW ---
            AnimatedVisibility(visible = showQuickPromptsStrip) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisPanelBlack)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    proactiveInsights.forEach { insight ->
                        Surface(
                            color = JarvisElevatedSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(1.dp, JarvisCyanPrimary, RoundedCornerShape(50))
                                .clickable(enabled = !isGenerating) {
                                    onSendMessage(insight.suggestedPrompt)
                                }
                                .testTag("jarvis_proactive_chip_${insight.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = insight.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = insight.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JarvisCyanBright,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    JARVIS_QUICK_PROMPTS.forEach { item ->
                        Surface(
                            color = JarvisElevatedSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(0.8.dp, JarvisHairlineCyan, RoundedCornerShape(50))
                                .clickable(enabled = !isGenerating) {
                                    onSendMessage(item.prompt)
                                }
                                .testTag("jarvis_quick_prompt_${item.id}")
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = JarvisTextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // --- 6. BOTTOM MESSAGE INPUT BAR (Matches center screen of reference image) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisPanelBlack)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill input container with '+' toggle, text field, and integrated voice button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(JarvisAbyssBlack)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    JarvisCyanPrimary.copy(alpha = 0.70f),
                                    JarvisHairlineCyan,
                                    JarvisCyanBright.copy(alpha = 0.65f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showQuickPromptsStrip = !showQuickPromptsStrip },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Toggle Quick Commands",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = when (activeLanguage) {
                                    "hindi" -> "जार्विस से हिंदी में पूछें (खर्च, बजट, खाता)..."
                                    "english" -> "Ask AI anything about your finances..."
                                    else -> "Ask AI anything in Hinglish, Hindi or English..."
                                },
                                fontSize = 13.sp,
                                color = JarvisTextSecondary
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = JarvisTextWhite,
                            unfocusedTextColor = JarvisTextWhite,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = JarvisCyanBright
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("jarvis_chat_input_field")
                    )

                    IconButton(
                        onClick = { handleMicButtonClick() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) JarvisCyanPrimary.copy(alpha = 0.30f)
                                else JarvisElevatedSurface
                            )
                            .border(
                                1.dp,
                                if (isListening) JarvisCyanBright else JarvisCyanPrimary.copy(alpha = 0.65f),
                                CircleShape
                            )
                            .testTag("jarvis_voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Voice Input" else "Start Voice Input",
                            tint = if (isListening) JarvisCyanBright else JarvisTextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Glowing Purple Circular Send Button
                IconButton(
                    onClick = {
                        val toSend = inputText.trim()
                        if (toSend.isNotEmpty() && !isGenerating) {
                            inputText = ""
                            onSendMessage(toSend)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = if (inputText.isNotBlank() && !isGenerating) 12.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = JarvisCyanPrimary,
                            spotColor = JarvisCyanPrimary
                        )
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isGenerating) {
                                Brush.linearGradient(
                                    colors = listOf(JarvisCyanPrimary, JarvisCyanAccent)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(JarvisElevatedSurface, JarvisPanelBlack)
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (inputText.isNotBlank() && !isGenerating) JarvisCyanBright else JarvisHairlineCyan,
                            CircleShape
                        )
                        .testTag("jarvis_send_message_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = JarvisCyanBright,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message to J.A.R.V.I.S.",
                            tint = if (inputText.isNotBlank()) JarvisTextWhite else JarvisTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showProfileCommandDialog) {
        JarvisCommandCenterProfileDialog(
            userDisplayName = userDisplayName,
            todaySpent = todaySpent,
            dailyLimit = dailyLimit,
            monthSpent = monthSpent,
            disciplineScore = disciplineScore,
            activeMode = activeMode,
            activeLanguage = activeLanguage,
            onSelectMode = onSelectMode,
            onSelectLanguage = onSelectLanguage,
            onDismiss = { showProfileCommandDialog = false }
        )
    }
}

/**
 * Full Voice Analysis Stage inspired by the right screen of the uploaded reference image,
 * featuring the large circular Pinterest J.A.R.V.I.S. core visual, live status, and 3 circular bottom controls.
 */
@Composable
private fun JarvisVoiceAnalysisFullStage(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    activeMode: String,
    activeLanguage: String,
    lastSpokenPreview: String,
    onToggleMic: () -> Unit,
    onReplayVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onCloseStage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val coreStatus = resolveJarvisCoreStatus(isListening, isThinking, isSpeaking)

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Voice Analysis",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = JarvisTextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (coreStatus) {
                    JarvisCoreStatus.LISTENING -> "Listening..."
                    JarvisCoreStatus.THINKING -> "Synthesizing KHARCHEE Ledger..."
                    JarvisCoreStatus.SPEAKING -> "Speaking Financial Briefing..."
                    JarvisCoreStatus.ONLINE -> "Ready for Voice Command"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = JarvisCyanBright,
                fontSize = 14.sp
            )
        }

        // Large Circular Pinterest J.A.R.V.I.S. Visual
        JarvisHolographicOrbVisualizer(
            isListening = isListening,
            isThinking = isThinking,
            isSpeaking = isSpeaking,
            activeMode = activeMode,
            activeLanguage = activeLanguage,
            lastSpokenPreview = lastSpokenPreview,
            onToggleMic = onToggleMic,
            onStopSpeaking = onStopSpeaking,
            reactorSize = 190.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Analyze your live KHARCHEE ledger\ninstantly using voice commands",
                fontFamily = headingFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = JarvisTextWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = "— smart, fast, and powered by real financial data\nin Hindi, Hinglish & English.",
                style = MaterialTheme.typography.bodySmall,
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom 3 Circular Controls: Replay/Reset, Large Glowing Mic, Close Stage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onReplayVoice,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(JarvisElevatedSurface)
                    .border(1.dp, JarvisHairlineCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Replay Spoken Response",
                    tint = JarvisTextWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = {
                    if (isSpeaking) onStopSpeaking() else onToggleMic()
                },
                modifier = Modifier
                    .size(72.dp)
                    .shadow(20.dp, CircleShape, ambientColor = JarvisCyanPrimary, spotColor = JarvisCyanPrimary)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                JarvisCyanPrimary.copy(alpha = 0.45f),
                                JarvisElevatedSurface
                            )
                        )
                    )
                    .border(1.6.dp, JarvisCyanBright, CircleShape)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Toggle Voice Recognition",
                    tint = JarvisTextWhite,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = onCloseStage,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(JarvisElevatedSurface)
                    .border(1.dp, JarvisHairlineCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Return to Chat View",
                    tint = JarvisTextWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TelemetryMiniBadge(
    label: String,
    value: String,
    color: Color
) {
    val headingFont = LocalAppFontFamily.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp,
            color = JarvisTextSecondary
        )
        Text(
            text = value,
            fontFamily = headingFont,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

/**
 * AI Command Center Bento Welcome Section (inspired by the left screen of the uploaded reference image).
 * Uses 100% real KHARCHEE financial data and triggers real JARVIS prompts.
 */
@Composable
private fun JarvisCommandCenterWelcomeSection(
    userDisplayName: String,
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long,
    disciplineScore: Int,
    onSelectQuickPrompt: (String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisElevatedSurface.copy(alpha = 0.90f),
                        JarvisPanelBlack
                    )
                )
            )
            .border(1.dp, JarvisHairlineCyan, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Smart Help,\nInstantly for You",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                color = JarvisTextWhite
            )
            Text(
                text = "Synchronized with $userDisplayName's live KHARCHEE ledger · Today ${formatInr(todaySpent)} / ${formatInr(dailyLimit)} · Month ${formatInr(monthSpent)}",
                style = MaterialTheme.typography.bodySmall,
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        // Asymmetric Bento Command Grid (matches left screen of reference image)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Tall Bento Card: Monthly Expense & Portfolio Intelligence
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(198.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                JarvisCyanDeep.copy(alpha = 0.65f),
                                JarvisUserBubbleEnd,
                                JarvisAbyssBlack
                            )
                        )
                    )
                    .border(1.dp, JarvisCyanPrimary.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .clickable {
                        onSelectQuickPrompt("Analyze my actual expenses this month by category, highlight my biggest spending leaks, and tell me how to save more.")
                    }
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(JarvisElevatedSurface)
                            .border(1.dp, JarvisCyanBright.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analyze Expenses",
                            tint = JarvisCyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = formatInr(monthSpent),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = JarvisCyanBright
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Analyzing\nPortfolio",
                                fontFamily = headingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                color = JarvisTextWhite
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Run Analysis",
                                tint = JarvisTextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Right Stacked Bento Cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                JarvisCompactBentoActionCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Daily Reserve\nAudit",
                    subtitle = "${formatInr(todaySpent)} / ${formatInr(dailyLimit)}",
                    onClick = {
                        onSelectQuickPrompt("Provide a comprehensive daily financial summary for today: total spent, percentage of daily limit used, and remaining allowance.")
                    }
                )

                JarvisCompactBentoActionCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Smart Budget\n& Hinglish Roast",
                    subtitle = "Score $disciplineScore/100",
                    onClick = {
                        onSelectQuickPrompt("Mera aaj aur is mahine ka kharcha dekh ke Hinglish mein roast karo aur saving tip do!")
                    }
                )
            }
        }
    }
}

@Composable
private fun JarvisCompactBentoActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(JarvisPanelBlack)
            .border(1.dp, JarvisHairlineCyan, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(JarvisElevatedSurface)
                    .border(0.8.dp, JarvisCyanPrimary.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = JarvisCyanBright,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = subtitle,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                color = JarvisCyanBright,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                fontFamily = headingFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = JarvisTextWhite
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Launch Command",
                tint = JarvisTextWhite,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Distinct Visual Styles for User and JARVIS Messages + Embedded Financial Insight Cards
 * matching the center "Chat Bot" screen of the uploaded reference image.
 */
@Composable
private fun JarvisMessageBubble(
    message: JarvisChatMessage,
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long,
    disciplineScore: Int,
    onSpeakMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val timeStr = remember(message.txEpochMillis) {
        timeFormatter.format(Date(message.txEpochMillis))
    }

    val showEmbeddedInsightCard = !isUser && remember(message.text) {
        val lower = message.text.lowercase()
        lower.contains("₹") ||
            lower.contains("spent") ||
            lower.contains("budget") ||
            lower.contains("limit") ||
            lower.contains("kharcha") ||
            lower.contains("खर्च") ||
            lower.contains("बजट") ||
            lower.contains("saving")
    }

    if (isUser) {
        // --- USER MESSAGE BUBBLE (Right-aligned glowing Purple-Violet glass bubble) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                JarvisUserBubbleStart,
                                JarvisUserBubbleEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        JarvisCyanPrimary.copy(alpha = 0.70f),
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JarvisTextWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = JarvisTextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    } else {
        // --- JARVIS MESSAGE BUBBLE (Left circular Pinterest JARVIS avatar + Dark Navy card + Financial Insight Card + Copy/Speak/Share pills) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            JarvisCircularReferenceBadge(
                size = 34.dp,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .background(JarvisPanelBlack)
                        .border(
                            1.dp,
                            JarvisHairlineCyan,
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "J.A.R.V.I.S. · ${message.mode.uppercase()}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            letterSpacing = 0.9.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanBright
                        )
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = JarvisTextSecondary
                        )
                    }

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = JarvisTextWhite,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    // Embedded Live Financial Insight Card inside the conversation
                    if (showEmbeddedInsightCard) {
                        JarvisConversationFinancialInsightCard(
                            todaySpent = todaySpent,
                            dailyLimit = dailyLimit,
                            monthSpent = monthSpent,
                            disciplineScore = disciplineScore
                        )
                    }
                }

                // Functional Copy, Speak & Share Action Pills (matches center screen of reference image)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JarvisBubbleActionPill(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("JARVIS Insight", message.text))
                            Toast.makeText(context, "Copied J.A.R.V.I.S. response", Toast.LENGTH_SHORT).show()
                        }
                    )

                    JarvisBubbleActionPill(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Speak",
                        onClick = { onSpeakMessage(message.text) }
                    )

                    JarvisBubbleActionPill(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = {
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message.text)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share J.A.R.V.I.S. Insight"))
                            } catch (_: Exception) {
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun JarvisBubbleActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = JarvisPanelBlack,
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, JarvisHairlineCyan),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyanBright,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = JarvisTextWhite
            )
        }
    }
}

/**
 * Financial Insight Card embedded directly inside JARVIS conversation bubbles.
 * Displays 100% real KHARCHEE ledger metrics (Today Reserve, Monthly Outflow, Discipline Score).
 */
@Composable
private fun JarvisConversationFinancialInsightCard(
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long,
    disciplineScore: Int
) {
    val safeLimit = dailyLimit.coerceAtLeast(1L)
    val dailyRatio = (todaySpent.toFloat() / safeLimit.toFloat()).coerceIn(0f, 1f)
    val remainingToday = (dailyLimit - todaySpent).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        JarvisElevatedSurface,
                        JarvisDeepNavyMid
                    )
                )
            )
            .border(0.9.dp, JarvisCyanPrimary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Live Ledger Snapshot",
                    tint = JarvisCyanBright,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE KHARCHEE TELEMETRY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = JarvisCyanBright
                )
            }
            Text(
                text = "Score $disciplineScore/100",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisEmeraldAccent
            )
        }

        // 2x2 Mini Insight Tiles (matching the 2x2 card grid inside the chat bubble in the reference image)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JarvisInsightMiniTile(
                label = "TODAY SPENT",
                value = formatInr(todaySpent),
                sub = "Limit ${formatInr(dailyLimit)}",
                accent = if (todaySpent > dailyLimit) JarvisAlertCrimson else JarvisCyanBright,
                modifier = Modifier.weight(1f)
            )
            JarvisInsightMiniTile(
                label = "MONTH OUTFLOW",
                value = formatInr(monthSpent),
                sub = "Left Today ${formatInr(remainingToday)}",
                accent = JarvisEmeraldAccent,
                modifier = Modifier.weight(1f)
            )
        }

        LinearProgressIndicator(
            progress = { dailyRatio },
            color = if (todaySpent > dailyLimit) JarvisAlertCrimson else JarvisCyanPrimary,
            trackColor = JarvisAbyssBlack,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

@Composable
private fun JarvisInsightMiniTile(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisPanelBlack)
            .border(0.7.dp, JarvisHairlineCyan, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            color = JarvisTextSecondary,
            letterSpacing = 0.6.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = accent
        )
        Text(
            text = sub,
            fontSize = 10.sp,
            color = JarvisTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Typing / Thinking Indicator when JARVIS is processing a financial query.
 */
@Composable
private fun JarvisStreamingBubble(
    streamingText: String,
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvisThinkingDots")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        JarvisCircularReferenceBadge(
            size = 34.dp,
            isAnimated = true
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(JarvisPanelBlack)
                .border(1.dp, JarvisCyanPrimary.copy(alpha = 0.65f), RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .padding(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = JarvisCyanBright,
                    strokeWidth = 1.8.dp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "J.A.R.V.I.S. IS THINKING · ANALYZING LEDGER (${formatInr(todaySpent)} / ${formatInr(dailyLimit)})...",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyanBright.copy(alpha = dotPulse)
                )
            }
            if (streamingText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = streamingText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JarvisTextWhite,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun JarvisErrorBanner(
    errorState: JarvisErrorState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(JarvisPanelBlack)
            .border(1.dp, JarvisAlertCrimson.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Error",
                tint = JarvisAlertCrimson,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (errorState.isRateLimit) "RATE LIMIT / QUOTA NOTICE" else "J.A.R.V.I.S. CONNECTION NOTICE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisAlertCrimson
            )
        }
        Text(
            text = errorState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextWhite,
            fontSize = 13.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyanPrimary,
                    contentColor = JarvisTextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("jarvis_retry_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Query", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "Dismiss",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = JarvisTextSecondary,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * In-screen AI Command Center Profile & Ledger Telemetry Dialog opened when the user taps
 * the profile header inside JARVIS.
 */
@Composable
private fun JarvisCommandCenterProfileDialog(
    userDisplayName: String,
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long,
    disciplineScore: Int,
    activeMode: String,
    activeLanguage: String,
    onSelectMode: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisDeepNavyTop,
                            JarvisPanelBlack
                        )
                    )
                )
                .border(1.2.dp, JarvisCyanPrimary, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JarvisCircularReferenceBadge(size = 44.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userDisplayName,
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = JarvisTextWhite
                        )
                        Text(
                            text = "J.A.R.V.I.S. AI Command Center Profile",
                            fontSize = 11.sp,
                            color = JarvisCyanBright
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Profile",
                        tint = JarvisTextSecondary
                    )
                }
            }

            JarvisConversationFinancialInsightCard(
                todaySpent = todaySpent,
                dailyLimit = dailyLimit,
                monthSpent = monthSpent,
                disciplineScore = disciplineScore
            )

            Text(
                text = "ACTIVE PERSONA & LANGUAGE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisTextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("advice" to "Wealth Advisor", "roast" to "Roast Mode").forEach { (key, label) ->
                    val selected = activeMode == key
                    Surface(
                        color = if (selected) JarvisCyanPrimary else JarvisElevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMode(key) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("hinglish" to "Hinglish", "hindi" to "हिंदी", "english" to "English").forEach { (key, label) ->
                    val selected = activeLanguage == key
                    Surface(
                        color = if (selected) JarvisCyanAccent else JarvisElevatedSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectLanguage(key) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyanPrimary,
                    contentColor = JarvisTextWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to J.A.R.V.I.S. Command Center", fontWeight = FontWeight.Bold)
            }
        }
    }
}
