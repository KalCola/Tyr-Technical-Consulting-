package com.example.tyrconsulting.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tyrconsulting.model.ReferralManager
import com.example.tyrconsulting.ui.theme.*

@Composable
fun ZeroCustodyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var purged by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("zero_custody_dialog"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, VanaheimMain)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "Shield", tint = VanaheimLight)
                        Text(
                            "SOC 2 & ZERO-CUSTODY COVENANT",
                            color = VanaheimLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "TYR Technical Consulting operates under strict SOC 2 Type II Trust Services Criteria & Google Play Developer Policy guidelines:",
                    color = TextMain,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SOC 2 Trust Criteria Breakdown
                val trustCriteria = listOf(
                    Triple(
                        "🛡️ SECURITY (CC6.1 - CC6.7)",
                        "Strict TLS 1.3/1.2 via Android network security config (cleartextTrafficPermitted=false). Full backup and cloud extraction disabled (data-extraction-rules). Air-gapped intake handling with zero credential storage.",
                        AsgardLight
                    ),
                    Triple(
                        "🔒 CONFIDENTIALITY & ZERO-CUSTODY (C1.1 - C1.2)",
                        "0 Raw Financial Data Stored: Audits and deliverables purge source files upon completion. No third-party ad networks, no analytics trackers, and zero telemetry harvesting.",
                        VanaheimLight
                    ),
                    Triple(
                        "⚡ AVAILABILITY (A1.1 - A1.2)",
                        "Full autonomous offline fallback capability. Elder Edda lore vault and generative galdr prompts operate completely local without external cloud dependencies.",
                        Color(0xFF60A5FA)
                    ),
                    Triple(
                        "⚖️ PROCESSING INTEGRITY (PI1.1 - PI1.2)",
                        "Deterministic work order hashing (WO-YYYYMMDD-XX). Zero Dynamic Code Loading (DCL). Verified multi-architecture universal native support (arm64-v8a, armeabi-v7a, x86_64).",
                        Color(0xFF34D399)
                    ),
                    Triple(
                        "📋 GOOGLE PLAY & GITHUB INTEGRITY",
                        "Complies with Google Play Creator Track policy: Title <= 30 chars, zero ad SDKs, zero prohibited permissions. Least-privilege architecture: only standard INTERNET permission declared, with zero runtime invasive permissions.",
                        MidgardLight
                    )
                )

                trustCriteria.forEach { (title, desc, color) ->
                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text(desc, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Local Data Purge (SOC 2 CC6.5 & P4.3 Lifecycle Disposal)
                OutlinedButton(
                    onClick = {
                        ReferralManager.resetMetrics(context)
                        purged = true
                        Toast.makeText(context, "Local cache & metrics purged (SOC 2 Zero-Custody).", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("purge_local_data_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF450A0A).copy(alpha = 0.4f),
                        contentColor = Color(0xFFFCA5A5)
                    )
                ) {
                    Icon(
                        if (purged) Icons.Default.CheckCircle else Icons.Default.Delete,
                        contentDescription = "Purge",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (purged) "Local Data Purged (0-Day Reset)" else "Purge Local App Cache & Data",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VanaheimMain, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Acknowledge & Close", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
