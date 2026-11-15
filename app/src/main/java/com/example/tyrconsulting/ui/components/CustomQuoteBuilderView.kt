package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import java.util.UUID

data class CustomLineItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val budgetStr: String,
    val estimatedAmount: Double,
    val turnaround: String,
    val isCustomUnlisted: Boolean = true,
    val isMonthly: Boolean = false,
    val sku: String = "CUSTOM-TASK"
)

@Composable
fun CustomQuoteBuilderView(
    catalogServicesInCart: MutableList<ServiceItem>,
    onNavigateToIntake: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToPaymentClarity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Custom unlisted tasks added by user
    val customTasks = remember { mutableStateListOf<CustomLineItem>() }

    // Inputs for adding a custom unlisted task
    var customTitle by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }
    var customBudget by remember { mutableStateOf("") }
    var customTurnaround by remember { mutableStateOf("Standard (24–48h)") }
    var customIsMonthly by remember { mutableStateOf(false) }
    var catalogSearchQuery by remember { mutableStateOf("") }
    var showCatalogPicker by remember { mutableStateOf(false) }

    // Quick suggestion templates for unlisted scopes
    val quickSuggestions = listOf(
        "Local NAS & Automated Backup Script" to 125.0,
        "Custom Python Web / Invoice Scraper" to 145.0,
        "WireGuard / Tailscale Mesh VPN Setup" to 110.0,
        "Bare-Metal Linux Server Hardening" to 160.0,
        "Vendor SOW Contract Redline Review" to 135.0,
        "Subcontractor Onboarding Document Suite" to 175.0
    )

    // Calculate Catalog Total
    val catalogTotal = remember(catalogServicesInCart.size) {
        catalogServicesInCart.sumOf { s ->
            extractPrice(s.price)
        }
    }

    // Calculate Custom Scope Total
    val customTotal = remember(customTasks.size, customTasks.map { it.estimatedAmount }) {
        customTasks.sumOf { it.estimatedAmount }
    }

    val grandTotal = catalogTotal + customTotal

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quote_builder_header_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MidgardMain, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛠️", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "CUSTOM QUOTE & TASK BUILDER",
                            color = AsgardLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Plug-and-play tasks, services, and bespoke solutions",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Prominent Note on Unlisted Services
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, VanaheimLight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("unlisted_services_notice")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("📌", fontSize = 14.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Bespoke Scope Policy — Pay Only For What You Need:",
                                color = VanaheimLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Not all technical services or bespoke engineering solutions are presently listed or offered in this app catalog. You can freely plug and play any custom task, automation script, or consulting scope below to build an exact, tailored quote instead of having to choose only from pre-set offerings.",
                                color = TextMain,
                                fontSize = 10.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Billing Clarity Callout
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("quote_builder_payment_clarity_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("💳 1-Time vs Monthly Clarity", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("All custom tasks are quoted as One-Time deliverables ($0/mo recurring) unless explicitly specified as a monthly retainer.", color = TextSecondary, fontSize = 9.sp)
                        }
                        TextButton(
                            onClick = onNavigateToPaymentClarity,
                            colors = ButtonDefaults.textButtonColors(contentColor = AsgardLight),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("View Guide ↗", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 1: Plug-and-Play Catalog Services
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Text("📦", fontSize = 14.sp)
                        Text(
                            text = "CATALOG SERVICES (${catalogServicesInCart.size} SELECTED)",
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    TextButton(
                        onClick = { showCatalogPicker = !showCatalogPicker },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("toggle_catalog_picker_btn")
                    ) {
                        Text(
                            if (showCatalogPicker) "Hide Catalog" else "+ Browse & Add",
                            fontSize = 11.sp,
                            color = AsgardLight
                        )
                    }
                }

                // Selected Catalog Items Chips
                if (catalogServicesInCart.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        catalogServicesInCart.forEach { service ->
                            Surface(
                                color = BgAttr,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                color = MidgardMain.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "CATALOG",
                                                    color = MidgardLight,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = service.title,
                                                color = TextMain,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = "${service.sku} • ${service.priceSub}",
                                            color = TextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = service.price,
                                            color = AsgardLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        IconButton(
                                            onClick = { catalogServicesInCart.remove(service) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove service",
                                                tint = MidgardLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No catalog services added yet. Use '+ Browse & Add' to pick pre-defined offerings or add custom unlisted tasks below.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Expandable Catalog Selector
                if (showCatalogPicker) {
                    HorizontalDivider(color = BorderSubtle)
                    OutlinedTextField(
                        value = catalogSearchQuery,
                        onValueChange = { catalogSearchQuery = it },
                        placeholder = { Text("Filter catalog services...", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("catalog_filter_in_quote"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr,
                            focusedBorderColor = MidgardMain,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    val matchingServices = TyrCatalog.services.filter { s ->
                        catalogSearchQuery.isEmpty() ||
                                s.title.contains(catalogSearchQuery, ignoreCase = true) ||
                                s.sku.contains(catalogSearchQuery, ignoreCase = true)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        matchingServices.forEach { s ->
                            val isSelected = catalogServicesInCart.contains(s)
                            Surface(
                                color = if (isSelected) MidgardMain.copy(alpha = 0.15f) else BgAttr,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) MidgardMain else BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(s.title, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${s.sku} • ${s.price}", color = AsgardLight, fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = {
                                            if (isSelected) catalogServicesInCart.remove(s) else catalogServicesInCart.add(s)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFF374151) else MidgardMain,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(if (isSelected) "✓ In Quote" else "+ Add", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Add Custom Unlisted Tasks/Services
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_task_input_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("✍️", fontSize = 14.sp)
                    Text(
                        text = "ADD CUSTOM UNLISTED TASK OR SERVICE",
                        color = VanaheimLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Request custom software, private server scripts, bespoke contract redlines, or automation tasks not in our standard catalog:",
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                // Quick Suggestion Chips
                Text(
                    text = "Quick Unlisted Suggestions (Tap to fill):",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { (title, price) ->
                        SuggestionChip(
                            onClick = {
                                customTitle = title
                                customBudget = "$${price.toInt()}"
                                customDescription = "Turnkey bespoke implementation: $title with verification documentation and clean Zero-Custody handover."
                            },
                            label = { Text(title, fontSize = 9.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = BgAttr,
                                labelColor = TextMain
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = BorderSubtle
                            )
                        )
                    }
                }

                // Custom Task Title Input
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Task / Service Title *", fontSize = 11.sp) },
                    placeholder = { Text("e.g. WireGuard VPN Mesh / Custom Scraper", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_task_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgAttr,
                        unfocusedContainerColor = BgAttr,
                        focusedBorderColor = AsgardMain,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Custom Task Description / Scope
                OutlinedTextField(
                    value = customDescription,
                    onValueChange = { customDescription = it },
                    label = { Text("Deliverable Requirements / Scope Notes", fontSize = 11.sp) },
                    placeholder = { Text("Describe specific needs, environment (OS, cloud, local), and required output formats...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_task_desc_input"),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgAttr,
                        unfocusedContainerColor = BgAttr,
                        focusedBorderColor = AsgardMain,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Budget & Turnaround Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customBudget,
                        onValueChange = { customBudget = it },
                        label = { Text("Target Budget ($)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 150 or Flexible", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("custom_task_budget_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr,
                            focusedBorderColor = AsgardMain,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = customTurnaround,
                        onValueChange = { customTurnaround = it },
                        label = { Text("SLA / Turnaround", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("custom_task_turnaround_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BgAttr,
                            unfocusedContainerColor = BgAttr,
                            focusedBorderColor = AsgardMain,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Billing Classification for this custom task
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !customIsMonthly,
                        onClick = { customIsMonthly = false },
                        label = {
                            Text(
                                "⚡ One-Time Fee ($0/Mo)",
                                fontSize = 10.sp,
                                fontWeight = if (!customIsMonthly) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AsgardMain.copy(alpha = 0.35f),
                            selectedLabelColor = AsgardLight
                        ),
                        modifier = Modifier.weight(1f).testTag("custom_task_onetime_toggle")
                    )
                    FilterChip(
                        selected = customIsMonthly,
                        onClick = { customIsMonthly = true },
                        label = {
                            Text(
                                "🔄 Monthly Retainer",
                                fontSize = 10.sp,
                                fontWeight = if (customIsMonthly) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF047857).copy(alpha = 0.35f),
                            selectedLabelColor = Color(0xFF34D399)
                        ),
                        modifier = Modifier.weight(1f).testTag("custom_task_monthly_toggle")
                    )
                }

                // Add Button
                Button(
                    onClick = {
                        if (customTitle.isNotBlank()) {
                            val parsedAmount = extractPrice(customBudget)
                            customTasks.add(
                                CustomLineItem(
                                    title = customTitle.trim(),
                                    description = customDescription.trim().ifEmpty { "Tailored scope according to client specifications." },
                                    budgetStr = if (customBudget.isBlank()) "Flexible / TBD" else if (!customBudget.startsWith("$")) "$$customBudget" else customBudget,
                                    estimatedAmount = parsedAmount,
                                    turnaround = customTurnaround.trim().ifEmpty { "Standard (24–48h)" },
                                    isMonthly = customIsMonthly
                                )
                            )
                            customTitle = ""
                            customDescription = ""
                            customBudget = ""
                            Toast.makeText(context, "Custom task plugged into quote!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a title for the custom task", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_custom_task_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VanaheimMain,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plug Custom Task Into Quote", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Section 3: Custom Unlisted Tasks in Active Quote
        if (customTasks.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CUSTOM UNLISTED TASKS (${customTasks.size})",
                            color = VanaheimLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        TextButton(
                            onClick = { customTasks.clear() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Clear Custom", fontSize = 10.sp, color = MidgardLight)
                        }
                    }

                    customTasks.forEach { task ->
                        Surface(
                            color = BgAttr,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = VanaheimMain.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "BESPOKE",
                                                color = VanaheimLight,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            color = if (task.isMonthly) Color(0xFF047857).copy(alpha = 0.25f) else AsgardMain.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, if (task.isMonthly) Color(0xFF34D399).copy(alpha = 0.5f) else AsgardLight.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                if (task.isMonthly) "🔄 MONTHLY RETAINER" else "⚡ ONE-TIME ($0/MO)",
                                                color = if (task.isMonthly) Color(0xFF34D399) else AsgardLight,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = task.title,
                                            color = TextMain,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = task.description,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "Budget: ${task.budgetStr}",
                                            color = AsgardLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• ${task.turnaround}",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { customTasks.remove(task) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove custom task",
                                        tint = MidgardLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Live Total & Scope Summary
        val totalItemsCount = catalogServicesInCart.size + customTasks.size
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quote_summary_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, AsgardMain),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ESTIMATED TOTAL & SCOPE SUMMARY",
                    color = AsgardLight,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Line Items:", color = TextSecondary, fontSize = 11.sp)
                    Text("$totalItemsCount (${catalogServicesInCart.size} Catalog + ${customTasks.size} Custom)", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (catalogServicesInCart.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Catalog Services Subtotal:", color = TextSecondary, fontSize = 11.sp)
                        Text("$${catalogTotal.toInt()}", color = TextMain, fontSize = 11.sp)
                    }
                }

                if (customTasks.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Custom Tasks Subtotal:", color = TextSecondary, fontSize = 11.sp)
                        Text("$${customTotal.toInt()}", color = TextMain, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = BorderSubtle)

                val oneTimeCatalogSum = catalogServicesInCart.filter {
                    !it.priceSub.contains("Month", true) &&
                    !it.priceSub.contains("Mo", true) &&
                    !it.priceSub.contains("Retainer", true) &&
                    it.page !in listOf(4, 5, 8)
                }.sumOf { extractPrice(it.price) }
                val monthlyCatalogSum = catalogServicesInCart.filter {
                    it.priceSub.contains("Month", true) ||
                    it.priceSub.contains("Mo", true) ||
                    it.priceSub.contains("Retainer", true) ||
                    it.page in listOf(4, 5, 8)
                }.sumOf { extractPrice(it.price) }

                val oneTimeCustomSum = customTasks.filter { !it.isMonthly }.sumOf { it.estimatedAmount }
                val monthlyCustomSum = customTasks.filter { it.isMonthly }.sumOf { it.estimatedAmount }

                val totalOneTimeCombined = oneTimeCatalogSum + oneTimeCustomSum
                val totalMonthlyCombined = monthlyCatalogSum + monthlyCustomSum

                // Dual Bucket Billing Breakdown
                Surface(
                    color = BgAttr,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("quote_builder_dual_breakdown")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PAYMENT MODEL BREAKDOWN", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            TextButton(
                                onClick = onNavigateToPaymentClarity,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(20.dp).testTag("open_payment_guide_btn")
                            ) {
                                Text("Payment Guide ↗", color = AsgardLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = BgCard,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, AsgardMain.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("⚡ ONE-TIME CAPITAL", color = AsgardLight, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                                    Text("$${totalOneTimeCombined.toInt()}", color = TextMain, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Text("$0/mo ongoing fees", color = TextMuted, fontSize = 8.sp)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = BgCard,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("🔄 RECURRING MONTHLY", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                                    Text("$${totalMonthlyCombined.toInt()}/mo", color = Color(0xFF34D399), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Text("Billed 1st of month", color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Combined Total Value:", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Pay only for selected scope", color = TextMuted, fontSize = 9.sp)
                    }
                    Text(
                        text = if (grandTotal > 0) "$${grandTotal.toInt()}" else "Flexible / TBD",
                        color = AsgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                // Dispatch & Action Buttons
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val subject = "TYR Custom Quote Request — $totalItemsCount Item(s) ($${grandTotal.toInt()})"
                            val body = buildString {
                                appendLine("TYR TECHNICAL CONSULTING — CUSTOM QUOTE REQUEST")
                                appendLine("Covenant: Asynchronous Remote Delivery • Zero-Custody Guarantee")
                                appendLine("--------------------------------------------------")
                                if (catalogServicesInCart.isNotEmpty()) {
                                    appendLine("[CATALOG SERVICES]")
                                    catalogServicesInCart.forEach { s ->
                                        appendLine("• ${s.sku}: ${s.title} — ${s.price}")
                                    }
                                    appendLine("Catalog Subtotal: $${catalogTotal.toInt()}")
                                    appendLine()
                                }
                                if (customTasks.isNotEmpty()) {
                                    appendLine("[CUSTOM UNLISTED TASKS]")
                                    customTasks.forEach { t ->
                                        appendLine("• ${t.title} (${t.turnaround})")
                                        appendLine("  Scope: ${t.description}")
                                        appendLine("  Target Budget: ${t.budgetStr}")
                                    }
                                    appendLine("Custom Subtotal: $${customTotal.toInt()}")
                                    appendLine()
                                }
                                appendLine("--------------------------------------------------")
                                appendLine("Combined Estimated Total: $${grandTotal.toInt()}")
                                appendLine("Notice: Client confirmed custom tailored scope request.")
                                appendLine("Delivery Window: Asynchronous (24–48 Hours standard)")
                                appendLine("Contact: tyr@tyrprotocol.pro • +1 (659) 257-8043")
                            }
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${TyrCatalog.email}".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            safeStartActivity(context, intent, fallbackCopyText = body)
                        },
                        modifier = Modifier.weight(1f).testTag("dispatch_quote_email_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MidgardMain,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispatch Quote", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val summary = buildString {
                                appendLine("TYR TECHNICAL CONSULTING — CUSTOM SOW QUOTE")
                                appendLine("--------------------------------------------------")
                                if (catalogServicesInCart.isNotEmpty()) {
                                    appendLine("CATALOG ITEMS:")
                                    catalogServicesInCart.forEach { s ->
                                        appendLine("• [${s.sku}] ${s.title}: ${s.price}")
                                    }
                                }
                                if (customTasks.isNotEmpty()) {
                                    appendLine("CUSTOM UNLISTED TASKS:")
                                    customTasks.forEach { t ->
                                        appendLine("• ${t.title}: ${t.budgetStr} (${t.turnaround})")
                                        appendLine("  Scope: ${t.description}")
                                    }
                                }
                                appendLine("--------------------------------------------------")
                                appendLine("Total Estimate: $${grandTotal.toInt()}")
                                appendLine("Zero-Custody: Raw files purged Day 0 upon completion.")
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("TYR Custom Quote", summary)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Custom quote copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.testTag("copy_quote_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy SOW", fontSize = 11.sp, color = TextMain)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToIntake,
                        modifier = Modifier.weight(1f).testTag("quote_to_intake_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Client Intake Form", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${TyrCatalog.phone.replace("[^0-9+]".toRegex(), "")}".toUri()
                            }
                            safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.testTag("quote_call_consult_btn")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = AsgardLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Review", fontSize = 11.sp, color = TextMain)
                    }
                }
            }
        }
    }
}

// Helper function to extract numeric price from strings like "$245", "$85 / $115", "150"
private fun extractPrice(priceStr: String): Double {
    if (priceStr.isBlank()) return 0.0
    val cleaned = priceStr.replace(",", "")
    val match = Regex("""\$?(\d+(\.\d+)?)""").find(cleaned)
    return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
}
