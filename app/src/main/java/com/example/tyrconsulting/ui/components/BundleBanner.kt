package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.example.tyrconsulting.model.BundlePlan
import com.example.tyrconsulting.ui.theme.*

@Composable
fun BundleBanner(
    bundle: BundlePlan,
    onInquireBundle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bundle_banner_ret_all_07"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(MidgardMain, AsgardMain, SvartalfheimMain)
            )
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Flagship",
                        tint = AsgardLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "FLAGSHIP RET-ALL-07",
                        color = AsgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(AsgardMain.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, AsgardLight, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(bundle.threeRealmSavings, color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bundle.title,
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(BgAttr, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .border(
                            width = 2.dp,
                            color = AsgardLight,
                            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                        )
                        .padding(6.dp)
                ) {
                    Column {
                        Text("STEWARD PLAN", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(bundle.stewardPrice, color = SteelSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Web + Shield + Vault", color = TextMuted, fontSize = 8.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(BgAttr, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .border(
                            width = 2.dp,
                            color = SvartalfheimLight,
                            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                        )
                        .padding(6.dp)
                ) {
                    Column {
                        Text("3-REALM LAUNCH", color = SvartalfheimLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(bundle.threeRealmLaunch, color = SteelSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Web + Domain + Intake", color = TextMuted, fontSize = 8.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            bundle.features.forEach { feat ->
                Text(
                    text = "✓ $feat",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onInquireBundle,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bundle_inquire_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AsgardMain,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Inquire About Steward Retainers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
