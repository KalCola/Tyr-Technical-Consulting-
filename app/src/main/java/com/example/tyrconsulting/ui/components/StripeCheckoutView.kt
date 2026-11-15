package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tyrconsulting.model.ClientQolManager
import com.example.tyrconsulting.model.GrowthMarketingManager
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.StripePaymentManager
import com.example.tyrconsulting.model.StripePaymentMethodType
import com.example.tyrconsulting.model.StripePaymentSplitOption
import com.example.tyrconsulting.model.StripeTransactionRecord
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StripeCheckoutSheetDialog(
    servicesToCheckout: List<ServiceItem>,
    onDismiss: () -> Unit,
    onPaymentCompleted: (StripeTransactionRecord) -> Unit,
    onOpenReceiptsVault: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedProfile = remember { ClientQolManager.getSavedProfile(context) }

    var clientName by remember { mutableStateOf(savedProfile.clientName.ifBlank { "Valued TYR Client" }) }
    var businessName by remember { mutableStateOf(savedProfile.businessName.ifBlank { "Independent Operator" }) }
    var clientEmail by remember { mutableStateOf(savedProfile.email.ifBlank { "client@tyrprotocol.pro" }) }
    var selectedMethod by remember { mutableStateOf(StripePaymentMethodType.CARD_TOKEN) }
    var ephemeralLast4 by remember { mutableStateOf("4242") }
    var ephemeralExpiry by remember { mutableStateOf("08/29") }
    var ephemeralZip by remember { mutableStateOf("35592") }
    var pciConsentChecked by remember { mutableStateOf(true) }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var completedReceipt by remember { mutableStateOf<StripeTransactionRecord?>(null) }

    var selectedSplitOption by remember { mutableStateOf(StripePaymentSplitOption.FULL_100) }
    var customSplitPercent by remember { mutableIntStateOf(50) }
    val activeSplitPercent = if (selectedSplitOption == StripePaymentSplitOption.CUSTOM_SPLIT) {
        customSplitPercent.coerceIn(10, 100)
    } else {
        selectedSplitOption.defaultPercent
    }

    val activeRefCode = remember { GrowthMarketingManager.getActiveInboundRefCode(context) }
    val rawOneTimeCents = remember(servicesToCheckout) {
        servicesToCheckout
            .filter { !StripePaymentManager.isMonthlyRecurringService(it) }
            .sumOf { StripePaymentManager.parseServicePriceToCents(it.price) }
    }
    val splitOneTimeCents = remember(rawOneTimeCents, activeSplitPercent) {
        if (rawOneTimeCents > 0L) {
            StripePaymentManager.applySplitToCents(rawOneTimeCents, activeSplitPercent)
        } else {
            0L
        }
    }
    val refDiscountCents = remember(splitOneTimeCents, activeRefCode) {
        GrowthMarketingManager.applyInboundReferralDiscountCents(splitOneTimeCents, activeRefCode)
    }
    val oneTimeCents = (splitOneTimeCents - refDiscountCents).coerceAtLeast(0L)
    val remainingMilestoneCents = (rawOneTimeCents - splitOneTimeCents).coerceAtLeast(0L)
    val monthlyCents = remember(servicesToCheckout) {
        servicesToCheckout
            .filter { StripePaymentManager.isMonthlyRecurringService(it) }
            .sumOf { StripePaymentManager.parseServicePriceToCents(it.price) }
    }
    val totalTodayCents = oneTimeCents + monthlyCents

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = BgSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFF635BFF)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("stripe_checkout_sheet_dialog")
        ) {
            if (completedReceipt != null) {
                val receipt = completedReceipt!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "STRIPE PAYMENT AUTHORIZED",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Official Digital Receipt • ${receipt.receiptNumber}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Settled:", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    receipt.formattedTotalToday,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            Text("Stripe PaymentIntent: ${receipt.transactionId}", color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("Timestamp: ${receipt.timestampIso}", color = TextSecondary, fontSize = 10.sp)
                            Text("Client: ${receipt.clientName} (${receipt.businessName})", color = TextMain, fontSize = 11.sp)
                            Text("Receipt Email: ${receipt.clientEmail}", color = TextSecondary, fontSize = 10.sp)
                            Text("Instrument: ${receipt.maskedInstrument} • ${receipt.stripeTokenId}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            Text("Purchased SKUs (${receipt.skus.size}):", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            receipt.itemTitles.forEach { line ->
                                Text("• $line", color = TextMain, fontSize = 10.sp)
                            }
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            Text("SHA-384 Cryptographic Receipt Digest:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(
                                receipt.sha384IntegrityHash,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val text = StripePaymentManager.formatDigitalReceiptText(receipt)
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cb?.setPrimaryClip(ClipData.newPlainText("Digital Receipt ${receipt.receiptNumber}", text))
                                Toast.makeText(context, "Digital Receipt copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_digital_receipt_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val text = StripePaymentManager.formatDigitalReceiptText(receipt)
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${receipt.clientEmail}".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "TYR Protocol Digital Receipt — ${receipt.receiptNumber}")
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                safeStartActivity(context, intent, fallbackCopyText = text)
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, AsgardLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Email Receipt", color = AsgardLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenReceiptsVault()
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0xFF635BFF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🧾 Open Receipts Vault", color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF635BFF), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💳", fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    "STRIPE CONNECTOR CHECKOUT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "PCI-DSS v4.0 SAQ-A • TLS 1.3 • Zero Raw Card Storage",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    // Line Items Summary Card
                    if (activeRefCode.isNotBlank() && refDiscountCents > 0L) {
                        Surface(
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF34D399)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "🤝 10% Referral Credit Active ($activeRefCode): -${StripeTransactionRecord.formatCentsUsd(refDiscountCents)} applied",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "1. ORDER SUMMARY (${servicesToCheckout.size} SKU${if (servicesToCheckout.size == 1) "" else "s"})",
                                color = AsgardLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            servicesToCheckout.forEach { svc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${svc.sku} • ${svc.title}",
                                        color = TextMain,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        svc.price,
                                        color = AsgardLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            if (rawOneTimeCents > 0L) {
                                Text(
                                    "DEPOSIT & MILESTONE SPLIT-PAYMENT CALCULATOR:",
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StripePaymentSplitOption.values().forEach { opt ->
                                        val isSel = selectedSplitOption == opt
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedSplitOption = opt
                                                if (opt != StripePaymentSplitOption.CUSTOM_SPLIT) {
                                                    customSplitPercent = opt.defaultPercent
                                                }
                                            },
                                            label = {
                                                Text(
                                                    opt.label,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF635BFF),
                                                selectedLabelColor = Color.White,
                                                containerColor = BgAttr,
                                                labelColor = TextSecondary
                                            ),
                                            modifier = Modifier.testTag("split_chip_${opt.id}")
                                        )
                                    }
                                }
                                if (selectedSplitOption == StripePaymentSplitOption.CUSTOM_SPLIT) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(20, 25, 40, 60, 75).forEach { pct ->
                                            OutlinedButton(
                                                onClick = { customSplitPercent = pct },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (customSplitPercent == pct) AsgardLight else BorderSubtle
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    "$pct%",
                                                    fontSize = 9.sp,
                                                    color = if (customSplitPercent == pct) AsgardLight else TextSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    selectedSplitOption.description,
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (activeSplitPercent < 100 && rawOneTimeCents > 0L)
                                        "One-Time Capital ($activeSplitPercent% Milestone Due Now):"
                                    else
                                        "One-Time Deliverable Capital:",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    StripeTransactionRecord.formatCentsUsd(oneTimeCents),
                                    color = TextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            if (remainingMilestoneCents > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remaining Balance Upon SOW Handover:", color = AsgardLight, fontSize = 9.sp)
                                    Text(
                                        StripeTransactionRecord.formatCentsUsd(remainingMilestoneCents),
                                        color = AsgardLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly Recurring Retainer:", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    "${StripeTransactionRecord.formatCentsUsd(monthlyCents)}/mo",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL DUE TODAY (USD):", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                Text(
                                    StripeTransactionRecord.formatCentsUsd(totalTodayCents),
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Billing Identity + 1-Tap Autofill
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("2. BILLING & RECEIPT RECIPIENT", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                OutlinedButton(
                                    onClick = {
                                        val p = ClientQolManager.getSavedProfile(context)
                                        if (p.hasData) {
                                            clientName = p.clientName
                                            businessName = p.businessName
                                            clientEmail = p.email
                                            Toast.makeText(context, "Autofilled saved client profile!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No saved profile yet — enter details below.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, AsgardLight)
                                ) {
                                    Text("1-Tap Autofill", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it; errorMessage = null },
                                label = { Text("Cardholder / Client Name *", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stripe_client_name_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = clientEmail,
                                    onValueChange = { clientEmail = it; errorMessage = null },
                                    label = { Text("Receipt Email *", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("stripe_client_email_input")
                                )
                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    label = { Text("Organization", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Payment Method Selection & Ephemeral Tokenization Pad
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF635BFF).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("3. STRIPE PAYMENT METHOD (PCI-DSS SAQ-A)", color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 10.sp)

                            StripePaymentMethodType.values().forEach { method ->
                                val isSel = selectedMethod == method
                                Surface(
                                    color = if (isSel) Color(0xFF1E1B4B) else BgAttr,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) Color(0xFF635BFF) else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMethod = method }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(method.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text(method.description, color = TextSecondary, fontSize = 9.sp, lineHeight = 12.sp)
                                        }
                                        Surface(
                                            color = Color(0xFF635BFF).copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                method.badge,
                                                color = Color(0xFFA5B4FC),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = ephemeralLast4,
                                    onValueChange = { input ->
                                        ephemeralLast4 = input.filter { it.isDigit() }.takeLast(4)
                                    },
                                    label = { Text("Instrument Last 4", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = ephemeralExpiry,
                                    onValueChange = { ephemeralExpiry = it.take(5) },
                                    label = { Text("MM/YY", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = ephemeralZip,
                                    onValueChange = { ephemeralZip = it.take(10) },
                                    label = { Text("Billing ZIP", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                "🛡️ PCI-DSS v4.0 Requirement 3.2 & 3.4 Enforcement: Full card numbers (PAN) and CVV security codes are never stored on disk. Only a cryptographic Stripe token (${selectedMethod.tokenPrefix}_•••) and last 4 digits are recorded.",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { pciConsentChecked = !pciConsentChecked }
                    ) {
                        Checkbox(
                            checked = pciConsentChecked,
                            onCheckedChange = { pciConsentChecked = it }
                        )
                        Text(
                            "I authorize TYR Technical Consulting to process ${StripeTransactionRecord.formatCentsUsd(totalTodayCents)} via Stripe Connector and issue a SHA-384 Digital Receipt.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (clientName.isBlank() || clientEmail.isBlank()) {
                                errorMessage = "Please enter your name and email address to receive your digital receipt."
                                return@Button
                            }
                            if (!pciConsentChecked) {
                                errorMessage = "Please confirm payment authorization above."
                                return@Button
                            }
                            isProcessing = true
                            errorMessage = null
                            scope.launch {
                                val res = StripePaymentManager.processStripeCheckout(
                                    context = context,
                                    services = servicesToCheckout,
                                    clientName = clientName,
                                    businessName = businessName,
                                    clientEmail = clientEmail,
                                    paymentMethod = selectedMethod,
                                    lastFourDigitsInput = ephemeralLast4,
                                    splitPercent = activeSplitPercent
                                )
                                isProcessing = false
                                if (res.success && res.record != null) {
                                    completedReceipt = res.record
                                    onPaymentCompleted(res.record)
                                } else {
                                    errorMessage = res.statusMessage
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_authorize_pay_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isProcessing) "Tokenizing & Authorizing with Stripe..."
                            else "Authorize & Pay ${StripeTransactionRecord.formatCentsUsd(totalTodayCents)} via Stripe",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val (_, sessionUrl, _) = StripePaymentManager.createCheckoutSession(
                                    services = servicesToCheckout,
                                    clientEmail = clientEmail,
                                    clientName = clientName,
                                    splitPercent = activeSplitPercent
                                )
                                val intent = Intent(Intent.ACTION_VIEW, sessionUrl.toUri())
                                safeStartActivity(context, intent, fallbackCopyText = sessionUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_hosted_checkout_btn"),
                        border = BorderStroke(1.dp, Color(0xFFA5B4FC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Hosted Stripe Checkout Session in Browser", color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StripeCheckoutAndReceiptsView(
    cartServices: List<ServiceItem>,
    onAddToCart: (ServiceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        StripePaymentManager.syncRuntimeKeysFromPrefs(context)
    }
    var activeSubTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf(StripePaymentManager.getTransactions(context)) }
    var checkoutModalServices by remember { mutableStateOf<List<ServiceItem>?>(null) }
    var auditVerificationBanner by remember { mutableStateOf<String?>(null) }

    val filteredCatalog = remember(searchQuery) {
        TyrCatalog.services.filter { s ->
            searchQuery.isBlank() ||
                s.title.contains(searchQuery, ignoreCase = true) ||
                s.sku.contains(searchQuery, ignoreCase = true) ||
                s.price.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .padding(vertical = 10.dp)
            .testTag("stripe_checkout_and_receipts_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131129)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF635BFF), Color(0xFF34D399), AsgardLight))),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF635BFF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "STRIPE CONNECTOR",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF34D399)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "PCI-DSS v4.0 SAQ-A",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (cartServices.isNotEmpty()) {
                        Button(
                            onClick = { checkoutModalServices = cartServices.toList() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Pay Cart (${cartServices.size})", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Text(
                    "In-App Service Checkout & SHA-384 Digital Receipts Vault",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    "Purchase any of the 27 catalog services via tokenized Stripe Checkout, view cryptographically signed SHA-384 digital receipts, and export IRS Schedule C transaction audit ledgers.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Sub-Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "🛒 All 27 Services Checkout",
                        1 to "🧾 Digital Receipts Vault (${transactions.size})",
                        2 to "🛡️ PCI-DSS & Stripe Diagnostics"
                    ).forEach { (idx, title) ->
                        FilterChip(
                            selected = activeSubTab == idx,
                            onClick = { activeSubTab = idx },
                            label = {
                                Text(
                                    title,
                                    fontSize = 10.sp,
                                    fontWeight = if (activeSubTab == idx) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF635BFF),
                                selectedLabelColor = Color.White,
                                containerColor = BgCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search all 27 catalog SKUs to purchase...", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA5B4FC)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stripe_catalog_search_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCatalog, key = { it.sku }) { svc ->
                        val isMonthly = StripePaymentManager.isMonthlyRecurringService(svc)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            svc.sku,
                                            color = AsgardLight,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                        Surface(
                                            color = if (isMonthly) Color(0xFF064E3B) else Color(0xFF1E1B4B),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                if (isMonthly) "🔄 Monthly Retainer" else "⚡ One-Time Deliverable",
                                                color = if (isMonthly) Color(0xFF34D399) else Color(0xFFA5B4FC),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(svc.title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${svc.price} • ${svc.priceSub}", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            onAddToCart(svc)
                                            Toast.makeText(context, "Added ${svc.sku} to Cart!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, BorderSubtle)
                                    ) {
                                        Text("+ Cart", fontSize = 10.sp, color = TextSecondary)
                                    }

                                    Button(
                                        onClick = { checkoutModalServices = listOf(svc) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("stripe_buy_sku_${svc.sku}")
                                    ) {
                                        Text("💳 Buy Now", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val ledgerMd = StripePaymentManager.exportTransactionAuditLedgerMarkdown(transactions)
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("transaction_audit_ledger_2026.md", ledgerMd))
                            Toast.makeText(context, "Copied transaction_audit_ledger_2026.md (Schedule C format)!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_schedule_c_ledger_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schedule C (.md)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val ledgerCsv = StripePaymentManager.exportTransactionAuditLedgerCsv(transactions)
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("transaction_audit_ledger_2026.csv", ledgerCsv))
                            Toast.makeText(context, "Copied Schedule C CSV Ledger (.csv) to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_schedule_c_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schedule C (.csv)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            transactions = StripePaymentManager.getTransactions(context)
                            val alerts = transactions.count { it.intrusionStatus.contains("ALERT") }
                            auditVerificationBanner = if (alerts == 0) {
                                "SHA-384 Ledger Audit Verified: ${transactions.size} receipt(s) intact • 0 Financial Intrusions."
                            } else {
                                "[FINANCIAL INTRUSION ALERT] $alerts transaction(s) failed SHA-384 verification!"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, AsgardLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify SHA-384", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (auditVerificationBanner != null) {
                    Surface(
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            auditVerificationBanner!!,
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (transactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧾 No Digital Receipts Stored Yet", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Purchase any catalog service from the 'All 27 Services Checkout' tab or any Service Card to generate a cryptographically signed SHA-384 Digital Receipt.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Button(
                                onClick = { activeSubTab = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Browse Catalog Services to Checkout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(transactions, key = { it.transactionId }) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${record.receiptNumber} • ${record.formattedTotalToday}",
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Surface(
                                            color = Color(0xFF064E3B),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                record.intrusionStatus,
                                                color = Color(0xFF34D399),
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text("Stripe TX: ${record.transactionId} | ${record.timestampIso}", color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                    Text("Client: ${record.clientName} (${record.clientEmail})", color = TextMain, fontSize = 11.sp)
                                    Text("Method: ${record.maskedInstrument} • Token: ${record.stripeTokenId}", color = TextSecondary, fontSize = 10.sp)
                                    Text("SKUs: ${record.skus.joinToString(", ")}", color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(
                                        "SHA-384: ${record.sha384IntegrityHash.take(48)}...",
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val txt = StripePaymentManager.formatDigitalReceiptText(record)
                                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                cb?.setPrimaryClip(ClipData.newPlainText("Receipt ${record.receiptNumber}", txt))
                                                Toast.makeText(context, "Copied Digital Receipt ${record.receiptNumber}!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Copy Receipt", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val txt = StripePaymentManager.formatDigitalReceiptText(record)
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = "mailto:${record.clientEmail}".toUri()
                                                    putExtra(Intent.EXTRA_SUBJECT, "TYR Digital Receipt — ${record.receiptNumber}")
                                                    putExtra(Intent.EXTRA_TEXT, txt)
                                                }
                                                safeStartActivity(context, intent, fallbackCopyText = txt)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Email Receipt", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val sampleServices = remember(cartServices) {
                    cartServices.ifEmpty { listOf(TyrCatalog.services.first()) }
                }
                val sampleConfig = remember(sampleServices) {
                    StripePaymentManager.buildCheckoutSessionConfig(sampleServices)
                }
                val encodedPreview = remember(sampleConfig, sampleServices) {
                    StripePaymentManager.encodeLiveCatalogCheckoutFormParams(
                        config = sampleConfig,
                        services = sampleServices,
                        clientEmail = "client@tyrprotocol.pro"
                    )
                }
                var runtimePubKeyInput by remember { mutableStateOf(StripePaymentManager.getEffectivePublishableKey()) }
                var runtimeSecKeyInput by remember { mutableStateOf("") }
                var keyStatusFingerprint by remember { mutableStateOf(StripePaymentManager.getPublishableKeyFingerprint()) }
                var apiDiagStatus by remember { mutableStateOf(StripePaymentManager.lastApiDiagnosticMessage) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF635BFF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("STRIPE CHECKOUT STUDIO & ACCOUNT BINDING", color = Color(0xFFA5B4FC), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            Text("Stripe Account ID: ${StripePaymentManager.STRIPE_ACCOUNT_ID}", color = Color(0xFF34D399), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Checkout Plan ID:  ${StripePaymentManager.CHECKOUT_PLAN_ID}", color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("Integration ID:    ${sampleConfig.integrationIdentifier} (${sampleConfig.originContext})", color = TextMain, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("Key Status:        $keyStatusFingerprint", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("Last API Status:   $apiDiagStatus", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 10.sp)

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, StripePaymentManager.CHECKOUT_STUDIO_DRAFT_URL.toUri())
                                        safeStartActivity(context, intent, fallbackCopyText = StripePaymentManager.CHECKOUT_STUDIO_DRAFT_URL)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_checkout_studio_plan_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Checkout Studio Plan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        cb?.setPrimaryClip(ClipData.newPlainText("Stripe Checkout Plan URL", StripePaymentManager.CHECKOUT_STUDIO_DRAFT_URL))
                                        Toast.makeText(context, "Copied Checkout Studio Plan URL!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color(0xFFA5B4FC)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Copy Plan URL", fontSize = 10.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val (sessionId, sessionUrl, _) = StripePaymentManager.createCheckoutSession(
                                            services = sampleServices,
                                            clientEmail = "client@tyrprotocol.pro",
                                            clientName = "TYR Diagnostic Verification"
                                        )
                                        apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                        Toast.makeText(context, "Session: $sessionId", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(Intent.ACTION_VIEW, sessionUrl.toUri())
                                        safeStartActivity(context, intent, fallbackCopyText = sessionUrl)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_live_checkout_session_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⚡ Test Live /v1/checkout/sessions API Call & Launch", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    var customSkuPriceMap by remember { mutableStateOf(StripePaymentManager.getCustomSkuPriceMap(context)) }
                    var selectedMapperSku by remember { mutableStateOf(TyrCatalog.services.first().sku) }
                    var mapperPriceIdInput by remember(selectedMapperSku, customSkuPriceMap) {
                        mutableStateOf(customSkuPriceMap[selectedMapperSku] ?: "")
                    }
                    var deepLinkTestStatus by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("RUNTIME STRIPE API KEY VAULT (OPTIONAL LOCAL OVERRIDE)", color = AsgardLight, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            Text(
                                "You can set STRIPE_PUBLISHABLE_KEY and STRIPE_SECRET_KEY in the AI Studio Secrets panel, or paste test/live keys below for immediate on-device /v1/checkout/sessions API execution.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            OutlinedTextField(
                                value = runtimePubKeyInput,
                                onValueChange = { runtimePubKeyInput = it },
                                label = { Text("Publishable Key (pk_test_... / pk_live_...)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = runtimeSecKeyInput,
                                onValueChange = { runtimeSecKeyInput = it },
                                label = { Text("Secret Key (sk_test_... / sk_live_...)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        StripePaymentManager.saveRuntimeKeys(context, runtimePubKeyInput, runtimeSecKeyInput)
                                        runtimeSecKeyInput = ""
                                        keyStatusFingerprint = StripePaymentManager.getPublishableKeyFingerprint()
                                        apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                        Toast.makeText(context, "Saved Stripe keys to local Zero-Custody vault!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save Keys Locally", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        StripePaymentManager.purgeRuntimeKeys(context)
                                        runtimePubKeyInput = ""
                                        runtimeSecKeyInput = ""
                                        keyStatusFingerprint = StripePaymentManager.getPublishableKeyFingerprint()
                                        apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                        Toast.makeText(context, "0-Day Purge: Wiped runtime Stripe keys!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("0-Day Purge Keys", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // In-App Stripe Price ID Mapper per Service SKU
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "IN-APP STRIPE PRICE ID MAPPER (${customSkuPriceMap.size}/27 SKUs Mapped)",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                            Text(
                                "Bind real Stripe Dashboard Price IDs (price_...) to individual catalog SKUs without rebuilding. Unmapped SKUs automatically use dynamic inline price_data.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TyrCatalog.services.forEach { svc ->
                                    val isMapped = customSkuPriceMap.containsKey(svc.sku)
                                    val isSel = selectedMapperSku == svc.sku
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedMapperSku = svc.sku },
                                        label = {
                                            Text(
                                                if (isMapped) "✓ ${svc.sku}" else svc.sku,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSel || isMapped) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF059669),
                                            selectedLabelColor = Color.White,
                                            containerColor = if (isMapped) Color(0xFF064E3B) else BgAttr,
                                            labelColor = if (isMapped) Color(0xFF34D399) else TextSecondary
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = mapperPriceIdInput,
                                onValueChange = { mapperPriceIdInput = it },
                                label = { Text("Stripe Price ID for $selectedMapperSku (e.g. price_1Pxyz...)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sku_price_mapper_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        customSkuPriceMap = StripePaymentManager.setCustomSkuPriceId(
                                            context,
                                            selectedMapperSku,
                                            mapperPriceIdInput
                                        )
                                        apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                        Toast.makeText(context, "Saved Stripe Price ID for $selectedMapperSku!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_sku_price_id_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Bind Price ID to $selectedMapperSku", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        StripePaymentManager.clearAllCustomSkuPriceIds(context)
                                        customSkuPriceMap = emptyMap()
                                        mapperPriceIdInput = ""
                                        apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                        Toast.makeText(context, "Reset all SKU Price IDs to inline price_data fallback.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reset All Mappings", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // Deep-Link Return Handler & Session Reconciliation Verifier
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("DEEP-LINK RETURN HANDLER & SESSION RECONCILIATION", color = Color(0xFF38BDF8), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            Text(
                                "Registered Android App Links (`https://tyrprotocol.pro/success`) and custom URI scheme (`tyrprotocol://checkout/success`) automatically reconcile hosted Stripe Checkout redirects.",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            if (deepLinkTestStatus != null) {
                                Text(
                                    deepLinkTestStatus!!,
                                    color = Color(0xFF34D399),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    val testUri = "tyrprotocol://checkout/success?session_id=cs_live_${System.currentTimeMillis()}&skus=${selectedMapperSku}"
                                    val (msg, _) = StripePaymentManager.reconcileCheckoutDeepLink(context, testUri)
                                    deepLinkTestStatus = msg
                                    transactions = StripePaymentManager.getTransactions(context)
                                    apiDiagStatus = StripePaymentManager.lastApiDiagnosticMessage
                                    Toast.makeText(context, "Deep-link session reconciled!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_deeplink_reconcile_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔗 Test Deep-Link Return Reconciliation ($selectedMapperSku)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("CHECKOUT STUDIO FIXED_BY_UI PARAMETERS", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            Text("• ui_mode: \"${sampleConfig.uiMode}\" (mode: \"${sampleConfig.mode}\")", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• billing_address_collection: \"${sampleConfig.billingAddressCollection}\"", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• phone_number_collection[enabled]: ${sampleConfig.phoneNumberCollectionEnabled}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• automatic_tax[enabled]: ${sampleConfig.automaticTaxEnabled}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• allow_promotion_codes: ${sampleConfig.allowPromotionCodes}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• payment_method_collection: ${sampleConfig.paymentMethodCollection ?: "(omitted for one-time payment)"}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• submit_type: \"${sampleConfig.submitType}\"", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• tax_id_collection: enabled=${sampleConfig.taxIdCollectionEnabled}, required=\"${sampleConfig.taxIdCollectionRequired}\"", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• consent_collection[terms_of_service]: \"${sampleConfig.consentTermsOfService}\"", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Text("• saved_payment_method_options[payment_method_save]: \"${sampleConfig.savedPaymentMethodSave}\"", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("Stripe Checkout Payload", encodedPreview))
                                    Toast.makeText(context, "Copied URL-encoded Checkout Session payload!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFF34D399)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy Encoded /v1/checkout/sessions Payload", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, Color(0xFF635BFF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("STRIPE CONNECTOR & PCI-DSS v4.0 POSTURE", color = Color(0xFFA5B4FC), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            Text("• PCI-DSS Requirement 3.2: CVV/CVC security codes are never persisted after authorization.", color = TextSecondary, fontSize = 10.sp)
                            Text("• PCI-DSS Requirement 3.3: Primary Account Numbers (PAN) are masked strictly to last 4 digits (•••• XXXX).", color = TextSecondary, fontSize = 10.sp)
                            Text("• PCI-DSS Requirement 3.4: Every transaction record is sealed with a 96-character SHA-384 digest.", color = TextSecondary, fontSize = 10.sp)
                            Text("• PCI-DSS Requirement 4.2: All Stripe API / Checkout Session calls enforce HTTPS TLS 1.3 transport.", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    if (checkoutModalServices != null) {
        StripeCheckoutSheetDialog(
            servicesToCheckout = checkoutModalServices!!,
            onDismiss = { checkoutModalServices = null },
            onPaymentCompleted = {
                transactions = StripePaymentManager.getTransactions(context)
            },
            onOpenReceiptsVault = {
                checkoutModalServices = null
                activeSubTab = 1
            }
        )
    }
}
