package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class SniffedCouponOffer(
    val code: String,
    val merchant: String,
    val category: String,
    val discountSummary: String,
    val estimatedSavingsInr: Long,
    val sourceSender: String,
    val rawSmsSnippet: String,
    val detectedAtMillis: Long = System.currentTimeMillis()
)

/**
 * AI Coupon & Cashback Auto-Sniffer:
 * Scans incoming bank/UPI/merchant notification SMS headers and bodies for discount/cashback codes
 * (e.g., "Use HDFC100", "ZOMATO50", "SWIGGYIT", "AXIS150") and surfaces a 1-tap contextual pill
 * inside Add Expense and Quick Spend flows.
 */
object CouponSniffer {

    private const val PREFS_NAME = "kharchee_ai_coupon_sniffer_prefs"
    private const val KEY_SNIFFED_COUPONS_JSON = "sniffed_coupons_json"

    // Matches explicit promo phrases like:
    // "Use HDFC100", "Use code ZOMATO50", "Apply code SWIGGY75", "Code: AXIS150", "promo code PVR100"
    private val EXPLICIT_CODE_PATTERNS = listOf(
        Regex(
            """\b(?:use\s+code|apply\s+code|promo\s+code|coupon\s+code|discount\s+code|voucher\s+code|use\s+promo|use\s+coupon|code\s*[:\-]|use|apply)\s+['"]?([A-Z0-9]{4,14})['"]?""",
            RegexOption.IGNORE_CASE
        ),
        Regex(
            """\b([A-Z]{3,10}\d{2,4})\b(?=.*?\b(?:off|cashback|discount|save|flat|upto|order|bill|voucher)\b)""",
            RegexOption.IGNORE_CASE
        )
    )

    private val RUPEE_DISCOUNT_REGEX = Regex(
        """(?:flat\s+|save\s+|get\s+|upto\s+|cashback\s+of\s+)?(?:Rs\.?|INR|\u20B9)\s*(\d{2,4})\s*(?:off|cashback|discount|back)?""",
        RegexOption.IGNORE_CASE
    )

    private val PERCENT_DISCOUNT_REGEX = Regex(
        """(\d{1,2})%\s*(?:off|cashback|discount)""",
        RegexOption.IGNORE_CASE
    )

    // Words that look like codes after "use" or "apply" but are NOT coupon codes
    private val NON_COUPON_STOPWORDS = setOf(
        "UPI", "CARD", "DEBIT", "CREDIT", "NEFT", "IMPS", "RTGS", "YOUR", "THIS", "LINK",
        "APP", "NOW", "ONLY", "FROM", "WITH", "BANK", "HDFC", "ICICI", "SBIN", "AXIS",
        "KOTAK", "PAYTM", "GPAY", "PASS", "PIN", "OTP", "MPIN", "CVV", "UTR", "INFO", "VPA"
    )

    private val IGNORE_OTP_REGEX = Regex(
        """\b(otp|one[-\s]?time\s+password|verification\s+code|upi\s+pin|mpin|do\s+not\s+share)\b""",
        RegexOption.IGNORE_CASE
    )

    private val MERCHANT_CATEGORY_HINTS = listOf(
        Triple("Zomato", "Food", listOf("zomato", "zmt")),
        Triple("Swiggy", "Food", listOf("swiggy", "instamart")),
        Triple("Domino's", "Food", listOf("domino", "pizza")),
        Triple("Starbucks / Cafe", "Chai & Snacks", listOf("starbucks", "chaayos", "coffee", "chai")),
        Triple("Blinkit / Zepto", "Grocery", listOf("blinkit", "zepto", "bigbasket", "grocery")),
        Triple("Uber / Ola", "Transport", listOf("uber", "ola", "rapido", "metro")),
        Triple("BookMyShow / PVR", "Entertainment", listOf("bookmyshow", "pvr", "inox", "movie", "netflix")),
        Triple("Amazon / Flipkart", "Shopping", listOf("amazon", "flipkart", "myntra", "ajio", "nykaa")),
        Triple("Jio / Airtel Bills", "Bills", listOf("jio", "airtel", "electricity", "recharge", "broadband")),
        Triple("HDFC Bank Offer", "Food", listOf("hdfc")),
        Triple("ICICI Bank Offer", "Shopping", listOf("icici")),
        Triple("Axis Bank Offer", "Food", listOf("axis")),
        Triple("SBI Card Offer", "Shopping", listOf("sbi"))
    )

    /**
     * Parses an incoming SMS header + body and extracts a valid [SniffedCouponOffer] if present.
     */
    fun sniffCouponFromSms(
        sender: String?,
        body: String,
        nowMillis: Long = System.currentTimeMillis()
    ): SniffedCouponOffer? {
        val cleanBody = body.trim()
        if (cleanBody.length < 8) return null
        if (IGNORE_OTP_REGEX.containsMatchIn(cleanBody)) return null

        val lower = cleanBody.lowercase(Locale.ENGLISH)
        val hasOfferContext = listOf(
            "use ", "code", "off", "cashback", "discount", "voucher", "promo", "offer", "save "
        ).any { lower.contains(it) }
        if (!hasOfferContext) return null

        var extractedCode: String? = null
        for (pattern in EXPLICIT_CODE_PATTERNS) {
            val matches = pattern.findAll(cleanBody)
            for (match in matches) {
                val candidate = match.groupValues.getOrNull(1)?.trim()?.uppercase(Locale.ENGLISH).orEmpty()
                if (isValidCouponCandidate(candidate)) {
                    extractedCode = candidate
                    break
                }
            }
            if (extractedCode != null) break
        }

        val code = extractedCode ?: return null

        // Determine merchant and category from body + sender + code prefix
        val combinedSearch = "${sender.orEmpty()} $cleanBody $code".lowercase(Locale.ENGLISH)
        val matchedMerchant = MERCHANT_CATEGORY_HINTS.firstOrNull { (_, _, keywords) ->
            keywords.any { combinedSearch.contains(it) }
        }

        val merchantName = matchedMerchant?.first ?: inferMerchantFromCode(code, sender)
        val category = matchedMerchant?.second ?: inferCategoryFromCode(code, lower)

        // Extract estimated rupee savings or percentage
        val rupeeMatch = RUPEE_DISCOUNT_REGEX.find(cleanBody)
        val pctMatch = PERCENT_DISCOUNT_REGEX.find(cleanBody)
        val trailingDigitsInCode = Regex("""(\d{2,4})$""").find(code)?.groupValues?.getOrNull(1)?.toLongOrNull()

        val estimatedSavings = when {
            rupeeMatch != null -> rupeeMatch.groupValues.getOrNull(1)?.toLongOrNull() ?: 50L
            trailingDigitsInCode != null && trailingDigitsInCode in 10L..1000L -> trailingDigitsInCode
            pctMatch != null -> 75L
            else -> 50L
        }.coerceIn(10L, 2000L)

        val discountSummary = when {
            rupeeMatch != null -> "₹$estimatedSavings OFF"
            pctMatch != null -> "${pctMatch.groupValues[1]}% OFF (up to ₹$estimatedSavings)"
            trailingDigitsInCode != null -> "₹$trailingDigitsInCode Instant Off"
            else -> "₹$estimatedSavings Cashback"
        }

        return SniffedCouponOffer(
            code = code,
            merchant = merchantName,
            category = category,
            discountSummary = discountSummary,
            estimatedSavingsInr = estimatedSavings,
            sourceSender = sender?.ifBlank { "SMS-OFFER" } ?: "SMS-OFFER",
            rawSmsSnippet = cleanBody.take(140),
            detectedAtMillis = nowMillis
        )
    }

    private fun isValidCouponCandidate(candidate: String): Boolean {
        if (candidate.length !in 4..14) return false
        if (candidate in NON_COUPON_STOPWORDS) return false
        val hasLetter = candidate.any { it.isLetter() }
        val hasDigit = candidate.any { it.isDigit() }
        // Must have at least 2 letters and either digits or known promo suffix
        val letterCount = candidate.count { it.isLetter() }
        if (letterCount < 2) return false
        // Reject pure 10+ digit UTR/reference strings
        if (!hasLetter) return false
        // Prefer codes with digits (e.g. HDFC100, ZOMATO50) or explicit all-caps promo words >= 5 chars
        return hasDigit || candidate.length >= 6
    }

    private fun inferMerchantFromCode(code: String, sender: String?): String {
        val upper = code.uppercase(Locale.ENGLISH)
        return when {
            upper.startsWith("HDFC") -> "HDFC Bank SmartBuy"
            upper.startsWith("ZOMATO") || upper.startsWith("ZMT") -> "Zomato"
            upper.startsWith("SWIGGY") || upper.startsWith("SWG") -> "Swiggy"
            upper.startsWith("AXIS") -> "Axis Bank Dining/Shopping"
            upper.startsWith("ICICI") -> "ICICI Bank Offer"
            upper.startsWith("AMZ") || upper.startsWith("AMAZON") -> "Amazon Pay"
            upper.startsWith("FLIP") || upper.startsWith("FK") -> "Flipkart"
            upper.startsWith("UBER") || upper.startsWith("OLA") -> "Cab / Metro Offer"
            upper.startsWith("PVR") || upper.startsWith("BMS") -> "BookMyShow / PVR"
            !sender.isNullOrBlank() -> sender.uppercase(Locale.ENGLISH)
            else -> "UPI Partner Offer"
        }
    }

    private fun inferCategoryFromCode(code: String, lowerBody: String): String {
        val upper = code.uppercase(Locale.ENGLISH)
        return when {
            upper.startsWith("ZOMATO") || upper.startsWith("SWIGGY") || lowerBody.contains("food") || lowerBody.contains("dining") -> "Food"
            upper.startsWith("CHAI") || lowerBody.contains("coffee") || lowerBody.contains("snack") -> "Chai & Snacks"
            upper.startsWith("UBER") || upper.startsWith("OLA") || upper.startsWith("METRO") || lowerBody.contains("ride") -> "Transport"
            upper.startsWith("PVR") || upper.startsWith("BMS") || lowerBody.contains("movie") -> "Entertainment"
            upper.startsWith("JIO") || upper.startsWith("AIRTEL") || lowerBody.contains("bill") || lowerBody.contains("recharge") -> "Bills"
            else -> "Shopping"
        }
    }

    fun inspectAndRecordIncomingSms(
        context: Context,
        sender: String?,
        body: String
    ): SniffedCouponOffer? {
        val offer = sniffCouponFromSms(sender = sender, body = body) ?: return null
        recordDetectedCoupon(context, offer)
        return offer
    }

    fun recordDetectedCoupon(context: Context, coupon: SniffedCouponOffer) {
        val existing = getDetectedCoupons(context, includeSeededDefaults = false)
        val merged = (listOf(coupon) + existing.filterNot { it.code.equals(coupon.code, ignoreCase = true) })
            .take(25)
        saveCouponsToPrefs(context, merged)
    }

    fun getDetectedCoupons(
        context: Context,
        includeSeededDefaults: Boolean = true
    ): List<SniffedCouponOffer> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SNIFFED_COUPONS_JSON, null)
        val parsed = mutableListOf<SniffedCouponOffer>()
        if (!raw.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    parsed.add(
                        SniffedCouponOffer(
                            code = obj.optString("code"),
                            merchant = obj.optString("merchant"),
                            category = obj.optString("category", "Food"),
                            discountSummary = obj.optString("discountSummary", "₹50 OFF"),
                            estimatedSavingsInr = obj.optLong("estimatedSavingsInr", 50L),
                            sourceSender = obj.optString("sourceSender", "VM-HDFCBK"),
                            rawSmsSnippet = obj.optString("rawSmsSnippet", ""),
                            detectedAtMillis = obj.optLong("detectedAtMillis", 0L)
                        )
                    )
                }
            }
        }
        if (parsed.isNotEmpty() || !includeSeededDefaults) {
            return parsed
        }
        return DEFAULT_SEEDED_SMS_COUPONS
    }

    fun findBestMatchingCoupon(
        context: Context,
        category: String,
        merchantQuery: String = ""
    ): SniffedCouponOffer? {
        val all = getDetectedCoupons(context, includeSeededDefaults = true)
        if (all.isEmpty()) return null
        val cleanQuery = merchantQuery.trim().lowercase(Locale.ENGLISH)
        if (cleanQuery.length >= 2) {
            val merchantHit = all.firstOrNull {
                it.merchant.lowercase(Locale.ENGLISH).contains(cleanQuery) ||
                    cleanQuery.contains(it.merchant.lowercase(Locale.ENGLISH).substringBefore(" ")) ||
                    it.code.lowercase(Locale.ENGLISH).contains(cleanQuery.take(4))
            }
            if (merchantHit != null) return merchantHit
        }
        return all.firstOrNull { it.category.equals(category, ignoreCase = true) }
            ?: all.firstOrNull()
    }

    fun copyCouponToClipboard(context: Context, code: String) {
        runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("Kharchee SMS Coupon", code))
        }
    }

    private fun saveCouponsToPrefs(context: Context, list: List<SniffedCouponOffer>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("code", item.code)
                    put("merchant", item.merchant)
                    put("category", item.category)
                    put("discountSummary", item.discountSummary)
                    put("estimatedSavingsInr", item.estimatedSavingsInr)
                    put("sourceSender", item.sourceSender)
                    put("rawSmsSnippet", item.rawSmsSnippet)
                    put("detectedAtMillis", item.detectedAtMillis)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SNIFFED_COUPONS_JSON, arr.toString())
            .apply()
    }

    private val DEFAULT_SEEDED_SMS_COUPONS = listOf(
        SniffedCouponOffer(
            code = "ZOMATO50",
            merchant = "Zomato",
            category = "Food",
            discountSummary = "₹50 OFF on Food Orders",
            estimatedSavingsInr = 50L,
            sourceSender = "VM-ZOMATO",
            rawSmsSnippet = "Craving dinner? Get flat Rs.50 OFF on your next order. Use ZOMATO50 via UPI!"
        ),
        SniffedCouponOffer(
            code = "HDFC100",
            merchant = "HDFC Bank SmartBuy",
            category = "Shopping",
            discountSummary = "₹100 Instant Cashback",
            estimatedSavingsInr = 100L,
            sourceSender = "AD-HDFCBK",
            rawSmsSnippet = "HDFC Bank Alert: Unlock Rs.100 instant cashback on Shopping & Electronics. Use HDFC100 at checkout."
        ),
        SniffedCouponOffer(
            code = "CHAI20",
            merchant = "Chaayos / Cafe",
            category = "Chai & Snacks",
            discountSummary = "₹20 Cashback",
            estimatedSavingsInr = 20L,
            sourceSender = "VM-PAYTMB",
            rawSmsSnippet = "Evening Chai treat! Use CHAI20 on UPI QR scan to get Rs.20 cashback."
        ),
        SniffedCouponOffer(
            code = "METRO25",
            merchant = "Uber / Metro Pass",
            category = "Transport",
            discountSummary = "₹25 OFF Commute",
            estimatedSavingsInr = 25L,
            sourceSender = "AX-AXISBK",
            rawSmsSnippet = "Commute smarter! Use code METRO25 on Metro & Auto UPI payments for Rs.25 off."
        ),
        SniffedCouponOffer(
            code = "PVR120",
            merchant = "PVR / BookMyShow",
            category = "Entertainment",
            discountSummary = "₹120 OFF Movie & OTT",
            estimatedSavingsInr = 120L,
            sourceSender = "JD-ICICIB",
            rawSmsSnippet = "Weekend movie night! Apply code PVR120 with ICICI UPI for flat Rs.120 off."
        ),
        SniffedCouponOffer(
            code = "JIO75",
            merchant = "JioFiber / Bills",
            category = "Bills",
            discountSummary = "₹75 Bill Cashback",
            estimatedSavingsInr = 75L,
            sourceSender = "JX-JIOFBR",
            rawSmsSnippet = "Pay your broadband or electricity bill today: Use JIO75 to claim Rs.75 cashback."
        )
    )
}

@Composable
fun ContextualCouponSnifferPill(
    coupon: SniffedCouponOffer,
    isApplied: Boolean,
    onApplyCoupon: (SniffedCouponOffer) -> Unit,
    modifier: Modifier = Modifier
) {
    val emerald = Color(0xFF00E676)
    val gold = Color(0xFFFFD700)
    Surface(
        color = if (isApplied) Color(0xFF06291A) else Color(0xFF141E18),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isApplied) emerald else gold.copy(alpha = 0.78f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onApplyCoupon(coupon) }
            .testTag("sms_coupon_sniffer_pill")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.LocalOffer,
                    contentDescription = "SMS Coupon Detected",
                    tint = if (isApplied) emerald else gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isApplied) {
                            "✓ Coupon ${coupon.code} Applied (-₹${coupon.estimatedSavingsInr} saved!)"
                        } else {
                            "💡 Coupon detected from SMS: ${coupon.code} — Tap to apply!"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isApplied) emerald else Color(0xFFF9FAFB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${coupon.merchant} (${coupon.sourceSender}) · ${coupon.discountSummary}",
                        fontSize = 10.sp,
                        color = if (isApplied) Color(0xFF6EE7B7) else gold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
