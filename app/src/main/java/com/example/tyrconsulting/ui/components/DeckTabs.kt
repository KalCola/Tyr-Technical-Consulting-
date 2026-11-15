package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.ui.theme.*

@Composable
fun DeckTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenCommandCenter: () -> Unit = {},
    isCompactMode: Boolean = false,
    onToggleCompactMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        "🪪 3D Card",
        "P1: Web & Domain",
        "P2: Legal & Admin",
        "P3: Cold Vault & Bills",
        "P4: Tech Care ($55+)",
        "P5: Audit Care ($75+)",
        "P6: Remote Turnkey",
        "P7: B2B Procurement",
        "P8: B2B Retainers",
        "✨ All Realms",
        "🧮 Estimate & Cart",
        "📝 Client Intake",
        "📋 Order Tracker",
        "💬 AI Systems Advisor",
        "🎨 Image Forge",
        "📡 Chat Relay",
        "✍️ E-Sign SOW",
        "🛡️ Zero-Custody Vault",
        "📅 Schedule Call",
        "🤝 Referral Vault",
        "🛠️ Custom Quote",
        "💳 Payment Clarity",
        "🏛️ About TYR",
        "🛟 Support & SLA",
        "📜 Privacy Policy",
        "💳 Stripe & Receipts",
        "🔬 Field Forensics",
        "🚀 Growth & Audit"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prominent Lead Button: Grouped Category Drawer & Quick Switcher
        Surface(
            onClick = onOpenCommandCenter,
            color = AsgardMain.copy(alpha = 0.25f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AsgardLight),
            modifier = Modifier
                .height(32.dp)
                .testTag("deck_command_hub_quick_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(13.dp))
                Text("🧭 Category Drawer", color = AsgardLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (onToggleCompactMode != null && selectedTab in 1..9) {
            Surface(
                onClick = onToggleCompactMode,
                color = if (isCompactMode) Color(0xFF064E3B) else BgCard,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isCompactMode) Color(0xFF34D399) else BorderSubtle),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("deck_compact_mode_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (isCompactMode) "▤ Compact: ON" else "▤ Compact: OFF",
                        color = if (isCompactMode) Color(0xFF34D399) else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (index) {
                        0 -> Color(0xFF9333EA)
                        7 -> Color(0xFF1E3A8A)
                        8 -> Color(0xFF047857)
                        10 -> MidgardMain
                        14 -> Color(0xFFD97706)
                        15 -> Color(0xFF0284C7)
                        16 -> Color(0xFF10B981)
                        17 -> Color(0xFF0F766E)
                        18 -> AsgardMain
                        19 -> Color(0xFF059669)
                        20 -> Color(0xFFD97706)
                        21 -> Color(0xFF047857)
                        22 -> Color(0xFF7C3AED)
                        23 -> Color(0xFF0284C7)
                        24 -> Color(0xFF0F766E)
                        25 -> Color(0xFF635BFF)
                        26 -> Color(0xFF059669)
                        27 -> Color(0xFFD97706)
                        else -> MidgardMain
                    },
                    selectedLabelColor = Color.White,
                    containerColor = BgCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) AsgardLight else BorderSubtle,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("deck_tab_$index")
            )
        }
    }
}
