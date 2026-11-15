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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

data class PrivacyPolicySection(
    val id: String,
    val badge: String,
    val title: String,
    val scopeTag: String,
    val bullets: List<String>
)

@Composable
fun UniversalPrivacyPolicyView(
    onNavigateToSupport: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedScopeFilter by remember { mutableStateOf("ALL") }

    val canonicalPolicyUrl = "https://tyrprotocol.pro/privacy"
    val effectiveDate = "November 13, 2026"

    val sections = remember {
        listOf(
            PrivacyPolicySection(
                id = "scope-universal",
                badge = "🌐",
                title = "1. Universal Scope & Covered Applications",
                scopeTag = "UNIVERSAL",
                bullets = listOf(
                    "This Master Privacy Policy governs all digital properties, mobile applications, web storefronts, and software engineering services operated by Caleb TR Holcomb d/b/a TYR Technical Consulting ('TYR', 'we', 'our').",
                    "Covered Platforms: (a) The TYR Protocol Business Storefront Website (https://tyrprotocol.pro and associated subdomains), (b) The TYR Protocol Android & Google Play Store Application (package ID: pro.tyrprotocol.storefront and related mobile releases), and (c) Custom software development, scripts, automation pipelines, and technical consulting engagements.",
                    "Jurisdiction & Entity: Operated from Northwest Alabama with 100% remote digital delivery nationwide across the United States (NAICS 541512 & 541611)."
                )
            ),
            PrivacyPolicySection(
                id = "zero-custody",
                badge = "🛡️",
                title = "2. Zero-Custody Covenant & Day-0 Financial Data Purge",
                scopeTag = "CONSULTING",
                bullets = listOf(
                    "Zero Raw Financial Data Stored: TYR Technical Consulting does NOT maintain a persistent database of client bank statements, payment card numbers (PAN), Social Security Numbers (SSN), bank routing numbers, or raw authentication credentials.",
                    "Day-0 RAM Purge: When clients provide billing statements or logs for expense audits, ISP dispute dossiers, or security reviews, files are processed strictly in volatile memory (RAM) to extract anonymized ledger totals and are cryptographically purged on Day 0 upon deliverable handover.",
                    "Pre-Transmission DLP Scrubbing: In-app tools (including the Zero-Custody Sanitizer and Google Chat Relay) automatically redact credit card numbers, SSNs, EINs, bank account numbers, and secret tokens before any message leaves your device."
                )
            ),
            PrivacyPolicySection(
                id = "google-play-mobile",
                badge = "📱",
                title = "3. Google Play Store & Mobile Application Data Safety",
                scopeTag = "MOBILE_APP",
                bullets = listOf(
                    "Least-Privilege Permissions: Our Android applications request only standard network connectivity (android.permission.INTERNET). We NEVER request invasive runtime permissions such as Camera, Microphone, Fine/Coarse GPS Location, Read Contacts, SMS/Call Logs, or External Storage access.",
                    "Local-First Preferences: Saved client autofill profiles, custom quote drafts, accessibility text-scaling settings, and referral link customizations are stored exclusively on your device using Android local SharedPreferences. You can wipe your local profile at any time with a single tap.",
                    "Native System Intents: Phone calls (tel:), text messages (smsto:), emails (mailto:), and contact card saves (.vcf) are handed off to your device's native system applications only when you explicitly tap an action button.",
                    "Zero Third-Party Ad SDKs: Our applications contain zero third-party advertising networks, behavioral trackers, or cross-app tracking identifiers."
                )
            ),
            PrivacyPolicySection(
                id = "web-storefront",
                badge = "🖥️",
                title = "4. Business Storefront Website & Cookie Policy",
                scopeTag = "WEB_STOREFRONT",
                bullets = listOf(
                    "Static Zero-Bloat Architecture: Our web storefront is engineered as a high-security static application without invasive third-party tracking pixels, behavioral profiling cookies, or data broker scripts.",
                    "Voluntary Client Intake: Information you enter into project intake forms, quote builders, or digital contract e-signature pads is used solely to generate your requested Statement of Work (SOW), cryptographic SHA-384 execution certificate, or support ticket.",
                    "TLS & Cryptographic Integrity: All web and API communications are encrypted in transit via HTTPS/TLS, and executed SOW certificates use deterministic SHA-384 cryptographic digests."
                )
            ),
            PrivacyPolicySection(
                id = "ai-development",
                badge = "✨",
                title = "5. AI Features, Firebase App Check & Custom Development",
                scopeTag = "AI_DEV",
                bullets = listOf(
                    "Firebase AI Logic & App Check: Optional in-app AI consultation and prompt synthesis features use Google Firebase AI Logic protected by Firebase App Check device attestation. No raw client financial records are used to train public AI models.",
                    "Offline Vault Fallback: Users may toggle off cloud AI synthesis at any time to use the 100% offline local vault with zero network transmission.",
                    "Custom Client Development: All custom code, scripts, and architectures built for clients under a signed SOW become 100% client property upon final payment, with zero backdoors, telemetry beacons, or developer lock-in."
                )
            ),
            PrivacyPolicySection(
                id = "referral-affiliate",
                badge = "🤝",
                title = "6. Referral & Affiliate Links Disclosure",
                scopeTag = "WEB_STOREFRONT",
                bullets = listOf(
                    "External Partner Links: The Partner & Referral Link Vault contains curated and user-customizable referral links to external third-party platforms (such as SoFi Invest, credit monitoring services, digital asset platforms, and security hardware).",
                    "Independent Third-Party Terms: When you tap an external referral link, you leave the TYR application and interact directly with that third-party provider under their respective privacy policy and terms of service. TYR never collects or sees your login or account activity on third-party platforms."
                )
            ),
            PrivacyPolicySection(
                id = "user-rights-contact",
                badge = "⚖️",
                title = "7. Your Privacy Rights (GDPR / CCPA) & Data Deletion",
                scopeTag = "UNIVERSAL",
                bullets = listOf(
                    "Right to Access, Correct & Delete: You have the right to request confirmation of any active work order records, request immediate erasure of intake correspondence, or clear all local app data on your device.",
                    "Children's Privacy (COPPA): Our B2B and commercial consulting services and applications are intended for adults and business operators (18+) and do not knowingly collect data from children under 13.",
                    "Direct Privacy Officer Contact: Caleb TR Holcomb d/b/a TYR Technical Consulting • Email: tyr@tyrprotocol.pro • Phone: +1 (659) 257-8043 • Region: Northwest Alabama & Nationwide Remote."
                )
            )
        )
    }

    val filteredSections = remember(selectedScopeFilter, sections) {
        if (selectedScopeFilter == "ALL") sections
        else sections.filter { it.scopeTag == selectedScopeFilter || it.scopeTag == "UNIVERSAL" }
    }

    fun generateMarkdownPolicy(): String = buildString {
        appendLine("# MASTER PRIVACY POLICY — TYR TECHNICAL CONSULTING")
        appendLine("**Effective Date:** $effectiveDate")
        appendLine("**Canonical Policy URL:** $canonicalPolicyUrl")
        appendLine("**Entity:** Caleb TR Holcomb d/b/a TYR Technical Consulting (NAICS 541512 & 541611)")
        appendLine("**Covered Platforms:** Business Storefront Website, Android & Google Play Apps (`pro.tyrprotocol.storefront`), and Custom Software / AI Development")
        appendLine()
        sections.forEach { sec ->
            appendLine("## ${sec.title}")
            sec.bullets.forEach { b ->
                appendLine("- $b")
            }
            appendLine()
        }
        appendLine("---")
        appendLine("Contact: tyr@tyrprotocol.pro | +1 (659) 257-8043 | Northwest Alabama & Nationwide Remote")
    }

    fun generateHtmlPolicy(): String = buildString {
        appendLine("<!DOCTYPE html>")
        appendLine("<html lang=\"en\">")
        appendLine("<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">")
        appendLine("<title>Privacy Policy | TYR Technical Consulting</title>")
        appendLine("<style>body{font-family:system-ui,-apple-system,sans-serif;background:#07070b;color:#f8fafc;max-width:820px;margin:0 auto;padding:24px;line-height:1.6}h1,h2{color:#fbbf24}section{background:#13141c;border:1px solid #262936;border-radius:10px;padding:16px;margin-bottom:16px}</style>")
        appendLine("</head><body>")
        appendLine("<h1>Master Privacy Policy — TYR Technical Consulting</h1>")
        appendLine("<p><strong>Effective Date:</strong> $effectiveDate<br>")
        appendLine("<strong>Entity:</strong> Caleb TR Holcomb d/b/a TYR Technical Consulting (NAICS 541512 &amp; 541611)<br>")
        appendLine("<strong>Applies To:</strong> Storefront Website (tyrprotocol.pro), Android App (pro.tyrprotocol.storefront), and Custom Software / AI Development.</p>")
        sections.forEach { sec ->
            appendLine("<section><h2>${escapeHtmlEntities(sec.title)}</h2><ul>")
            sec.bullets.forEach { b ->
                appendLine("<li>${escapeHtmlEntities(b)}</li>")
            }
            appendLine("</ul></section>")
        }
        appendLine("<footer><p>Contact: <a href=\"mailto:tyr@tyrprotocol.pro\" style=\"color:#38bdf8\">tyr@tyrprotocol.pro</a> | +1 (659) 257-8043 | Northwest Alabama &amp; Nationwide Remote</p></footer>")
        appendLine("</body></html>")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
            .testTag("universal_privacy_policy_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & 1-Tap URL / Markdown / HTML Export Suite
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(VanaheimLight, Color(0xFF38BDF8), AsgardLight))),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F766E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Universal Master Privacy Policy",
                            color = TextMain,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            "Covers Website, Google Play App & Custom Development",
                            color = VanaheimLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    "Generalized statutory privacy policy engineered to satisfy Google Play Console Privacy Policy URL requirements, storefront website compliance, OAuth consent screens, and B2B vendor onboarding.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Canonical URL Box
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, VanaheimLight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Privacy Policy URL", canonicalPolicyUrl))
                            Toast.makeText(context, "Copied Privacy Policy URL: $canonicalPolicyUrl", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("copy_privacy_url_box")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CANONICAL PRIVACY POLICY URL (FOR PLAY CONSOLE & WEB)", color = VanaheimLight, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                canonicalPolicyUrl,
                                color = TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Policy URL", tint = VanaheimLight, modifier = Modifier.size(16.dp))
                    }
                }

                // 3 Export Buttons: Copy URL, Copy Markdown, Copy Standalone HTML
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Privacy Policy URL", canonicalPolicyUrl))
                            Toast.makeText(context, "Copied Policy URL!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_privacy_url"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Copy URL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val md = generateMarkdownPolicy()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Privacy Policy Markdown", md))
                            Toast.makeText(context, "Copied full Markdown Privacy Policy!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_privacy_markdown"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AsgardLight),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Copy Markdown", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val html = generateHtmlPolicy()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Privacy Policy HTML", html))
                            Toast.makeText(context, "Copied standalone HTML Privacy Policy!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_privacy_html"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Copy HTML", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Scope Filter Pills
        SimpleFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 6.dp,
            verticalSpacing = 6.dp
        ) {
            listOf(
                "ALL" to "All Clauses (7)",
                "MOBILE_APP" to "📱 Android & Play Store",
                "WEB_STOREFRONT" to "🌐 Website & Referrals",
                "CONSULTING" to "🛡️ Zero-Custody Audits",
                "AI_DEV" to "✨ AI & Custom Dev"
            ).forEach { (key, label) ->
                val isSel = selectedScopeFilter == key
                FilterChip(
                    selected = isSel,
                    onClick = { selectedScopeFilter = key },
                    label = { Text(label, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                        selectedLabelColor = AsgardLight,
                        containerColor = BgCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // 3. Policy Clauses Cards
        filteredSections.forEach { sec ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(sec.badge, fontSize = 16.sp)
                        Text(
                            sec.title,
                            color = AsgardLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    sec.bullets.forEach { bullet ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = VanaheimLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                bullet,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Interactive Data Deletion & Privacy Officer Dispatch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgAttr),
            border = BorderStroke(1.dp, VanaheimLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "DATA DELETION & PRIVACY RIGHTS DISPATCH",
                    color = VanaheimLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    "Need to exercise a GDPR/CCPA data deletion right, verify a 0-Day RAM purge certificate, or contact our Privacy Officer?",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${TyrCatalog.email}".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Privacy Rights / Data Deletion Request — TYR Protocol")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Hello Caleb TR Holcomb (Privacy Officer),\n\nI am submitting a formal privacy / data deletion request regarding:\n"
                                )
                            }
                            safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.email)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Privacy Officer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToVault,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, VanaheimLight)
                    ) {
                        Text("Open 0-Day Purge Vault", fontSize = 11.sp, color = VanaheimLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
