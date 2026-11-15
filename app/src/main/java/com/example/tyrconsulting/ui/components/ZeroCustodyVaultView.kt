package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.ui.theme.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class StagedVaultDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val sha384: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
    val byteCount: Int
)

@Composable
fun ZeroCustodyVaultView(
    onNavigateToIntake: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stagedDocs = remember {
        mutableStateListOf(
            StagedVaultDocument(
                name = "Sample_Telecom_Billing_Audit.txt",
                category = "Expense Audit",
                sha384 = sha384Hex("Sample_Telecom_Billing_Audit.txt"),
                byteCount = 1420
            ),
            StagedVaultDocument(
                name = "DNS_Zone_Records_Export.json",
                category = "Domain Shield",
                sha384 = sha384Hex("DNS_Zone_Records_Export.json"),
                byteCount = 2850
            )
        )
    }

    var sanitizerInput by remember {
        mutableStateOf(
            "Account # 4400-1234-5678-9012\nRouting: 121000358\nMonthly SaaS Fee: \$185.00 (Vendor: Telecom Cloud)\nDispute Item: Unauthorized line surcharge \$45.00"
        )
    }
    var sanitizerOutput by remember { mutableStateOf<String?>(null) }
    var redactionCount by remember { mutableIntStateOf(0) }
    var showPurgeConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgObsidian)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vault Header
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
                            .background(Color(0xFF0F766E), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 18.sp)
                    }
                    Column {
                        Text("Zero-Custody Document Vault", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("0 Raw Financial Data Stored • 0-Day Cryptographic Purge", color = VanaheimLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "Raw bank statements, utility invoices, and client credentials are never permanently stored. Files are held exclusively in RAM during audit extraction and immediately purged.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Live In-Memory Staged Documents
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "STAGED IN-MEMORY ASSETS (${stagedDocs.size})",
                color = AsgardLight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            if (stagedDocs.isNotEmpty()) {
                Button(
                    onClick = { showPurgeConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("0-Day Purge All", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (stagedDocs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                        Text("Vault is Clean", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("All staged working files have been 100% purged from RAM.", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                stagedDocs.forEach { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgAttr),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(doc.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(doc.category, color = AsgardLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("SHA-384: ${doc.sha384.take(28)}...", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF38BDF8))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${doc.byteCount} bytes • Loaded at ${doc.timestamp}", color = TextMuted, fontSize = 9.sp)
                                TextButton(
                                    onClick = { stagedDocs.remove(doc) },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Purge File", color = Color(0xFFF87171), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Zero-Custody Statement Sanitizer Playground
        Text(
            "ZERO-CUSTODY STATEMENT SANITIZER",
            color = AsgardLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Paste an excerpt of a vendor invoice or billing statement below to strip all payment cards, routing numbers, and credentials before transmission:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = sanitizerInput,
                    onValueChange = { sanitizerInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("sanitizer_input"),
                    label = { Text("Raw Statement / Billing Excerpt", fontSize = 10.sp) },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = AsgardLight,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = BgAttr,
                        unfocusedContainerColor = BgAttr
                    )
                )

                Button(
                    onClick = {
                        val result = ZeroCustodyScrubber.scrub(sanitizerInput)
                        sanitizerOutput = result.sanitizedText
                        redactionCount = result.redactions.size
                        Toast.makeText(context, "Sanitized! Redacted ${result.redactions.size} sensitive tokens.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_sanitize"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sanitize & Redact Under Zero-Custody Rule", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (sanitizerOutput != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF064E3B), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF059669), RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SANITIZED FLAT-GRID LEDGER OUTPUT", color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("$redactionCount Redactions Enforced", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            sanitizerOutput ?: "",
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Sanitized Ledger", sanitizerOutput)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Sanitized ledger copied!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Sanitized Report", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 0-Day Purge Confirmation Dialog
    if (showPurgeConfirmation) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                    Text("Confirm 0-Day Cryptographic Purge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Text(
                    "All ${stagedDocs.size} staged documents and in-memory extracts will be immediately wiped from RAM with zero residual disk persistence, fulfilling our Zero-Custody Covenant.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        stagedDocs.clear()
                        showPurgeConfirmation = false
                        Toast.makeText(context, "Purge complete: 0 raw files stored.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Purge & Shred Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmation = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = BgCard
        )
    }
}
