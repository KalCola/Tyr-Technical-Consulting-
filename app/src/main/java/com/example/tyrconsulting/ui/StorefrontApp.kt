package com.example.tyrconsulting.ui

import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.ClientQolManager
import com.example.tyrconsulting.model.GrowthMarketingManager
import com.example.tyrconsulting.model.SavedClientProfile
import com.example.tyrconsulting.model.ServiceItem
import com.example.tyrconsulting.model.StripePaymentManager
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.components.*
import com.example.tyrconsulting.ui.theme.*

@Composable
fun StorefrontApp(initialDeepLinkUri: String? = null) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    val cartServices = remember { mutableStateListOf<ServiceItem>() }

    var fontScaleMultiplier by remember { mutableFloatStateOf(ClientQolManager.getFontScale(context)) }
    var highContrastMode by remember { mutableStateOf(ClientQolManager.isHighContrast(context)) }
    var compactCardMode by remember { mutableStateOf(ClientQolManager.isCompactCardMode(context)) }
    var storefrontMode by remember { mutableStateOf(ClientQolManager.getStorefrontMode(context)) }
    var recentTabs by remember { mutableStateOf(ClientQolManager.getRecentTabs(context)) }
    var favoriteSkus by remember { mutableStateOf(ClientQolManager.getFavoriteSkus(context)) }
    var showAccessibilityProfileModal by remember { mutableStateOf(false) }

    val updateStorefrontMode: (String) -> Unit = { raw ->
        val normalized = if (raw.equals("BUSINESS", ignoreCase = true)) "BUSINESS" else "PERSONAL"
        storefrontMode = normalized
        ClientQolManager.setStorefrontMode(context, normalized)
    }

    var activeInquiryItem by remember { mutableStateOf<ServiceItem?>(null) }
    var showInquiryDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showGeminiOptionsDialog by remember { mutableStateOf(false) }
    var showCommandCenterModal by remember { mutableStateOf(false) }
    var showTopOptionsMenu by remember { mutableStateOf(false) }
    var stripeCheckoutItems by remember { mutableStateOf<List<ServiceItem>?>(null) }
    var chainedWorkOrderForSow by remember { mutableStateOf<WorkOrder?>(null) }
    var deepLinkBannerMessage by remember { mutableStateOf<String?>(null) }
    val activeOrders = remember { mutableStateListOf<WorkOrder>() }

    LaunchedEffect(selectedTab) {
        recentTabs = ClientQolManager.recordRecentTab(context, selectedTab)
    }

    LaunchedEffect(initialDeepLinkUri) {
        if (!initialDeepLinkUri.isNullOrBlank()) {
            val parsedUri = initialDeepLinkUri.toUri()
            val refCode = parsedUri.getQueryParameter("ref")?.trim().orEmpty()
            val skuCode = parsedUri.getQueryParameter("sku")?.trim()?.uppercase().orEmpty()
            val pageParam = parsedUri.getQueryParameter("page")?.toIntOrNull()
            val modeParam = (parsedUri.getQueryParameter("mode") ?: parsedUri.getQueryParameter("face"))?.trim().orEmpty()

            if (modeParam.equals("business", ignoreCase = true)) {
                updateStorefrontMode("BUSINESS")
            } else if (modeParam.equals("personal", ignoreCase = true)) {
                updateStorefrontMode("PERSONAL")
            }

            if (refCode.isNotBlank()) {
                val applied = GrowthMarketingManager.setActiveInboundRefCode(context, refCode)
                deepLinkBannerMessage = "Referral Code $applied active — 10% Welcome Credit unlocked!"
            }

            if (skuCode.isNotBlank()) {
                val matched = TyrCatalog.services.find { it.sku.equals(skuCode, ignoreCase = true) }
                if (matched != null) {
                    if (!cartServices.contains(matched)) cartServices.add(matched)
                    selectedTab = matched.page.coerceIn(1, 9)
                    deepLinkBannerMessage = (deepLinkBannerMessage?.plus(" • ") ?: "") +
                        "Focused ${matched.sku} (${matched.price}) & added to Quote Cart."
                }
            } else if (pageParam != null && pageParam in 0..27) {
                selectedTab = pageParam
            }

            val (msg, record) = StripePaymentManager.reconcileCheckoutDeepLink(context, initialDeepLinkUri)
            if (msg.isNotBlank()) {
                deepLinkBannerMessage = msg
                if (record != null) {
                    selectedTab = 25
                }
            }
        }
    }

    fun getScreenTitle(tab: Int): String = when (tab) {
        0 -> "3D Omni-Directional Card"
        in 1..9 -> "Nine Realms Catalog"
        10 -> "Estimate & Cart"
        11 -> "Client Intake"
        12 -> "Order Tracker"
        13 -> "AI Systems Advisor"
        14 -> "Norse Image Forge"
        15 -> "Google Chat Relay"
        16 -> "E-Sign SOW"
        17 -> "Zero-Custody Vault"
        18 -> "Schedule Consultation"
        19 -> "Partner & Referral Vault"
        20 -> "Custom Quote Builder"
        21 -> "Payment Clarity"
        22 -> "About TYR & Principal"
        23 -> "Support & SLA Center"
        24 -> "Universal Privacy Policy"
        25 -> "Stripe Checkout & Receipts"
        26 -> "Field Forensics & Telemetry"
        27 -> "$0 Growth & Self-Audit"
        else -> "Storefront"
    }

    // Intercept back button when not on Home/3D Card
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val onDialPhone = {
        val intent = Intent(Intent.ACTION_DIAL, "tel:${TyrCatalog.phoneRaw}".toUri())
        safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.phone)
    }

    val onSendEmail = { subject: String, body: String ->
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:${TyrCatalog.email}".toUri()
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        safeStartActivity(context, intent, fallbackCopyText = TyrCatalog.email)
    }

    val baseDensity = LocalDensity.current
    val effectiveDensity = remember(baseDensity, fontScaleMultiplier) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * fontScaleMultiplier
        )
    }

    CompositionLocalProvider(LocalDensity provides effectiveDensity) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (highContrastMode) Color.Black else BgObsidian,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (highContrastMode) Color(0xFF05070B) else BgSurface)
            ) {
                Surface(
                    color = if (highContrastMode) Color(0xFF05070B) else BgSurface,
                    contentColor = TextMain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showCommandCenterModal = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Brush.linearGradient(listOf(MidgardMain, Color(0xFF7F1D1D))),
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ᛏ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    "TYR Protocol",
                                    color = TextMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (storefrontMode == "BUSINESS") "🏢" else "👤"} ${getScreenTitle(selectedTab)}",
                                    color = if (storefrontMode == "BUSINESS") VanaheimLight else AsgardLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 0. Accessibility & Saved Client Profile Quick Button
                            IconButton(
                                onClick = { showAccessibilityProfileModal = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("top_accessibility_profile_button")
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Accessibility & Client Profile",
                                    tint = if (fontScaleMultiplier > 1.0f || highContrastMode) Color(0xFF34D399) else TextSecondary
                                )
                            }

                            // 1. Quick Command Hub Launcher
                            IconButton(
                                onClick = { showCommandCenterModal = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("top_command_hub_button")
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = "Command Hub", tint = AsgardLight)
                            }

                            // 2. Estimate / Cart with Live Badge
                            IconButton(
                                onClick = { selectedTab = if (cartServices.isNotEmpty()) 10 else 20 },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("top_cart_estimate_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (cartServices.isNotEmpty()) {
                                            Badge(containerColor = MidgardMain) {
                                                Text("${cartServices.size}", fontSize = 9.sp, color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart Estimate", tint = TextMain)
                                }
                            }

                            // 3. Direct Phone Call Action
                            IconButton(
                                onClick = onDialPhone,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("top_call_button")
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call Consult", tint = MidgardLight)
                            }

                            // 4. Clean Overflow Menu
                            Box {
                                IconButton(
                                    onClick = { showTopOptionsMenu = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("top_more_options_button")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextMuted)
                                }

                                DropdownMenu(
                                    expanded = showTopOptionsMenu,
                                    onDismissRequest = { showTopOptionsMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (storefrontMode == "BUSINESS") "👤 Switch to Personal Mode" else "🏢 Switch to Business Mode",
                                                fontSize = 11.sp
                                            )
                                        },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            updateStorefrontMode(if (storefrontMode == "BUSINESS") "PERSONAL" else "BUSINESS")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🚀 $0 Growth, 60s Audit & QR Flyer", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 27
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💳 Stripe Checkout & Digital Receipts", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 25
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🔬 Field Forensics, EXIF Scrubber & Telemetry", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 26
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🏛️ About TYR & Principal Bio", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 22
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🛟 Support & SLA Contact Center", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 23
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📜 Universal Privacy Policy (URL/MD/HTML)", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 24
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🤝 Partner & Referral Link Vault", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 19
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("♿ Text Scaling & Client Autofill Profile", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            showAccessibilityProfileModal = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💳 Payment Clarity (1-Time vs Monthly)", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 21
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🛠️ Custom Quote Builder", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            selectedTab = 20
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🛡️ Zero-Custody Covenant Modal", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            showPrivacyDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("✨ AI Model Parameters", fontSize = 11.sp) },
                                        onClick = {
                                            showTopOptionsMenu = false
                                            showGeminiOptionsDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Navigation Tabs Bar
                DeckTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onOpenCommandCenter = { showCommandCenterModal = true },
                    isCompactMode = compactCardMode,
                    onToggleCompactMode = {
                        compactCardMode = !compactCardMode
                        ClientQolManager.setCompactCardMode(context, compactCardMode)
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )

                if (deepLinkBannerMessage != null) {
                    Surface(
                        color = Color(0xFF064E3B),
                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                deepLinkBannerMessage!!,
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { deepLinkBannerMessage = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = BgSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("pinned_bottom_action_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🪪", fontSize = 14.sp) },
                    label = { Text("Card", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AsgardLight,
                        selectedTextColor = AsgardLight,
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab in 1..9,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Services", modifier = Modifier.size(15.dp)) },
                    label = { Text("Catalog", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AsgardLight,
                        selectedTextColor = AsgardLight,
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 10 || selectedTab == 20 || selectedTab == 21,
                    onClick = { selectedTab = if (cartServices.isNotEmpty()) 10 else 20 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartServices.isNotEmpty()) {
                                    Badge(containerColor = MidgardMain) {
                                        Text("${cartServices.size}", fontSize = 8.sp, color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = "Quotes & Cart", modifier = Modifier.size(15.dp))
                        }
                    },
                    label = { Text("Quotes", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AsgardLight,
                        selectedTextColor = AsgardLight,
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 25,
                    onClick = { selectedTab = 25 },
                    icon = { Icon(Icons.Default.Payment, contentDescription = "Stripe & Ledger", modifier = Modifier.size(15.dp)) },
                    label = { Text("Stripe", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA5B4FC),
                        selectedTextColor = Color(0xFFA5B4FC),
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 26,
                    onClick = { selectedTab = 26 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Field Forensics", modifier = Modifier.size(15.dp)) },
                    label = { Text("Forensics", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF34D399),
                        selectedTextColor = Color(0xFF34D399),
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = showCommandCenterModal,
                    onClick = { showCommandCenterModal = true },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Category Drawer", modifier = Modifier.size(15.dp)) },
                    label = { Text("Drawer", fontSize = 8.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AsgardLight,
                        selectedTextColor = AsgardLight,
                        indicatorColor = BgAttr,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 0) {
                ExtendedFloatingActionButton(
                    onClick = onDialPhone,
                    icon = { Icon(Icons.Default.Phone, contentDescription = "Call") },
                    text = { Text("Call Consult", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    containerColor = MidgardMain,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_contact")
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            // DEDICATED STANDALONE 3D BUSINESS CARD PAGE:
            // Renders completely on its own page with NO parent scrollbars, NO list gesture stealing,
            // and full responsive immersion without truncation!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OmniBusinessCardStandalone(
                    onNavigateToCatalog = { selectedTab = 1 },
                    onOpenIntake = { selectedTab = 11 },
                    onOpenQuoteBuilder = { selectedTab = 20 },
                    onOpenPaymentClarity = { selectedTab = 21 },
                    storefrontMode = storefrontMode,
                    onStorefrontModeChanged = updateStorefrontMode,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (selectedTab in 1..10) {
            // STOREFRONT CATALOG & ESTIMATE PAGES (Tabs 1..10)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HeroSection(
                        onPhoneClick = onDialPhone,
                        onEmailClick = {
                            activeInquiryItem = null
                            showInquiryDialog = true
                        },
                        onPrivacyClick = { showPrivacyDialog = true },
                        onCardClick = { selectedTab = 0 },
                        storefrontMode = storefrontMode,
                        onStorefrontModeChanged = updateStorefrontMode,
                        onForgeClick = { selectedTab = 14 },
                        onSignClick = { selectedTab = 16 },
                        onVaultClick = { selectedTab = 17 },
                        onScheduleClick = { selectedTab = 18 },
                        onReferralClick = { selectedTab = 19 },
                        onCustomQuoteClick = { selectedTab = 20 },
                        onPaymentClarityClick = { selectedTab = 21 }
                    )
                }

                when (selectedTab) {
                    // Tab 1: Page 1: Midgard, Asgard, Vanaheim
                    1 -> {
                        items(TyrCatalog.services.filter { it.page == 1 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 2: Page 2: Alfheim, Nidavellir, Muspelheim
                    2 -> {
                        items(TyrCatalog.services.filter { it.page == 2 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 3: Page 3: Svartalfheim, Niflheim, Jotunheim
                    3 -> {
                        items(TyrCatalog.services.filter { it.page == 3 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 4: Page 4: Tech Care Plans
                    4 -> {
                        items(TyrCatalog.services.filter { it.page == 4 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 5: Page 5: Expense & Admin + Flagship Bundle
                    5 -> {
                        item {
                            BundleBanner(
                                bundle = TyrCatalog.flagshipBundle,
                                onInquireBundle = {
                                    activeInquiryItem = null
                                    showInquiryDialog = true
                                }
                            )
                        }
                        items(TyrCatalog.services.filter { it.page == 5 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 6: Page 6: Pure Remote Deliverables (No License or Certification Requirements)
                    6 -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                border = BorderStroke(1.dp, VanaheimLight.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("ᛏ", color = VanaheimLight, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        Text(
                                            "REMOTE DELIVERABLES • ZERO BARRIERS",
                                            color = VanaheimLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        "Pure asynchronous digital delivery. Zero trade license requirements, zero IT certifications needed, and zero ongoing SaaS hostage fees. Delivered in standard open formats (.md, .docx, .xlsx, .pdf, .sh, .ps1) with 100% master file ownership.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("⚡ 24–48h Turnaround", color = TextMain, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("🛡️ Day-0 Data Purge", color = VanaheimLight, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("📁 $0 SaaS Lock-in", color = AsgardLight, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                        items(TyrCatalog.services.filter { it.page == 6 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            WorkflowSection(
                                onStepAction = { step ->
                                    when (step.step) {
                                        1 -> selectedTab = 9 // jump to all realms
                                        2 -> showPrivacyDialog = true
                                        3 -> onDialPhone()
                                    }
                                }
                            )
                        }
                    }
                    // Tab 7: Page 7: B2B Procurement & Corporate Governance Suite
                    7 -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🏢", fontSize = 16.sp)
                                        Text(
                                            "B2B PROCUREMENT & CORPORATE GOVERNANCE",
                                            color = Color(0xFF60A5FA),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        "Enterprise-grade vendor documentation and cyber defense protocols. Designed for commercial firms, subcontractors, and expanding agencies. Delivered with full editable source documents (.docx, .pdf) and turnkey onboarding workflows.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("⚖️ MSA + SOW Suite", color = TextMain, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("🛡️ Anti-Wire Fraud SOP", color = Color(0xFF60A5FA), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(
                                            color = BgAttr,
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Text("⚡ Turnkey Setup", color = AsgardLight, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                        items(TyrCatalog.services.filter { it.page == 7 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 8: Page 8: Monthly Low-Effort B2B Retainers (Automated / High-Margin)
                    8 -> {
                        item {
                            BundleBanner(
                                bundle = TyrCatalog.corporateB2BBundle,
                                onInquireBundle = {
                                    activeInquiryItem = null
                                    showInquiryDialog = true
                                }
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🔄", fontSize = 16.sp)
                                        Text(
                                            "MONTHLY LOW-EFFORT B2B RETAINERS",
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        "Automated surveillance and proactive monitoring contracts. Scripted reconnaissance, synthetic uptime pinging, SaaS drift audits, and vendor renewal alerts deliver high enterprise value with streamlined, automated execution.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        items(TyrCatalog.services.filter { it.page == 8 }) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 9: All Realms (Full Catalog with Search & Filter)
                    9 -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_catalog_input"),
                                    placeholder = { Text("Search services, SKUs, or deliverables...", color = TextMuted, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = AsgardLight) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextMuted)
                                            }
                                        }
                                    },
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

                                // QoL Category Filter Pills
                                val categories = listOf(
                                    "ALL" to "All (22)",
                                    "FAV" to "★ Pinned Favorites (${favoriteSkus.size})",
                                    "REMOTE" to "⚡ Remote Deliverables",
                                    "B2B" to "🏢 B2B & Retainers",
                                    "TECH" to "🌐 Web & Email",
                                    "LEGAL" to "⚖️ Contracts & SOP",
                                    "FINANCE" to "💰 Finance & Ledger",
                                    "CARE" to "🛡️ Care Plans"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.forEach { (catId, catLabel) ->
                                        val isSel = selectedCategoryFilter == catId
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedCategoryFilter = catId },
                                            label = { Text(catLabel, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AsgardMain.copy(alpha = 0.3f),
                                                selectedLabelColor = AsgardLight,
                                                containerColor = BgAttr,
                                                labelColor = TextMuted
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSel,
                                                borderColor = if (isSel) AsgardLight else BorderSubtle
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        val filtered = TyrCatalog.services.filter { s ->
                            val matchesSearch = searchQuery.isEmpty() ||
                                    s.title.contains(searchQuery, ignoreCase = true) ||
                                    s.sku.contains(searchQuery, ignoreCase = true) ||
                                    s.meaning.contains(searchQuery, ignoreCase = true) ||
                                    s.bullets.any { b -> b.contains(searchQuery, ignoreCase = true) }

                            val matchesCategory = when (selectedCategoryFilter) {
                                "FAV" -> favoriteSkus.contains(s.sku)
                                "REMOTE" -> s.page == 6
                                "B2B" -> s.page in listOf(7, 8)
                                "TECH" -> s.realmId in listOf("midgard", "asgard", "vanaheim") && s.page <= 3
                                "LEGAL" -> s.realmId in listOf("nidavellir", "alfheim") && s.page <= 3
                                "FINANCE" -> s.realmId in listOf("svartalfheim", "niflheim", "jotunheim") && s.page <= 3
                                "CARE" -> s.page in listOf(4, 5)
                                else -> true
                            }
                            matchesSearch && matchesCategory
                        }
                        items(filtered) { service ->
                            ServiceCard(
                                service = service,
                                onInquire = {
                                    activeInquiryItem = it
                                    showInquiryDialog = true
                                },
                                onAddToEstimate = {
                                    if (cartServices.contains(it)) cartServices.remove(it) else cartServices.add(it)
                                },
                                isInEstimate = cartServices.contains(service),
                                isFavorite = favoriteSkus.contains(service.sku),
                                onToggleFavorite = { svc ->
                                    favoriteSkus = ClientQolManager.toggleFavoriteSku(context, svc.sku)
                                },
                                onBuyWithStripe = { svc ->
                                    stripeCheckoutItems = listOf(svc)
                                },
                                isCompactMode = compactCardMode,
                                storefrontMode = storefrontMode
                            )
                        }
                    }
                    // Tab 10: Estimate Calculator
                    10 -> {
                        item {
                            EstimateCalculator(
                                selectedServices = cartServices,
                                onRemoveService = { cartServices.remove(it) },
                                onClearAll = { cartServices.clear() },
                                onDraftOrder = {
                                    selectedTab = 11
                                },
                                onApplyPreset = { pkg ->
                                    pkg.serviceSkus.forEach { sku ->
                                        TyrCatalog.services.find { it.sku == sku }?.let { s ->
                                            if (!cartServices.contains(s)) cartServices.add(s)
                                        }
                                    }
                                },
                                onOpenCustomQuoteBuilder = {
                                    selectedTab = 20
                                },
                                onOpenPaymentClarity = {
                                    selectedTab = 21
                                },
                                onCheckoutWithStripe = { itemsToPay ->
                                    stripeCheckoutItems = itemsToPay.toList()
                                }
                            )
                        }
                    }
                }

                // Pager Controls for Pages 1–8
                if (selectedTab in 1..8) {
                    val pageIndex = selectedTab - 1
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { if (selectedTab > 1) selectedTab-- else selectedTab = 8 },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prev", fontSize = 11.sp)
                            }

                            Text(
                                text = "Page ${pageIndex + 1} of 8",
                                color = AsgardLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            OutlinedButton(
                                onClick = { if (selectedTab < 8) selectedTab++ else selectedTab = 1 },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Next", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { selectedTab = 22 },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("🏛️ About TYR", color = AsgardLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("•", color = TextMuted, fontSize = 10.sp)
                            TextButton(
                                onClick = { selectedTab = 23 },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("🛟 Support & SLA", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("•", color = TextMuted, fontSize = 10.sp)
                            TextButton(
                                onClick = { selectedTab = 24 },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("📜 Privacy Policy", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            "© ${TyrCatalog.organizationName} • ${TyrCatalog.legalName}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            "NAICS 541512 • 541611 • Zero-Custody Guarantee",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        } else {
            // DEDICATED FULL-SCREEN WORKFLOW PAGES (Tabs 11..24)
            // Rendered in a bounded Box to eliminate nested scrolling infinity-height measurement crashes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp)
            ) {
                when (selectedTab) {
                    11 -> ClientIntakeView(
                        preSelectedServices = cartServices,
                        onOrderSubmitted = { order ->
                            activeOrders.add(0, order)
                            selectedTab = 12
                        },
                        onNavigateBack = { selectedTab = 1 },
                        onChainToSowAndStripe = { order ->
                            activeOrders.add(0, order)
                            chainedWorkOrderForSow = order
                            selectedTab = 16
                        }
                    )
                    12 -> WorkOrdersTrackerView(
                        orders = activeOrders,
                        onNavigateToIntake = { selectedTab = 11 }
                    )
                    13 -> GeminiChatView(
                        onSelectSku = { sku ->
                            val s = TyrCatalog.services.find { it.sku == sku }
                            if (s != null && !cartServices.contains(s)) {
                                cartServices.add(s)
                            }
                        },
                        onLaunchIntake = { skus ->
                            cartServices.clear()
                            skus.forEach { sku ->
                                TyrCatalog.services.find { it.sku == sku }?.let { cartServices.add(it) }
                            }
                            selectedTab = 11
                        },
                        onNavigateToRelay = { selectedTab = 15 }
                    )
                    14 -> Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        NorseImageForgeView(
                            onExploreServices = { selectedTab = 1 }
                        )
                    }
                    15 -> GoogleChatRelay(
                        onNavigateToIntake = { selectedTab = 11 }
                    )
                    16 -> Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        DigitalContractSignView(
                            onNavigateToIntake = { selectedTab = 11 },
                            prefilledWorkOrder = chainedWorkOrderForSow,
                            onProceedToStripeCheckout = { services ->
                                stripeCheckoutItems = services
                            }
                        )
                    }
                    17 -> ZeroCustodyVaultView(
                        onNavigateToIntake = { selectedTab = 11 }
                    )
                    18 -> Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        AppointmentSchedulerView(
                            onNavigateToIntake = { selectedTab = 11 }
                        )
                    }
                    19 -> ReferralHubView(
                        onNavigateToIntake = { selectedTab = 11 },
                        onExploreServices = { selectedTab = 1 }
                    )
                    20 -> CustomQuoteBuilderView(
                        catalogServicesInCart = cartServices,
                        onNavigateToIntake = { selectedTab = 11 },
                        onNavigateToServices = { selectedTab = 1 },
                        onNavigateToPaymentClarity = { selectedTab = 21 }
                    )
                    21 -> PaymentModelsClarityView(
                        onNavigateToIntake = { selectedTab = 11 },
                        onNavigateToServices = { selectedTab = 1 },
                        onAddToCart = { service ->
                            if (!cartServices.contains(service)) {
                                cartServices.add(service)
                            }
                        },
                        cartServices = cartServices
                    )
                    22 -> AboutTyrView(
                        onNavigateToCatalog = { selectedTab = 1 },
                        onNavigateToSupport = { selectedTab = 23 },
                        onNavigateToPrivacy = { selectedTab = 24 },
                        onNavigateToCard = { selectedTab = 0 }
                    )
                    23 -> SupportContactView(
                        onNavigateToChatRelay = { selectedTab = 15 },
                        onNavigateToSchedule = { selectedTab = 18 },
                        onNavigateToPrivacy = { selectedTab = 24 }
                    )
                    24 -> UniversalPrivacyPolicyView(
                        onNavigateToSupport = { selectedTab = 23 },
                        onNavigateToVault = { selectedTab = 17 }
                    )
                    25 -> StripeCheckoutAndReceiptsView(
                        cartServices = cartServices,
                        onAddToCart = { service ->
                            if (!cartServices.contains(service)) {
                                cartServices.add(service)
                            }
                        }
                    )
                    26 -> FieldForensicsAndTelemetryView(
                        onLaunchChainedPipeline = {
                            selectedTab = 11
                        },
                        onNavigateToIntake = { selectedTab = 11 },
                        onNavigateToStripe = { selectedTab = 25 }
                    )
                    27 -> GrowthMarketingView(
                        onAddMatchedSkusToCart = { matched ->
                            matched.forEach { svc ->
                                if (!cartServices.contains(svc)) cartServices.add(svc)
                            }
                        },
                        onLaunchIntakeForSkus = { matched ->
                            matched.forEach { svc ->
                                if (!cartServices.contains(svc)) cartServices.add(svc)
                            }
                            selectedTab = 11
                        },
                        onLaunchStripeForSkus = { matched ->
                            if (matched.isNotEmpty()) {
                                stripeCheckoutItems = matched
                            }
                        },
                        onOpen3dCard = { selectedTab = 0 }
                    )
                }
            }
        }
    }
    }

    if (stripeCheckoutItems != null) {
        StripeCheckoutSheetDialog(
            servicesToCheckout = stripeCheckoutItems!!,
            onDismiss = { stripeCheckoutItems = null },
            onPaymentCompleted = { record ->
                Toast.makeText(
                    context,
                    "Digital Receipt ${record.receiptNumber} saved to vault!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onOpenReceiptsVault = {
                stripeCheckoutItems = null
                selectedTab = 25
            }
        )
    }

    if (showInquiryDialog) {
        InquiryDialog(
            item = activeInquiryItem,
            itemsList = cartServices,
            onDismiss = { showInquiryDialog = false },
            onCallNow = {
                showInquiryDialog = false
                onDialPhone()
            },
            onSendEmail = { subject, body ->
                showInquiryDialog = false
                onSendEmail(subject, body)
            }
        )
    }

    if (showPrivacyDialog) {
        ZeroCustodyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showGeminiOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showGeminiOptionsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✨", fontSize = 18.sp)
                    Text("Gemini AI Option Toggles", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Configure live Gemini AI options across the Norse Image Forge and Gemini Advisor.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Supported Models & Options:", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("• 3.8 Flash (gemini-3.8-flash) — Balanced skaldic generation", color = TextMain, fontSize = 11.sp)
                            Text("• 3.5 Flash-Lite (gemini-3.5-flash-lite) — Rapid low-latency", color = TextMain, fontSize = 11.sp)
                            Text("• 4 Argon (gemini-4-argon) — Deep technical & contract reasoning", color = TextMain, fontSize = 11.sp)
                            Text("• Sovereign Offline Vault — 0 Network / 0 API instant fallback", color = VanaheimLight, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showGeminiOptionsDialog = false
                                selectedTab = 14 // Norse Image Forge
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🎨 Image Forge", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showGeminiOptionsDialog = false
                                selectedTab = 13 // Gemini Chat
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain)
                        ) {
                            Text("💬 AI Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGeminiOptionsDialog = false }) {
                    Text("Close", color = AsgardLight)
                }
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showCommandCenterModal) {
        CommandCenterModal(
            onDismiss = { showCommandCenterModal = false },
            onSelectDestination = { index ->
                selectedTab = index
                showCommandCenterModal = false
            },
            cartItemCount = cartServices.size,
            onCallConsult = {
                showCommandCenterModal = false
                onDialPhone()
            },
            recentTabs = recentTabs
        )
    }

    if (showAccessibilityProfileModal) {
        val currentProfile = remember { ClientQolManager.getSavedProfile(context) }
        var pName by remember { mutableStateOf(currentProfile.fullName) }
        var pOrg by remember { mutableStateOf(currentProfile.organization) }
        var pEmail by remember { mutableStateOf(currentProfile.email) }
        var pPhone by remember { mutableStateOf(currentProfile.phone) }
        var pDomain by remember { mutableStateOf(currentProfile.domain) }

        AlertDialog(
            onDismissRequest = { showAccessibilityProfileModal = false },
            containerColor = BgSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("♿", fontSize = 18.sp)
                    Text(
                        "Accessibility & Client Profile",
                        color = AsgardLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Dynamic Text Scaling
                    Text(
                        "1. READABILITY & TEXT SCALING",
                        color = AsgardLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1.0f to "100% Standard",
                            1.15f to "115% Large",
                            1.30f to "130% Extra"
                        ).forEach { (scaleVal, label) ->
                            val isSel = kotlin.math.abs(fontScaleMultiplier - scaleVal) < 0.02f
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    fontScaleMultiplier = scaleVal
                                    ClientQolManager.setFontScale(context, scaleVal)
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MidgardMain,
                                    selectedLabelColor = Color.White,
                                    containerColor = BgAttr,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // 2. High-Contrast Toggle
                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("High-Contrast OLED Mode", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("True-black background for maximum readability", color = TextMuted, fontSize = 9.sp)
                            }
                            Switch(
                                checked = highContrastMode,
                                onCheckedChange = {
                                    highContrastMode = it
                                    ClientQolManager.setHighContrast(context, it)
                                }
                            )
                        }
                    }

                    // 2b. Compact Service Cards Toggle
                    Surface(
                        color = BgAttr,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Compact Service Cards Mode", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Dense 1-row SKU cards across catalog realms", color = TextMuted, fontSize = 9.sp)
                            }
                            Switch(
                                checked = compactCardMode,
                                onCheckedChange = {
                                    compactCardMode = it
                                    ClientQolManager.setCompactCardMode(context, it)
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSubtle)

                    // 3. Saved Client Profile for 1-Tap Autofill
                    Text(
                        "2. SAVED CLIENT PROFILE (1-TAP AUTOFILL)",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        "Stored strictly on your device to autofill Intake, Custom Quotes, and Support SLA tickets in 1 tap.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Full Name", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pOrg,
                        onValueChange = { pOrg = it },
                        label = { Text("Business / Organization", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pEmail,
                        onValueChange = { pEmail = it },
                        label = { Text("Email Address", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pPhone,
                        onValueChange = { pPhone = it },
                        label = { Text("Phone Number", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pDomain,
                        onValueChange = { pDomain = it },
                        label = { Text("Primary Domain / Website", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ClientQolManager.saveProfile(
                            context,
                            SavedClientProfile(
                                clientName = pName.trim(),
                                businessName = pOrg.trim(),
                                email = pEmail.trim(),
                                phone = pPhone.trim(),
                                domain = pDomain.trim()
                            )
                        )
                        showAccessibilityProfileModal = false
                        Toast.makeText(context, "Accessibility & Client Profile saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Save Preferences", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityProfileModal = false }) {
                    Text("Close", color = TextMuted, fontSize = 11.sp)
                }
            }
        )
    }
}
