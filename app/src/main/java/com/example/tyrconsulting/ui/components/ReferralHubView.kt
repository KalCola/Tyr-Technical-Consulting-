package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import java.util.Locale
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import com.example.tyrconsulting.model.*
import com.example.tyrconsulting.ui.theme.*
import java.util.UUID

@Composable
fun ReferralHubView(
    onNavigateToIntake: () -> Unit = {},
    onExploreServices: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var metrics by remember { mutableStateOf(ReferralManager.getMetrics(context)) }
    var partnerLinks by remember { mutableStateOf(ReferralManager.getPartnerLinks(context)) }

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var editingLink by remember { mutableStateOf<PartnerReferralLink?>(null) }
    var showCustomCodeDialog by remember { mutableStateOf(false) }
    var customCodeInput by remember { mutableStateOf(metrics.referralCode) }
    var showAnalyticsAccordion by remember { mutableStateOf(false) }

    val filteredLinks = remember(partnerLinks, selectedCategoryFilter, searchQuery) {
        partnerLinks.filter { link ->
            val matchesCat = when (selectedCategoryFilter) {
                "ALL" -> true
                "PINNED" -> link.isPinned
                else -> link.category.name == selectedCategoryFilter
            }
            val matchesQuery = searchQuery.isBlank() ||
                link.title.contains(searchQuery, ignoreCase = true) ||
                link.description.contains(searchQuery, ignoreCase = true) ||
                link.yourReward.contains(searchQuery, ignoreCase = true) ||
                link.friendBonus.contains(searchQuery, ignoreCase = true) ||
                link.category.label.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val totalPartnerClicks = remember(partnerLinks) { partnerLinks.sumOf { it.clicks } }
    val pinnedCount = remember(partnerLinks) { partnerLinks.count { it.isPinned } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
            .testTag("referral_hub_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header Card with Quick Add Link & Master Code Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(VanaheimLight, AsgardLight, MidgardMain))),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(VanaheimMain, Color(0xFF0284C7)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤝", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                "Partner & Referral Link Vault",
                                color = TextMain,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                "SoFi Invest • Credit Monitoring • Crypto • Security • TYR",
                                color = VanaheimLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            editingLink = null
                            showAddOrEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AsgardLight,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_add_custom_referral")
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Link", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Text(
                    "One organized hub for all your personal and business referral links. Tap any card to copy your invite link, share with friends or clients in one tap, or customize the URL and promo code with your exact link.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                // 4-Metric Quick Summary Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReferralSummaryPill(
                        label = "Active Links",
                        value = "${partnerLinks.size}",
                        accentColor = AsgardLight,
                        modifier = Modifier.weight(1f)
                    )
                    ReferralSummaryPill(
                        label = "Pinned ★",
                        value = "$pinnedCount",
                        accentColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                    ReferralSummaryPill(
                        label = "Link Shares",
                        value = "$totalPartnerClicks",
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    ReferralSummaryPill(
                        label = "Master Code",
                        value = metrics.referralCode,
                        accentColor = VanaheimLight,
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable { showCustomCodeDialog = true }
                    )
                }
            }
        }

        // 2. Search & Category Filter Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("referral_search_input"),
                    placeholder = {
                        Text(
                            "Search SoFi, credit monitoring, crypto, security, or bonus...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Links", tint = AsgardLight)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = AsgardLight,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = BgAttr,
                        unfocusedContainerColor = BgAttr
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                val filterTabs = listOf(
                    "ALL" to "All Offers (${partnerLinks.size})",
                    "PINNED" to "★ Pinned ($pinnedCount)",
                    ReferralLinkCategory.FINTECH.name to "💰 FinTech & SoFi",
                    ReferralLinkCategory.CREDIT.name to "🛡️ Credit Monitoring",
                    ReferralLinkCategory.CRYPTO.name to "🪙 Crypto & Web3",
                    ReferralLinkCategory.SECURITY.name to "🔒 Security & Cloud",
                    ReferralLinkCategory.TYR_CONSULTING.name to "ᛏ TYR Storefront"
                )

                SimpleFlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 6.dp,
                    verticalSpacing = 6.dp
                ) {
                    filterTabs.forEach { (catKey, label) ->
                        val isSelected = selectedCategoryFilter == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = catKey },
                            label = {
                                Text(
                                    label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                                selectedLabelColor = AsgardLight,
                                containerColor = BgAttr,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AsgardLight else BorderSubtle
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("ref_cat_chip_${catKey.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Partner Referral Link Cards List
        if (filteredLinks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔍", fontSize = 28.sp)
                    Text("No matching referral links found", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Try clearing your filter or tap '+ Add Link' to add a new referral offer.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    OutlinedButton(
                        onClick = {
                            selectedCategoryFilter = "ALL"
                            searchQuery = ""
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Show All Links", color = AsgardLight, fontSize = 11.sp)
                    }
                }
            }
        } else {
            filteredLinks.forEach { link ->
                PartnerReferralCard(
                    link = link,
                    onCopyLink = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText(link.title, link.referralUrl))
                        partnerLinks = ReferralManager.incrementPartnerLinkClick(context, link.id)
                        metrics = ReferralManager.getMetrics(context)
                        Toast.makeText(context, "Copied link: ${link.title}", Toast.LENGTH_SHORT).show()
                    },
                    onShareLink = {
                        partnerLinks = ReferralManager.incrementPartnerLinkClick(context, link.id)
                        metrics = ReferralManager.getMetrics(context)
                        val shareBody = buildString {
                            appendLine("${link.icon} ${link.title}")
                            appendLine("🎁 Your Bonus: ${link.friendBonus}")
                            if (link.referralCode.isNotBlank()) {
                                appendLine("🔑 Promo / Invite Code: ${link.referralCode}")
                            }
                            appendLine()
                            appendLine(link.description)
                            appendLine()
                            appendLine("Sign up via my referral link:")
                            appendLine(link.referralUrl)
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${link.title} — Invite Bonus (${link.friendBonus})")
                            putExtra(Intent.EXTRA_TEXT, shareBody)
                        }
                        safeStartActivity(
                            context,
                            Intent.createChooser(shareIntent, "Share ${link.title}"),
                            fallbackCopyText = shareBody
                        )
                    },
                    onOpenLink = {
                        partnerLinks = ReferralManager.incrementPartnerLinkClick(context, link.id)
                        metrics = ReferralManager.getMetrics(context)
                        val viewIntent = Intent(Intent.ACTION_VIEW, link.referralUrl.toUri())
                        safeStartActivity(context, viewIntent, fallbackCopyText = link.referralUrl)
                    },
                    onTogglePin = {
                        partnerLinks = ReferralManager.togglePinPartnerLink(context, link.id)
                    },
                    onEditLink = {
                        editingLink = link
                        showAddOrEditDialog = true
                    }
                )
            }
        }

        // 4. Collapsible TYR Storefront Ambassador Analytics & Tick Counter
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("referral_tick_counter_card"),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAnalyticsAccordion = !showAnalyticsAccordion },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📊", fontSize = 18.sp)
                        Column {
                            Text(
                                "LIVE REFERRAL FUNNEL & BOUNTY TRACKER",
                                color = AsgardLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                "Clicks: ${metrics.totalClicks} • Intakes: ${metrics.totalIntakes} • Earned: $${String.format(Locale.US, "%.2f", metrics.totalRewardEarned)}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showAnalyticsAccordion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Analytics",
                        tint = AsgardLight
                    )
                }

                AnimatedVisibility(visible = showAnalyticsAccordion) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(color = BorderSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TickCounterTile(
                                icon = "👆",
                                count = metrics.totalClicks,
                                label = "Link Clicks",
                                subtext = "Opened referral link",
                                accentColor = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            TickCounterTile(
                                icon = "📝",
                                count = metrics.totalIntakes,
                                label = "Signups / Intakes",
                                subtext = "Verified submissions",
                                accentColor = Color(0xFFFBBF24),
                                modifier = Modifier.weight(1f)
                            )
                            TickCounterTile(
                                icon = "🏆",
                                count = metrics.totalPurchased,
                                label = "Completed",
                                subtext = "Qualified rewards",
                                accentColor = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Quick Test / Update Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    metrics = ReferralManager.recordClick(context)
                                    Toast.makeText(context, "+1 Click logged", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("+1 Click", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    metrics = ReferralManager.recordIntake(context)
                                    Toast.makeText(context, "+1 Signup logged", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("+1 Signup", fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    metrics = ReferralManager.recordPurchase(context, bountyEarned = 25.0)
                                    Toast.makeText(context, "+$25.00 Reward logged!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("+$25 Bonus", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    partnerLinks = ReferralManager.resetPartnerLinksToDefaults(context)
                                    Toast.makeText(context, "Restored default partner offers", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Restore Default Offers", color = AsgardLight, fontSize = 10.sp)
                            }

                            TextButton(
                                onClick = {
                                    metrics = ReferralManager.resetMetrics(context)
                                    Toast.makeText(context, "Counters reset to 0", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Reset Counters", color = Color(0xFFF87171), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. FTC & Play Store Affiliate Transparency Disclosure
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgAttr),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "⚖️ REFERRAL & AFFILIATE DISCLOSURE (16 CFR PART 255)",
                    color = AsgardLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    "External referral links (including SoFi Invest, credit monitoring, digital asset platforms, and security hardware) direct to independent third-party platforms via your device browser. Bonus eligibility, terms, and promotional values are governed by each respective platform's terms of service. Zero raw financial or account credentials are ever collected or stored inside this app.",
                    color = TextMuted,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }

    // Add or Edit Partner Referral Link Modal
    if (showAddOrEditDialog) {
        AddOrEditReferralLinkDialog(
            initialLink = editingLink,
            defaultCode = metrics.referralCode,
            onDismiss = { showAddOrEditDialog = false },
            onSave = { savedLink ->
                partnerLinks = ReferralManager.saveOrUpdatePartnerLink(context, savedLink)
                showAddOrEditDialog = false
                Toast.makeText(context, "Saved '${savedLink.title}' to Referral Vault!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { linkId ->
                partnerLinks = ReferralManager.deletePartnerLink(context, linkId)
                showAddOrEditDialog = false
                Toast.makeText(context, "Removed referral link.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Master Referral Code Customization Dialog
    if (showCustomCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCodeDialog = false },
            title = {
                Text("Customize Master Referral Code", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Set your default referral handle or code used when creating new partner links:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = customCodeInput,
                        onValueChange = { customCodeInput = it.uppercase() },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_referral_code"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customCodeInput.isNotBlank()) {
                            metrics = ReferralManager.updateReferralCode(context, customCodeInput)
                            showCustomCodeDialog = false
                            Toast.makeText(context, "Master code updated to ${metrics.referralCode}!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain)
                ) {
                    Text("Save Code", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCodeDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun PartnerReferralCard(
    link: PartnerReferralLink,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onOpenLink: () -> Unit,
    onTogglePin: () -> Unit,
    onEditLink: () -> Unit
) {
    val categoryAccent = when (link.category) {
        ReferralLinkCategory.FINTECH -> Color(0xFF34D399)
        ReferralLinkCategory.CREDIT -> Color(0xFF38BDF8)
        ReferralLinkCategory.CRYPTO -> Color(0xFFF59E0B)
        ReferralLinkCategory.SECURITY -> Color(0xFFA855F7)
        ReferralLinkCategory.TYR_CONSULTING -> MidgardLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("partner_referral_card_${link.id}"),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, if (link.isPinned) categoryAccent.copy(alpha = 0.75f) else BorderSubtle),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Icon, Category Badge, Title, Pin & Edit Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(categoryAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(link.icon, fontSize = 20.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = categoryAccent.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, categoryAccent)
                            ) {
                                Text(
                                    link.category.label.uppercase(),
                                    color = categoryAccent,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (link.clicks > 0) {
                                Text(
                                    "${link.clicks} shares",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            link.title,
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (link.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Pin Offer",
                            tint = if (link.isPinned) Color(0xFFFBBF24) else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditLink,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_referral_btn_${link.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Link",
                            tint = AsgardLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Dual Reward Highlight Boxes (Your Bonus vs Friend Bonus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryAccent.copy(alpha = 0.45f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("YOUR REFERRAL BONUS", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(link.yourReward, color = categoryAccent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }

                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.45f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("FRIEND / CLIENT BONUS", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(link.friendBonus, color = AsgardLight, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }

            Text(
                link.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Copyable URL & Promo Code Bar
            Surface(
                color = BgAttr,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyLink() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            link.referralUrl,
                            color = TextMain,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        if (link.referralCode.isNotBlank()) {
                            Text(
                                "Code: ${link.referralCode}",
                                color = categoryAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = AsgardLight,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 3-Button Action Strip: Copy Link, Share Invite, Open Offer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopyLink,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_ref_btn_${link.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShareLink,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_ref_btn_${link.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryAccent),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = categoryAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Invite", fontSize = 11.sp, color = categoryAccent, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenLink,
                    modifier = Modifier.testTag("open_ref_btn_${link.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Link",
                        tint = TextMain,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOrEditReferralLinkDialog(
    initialLink: PartnerReferralLink?,
    defaultCode: String,
    onDismiss: () -> Unit,
    onSave: (PartnerReferralLink) -> Unit,
    onDelete: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialLink?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(initialLink?.category ?: ReferralLinkCategory.FINTECH) }
    var referralUrl by remember { mutableStateOf(initialLink?.referralUrl ?: "https://") }
    var referralCode by remember { mutableStateOf(initialLink?.referralCode ?: defaultCode) }
    var yourReward by remember { mutableStateOf(initialLink?.yourReward ?: "$50 Referral Bonus") }
    var friendBonus by remember { mutableStateOf(initialLink?.friendBonus ?: "$25 Welcome Bonus") }
    var description by remember { mutableStateOf(initialLink?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialLink == null) "Add Partner Referral Link" else "Edit Referral Link",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Category:",
                    color = AsgardLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                SimpleFlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 6.dp,
                    verticalSpacing = 6.dp
                ) {
                    ReferralLinkCategory.entries.forEach { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.badgeIcon} ${cat.label}", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                                selectedLabelColor = AsgardLight,
                                containerColor = BgAttr,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Program Name (e.g., SoFi Invest, Robinhood, Amex)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referralUrl,
                    onValueChange = { referralUrl = it },
                    label = { Text("Your Exact Referral URL *", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referralCode,
                    onValueChange = { referralCode = it },
                    label = { Text("Promo / Invite Code (Optional)", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = yourReward,
                        onValueChange = { yourReward = it },
                        label = { Text("Your Bonus", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = friendBonus,
                        onValueChange = { friendBonus = it },
                        label = { Text("Friend Bonus", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Qualification Requirements", fontSize = 10.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialLink != null) {
                    TextButton(
                        onClick = { onDelete(initialLink.id) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Offer", color = Color(0xFFF87171), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && referralUrl.isNotBlank()) {
                        onSave(
                            PartnerReferralLink(
                                id = initialLink?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                category = selectedCategory,
                                icon = initialLink?.icon ?: selectedCategory.badgeIcon,
                                referralUrl = referralUrl.trim(),
                                referralCode = referralCode.trim(),
                                yourReward = yourReward.trim().ifBlank { "Partner Bonus" },
                                friendBonus = friendBonus.trim().ifBlank { "Welcome Bonus" },
                                description = description.trim().ifBlank { "Tap to claim your referral welcome bonus." },
                                clicks = initialLink?.clicks ?: 0,
                                isPinned = initialLink?.isPinned ?: true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MidgardMain)
            ) {
                Text("Save Link", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = BgCard,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ReferralSummaryPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(label, color = TextMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun TickCounterTile(
    icon: String,
    count: Int,
    label: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 14.sp)
                Surface(
                    color = BgCard,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "$count",
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            Text(text = label, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(text = subtext, color = TextMuted, fontSize = 8.sp, lineHeight = 11.sp)
        }
    }
}
