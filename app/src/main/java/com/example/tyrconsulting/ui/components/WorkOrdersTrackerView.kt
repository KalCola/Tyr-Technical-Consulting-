package com.example.tyrconsulting.ui.components

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
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
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun WorkOrdersTrackerView(
    orders: List<WorkOrder>,
    onNavigateToIntake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .padding(16.dp)
            .testTag("work_orders_tracker_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE WORK ORDER LEDGER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Live client deliverables & intake ledger", color = TextMuted, fontSize = 11.sp)
                }
                Button(
                    onClick = onNavigateToIntake,
                    colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Order", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Intake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(36.dp))
                        Text("No active work orders submitted yet.", color = TextSecondary, fontSize = 13.sp)
                        Text("Submit a client intake brief to generate an official SLA work order.", color = TextMuted, fontSize = 11.sp)
                        Button(
                            onClick = onNavigateToIntake,
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Launch Client Intake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.id, color = AsgardLight, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .background(VanaheimMain.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(order.status, color = VanaheimLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("${order.clientName} ${if (order.businessName.isNotBlank()) "• ${order.businessName}" else ""}", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Timeline: ${order.timeline} • Value: ${order.totalEstimatedPrice}", color = TextSecondary, fontSize = 11.sp)

                        Text("Services: ${order.selectedSkus.joinToString(", ")}", color = TextMuted, fontSize = 10.sp, maxLines = 1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:${TyrCatalog.email}".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Status Update: [${order.id}]")
                                        putExtra(Intent.EXTRA_TEXT, "Inquiry on active work order ${order.id}.")
                                    }
                                    safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.email)
                                }
                            ) {
                                Text("Email Lead Architect", fontSize = 11.sp, color = MidgardLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
