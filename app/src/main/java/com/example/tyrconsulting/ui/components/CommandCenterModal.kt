package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tyrconsulting.ui.theme.*

data class NavigationDestinationItem(
    val tabIndex: Int,
    val icon: String,
    val title: String,
    val category: String,
    val description: String,
    val badge: String,
    val badgeColor: Color
)

@Composable
fun CommandCenterModal(
    onDismiss: () -> Unit,
    onSelectDestination: (Int) -> Unit,
    cartItemCount: Int,
    onCallConsult: () -> Unit,
    recentTabs: List<Int> = listOf(0, 1, 25, 26),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val allDestinations = remember(cartItemCount) {
        listOf(
            // Pillar 1: Storefront Catalog
            NavigationDestinationItem(
                tabIndex = 1,
                icon = "🌐",
                title = "Page 1: Web & Domain Identity",
                category = "CATALOG",
                description = "Custom single-page websites, custom email setup, and QR intake forms ($0/mo hosting).",
                badge = "⚡ $0/Mo Ever",
                badgeColor = AsgardLight
            ),
            NavigationDestinationItem(
                tabIndex = 2,
                icon = "⚖️",
                title = "Page 2: Legal, Resumes & Buying Guides",
                category = "CATALOG",
                description = "Subcontractor agreements, executive resumes, and technical hardware buyer research.",
                badge = "⚡ One-Time Build",
                badgeColor = AlfheimLight
            ),
            NavigationDestinationItem(
                tabIndex = 3,
                icon = "🛡️",
                title = "Page 3: Cold Vault & Expense Ledger",
                category = "CATALOG",
                description = "Offline wealth ledgers, encrypted cold backups, and ISP dispute dossiers.",
                badge = "⚡ Zero-Custody",
                badgeColor = VanaheimLight
            ),
            NavigationDestinationItem(
                tabIndex = 4,
                icon = "🔧",
                title = "Page 4: Technical Care Plans",
                category = "CATALOG",
                description = "Monthly website maintenance, domain expiration watch, and document care ($55–$95/mo).",
                badge = "🔄 Month-to-Month",
                badgeColor = Color(0xFF34D399)
            ),
            NavigationDestinationItem(
                tabIndex = 5,
                icon = "🔍",
                title = "Page 5: Financial Statement Audits",
                category = "CATALOG",
                description = "Ephemeral monthly bank statement audits and utility bill watchdog Defense ($75–$115/mo).",
                badge = "🔄 Ephemeral Audit",
                badgeColor = Color(0xFF34D399)
            ),
            NavigationDestinationItem(
                tabIndex = 6,
                icon = "🚀",
                title = "Page 6: Pure Remote Deliverables",
                category = "CATALOG",
                description = "SOP Vaults, workstation hardening, Schedule C ledgers (Zero licenses or certs needed).",
                badge = "⚡ Turnkey Digital",
                badgeColor = VanaheimLight
            ),
            NavigationDestinationItem(
                tabIndex = 7,
                icon = "🏢",
                title = "Page 7: B2B Procurement & MSA Suite",
                category = "CATALOG",
                description = "Master Services Agreements, corporate anti-phishing wire SOPs, and vendor packets.",
                badge = "⚡ Enterprise Ready",
                badgeColor = Color(0xFF60A5FA)
            ),
            NavigationDestinationItem(
                tabIndex = 8,
                icon = "🔄",
                title = "Page 8: Automated B2B Retainers",
                category = "CATALOG",
                description = "Scripted external security recon, SaaS spend watchdog, and 24/7 uptime monitoring.",
                badge = "🔄 High-Margin MRR",
                badgeColor = Color(0xFF34D399)
            ),
            NavigationDestinationItem(
                tabIndex = 9,
                icon = "✨",
                title = "All Realms Master Explorer",
                category = "CATALOG",
                description = "Search all nine realms and services with interactive category chips.",
                badge = "Full Directory",
                badgeColor = TextMuted
            ),

            // Pillar 2: Pricing & Quotes
            NavigationDestinationItem(
                tabIndex = 20,
                icon = "🛠️",
                title = "Custom Quote Builder (Unlisted Tasks)",
                category = "PRICING",
                description = "Plug and play unlisted tasks, automation scripts, and bespoke scopes to build exact quotes.",
                badge = "Bespoke Builder",
                badgeColor = Color(0xFFD97706)
            ),
            NavigationDestinationItem(
                tabIndex = 10,
                icon = "🧮",
                title = "Estimate Calculator & Cart",
                category = "PRICING",
                description = "Live estimate with automatic dual-bucket split between one-time builds and monthly fees.",
                badge = "Live Cart ($cartItemCount)",
                badgeColor = MidgardLight
            ),
            NavigationDestinationItem(
                tabIndex = 21,
                icon = "💳",
                title = "Payment Clarity & Billing Defense",
                category = "PRICING",
                description = "Transparent distinction between 1-time fixed deliverables and monthly retainers to prevent disputes.",
                badge = "Zero Ambiguity",
                badgeColor = Color(0xFF34D399)
            ),

            // Pillar 3: Client Operations
            NavigationDestinationItem(
                tabIndex = 11,
                icon = "📝",
                title = "Client Intake Onboarding",
                category = "OPERATIONS",
                description = "Submit formal work orders, project requirements, and execution details.",
                badge = "Direct Intake",
                badgeColor = MidgardMain
            ),
            NavigationDestinationItem(
                tabIndex = 12,
                icon = "📋",
                title = "Live Work Order Tracker",
                category = "OPERATIONS",
                description = "Monitor submitted deliverables, milestones, and project execution status.",
                badge = "Order Ledger",
                badgeColor = AsgardLight
            ),
            NavigationDestinationItem(
                tabIndex = 16,
                icon = "✍️",
                title = "Digital Contract E-Sign",
                category = "OPERATIONS",
                description = "Legally binding UETA/ESIGN digital Statement of Work execution with cryptographic audit blocks.",
                badge = "UETA Compliant",
                badgeColor = Color(0xFF10B981)
            ),
            NavigationDestinationItem(
                tabIndex = 17,
                icon = "🛡️",
                title = "Zero-Custody Vault & Privacy",
                category = "OPERATIONS",
                description = "Cryptographic proof that client raw statements and credentials are purged Day 0 upon completion.",
                badge = "Day-0 Purge",
                badgeColor = Color(0xFF14B8A6)
            ),

            // Pillar 4: Intelligence & Channels
            NavigationDestinationItem(
                tabIndex = 13,
                icon = "💬",
                title = "Gemini AI Systems Advisor",
                category = "INTELLIGENCE",
                description = "Interactive systems consulting chat to scope architectures, compare services, and draft SOWs.",
                badge = "Server-Side AI",
                badgeColor = AsgardLight
            ),
            NavigationDestinationItem(
                tabIndex = 18,
                icon = "📅",
                title = "Schedule Intake Consultation",
                category = "INTELLIGENCE",
                description = "Book a direct 1-on-1 technical discovery call with Caleb Holcomb (Northwest Alabama & Nationwide).",
                badge = "Direct Calendar",
                badgeColor = AsgardLight
            ),
            NavigationDestinationItem(
                tabIndex = 19,
                icon = "🤝",
                title = "Partner & Referral Link Vault",
                category = "INTELLIGENCE",
                description = "SoFi Invest, Credit Monitoring, Crypto, Security, and TYR referral links with custom link builder.",
                badge = "Multi-Partner Vault",
                badgeColor = Color(0xFF059669)
            ),
            NavigationDestinationItem(
                tabIndex = 15,
                icon = "📡",
                title = "Google Chat Relay",
                category = "INTELLIGENCE",
                description = "Direct webhook bridge relaying mission-critical alerts to private team rooms.",
                badge = "Webhook Bridge",
                badgeColor = Color(0xFF0284C7)
            ),
            NavigationDestinationItem(
                tabIndex = 14,
                icon = "🎨",
                title = "Norse Image Forge",
                category = "INTELLIGENCE",
                description = "On-demand heroic visual assets inspired by Týr, Freyja, and Norse mythology.",
                badge = "Visual Forge",
                badgeColor = Color(0xFFD97706)
            ),
            NavigationDestinationItem(
                tabIndex = 0,
                icon = "🪪",
                title = "3D Interactive Business Card",
                category = "INTELLIGENCE",
                description = "Full 3D gyroscopic sensory card with contact vCard, NAICS credentials, and bio.",
                badge = "Interactive 3D",
                badgeColor = Color(0xFF9333EA)
            ),
            NavigationDestinationItem(
                tabIndex = 22,
                icon = "🏛️",
                title = "About TYR & Principal Architect",
                category = "OPERATIONS",
                description = "Principal Systems Architect bio, Indivisible Patron Triad ethos, NAICS 541512/541611 credentials.",
                badge = "Sovereign Bio",
                badgeColor = Color(0xFF7C3AED)
            ),
            NavigationDestinationItem(
                tabIndex = 23,
                icon = "🛟",
                title = "Support & SLA Contact Center",
                category = "OPERATIONS",
                description = "Priority P1-P4 SLA ticket generator, 4-channel direct dispatch, live status, and interactive FAQ.",
                badge = "SLA Support",
                badgeColor = Color(0xFF38BDF8)
            ),
            NavigationDestinationItem(
                tabIndex = 24,
                icon = "📜",
                title = "Universal Privacy Policy Hub",
                category = "OPERATIONS",
                description = "Generalized privacy policy for Storefront Web, Google Play App, and Custom Dev with URL/MD/HTML export.",
                badge = "Play & Web URL",
                badgeColor = Color(0xFF34D399)
            ),
            NavigationDestinationItem(
                tabIndex = 25,
                icon = "💳",
                title = "Stripe Checkout & Digital Receipts Vault",
                category = "PRICING",
                description = "PCI-DSS v4.0 tokenized Stripe checkout, deposit split calculator, SKU Price ID mapper, and Schedule C MD/CSV exports.",
                badge = "Stripe Connector",
                badgeColor = Color(0xFFA5B4FC)
            ),
            NavigationDestinationItem(
                tabIndex = 26,
                icon = "🔬",
                title = "Field Forensics, EXIF Scrubber & Telemetry",
                category = "OPERATIONS",
                description = "Local SHA-384 file/photo integrity hasher, 1-tap EXIF/GPS scrubber, Intake->SOW->Stripe pipeline, and RCA telemetry builder.",
                badge = "SHA-384 Toolkit",
                badgeColor = Color(0xFF34D399)
            ),
            NavigationDestinationItem(
                tabIndex = 27,
                icon = "🚀",
                title = "$0 Growth, Quick Audit & QR Studio",
                category = "INTELLIGENCE",
                description = "60s Quick Audit quiz, one-tap Nextdoor/FB/SMS pitches, printable QR flyers & pocket cards, NW Alabama case studies, and GBP checklist.",
                badge = "$0 Marketing",
                badgeColor = Color(0xFFFBBF24)
            )
        )
    }

    val filteredDestinations = remember(searchQuery, selectedCategoryFilter, allDestinations) {
        allDestinations.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == "ALL" || item.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = BgSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("command_center_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .background(
                                Brush.linearGradient(listOf(MidgardMain, AsgardMain)),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧭", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "GROUPED CATEGORY DRAWER & QUICK SWITCHER",
                            color = AsgardLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Instant 1-Tap Access Across All 28 Operational Capabilities",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_command_center_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            // Recent Tabs Quick-Switcher Strip
            if (recentTabs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⏱️ Recent:", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    recentTabs.take(4).forEach { tabIdx ->
                        val dest = allDestinations.find { it.tabIndex == tabIdx }
                        if (dest != null) {
                            Surface(
                                onClick = {
                                    onSelectDestination(dest.tabIndex)
                                    onDismiss()
                                },
                                color = BgAttr,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text(
                                    "${dest.icon} ${dest.title.take(14)}",
                                    color = TextMain,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AsgardLight) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                placeholder = { Text("Search services, forensics, Stripe, intake, vault...", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.fillMaxWidth().testTag("command_center_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AsgardLight,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = BgAttr,
                    unfocusedContainerColor = BgAttr,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val categories = listOf(
                    "ALL" to "All (${allDestinations.size})",
                    "CATALOG" to "Catalog",
                    "PRICING" to "Stripe & Quotes",
                    "OPERATIONS" to "Ops & Forensics",
                    "INTELLIGENCE" to "AI & Comms"
                )

                categories.forEach { (catId, catLabel) ->
                    val isSel = selectedCategoryFilter == catId
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = catId },
                        label = {
                            Text(
                                catLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                            selectedLabelColor = AsgardLight,
                            containerColor = BgAttr,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = if (isSel) AsgardLight else BorderSubtle
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }

            // List of Destinations
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredDestinations) { dest ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectDestination(dest.tabIndex)
                                onDismiss()
                            }
                            .testTag("nav_destination_${dest.tabIndex}"),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BgAttr, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dest.icon, fontSize = 18.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dest.title,
                                        color = TextMain,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Surface(
                                        color = dest.badgeColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, dest.badgeColor.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = dest.badge,
                                            color = dest.badgeColor,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = dest.description,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 2
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Go",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (filteredDestinations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No capabilities match '$searchQuery'. Try another search term.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Bottom Fast Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallConsult,
                    modifier = Modifier.weight(1f).testTag("modal_call_consult_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Consult", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).testTag("modal_close_btn"),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text("Close Hub", fontSize = 11.sp, color = TextMain)
                }
            }
        }
        }
    }
}
