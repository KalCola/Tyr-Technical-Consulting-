package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.GrowthChannel
import com.example.tyrconsulting.model.GrowthMarketingManager
import com.example.tyrconsulting.model.ReferralManager
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun ServiceCard(
    service: ServiceItem,
    onInquire: (ServiceItem) -> Unit,
    onAddToEstimate: (ServiceItem) -> Unit,
    isInEstimate: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: ((ServiceItem) -> Unit)? = null,
    onBuyWithStripe: ((ServiceItem) -> Unit)? = null,
    isCompactMode: Boolean = false,
    storefrontMode: String = "PERSONAL",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val realm = TyrCatalog.realms.find { it.id == service.realmId }
    val realmColor = realm?.let { Color(it.mainColorHex) } ?: MidgardMain
    val realmLightColor = realm?.let { Color(it.lightColorHex) } ?: MidgardLight
    var expandedInCompact by remember(isCompactMode) { mutableStateOf(false) }

    val isBusinessTier = service.page in listOf(7, 8) ||
        service.sku.startsWith("SVC-B2B") ||
        service.sku.startsWith("RET-")
    val matchesActiveMode = (storefrontMode.equals("BUSINESS", ignoreCase = true) && isBusinessTier) ||
        (!storefrontMode.equals("BUSINESS", ignoreCase = true) && !isBusinessTier)

    val borderColor = when {
        isFavorite -> AsgardLight
        matchesActiveMode && isBusinessTier -> VanaheimLight.copy(alpha = 0.78f)
        matchesActiveMode && !isBusinessTier -> realmColor.copy(alpha = 0.85f)
        else -> realmColor.copy(alpha = 0.35f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_card_${service.sku}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isFavorite || matchesActiveMode) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompactMode && !expandedInCompact) 10.dp else 14.dp)
        ) {
            // Header: Rune Pill, SKU, Tier Badge, Favorite Star, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(realmColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, realmColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = service.runePill,
                            color = realmLightColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = service.sku,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                if (isBusinessTier) Color(0xFF064E3B).copy(alpha = 0.7f) else MidgardMain.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (isBusinessTier) VanaheimLight.copy(alpha = 0.6f) else MidgardLight.copy(alpha = 0.55f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isBusinessTier) "🏢 Business" else "👤 Personal",
                            color = if (isBusinessTier) VanaheimLight else MidgardLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = { onToggleFavorite(service) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("fav_btn_${service.sku}")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavorite) "Unpin Favorite" else "Pin Favorite",
                                tint = if (isFavorite) AsgardLight else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = service.price,
                        color = realmLightColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isCompactMode && !expandedInCompact) 14.sp else 17.sp
                    )
                    Text(
                        text = service.priceSub,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isCompactMode && !expandedInCompact) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.title,
                            color = TextMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${service.badge} • ${service.specTag}",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { onAddToEstimate(service) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(if (isInEstimate) "✓ Cart" else "+ Cart", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (onBuyWithStripe != null) {
                            Button(
                                onClick = { onBuyWithStripe(service) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("💳 Pay", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(
                            onClick = { expandedInCompact = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Details ▾", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                return@Column
            }

            if (isCompactMode && expandedInCompact) {
                TextButton(
                    onClick = { expandedInCompact = false },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("▴ Collapse Compact View", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.meaning,
                color = realmLightColor.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.title,
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Cell Attribute Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgAttr, RoundedCornerShape(6.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .border(
                        width = 2.dp,
                        color = realmColor,
                        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                service.attrs.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(
                            value,
                            color = if (label == "Norse Realm") realmLightColor else SteelSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bullets
            service.bullets.forEach { bullet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = realmLightColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bullet,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Deliverable Badge & Spec Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(BgAttr, RoundedCornerShape(4.dp))
                        .border(1.dp, realmColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(service.badge, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = service.specTag,
                    color = realmLightColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onAddToEstimate(service) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("estimate_btn_${service.sku}"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isInEstimate) VanaheimLight else TextSecondary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = "Estimate",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isInEstimate) "In Cart" else "+ Cart",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val ownCode = ReferralManager.getMetrics(context).referralCode
                        val pitch = GrowthMarketingManager.buildPitchText(
                            channel = GrowthChannel.SMS,
                            service = service,
                            ownRefCode = ownCode,
                            includeRef = true
                        )
                        GrowthMarketingManager.recordOutboundShare(context)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "TYR Technical Consulting — ${service.sku}")
                            putExtra(Intent.EXTRA_TEXT, pitch)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share ${service.sku}"))
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("share_sku_btn_${service.sku}")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share SKU Deep-Link",
                        tint = VanaheimLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
                OutlinedButton(
                    onClick = {
                        val specSummary = buildString {
                            appendLine("TYR PROTOCOL SERVICE SPECIFICATION")
                            appendLine("SKU: ${service.sku} (${service.runePill})")
                            appendLine("Service: ${service.title}")
                            appendLine("Rate: ${service.price} (${service.priceSub})")
                            appendLine("Deliverable: ${service.badge} | ${service.specTag}")
                            appendLine("Included Scope:")
                            service.bullets.forEach { b -> appendLine(" - $b") }
                            appendLine("Storefront: https://tyrprotocol.pro | ${TyrCatalog.phone}")
                        }
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cb?.setPrimaryClip(ClipData.newPlainText("SKU Spec ${service.sku}", specSummary))
                        Toast.makeText(context, "Copied ${service.sku} spec sheet!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("copy_spec_btn_${service.sku}")
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Spec Summary",
                        tint = AsgardLight,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Button(
                    onClick = { onInquire(service) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("order_btn_${service.sku}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = realmColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Inquire",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Order SKU", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (onBuyWithStripe != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { onBuyWithStripe(service) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stripe_checkout_btn_${service.sku}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF635BFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Text(
                        "💳 Instant Stripe Checkout (${service.price}) • Digital Receipt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

