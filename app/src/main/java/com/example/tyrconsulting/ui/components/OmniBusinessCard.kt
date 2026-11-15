package com.example.tyrconsulting.ui.components

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.GrowthMarketingManager
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*

/**
 * Unified Nine Realms Obsidian 3D Personal & Business Card.
 * Synchronizes Personal (Front) <-> Business (Back) state with the storefront mode,
 * features Patron Triad attribute strips on both faces, and renders scannable offline ISO QR codes.
 */
@Composable
fun OmniBusinessCard(
    onExploreServices: () -> Unit = {},
    onOpenIntake: () -> Unit = {},
    onOpenQuoteBuilder: () -> Unit = {},
    onOpenPaymentClarity: () -> Unit = {},
    storefrontMode: String = "PERSONAL",
    onStorefrontModeChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    var isFlippedToBack by remember { mutableStateOf(storefrontMode.equals("BUSINESS", ignoreCase = true)) }
    var dragYaw by remember { mutableFloatStateOf(0f) }
    var dragPitch by remember { mutableFloatStateOf(0f) }
    var selectedDeityIndex by remember { mutableIntStateOf(0) }
    var qrVcardMode by remember { mutableStateOf(false) }

    LaunchedEffect(storefrontMode) {
        val targetBack = storefrontMode.equals("BUSINESS", ignoreCase = true)
        if (isFlippedToBack != targetBack) {
            dragYaw = 0f
            dragPitch = 0f
            isFlippedToBack = targetBack
        }
    }

    val switchCardMode: (Boolean) -> Unit = { toBusiness ->
        dragYaw = 0f
        dragPitch = 0f
        isFlippedToBack = toBusiness
        onStorefrontModeChanged(if (toBusiness) "BUSINESS" else "PERSONAL")
    }

    val baseFlipAngle by animateFloatAsState(
        targetValue = if (isFlippedToBack) 180f else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "cardFlipAngle"
    )

    val effectiveRotationY = baseFlipAngle + dragYaw
    val normalizedAngle = ((effectiveRotationY % 360f) + 360f) % 360f
    val showingBackFace = normalizedAngle in 90f..270f

    val saveNativeContact = {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, "Caleb TR Holcomb")
            putExtra(ContactsContract.Intents.Insert.COMPANY, "TYR Technical Consulting")
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, "Principal Systems Engineer & Expense Auditor")
            putExtra(ContactsContract.Intents.Insert.PHONE, TyrCatalog.phone)
            putExtra(
                ContactsContract.Intents.Insert.PHONE_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK
            )
            putExtra(ContactsContract.Intents.Insert.EMAIL, TyrCatalog.email)
            putExtra(
                ContactsContract.Intents.Insert.EMAIL_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE_WORK
            )
            putExtra(
                ContactsContract.Intents.Insert.SECONDARY_EMAIL,
                "colacal43@gmail.com"
            )
            putExtra(
                ContactsContract.Intents.Insert.NOTES,
                "TYR Protocol (NAICS 541512 & 541611) • Northwest Alabama & Nationwide Remote • Zero-Custody 0-Day Data Purge"
            )
        }
        safeStartActivity(context, contactIntent, fallbackCopyText = GrowthMarketingManager.vCardPayload)
    }

    val shareCardIdentity = {
        val modeQuery = if (showingBackFace) "business" else "personal"
        val shareText = buildString {
            appendLine("TYR Technical Consulting — Caleb TR Holcomb")
            appendLine("NAICS 541512 & 541611 • Northwest Alabama & US Remote")
            appendLine("Direct Line: ${TyrCatalog.phone}")
            appendLine("Work: ${TyrCatalog.email} | Personal: colacal43@gmail.com")
            appendLine("Storefront & 3D Card: https://tyrprotocol.pro/?mode=$modeQuery")
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "TYR Technical Consulting — Caleb Holcomb")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        safeStartActivity(
            context,
            Intent.createChooser(shareIntent, "Share TYR Business Card"),
            fallbackCopyText = shareText
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("omni_business_card_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Unified Personal/Business Mode & 3D Controls Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    Brush.linearGradient(listOf(MidgardMain, AsgardMain)),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ᛏ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        Column {
                            Text(
                                text = if (showingBackFace) "BUSINESS FACE • TYR PROTOCOL" else "PERSONAL FACE • CALEB HOLCOMB",
                                color = if (showingBackFace) VanaheimLight else AsgardLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Drag to tilt 3D • Double-tap or use toggle to flip",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { switchCardMode(!isFlippedToBack) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AsgardLight),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("btn_flip_3d_card")
                        ) {
                            Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip 3D Card",
                                tint = AsgardLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isFlippedToBack) "Personal" else "Flip 180°",
                                fontSize = 10.sp,
                                color = AsgardLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (dragYaw != 0f || dragPitch != 0f) {
                            IconButton(
                                onClick = {
                                    dragYaw = 0f
                                    dragPitch = 0f
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.Default.CenterFocusStrong,
                                    contentDescription = "Reset 3D Tilt",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Segmented Personal / Business Side Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgAttr, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { switchCardMode(false) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .testTag("card_mode_personal_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showingBackFace) MidgardMain else Color.Transparent,
                            contentColor = if (!showingBackFace) Color.White else TextSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            "👤 Personal Front",
                            fontSize = 11.sp,
                            fontWeight = if (!showingBackFace) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { switchCardMode(true) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .testTag("card_mode_business_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showingBackFace) Color(0xFF059669) else Color.Transparent,
                            contentColor = if (showingBackFace) Color.White else TextSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            "🏢 Business Back",
                            fontSize = 11.sp,
                            fontWeight = if (showingBackFace) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. 3D Interactive Stage Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { switchCardMode(!isFlippedToBack) }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragYaw = (dragYaw + dragAmount.x * 0.35f).coerceIn(-65f, 65f)
                        dragPitch = (dragPitch - dragAmount.y * 0.25f).coerceIn(-25f, 25f)
                    }
                }
                .graphicsLayer {
                    rotationY = effectiveRotationY
                    rotationX = dragPitch
                    cameraDistance = 16f * density
                }
                .testTag("interactive_3d_card_stage")
        ) {
            if (!showingBackFace) {
                FrontIdentityCardFace(
                    context = context,
                    selectedDeityIndex = selectedDeityIndex,
                    onSelectDeity = { selectedDeityIndex = it },
                    qrVcardMode = qrVcardMode,
                    onToggleQrMode = { qrVcardMode = !qrVcardMode },
                    onSaveContact = saveNativeContact,
                    onShareCard = shareCardIdentity,
                    onFlipToBack = { switchCardMode(true) }
                )
            } else {
                Box(
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f
                    }
                ) {
                    BackBusinessCardFace(
                        context = context,
                        selectedDeityIndex = selectedDeityIndex,
                        onSelectDeity = { selectedDeityIndex = it },
                        qrVcardMode = qrVcardMode,
                        onToggleQrMode = { qrVcardMode = !qrVcardMode },
                        onSaveContact = saveNativeContact,
                        onShareCard = shareCardIdentity,
                        onExploreServices = onExploreServices,
                        onOpenQuoteBuilder = onOpenQuoteBuilder,
                        onFlipToFront = { switchCardMode(false) }
                    )
                }
            }
        }

        // 3. Fast Navigation Launchpad Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "QUICK LAUNCHPAD • STOREFRONT & OPERATIONS",
                    color = AsgardLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExploreServices,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("card_btn_explore_catalog"),
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesomeMosaic, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Service Catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenQuoteBuilder,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .testTag("card_btn_quote_builder"),
                        colors = ButtonDefaults.buttonColors(containerColor = AsgardMain),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Custom Quote", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenPaymentClarity,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .testTag("card_btn_payment_clarity"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, VanaheimLight),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("💳 1-Time vs Monthly", fontSize = 10.sp, color = VanaheimLight, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenIntake,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .testTag("card_btn_direct_intake"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AsgardLight),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("📝 Client Intake Form", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OmniBusinessCardStandalone(
    onNavigateToCatalog: () -> Unit = {},
    onOpenIntake: () -> Unit = {},
    onOpenQuoteBuilder: () -> Unit = {},
    onOpenPaymentClarity: () -> Unit = {},
    storefrontMode: String = "PERSONAL",
    onStorefrontModeChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    OmniBusinessCard(
        onExploreServices = onNavigateToCatalog,
        onOpenIntake = onOpenIntake,
        onOpenQuoteBuilder = onOpenQuoteBuilder,
        onOpenPaymentClarity = onOpenPaymentClarity,
        storefrontMode = storefrontMode,
        onStorefrontModeChanged = onStorefrontModeChanged,
        modifier = modifier
    )
}
