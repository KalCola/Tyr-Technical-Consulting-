package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.ClientQolManager
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ContractTemplate(
    val id: String,
    val title: String,
    val code: String,
    val summary: String,
    val statutoryRef: String
)

val CONTRACT_TEMPLATES = listOf(
    ContractTemplate(
        id = "MSA-SOW-01",
        title = "Master Services Agreement & Statement of Work",
        code = "DOC-001_WO-2026-1028-01",
        summary = "Standard technical consulting engagement agreement governing scope, milestones, IP assignment upon payment, and 0-Day raw data purge covenant.",
        statutoryRef = "15 U.S.C. § 7001 (E-SIGN) & Alabama UETA (Ala. Code § 8-1A-1)"
    ),
    ContractTemplate(
        id = "EXEC-RETAINER-02",
        title = "Executive Architecture Retainer Agreement",
        code = "DOC-001_WO-2026-1028-02",
        summary = "Monthly technical care, security monitoring, DNSSEC management, and emergency priority response SLA with strict zero-custody guarantees.",
        statutoryRef = "NAICS 541512 Computer Systems Design Services"
    ),
    ContractTemplate(
        id = "M-NDA-03",
        title = "Mutual Non-Disclosure & Zero-Custody IP Covenant",
        code = "DOC-002_NDA-2026-0001",
        summary = "Bilateral trade secret protection with mandatory cryptographic destruction of raw customer credentials immediately upon project handover.",
        statutoryRef = "Uniform Trade Secrets Act & 18 U.S.C. § 1836 (Defend Trade Secrets Act)"
    )
)

data class SignedCertificate(
    val docCode: String,
    val signerName: String,
    val signerTitle: String,
    val signerOrg: String,
    val signerEmail: String,
    val timestamp: String,
    val sha384Fingerprint: String
)

@Composable
fun DigitalContractSignView(
    onNavigateToIntake: () -> Unit = {},
    prefilledWorkOrder: WorkOrder? = null,
    onProceedToStripeCheckout: ((List<ServiceItem>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedProfile = remember { ClientQolManager.getSavedProfile(context) }
    var selectedTemplate by remember { mutableStateOf(CONTRACT_TEMPLATES[0]) }

    var signerName by remember(prefilledWorkOrder) {
        mutableStateOf(prefilledWorkOrder?.clientName?.ifBlank { savedProfile.clientName } ?: savedProfile.clientName)
    }
    var signerTitle by remember { mutableStateOf("Authorized Representative") }
    var signerOrg by remember(prefilledWorkOrder) {
        mutableStateOf(prefilledWorkOrder?.businessName?.ifBlank { savedProfile.businessName } ?: savedProfile.businessName)
    }
    var signerEmail by remember(prefilledWorkOrder) {
        mutableStateOf(prefilledWorkOrder?.email?.ifBlank { savedProfile.email } ?: savedProfile.email)
    }
    var consentChecked by remember { mutableStateOf(true) }

    // Touch signature paths
    val signaturePoints = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke = remember { mutableStateListOf<Offset>() }

    var signedCertificate by remember { mutableStateOf<SignedCertificate?>(null) }

    val hasSignature = signaturePoints.isNotEmpty() || currentStroke.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgObsidian)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MidgardMain, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✍️", fontSize = 18.sp)
                    }
                    Column {
                        Text("Digital Contract E-Sign", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("E-SIGN Act (15 U.S.C. § 7001) Compliant • Zero-Custody SOW Execution", color = AsgardLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "Execute binding Statements of Work (SOW), Master Services Agreements (MSA), and Mutual NDAs directly on-device with SHA-384 cryptographic verification.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                if (prefilledWorkOrder != null) {
                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "⚡ CHAINED WORK ORDER ACTIVE: ${prefilledWorkOrder.id} (${prefilledWorkOrder.totalEstimatedPrice})",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                            Text(
                                "SKUs in Scope: ${prefilledWorkOrder.selectedSkus.joinToString(", ")} • Sign below to unlock Stripe Deposit / Full Settlement.",
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Template Selector
        Text("1. SELECT DOCUMENT TO EXECUTE", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CONTRACT_TEMPLATES.forEach { tmpl ->
                val isSelected = selectedTemplate == tmpl
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedTemplate = tmpl
                            signedCertificate = null
                        },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) BgAttr else BgCard),
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AsgardLight else BorderSubtle),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tmpl.title, color = if (isSelected) TextMain else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(tmpl.code, color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(tmpl.summary, color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                    }
                }
            }
        }

        // Signer Information Fields
        Text("2. SIGNER IDENTITY & AUTHORITY", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = signerName,
                        onValueChange = { signerName = it },
                        modifier = Modifier.weight(1f).testTag("signer_name_input"),
                        label = { Text("Full Legal Name *", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                    OutlinedTextField(
                        value = signerTitle,
                        onValueChange = { signerTitle = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Signer Title", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = signerOrg,
                        onValueChange = { signerOrg = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Company / Organization", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                    OutlinedTextField(
                        value = signerEmail,
                        onValueChange = { signerEmail = it },
                        modifier = Modifier.weight(1f).testTag("signer_email_input"),
                        label = { Text("Official Email *", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                }
            }
        }

        // Interactive Signature Pad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("3. TOUCHSCREEN SIGNATURE PAD", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            if (hasSignature) {
                TextButton(
                    onClick = {
                        signaturePoints.clear()
                        currentStroke.clear()
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear", modifier = Modifier.size(12.dp), tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Signature", color = Color(0xFFF87171), fontSize = 10.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF07070B)),
            border = BorderStroke(1.dp, if (hasSignature) AsgardLight else BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke = mutableStateListOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke.add(change.position)
                            },
                            onDragEnd = {
                                if (currentStroke.isNotEmpty()) {
                                    signaturePoints.add(currentStroke.toList())
                                    currentStroke = mutableStateListOf()
                                }
                            }
                        )
                    }
            ) {
                // Background Guide Lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseline = size.height * 0.78f
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(20f, baseline),
                        end = Offset(size.width - 20f, baseline),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw completed strokes
                    signaturePoints.forEach { stroke ->
                        if (stroke.size > 1) {
                            val path = Path().apply {
                                moveTo(stroke.first().x, stroke.first().y)
                                for (i in 1 until stroke.size) {
                                    lineTo(stroke[i].x, stroke[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF38BDF8),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Draw in-progress stroke
                    if (currentStroke.size > 1) {
                        val path = Path().apply {
                            moveTo(currentStroke.first().x, currentStroke.first().y)
                            for (i in 1 until currentStroke.size) {
                                lineTo(currentStroke[i].x, currentStroke[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                if (!hasSignature) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "✍️ Sign with finger or stylus above the baseline",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    "X",
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 32.dp)
                )
            }
        }

        // Statutory Consent Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = consentChecked,
                onCheckedChange = { consentChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = AsgardLight, checkmarkColor = Color.Black)
            )
            Column {
                Text(
                    "Electronic Signature Consent (15 U.S.C. § 7001):",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    "I verify that I have authority to execute this agreement on behalf of the named organization. I agree that my digital signature constitutes a binding electronic record under the E-SIGN Act and Alabama UETA. Zero-Custody rule acknowledged: raw financial source files are purged day 0 upon completion.",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
        }

        // Execute & Bind Button
        Button(
            onClick = {
                val nowStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                // Compute deterministic SHA-384 fingerprint over (docCode + signerName + signerEmail + timestamp + pointCount)
                val rawPayload = "${selectedTemplate.code}|${signerName.trim()}|${signerEmail.trim()}|${signerOrg.trim()}|$nowStr|${signaturePoints.size}"
                val hexString = sha384Hex(rawPayload)

                signedCertificate = SignedCertificate(
                    docCode = selectedTemplate.code,
                    signerName = signerName.trim(),
                    signerTitle = signerTitle.trim(),
                    signerOrg = signerOrg.trim().ifBlank { "Independent" },
                    signerEmail = signerEmail.trim(),
                    timestamp = nowStr,
                    sha384Fingerprint = hexString
                )
                Toast.makeText(context, "Contract executed! Cryptographic certificate generated.", Toast.LENGTH_LONG).show()
            },
            enabled = signerName.isNotBlank() && signerEmail.isNotBlank() && hasSignature && consentChecked,
            modifier = Modifier.fillMaxWidth().testTag("btn_execute_contract"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AsgardLight, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Execute & Bind Contract (Generate SHA-384)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Signed Cryptographic Certificate Card
        if (signedCertificate != null) {
            val cert = signedCertificate!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Text("EXECUTED CONTRACT CERTIFICATE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text(cert.docCode, color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                    }

                    Text("Signer: ${cert.signerName} (${cert.signerTitle}) • ${cert.signerOrg}", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Email: ${cert.signerEmail} • Timestamp: ${cert.timestamp}", color = TextSecondary, fontSize = 10.sp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text("SHA-384 CRYPTOGRAPHIC PROOF:", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(cert.sha384Fingerprint, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val certBody = """
                                    TYR TECHNICAL CONSULTING — EXECUTED CONTRACT CERTIFICATE
                                    ========================================================
                                    Document: ${cert.docCode} (${selectedTemplate.title})
                                    Signer: ${cert.signerName}
                                    Title: ${cert.signerTitle}
                                    Organization: ${cert.signerOrg}
                                    Email: ${cert.signerEmail}
                                    Execution Timestamp: ${cert.timestamp}
                                    SHA-384 Digest: ${cert.sha384Fingerprint}
                                    Statutory Reference: ${selectedTemplate.statutoryRef}
                                    Zero-Custody Covenant: 0 Raw Financial Data Stored (0-Day Purge Enforced)
                                """.trimIndent()

                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${TyrCatalog.email}".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Executed Contract [${cert.docCode}] - ${cert.signerName}")
                                    putExtra(Intent.EXTRA_TEXT, certBody)
                                }
                                safeStartActivity(context, intent, fallbackCopyText = certBody)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email Certificate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Contract SHA-384", cert.sha384Fingerprint)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "SHA-384 copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hash", modifier = Modifier.size(13.dp), tint = TextMain)
                        }
                    }

                    if (onProceedToStripeCheckout != null) {
                        Button(
                            onClick = {
                                val matchedServices = prefilledWorkOrder?.selectedSkus?.mapNotNull { sku ->
                                    TyrCatalog.services.find { it.sku == sku }
                                }?.ifEmpty { listOf(TyrCatalog.services.first()) }
                                    ?: listOf(TyrCatalog.services.first())
                                onProceedToStripeCheckout(matchedServices)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sow_proceed_to_stripe_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "💳 Step 3: Proceed to Stripe Deposit / Full Settlement",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
