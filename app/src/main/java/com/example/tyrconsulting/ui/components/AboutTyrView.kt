package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun AboutTyrView(
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToCard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTriadIndex by remember { mutableIntStateOf(0) }

    val triadPillars = listOf(
        Triple(
            "ᛏ Týr — The Law & Backbone",
            "Radical Honesty, Kept Oaths & Technical Accountability",
            "Anchored by Teiwaz (ᛏ), TYR Protocol faces technical bottlenecks and vendor bloat directly without excuses. Every engagement is governed by a clear, flat-rate Statement of Work (SOW) with zero surprise billing and verifiable deliverables."
        ),
        Triple(
            "ᚠ Freyja — The Vital Flame & Sovereignty",
            "Sacred Client Boundaries, Zero-Custody & 100% Ownership",
            "Clients retain 100% sovereign ownership of their domain names, source code, master documents, and financial data. Raw statements and credentials are held strictly in volatile memory during audit extraction and purged on Day 0."
        ),
        Triple(
            "ᚲ Loki — The Catalyst & Tactical Alternative",
            "Breaking Vendor Lock-In, Frugal Utility & Resilient Architecture",
            "Refusing predatory SaaS rent-seeking and fragile defaults (such as lock-out prone TPM/firmware-bound drive locks), TYR engineers $0/month static web architectures, transparent user-controlled encryption (VeraCrypt / Cryptomator / 7-Zip AES-256), and SHA-384 integrity."
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
            .testTag("about_tyr_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Executive Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(AsgardLight, MidgardMain, VanaheimLight))),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(MidgardMain, Color(0xFF7F1D1D)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ᛏ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TYR Technical Consulting",
                            color = TextMain,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            "Caleb TR Holcomb d/b/a TYR • Lead Systems Architect",
                            color = AsgardLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Text(
                            "📍 Northwest Alabama & Nationwide Remote Delivery",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                Text(
                    "TYR Technical Consulting is an independent systems architecture, cybersecurity hardening, and administrative engineering practice founded by Caleb TR Holcomb. Built on first-principles logic and telemetry-driven root-cause analysis, we equip Main Street businesses, subcontractors, and commercial enterprises with resilient digital infrastructure that they own 100% outright.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToCatalog,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore 18 Services", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToCard,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AsgardLight)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3D Business Card", fontSize = 11.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Interactive Indivisible Patron Triad Ethos
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
                Text(
                    "THE INDIVISIBLE PATRON TRIAD & ENGINEERING ETHOS",
                    color = AsgardLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ᛏ Týr", "ᚠ Freyja", "ᚲ Loki").forEachIndexed { idx, label ->
                        val selected = selectedTriadIndex == idx
                        FilterChip(
                            selected = selected,
                            onClick = { selectedTriadIndex = idx },
                            label = {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                                selectedLabelColor = AsgardLight,
                                containerColor = BgAttr,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) AsgardLight else BorderSubtle
                            )
                        )
                    }
                }

                val currentPillar = triadPillars[selectedTriadIndex]
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            currentPillar.first,
                            color = AsgardLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            currentPillar.second,
                            color = VanaheimLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            currentPillar.third,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 3. Verified Federal & Commercial Procurement Credentials
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
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
                    Text(
                        "COMMERCIAL & PROCUREMENT CREDENTIALS",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                    TextButton(
                        onClick = {
                            val creds = """
                                Entity: TYR Technical Consulting (Caleb TR Holcomb d/b/a TYR)
                                Primary NAICS: 541512 (Computer Systems Design Services)
                                Secondary NAICS: 541611 (Administrative & General Management Consulting)
                                Federal EIN: ${TyrCatalog.ein}
                                Service Area: Northwest Alabama & Nationwide Remote
                                Contact: ${TyrCatalog.email} | ${TyrCatalog.phone}
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("TYR Credentials", creds))
                            Toast.makeText(context, "Vendor procurement credentials copied!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Vendor Sheet", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }

                CredentialRowItem("Operating Entity", "Caleb TR Holcomb d/b/a TYR")
                CredentialRowItem("Primary NAICS Code", "541512 — Computer Systems Design Services")
                CredentialRowItem("Secondary NAICS Code", "541611 — Admin & General Management Consulting")
                CredentialRowItem("Federal Tax ID (EIN)", TyrCatalog.ein)
                CredentialRowItem("Jurisdiction & Scope", "Northwest Alabama & Nationwide Remote (US)")
                CredentialRowItem("Cryptographic Standard", "Argon2id + SHA-384 Integrity Verification")
            }
        }

        // 4. Core Engineering & Client Sovereignty Guarantees
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
                Text(
                    "FOUR PILLARS OF CLIENT SOVEREIGNTY",
                    color = VanaheimLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )

                SovereigntyGuaranteeItem(
                    icon = "🛡️",
                    title = "Zero-Custody Day-0 Data Purge",
                    desc = "Raw bank statements, ISP bills, and credentials are never stored in persistent databases. All sensitive working files are scrubbed and purged from RAM on Day 0 upon deliverable handover."
                )
                SovereigntyGuaranteeItem(
                    icon = "⚡",
                    title = "$0/Month Hostage-Free Web & Docs",
                    desc = "We build self-contained static web architectures and open-format operational vaults (.md, .docx, .xlsx, .pdf, .sh, .ps1) so you never pay recurring rent just to keep your site online."
                )
                SovereigntyGuaranteeItem(
                    icon = "🔐",
                    title = "Non-Lockout Sovereign Encryption",
                    desc = "We strictly prohibit lock-out prone TPM/firmware-bound drive locks in favor of transparent, user-controlled encryption (VeraCrypt detached headers, Cryptomator, 7-Zip AES-256) with multi-path recovery."
                )
                SovereigntyGuaranteeItem(
                    icon = "⚖️",
                    title = "Crystal-Clear 1-Time vs. Monthly Billing",
                    desc = "Every SOW explicitly separates one-time capital builds ($0/mo forever) from optional month-to-month maintenance retainers—eliminating billing ambiguity."
                )
            }
        }

        // 5. Bottom Navigation to Support & Universal Privacy Policy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToSupport,
                modifier = Modifier
                    .weight(1f)
                    .testTag("about_to_support_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AsgardMain),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Support & SLA Center", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToPrivacy,
                modifier = Modifier
                    .weight(1f)
                    .testTag("about_to_privacy_btn"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VanaheimLight)
            ) {
                Icon(Icons.Default.Policy, contentDescription = null, tint = VanaheimLight, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Privacy Policy Hub", fontSize = 11.sp, color = VanaheimLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CredentialRowItem(label: String, value: String) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(0.9f))
            Text(
                value,
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1.3f)
            )
        }
    }
}

@Composable
private fun SovereigntyGuaranteeItem(icon: String, title: String, desc: String) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(icon, fontSize = 18.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(desc, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}
