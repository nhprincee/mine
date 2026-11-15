package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.KhataContactComputedState
import com.example.data.model.KhataDirection
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 1-Tap Viral WhatsApp Split & Khata Reminder Card Generator.
 *
 * Generates a crisp, shareable dark-mode graphic card (`Bitmap`) showing:
 * - "Kharchee Split Receipt" header
 * - Exact breakdown of who owes what (₹Amount, Original Recorded, Paid, Net Due)
 * - User's UPI ID & scannable-style UPI QR code matrix for instant payment
 * - Subtle "Settled via Kharchee" branding footer
 * Shares directly via `Intent.ACTION_SEND` to WhatsApp / Instagram.
 */
enum class VasooliReminderTone(
    val id: String,
    val badgeTitle: String,
    val subtitle: String
) {
    POLITE(
        id = "POLITE",
        badgeTitle = "😇 Polite",
        subtitle = "Friendly casual reminder with exact ₹ amount & UPI ID"
    ),
    BHAICHARA_SAVAGE(
        id = "BHAICHARA_SAVAGE",
        badgeTitle = "😂 Bhaichara / Savage",
        subtitle = "Witty Hinglish meme reminder (Jarvis gala daba raha hai 💀)"
    ),
    JARVIS_OFFICIAL(
        id = "JARVIS_OFFICIAL",
        badgeTitle = "🤖 Jarvis Official Notice",
        subtitle = "Formal automated AI CFO ledger statement & UPI link"
    )
}

object KhataSplitCardGenerator {

    private const val PREFS_SPLIT_CARD = "kharchee_split_card_vip_prefs"
    private const val KEY_GOLD_VIP_THEME = "sovereign_gold_vip_receipt_enabled"

    fun isSovereignGoldVipThemeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_SPLIT_CARD, Context.MODE_PRIVATE)
            .getBoolean(KEY_GOLD_VIP_THEME, true)
    }

    fun setSovereignGoldVipThemeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_SPLIT_CARD, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_GOLD_VIP_THEME, enabled)
            .apply()
    }

    fun isDeviceSovereignProActive(context: Context): Boolean {
        return SovereignActivationManager.getSavedVerifiedActivationCode(context).isNotBlank()
    }

    fun buildVasooliToneMessage(
        tone: VasooliReminderTone,
        friendName: String,
        amountInr: Long,
        itemSummary: String,
        userDisplayName: String,
        userUpiId: String
    ): String {
        val cleanFriend = friendName.trim().ifBlank { "Dost" }
        val cleanSender = userDisplayName.trim().ifBlank { "Kharchee Member" }
        val cleanItem = itemSummary.trim().ifBlank { "Shared Split / Udhaar" }
        val formattedAmt = PerformancePreferences.preFormatInr(amountInr.coerceAtLeast(0L))
        val resolvedUpi = PaymentUtil.resolvePayeeVpa(userUpiId.trim(), cleanSender)
        val upiDeepLink = if (resolvedUpi.isNotBlank()) {
            "upi://pay?pa=$resolvedUpi&pn=${Uri.encode(cleanSender)}&am=${amountInr.coerceAtLeast(0L)}&tn=${Uri.encode("Kharchee Split: $cleanItem")}&cu=INR"
        } else {
            ""
        }

        return when (tone) {
            VasooliReminderTone.POLITE -> buildString {
                appendLine("Hey *$cleanFriend*! 😊")
                appendLine("Just a quick friendly reminder for our *$cleanItem* split of *$formattedAmt*.")
                if (resolvedUpi.isNotBlank()) {
                    appendLine("• *UPI ID:* `$resolvedUpi`")
                    appendLine("• *1-Tap Pay:* $upiDeepLink")
                } else {
                    appendLine("• Ping me on GPay / PhonePe whenever convenient!")
                }
                append("🧾 _Kharchee Split Receipt attached_")
            }

            VasooliReminderTone.BHAICHARA_SAVAGE -> buildString {
                appendLine("Bhai *$cleanFriend*, *$formattedAmt* bhej de, Jarvis mera gala daba raha hai 💀🚨")
                appendLine("• *Hisaab:* $cleanItem ($formattedAmt pending)")
                if (resolvedUpi.isNotBlank()) {
                    appendLine("• *Turant UPI Maar:* `$resolvedUpi`")
                    appendLine("• *1-Tap Link:* $upiDeepLink")
                }
                append("😂 _Dosti apni jagah, Kharchee ledger apni jagah!_")
            }

            VasooliReminderTone.JARVIS_OFFICIAL -> buildString {
                appendLine("🤖 *JARVIS AI CFO — OFFICIAL LEDGER NOTICE*")
                appendLine("• *Account Holder:* $cleanFriend")
                appendLine("• *Creditor:* $cleanSender")
                appendLine("• *Outstanding Principal:* $formattedAmt")
                appendLine("• *Particulars:* $cleanItem")
                if (resolvedUpi.isNotBlank()) {
                    appendLine("• *Designated Settlement VPA:* `$resolvedUpi`")
                    appendLine("• *Instant Settlement URI:* $upiDeepLink")
                }
                append("⚡ _Automated Dispatch via Kharchee Sovereign Ledger_")
            }
        }
    }

    fun shareSplitReceiptCardOnWhatsApp(
        context: Context,
        contactState: KhataContactComputedState,
        userDisplayName: String = "Kharchee Member",
        userUpiId: String = "",
        specificEntryDescription: String? = null,
        specificEntryAmount: Long? = null,
        customCaptionText: String? = null,
        forceSovereignGoldVip: Boolean? = null,
        onStatusToast: (String) -> Unit = {}
    ) {
        try {
            val effectiveAmount = specificEntryAmount ?: contactState.outstandingAmount.coerceAtLeast(0L)
            val resolvedUpiId = PaymentUtil.resolvePayeeVpa(
                rawPhoneOrVpa = userUpiId.trim(),
                payeeName = userDisplayName
            )
            val effectiveItemSummary = specificEntryDescription
                ?: contactState.activeEntries.firstOrNull()?.description?.ifBlank { "Shared Split / Udhaar" }
                ?: "Shared Split / Udhaar"

            val useGoldVip = forceSovereignGoldVip
                ?: (isDeviceSovereignProActive(context) && isSovereignGoldVipThemeEnabled(context))

            val bitmap = renderDarkSplitReceiptBitmap(
                friendName = contactState.contact.name,
                friendPhone = contactState.contact.phone,
                outstandingAmount = effectiveAmount,
                originalRecorded = contactState.totalOriginalRecordedForActiveDirection.coerceAtLeast(effectiveAmount),
                alreadyPaid = contactState.totalPaidOrReceivedForActiveDirection,
                direction = contactState.netDirection ?: KhataDirection.LENA_HAI,
                itemSummary = effectiveItemSummary,
                userDisplayName = userDisplayName.ifBlank { "Kharchee Member" },
                userUpiId = resolvedUpiId.ifBlank { "Add your UPI ID in Kharchee Profile" },
                isSovereignGoldVip = useGoldVip
            )

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val safeName = contactState.contact.name.replace(Regex("[^a-zA-Z0-9]+"), "_").lowercase(Locale.ENGLISH)
            val imageFile = File(reportsDir, "kharchee_split_receipt_${safeName}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val formattedAmt = PerformancePreferences.preFormatInr(effectiveAmount)
            val upiDeepLink = "upi://pay?pa=$resolvedUpiId&pn=${Uri.encode(userDisplayName.ifBlank { "Kharchee" })}&am=$effectiveAmount&tn=${Uri.encode("Kharchee Split Settlement")}&cu=INR"
            val shareCaption = customCaptionText?.takeIf { it.isNotBlank() } ?: buildString {
                if (useGoldVip) {
                    appendLine("👑 *Verified Kharchee Sovereign — 24K Gold VIP Split Receipt*")
                } else {
                    appendLine("🧾 *Kharchee Split Receipt*")
                }
                appendLine("Hey *${contactState.contact.name}*, here is our split summary:")
                appendLine("• *Pending Due (Lena Hai):* $formattedAmt")
                appendLine("• *Pay Instantly via UPI ID:* `$resolvedUpiId`")
                appendLine("• *1-Tap UPI Link:* $upiDeepLink")
                append(if (useGoldVip) "👑 _Verified Kharchee Sovereign_" else "✨ _Settled via Kharchee_")
            }

            // Try targeting WhatsApp directly if installed, else show universal share chooser (WhatsApp / Instagram / Telegram)
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareCaption)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pm = context.packageManager
            if (whatsappIntent.resolveActivity(pm) != null) {
                context.startActivity(whatsappIntent)
                onStatusToast("📲 Sharing Kharchee Split Receipt to WhatsApp...")
            } else {
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, shareCaption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(chooserIntent, "Share Split Card on WhatsApp / Instagram")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                onStatusToast("🧾 Split Receipt Card ready to share!")
            }
        } catch (e: Exception) {
            onStatusToast("⚠️ Could not share split card: ${e.localizedMessage ?: "Error"}")
        }
    }

    fun renderDarkSplitReceiptBitmap(
        friendName: String,
        friendPhone: String,
        outstandingAmount: Long,
        originalRecorded: Long,
        alreadyPaid: Long,
        direction: KhataDirection,
        itemSummary: String,
        userDisplayName: String,
        userUpiId: String,
        isSovereignGoldVip: Boolean = false
    ): Bitmap {
        val width = 1080
        val height = 1360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Deep Obsidian or 24K Gold Luxury Canvas Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                if (isSovereignGoldVip) {
                    intArrayOf(
                        Color.parseColor("#090703"),
                        Color.parseColor("#1C1507"),
                        Color.parseColor("#090703")
                    )
                } else {
                    intArrayOf(
                        Color.parseColor("#07080D"),
                        Color.parseColor("#110E20"),
                        Color.parseColor("#080A10")
                    )
                },
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Main Receipt Card Container
        val cardRect = RectF(56f, 56f, width - 56f, height - 56f)
        val cardSurfacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#141008") else Color.parseColor("#121521")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardSurfacePaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            if (isSovereignGoldVip) {
                shader = LinearGradient(
                    56f, 56f, width - 56f, height - 56f,
                    intArrayOf(
                        Color.parseColor("#FFE082"),
                        Color.parseColor("#D4AF37"),
                        Color.parseColor("#FFB300"),
                        Color.parseColor("#FFF8E1")
                    ),
                    floatArrayOf(0f, 0.35f, 0.7f, 1f),
                    Shader.TileMode.CLAMP
                )
                strokeWidth = 6.5f
            } else {
                color = Color.parseColor("#2EE59D")
                alpha = 180
                strokeWidth = 3.5f
            }
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardBorderPaint)

        // 3. Top Header Badge: "KHARCHEE SPLIT RECEIPT" or 24K Gold VIP Crest
        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val sansBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val sansRegular = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val headerTagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFD700") else Color.parseColor("#D4AF37")
            textSize = 26f
            typeface = monoBold
            letterSpacing = 0.14f
        }
        canvas.drawText(
            if (isSovereignGoldVip) "👑 24K GOLD VIP SPLIT RECEIPT" else "⚡ KHARCHEE SPLIT RECEIPT",
            112f,
            146f,
            headerTagPaint
        )

        val dateStr = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.ENGLISH).format(Date())
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 24f
            typeface = sansRegular
        }
        canvas.drawText("Issued by $userDisplayName  •  $dateStr", 112f, 192f, subHeaderPaint)

        // Divider line
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#5C4612") else Color.parseColor("#2B2640")
            strokeWidth = 2f
        }
        canvas.drawLine(112f, 230f, width - 112f, 230f, dividerPaint)

        // 4. Who Owes What Hero Section
        val directionLabel = if (direction == KhataDirection.LENA_HAI) {
            "LENA HAI  •  DUE FROM ${friendName.uppercase(Locale.ENGLISH)}"
        } else {
            "SPLIT SUMMARY  •  ${friendName.uppercase(Locale.ENGLISH)}"
        }
        val pillLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFD700") else Color.parseColor("#2EE59D")
            textSize = 25f
            typeface = monoBold
        }
        canvas.drawText(directionLabel, 112f, 294f, pillLabelPaint)

        // Big ₹Amount
        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFF8E1") else Color.parseColor("#FFFFFF")
            textSize = 104f
            typeface = sansBold
        }
        val formattedOutstanding = PerformancePreferences.preFormatInr(outstandingAmount)
        canvas.drawText(formattedOutstanding, 112f, 416f, amountPaint)

        // Note / Split Description
        val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFE082") else Color.parseColor("#D8B4FE")
            textSize = 30f
            typeface = sansBold
        }
        val cleanItem = if (itemSummary.length > 42) itemSummary.take(39) + "..." else itemSummary
        canvas.drawText("For: $cleanItem", 112f, 474f, itemPaint)

        // 5. Breakdown Box (Total Recorded vs Paid vs Net Balance)
        val breakdownRect = RectF(112f, 520f, width - 112f, 740f)
        val breakdownBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#211A0A") else Color.parseColor("#1A1730")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(breakdownRect, 28f, 28f, breakdownBgPaint)

        val rowLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFE082") else Color.parseColor("#A78BFA")
            textSize = 26f
            typeface = sansRegular
        }
        val rowValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            textSize = 28f
            typeface = monoBold
            textAlign = Paint.Align.RIGHT
        }

        // Row 1: Friend Name & Phone
        canvas.drawText("Split With", 152f, 578f, rowLabelPaint)
        val friendDisplay = if (friendPhone.isNotBlank()) "$friendName ($friendPhone)" else friendName
        canvas.drawText(friendDisplay, width - 152f, 578f, rowValuePaint)

        // Row 2: Total Recorded & Already Settled
        canvas.drawText("Total Split / Recorded", 152f, 636f, rowLabelPaint)
        canvas.drawText(PerformancePreferences.preFormatInr(originalRecorded), width - 152f, 636f, rowValuePaint)

        // Row 3: Already Paid / Net Payable
        canvas.drawText("Already Settled / Paid", 152f, 694f, rowLabelPaint)
        val paidValuePaint = Paint(rowValuePaint).apply { color = Color.parseColor("#2EE59D") }
        canvas.drawText(PerformancePreferences.preFormatInr(alreadyPaid), width - 152f, 694f, paidValuePaint)

        // 6. Instant UPI ID + QR Code Section
        val upiBoxRect = RectF(112f, 780f, width - 112f, 1140f)
        val upiBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#191306") else Color.parseColor("#0D131F")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(upiBoxRect, 32f, 32f, upiBoxPaint)

        val upiBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFD700") else Color.parseColor("#D4AF37")
            alpha = if (isSovereignGoldVip) 230 else 150
            style = Paint.Style.STROKE
            strokeWidth = if (isSovereignGoldVip) 3.5f else 2f
        }
        canvas.drawRoundRect(upiBoxRect, 32f, 32f, upiBorderPaint)

        // Draw deterministic QR code graphic on the left of the UPI box
        val qrSize = 260f
        val qrLeft = 152f
        val qrTop = 830f
        drawStylizedUpiQrMatrix(
            canvas = canvas,
            left = qrLeft,
            top = qrTop,
            size = qrSize,
            seedPayload = "upi://pay?pa=$userUpiId&am=$outstandingAmount"
        )

        // Right side of UPI Box: Pay via UPI details
        val upiTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFD700") else Color.parseColor("#D4AF37")
            textSize = 24f
            typeface = monoBold
        }
        canvas.drawText("INSTANT UPI SETTLEMENT", 452f, 868f, upiTitlePaint)

        val upiHandlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 34f
            typeface = monoBold
        }
        canvas.drawText(userUpiId, 452f, 928f, upiHandlePaint)

        val upiSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 24f
            typeface = sansRegular
        }
        canvas.drawText("Payee: $userDisplayName", 452f, 982f, upiSubPaint)
        canvas.drawText("Apps: GPay • PhonePe • Paytm • BHIM", 452f, 1028f, upiSubPaint)

        val scanHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2EE59D")
            textSize = 23f
            typeface = monoBold
        }
        canvas.drawText("✓ Scan QR or Copy UPI ID to Settle", 452f, 1082f, scanHintPaint)

        // 7. Branding / Watermark Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSovereignGoldVip) Color.parseColor("#FFD700") else Color.parseColor("#D4AF37")
            textSize = if (isSovereignGoldVip) 26f else 24f
            typeface = monoBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isSovereignGoldVip) {
                "👑 Verified Kharchee Sovereign  •  24K Gold VIP Ledger"
            } else {
                "Settled via Kharchee  •  Sovereign Personal Finance & Khata"
            },
            width / 2f,
            1238f,
            footerPaint
        )

        return bitmap
    }

    private fun drawStylizedUpiQrMatrix(
        canvas: Canvas,
        left: Float,
        top: Float,
        size: Float,
        seedPayload: String
    ) {
        val quietZone = 16f
        val bgRect = RectF(left, top, left + size, top + size)
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bgRect, 20f, 20f, whitePaint)

        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0B0D14")
            style = Paint.Style.FILL
        }

        val gridCount = 21
        val innerSize = size - quietZone * 2f
        val cell = innerSize / gridCount
        val startX = left + quietZone
        val startY = top + quietZone

        fun isFinderPattern(r: Int, c: Int): Boolean {
            val inTopLeft = r in 0..6 && c in 0..6
            val inTopRight = r in 0..6 && c in (gridCount - 7) until gridCount
            val inBottomLeft = r in (gridCount - 7) until gridCount && c in 0..6
            return inTopLeft || inTopRight || inBottomLeft
        }

        fun drawFinder(r0: Int, c0: Int) {
            for (dr in 0..6) {
                for (dc in 0..6) {
                    val isBorder = dr == 0 || dr == 6 || dc == 0 || dc == 6
                    val isCenter = dr in 2..4 && dc in 2..4
                    if (isBorder || isCenter) {
                        val cx = startX + (c0 + dc) * cell
                        val cy = startY + (r0 + dr) * cell
                        canvas.drawRect(cx, cy, cx + cell, cy + cell, darkPaint)
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, gridCount - 7)
        drawFinder(gridCount - 7, 0)

        val hash = abs(seedPayload.hashCode())
        for (r in 0 until gridCount) {
            for (c in 0 until gridCount) {
                if (isFinderPattern(r, c)) continue
                val bit = ((hash xor (r * 37 + c * 101 + r * c * 17)) and 1) == 0
                if (bit) {
                    val cx = startX + c * cell
                    val cy = startY + r * cell
                    canvas.drawRect(cx, cy, cx + cell, cy + cell, darkPaint)
                }
            }
        }
    }
}
