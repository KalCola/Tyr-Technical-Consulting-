package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.example.tyrconsulting.model.ClientQolManager
import com.example.tyrconsulting.model.ReferralManager
import com.example.tyrconsulting.model.SavedClientProfile
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class WorkOrder(
    val id: String,
    val clientName: String,
    val businessName: String,
    val email: String,
    val phone: String,
    val preferredContact: String,
    val selectedSkus: List<String>,
    val timeline: String,
    val domain: String,
    val description: String,
    val totalEstimatedPrice: String,
    val timestamp: String,
    val status: String = "INTAKE_SUBMITTED"
)

@Composable
fun ClientIntakeView(
    preSelectedServices: List<ServiceItem> = emptyList(),
    onOrderSubmitted: ((WorkOrder) -> Unit)? = null,
    onChainToSowAndStripe: ((WorkOrder) -> Unit)? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var clientName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var preferredContact by remember { mutableStateOf("Email") }
    var timeline by remember { mutableStateOf("Standard (7-14 Days)") }
    var domain by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var referralPartnerCode by remember { mutableStateOf("") }
    var zeroCustodyAcknowledged by remember { mutableStateOf(true) }

    val selectedSkus = remember {
        mutableStateListOf<String>().apply {
            if (preSelectedServices.isNotEmpty()) {
                addAll(preSelectedServices.map { it.sku })
            } else {
                add("SVC-WEB-01")
            }
        }
    }

    var submittedOrder by remember { mutableStateOf<WorkOrder?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val allServices = TyrCatalog.services

    fun calculateTotalEstimated(): String {
        var total = 0
        selectedSkus.forEach { sku ->
            val svc = allServices.find { it.sku == sku }
            if (svc != null) {
                val cleaned = svc.price.replace("$", "").replace(",", "").trim()
                cleaned.toIntOrNull()?.let { total += it }
            }
        }
        return if (total > 0) "$$total" else "Custom SLA Quote"
    }

    fun submitIntake() {
        if (clientName.isBlank()) {
            errorMessage = "Please enter your full name."
            return
        }
        if (email.isBlank() && phone.isBlank()) {
            errorMessage = "Please enter at least an email or phone number for intake confirmation."
            return
        }
        if (selectedSkus.isEmpty()) {
            errorMessage = "Please select at least one service SKU."
            return
        }
        if (!zeroCustodyAcknowledged) {
            errorMessage = "You must acknowledge the Zero-Custody Covenant."
            return
        }

        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomSuffix = (10..99).random()
        val orderId = "WO-$dateStr-$randomSuffix"

        val order = WorkOrder(
            id = orderId,
            clientName = clientName,
            businessName = businessName,
            email = email,
            phone = phone,
            preferredContact = preferredContact,
            selectedSkus = selectedSkus.toList(),
            timeline = timeline,
            domain = domain,
            description = description,
            totalEstimatedPrice = calculateTotalEstimated(),
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )

        submittedOrder = order
        errorMessage = null

        if (referralPartnerCode.isNotBlank()) {
            ReferralManager.recordIntake(context)
        }

        onOrderSubmitted?.invoke(order)
    }

    if (submittedOrder != null) {
        val order = submittedOrder!!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgObsidian)
                .padding(16.dp)
                .testTag("intake_confirmation_view"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(VanaheimMain)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = VanaheimLight)
                            Text("WORK ORDER INTAKE CONFIRMED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Order ID: ${order.id}", color = AsgardLight, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Timestamp: ${order.timestamp}", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Client: ${order.clientName} (${if (order.businessName.isNotBlank()) order.businessName else "Independent"})", color = TextMain, fontSize = 12.sp)
                        Text("Contact: ${if (order.email.isNotBlank()) order.email else order.phone} [Preferred: ${order.preferredContact}]", color = TextSecondary, fontSize = 12.sp)
                        Text("Timeline: ${order.timeline}", color = TextSecondary, fontSize = 12.sp)
                        Text("Estimated Total: ${order.totalEstimatedPrice}", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Selected SKUs:", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        order.selectedSkus.forEach { sku ->
                            val s = allServices.find { it.sku == sku }
                            Text("• $sku: ${s?.title ?: ""} (${s?.price ?: ""})", color = TextSecondary, fontSize = 11.sp)
                        }
                        if (order.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scope Notes:", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(order.description, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BgAttr),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MidgardMain)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ᛏ ZERO-CUSTODY PLEDGE ACTIVE", color = MidgardLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "0 Raw Financial Data Stored. Raw intake files and credentials are held strictly in air-gapped memory and purged (0-Day) upon deliverable acceptance.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onChainToSowAndStripe != null) {
                        Button(
                            onClick = { onChainToSowAndStripe(order) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("intake_chain_to_sow_stripe_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Chain to E-Sign SOW & Stripe Deposit", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val subject = "Engagement Intake: [${order.id}] ${order.clientName}"
                            val body = """
Hello Caleb TR Holcomb d/b/a TYR,

I have submitted an intake work order under the Bifröst protocol.

Work Order ID: ${order.id}
Client: ${order.clientName}
Business: ${order.businessName}
Contact: ${order.email} / ${order.phone}
Preferred Contact: ${order.preferredContact}
Timeline: ${order.timeline}
Estimated Value: ${order.totalEstimatedPrice}

Selected Services:
${order.selectedSkus.joinToString("\n") { "- $it" }}

Project Scope & Description:
${order.description}
${if (referralPartnerCode.isNotBlank()) "Referral Partner Code: $referralPartnerCode (10% Bounty Attributed)\n" else ""}
Zero-Custody Covenant: Acknowledged

Please transmit binding Statement of Work (SOW).
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${TyrCatalog.email}".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            safeStartActivity(context, intent, fallbackCopyText = body)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intake_dispatch_email_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Intake Brief via Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val receiptText = buildString {
                                appendLine("TYR PROTOCOL WORK ORDER RECEIPT")
                                appendLine("Order ID: ${order.id} | Timestamp: ${order.timestamp}")
                                appendLine("Client: ${order.clientName} (${order.businessName.ifBlank { "Independent" }})")
                                appendLine("Contact: ${order.email} / ${order.phone}")
                                appendLine("Estimated Total: ${order.totalEstimatedPrice}")
                                appendLine("Selected SKUs: ${order.selectedSkus.joinToString(", ")}")
                                appendLine("Scope: ${order.description}")
                            }
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("Intake Receipt ${order.id}", receiptText))
                            Toast.makeText(context, "Work Order Receipt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intake_copy_receipt_btn"),
                        border = BorderStroke(1.dp, AsgardLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Work Order Receipt Text", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
                            safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intake_call_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AsgardMain, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Consultation: ${TyrCatalog.phone}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            submittedOrder = null
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Return to Catalog", fontSize = 12.sp)
                    }
                }
            }
        }
        return
    }

    // Intake Form View
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgObsidian)
            .padding(16.dp)
            .testTag("client_intake_form"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MidgardMain, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ᛏ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Text(
                        "CLIENT INTAKE & WORK ORDER DISPATCH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Initiate defensive systems, document overhaul, or financial expense auditing under binding SLA covenants.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDC2626))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                        Text(errorMessage!!, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 1: Client Information + 1-Tap Autofill & Save Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. CLIENT IDENTIFIER", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val saved = ClientQolManager.getSavedProfile(context)
                                    if (!saved.isEmpty()) {
                                        clientName = saved.fullName
                                        businessName = saved.organization
                                        email = saved.email
                                        phone = saved.phone
                                        domain = saved.domain
                                        preferredContact = saved.preferredChannel
                                        errorMessage = null
                                        Toast.makeText(context, "Autofilled saved client profile!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Enter your details below and tap 'Save Profile' for future 1-tap autofill.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, AsgardLight),
                                modifier = Modifier.testTag("intake_autofill_profile_btn")
                            ) {
                                Icon(Icons.Default.PersonPin, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Autofill", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (clientName.isBlank() && email.isBlank() && phone.isBlank()) {
                                        Toast.makeText(context, "Enter name or contact first to save profile.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        ClientQolManager.saveProfile(
                                            context,
                                            SavedClientProfile(
                                                clientName = clientName.trim(),
                                                businessName = businessName.trim(),
                                                email = email.trim(),
                                                phone = phone.trim(),
                                                domain = domain.trim(),
                                                preferredContact = preferredContact
                                            )
                                        )
                                        Toast.makeText(context, "Saved client profile on device for 1-tap autofill!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF34D399)),
                                modifier = Modifier.testTag("intake_save_profile_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Profile", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it; errorMessage = null },
                        label = { Text("Client Full Name *", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = MidgardMain,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business / Organization Name (Optional)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = MidgardMain,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email Address *", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain,
                                focusedBorderColor = MidgardMain,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; errorMessage = null },
                            label = { Text("Phone Number", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain,
                                focusedBorderColor = MidgardMain,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            singleLine = true
                        )
                    }

                    Column {
                        Text("Preferred Dispatch Channel:", color = TextMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("Email", "Phone", "SMS Text").forEach { method ->
                                val isSelected = preferredContact == method
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { preferredContact = method },
                                    label = { Text(method, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MidgardMain,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = referralPartnerCode,
                        onValueChange = { referralPartnerCode = it },
                        label = { Text("Referral Partner Code (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. TYR-AMBASSADOR-01", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("referral_code_input"),
                        leadingIcon = { Text("🤝", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = AsgardLight,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        singleLine = true,
                        supportingText = {
                            Text(
                                "Attributing an ambassador or partner unlocks 50% rev-share bounty tracking.",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    )
                }
            }
        }

        // Section 2: Selected Service SKUs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2. SERVICE SELECTION (${selectedSkus.size} Active)", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(calculateTotalEstimated(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Text("Tap to add or remove services from your work order:", color = TextMuted, fontSize = 10.sp)

                    allServices.forEach { svc ->
                        val isChecked = selectedSkus.contains(svc.sku)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isChecked) Color(0xFF1E1418) else BgAttr,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isChecked) MidgardMain else BorderSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    if (isChecked) {
                                        if (selectedSkus.size > 1) selectedSkus.remove(svc.sku)
                                    } else {
                                        selectedSkus.add(svc.sku)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(svc.runePill, color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(svc.sku, color = MidgardLight, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(svc.title, color = TextMain, fontSize = 11.sp, maxLines = 1)
                            }
                            Text(svc.price, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 3: Project Specifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. SCOPE & TIMELINE", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                    Column {
                        Text("Target Turnaround:", color = TextMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("Standard (7-14 Days)", "Rush (48-72h)", "Flexible").forEach { opt ->
                                val isSelected = timeline == opt
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { timeline = opt },
                                    label = { Text(opt, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AsgardMain,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("Existing Domain / Tech Stack (If Applicable)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = MidgardMain,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Project Goals, Priorities & Requirements", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain,
                            focusedBorderColor = MidgardMain,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }
            }
        }

        // Section 4: Zero-Custody Acknowledgment & Submit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131015)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MidgardMain)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { zeroCustodyAcknowledged = !zeroCustodyAcknowledged }
                    ) {
                        Checkbox(
                            checked = zeroCustodyAcknowledged,
                            onCheckedChange = { zeroCustodyAcknowledged = it },
                            colors = CheckboxDefaults.colors(checkedColor = MidgardMain)
                        )
                        Column {
                            Text("Zero-Custody Covenant Acknowledged", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                "0 Raw Financial Data Stored. Statements and credentials purged 0-Day upon project completion.",
                                color = TextMuted,
                                fontSize = 9.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = { submitIntake() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_intake_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Submit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate & Dispatch Work Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (onChainToSowAndStripe != null) {
                        OutlinedButton(
                            onClick = {
                                submitIntake()
                                submittedOrder?.let { created ->
                                    onChainToSowAndStripe(created)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_and_chain_sow_stripe_btn"),
                            border = BorderStroke(1.dp, Color(0xFF635BFF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ 1-Tap Submit & Chain to E-Sign SOW + Stripe", color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
