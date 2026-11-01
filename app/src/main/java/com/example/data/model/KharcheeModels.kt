package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dailyLimit: Long = 500L,
    val lockdownActive: Boolean = false,
    val autoLockdownAt90: Boolean = true,
    val stealthMode: Boolean = false,
    val themeId: String = "stark",
    val myUpiId: String = "",
    val upiGuardTone: String = "hinglish",
    @get:PropertyName("isPro") @PropertyName("isPro") val isPro: Boolean = false,
    val proTier: String = "free", // "free", "gold", "platinum"
    val proExpiryMillis: Long = 0L,
    val nightGuardEnabled: Boolean = true,
    val nightGuardStartHour: Int = 22,
    val nightGuardStartMinute: Int = 0,
    val nightGuardEndHour: Int = 5,
    val nightGuardEndMinute: Int = 0,
    val nightGuardCooldownEndMillis: Long = 0L,
    val lastChallengeId: String = "",
    val enablePreSpendConfirm: Boolean = false,
    val notifyPaymentReminders: Boolean = true,
    val notifyNightGuardAlert: Boolean = true,
    val notifyMorningReview: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class ExpenseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Food",
    val amount: Long = 0L,
    val paymentMode: String = "UPI",
    val type: String = "expense",
    val note: String = "",
    val txEpochMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class KhataFriendRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val avatarInitials: String = "FR",
    val upiId: String = "",
    val balance: Long = 0L,
    val lastDescription: String = "",
    val settled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SavingsGoalRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val targetAmount: Long = 25000L,
    val savedAmount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class JarvisChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user", // "user" or "jarvis"
    val text: String = "",
    val mode: String = "roast", // "roast" or "advice"
    val language: String = "hinglish", // "hinglish", "hindi", or "english"
    val txEpochMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PaymentReminderRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val category: String = "Bills",
    val dueEpochMillis: Long = System.currentTimeMillis() + 86_400_000L,
    val intervalType: String = "same_day", // "same_day", "1_day_before", "3_days_before", "weekly"
    @get:PropertyName("isPaid") @PropertyName("isPaid") val isPaid: Boolean = false,
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class NightGuardEventRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Shopping",
    val amount: Long = 0L,
    val outcome: String = "cancelled", // "cancelled", "postponed", "proceeded", "essential_bypass"
    val bypassReason: String = "",
    val challengeCategory: String = "",
    val eventEpochMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class QuickAddPreset(
    val id: String,
    val emoji: String,
    val label: String,
    val amount: Long,
    val category: String,
)

val QUICK_ADD_PRESETS = listOf(
    QuickAddPreset("chai", "☕", "Chai", 10L, "Chai & Snacks"),
    QuickAddPreset("diet", "🥪", "Diet", 50L, "Food"),
    QuickAddPreset("petrol", "⛽", "Petrol", 100L, "Transport"),
    QuickAddPreset("grocery", "🛒", "Grocery", 200L, "Grocery"),
)

val EXPENSE_CATEGORIES = listOf(
    "Food",
    "Chai & Snacks",
    "Transport",
    "Grocery",
    "Entertainment",
    "Bills",
    "Shopping",
)

val INCOME_CATEGORIES = listOf(
    "Salary",
    "Freelance",
    "Cashback",
)

val ALL_CATEGORIES = EXPENSE_CATEGORIES + INCOME_CATEGORIES
val PAYMENT_MODES = listOf("UPI", "Cash", "Card")

fun isIncomeTransaction(record: ExpenseRecord): Boolean {
    return record.type == "income" || record.category in INCOME_CATEGORIES
}

data class ParsedSmsTransaction(
    val rawText: String,
    val amount: Long,
    val merchant: String,
    val category: String,
    val paymentMode: String,
    val confidence: String,
    val isIgnoredSecurityMessage: Boolean = false,
)

data class SampleBankSms(
    val label: String,
    val appTitle: String,
    val sms: String,
)

val SAMPLE_BANK_SMS_LIST = listOf(
    SampleBankSms(
        label = "GPay · Chai ₹30",
        appTitle = "Google Pay",
        sms = "Paid ₹30 to Sharma Chai Tapri via UPI. UPI Ref: 430291847561."
    ),
    SampleBankSms(
        label = "PhonePe · Swiggy ₹320",
        appTitle = "PhonePe",
        sms = "Paid ₹320 to SWIGGY from HDFC Bank A/c **4582. UTR: 430299182736."
    ),
    SampleBankSms(
        label = "Paytm · Myntra ₹699",
        appTitle = "Paytm",
        sms = "Money Sent: Paid Rs. 699 to Myntra Designs Pvt Ltd via UPI."
    ),
    SampleBankSms(
        label = "BHIM · Uber Auto ₹140",
        appTitle = "BHIM",
        sms = "INR 140.00 debited via BHIM UPI to Uber India Systems. Ref 430288172635."
    ),
    SampleBankSms(
        label = "SBI · HP Petrol ₹150",
        appTitle = "SBI Alert",
        sms = "Your A/C ending 3012 is debited by INR 150.00 on 24Oct24 spent at HP PETROL PUMP via UPI. - SBI"
    ),
    SampleBankSms(
        label = "Ignored · Bank OTP (Safety Test)",
        appTitle = "Security Alert",
        sms = "Your OTP for payment of Rs 500 is 482910. Do not share your OTP or UPI PIN with anyone."
    )
)

object SmsTransactionParser {
    private val categoryKeywords = mapOf(
        "Chai & Snacks" to listOf("chai", "tea", "coffee", "tapri", "chaayos", "starbucks", "samosa", "snack", "bakery", "cafe"),
        "Food" to listOf("swiggy", "zomato", "restaurant", "biryani", "paneer", "shawarma", "pizza", "burger", "dining", "food", "kitchen"),
        "Transport" to listOf("petrol", "fuel", "hpcl", "bpcl", "iocl", "uber", "ola", "rapido", "metro", "irctc", "auto", "cab", "parking"),
        "Grocery" to listOf("zepto", "blinkit", "instamart", "bigbasket", "dmart", "grocery", "milk", "vegetables", "supermarket", "oats"),
        "Entertainment" to listOf("pvr", "inox", "bookmyshow", "netflix", "spotify", "turf", "movie", "cinema", "gaming"),
        "Bills" to listOf("electricity", "broadband", "jio", "airtel", "vi", "recharge", "bescom", "water", "gas", "bill", "rent"),
        "Shopping" to listOf("myntra", "amazon", "flipkart", "ajio", "nykaa", "meesho", "zara", "shopping", "mall", "store"),
        "Salary" to listOf("salary", "payroll", "stipend", "employer"),
        "Freelance" to listOf("freelance", "client", "invoice", "consulting"),
        "Cashback" to listOf("cashback", "reward", "refund")
    )

    private val sensitiveIgnoreRegex = Regex(
        "\\b(otp|one time password|verification code|upi pin|mpin|enter pin|passcode|password|cvv|do not share|requested|collect request)\\b",
        RegexOption.IGNORE_CASE
    )

    private val amountRegexes = listOf(
        Regex("(?:debited(?:\\s+by)?|spent|paid|transferred|deducted)\\s*(?:of)?\\s*(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:is|was)?\\s*(?:debited|spent|paid|deducted)", RegexOption.IGNORE_CASE),
        Regex("paid\\s+(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE)
    )

    private val merchantPatterns = listOf(
        Regex("(?:to|at|towards|transfer\\s+to|paid\\s+to)\\s+(?:vpa\\s+)?([A-Za-z0-9&.\\-_ ]+?)(?:\\s+(?:on|ref|via|using|avl|bal|upi|thru|from|utr|\\.|,)|$)", RegexOption.IGNORE_CASE),
        Regex("at\\s+([A-Za-z0-9&.\\-_ ]+?)(?:\\s+on|\\s+dated|\\.|,|$)", RegexOption.IGNORE_CASE)
    )

    fun parse(text: String): ParsedSmsTransaction? {
        val normalized = text.trim()
        if (normalized.isEmpty()) return null

        if (sensitiveIgnoreRegex.containsMatchIn(normalized)) {
            return ParsedSmsTransaction(
                rawText = normalized,
                amount = 0L,
                merchant = "Blocked Security / OTP Message",
                category = "Food",
                paymentMode = "UPI",
                confidence = "blocked",
                isIgnoredSecurityMessage = true
            )
        }

        var extractedAmount: Long? = null
        for (regex in amountRegexes) {
            val match = regex.find(normalized)
            if (match != null) {
                val cleaned = match.groupValues[1].replace(",", "")
                val parsed = cleaned.toDoubleOrNull()?.roundToInt()?.toLong()
                if (parsed != null && parsed > 0L) {
                    extractedAmount = parsed
                    break
                }
            }
        }
        val amount = extractedAmount ?: return null

        var merchant = ""
        for (pattern in merchantPatterns) {
            val match = pattern.find(normalized)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.length > 1 && !candidate.lowercase().contains("a/c")) {
                    merchant = candidate
                        .replace(Regex("\\b(ref|upi|utr|txn|avl|bal)\\b.*$", RegexOption.IGNORE_CASE), "")
                        .trim(' ', '.', ',', '-', '_')
                    break
                }
            }
        }
        if (merchant.isBlank()) {
            merchant = "UPI Merchant"
        }

        val searchTarget = "${merchant.lowercase()} ${normalized.lowercase()}"
        var detectedCategory = "Food"
        var matchedCat = false
        for ((cat, keywords) in categoryKeywords) {
            if (keywords.any { searchTarget.contains(it) }) {
                detectedCategory = cat
                matchedCat = true
                break
            }
        }

        val lower = normalized.lowercase()
        val paymentMode = when {
            lower.contains("card") || lower.contains("pos") -> "Card"
            lower.contains("cash") || lower.contains("atm") -> "Cash"
            else -> "UPI"
        }

        val confidence = if (matchedCat && merchant != "UPI Merchant") "high" else "medium"
        return ParsedSmsTransaction(
            rawText = normalized,
            amount = amount,
            merchant = merchant.take(80),
            category = detectedCategory,
            paymentMode = paymentMode,
            confidence = confidence,
            isIgnoredSecurityMessage = false
        )
    }
}

data class AccountabilityEvaluation(
    val headline: String,
    val message: String,
    val urgency: String, // "critical" | "warning" | "safe"
    val dailyUsedPercent: Int,
    val dailyRemainingAfter: Long,
    val monthlyRemainingAfter: Long,
)

object UpiAccountabilityEngine {
    fun evaluate(
        amount: Long,
        merchant: String,
        category: String,
        todaySpentBefore: Long,
        monthSpentBefore: Long,
        dailyLimit: Long,
        sameCategoryTodayCount: Int,
        tone: String
    ): AccountabilityEvaluation {
        val safeDailyLimit = max(50L, dailyLimit)
        val monthlyBudget = safeDailyLimit * 30L
        val todaySpentAfter = todaySpentBefore + amount
        val monthSpentAfter = monthSpentBefore + amount
        val dailyRemainingAfter = max(0L, safeDailyLimit - todaySpentAfter)
        val monthlyRemainingAfter = max(0L, monthlyBudget - monthSpentAfter)
        val dailyUsedPercent = ((todaySpentAfter.toDouble() / safeDailyLimit.toDouble()) * 100.0).roundToInt()

        val isDailyBreach = todaySpentAfter > safeDailyLimit
        val isDailyNear = !isDailyBreach && dailyUsedPercent >= 80
        val isRepeatChai = category == "Chai & Snacks" && sameCategoryTodayCount >= 2

        val urgency = when {
            isDailyBreach -> "critical"
            isDailyNear || isRepeatChai -> "warning"
            else -> "safe"
        }

        val headline: String
        val message: String

        if (isDailyBreach) {
            if (tone == "hinglish") {
                headline = "🚨 Bhai, Aaj Ka Limit Cross Ho Gaya!"
                message = "Paid ₹$amount to $merchant. Aaj ₹$todaySpentAfter kharch ho chuke hain (Daily limit ₹$safeDailyLimit). Monthly budget mein ₹$monthlyRemainingAfter bacha hai — ab thoda break laga!"
            } else if (tone == "strict") {
                headline = "🚨 Daily Spending Limit Exceeded"
                message = "₹$amount at $merchant pushed today's spend to ₹$todaySpentAfter ($dailyUsedPercent% of ₹$safeDailyLimit limit). Remaining monthly budget: ₹$monthlyRemainingAfter."
            } else {
                headline = "⚠️ Over Today's Spending Limit"
                message = "You spent ₹$amount at $merchant. Today's total is ₹$todaySpentAfter against your ₹$safeDailyLimit daily target."
            }
        } else if (isDailyNear) {
            if (tone == "hinglish") {
                headline = "💸 Bhai, spending limit ke paas ho — aaj thoda ruk!"
                message = "₹$amount at $merchant ($category). Aaj ke ₹$safeDailyLimit limit mein sirf ₹$dailyRemainingAfter bacha hai ($dailyUsedPercent% used)."
            } else {
                headline = "⚠️ $dailyUsedPercent% of Daily Limit Used"
                message = "₹$amount paid to $merchant. Only ₹$dailyRemainingAfter left for today and ₹$monthlyRemainingAfter left this month."
            }
        } else if (isRepeatChai) {
            if (tone == "hinglish") {
                headline = "☕ Bhai chai/snacks phir se?"
                message = "Aaj ki #$sameCategoryTodayCount $category payment (₹$amount at $merchant)! Today remaining: ₹$dailyRemainingAfter."
            } else {
                headline = "☕ Repeat $category Purchase (#$sameCategoryTodayCount Today)"
                message = "Logged ₹$amount at $merchant. Remaining daily budget: ₹$dailyRemainingAfter."
            }
        } else {
            if (tone == "hinglish") {
                headline = "✅ ₹$amount logged ($merchant)"
                message = "Control mein ho bhai! Aaj ke limit mein abhi ₹$dailyRemainingAfter bacha hai (Monthly remaining: ₹$monthlyRemainingAfter)."
            } else {
                headline = "✅ Paid ₹$amount to $merchant"
                message = "Within budget: ₹$dailyRemainingAfter left today ($dailyUsedPercent% used) · ₹$monthlyRemainingAfter left this month."
            }
        }

        return AccountabilityEvaluation(
            headline = headline,
            message = message,
            urgency = urgency,
            dailyUsedPercent = min(999, dailyUsedPercent),
            dailyRemainingAfter = dailyRemainingAfter,
            monthlyRemainingAfter = monthlyRemainingAfter
        )
    }
}

data class StarkBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val requirementText: String,
    val symbol: String,
)

val STARK_ARC_BADGES = listOf(
    StarkBadge("iron-discipline", "Iron Discipline", "Stayed under daily limit for 3+ days", "3-Day Streak", "⚡"),
    StarkBadge("zero-leak-legend", "Zero-Leak Legend", "Tracked every UPI & Cash expense", "5+ Logs", "🛡️"),
    StarkBadge("chai-commander", "Tapri Commander", "Logged Chai & Snacks with full honesty", "Chai Tracker", "☕"),
    StarkBadge("khata-kingpin", "Khata Recovery Pro", "Split bills & tracked friend settlements", "Active Khata", "👑"),
    StarkBadge("rainy-day-architect", "Vault Architect", "Built a rainy-day emergency savings goal", "Savings Goal", "💎")
)

fun isSameDay(epochA: Long, epochB: Long): Boolean {
    val calA = Calendar.getInstance().apply { timeInMillis = epochA }
    val calB = Calendar.getInstance().apply { timeInMillis = epochB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

fun isSameMonth(epochA: Long, epochB: Long): Boolean {
    val calA = Calendar.getInstance().apply { timeInMillis = epochA }
    val calB = Calendar.getInstance().apply { timeInMillis = epochB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH)
}

data class ProactiveFinancialInsight(
    val id: String,
    val title: String,
    val message: String,
    val category: String,
    val severity: String, // "surge" | "warning" | "positive" | "info"
    val suggestedPrompt: String,
    val iconEmoji: String = "⚡",
    val highlightStat: String = ""
)
