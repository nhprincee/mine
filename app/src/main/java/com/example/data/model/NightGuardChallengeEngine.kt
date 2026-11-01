package com.example.data.model

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

data class PendingSpendRequest(
    val title: String,
    val category: String,
    val amount: Long,
    val paymentMode: String,
    val note: String = "",
    val triggerUpiGuard: Boolean = true,
)

val ESSENTIAL_BYPASS_REASONS = listOf(
    "Medical expenses",
    "Emergency travel",
    "Essential bills",
    "Other urgent necessities"
)

val REMINDER_INTERVAL_OPTIONS = listOf(
    "same_day" to "Same Day (9:00 AM)",
    "1_day_before" to "1 Day Before",
    "3_days_before" to "3 Days Before",
    "weekly" to "Weekly Recurring"
)

object NightGuardSchedule {

    fun isWithinNightGuardWindow(
        nowMillis: Long,
        startHour: Int = 22,
        startMinute: Int = 0,
        endHour: Int = 5,
        endMinute: Int = 0
    ): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startTotalMinutes = (startHour.coerceIn(0, 23) * 60) + startMinute.coerceIn(0, 59)
        val endTotalMinutes = (endHour.coerceIn(0, 23) * 60) + endMinute.coerceIn(0, 59)

        return if (startTotalMinutes == endTotalMinutes) {
            true // 24-hour window if start == end
        } else if (startTotalMinutes > endTotalMinutes) {
            // Overnight span, e.g. 22:00 (1320) to 05:00 (300)
            currentMinutes >= startTotalMinutes || currentMinutes < endTotalMinutes
        } else {
            // Same-day span, e.g. 00:00 to 05:00
            currentMinutes in startTotalMinutes until endTotalMinutes
        }
    }

    fun formatHourMinute12h(hour24: Int, minute: Int): String {
        val h = hour24.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        val amPm = if (h >= 12) "PM" else "AM"
        val hour12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, m, amPm)
    }

    fun formatTimeWindow(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): String {
        return "${formatHourMinute12h(startHour, startMinute)} – ${formatHourMinute12h(endHour, endMinute)}"
    }
}

data class NightGuardChallenge(
    val id: String,
    val category: String, // Quick Mathematics, Memory Sequence, Pattern Recognition, Word Puzzles, Short Attention Exercise
    val prompt: String,
    val subtitleHint: String,
    val options: List<String>,
    val correctAnswer: String,
    val memoryFlashSequence: String? = null,
    val estimatedSeconds: Int = 15
)

object NightGuardChallengeEngine {

    const val FIVE_MINUTES_MS = 5 * 60 * 1000L // 300,000 ms (5 minutes)

    val CHALLENGE_CATEGORIES = listOf(
        "Quick Mathematics",
        "Memory Sequence",
        "Pattern Recognition",
        "Word Puzzles",
        "Short Attention Exercise"
    )

    private val allChallenges: List<NightGuardChallenge> = listOf(
        // 1. Quick Mathematics
        NightGuardChallenge(
            id = "math_1",
            category = "Quick Mathematics",
            prompt = "Calculate before spending: (18 × 4) - 19 = ?",
            subtitleHint = "Engage analytical cortex · 15s mental arithmetic",
            options = listOf("51", "53", "57", "49"),
            correctAnswer = "53",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "math_2",
            category = "Quick Mathematics",
            prompt = "If you save ₹350 every day for 6 days and spend ₹420 once, how much net savings remain?",
            subtitleHint = "Real-world rupee arithmetic check",
            options = listOf("₹1,580", "₹1,680", "₹1,720", "₹1,640"),
            correctAnswer = "₹1,680",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "math_3",
            category = "Quick Mathematics",
            prompt = "Solve: (144 ÷ 12) + (15 × 3) = ?",
            subtitleHint = "Divide first, then add the product",
            options = listOf("55", "57", "63", "47"),
            correctAnswer = "57",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "math_4",
            category = "Quick Mathematics",
            prompt = "What is 15% of ₹2,400?",
            subtitleHint = "Mental percentage check (10% + 5%)",
            options = listOf("₹320", "₹360", "₹380", "₹340"),
            correctAnswer = "₹360",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "math_5",
            category = "Quick Mathematics",
            prompt = "Solve: 95 - (8 × 7) + 14 = ?",
            subtitleHint = "Order of operations check",
            options = listOf("51", "53", "49", "55"),
            correctAnswer = "53",
            estimatedSeconds = 15
        ),

        // 2. Memory Sequence
        NightGuardChallenge(
            id = "mem_1",
            category = "Memory Sequence",
            prompt = "Memorize the code above. What is the sequence in REVERSE order?",
            subtitleHint = "Read right-to-left carefully",
            options = listOf("9 - 2 - 7 - 4", "4 - 7 - 9 - 2", "9 - 7 - 2 - 4", "2 - 9 - 7 - 4"),
            correctAnswer = "9 - 2 - 7 - 4",
            memoryFlashSequence = "4 - 7 - 2 - 9",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "mem_2",
            category = "Memory Sequence",
            prompt = "Look at the symbol sequence above. Which symbol was in the 3RD position?",
            subtitleHint = "Count from left (1st, 2nd, 3rd...)",
            options = listOf("◆ Diamond", "▲ Triangle", "● Circle", "■ Square"),
            correctAnswer = "▲ Triangle",
            memoryFlashSequence = "● Circle  ·  ■ Square  ·  ▲ Triangle  ·  ◆ Diamond  ·  ★ Star",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "mem_3",
            category = "Memory Sequence",
            prompt = "What is the SUM of the 1st and 4th numbers in the sequence above?",
            subtitleHint = "Add only the 1st and 4th numbers",
            options = listOf("19", "21", "17", "23"),
            correctAnswer = "21",
            memoryFlashSequence = "8  ·  15  ·  6  ·  13  ·  9",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "mem_4",
            category = "Memory Sequence",
            prompt = "Which word in the sequence above has exactly 6 letters?",
            subtitleHint = "Count the letters in each word carefully",
            options = listOf("VAULT", "WEALTH", "RESERVE", "GOLD"),
            correctAnswer = "WEALTH",
            memoryFlashSequence = "VAULT  ·  GOLD  ·  WEALTH  ·  RESERVE",
            estimatedSeconds = 18
        ),
        NightGuardChallenge(
            id = "mem_5",
            category = "Memory Sequence",
            prompt = "If you remove all EVEN digits from the sequence above, what remains?",
            subtitleHint = "Keep only odd digits in their original order",
            options = listOf("7 - 3 - 9", "7 - 5 - 9", "3 - 7 - 9", "7 - 3 - 5"),
            correctAnswer = "7 - 3 - 9",
            memoryFlashSequence = "4 - 7 - 2 - 3 - 8 - 9",
            estimatedSeconds = 18
        ),

        // 3. Pattern Recognition
        NightGuardChallenge(
            id = "pat_1",
            category = "Pattern Recognition",
            prompt = "Find the next number in the sequence: 3, 7, 15, 31, 63, ?",
            subtitleHint = "Each step doubles the previous number and adds 1",
            options = listOf("125", "127", "129", "119"),
            correctAnswer = "127",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "pat_2",
            category = "Pattern Recognition",
            prompt = "Complete the Fibonacci-style reserve pattern: 5, 8, 13, 21, 34, ?",
            subtitleHint = "Add the previous two numbers together",
            options = listOf("53", "55", "57", "51"),
            correctAnswer = "55",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "pat_3",
            category = "Pattern Recognition",
            prompt = "Find the missing square number: 4, 9, 16, 25, ?, 49",
            subtitleHint = "Consecutive perfect squares (2², 3², 4², 5²...)",
            options = listOf("32", "36", "40", "35"),
            correctAnswer = "36",
            estimatedSeconds = 12
        ),
        NightGuardChallenge(
            id = "pat_4",
            category = "Pattern Recognition",
            prompt = "What comes next in the alternating sequence: 20, 18, 23, 21, 26, ?",
            subtitleHint = "Pattern alternates: -2, then +5",
            options = listOf("23", "24", "28", "25"),
            correctAnswer = "24",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "pat_5",
            category = "Pattern Recognition",
            prompt = "Complete the letter-step pattern: B2, E5, H8, K11, ?",
            subtitleHint = "Advance 3 letters and add 3 to the number",
            options = listOf("M13", "N14", "O15", "N13"),
            correctAnswer = "N14",
            estimatedSeconds = 18
        ),

        // 4. Word Puzzles
        NightGuardChallenge(
            id = "word_1",
            category = "Word Puzzles",
            prompt = "Unscramble the financial anagram:  B - U - D - G - E - T  is formed from which scrambled letters?",
            subtitleHint = "Match the exact 6 letters of BUDGET",
            options = listOf("G E D T U B", "B U G D T R", "D E B T U G", "G U B D E S"),
            correctAnswer = "G E D T U B",
            estimatedSeconds = 18
        ),
        NightGuardChallenge(
            id = "word_2",
            category = "Word Puzzles",
            prompt = "Unscramble the word 'V I N S G A S' to reveal a core wealth habit:",
            subtitleHint = "7-letter financial word starting with S",
            options = listOf("SAVINGS", "SCALING", "SHARING", "SIGNALS"),
            correctAnswer = "SAVINGS",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "word_3",
            category = "Word Puzzles",
            prompt = "Which word is the ODD ONE OUT when building long-term financial discipline?",
            subtitleHint = "Three represent wealth building; one represents impulsive outflow",
            options = listOf("COMPOUNDING", "SPLURGING", "RESERVE", "INVESTMENT"),
            correctAnswer = "SPLURGING",
            estimatedSeconds = 12
        ),
        NightGuardChallenge(
            id = "word_4",
            category = "Word Puzzles",
            prompt = "Unscramble 'P R U D E N T' — what does it mean in personal finance?",
            subtitleHint = "Acting with care and thought for the future",
            options = listOf("Wise & cautious with money", "Spending without limits", "Borrowing at high interest", "Ignoring monthly bills"),
            correctAnswer = "Wise & cautious with money",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "word_5",
            category = "Word Puzzles",
            prompt = "If 'SPEND' is coded by shifting each letter +1 forward (T-Q-F-O-E), how is 'SAVE' coded?",
            subtitleHint = "Shift S, A, V, E each 1 letter forward in the alphabet",
            options = listOf("T B W F", "T Z W F", "R B W F", "T B U F"),
            correctAnswer = "T B W F",
            estimatedSeconds = 22
        ),

        // 5. Short Attention Exercise
        NightGuardChallenge(
            id = "att_1",
            category = "Short Attention Exercise",
            prompt = "Count carefully: How many times does the digit '7' appear in  7170774727 ?",
            subtitleHint = "Scan each digit slowly from left to right",
            options = listOf("5", "6", "7", "4"),
            correctAnswer = "6",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "att_2",
            category = "Short Attention Exercise",
            prompt = "In the phrase 'THINK TWICE BEFORE LATE NIGHT SPENDING', how many words have 5 or more letters?",
            subtitleHint = "Check the length of each of the 6 words",
            options = listOf("4 words", "5 words", "6 words", "3 words"),
            correctAnswer = "5 words",
            estimatedSeconds = 20
        ),
        NightGuardChallenge(
            id = "att_3",
            category = "Short Attention Exercise",
            prompt = "Select the option that is written in STRICTLY ASCENDING numerical order:",
            subtitleHint = "Every number must be larger than the one before it",
            options = listOf(
                "14 · 29 · 47 · 63 · 88",
                "14 · 32 · 29 · 63 · 88",
                "18 · 24 · 51 · 49 · 92",
                "12 · 27 · 45 · 44 · 79"
            ),
            correctAnswer = "14 · 29 · 47 · 63 · 88",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "att_4",
            category = "Short Attention Exercise",
            prompt = "How many '₹' symbols appear in this string:  ₹ · $ · ₹ · € · ₹ · ₹ · £ · ₹ ?",
            subtitleHint = "Ignore $, €, and £ — count only ₹",
            options = listOf("4", "5", "6", "3"),
            correctAnswer = "5",
            estimatedSeconds = 15
        ),
        NightGuardChallenge(
            id = "att_5",
            category = "Short Attention Exercise",
            prompt = "Which two numbers in  [ 12,  25,  38,  41,  65 ]  add up to exactly 90?",
            subtitleHint = "Check pairs that end in digits summing to 10",
            options = listOf("25 and 65", "12 and 65", "38 and 41", "25 and 41"),
            correctAnswer = "25 and 65",
            estimatedSeconds = 18
        )
    )

    fun getAllChallenges(): List<NightGuardChallenge> = allChallenges

    fun pickNextChallenge(
        excludeIds: Set<String> = emptySet(),
        preferredCategory: String? = null
    ): NightGuardChallenge {
        val poolByCategory = if (!preferredCategory.isNullOrBlank()) {
            allChallenges.filter { it.category.equals(preferredCategory, ignoreCase = true) }
        } else {
            allChallenges
        }.ifEmpty { allChallenges }

        val filtered = poolByCategory.filter { it.id !in excludeIds }
        val candidates = filtered.ifEmpty {
            allChallenges.filter { it.id !in excludeIds }.ifEmpty { allChallenges }
        }
        return candidates[Random.nextInt(candidates.size)]
    }

    fun validateAnswer(challenge: NightGuardChallenge, selectedAnswer: String): Boolean {
        return challenge.correctAnswer.trim().equals(selectedAnswer.trim(), ignoreCase = true)
    }
}

object NightGuardCooldownStore {
    private const val PREFS_NAME = "kharchee_night_guard_prefs"
    private const val KEY_COOLDOWN_PREFIX = "cooldown_end_ms_"
    private const val KEY_RECENT_IDS_PREFIX = "recent_challenge_ids_"

    fun getLocalCooldownEndMillis(context: Context?, userId: String): Long {
        if (context == null || userId.isBlank()) return 0L
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_COOLDOWN_PREFIX + userId, 0L)
    }

    fun saveLocalCooldownEndMillis(context: Context?, userId: String, cooldownEndMillis: Long) {
        if (context == null || userId.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_COOLDOWN_PREFIX + userId, cooldownEndMillis).apply()
    }

    fun resolveEffectiveCooldownEndMillis(
        context: Context?,
        userId: String,
        firestoreCooldownEndMillis: Long
    ): Long {
        val localEnd = getLocalCooldownEndMillis(context, userId)
        val effective = max(localEnd, firestoreCooldownEndMillis)
        if (effective > localEnd && context != null) {
            saveLocalCooldownEndMillis(context, userId, effective)
        }
        return effective
    }

    fun getRemainingCooldownMillis(
        nowMillis: Long,
        effectiveCooldownEndMillis: Long
    ): Long {
        return max(0L, effectiveCooldownEndMillis - nowMillis)
    }

    fun formatRemainingCooldown(remainingMillis: Long): String {
        val totalSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun getRecentChallengeIds(context: Context?, userId: String): Set<String> {
        if (context == null || userId.isBlank()) return emptySet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENT_IDS_PREFIX + userId, "") ?: ""
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun recordRecentChallengeId(context: Context?, userId: String, challengeId: String): Set<String> {
        if (context == null || userId.isBlank() || challengeId.isBlank()) return setOf(challengeId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = (prefs.getString(KEY_RECENT_IDS_PREFIX + userId, "") ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != challengeId }
        val updated = (current + challengeId).takeLast(8)
        prefs.edit().putString(KEY_RECENT_IDS_PREFIX + userId, updated.joinToString(",")).apply()
        return updated.toSet()
    }
}

data class DailyNightSpendBucket(
    val dayLabel: String,
    val amount: Long
)

data class NightSpendingSummary(
    val totalNightSpentAllTime: Long,
    val nightSpentLast7Days: Long,
    val nightSpentPrev7Days: Long,
    val nightSpentLast30Days: Long,
    val nightTxCountLast30Days: Int,
    val weeklyTrendPercent: Int,
    val cancelledCount: Int,
    val postponedCount: Int,
    val proceededCount: Int,
    val essentialBypassCount: Int,
    val savedFromCancelledAmount: Long,
    val postponedAmount: Long,
    val dailyBuckets7d: List<DailyNightSpendBucket>
)

object NightSpendingAnalytics {

    fun computeSummary(
        expenses: List<ExpenseRecord>,
        nightGuardEvents: List<NightGuardEventRecord>,
        settings: UserSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): NightSpendingSummary {
        val dayMs = 86_400_000L
        val sevenDaysAgo = nowMillis - (7L * dayMs)
        val fourteenDaysAgo = nowMillis - (14L * dayMs)
        val thirtyDaysAgo = nowMillis - (30L * dayMs)

        val nightExpenses = expenses.filter { tx ->
            !isIncomeTransaction(tx) && NightGuardSchedule.isWithinNightGuardWindow(
                nowMillis = tx.txEpochMillis,
                startHour = settings.nightGuardStartHour,
                startMinute = settings.nightGuardStartMinute,
                endHour = settings.nightGuardEndHour,
                endMinute = settings.nightGuardEndMinute
            )
        }

        val totalNightSpentAllTime = nightExpenses.sumOf { it.amount }
        val nightSpentLast7Days = nightExpenses.filter { it.txEpochMillis >= sevenDaysAgo }.sumOf { it.amount }
        val nightSpentPrev7Days = nightExpenses.filter { it.txEpochMillis in fourteenDaysAgo until sevenDaysAgo }.sumOf { it.amount }
        val nightExpenses30d = nightExpenses.filter { it.txEpochMillis >= thirtyDaysAgo }
        val nightSpentLast30Days = nightExpenses30d.sumOf { it.amount }

        val weeklyTrendPercent = when {
            nightSpentPrev7Days > 0L -> (((nightSpentLast7Days - nightSpentPrev7Days).toDouble() / nightSpentPrev7Days.toDouble()) * 100.0).toInt()
            nightSpentLast7Days > 0L -> 100
            else -> 0
        }

        val cancelledEvents = nightGuardEvents.filter { it.outcome == "cancelled" }
        val postponedEvents = nightGuardEvents.filter { it.outcome == "postponed" }
        val proceededEvents = nightGuardEvents.filter { it.outcome == "proceeded" }
        val bypassEvents = nightGuardEvents.filter { it.outcome == "essential_bypass" }

        val dayFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dailyBuckets = (6 downTo 0).map { offset ->
            val targetTime = nowMillis - offset * dayMs
            val label = dayFmt.format(Date(targetTime))
            val dayNightSum = nightExpenses
                .filter { isSameDay(it.txEpochMillis, targetTime) }
                .sumOf { it.amount }
            DailyNightSpendBucket(dayLabel = label, amount = dayNightSum)
        }

        return NightSpendingSummary(
            totalNightSpentAllTime = totalNightSpentAllTime,
            nightSpentLast7Days = nightSpentLast7Days,
            nightSpentPrev7Days = nightSpentPrev7Days,
            nightSpentLast30Days = nightSpentLast30Days,
            nightTxCountLast30Days = nightExpenses30d.size,
            weeklyTrendPercent = weeklyTrendPercent,
            cancelledCount = cancelledEvents.size,
            postponedCount = postponedEvents.size,
            proceededCount = proceededEvents.size,
            essentialBypassCount = bypassEvents.size,
            savedFromCancelledAmount = cancelledEvents.sumOf { it.amount },
            postponedAmount = postponedEvents.sumOf { it.amount },
            dailyBuckets7d = dailyBuckets
        )
    }
}
