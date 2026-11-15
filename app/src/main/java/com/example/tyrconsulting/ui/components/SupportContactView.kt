package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.ClientQolManager
import com.example.tyrconsulting.model.SavedClientProfile
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SlaSeverityTier(
    val code: String,
    val title: String,
    val responseTarget: String,
    val color: Color,
    val description: String
) {
    P1_CRITICAL(
        "P1-CRITICAL",
        "P1: Critical Outage / Active Incident",
        "< 2 Hours SLA",
        Color(0xFFEF4444),
        "Production website down, DNS hijack/lockout, or active security incident."
    ),
    P2_HIGH(
        "P2-URGENT",
        "P2: Urgent Security / Billing Audit",
        "< 6 Hours SLA",
        Color(0xFFF59E0B),
        "Time-sensitive vendor dispute, DMARC/email deliverability block, or contract redline."
    ),
    P3_STANDARD(
        "P3-STANDARD",
        "P3: Standard Consulting & Deliverable",
        "< 24 Hours SLA",
        Color(0xFF38BDF8),
        "New project scoping, existing work order update, or custom automation quote."
    ),
    P4_GENERAL(
        "P4-GENERAL",
        "P4: General Question / Partner Inquiry",
        "< 48 Hours SLA",
        Color(0xFF34D399),
        "General storefront questions, referral partnerships, or documentation requests."
    )
}

data class SupportFaqItem(
    val question: String,
    val answer: String
)

@Composable
fun SupportContactView(
    onNavigateToChatRelay: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedSeverity by remember { mutableStateOf(SlaSeverityTier.P3_STANDARD) }
    var issueCategory by remember { mutableStateOf("Web & Domain Architecture") }
    var clientName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var issueSummary by remember { mutableStateOf("") }
    var generatedTicketReceipt by remember { mutableStateOf<String?>(null) }
    var expandedFaqIndex by remember { mutableIntStateOf(0) }

    val categories = listOf(
        "Web & Domain Architecture",
        "Zero-Custody Financial / ISP Audit",
        "B2B MSA / SOW Contract Support",
        "Workstation / Encryption Hardening",
        "Billing & Retainer Clarification",
        "App / Technical Bug Report"
    )

    val faqItems = remember {
        listOf(
            SupportFaqItem(
                "How fast do you respond to support tickets and project inquiries?",
                "All inquiries are answered directly by Principal Systems Architect Caleb TR Holcomb. P1 Critical production or security incidents receive response within 2 hours; P2 Urgent issues within 6 hours; and standard project scopes within 24 hours."
            ),
            SupportFaqItem(
                "What happens to my files or statements when I request an audit or support?",
                "Under our Zero-Custody Covenant, raw statements, invoices, and temporary credentials are processed strictly in volatile memory during active troubleshooting and purged on Day 0 upon ticket closure."
            ),
            SupportFaqItem(
                "Are one-time storefront packages really $0/month forever?",
                "Yes. One-time builds (Pages 1, 2, 3, 6, and 7) are delivered as 100% client-owned master assets and static architectures with $0/month hosting and zero mandatory recurring fees. Monthly billing only applies if you explicitly enroll in a Care Plan (Pages 4, 5, or 8)."
            ),
            SupportFaqItem(
                "How do revisions and deliverable acceptance work?",
                "Every fixed-price deliverable includes a structured review window after initial handover to fine-tune copy, layout, scripts, or contract clauses before final sign-off."
            ),
            SupportFaqItem(
                "Can I call or text directly instead of filling out a form?",
                "Absolutely. Tap 'Call Direct' or 'Send SMS' at the top of this screen to reach +1 (659) 257-8043, or use the Google Chat Relay bridge for real-time sanitized messaging."
            )
        )
    }

    fun buildFormattedTicket(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'CST'", Locale.US).format(Date())
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val rawSeed = "${selectedSeverity.code}|$issueCategory|$clientName|$clientEmail|$timestamp|$issueSummary"
        val digest = sha384Hex(rawSeed)
        val ticketId = "SLA-$dateCode-${digest.take(6).uppercase()}"

        return buildString {
            appendLine("==================================================")
            appendLine("TYR TECHNICAL CONSULTING — PRIORITY SLA TICKET")
            appendLine("==================================================")
            appendLine("TICKET ID: $ticketId")
            appendLine("SEVERITY: ${selectedSeverity.code} (${selectedSeverity.responseTarget})")
            appendLine("CATEGORY: $issueCategory")
            appendLine("TIMESTAMP: $timestamp")
            appendLine("CLIENT: ${clientName.ifBlank { "Prospective Client" }} (${businessName.ifBlank { "Independent" }})")
            appendLine("CONTACT: ${clientEmail.ifBlank { "N/A" }} | ${clientPhone.ifBlank { "N/A" }}")
            appendLine("--------------------------------------------------")
            appendLine("ISSUE / REQUEST SUMMARY:")
            appendLine(issueSummary.ifBlank { "Requesting direct technical consultation and SLA support." })
            appendLine("--------------------------------------------------")
            appendLine("ZERO-CUSTODY PLEDGE: Day-0 RAM Purge Enforced")
            appendLine("SHA-384 RECEIPT: ${digest.take(48)}...")
            appendLine("==================================================")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
            .testTag("support_contact_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live System Telemetry & Operational Status Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF065F46)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛟", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                "Support & SLA Contact Center",
                                color = TextMain,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                "Direct Principal Engineer Dispatch • Northwest Alabama & Remote US",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399))
                            )
                            Text(
                                "ONLINE",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    "Skip automated chatbots and tier-1 call centers. Connect directly with Lead Systems Architect Caleb TR Holcomb via phone, SMS, email, Google Chat Relay, or open a severity-triaged SLA support ticket below.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                // Direct 4-Channel Dispatch Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportQuickChannelBtn(
                        icon = "📞",
                        title = "Call Direct",
                        sub = "(659) 257-8043",
                        accent = MidgardLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_btn_call"),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
                            safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
                        }
                    )
                    SupportQuickChannelBtn(
                        icon = "💬",
                        title = "Send SMS",
                        sub = "Direct Text",
                        accent = AsgardLight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_btn_sms"),
                        onClick = {
                            val smsIntent = Intent(Intent.ACTION_SENDTO, "smsto:${TyrCatalog.phoneRaw}".toUri()).apply {
                                putExtra("sms_body", "Hello Caleb (TYR Support), I need assistance with: ")
                            }
                            safeStartActivity(context, smsIntent, fallbackCopyText = TyrCatalog.phone)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportQuickChannelBtn(
                        icon = "✉️",
                        title = "Email SLA",
                        sub = "tyr@tyrprotocol.pro",
                        accent = Color(0xFF38BDF8),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_btn_email"),
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:${TyrCatalog.email}".toUri()).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "TYR Support & Consultation Inquiry")
                            }
                            safeStartActivity(context, emailIntent, fallbackCopyText = TyrCatalog.email)
                        }
                    )
                    SupportQuickChannelBtn(
                        icon = "📡",
                        title = "Google Chat",
                        sub = "Zero-Custody Bridge",
                        accent = Color(0xFF34D399),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_btn_gchat"),
                        onClick = onNavigateToChatRelay
                    )
                }
            }
        }

        // 2. Priority SLA Ticket Generator with Severity Triage & 1-Tap Autofill
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PRIORITY SLA TICKET GENERATOR",
                            color = AsgardLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            "Select severity level and auto-fill saved client profile",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }

                    // 1-Tap Profile Autofill Button
                    OutlinedButton(
                        onClick = {
                            val saved = ClientQolManager.getSavedProfile(context)
                            if (saved.hasData) {
                                clientName = saved.clientName
                                businessName = saved.businessName
                                clientEmail = saved.email
                                clientPhone = saved.phone
                                Toast.makeText(context, "Auto-filled from Saved Client Profile!", Toast.LENGTH_SHORT).show()
                            } else if (clientName.isNotBlank() || clientEmail.isNotBlank() || clientPhone.isNotBlank()) {
                                ClientQolManager.saveProfile(
                                    context,
                                    SavedClientProfile(
                                        clientName = clientName,
                                        businessName = businessName,
                                        email = clientEmail,
                                        phone = clientPhone
                                    )
                                )
                                Toast.makeText(context, "Saved current details as your Client Profile!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Enter your contact info once to save for 1-tap autofill.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, AsgardLight),
                        modifier = Modifier.testTag("support_autofill_profile_btn")
                    ) {
                        Icon(Icons.Default.PersonPin, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1-Tap Autofill", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                    }
                }

                // Severity Triage Grid
                SlaSeverityTier.entries.forEach { tier ->
                    val isSel = selectedSeverity == tier
                    Surface(
                        color = if (isSel) tier.color.copy(alpha = 0.14f) else BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) tier.color else BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSeverity = tier }
                            .testTag("sla_tier_${tier.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tier.title,
                                    color = if (isSel) tier.color else TextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    tier.description,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = BgCard,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, tier.color.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    tier.responseTarget,
                                    color = tier.color,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Category Chips
                Text("Issue / Request Category:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                SimpleFlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 6.dp,
                    verticalSpacing = 6.dp
                ) {
                    categories.forEach { cat ->
                        val isSel = issueCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { issueCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                                selectedLabelColor = AsgardLight,
                                containerColor = BgAttr,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Contact Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Your Name *", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_name_input")
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Company / Org", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Email Address", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_email_input")
                    )
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Phone Number", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = issueSummary,
                    onValueChange = { issueSummary = it },
                    label = { Text("Describe the issue, SKU, or support request *", fontSize = 10.sp) },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_summary_input")
                )

                // Generate & Dispatch Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() || clientEmail.isNotBlank() || clientPhone.isNotBlank()) {
                                ClientQolManager.saveProfile(
                                    context,
                                    SavedClientProfile(
                                        clientName = clientName,
                                        businessName = businessName,
                                        email = clientEmail,
                                        phone = clientPhone
                                    )
                                )
                            }
                            val ticketText = buildFormattedTicket()
                            generatedTicketReceipt = ticketText
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${TyrCatalog.email}".toUri()
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "[${selectedSeverity.code}] TYR SLA Support: $issueCategory"
                                )
                                putExtra(Intent.EXTRA_TEXT, ticketText)
                            }
                            safeStartActivity(context, emailIntent, fallbackCopyText = ticketText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_dispatch_sla_ticket"),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch SLA Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val ticketText = buildFormattedTicket()
                            generatedTicketReceipt = ticketText
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("TYR SLA Ticket", ticketText))
                            Toast.makeText(context, "SLA Ticket receipt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AsgardLight),
                        modifier = Modifier.testTag("btn_copy_sla_ticket")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Receipt", fontSize = 11.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                    }
                }

                if (generatedTicketReceipt != null) {
                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "✓ GENERATED SLA RECEIPT (SHA-384 VERIFIED)",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                generatedTicketReceipt ?: "",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Client Support FAQ Accordion
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "FREQUENTLY ASKED SUPPORT & SLA QUESTIONS",
                    color = AsgardLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )

                faqItems.forEachIndexed { idx, faq ->
                    val expanded = expandedFaqIndex == idx
                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (expanded) AsgardLight.copy(alpha = 0.5f) else BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFaqIndex = if (expanded) -1 else idx }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    faq.question,
                                    color = if (expanded) AsgardLight else TextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = AsgardLight
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        faq.answer,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportQuickChannelBtn(
    icon: String,
    title: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 18.sp)
            Column {
                Text(title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(sub, color = accent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
