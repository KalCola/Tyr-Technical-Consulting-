package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

@Composable
fun InquiryDialog(
    item: ServiceItem?,
    itemsList: List<ServiceItem> = emptyList(),
    onDismiss: () -> Unit,
    onCallNow: () -> Unit,
    onSendEmail: (subject: String, body: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("inquiry_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MidgardMain)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "DISPATCH INQUIRY",
                        color = MidgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (item != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgAttr, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(item.title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "${item.sku} • ${item.price} (${item.priceSub})",
                                color = AsgardLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.meaning, color = TextMuted, fontSize = 10.sp)
                        }
                    }
                } else if (itemsList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgAttr, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("${itemsList.size} Items in Custom Order", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            itemsList.take(3).forEach { s ->
                                Text("• ${s.sku}: ${s.title} (${s.price})", color = TextSecondary, fontSize = 10.sp)
                            }
                            if (itemsList.size > 3) {
                                Text("+ ${itemsList.size - 3} more items", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Direct consultation dispatch to CALEB TR HOLCOMB (d/b/a TYR). Choose your preferred method:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onCallNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_call_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AsgardMain, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call ${TyrCatalog.phone}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val subject = if (item != null) "Inquiry: ${item.sku} - ${item.title}" else "Inquiry: Multiple Services Order"
                val body = if (item != null) {
                    "Hello Caleb,\n\nI am interested in ${item.title} (${item.sku}, ${item.price}). Please let me know the intake steps.\n\nThank you!"
                } else {
                    val skus = itemsList.joinToString(", ") { "${it.sku} (${it.title})" }
                    "Hello Caleb,\n\nI would like an estimate on the following services:\n$skus\n\nThank you!"
                }

                OutlinedButton(
                    onClick = { onSendEmail(subject, body) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_email_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MidgardLight),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Encrypted Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
