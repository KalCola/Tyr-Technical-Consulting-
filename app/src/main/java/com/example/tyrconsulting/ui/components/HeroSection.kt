package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun HeroSection(
    onPhoneClick: () -> Unit,
    onEmailClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onCardClick: () -> Unit,
    storefrontMode: String = "PERSONAL",
    onStorefrontModeChanged: (String) -> Unit = {},
    onForgeClick: () -> Unit = {},
    onSignClick: () -> Unit = {},
    onVaultClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onReferralClick: () -> Unit = {},
    onCustomQuoteClick: () -> Unit = {},
    onPaymentClarityClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isBusinessMode = storefrontMode.equals("BUSINESS", ignoreCase = true)
    val heroBorderColors = if (isBusinessMode) {
        listOf(AsgardLight, VanaheimLight)
    } else {
        listOf(MidgardMain, AsgardMain)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(heroBorderColors)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(listOf(MidgardMain, Color(0xFF7F1D1D))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, AsgardLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ᛏ",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = if (isBusinessMode) "TYR TECHNICAL CONSULTING" else "CALEB HOLCOMB • TYR",
                            color = TextMain,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isBusinessMode) {
                                "B2B Procurement • Governance • Retainers"
                            } else {
                                "Personal Tech • Email Shield • Audit Defense"
                            },
                            color = if (isBusinessMode) VanaheimLight else AsgardLight,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = onCardClick,
                        label = { Text("3D Card", fontSize = 10.sp, color = AsgardLight) },
                        modifier = Modifier.testTag("hero_3d_card_chip")
                    )
                    AssistChip(
                        onClick = onPrivacyClick,
                        label = { Text("0-Custody", fontSize = 10.sp, color = VanaheimLight) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Zero-Custody",
                                tint = VanaheimLight,
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        modifier = Modifier.testTag("zero_custody_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Personal / Business Storefront Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgAttr, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onStorefrontModeChanged("PERSONAL") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .testTag("hero_mode_personal_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isBusinessMode) MidgardMain else Color.Transparent,
                        contentColor = if (!isBusinessMode) Color.White else TextSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        "👤 Personal Focus",
                        fontSize = 11.sp,
                        fontWeight = if (!isBusinessMode) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }

                Button(
                    onClick = { onStorefrontModeChanged("BUSINESS") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .testTag("hero_mode_business_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBusinessMode) Color(0xFF059669) else Color.Transparent,
                        contentColor = if (isBusinessMode) Color.White else TextSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        "🏢 Business Focus",
                        fontSize = 11.sp,
                        fontWeight = if (isBusinessMode) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle & Area
            Text(
                text = if (isBusinessMode) {
                    "Enterprise-grade vendor governance, B2B compliance suites, and automated security retainers with 100% client master-file custody."
                } else {
                    "Flat-rate technology, private custom email, and Schedule C audit defense for individuals and operators in Northwest Alabama & nationwide."
                },
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Attributes strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isBusinessMode) {
                    AttrPill("ENTITY", "TYR CONSULTING", AsgardLight, Modifier.weight(1f))
                    AttrPill("NAICS", "541512 • 541611", VanaheimLight, Modifier.weight(1f))
                    AttrPill("CUSTODY", "0-Day Purge SLA", MidgardMain, Modifier.weight(1f))
                } else {
                    AttrPill("LEGAL", "CALEB TR HOLCOMB", MidgardMain, Modifier.weight(1f))
                    AttrPill("PILLAR", "ᛏ Teiwaz • Triad", AsgardMain, Modifier.weight(1f))
                    AttrPill("RATES", "$65 – $495 Flat", VanaheimMain, Modifier.weight(1f))
                }
            }

            Surface(
                color = BgAttr,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = "📡 Service Catalog: ${TyrCatalog.catalogSourceLabel}",
                    color = AsgardLight,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onCardClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hero_card_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFC084FC)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("🪪 3D Card", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onForgeClick,
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("hero_forge_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AsgardLight
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("🎨 Forge (ᛏ ᚠ)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPhoneClick,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("hero_phone_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MidgardLight
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Call", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEmailClick,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("hero_email_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidgardMain,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inquire / Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSignClick,
                    modifier = Modifier.weight(1f).testTag("hero_sign_button"),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Text("✍️ E-Sign SOW", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onVaultClick,
                    modifier = Modifier.weight(1f).testTag("hero_vault_button"),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF14B8A6)),
                    border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.5f))
                ) {
                    Text("🛡️ Vault", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onScheduleClick,
                    modifier = Modifier.weight(1f).testTag("hero_schedule_button"),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AsgardLight),
                    border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.5f))
                ) {
                    Text("📅 Schedule", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReferralClick,
                    modifier = Modifier.weight(1f).testTag("hero_referral_button"),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399)),
                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                ) {
                    Text("🤝 Referral", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onCustomQuoteClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("hero_custom_quote_builder_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("🛠️ Custom Quote", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = onPaymentClarityClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("hero_payment_clarity_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF047857),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("💳 1-Time vs Monthly", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun AttrPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(BgAttr, RoundedCornerShape(6.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .border(
                width = 2.dp,
                color = accentColor,
                shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
            )
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = SteelSilver, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
