package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.PresetPackage
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun EstimateCalculator(
    selectedServices: List<ServiceItem>,
    onRemoveService: (ServiceItem) -> Unit,
    onClearAll: () -> Unit,
    onDraftOrder: () -> Unit,
    onApplyPreset: ((PresetPackage) -> Unit)? = null,
    onOpenCustomQuoteBuilder: (() -> Unit)? = null,
    onOpenPaymentClarity: (() -> Unit)? = null,
    onCheckoutWithStripe: ((List<ServiceItem>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("estimate_calculator_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AsgardMain.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ESTIMATE & CART BUILDER",
                        color = AsgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${selectedServices.size} Service(s) selected",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                if (selectedServices.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.textButtonColors(contentColor = MidgardLight),
                        modifier = Modifier.testTag("clear_cart_btn")
                    ) {
                        Text("Clear All", fontSize = 11.sp)
                    }
                }
            }

            // Custom Quote Builder Callout
            Surface(
                color = BgAttr,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AsgardMain.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🛠️", fontSize = 14.sp)
                            Text("Want unlisted tasks or bespoke work?", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text(
                            "Not all services are listed in our catalog. Plug and play custom unlisted tasks in the Custom Quote Builder to pay only for what you want.",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            lineHeight = 13.sp
                        )
                    }
                    if (onOpenCustomQuoteBuilder != null) {
                        Button(
                            onClick = onOpenCustomQuoteBuilder,
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_custom_quote_builder_btn")
                        ) {
                            Text("Build Quote", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // QoL Feature: Turnkey Remote Deliverable Preset Packages
            Text(
                text = "⚡ TURNKEY PRESET PACKAGES (ZERO LICENSES OR CERTS REQUIRED)",
                color = VanaheimLight,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TyrCatalog.presetPackages.forEach { pkg ->
                    val isContained = pkg.serviceSkus.all { sku -> selectedServices.any { it.sku == sku } }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isContained) VanaheimLight.copy(alpha = 0.6f) else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(pkg.title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(pkg.runeBadge, color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(pkg.description, color = TextSecondary, fontSize = 9.sp, maxLines = 1)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
                                    Text(pkg.bundledPrice, color = VanaheimLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(pkg.savings, color = MidgardLight, fontSize = 9.sp)
                                    Text("• ${pkg.turnaround}", color = TextMuted, fontSize = 9.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    onApplyPreset?.invoke(pkg)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isContained) BgCard else VanaheimMain,
                                    contentColor = if (isContained) VanaheimLight else Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                if (isContained) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Added", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            if (selectedServices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgAttr, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No individual services in estimate yet.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Select a Preset Package above, or tap '+ Estimate' on any catalog service across Pages 1–6 to calculate quotes.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedServices.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgAttr, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.title,
                                    color = TextMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${s.sku} • ${s.priceSub}",
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = s.price,
                                    color = AsgardLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                IconButton(
                                    onClick = { onRemoveService(s) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MidgardLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val oneTimeServicesList = selectedServices.filter {
                        !it.priceSub.contains("Month", true) &&
                        !it.priceSub.contains("Mo", true) &&
                        !it.priceSub.contains("Retainer", true) &&
                        it.page !in listOf(4, 5, 8)
                    }
                    val monthlyServicesList = selectedServices.filter {
                        it.priceSub.contains("Month", true) ||
                        it.priceSub.contains("Mo", true) ||
                        it.priceSub.contains("Retainer", true) ||
                        it.page in listOf(4, 5, 8)
                    }

                    fun parsePrice(priceStr: String): Double {
                        val clean = priceStr.replace("$", "").replace(",", "").trim()
                        val token = if (clean.contains("–")) clean.substringBefore("–").trim()
                                    else if (clean.contains("-")) clean.substringBefore("-").trim()
                                    else clean.replace("+", "").trim()
                        return token.toDoubleOrNull() ?: 0.0
                    }

                    val oneTimeTotal = oneTimeServicesList.sumOf { parsePrice(it.price) }
                    val monthlyTotal = monthlyServicesList.sumOf { parsePrice(it.price) }

                    // Billing Model Segregation Card:
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("billing_breakdown_card"),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("BILLING MODEL BREAKDOWN", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                if (onOpenPaymentClarity != null) {
                                    TextButton(
                                        onClick = onOpenPaymentClarity,
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(22.dp).testTag("open_payment_clarity_from_cart")
                                    ) {
                                        Text("Payment Models Guide ↗", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = BgAttr,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AsgardMain.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text("⚡ ONE-TIME CAPITAL", color = AsgardLight, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                                        Text("$${oneTimeTotal.toInt()}", color = TextMain, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        Text("${oneTimeServicesList.size} item(s) • $0/mo ongoing", color = TextMuted, fontSize = 8.sp)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = BgAttr,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text("🔄 RECURRING MONTHLY", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                                        Text("$${monthlyTotal.toInt()}/mo", color = Color(0xFF34D399), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        Text("${monthlyServicesList.size} item(s) • Billed 1st/mo", color = TextMuted, fontSize = 8.sp)
                                    }
                                }
                            }
                            Text(
                                "🛡️ Zero-Surprise Defense: One-time builds never incur monthly charges. Monthly care plans cover active sentry upkeep.",
                                color = SteelSilver,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remote Deliverable Guarantee Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgAttr),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VanaheimLight.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = VanaheimLight, modifier = Modifier.size(14.dp))
                                Text("Remote Deliverable Guarantee", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Text("• 100% Asynchronous Digital Delivery: Handover via secure Git repo, Google Drive, or offline zip.", color = TextSecondary, fontSize = 9.sp)
                            Text("• Zero Licensing or Certification Hurdles: Open formats (.docx, .xlsx, .pdf, .md) with 0 platform lock-in.", color = TextSecondary, fontSize = 9.sp)
                            Text("• Zero-Custody Privacy: Raw credentials and source statements purged Day 0 upon delivery verification.", color = TextSecondary, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (onCheckoutWithStripe != null) {
                        Button(
                            onClick = { onCheckoutWithStripe(selectedServices) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_stripe_checkout_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF635BFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "💳 Pay Cart Now with Stripe Connector ($${(oneTimeTotal + monthlyTotal).toInt()})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDraftOrder,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("draft_order_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MidgardMain,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Order", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draft Client Intake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val summary = buildString {
                                    appendLine("TYR TECHNICAL CONSULTING — SOW ESTIMATE SUMMARY")
                                    appendLine("Delivered Asynchronously • Zero Licenses/Certs Required")
                                    appendLine("--------------------------------------------------")
                                    selectedServices.forEach { s ->
                                        appendLine("• [${s.sku}] ${s.title}: ${s.price} (${s.priceSub})")
                                    }
                                    appendLine("--------------------------------------------------")
                                    appendLine("Delivery Window: 24–48 Hours Asynchronous")
                                    appendLine("Zero-Custody Guarantee: All raw client working files purged Day 0")
                                    appendLine("Inquiries: tyr@tyrprotocol.pro • +1 (659) 257-8043")
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("TYR Estimate Summary", summary)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "SOW Estimate Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy SOW", tint = AsgardLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy SOW", fontSize = 11.sp, color = TextMain)
                        }
                    }
                }
            }
        }
    }
}
