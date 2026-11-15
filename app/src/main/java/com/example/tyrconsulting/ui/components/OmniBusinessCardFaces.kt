package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.GrowthMarketingManager
import com.example.tyrconsulting.model.GrowthQrEncoder
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

private data class TriadSpec(
    val tabLabel: String,
    val attrs: List<Pair<String, String>>,
    val heading: String,
    val body: String,
    val accent: Color
)

private val personalTriadSpecs = listOf(
    TriadSpec(
        tabLabel = "ᛏ Týr",
        attrs = listOf("Pillar" to "ᛏ Tiwaz Spine", "Alignment" to "Pisces • 1997", "Region" to "NW Alabama"),
        heading = "ᛏ TÝR — UNYIELDING INTEGRITY & KEPT OATHS",
        body = "Direct, plainspoken candor and technical accountability—standing firmly behind every architecture and word without pretense.",
        accent = MidgardMain
    ),
    TriadSpec(
        tabLabel = "ᚠ Freyja",
        attrs = listOf("Pillar" to "ᚠ Fehu Flame", "Boundary" to "Sovereign Agency", "Custody" to "Zero-Coercion"),
        heading = "ᚠ FREYJA — SOVEREIGN BOUNDARIES & SELF-WORTH",
        body = "Fierce independence and personal privacy—every relationship and collaboration remains voluntary, transparent, and uncoerced.",
        accent = AsgardLight
    ),
    TriadSpec(
        tabLabel = "ᚲ Loki",
        attrs = listOf("Pillar" to "ᚲ Kenaz Wit", "Craft" to "Bare-Metal Linux", "Ethos" to "Zero Dogma"),
        heading = "ᚲ LOKI — TACTICAL ADAPTABILITY WITHOUT DOGMA",
        body = "Relentless curiosity across hardware, Windows/Linux internals, and code—solving hard bottlenecks through lateral ingenuity.",
        accent = VanaheimLight
    )
)

private val businessTriadSpecs = listOf(
    TriadSpec(
        tabLabel = "ᛏ Týr",
        attrs = listOf("NAICS" to "541512 • 541611", "Custody" to "0-Day Purge", "Rates" to "$65–$495 Flat"),
        heading = "ᛏ TÝR QUALITY — ROOT-CAUSE PRECISION & VERIFIABLE RIGOR",
        body = "Telemetry-first diagnostics and SHA-384 verifiable engineering backed by statutory compliance and clean documentation.",
        accent = MidgardMain
    ),
    TriadSpec(
        tabLabel = "ᚠ Freyja",
        attrs = listOf("Ownership" to "100% Client", "Billing" to "PCI-DSS SAQ-A", "Defense" to "Schedule C / ISP"),
        heading = "ᚠ FREYJA QUALITY — 100% CLIENT CUSTODY & ZERO LOCK-IN",
        body = "Complete administrative sovereignty. Clients hold full domain, repo, and asset custody outright—never held hostage by SaaS fees.",
        accent = AsgardLight
    ),
    TriadSpec(
        tabLabel = "ᚲ Loki",
        attrs = listOf("Lock-In" to "$0/Mo Hosting", "Turnaround" to "24–72h Turnkey", "Delivery" to "US Remote + NW AL"),
        heading = "ᚲ LOKI QUALITY — LEAN, LOCAL-FIRST & RESILIENT EXECUTION",
        body = "Eliminating bloated agency retainers to ship fast, low-overhead, durable systems engineered for Main Street and B2B teams.",
        accent = VanaheimLight
    )
)

@Composable
internal fun FrontIdentityCardFace(
    context: Context,
    selectedDeityIndex: Int,
    onSelectDeity: (Int) -> Unit,
    qrVcardMode: Boolean,
    onToggleQrMode: () -> Unit,
    onSaveContact: () -> Unit,
    onShareCard: () -> Unit,
    onFlipToBack: () -> Unit
) {
    val activeSpec = personalTriadSpecs[selectedDeityIndex.coerceIn(0, personalTriadSpecs.lastIndex)]
    val borderBrush = Brush.linearGradient(
        listOf(activeSpec.accent, AsgardLight, MidgardMain)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.5.dp, borderBrush),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Identity Header + Interactive Tap-to-Toggle QR Dock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(BgAttr)
                        .border(1.5.dp, activeSpec.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(38.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        drawCircle(
                            color = AsgardMain.copy(alpha = 0.45f),
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                        drawLine(
                            color = AsgardLight,
                            start = Offset(cx, cy - 13.dp.toPx()),
                            end = Offset(cx, cy + 13.dp.toPx()),
                            strokeWidth = 2.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        val path = Path().apply {
                            moveTo(cx - 8.dp.toPx(), cy - 5.dp.toPx())
                            lineTo(cx, cy - 13.dp.toPx())
                            lineTo(cx + 8.dp.toPx(), cy - 5.dp.toPx())
                        }
                        drawPath(
                            path = path,
                            color = MidgardLight,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = activeSpec.accent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, activeSpec.accent.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "PERSONAL IDENTITY • TEIWAZ (ᛏ)",
                            color = activeSpec.accent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Caleb TR Holcomb",
                        color = TextMain,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        "Principal Systems Engineer & Developer",
                        color = AsgardLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Text(
                        "📍 Northwest Alabama & Nationwide Remote",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                MiniInteractiveQrBadge(
                    isVcard = qrVcardMode,
                    modeParam = "personal",
                    accent = activeSpec.accent,
                    onToggle = onToggleQrMode
                )
            }

            // 3-Column Attribute Strip matching Storefront Design
            TriadAttrStrip(attrs = activeSpec.attrs, accent = activeSpec.accent)

            // Patron Triad Selector Tabs
            TriadTabsRow(
                specs = personalTriadSpecs,
                selectedIndex = selectedDeityIndex,
                onSelect = onSelectDeity
            )

            // Triad Insight Box
            TriadInsightBox(spec = activeSpec)

            // Direct Personal & Fallback Contact Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChannelMiniChip(
                    label = "📞 Call",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val i = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
                        safeStartActivity(context, i, fallbackCopyText = TyrCatalog.phone)
                    }
                )
                ChannelMiniChip(
                    label = "✉️ Personal",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val i = Intent(Intent.ACTION_SENDTO, "mailto:colacal43@gmail.com".toUri())
                        safeStartActivity(context, i, fallbackCopyText = "colacal43@gmail.com")
                    }
                )
                ChannelMiniChip(
                    label = "⌘ GitHub",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val i = Intent(Intent.ACTION_VIEW, "https://github.com/kalcola".toUri())
                        safeStartActivity(context, i, fallbackCopyText = "https://github.com/kalcola")
                    }
                )
            }

            // Action Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onSaveContact,
                    modifier = Modifier
                        .weight(1.2f)
                        .heightIn(min = 42.dp)
                        .testTag("front_save_contact_vcf_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VanaheimMain,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save .vcf", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                OutlinedButton(
                    onClick = onShareCard,
                    modifier = Modifier
                        .weight(0.9f)
                        .heightIn(min = 42.dp)
                        .testTag("front_share_card_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Card", tint = TextMain, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 10.sp, color = TextMain, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onFlipToBack,
                    modifier = Modifier
                        .weight(1.1f)
                        .heightIn(min = 42.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AsgardLight),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Business ⇄", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun BackBusinessCardFace(
    context: Context,
    selectedDeityIndex: Int,
    onSelectDeity: (Int) -> Unit,
    qrVcardMode: Boolean,
    onToggleQrMode: () -> Unit,
    onSaveContact: () -> Unit,
    onShareCard: () -> Unit,
    onExploreServices: () -> Unit,
    onOpenQuoteBuilder: () -> Unit,
    onFlipToFront: () -> Unit
) {
    val activeSpec = businessTriadSpecs[selectedDeityIndex.coerceIn(0, businessTriadSpecs.lastIndex)]
    val borderBrush = Brush.linearGradient(
        listOf(activeSpec.accent, VanaheimLight, AsgardLight)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.5.dp, borderBrush),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header + Toggleable QR Dock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = activeSpec.accent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, activeSpec.accent.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "BUSINESS FACE • ZERO-CUSTODY SLA",
                            color = activeSpec.accent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "TYR TECHNICAL CONSULTING",
                        color = AsgardLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "CALEB TR HOLCOMB d/b/a TYR • Verified Practice",
                        color = TextMain,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "NAICS 541512 & 541611 • EIN: ${TyrCatalog.ein}",
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }

                MiniInteractiveQrBadge(
                    isVcard = qrVcardMode,
                    modeParam = "business",
                    accent = activeSpec.accent,
                    onToggle = onToggleQrMode
                )
            }

            // 3-Column Business Attribute Strip
            TriadAttrStrip(attrs = activeSpec.attrs, accent = activeSpec.accent)

            // Business Patron Triad Tabs
            TriadTabsRow(
                specs = businessTriadSpecs,
                selectedIndex = selectedDeityIndex,
                onSelect = onSelectDeity
            )

            TriadInsightBox(spec = activeSpec)

            // Direct Business Channels
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ContactChannelRow(
                    icon = "📞",
                    label = "Direct Consult Line",
                    value = TyrCatalog.phone,
                    actionLabel = "Dial",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
                        safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
                    }
                )
                ContactChannelRow(
                    icon = "✉️",
                    label = "Work Dispatch & Billing",
                    value = TyrCatalog.email,
                    actionLabel = "Email",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, "mailto:${TyrCatalog.email}".toUri())
                        safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.email)
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChannelMiniChip(
                        label = "📲 SMS Handshake",
                        modifier = Modifier.weight(1.2f),
                        onClick = {
                            val smsUri = ("sms:${TyrCatalog.phoneRaw}?body=" +
                                "Hi%20Caleb%20(TYR%20Technical%20Consulting)%2C%20here%20is%20my%20contact%20info%3A%20").toUri()
                            safeStartActivity(context, Intent(Intent.ACTION_SENDTO, smsUri), fallbackCopyText = TyrCatalog.phone)
                        }
                    )
                    ChannelMiniChip(
                        label = "in LinkedIn",
                        modifier = Modifier.weight(0.9f),
                        onClick = {
                            val i = Intent(Intent.ACTION_VIEW, "https://www.linkedin.com/company/tyrs-technical-consulting".toUri())
                            safeStartActivity(context, i, fallbackCopyText = "https://www.linkedin.com/company/tyrs-technical-consulting")
                        }
                    )
                    ChannelMiniChip(
                        label = "🌐 Copy URL",
                        modifier = Modifier.weight(0.9f),
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("TYR Website", "https://tyrprotocol.pro"))
                            Toast.makeText(context, "Copied https://tyrprotocol.pro", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onSaveContact,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp)
                        .testTag("back_save_contact_vcf_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = VanaheimMain, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save .vcf", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }

                OutlinedButton(
                    onClick = onShareCard,
                    modifier = Modifier
                        .weight(0.8f)
                        .heightIn(min = 42.dp)
                        .testTag("back_share_card_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Card", tint = TextMain, modifier = Modifier.size(14.dp))
                }

                Button(
                    onClick = onOpenQuoteBuilder,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.RequestQuote, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quote SOW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onFlipToFront,
                    modifier = Modifier
                        .weight(0.9f)
                        .heightIn(min = 42.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("↩ Personal", fontSize = 9.sp, color = TextMain, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TriadAttrStrip(
    attrs: List<Pair<String, String>>,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgAttr, RoundedCornerShape(6.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .border(
                width = 2.dp,
                color = accent,
                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
            )
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        attrs.forEach { (label, value) ->
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(value, color = SteelSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TriadTabsRow(
    specs: List<TriadSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        specs.forEachIndexed { idx, item ->
            val selected = selectedIndex == idx
            OutlinedButton(
                onClick = { onSelect(idx) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 38.dp),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) item.accent.copy(alpha = 0.16f) else BgAttr,
                    contentColor = if (selected) item.accent else TextSecondary
                ),
                border = BorderStroke(1.dp, if (selected) item.accent else BorderSubtle),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    item.tabLabel,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TriadInsightBox(spec: TriadSpec) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgAttr, RoundedCornerShape(8.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = spec.accent,
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            )
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                spec.heading,
                color = spec.accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
            )
            Text(
                spec.body,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MiniInteractiveQrBadge(
    isVcard: Boolean,
    modeParam: String,
    accent: Color,
    onToggle: () -> Unit
) {
    val payload = if (isVcard) {
        GrowthMarketingManager.MECARD_QR_STRING
    } else {
        "https://tyrprotocol.pro/?mode=$modeParam"
    }

    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
        modifier = Modifier
            .clickable { onToggle() }
            .testTag("card_qr_toggle_$modeParam")
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(3.dp)
            ) {
                ScannableQrCanvas(payload = payload, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = if (isVcard) "MECARD .vcf" else "tyrprotocol.pro",
                color = accent,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Tap QR ⇄",
                color = TextMuted,
                fontSize = 7.sp
            )
        }
    }
}

@Composable
private fun ScannableQrCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val matrix = remember(payload) { GrowthQrEncoder.buildMatrix(payload) }
    Canvas(modifier = modifier) {
        val n = matrix.size
        if (n <= 0) return@Canvas
        val cellW = size.width / n
        val cellH = size.height / n
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (matrix[r][c]) {
                    drawRect(
                        color = Color(0xFF090A0F),
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW + 0.4f, cellH + 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelMiniChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 38.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BgAttr,
            contentColor = TextMain
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun ContactChannelRow(
    icon: String,
    label: String,
    value: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        color = BgAttr,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(icon, fontSize = 15.sp)
                Column {
                    Text(label, color = TextMuted, fontSize = 9.sp)
                    Text(value, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Surface(
                color = MidgardMain.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MidgardLight),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    actionLabel,
                    color = MidgardLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
