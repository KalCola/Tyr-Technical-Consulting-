package com.example.tyrconsulting.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ConsultationSlot(
    val dateLabel: String,
    val timeLabel: String,
    val isAvailable: Boolean = true
)

@Composable
fun AppointmentSchedulerView(
    onNavigateToIntake: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDuration by remember { mutableStateOf("15-Min Strategy Call") }
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    var selectedTimeSlot by remember { mutableStateOf("10:00 AM CST") }

    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var scopeTopic by remember { mutableStateOf("Midgard Web & Asgard Domain Shield") }

    val calendarDays = remember {
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("EEE, MMM d", Locale.US)
        (0 until 7).map { offset ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, offset + 1)
            // Skip weekends
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY) cal.add(Calendar.DAY_OF_YEAR, 2)
            else if (dayOfWeek == Calendar.SUNDAY) cal.add(Calendar.DAY_OF_YEAR, 1)
            format.format(cal.time)
        }.distinct().take(5)
    }

    val timeSlots = listOf(
        "09:30 AM CST",
        "10:30 AM CST",
        "01:00 PM CST",
        "02:30 PM CST",
        "04:00 PM CST"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgObsidian)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                            .background(AsgardMain, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", fontSize = 18.sp)
                    }
                    Column {
                        Text("Bifröst Strategy Call Scheduler", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Direct Consult with Lead Systems Architect Caleb Holcomb", color = AsgardLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "Schedule a zero-obligation 15-minute discovery or 30-minute technical architecture call. All consultations are direct, unscripted, and backed by our Zero-Custody Covenant.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 1. Duration Choice
        Text("1. SELECT SESSION DURATION", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("15-Min Strategy Call" to "Rapid Scoping & Feasibility", "30-Min Architecture Deep Dive" to "Detailed Systems & Contract Review").forEach { (dur, desc) ->
                val isSelected = selectedDuration == dur
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDuration = dur },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) BgAttr else BgCard),
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AsgardLight else BorderSubtle),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(dur, color = if (isSelected) TextMain else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(desc, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        // 2. Date Selection
        Text("2. SELECT BUSINESS DAY (CENTRAL TIME / CST)", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(calendarDays.size) { idx ->
                val day = calendarDays[idx]
                val isSelected = selectedDateIndex == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDateIndex = idx },
                    label = { Text(day, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MidgardMain,
                        selectedLabelColor = Color.White,
                        containerColor = BgCard,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // 3. Time Slot Selection
        Text("3. SELECT TIME SLOT (ALABAMA / CST)", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        SimpleFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 6.dp,
            verticalSpacing = 6.dp
        ) {
            timeSlots.forEach { slot ->
                val isSelected = selectedTimeSlot == slot
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTimeSlot = slot },
                    label = { Text(slot, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AsgardLight,
                        selectedLabelColor = Color.Black,
                        containerColor = BgCard,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // 4. Contact Details
        Text("4. YOUR CONTACT DETAILS", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    modifier = Modifier.fillMaxWidth().testTag("booking_name_input"),
                    label = { Text("Your Full Name *", fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = AsgardLight,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = BgAttr,
                        unfocusedContainerColor = BgAttr
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        modifier = Modifier.weight(1f).testTag("booking_phone_input"),
                        label = { Text("Telephone Number *", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        modifier = Modifier.weight(1f).testTag("booking_email_input"),
                        label = { Text("Email Address *", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr
                        )
                    )
                }
            }
        }

        // Booking Action
        val selectedDate = calendarDays.getOrElse(selectedDateIndex) { "Upcoming Business Day" }

        Button(
            onClick = {
                val subject = "Bifröst Booking Request: $selectedDuration - $clientName"
                val body = """
                    Hello Caleb Holcomb,
                    
                    I would like to schedule a $selectedDuration.
                    
                    Requested Date: $selectedDate
                    Requested Time: $selectedTimeSlot
                    Client: $clientName
                    Phone: $clientPhone
                    Email: $clientEmail
                    Topic: $scopeTopic
                    
                    Zero-Custody Rule: Acknowledged.
                    Please confirm availability and calendar invitation.
                """.trimIndent()

                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${TyrCatalog.email}".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                safeStartActivity(context, intent, fallbackCopyText = body)
                Toast.makeText(context, "Booking invitation prepared! Dispatched to email client.", Toast.LENGTH_LONG).show()
            },
            enabled = clientName.isNotBlank() && (clientPhone.isNotBlank() || clientEmail.isNotBlank()),
            modifier = Modifier.fillMaxWidth().testTag("btn_confirm_booking"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Confirm $selectedDuration on $selectedDate at $selectedTimeSlot", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
                safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AsgardLight)
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Or Call Immediately: (659) 257-8043", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
