package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun PaymentModelsClarityView(
    onNavigateToIntake: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onAddToCart: (ServiceItem) -> Unit = {},
    cartServices: List<ServiceItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 0: Matrix Overview, 1: One-Time Builds, 2: Monthly Subscriptions, 3: SOW Defense & FAQ
    var selectedFilterTab by remember { mutableStateOf(0) }

    // Service selector for SOW Defense Clause Generator
    var selectedDefenseSku by remember { mutableStateOf("SVC-WEB-01") }

    val oneTimeServices = remember {
        TyrCatalog.services.filter { s ->
            s.page in listOf(1, 2, 3, 6, 7) ||
            s.priceSub.contains("One-Time", ignoreCase = true) ||
            s.priceSub.contains("Suite", ignoreCase = true) ||
            s.priceSub.contains("Kit", ignoreCase = true) ||
            s.priceSub.contains("Workbook", ignoreCase = true) ||
            s.priceSub.contains("Templates", ignoreCase = true) ||
            s.priceSub.contains("Dossier", ignoreCase = true) ||
            s.priceSub.contains("Build", ignoreCase = true) ||
            s.priceSub.contains("Governance", ignoreCase = true)
        }
    }

    val monthlyServices = remember {
        TyrCatalog.services.filter { s ->
            s.page in listOf(4, 5, 8) ||
            s.priceSub.contains("Month", ignoreCase = true) ||
            s.priceSub.contains("Mo", ignoreCase = true) ||
            s.priceSub.contains("Retainer", ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Hero Card: Radical Billing Transparency
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_clarity_hero_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(MidgardMain, Color(0xFF10B981)))
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF047857), MidgardMain)),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💳", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "PAYMENT CLARITY & BILLING DEFENSE",
                            color = AsgardLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Zero-Ambiguity Distinction: One-Time Deliverables vs Monthly Retainers",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Plain-English Oath Box
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("billing_oath_box")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("ᛏ", color = Color(0xFF34D399), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(
                                "TÝR'S BILLING OATH — THE 'ZERO SURPRISE' DEFENSE",
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Every scope under Caleb Holcomb d/b/a TYR is strictly defined as either a 100% Owned One-Time Capital Deliverable OR an Active Monthly Retainer. One-time projects never incur recurring charges or surprise renewals. Monthly retainers never cover net-new major builds without an approved SOW. No surprises. No ambiguous billing.",
                            color = TextMain,
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Filter / Segment Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "⚖️ Dual Matrix Overview",
                "⚡ One-Time Builds (${oneTimeServices.size})",
                "🔄 Monthly Retainers (${monthlyServices.size})",
                "🛡️ SOW Defense & FAQs"
            )

            tabs.forEachIndexed { index, label ->
                val isSelected = selectedFilterTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterTab = index },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (index) {
                            1 -> AsgardMain.copy(alpha = 0.35f)
                            2 -> Color(0xFF047857).copy(alpha = 0.45f)
                            3 -> Color(0xFF3B82F6).copy(alpha = 0.35f)
                            else -> MidgardMain.copy(alpha = 0.35f)
                        },
                        selectedLabelColor = Color.White,
                        containerColor = BgAttr,
                        labelColor = TextMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AsgardLight else BorderSubtle,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("payment_tab_$index")
                )
            }
        }

        // CONTENT SECTION BASED ON SELECTED TAB
        when (selectedFilterTab) {
            0 -> DualMatrixOverviewSection(
                oneTimeCount = oneTimeServices.size,
                monthlyCount = monthlyServices.size,
                onViewOneTime = { selectedFilterTab = 1 },
                onViewMonthly = { selectedFilterTab = 2 },
                onViewDefense = { selectedFilterTab = 3 }
            )
            1 -> OneTimeServicesListSection(
                services = oneTimeServices,
                cartServices = cartServices,
                onAddToCart = onAddToCart,
                onNavigateToIntake = onNavigateToIntake
            )
            2 -> MonthlyServicesListSection(
                services = monthlyServices,
                cartServices = cartServices,
                onAddToCart = onAddToCart,
                onNavigateToIntake = onNavigateToIntake
            )
            3 -> SowDefenseAndFaqSection(
                selectedSku = selectedDefenseSku,
                onSelectSku = { selectedDefenseSku = it },
                allServices = TyrCatalog.services
            )
        }
    }
}

@Composable
private fun DualMatrixOverviewSection(
    oneTimeCount: Int,
    monthlyCount: Int,
    onViewOneTime: () -> Unit,
    onViewMonthly: () -> Unit,
    onViewDefense: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // High-Contrast Comparison Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Column 1: One-Time
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("one_time_matrix_card"),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = AsgardMain.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "⚡ ONE-TIME FEE",
                            color = AsgardLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        "Capital Deliverable",
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        "• $0.00 / Month Recurring\n• 100% Client Ownership\n• Master Code & PDF Files\n• Zero SaaS / Hostage Fees\n• 50% Deposit / 50% Delivery",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )

                    Button(
                        onClick = onViewOneTime,
                        colors = ButtonDefaults.buttonColors(containerColor = AsgardMain),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("view_one_time_btn")
                    ) {
                        Text("View Builds ($oneTimeCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Column 2: Monthly Recurring
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("monthly_matrix_card"),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFF047857).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                    ) {
                        Text(
                            "🔄 MONTHLY RETAINER",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        "Active Care & Sentry",
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        "• Predictable Monthly Rate\n• Billed 1st of each month\n• Proactive Sentry & Upkeep\n• Scheduled Edits & Audits\n• Cancel anytime (30-day)",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )

                    Button(
                        onClick = onViewMonthly,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("view_monthly_btn")
                    ) {
                        Text("View Care ($monthlyCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Side-by-Side Comparison Rules Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "CONTRACTUAL RULES OF ENGAGEMENT",
                    color = SteelSilver,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                ContractRuleRow(
                    feature = "Billing Trigger",
                    oneTime = "50% upfront, 50% upon final delivery milestone",
                    monthly = "Automated on the 1st of every month"
                )
                HorizontalDivider(color = BorderSubtle)
                ContractRuleRow(
                    feature = "Ongoing Cost",
                    oneTime = "$0.00 / month forever — zero recurring charges",
                    monthly = "Strictly fixed monthly fee ($55 to $195/mo)"
                )
                HorizontalDivider(color = BorderSubtle)
                ContractRuleRow(
                    feature = "Scope Boundary",
                    oneTime = "Rigidly defined in SOW; new features quoted anew",
                    monthly = "Upkeep, scans, & defined edits (not net-new apps)"
                )
                HorizontalDivider(color = BorderSubtle)
                ContractRuleRow(
                    feature = "Cancellation",
                    oneTime = "Complete upon delivery; zero contract to cancel",
                    monthly = "Month-to-month, cancel anytime with 30-day notice"
                )
                HorizontalDivider(color = BorderSubtle)
                ContractRuleRow(
                    feature = "IP & Code",
                    oneTime = "100% master repo & deliverables transferred to client",
                    monthly = "Service reports & ongoing access to active sentry"
                )
            }
        }

        // Callout to SOW Defense Clause Generator
        Surface(
            color = BgAttr,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Need to protect against billing misunderstandings?",
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        "Generate ironclad plain-English billing clauses to stamp directly onto client invoices and quotes.",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                    onClick = onViewDefense,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA))
                ) {
                    Text("SOW Generator", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContractRuleRow(
    feature: String,
    oneTime: String,
    monthly: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(feature, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "⚡ $oneTime",
                color = AsgardLight,
                fontSize = 9.sp,
                lineHeight = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "🔄 $monthly",
                color = Color(0xFF34D399),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OneTimeServicesListSection(
    services: List<ServiceItem>,
    cartServices: List<ServiceItem>,
    onAddToCart: (ServiceItem) -> Unit,
    onNavigateToIntake: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Notice Banner
        Surface(
            color = BgAttr,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚡", fontSize = 16.sp)
                Column {
                    Text(
                        "ONE-TIME FIXED CAPITAL BUILDS — $0.00 / MONTH EVER",
                        color = AsgardLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        "These services are purchased once. You receive 100% completed deliverables with full master code ownership. There are zero recurring monthly charges, zero platform subscriptions, and zero maintenance obligations.",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        services.forEach { service ->
            OneTimeServiceCard(
                service = service,
                isInCart = cartServices.contains(service),
                onAddToCart = { onAddToCart(service) }
            )
        }
    }
}

@Composable
private fun OneTimeServiceCard(
    service: ServiceItem,
    isInCart: Boolean,
    onAddToCart: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("one_time_card_${service.sku}"),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = AsgardMain.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.5f))
                ) {
                    Text(
                        "⚡ ONE-TIME • $0/MO EVER",
                        color = AsgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${service.price} Flat",
                    color = AsgardLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Text(
                text = service.title,
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = service.meaning,
                color = TextSecondary,
                fontSize = 10.sp
            )

            // Bullets
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                service.bullets.take(2).forEach { bullet ->
                    Text(
                        "• $bullet",
                        color = TextMuted,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            // SOW Defense Clause Tag
            Surface(
                color = BgAttr,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📄 SOW Defense Clause: Single capital project. No ongoing subscription fees apply.",
                    color = SteelSilver,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SKU: ${service.sku}",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clause = "[SOW BILLING DEFENSE CLAUSE — SKU ${service.sku}]\n" +
                                    "Project: ${service.title}\n" +
                                    "Billing Type: ONE-TIME FIXED FEE (${service.price})\n" +
                                    "Recurring Obligation: $0.00 / Month (ZERO)\n" +
                                    "Terms: Client acknowledges this is a single milestone deliverable. No recurring subscription fees or monthly charges shall be billed."
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SOW Clause", clause))
                            Toast.makeText(context, "SOW Defense Clause copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Copy Clause", fontSize = 9.sp, color = TextMain)
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInCart) Color(0xFF047857) else MidgardMain
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(28.dp).testTag("add_one_time_${service.sku}")
                    ) {
                        Text(if (isInCart) "✓ Added" else "+ Add", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyServicesListSection(
    services: List<ServiceItem>,
    cartServices: List<ServiceItem>,
    onAddToCart: (ServiceItem) -> Unit,
    onNavigateToIntake: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Notice Banner
        Surface(
            color = BgAttr,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔄", fontSize = 16.sp)
                Column {
                    Text(
                        "MONTHLY SUBSCRIPTION PLANS & RETAINERS",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        "These services are ongoing month-to-month contracts. Invoiced on the 1st of every month for active surveillance, uptime monitors, and scheduled upkeep. You may cancel at any time with 30 days written notice.",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        services.forEach { service ->
            MonthlyServiceCard(
                service = service,
                isInCart = cartServices.contains(service),
                onAddToCart = { onAddToCart(service) }
            )
        }
    }
}

@Composable
private fun MonthlyServiceCard(
    service: ServiceItem,
    isInCart: Boolean,
    onAddToCart: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_card_${service.sku}"),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xFF047857).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                ) {
                    Text(
                        "🔄 RECURRING MONTHLY • 30-DAY CANCEL",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${service.price} / Mo",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Text(
                text = service.title,
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = service.meaning,
                color = TextSecondary,
                fontSize = 10.sp
            )

            // Bullets
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                service.bullets.take(2).forEach { bullet ->
                    Text(
                        "• $bullet",
                        color = TextMuted,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            // Defense Clause
            Surface(
                color = BgAttr,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📄 SOW Defense Clause: Invoiced monthly on the 1st. Does NOT cover net-new major builds without separate SOW.",
                    color = Color(0xFF6EE7B7),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SKU: ${service.sku}",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clause = "[SOW BILLING DEFENSE CLAUSE — SKU ${service.sku}]\n" +
                                    "Service: ${service.title}\n" +
                                    "Billing Type: MONTHLY RECURRING RETAINER (${service.price}/Mo)\n" +
                                    "Invoicing: Billed on the 1st calendar day of each month\n" +
                                    "Scope Limitation: Covers proactive monitoring and scheduled maintenance. Un-scoped major application development is excluded and quoted under a separate one-time SOW.\n" +
                                    "Termination: Client may cancel with 30 days written notice."
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("SOW Clause", clause))
                            Toast.makeText(context, "Monthly SOW Defense Clause copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Copy Clause", fontSize = 9.sp, color = TextMain)
                    }

                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInCart) Color(0xFF047857) else MidgardMain
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(28.dp).testTag("add_monthly_${service.sku}")
                    ) {
                        Text(if (isInCart) "✓ Added" else "+ Add", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SowDefenseAndFaqSection(
    selectedSku: String,
    onSelectSku: (String) -> Unit,
    allServices: List<ServiceItem>
) {
    val context = LocalContext.current
    var expandedDropdown by remember { mutableStateOf(false) }
    val currentService = allServices.find { it.sku == selectedSku } ?: allServices.first()

    val isMonthly = currentService.page in listOf(4, 5, 8) ||
            currentService.priceSub.contains("Month", ignoreCase = true) ||
            currentService.priceSub.contains("Mo", ignoreCase = true) ||
            currentService.priceSub.contains("Retainer", ignoreCase = true)

    val generatedClause = remember(selectedSku) {
        buildString {
            appendLine("==================================================")
            appendLine("TYR TECHNICAL CONSULTING — SOW DEFENSE CLAUSE")
            appendLine("CALEB TR HOLCOMB d/b/a TYR • NAICS 541512 / 541611")
            appendLine("==================================================")
            appendLine("SERVICE SKU: ${currentService.sku}")
            appendLine("NAME: ${currentService.title}")
            if (isMonthly) {
                appendLine("BILLING CLASSIFICATION: [MONTHLY RECURRING RETAINER]")
                appendLine("RATE: ${currentService.price} per month")
                appendLine("BILLING CYCLE: Invoiced on the 1st of each calendar month.")
                appendLine("SCOPE BOUNDARY: Covers scheduled upkeep, monitoring, and stated maintenance quota.")
                appendLine("NON-ASSUMPTION DEFENSE: Client acknowledges this monthly fee does NOT cover net-new major builds, custom app programming, or un-scoped development.")
                appendLine("CANCELLATION: Month-to-month. Client may terminate anytime with 30 days written notice to tyr@tyrprotocol.pro.")
            } else {
                appendLine("BILLING CLASSIFICATION: [ONE-TIME FIXED CAPITAL PURCHASE]")
                appendLine("PRICE: ${currentService.price} Flat Fixed Price")
                appendLine("RECURRING COST: $0.00 / MONTH FOREVER (ZERO ONGOING CHARGES)")
                appendLine("PAYMENT MILESTONES: 50% non-refundable bench retainer upon SOW execution; 50% balance upon final deliverable handover.")
                appendLine("NON-ASSUMPTION DEFENSE: Caleb Holcomb d/b/a TYR will NEVER charge recurring or automatic renewal fees for this purchase. All code and master assets become 100% property of Client upon final payment.")
            }
            appendLine("GOVERNING LAW: State of Alabama / Uniform Commercial Code (UCC)")
            appendLine("==================================================")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Generator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sow_defense_generator_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🛡️", fontSize = 16.sp)
                    Text(
                        "DISPUTE DEFENSE SOW CLAUSE GENERATOR",
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    "Select any catalog service to generate the exact contractual billing clause. Copy and paste into your quotes, proposals, or client emails to legally eliminate any 'I thought that was a one-time fee' defense.",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                // Service Picker Dropdown (Stable M3 Box + OutlinedCard + DropdownMenu)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDropdown = !expandedDropdown }
                            .testTag("sow_sku_picker_card"),
                        colors = CardDefaults.outlinedCardColors(containerColor = BgAttr),
                        border = BorderStroke(1.dp, if (expandedDropdown) AsgardLight else BorderSubtle),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentService.sku} — ${currentService.title} (${currentService.price})",
                                fontSize = 11.sp,
                                color = TextMain,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expandedDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle SKU Dropdown",
                                tint = AsgardLight
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(BgSurface)
                    ) {
                        allServices.forEach { s ->
                            val sIsMonthly = s.page in listOf(4, 5, 8)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${if (sIsMonthly) "🔄" else "⚡"} ${s.sku} • ${s.title}",
                                            fontSize = 11.sp,
                                            color = TextMain
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            s.price,
                                            fontSize = 11.sp,
                                            color = if (sIsMonthly) Color(0xFF34D399) else AsgardLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSku(s.sku)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Preview Box
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = generatedClause,
                        color = if (isMonthly) Color(0xFF6EE7B7) else SteelSilver,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("SOW Defense Clause", generatedClause))
                        Toast.makeText(context, "Full SOW Clause copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("copy_full_sow_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Defense Clause to Clipboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // FAQs for Billing Defense
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "BILLING DISPUTE DEFENSE — FREQUENT QUESTIONS",
                    color = TextMain,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                BillingFaqItem(
                    q = "Will I ever be billed monthly fees for a website or document suite?",
                    a = "Never. Turnkey catalog builds are strictly one-time capital purchases. You receive all master files, raw code, and templates upon final payment. Caleb Holcomb d/b/a TYR will never automatically charge your card or send monthly bills for build projects."
                )
                HorizontalDivider(color = BorderSubtle)
                BillingFaqItem(
                    q = "Does a monthly care plan cover building brand-new features from scratch?",
                    a = "No. Monthly care plans are priced for continuous monitoring, uptime pings, security scans, and scheduled edits (up to the stated allowance). If you need an entirely new web application, database, or document suite, it will be quoted under a separate one-time SOW."
                )
                HorizontalDivider(color = BorderSubtle)
                BillingFaqItem(
                    q = "How do clients cancel an ongoing monthly subscription?",
                    a = "Zero friction. Send a simple 30-day written notice via email (tyr@tyrprotocol.pro) or text to (659) 257-8043. Recurring billing stops cleanly with zero penalty fees or hostage traps."
                )
                HorizontalDivider(color = BorderSubtle)
                BillingFaqItem(
                    q = "Can a client bundle a one-time build with a monthly care plan?",
                    a = "Yes. Many businesses order a One-Time Build (e.g., Website Build at $245) and pair it with an optional Website Care Plan ($55/mo) for hands-off peace of mind. Both line items are separately specified on your invoice so there is zero confusion."
                )
            }
        }
    }
}

@Composable
private fun BillingFaqItem(q: String, a: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("Q: $q", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(a, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
    }
}
