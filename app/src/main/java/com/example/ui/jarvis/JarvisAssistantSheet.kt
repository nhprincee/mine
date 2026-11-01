package com.example.ui.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.JarvisChatMessage
import com.example.data.model.ProactiveFinancialInsight
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
import com.example.ui.viewmodel.JarvisErrorState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

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
    var voiceStatusBanner by remember { mutableStateOf<String?>(null) }
    var lastSpokenMessage by remember {
        mutableStateOf(messages.lastOrNull { it.role == "jarvis" }?.text.orEmpty())
    }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = initialPrompt
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "jarvisCorePulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsePhase"
    )

    BackHandler(onBack = onClose)

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

    val activeStateColor = when {
        isListening -> CrimsonRuby
        isSpeaking -> ChampagneGold
        isGenerating -> SophisticatedEmerald
        else -> ChampagneGold
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.94f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .testTag("jarvis_assistant_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(ObsidianBlack)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            ChampagneGold.copy(alpha = 0.65f),
                            MutedPlatinum.copy(alpha = 0.3f),
                            ChampagneGold.copy(alpha = 0.55f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            // --- LUXURY AI CONCIERGE HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RichGraphite)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Animated Acoustic Neural Orb
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = (size.minDimension / 2f) * (0.75f + 0.22f * pulsePhase)
                            drawCircle(
                                color = activeStateColor.copy(alpha = 0.18f),
                                radius = radius
                            )
                            drawCircle(
                                color = activeStateColor.copy(alpha = 0.5f),
                                radius = size.minDimension * 0.42f,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElevatedGraphite)
                                .border(0.8.dp, activeStateColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "J.A.R.V.I.S. AI Core",
                                tint = activeStateColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "J.A.R.V.I.S.",
                                fontFamily = headingFont,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory,
                                letterSpacing = 1.5.sp,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = activeStateColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(0.8.dp, activeStateColor.copy(alpha = 0.45f), RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = when {
                                        isSpeaking -> "🔊 SPEAKING"
                                        isListening -> "🎙️ LISTENING"
                                        isGenerating -> "◆ ANALYZING"
                                        else -> "CONCIERGE ONLINE"
                                    },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    color = activeStateColor,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "Private Wealth Intelligence · Hindi · Hinglish · English",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedIvory,
                            fontSize = 11.sp
                        )
                    }
                }

                // Voice Controls: Mute/Unmute, Replay, Stop Speaking, Clear, Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val nextMute = !isVoiceMuted
                            isVoiceMuted = nextMute
                            if (nextMute) stopSpeaking()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("jarvis_mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isVoiceMuted) "Unmute Spoken Voice" else "Mute Spoken Voice",
                            tint = if (isVoiceMuted) CrimsonRuby else ChampagneGold,
                            modifier = Modifier.size(20.dp)
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
                            .testTag("jarvis_replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay Last Spoken Response",
                            tint = if (lastSpokenMessage.isNotBlank()) MutedPlatinum else MutedIvory.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isSpeaking) {
                        IconButton(
                            onClick = { stopSpeaking() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("jarvis_stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop Speaking",
                                tint = CrimsonRuby,
                                modifier = Modifier.size(22.dp)
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
                                .testTag("jarvis_clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Conversation History",
                                tint = MutedIvory,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            stopSpeaking()
                            onClose()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("jarvis_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close J.A.R.V.I.S.",
                            tint = WarmIvory,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // Animated Acoustic Waveform Visualizer when Listening or Speaking
            AnimatedVisibility(visible = isListening || isSpeaking) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedGraphite)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isListening) "🎙️ LISTENING TO VOICE COMMAND..." else "🔊 J.A.R.V.I.S. VOICE SYNTHESIS ACTIVE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        color = activeStateColor
                    )
                    Canvas(modifier = Modifier.width(88.dp).height(18.dp)) {
                        val barCount = 12
                        val barWidth = 4.dp.toPx()
                        val gap = (size.width - barCount * barWidth) / (barCount - 1)
                        for (i in 0 until barCount) {
                            val wave = abs(sin((pulsePhase * 6.28f) + i * 0.55f)).toFloat()
                            val barH = (size.height * (0.25f + 0.75f * wave)).coerceAtLeast(4f)
                            val topY = (size.height - barH) / 2f
                            drawRoundRect(
                                color = activeStateColor,
                                topLeft = Offset(i * (barWidth + gap), topY),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }
            }

            // --- PERSONA MODE + LANGUAGE + LIVE TELEMETRY STRIP ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RichGraphite.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryMiniBadge(label = "TODAY", value = "₹$todaySpent / ₹$dailyLimit", color = WarmIvory)
                    TelemetryMiniBadge(label = "MONTH", value = "₹$monthSpent", color = ChampagneGold)
                    TelemetryMiniBadge(label = "DISCIPLINE", value = "$disciplineScore", color = SophisticatedEmerald)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "roast" to "◆ Roast Mode",
                        "advice" to "❖ Wealth Advisor"
                    ).forEach { (modeKey, modeLabel) ->
                        val selected = activeMode == modeKey
                        Surface(
                            color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ElevatedGraphite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    0.8.dp,
                                    if (selected) ChampagneGold else HairlinePlatinum,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelectMode(modeKey) }
                                .testTag("jarvis_mode_$modeKey")
                        ) {
                            Text(
                                text = modeLabel,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) ChampagneGold else MutedPlatinum,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                            color = if (selected) MutedPlatinum.copy(alpha = 0.18f) else ElevatedGraphite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    0.8.dp,
                                    if (selected) MutedPlatinum else HairlinePlatinum,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelectLanguage(langKey) }
                                .testTag("jarvis_lang_$langKey")
                        ) {
                            Text(
                                text = langLabel,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) WarmIvory else MutedIvory,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            if (voiceStatusBanner != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedGraphite)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = voiceStatusBanner!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmIvory,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "Dismiss",
                        color = ChampagneGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { voiceStatusBanner = null }
                            .padding(start = 8.dp)
                    )
                }
            }

            // --- CONVERSATION MESSAGES AREA ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(ObsidianBlack)
                    .testTag("jarvis_message_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    JarvisHolographicOrbVisualizer(
                        isListening = isListening,
                        isThinking = isGenerating,
                        isSpeaking = isSpeaking,
                        activeMode = activeMode,
                        activeLanguage = activeLanguage,
                        lastSpokenPreview = lastSpokenMessage,
                        onToggleMic = { handleMicButtonClick() },
                        onStopSpeaking = { stopSpeaking() }
                    )
                }

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
                    JarvisWelcomeCard(
                        userDisplayName = userDisplayName,
                        todaySpent = todaySpent,
                        dailyLimit = dailyLimit,
                        monthSpent = monthSpent,
                        onSelectQuickPrompt = { prompt ->
                            onSendMessage(prompt)
                        }
                    )
                }

                items(messages, key = { it.id.ifEmpty { "${it.txEpochMillis}_${it.role}" } }) { message ->
                    JarvisMessageBubble(
                        message = message,
                        onSpeakMessage = { text ->
                            isVoiceMuted = false
                            speakText(text)
                        }
                    )
                }

                if (isGenerating) {
                    item {
                        JarvisStreamingBubble(streamingText = streamingReply)
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

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // --- QUICK SUGGESTION & PROACTIVE CHIPS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RichGraphite)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dynamic Proactive Insights
                proactiveInsights.forEach { insight ->
                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(1.dp, ChampagneGold, RoundedCornerShape(50))
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
                                color = ChampagneGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                JARVIS_QUICK_PROMPTS.forEach { item ->
                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                            .clickable(enabled = !isGenerating) {
                                onSendMessage(item.prompt)
                            }
                            .testTag("jarvis_quick_prompt_${item.id}")
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = WarmIvory,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- BOTTOM COMPOSER BAR (VOICE + TEXT INPUT + SEND) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RichGraphite)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = { handleMicButtonClick() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) CrimsonRuby.copy(alpha = 0.22f)
                            else ElevatedGraphite
                        )
                        .border(
                            0.8.dp,
                            if (isListening) CrimsonRuby else ChampagneGold,
                            CircleShape
                        )
                        .testTag("jarvis_voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Voice Input" else "Start Voice Input",
                        tint = if (isListening) CrimsonRuby else ChampagneGold,
                        modifier = Modifier.size(21.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = when (activeLanguage) {
                                "hindi" -> "जार्विस से हिंदी में पूछें (खर्च, बजट, खाता)..."
                                "english" -> "Ask J.A.R.V.I.S. about your expenses or reserve..."
                                else -> "Ask Jarvis in Hinglish, Hindi or English..."
                            },
                            fontSize = 13.sp,
                            color = MutedIvory
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChampagneGold,
                        unfocusedBorderColor = HairlinePlatinum,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory,
                        focusedContainerColor = ObsidianBlack,
                        unfocusedContainerColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("jarvis_chat_input_field")
                )

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
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isGenerating) ChampagneGold
                            else ElevatedGraphite
                        )
                        .testTag("jarvis_send_message_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = ChampagneGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message to J.A.R.V.I.S.",
                            tint = if (inputText.isNotBlank()) ObsidianBlack else MutedIvory,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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
            color = MutedIvory
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

@Composable
private fun JarvisWelcomeCard(
    userDisplayName: String,
    todaySpent: Long,
    dailyLimit: Long,
    monthSpent: Long,
    onSelectQuickPrompt: (String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlineGold, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SOVEREIGN AI WEALTH CONCIERGE",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.2.sp,
            color = ChampagneGold
        )
        Text(
            text = "At Your Service, $userDisplayName",
            fontFamily = headingFont,
            style = MaterialTheme.typography.titleLarge,
            color = WarmIvory,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Synchronized with your live Kharchee ledger (Today: ₹$todaySpent / ₹$dailyLimit · Month: ₹$monthSpent). Converse in Hindi (हिंदी), Hinglish, or English—or tap the microphone for hands-free voice synthesis.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedPlatinum,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onSelectQuickPrompt("Analyze my actual expenses this month and tell me how to save more.")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Analyze Portfolio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    onSelectQuickPrompt("Mera aaj aur is mahine ka kharcha dekh ke Hinglish mein roast karo aur saving tip do!")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElevatedGraphite,
                    contentColor = ChampagneGold
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(0.8.dp, HairlineGold, RoundedCornerShape(12.dp))
            ) {
                Text("◆ Hinglish Roast", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun JarvisMessageBubble(
    message: JarvisChatMessage,
    onSpeakMessage: (String) -> Unit
) {
    val isUser = message.role == "user"
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val timeStr = remember(message.txEpochMillis) {
        timeFormatter.format(Date(message.txEpochMillis))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) ElevatedGraphite else RichGraphite
                )
                .border(
                    0.8.dp,
                    if (isUser) HairlineGold else HairlinePlatinum,
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 15.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) "YOU" else "J.A.R.V.I.S. · ${message.mode.uppercase()}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isUser) ChampagneGold else MutedPlatinum
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedIvory
                    )
                    if (!isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElevatedGraphite)
                                .border(0.6.dp, HairlineGold, RoundedCornerShape(8.dp))
                                .clickable { onSpeakMessage(message.text) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak or Replay Message",
                                tint = ChampagneGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Speak",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChampagneGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = WarmIvory,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun JarvisStreamingBubble(streamingText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .background(RichGraphite)
                .border(0.8.dp, HairlineGold, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .padding(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = ChampagneGold,
                    strokeWidth = 1.8.dp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "J.A.R.V.I.S. SYNTHESIZING LEDGER TELEMETRY...",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChampagneGold
                )
            }
            if (streamingText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = streamingText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WarmIvory,
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
            .clip(RoundedCornerShape(16.dp))
            .background(RichGraphite)
            .border(0.8.dp, CrimsonRuby.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Error",
                tint = CrimsonRuby,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (errorState.isRateLimit) "RATE LIMIT / QUOTA NOTICE" else "J.A.R.V.I.S. CONNECTION NOTICE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = CrimsonRuby
            )
        }
        Text(
            text = errorState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmIvory,
            fontSize = 13.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(10.dp),
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
                color = MutedPlatinum,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
