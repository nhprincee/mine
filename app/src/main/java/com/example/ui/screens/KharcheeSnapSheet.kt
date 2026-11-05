package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PAYMENT_MODES
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.json.JSONObject

data class SnapExtractedReceipt(
    val isValidReceipt: Boolean,
    val clarityWarning: String,
    val merchantName: String,
    val totalAmount: Long,
    val taxAmount: Long,
    val currency: String,
    val transactionDate: String,
    val suggestedCategory: String,
    val itemsSummary: String
)

fun detectDuplicateReceipt(
    existingExpenses: List<ExpenseRecord>,
    amount: Long,
    merchantName: String,
    nowEpochMillis: Long = System.currentTimeMillis()
): ExpenseRecord? {
    if (amount <= 0L) return null
    val normalizedMerchant = merchantName.trim().lowercase()
    return existingExpenses.firstOrNull { tx ->
        val sameAmount = tx.amount == amount
        val within48Hours = abs(nowEpochMillis - tx.txEpochMillis) <= 48L * 3600_000L
        val titleLower = tx.title.trim().lowercase()
        val sameMerchant = normalizedMerchant.isNotBlank() &&
            (titleLower.contains(normalizedMerchant) || normalizedMerchant.contains(titleLower))
        sameAmount && (sameMerchant || within48Hours)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KharcheeSnapBottomSheet(
    userId: String,
    existingExpenses: List<ExpenseRecord>,
    onDismiss: () -> Unit,
    onConfirmSaveReceiptExpense: (
        title: String,
        category: String,
        amount: Long,
        paymentMode: String,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val headingFont = LocalAppFontFamily.current

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rawReceiptText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }
    var isInvalidOrUnclearPhoto by remember { mutableStateOf(false) }

    // Editable Extracted Fields (requiring explicit user confirmation before saving)
    val todayFormatted = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    }
    var merchantName by remember { mutableStateOf("") }
    var totalAmountStr by remember { mutableStateOf("") }
    var taxAmountStr by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf("INR (₹)") }
    var transactionDate by remember { mutableStateOf(todayFormatted) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedPaymentMode by remember { mutableStateOf("UPI") }
    var extractedItemsSummary by remember { mutableStateOf("") }
    var hasExtractedCandidate by remember { mutableStateOf(false) }

    val parsedAmount = totalAmountStr.trim().toLongOrNull() ?: 0L
    val duplicateMatch = remember(existingExpenses, parsedAmount, merchantName) {
        detectDuplicateReceipt(existingExpenses, parsedAmount, merchantName)
    }

    fun runGeminiVisionReceiptExtraction(bitmap: Bitmap?, textHint: String) {
        isScanning = true
        scanStatusMessage = "KHARCHEE SNAP AI analyzing receipt (Hindi / English OCR)..."
        isInvalidOrUnclearPhoto = false

        scope.launch {
            try {
                val receiptSchema = Schema.obj(
                    mapOf(
                        "isValidReceipt" to Schema.boolean(),
                        "clarityWarning" to Schema.string(),
                        "merchantName" to Schema.string(),
                        "totalAmount" to Schema.long(),
                        "taxAmount" to Schema.long(),
                        "currency" to Schema.string(),
                        "transactionDate" to Schema.string(),
                        "suggestedCategory" to Schema.string(),
                        "itemsSummary" to Schema.string()
                    )
                )
                val model = Firebase.ai.generativeModel(
                    modelName = "gemini-3.8-flash",
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = receiptSchema
                    }
                )

                val promptText = """
                    You are KHARCHEE SNAP, an accurate Indian financial receipt & bill OCR parser supporting English, Hindi (Devanagari), and Hinglish receipts (restaurant bills, grocery receipts, UPI screenshots, medical bills, fuel slips, tax invoices).
                    CRITICAL RULES:
                    1. Never invent or guess a total amount if the image is a random object, selfie, landscape, or too blurry to read the total amount. In that case, set isValidReceipt = false, totalAmount = 0, and explain why in clarityWarning.
                    2. If it is a genuine receipt/bill/payment screenshot, set isValidReceipt = true, extract merchantName, totalAmount (integer rupees), taxAmount (GST/CGST/SGST integer rupees or 0), currency (default "INR"), transactionDate, suggestedCategory (one of: Food, Chai & Snacks, Transport, Grocery, Entertainment, Bills, Shopping, Health), and itemsSummary (comma-separated list of purchased items).
                    Additional text context provided by user: $textHint
                """.trimIndent()

                val response = if (bitmap != null) {
                    model.generateContent(
                        content {
                            image(bitmap)
                            text(promptText)
                        }
                    )
                } else {
                    model.generateContent(promptText)
                }

                val jsonText = response.text?.trim().orEmpty()
                val obj = JSONObject(jsonText)
                val valid = obj.optBoolean("isValidReceipt", false)
                val warning = obj.optString("clarityWarning", "")
                val merch = obj.optString("merchantName", "").trim()
                val amt = obj.optLong("totalAmount", 0L)
                val tax = obj.optLong("taxAmount", 0L)
                val curr = obj.optString("currency", "INR").ifBlank { "INR" }
                val dt = obj.optString("transactionDate", todayFormatted).ifBlank { todayFormatted }
                val cat = obj.optString("suggestedCategory", "Food")
                val items = obj.optString("itemsSummary", "")

                hasExtractedCandidate = true
                if (!valid || amt <= 0L) {
                    isInvalidOrUnclearPhoto = true
                    totalAmountStr = "" // Never invent an amount when unclear!
                    merchantName = merch
                    scanStatusMessage = warning.ifBlank {
                        "Receipt amount is unclear or this photo does not appear to be a valid bill. Please verify and enter the exact amount manually."
                    }
                } else {
                    isInvalidOrUnclearPhoto = false
                    merchantName = merch.ifBlank { "Receipt Merchant" }
                    totalAmountStr = amt.toString()
                    taxAmountStr = if (tax > 0L) tax.toString() else "0"
                    currencyCode = curr
                    transactionDate = dt
                    if (cat in EXPENSE_CATEGORIES) {
                        selectedCategory = cat
                    }
                    extractedItemsSummary = items
                    scanStatusMessage = "✓ Receipt extracted! Review fields below and tap Confirm & Save."
                }
            } catch (e: Exception) {
                // Fallback to deterministic local parser if text was provided, never inventing amounts for image-only
                val localParsed = if (textHint.isNotBlank()) parseSpokenExpensePhrase(textHint) else null
                hasExtractedCandidate = true
                if (localParsed != null && localParsed.second > 0L) {
                    isInvalidOrUnclearPhoto = false
                    merchantName = localParsed.first
                    totalAmountStr = localParsed.second.toString()
                    selectedCategory = localParsed.third
                    extractedItemsSummary = "Parsed from receipt text"
                    scanStatusMessage = "✓ Extracted from receipt text. Please review and confirm below."
                } else {
                    isInvalidOrUnclearPhoto = true
                    totalAmountStr = ""
                    scanStatusMessage = "Could not automatically read a clear receipt total. Please enter or verify the details below before saving."
                }
            } finally {
                isScanning = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            runGeminiVisionReceiptExtraction(bitmap, rawReceiptText)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
            if (bmp != null) {
                capturedBitmap = bmp
                runGeminiVisionReceiptExtraction(bmp, rawReceiptText)
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SophisticatedEmerald,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = SophisticatedEmerald,
        unfocusedLabelColor = MutedIvory
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("kharchee_snap_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SophisticatedEmerald.copy(alpha = 0.15f))
                            .border(1.dp, SophisticatedEmerald, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "KHARCHEE SNAP",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "KHARCHEE SNAP · SNAP & TRACK",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.3.sp,
                            color = SophisticatedEmerald
                        )
                        Text(
                            text = "Snap it. Track it. Done.",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close KHARCHEE SNAP", tint = MutedPlatinum)
                }
            }

            Text(
                text = "Capture a paper bill with your camera, upload a receipt image from your gallery, or paste a UPI/SMS receipt in English or Hindi.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedPlatinum
            )

            // Camera Capture & Gallery Upload Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedEmerald,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("snap_camera_capture_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Capture", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera Snap", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("snap_gallery_upload_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery Upload", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Receipt", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            // Optional Receipt Preview if captured
            val previewBmp = capturedBitmap
            if (previewBmp != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = previewBmp.asImageBitmap(),
                        contentDescription = "Captured Receipt Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        color = ObsidianBlack.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "RECEIPT ATTACHED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = SophisticatedEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Optional Text / Hindi-English Bill Text Paste
            OutlinedTextField(
                value = rawReceiptText,
                onValueChange = { rawReceiptText = it },
                label = { Text("Or Paste Receipt / Bill / UPI Text (Hindi or English)") },
                placeholder = { Text("e.g., Haldiram's Total ₹480 GST ₹24 or पेटीएम से ₹350 किराना स्टोर को भुगतान") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snap_text_input")
            )

            if (rawReceiptText.isNotBlank()) {
                OutlinedButton(
                    onClick = { runGeminiVisionReceiptExtraction(capturedBitmap, rawReceiptText) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, SophisticatedEmerald),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snap_analyze_text_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Receipt Details with AI", fontWeight = FontWeight.SemiBold)
                }
            }

            if (isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, SophisticatedEmerald, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = SophisticatedEmerald,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = scanStatusMessage ?: "Extracting merchant, items, tax & total...",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory
                    )
                }
            }

            if (!isScanning && scanStatusMessage != null) {
                val statusColor = if (isInvalidOrUnclearPhoto) CrimsonRuby else SophisticatedEmerald
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(0.8.dp, statusColor.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("snap_status_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isInvalidOrUnclearPhoto) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = scanStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory,
                        fontSize = 12.sp
                    )
                }
            }

            // Duplicate Receipt Detection Warning Banner
            if (duplicateMatch != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ChampagneGold.copy(alpha = 0.14f))
                        .border(1.dp, ChampagneGold, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("snap_duplicate_warning_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Duplicate Receipt Warning",
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "POSSIBLE DUPLICATE RECEIPT DETECTED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChampagneGold
                        )
                        Text(
                            text = "A similar transaction '${duplicateMatch.title}' for ₹${duplicateMatch.amount} was already logged recently. Please verify before saving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmIvory,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // Editable Receipt Fields (User can always edit before confirming)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFY & EDIT EXTRACTED RECEIPT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = ChampagneGold
                )
                Text(
                    text = currencyCode,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = MetallicSilver
                )
            }

            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text("Merchant / Store Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snap_merchant_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = { totalAmountStr = it },
                    label = { Text("Total Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("snap_total_amount_input")
                )

                OutlinedTextField(
                    value = taxAmountStr,
                    onValueChange = { taxAmountStr = it },
                    label = { Text("Tax / GST (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("snap_tax_amount_input")
                )
            }

            OutlinedTextField(
                value = transactionDate,
                onValueChange = { transactionDate = it },
                label = { Text("Receipt Date") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snap_date_input")
            )

            OutlinedTextField(
                value = extractedItemsSummary,
                onValueChange = { extractedItemsSummary = it },
                label = { Text("Individual Purchased Items") },
                placeholder = { Text("e.g., 2x Masala Dosa, 1x Filter Coffee") },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snap_items_input")
            )

            // Suggested Category Selector
            Text(
                text = "SUGGESTED CATEGORY",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MetallicSilver
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EXPENSE_CATEGORIES.chunked(4).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCats.forEach { cat ->
                            val selected = selectedCategory == cat
                            Surface(
                                color = if (selected) SophisticatedEmerald.copy(alpha = 0.18f) else ObsidianBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        0.8.dp,
                                        if (selected) SophisticatedEmerald else HairlinePlatinum,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (selected) SophisticatedEmerald else MutedPlatinum,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Method Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (PAYMENT_MODES + listOf("Bank")).forEach { mode ->
                    val selected = selectedPaymentMode == mode
                    Surface(
                        color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                0.8.dp,
                                if (selected) ChampagneGold else HairlinePlatinum,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedPaymentMode = mode }
                    ) {
                        Text(
                            text = mode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) ChampagneGold else MutedPlatinum,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                        )
                    }
                }
            }

            // Explicit User Confirmation Button
            Button(
                onClick = {
                    val finalAmount = totalAmountStr.trim().toLongOrNull() ?: 0L
                    if (finalAmount <= 0L) {
                        scanStatusMessage = "Please enter a valid receipt amount (> ₹0) before confirming."
                        isInvalidOrUnclearPhoto = true
                        return@Button
                    }
                    val finalMerchant = merchantName.trim().ifBlank { "$selectedCategory Receipt" }
                    val taxPart = taxAmountStr.trim().toLongOrNull()?.takeIf { it > 0L }?.let { " · Tax: ₹$it" } ?: ""
                    val itemsPart = extractedItemsSummary.trim().takeIf { it.isNotBlank() }?.let { " · Items: $it" } ?: ""
                    val noteWithAttachment = "[SNAP_RECEIPT] Date: $transactionDate$taxPart$itemsPart"

                    KharcheeProfileAndToolsStore.incrementSnapScanCount(context, userId)
                    onConfirmSaveReceiptExpense(
                        finalMerchant,
                        selectedCategory,
                        finalAmount,
                        selectedPaymentMode,
                        noteWithAttachment
                    )
                    onDismiss()
                },
                enabled = parsedAmount > 0L,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedEmerald,
                    contentColor = ObsidianBlack,
                    disabledContainerColor = ElevatedGraphite,
                    disabledContentColor = MutedIvory
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("snap_confirm_save_button")
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (parsedAmount > 0L) "Confirm & Save ₹$parsedAmount to Passbook" else "Enter Valid Amount to Confirm",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
