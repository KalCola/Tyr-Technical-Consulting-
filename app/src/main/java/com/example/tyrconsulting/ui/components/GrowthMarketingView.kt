package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.*
import com.example.tyrconsulting.ui.theme.*

private enum class GrowthSubTab(val label: String) {
    QUIZ("⚡ 60s Audit"),
    PITCH("📣 Pitch & Share"),
    FLYER("🖨️ QR Flyer"),
    PROOF("📊 Proof & Ref"),
    GBP("📍 GBP & SEO")
}

@Composable
fun GrowthMarketingView(
    onAddMatchedSkusToCart: (List<ServiceItem>) -> Unit = {},
    onLaunchIntakeForSkus: (List<ServiceItem>) -> Unit = {},
    onLaunchStripeForSkus: (List<ServiceItem>) -> Unit = {},
    onOpen3dCard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(GrowthSubTab.QUIZ) }

    // Quiz State
    var quizAnswers by remember {
        mutableStateOf(mapOf("q_web" to 0, "q_email" to 0, "q_legal" to 0, "q_bills" to 0))
    }

    // Pitch State
    var selectedChannel by remember { mutableStateOf(GrowthChannel.NEXTDOOR) }
    var selectedPitchSkuIndex by remember { mutableIntStateOf(0) }
    var pitchIncludeRef by remember { mutableStateOf(true) }

    // Flyer State
    var flyerMode by remember { mutableStateOf("FLYER") }
    var flyerTargetType by remember { mutableStateOf("STOREFRONT") }
    var flyerSkuIndex by remember { mutableIntStateOf(0) }
    var flyerIncludeRef by remember { mutableStateOf(true) }

    // Referral & Checklist State
    val metrics = remember { ReferralManager.getMetrics(context) }
    var ownRefCode by remember { mutableStateOf(metrics.referralCode) }
    var activeInboundRef by remember { mutableStateOf(GrowthMarketingManager.getActiveInboundRefCode(context)) }
    var manualInboundInput by remember { mutableStateOf(activeInboundRef) }
    var shareCount by remember { mutableIntStateOf(GrowthMarketingManager.getOutboundShareCount(context)) }
    var checklistState by remember { mutableStateOf(GrowthMarketingManager.getChecklistState(context)) }

    val allServices = TyrCatalog.services
    val pitchService = allServices.getOrNull(selectedPitchSkuIndex) ?: allServices.firstOrNull()
    val flyerService = allServices.getOrNull(flyerSkuIndex) ?: allServices.firstOrNull()

    fun copyToClipboard(label: String, value: String) {
        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cb?.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
    }

    fun sharePlainText(subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        shareCount = GrowthMarketingManager.recordOutboundShare(context)
        context.startActivity(Intent.createChooser(intent, "Share via $0 Channel"))
    }

    fun printHtmlCollateral(html: String, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service unavailable on this device.", Toast.LENGTH_SHORT).show()
            return
        }
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val adapter = view?.createPrintDocumentAdapter(jobName) ?: return
                printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🚀 $0 SELF-MARKETING & GROWTH HUB",
                        color = AsgardLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "$0 Ad Spend",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    "60-second quick audit quiz, one-tap Nextdoor/Facebook/SMS pitches, printable QR flyers, NW Alabama proof cards, referral attribution & GBP setup.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpen3dCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("🪪 3D Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { sharePlainText("Caleb Holcomb — TYR vCard", GrowthMarketingManager.vCardPayload) },
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("⬇ Share .vcf Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub-Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GrowthSubTab.entries.forEach { tab ->
                val isSel = activeSubTab == tab
                FilterChip(
                    selected = isSel,
                    onClick = { activeSubTab = tab },
                    label = { Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MidgardMain,
                        selectedLabelColor = Color.White,
                        containerColor = BgCard,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("growth_tab_${tab.name}")
                )
            }
        }

        when (activeSubTab) {
            GrowthSubTab.QUIZ -> {
                val result = GrowthMarketingManager.evaluateQuickAudit(quizAnswers)
                val matched = result.recommendedSkus.mapNotNull { sku -> allServices.find { it.sku == sku } }
                val scoreColor = when {
                    result.score >= 80 -> VanaheimLight
                    result.score >= 50 -> AsgardLight
                    else -> MidgardLight
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SOVEREIGNTY & SAVINGS SCORE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Estimated Annual Leak: $${result.estAnnualLeakUsd}/yr",
                                color = TextMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "${result.score}/100",
                            color = scoreColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                GrowthMarketingManager.quizQuestions.forEach { q ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(q.prompt, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            q.options.forEachIndexed { idx, opt ->
                                val selected = (quizAnswers[q.id] ?: 0) == idx
                                Surface(
                                    color = if (selected) MidgardMain.copy(alpha = 0.2f) else BgAttr,
                                    border = BorderStroke(1.dp, if (selected) MidgardLight else BorderSubtle),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { quizAnswers = quizAnswers.toMutableMap().apply { put(q.id, idx) } }
                                ) {
                                    Text(
                                        text = (if (selected) "✓ " else "") + opt.label,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF052E16)),
                    border = BorderStroke(1.dp, VanaheimLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("RECOMMENDED MATCHED DELIVERABLES (${matched.size})", color = VanaheimLight, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        matched.forEach { svc ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${svc.sku} — ${svc.title}", color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(svc.price, color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onLaunchStripeForSkus(matched) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("💳 Stripe Split", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onLaunchIntakeForSkus(matched) },
                                colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("📝 Intake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    onAddMatchedSkusToCart(matched)
                                    Toast.makeText(context, "Added ${matched.size} SKUs to Cart!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("+ Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            GrowthSubTab.PITCH -> {
                val pitchBody = GrowthMarketingManager.buildPitchText(selectedChannel, pitchService, ownRefCode, pitchIncludeRef)
                val deepUrl = GrowthMarketingManager.buildShareUrl(pitchService?.sku.orEmpty(), ownRefCode, pitchIncludeRef)

                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Channel Template", color = AsgardLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GrowthChannel.entries.forEach { ch ->
                                FilterChip(
                                    selected = selectedChannel == ch,
                                    onClick = { selectedChannel = ch },
                                    label = { Text(ch.label, fontSize = 11.sp) }
                                )
                            }
                        }
                        Text("Featured SKU (Tap to Cycle)", color = AsgardLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedButton(
                            onClick = {
                                if (allServices.isNotEmpty()) {
                                    selectedPitchSkuIndex = (selectedPitchSkuIndex + 1) % allServices.size
                                }
                            },
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                        ) {
                            Text(
                                "${pitchService?.sku ?: "SVC-WEB-01"} — ${pitchService?.title ?: ""} (${pitchService?.price ?: ""})",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Attach 10% Ref Code ($ownRefCode)", color = TextSecondary, fontSize = 11.sp)
                            Switch(checked = pitchIncludeRef, onCheckedChange = { pitchIncludeRef = it })
                        }
                        Surface(
                            color = BgAttr,
                            border = BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(deepUrl, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                HorizontalDivider(color = BorderSubtle)
                                Text(pitchBody, color = TextMain, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    copyToClipboard("TYR Pitch", pitchBody)
                                    shareCount = GrowthMarketingManager.recordOutboundShare(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("📋 Copy Pitch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { sharePlainText("TYR Technical Consulting — ${pitchService?.sku}", pitchBody) },
                                colors = ButtonDefaults.buttonColors(containerColor = VanaheimMain),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("↗ OS Share Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            GrowthSubTab.FLYER -> {
                val targetUrl = when (flyerTargetType) {
                    "VCARD" -> GrowthMarketingManager.MECARD_QR_STRING
                    "SKU" -> GrowthMarketingManager.buildShareUrl(flyerService?.sku.orEmpty(), ownRefCode, flyerIncludeRef)
                    else -> GrowthMarketingManager.buildShareUrl("", ownRefCode, flyerIncludeRef)
                }
                val matrix = remember(targetUrl) { GrowthQrEncoder.buildMatrix(targetUrl) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Printable QR Flyer & Leave-Behind Card", color = AsgardLight, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = flyerMode == "FLYER", onClick = { flyerMode = "FLYER" }, label = { Text("📄 8.5×11 Flyer", fontSize = 11.sp) })
                            FilterChip(selected = flyerMode == "CARD", onClick = { flyerMode = "CARD" }, label = { Text("🪪 3.5×2 Pocket Card", fontSize = 11.sp) })
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(selected = flyerTargetType == "STOREFRONT", onClick = { flyerTargetType = "STOREFRONT" }, label = { Text("🌐 Storefront", fontSize = 11.sp) })
                            FilterChip(selected = flyerTargetType == "SKU", onClick = { flyerTargetType = "SKU" }, label = { Text("🏷️ Specific SKU", fontSize = 11.sp) })
                            FilterChip(selected = flyerTargetType == "VCARD", onClick = { flyerTargetType = "VCARD" }, label = { Text("👤 Contact QR", fontSize = 11.sp) })
                        }
                        if (flyerTargetType == "SKU") {
                            OutlinedButton(
                                onClick = { if (allServices.isNotEmpty()) flyerSkuIndex = (flyerSkuIndex + 1) % allServices.size },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${flyerService?.sku} — ${flyerService?.title} (${flyerService?.price})", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Referral Code $ownRefCode", color = TextSecondary, fontSize = 11.sp)
                            Switch(checked = flyerIncludeRef, onCheckedChange = { flyerIncludeRef = it })
                        }

                        // Live Vector QR Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Canvas(modifier = Modifier.size(164.dp)) {
                                    val n = matrix.size
                                    val cell = size.width / n.toFloat()
                                    for (r in 0 until n) {
                                        for (c in 0 until n) {
                                            if (matrix[r][c]) {
                                                drawRect(
                                                    color = Color(0xFF090A0F),
                                                    topLeft = Offset(c * cell, r * cell),
                                                    size = Size(cell + 0.5f, cell + 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(targetUrl, color = Color(0xFF334155), fontFamily = FontFamily.Monospace, fontSize = 9.sp, maxLines = 2)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val html = GrowthMarketingManager.buildPrintableHtml(
                                        mode = flyerMode,
                                        targetUrl = targetUrl,
                                        skuTitle = if (flyerTargetType == "SKU") flyerService?.title.orEmpty() else "",
                                        skuPrice = if (flyerTargetType == "SKU") flyerService?.price.orEmpty() else "",
                                        refCode = ownRefCode,
                                        includeRef = flyerIncludeRef
                                    )
                                    printHtmlCollateral(html, "TYR_${flyerMode}_QR")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("🖨️ Print / Save PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { copyToClipboard("Target QR URL", targetUrl) },
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                            ) {
                                Text("📋 Copy QR Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            GrowthSubTab.PROOF -> {
                // Referral Attribution Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🤝 REFERRAL CREDIT & ATTRIBUTION", color = AsgardLight, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Share Code: $ownRefCode", color = TextMain, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Outbound Shares: $shareCount", color = VanaheimLight, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (activeInboundRef.isNotBlank()) {
                            Surface(color = Color(0xFF064E3B), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "✓ Active Checkout Credit: $activeInboundRef (10% off 1-time builds)",
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = manualInboundInput,
                                onValueChange = { manualInboundInput = it },
                                label = { Text("Apply Neighbor Referral Code (10% Off)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    activeInboundRef = GrowthMarketingManager.setActiveInboundRefCode(context, manualInboundInput)
                                    Toast.makeText(
                                        context,
                                        if (activeInboundRef.isNotBlank()) "Applied 10% credit: $activeInboundRef" else "Cleared referral code",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VanaheimMain),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Apply", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Case Study Proof Cards
                GrowthMarketingManager.caseStudies.forEach { cs ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cs.id, color = VanaheimLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(cs.sku, color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                            Text(cs.headline, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(cs.region, color = Color(0xFF38BDF8), fontSize = 10.sp)
                            Text("Before: ${cs.beforeState}", color = TextMuted, fontSize = 11.sp)
                            Text("Deliverable: ${cs.deliverable}", color = TextSecondary, fontSize = 11.sp)
                            Text("✓ ${cs.verifiedOutcome}", color = VanaheimLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            GrowthSubTab.GBP -> {
                val doneCount = GrowthMarketingManager.gbpChecklistItems.count { checklistState[it.id] == true }
                val totalCount = GrowthMarketingManager.gbpChecklistItems.size
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, VanaheimMain.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📍 $0 GBP & LOCAL DIRECTORY CHECKLIST", color = VanaheimLight, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("Service-Area posture hides residential addresses across NW Alabama.", color = TextSecondary, fontSize = 10.sp)
                        }
                        Text("$doneCount/$totalCount", color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                GrowthMarketingManager.gbpChecklistItems.forEach { item ->
                    val checked = checklistState[item.id] == true
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (checked) Color(0xFF052E16) else BgCard),
                        border = BorderStroke(1.dp, if (checked) VanaheimLight else BorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { checklistState = GrowthMarketingManager.toggleChecklistItem(context, item.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { checklistState = GrowthMarketingManager.toggleChecklistItem(context, item.id) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(item.cost, color = VanaheimLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.detail, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
