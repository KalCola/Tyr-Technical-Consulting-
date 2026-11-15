package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.R
import com.example.tyrconsulting.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.math.*

enum class NorseDeity(
    val title: String,
    val epithet: String,
    val primaryRune: String,
    val primaryRuneName: String,
    val accentColor: Color,
    val secondaryColor: Color,
    val defaultAttributes: List<String>,
    val drawableRes: Int,
    val sigilRes: Int
) {
    TYR(
        title = "Týr (ᛏ Tiwaz)",
        epithet = "God of Law, Cosmic Order & Honorable Sacrifice",
        primaryRune = "ᛏ",
        primaryRuneName = "Tiwaz (Justice / Polaris / Truth)",
        accentColor = AsgardLight,
        secondaryColor = MidgardMain,
        defaultAttributes = listOf(
            "Severed Right Hand & Gleipnir Chain",
            "Runed Greatsword of Law",
            "Ancient Thing-Stead Parliament Stone",
            "Polaris North Star Apex",
            "Obsidian & Weathered Iron Plate Armor",
            "Stormy Thundercloud Nordic Skies"
        ),
        drawableRes = R.drawable.img_tyr_tiwaz,
        sigilRes = R.drawable.ic_tyr_sigil
    ),
    FREYA(
        title = "Freyja (ᚠ Fehu)",
        epithet = "Queen of Vanir, Wealth, Seidr & Falcon Flight",
        primaryRune = "ᚠ",
        primaryRuneName = "Fehu (Wealth / Golden Abundance)",
        accentColor = VanaheimLight,
        secondaryColor = Color(0xFFF59E0B),
        defaultAttributes = listOf(
            "Brísingamen Flaming Golden Torc",
            "Valshamr Falcon Feathered Cloak",
            "Blue-Eyed Sacred Forest Cats Chariot",
            "Tears of Red-Gold and Sea Amber",
            "Fólkvangr Meadow & Sessrúmnir Hall",
            "Seidr Shamanic Weave of Fate"
        ),
        drawableRes = R.drawable.img_freya_fehu,
        sigilRes = R.drawable.ic_freya_sigil
    ),
    SYNCRETIC(
        title = "Týr & Freyja Alliance (ᛏ ᚠ)",
        epithet = "Sovereign Union of Divine Justice & Generative Wealth",
        primaryRune = "ᛏᚠ",
        primaryRuneName = "Tiwaz & Fehu (Law Safeguarding Prosperity)",
        accentColor = Color(0xFFA855F7),
        secondaryColor = AsgardLight,
        defaultAttributes = listOf(
            "Scales of Law Balanced Over Golden Wheat",
            "Gleipnir Entwined with Brísingamen",
            "Falcon Perched on Sword of Justice",
            "Yggdrasil Aesir & Vanir Roots",
            "Twin Auroral Shields (Iron & Amber)"
        ),
        drawableRes = R.drawable.img_syncretic_realm,
        sigilRes = R.drawable.ic_tyr_sigil
    )
}

data class ArtStyle(
    val name: String,
    val description: String,
    val promptModifier: String
)

val ART_STYLES = listOf(
    ArtStyle(
        name = "Elder Edda Mythic Oil",
        description = "Dramatic chiaroscuro, heavy textured brushwork, Frazetta & Rembrandt lighting",
        promptModifier = "mythological masterwork oil painting, dramatic chiaroscuro, rich thick impasto texture, atmospheric tenebrism, Rembrandt and Frank Frazetta lighting, epic scale, deep umber and glowing amber tones"
    ),
    ArtStyle(
        name = "Norse Woodcut & Engraving",
        description = "16th-century Albrecht Dürer linocut, intricate knotwork, aged vellum parchment",
        promptModifier = "authentic 16th-century Albrecht Dürer woodcut engraving, intricate Nordic knotwork borders, crosshatched ink lines on aged vellum parchment, stark contrast, sacred talismanic style"
    ),
    ArtStyle(
        name = "Asgardian Cyber-Viking",
        description = "High-tech neo-Nordic sci-fi, obsidian armor, neon cyan & gold runic circuitry",
        promptModifier = "futuristic cyber-viking aesthetic, matte black obsidian composite armor, glowing neon runic traces in cyan and burning gold, holographic Gleipnir energy tether, cinematic raytracing"
    ),
    ArtStyle(
        name = "Basalt Stone & 24K Gold Leaf",
        description = "Deep Scandinavian rune stone carving with hammered gold leaf inlay and moss",
        promptModifier = "ancient Scandinavian rune stone carved from deep dark basalt granite, runic channels filled with hammered 24k gold leaf, weathered stone relief, natural mountain moss and frost"
    ),
    ArtStyle(
        name = "Aurora Borealis Ethereal",
        description = "Luminous northern lights watercolor, translucent emerald and violet celestial skies",
        promptModifier = "ethereal northern lights aurora borealis watercolor, luminous curtains of emerald, turquoise and violet starlight, flowing translucent drapery, crystalline frost particles"
    ),
    ArtStyle(
        name = "Cinematic 8K Unreal Dark Fantasy",
        description = "Ultra-photorealistic dark fantasy, volumetric fog, ray-traced god rays, weathered steel",
        promptModifier = "cinematic 8k dark fantasy photograph, volumetric fog, god rays through thunderheads, photorealistic damascus steel texture, authentic historical Norse garments, Unreal Engine 5 render"
    )
)

enum class GeminiForgeModel(val modelId: String, val label: String, val subtitle: String) {
    FLASH_3_8("gemini-3.8-flash", "3.8 Flash (Balanced Skald)", "Recommended: Poetic Galdr & Master Prompt"),
    FLASH_LITE_3_5("gemini-3.5-flash-lite", "3.5 Flash-Lite (Rapid)", "Sub-second low-latency synthesis"),
    ARGON_4("gemini-4-argon", "4 Argon (Deep Reasoning)", "Advanced mythic lore & alliterative meter")
}

fun generateLocalElderEddaVision(
    deity: NorseDeity,
    style: ArtStyle,
    aspectRatio: String,
    attrs: List<String>,
    includeGaldr: Boolean,
    highDetails: Boolean
): String {
    val galdrVerse = when (deity) {
        NorseDeity.TYR -> """[GALDR INCANTATION — OLD NORSE FORNYRÐISLAG METER]
ᛏ Tiwaz ríkr, rún at dómum,
Stendr Fenrisúlfr á þingsteini;
Hönd gaf Týr fyr frið ok heit,
Skínn Leiðarstjarna skírum boga."""
        NorseDeity.FREYA -> """[GALDR INCANTATION — OLD NORSE LJÓÐAHÁTTR METER]
ᚠ Fehu glóir á Fólkvangi,
Gullit tár um vanga fellr;
Valshamr fleygir fagrri dís,
Ekr köttum tveim til Sessrúmnis."""
        NorseDeity.SYNCRETIC -> """[GALDR INCANTATION — ALLIANCE GALDR OF LAW & WEALTH]
ᛏ ᚠ Tvær rúnir saman ristnar,
Lög ok auðr í einum sal;
Sverðit vǫrðr ok gullit grœr,
Heill sé sá er heitir trútt."""
    }

    val visionDesc = when (deity) {
        NorseDeity.TYR -> """[MYTHOLOGICAL VISION]
The one-handed sovereign Týr stands resolute atop the ancient Thing-Stead assembly stone in Asgard. His left hand grips his runed broadsword of immutable law, its steel reflecting the auroral fires of the northern sky. His right forearm bears the sacred scar of the oath pledged to bind the apocalyptic wolf Fenrir with the gossamer-thin, unbreakable Gleipnir chain forged of mountain breath and cat's footfalls.

Above his helm, the North Star Polaris (Leiðarstjarna) shines with piercing intensity through stormy clouds of charcoal iron and slate. He embodies uncompromising truth, sovereign covenants, and the solemn duty to safeguard the defenseless with zero fear of personal sacrifice."""
        NorseDeity.FREYA -> """[MYTHOLOGICAL VISION]
Freyja, sovereign queen of the Vanir and mistress of Seidr magic, glides across the blossoming golden meadows of Fólkvangr before the high gates of Sessrúmnir Hall. At her throat blazes the sacred Brísingamen torc, glowing with the eternal fire of solar alchemy. Her Valshamr falcon-feathered cloak catches the celestial wind, granting her effortless flight across the Nine Realms.

Beside her stands her majestic chariot, drawn by the two sacred blue-eyed forest cats Bygul and Trjegul. Golden tears of red amber trace her cheeks, falling to crystallize into precious gemstones upon the earth. She weaves the golden threads of prosperity, sovereignty, and impenetrable clan protection."""
        NorseDeity.SYNCRETIC -> """[MYTHOLOGICAL VISION]
In the sacred chamber between Asgard and Vanaheim, Týr and Freyja unite their divine realms. The balance scales of cosmic justice (ᛏ Tiwaz) rest upon a pedestal of golden autumn wheat (ᚠ Fehu). The unbreakable Gleipnir cord is entwined with the golden links of Brísingamen, while a white-tailed arctic falcon perches calmly upon the hilt of Týr's greatsword of law.

Iron armor and flaming golden robes form a singular harmonious bulwark. Immutable law shields generative enterprise, establishing that true sovereign prosperity can only thrive when safeguarded by unbending integrity and defensive perimeter security."""
    }

    val cameraPrompt = """[SYNTHESIZED MASTER PROMPT]
Cinematic masterwork, ${if (highDetails) "8k resolution, volumetric atmospheric fog, photorealistic octane render, Hasselblad medium format camera, rim lighting, dramatic tenebrism" else "clean focused composition, distinct silhouette, rich palette"}, ${deity.title}, Elder Futhark rune ${deity.primaryRune} engraved in radiant runic luminescence, canon attributes: ${attrs.joinToString(", ")}, rendered in ${style.name} style (${style.promptModifier}), aspect ratio $aspectRatio."""

    val consecration = """[RUNIC CONSECRATION]
The sacred rune ${deity.primaryRune} (${deity.primaryRuneName}) seals this visual synthesis with zero-compromise sovereign protection, aligning divine archetypal power with sovereign data ownership and unyielding digital armor."""

    return if (includeGaldr) {
        "$galdrVerse\n\n$visionDesc\n\n$cameraPrompt\n\n$consecration"
    } else {
        "$visionDesc\n\n$cameraPrompt\n\n$consecration"
    }
}

@Composable
fun NorseImageForgeView(
    onExploreServices: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDeity by remember { mutableStateOf(NorseDeity.TYR) }
    var selectedStyle by remember { mutableStateOf(ART_STYLES[0]) }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }

    // Gemini Options Toggles
    var useGeminiCloudAi by remember { mutableStateOf(true) }
    var selectedGeminiModel by remember { mutableStateOf(GeminiForgeModel.FLASH_3_8) }
    var includeGaldrPoetry by remember { mutableStateOf(true) }
    var highCinematicDetails by remember { mutableStateOf(true) }

    // Active attributes selection
    val activeAttributes = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize attributes when deity changes
    LaunchedEffect(selectedDeity) {
        activeAttributes.clear()
        selectedDeity.defaultAttributes.forEach { attr ->
            activeAttributes[attr] = true
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var isForging by remember { mutableStateOf(false) }
    var showPromptCopiedDialog by remember { mutableStateOf(false) }
    var generatedVisionTitle by remember { mutableStateOf<String?>(null) }
    var forgedMythologyLore by remember { mutableStateOf<String?>(null) }
    var forgeErrorMessage by remember { mutableStateOf<String?>(null) }
    var expandedLore by remember { mutableStateOf(false) }

    // Synthesized Prompt
    val synthesizedPrompt = remember(selectedDeity, selectedStyle, selectedAspectRatio, activeAttributes.toMap()) {
        val includedAttrs = activeAttributes.filter { it.value }.keys.joinToString(", ")
        val deityDesc = when (selectedDeity) {
            NorseDeity.TYR -> "The Norse god Týr (Tiwaz), the one-handed sovereign of divine law and honorable martial sacrifice"
            NorseDeity.FREYA -> "The Norse goddess Freyja (Fehu), queen of the Vanir, sovereign of love, war, seidr magic, and golden harvest"
            NorseDeity.SYNCRETIC -> "The grand syncretic alliance of Týr and Freyja, embodying the sacred balance of divine law protecting flourishing abundance"
        }

        "$deityDesc. Centered prominently with the sacred Elder Futhark rune ${selectedDeity.primaryRune} (${selectedDeity.primaryRuneName}). Incorporating the canonical mythic attributes: $includedAttrs. Art style: ${selectedStyle.promptModifier}. Aspect ratio: $selectedAspectRatio. Composition: Majestic, balanced, heroic, solemn dignity, highly detailed, masterwork quality, cinematic lighting."
    }

    val negativePrompt = "cartoon, anime, plastic, lowres, distorted anatomy, extra limbs, modern firearms, nylon clothing, blurry, watermark, signature, oversaturated, amateur render"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgObsidian),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MidgardMain, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ᛏ ᚠ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            "Runic Image Forge",
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Norse Mythic Visual Synthesis • Týr & Freyja Studio",
                            color = selectedDeity.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    "Craft canonical Norse mythological imagery incorporating Týr (ᛏ Tiwaz), Freyja (ᚠ Fehu), their sacred runes, divine attributes, and historic artistic styles.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Divine Quick-Pick Presets
        Text(
            "⚡ DIVINE QUICK-PICK PRESETS",
            color = VanaheimLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickPresets: List<Triple<String, NorseDeity, ArtStyle>> = listOf(
                Triple("ᛏ Týr: Fenrir Oath", NorseDeity.TYR, ART_STYLES[1]),
                Triple("ᚠ Freyja: Falcon Flight", NorseDeity.FREYA, ART_STYLES[0]),
                Triple("ᛏ ᚠ Syncretic Pact", NorseDeity.SYNCRETIC, ART_STYLES[3])
            )

            quickPresets.forEach { preset ->
                val label = preset.first
                val deity = preset.second
                val style = preset.third
                val isCurrent = selectedDeity == deity && selectedStyle == style
                OutlinedButton(
                    onClick = {
                        selectedDeity = deity
                        selectedStyle = style
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isCurrent) deity.accentColor else BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isCurrent) deity.accentColor.copy(alpha = 0.2f) else BgAttr
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        color = if (isCurrent) deity.accentColor else TextMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Deity Selection Selector
        Text(
            "1. SELECT DEITY & RUNIC FOCUS",
            color = AsgardLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NorseDeity.values().forEach { deity ->
                val isSelected = selectedDeity == deity
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDeity = deity }
                        .testTag("deity_card_${deity.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BgAttr else BgCard
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) deity.accentColor else BorderSubtle
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            deity.primaryRune,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) deity.accentColor else TextMuted
                        )
                        Text(
                            deity.title.substringBefore(" ("),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) TextMain else TextSecondary
                        )
                        Text(
                            deity.primaryRuneName.substringBefore(" ("),
                            fontSize = 9.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Live Visual Showcase of Selected Deity
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, selectedDeity.accentColor.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image Banner Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07070B))
                ) {
                    Image(
                        painter = painterResource(id = selectedDeity.drawableRes),
                        contentDescription = selectedDeity.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay Vignette and Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(selectedDeity.primaryRune, color = selectedDeity.accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(selectedDeity.epithet, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Dynamic Canvas Sigil & Interactive Animation
                RunicVisualCanvas(
                    deity = selectedDeity,
                    style = selectedStyle,
                    isForging = isForging,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
        }

        // 2. Canon Attributes Customizer
        Text(
            "2. MYTHIC ATTRIBUTES & EMBLEMS",
            color = AsgardLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        SimpleFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 6.dp,
            verticalSpacing = 6.dp
        ) {
            selectedDeity.defaultAttributes.forEach { attr ->
                val checked = activeAttributes[attr] ?: true
                FilterChip(
                    selected = checked,
                    onClick = { activeAttributes[attr] = !checked },
                    label = { Text(attr, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        if (checked) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = selectedDeity.accentColor)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BgAttr,
                        selectedLabelColor = selectedDeity.accentColor,
                        containerColor = BgCard,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = checked,
                        borderColor = if (checked) selectedDeity.accentColor else BorderSubtle,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("attr_chip_${attr.take(8)}")
                )
            }
        }

        // 3. Artistic Style Matrix
        Text(
            "3. HISTORICAL & ARTISTIC STYLE",
            color = AsgardLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ART_STYLES.forEach { style ->
                val isSelected = selectedStyle == style
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStyle = style }
                        .testTag("style_card_${style.name.take(6).lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BgAttr else BgCard
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) AsgardLight else BorderSubtle
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedStyle = style },
                            colors = RadioButtonDefaults.colors(selectedColor = AsgardLight, unselectedColor = TextMuted)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(style.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSelected) TextMain else TextSecondary)
                            Text(style.description, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // 4. Aspect Ratio Selector
        Text(
            "4. ASPECT RATIO",
            color = AsgardLight,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("16:9" to "Landscape", "1:1" to "Square Sigil", "9:16" to "Wallpaper", "4:3" to "Chronicle").forEach { (ratio, label) ->
                val isSelected = selectedAspectRatio == ratio
                OutlinedButton(
                    onClick = { selectedAspectRatio = ratio },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) BgAttr else Color.Transparent,
                        contentColor = if (isSelected) AsgardLight else TextSecondary
                    ),
                    border = BorderStroke(1.dp, if (isSelected) AsgardLight else BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ratio, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(label, fontSize = 9.sp, color = TextMuted)
                    }
                }
            }
        }

        // 4.5. Gemini AI Option Toggles Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gemini_forge_options_card"),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, if (useGeminiCloudAi) AsgardLight.copy(alpha = 0.6f) else BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (useGeminiCloudAi) "✨" else "🏛️",
                            fontSize = 16.sp
                        )
                        Column {
                            Text(
                                "GEMINI AI SYNTHESIS OPTIONS",
                                color = if (useGeminiCloudAi) AsgardLight else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                if (useGeminiCloudAi) "Powered by Firebase AI Logic (${selectedGeminiModel.label})" else "Offline Elder Edda Vault Active (0 Network / 0 API)",
                                color = if (useGeminiCloudAi) VanaheimLight else TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Master Gemini Option Switch
                    Switch(
                        checked = useGeminiCloudAi,
                        onCheckedChange = { useGeminiCloudAi = it },
                        modifier = Modifier.testTag("switch_gemini_cloud_ai"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AsgardMain,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BgAttr
                        )
                    )
                }

                if (useGeminiCloudAi) {
                    Text(
                        "Gemini Model Engine:",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Model Selection Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GeminiForgeModel.values().forEach { modelOpt ->
                            val isSelected = selectedGeminiModel == modelOpt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGeminiModel = modelOpt },
                                label = { Text(modelOpt.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AsgardMain.copy(alpha = 0.4f),
                                    selectedLabelColor = AsgardLight,
                                    containerColor = BgAttr,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) AsgardLight else BorderSubtle
                                ),
                                modifier = Modifier.testTag("chip_model_${modelOpt.name.lowercase()}")
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Sub-feature toggles: Galdr Poetry Verse & 8K Volumetric Cinema Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include Galdr Poetic Verse", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Ancient alliterative meter invoking the sacred rune", color = TextMuted, fontSize = 9.sp)
                    }
                    Switch(
                        checked = includeGaldrPoetry,
                        onCheckedChange = { includeGaldrPoetry = it },
                        modifier = Modifier.testTag("switch_include_galdr"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MidgardMain
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("8K Volumetric Camera Prompting", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Octane/Hasselblad lighting & atmospheric tenebrism tags", color = TextMuted, fontSize = 9.sp)
                    }
                    Switch(
                        checked = highCinematicDetails,
                        onCheckedChange = { highCinematicDetails = it },
                        modifier = Modifier.testTag("switch_cinematic_details"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VanaheimMain
                        )
                    )
                }
            }
        }

        // 5. Forge Action & Live Prompt Synthesizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SYNTHESIZED AI PROMPT",
                        color = AsgardLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Norse Image Prompt", synthesizedPrompt)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showPromptCopiedDialog = true
                        },
                        modifier = Modifier.testTag("btn_copy_prompt")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", tint = AsgardLight)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgAttr, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        synthesizedPrompt,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isForging) {
                                isForging = true
                                forgeErrorMessage = null
                                coroutineScope.launch {
                                    val includedAttrs = activeAttributes.filter { it.value }.keys.toList()
                                    if (!useGeminiCloudAi) {
                                        // Offline Elder Edda Vault instant generation
                                        val localVision = generateLocalElderEddaVision(
                                            deity = selectedDeity,
                                            style = selectedStyle,
                                            aspectRatio = selectedAspectRatio,
                                            attrs = includedAttrs,
                                            includeGaldr = includeGaldrPoetry,
                                            highDetails = highCinematicDetails
                                        )
                                        forgedMythologyLore = localVision
                                        generatedVisionTitle = "${selectedDeity.title} • Elder Edda Vault (Offline Mode)"
                                        isForging = false
                                        return@launch
                                    }

                                    try {
                                        val generativeModel = Firebase.ai.generativeModel(
                                            modelName = selectedGeminiModel.modelId,
                                            systemInstruction = content {
                                                text("You are the Sacred Norse Skald and Visual Vision Crafter for TYR Technical Consulting. You generate authentic, poetic, high-mythology descriptions, alliterative Eddic stanzas (Galdr), and masterwork visual generation prompts embodying Tyr (ᛏ Tiwaz), Freyja (ᚠ Fehu), their runes, sacred relics, beasts, and colors.")
                                            }
                                        )
                                        val prompt = "Synthesize a profound divine vision and cinematic master prompt for the following Norse divine invocation:\n" +
                                            "Deity: ${selectedDeity.title} (${selectedDeity.epithet})\n" +
                                            "Sacred Elder Futhark Rune: ${selectedDeity.primaryRune} (${selectedDeity.primaryRuneName})\n" +
                                            "Chosen Attributes: ${includedAttrs.joinToString(", ")}\n" +
                                            "Art Style: ${selectedStyle.name} (${selectedStyle.promptModifier})\n" +
                                            "Aspect Ratio: $selectedAspectRatio\n" +
                                            "Galdr Poetry Requested: $includeGaldrPoetry\n" +
                                            "Volumetric 8K Camera Detail Requested: $highCinematicDetails\n\n" +
                                            "Please provide a structured response with:\n" +
                                            (if (includeGaldrPoetry) "1. [GALDR INCANTATION] - A sacred 4-line alliterative poetic stanza in Old Norse rhythm invoking the deity and rune.\n" else "") +
                                            "2. [MYTHOLOGICAL VISION] - A 2-paragraph rich cinematic depiction of the deity in their realm, describing their attire, sacred items (Týr's sword of justice, the binding of Fenrir with Gleipnir, Freyja's Brísingamen torc, falcon cloak Valshamr, golden tears, and chariot drawn by blue-grey cats Bygul and Trjegul).\n" +
                                            "3. [SYNTHESIZED MASTER PROMPT] - An optimized, camera-detailed prompt for AI image generation (lighting, atmosphere, volumetric dust, color palette).\n" +
                                            "4. [RUNIC CONSECRATION] - The esoteric meaning of the rune and how it aligns with modern sovereign security and flourishing."

                                        val response = withTimeout(60_000L) {
                                            generativeModel.generateContent(prompt)
                                        }
                                        val text = response.text.orEmpty()
                                        if (text.isNotBlank()) {
                                            forgedMythologyLore = text
                                            generatedVisionTitle = "${selectedDeity.title} • Gemini Vision Synthesis (${selectedGeminiModel.label})"
                                        } else {
                                            // Fallback to local
                                            forgedMythologyLore = generateLocalElderEddaVision(
                                                deity = selectedDeity,
                                                style = selectedStyle,
                                                aspectRatio = selectedAspectRatio,
                                                attrs = includedAttrs,
                                                includeGaldr = includeGaldrPoetry,
                                                highDetails = highCinematicDetails
                                            )
                                            generatedVisionTitle = "${selectedDeity.title} • Elder Edda Vault (Fallback)"
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        Log.d("NorseForge", "Firebase AI inference fallback to local vault: ${e.message}")
                                        // Provide seamless local fallback rather than leaving user with an error
                                        forgedMythologyLore = generateLocalElderEddaVision(
                                            deity = selectedDeity,
                                            style = selectedStyle,
                                            aspectRatio = selectedAspectRatio,
                                            attrs = includedAttrs,
                                            includeGaldr = includeGaldrPoetry,
                                            highDetails = highCinematicDetails
                                        )
                                        generatedVisionTitle = "${selectedDeity.title} • Elder Edda Runic Vault"
                                        forgeErrorMessage = null
                                    } finally {
                                        isForging = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_forge_vision"),
                        enabled = !isForging,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedDeity.accentColor,
                            contentColor = Color.Black,
                            disabledContainerColor = selectedDeity.accentColor.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isForging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (useGeminiCloudAi) "Invoking ${selectedGeminiModel.modelId}..." else "Accessing Vault...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (useGeminiCloudAi) "Forge Divine Vision (${selectedGeminiModel.label.take(9)})" else "Forge Vault Vision (Offline)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Norse Prompt", "$synthesizedPrompt\n\nNegative: $negativePrompt")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Full prompt + negative copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy All", tint = TextMain, modifier = Modifier.size(16.dp))
                    }
                }

                if (forgeErrorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgAttr),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Text(forgeErrorMessage ?: "", color = Color(0xFFFCA5A5), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }

                if (generatedVisionTitle != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgAttr),
                        border = BorderStroke(1.dp, selectedDeity.accentColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = selectedDeity.accentColor, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Vision Synthesized via Gemini 3.8 Flash", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(generatedVisionTitle ?: "", color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            if (!forgedMythologyLore.isNullOrBlank()) {
                                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgCard, RoundedCornerShape(6.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        forgedMythologyLore ?: "",
                                        color = TextMain,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        lineHeight = 16.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Divine Norse Synthesis", forgedMythologyLore ?: "")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Divine synthesis copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        border = BorderStroke(1.dp, BorderSubtle)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Synthesis", tint = AsgardLight, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Synthesis", fontSize = 10.sp, color = TextMain)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Sacred Eddic Lore Codex
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard),
            border = BorderStroke(1.dp, BorderSubtle),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedLore = !expandedLore },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📜", fontSize = 16.sp)
                        Text(
                            "Eddic Lore: Týr & Freyja Attributes",
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        if (expandedLore) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Lore",
                        tint = AsgardLight
                    )
                }

                if (expandedLore) {
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "• Týr (ᛏ Tiwaz): Eldest son of sky father traditions, Týr pledged his right hand into the jaws of the wolf Fenrir on the desolate isle of Lyngvi so the gods could test the silken chain Gleipnir. When Fenrir found he was bound forever, he bit off Týr's hand. Týr represents the supreme law of inviolable oaths: true justice requires personal sacrifice and moral fortitude.\n\n" +
                                "• Freyja (ᚠ Fehu): Chief goddess of the Vanir clan and Lady of Fólkvangr ('Field of the Folk'). She commands the feathered falcon cloak (Valshamr), enabling inter-realm flight, and wears Brísingamen, the blazing solar torc crafted by the four Alfrigg smiths. As recipient of half of all fallen heroes, Freyja harmonizes martial bravery with generative abundance, wealth (Fehu), and love.\n\n" +
                                "• Syncretic Alliance: In the historic peace between the Aesir and Vanir, Týr's lawful integrity and Freyja's boundless prosperity unite. In our consulting practice, this informs our dual mandate: rock-solid defensive governance (NAICS 541512) paired with active administrative and operational growth (NAICS 541611).",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RunicVisualCanvas(
    deity: NorseDeity,
    style: ArtStyle,
    isForging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "runic_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = min(w, h) * 0.42f

        // Outer Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    deity.accentColor.copy(alpha = 0.25f * pulse),
                    deity.secondaryColor.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = radius * 1.4f
            ),
            radius = radius * 1.4f,
            center = Offset(cx, cy)
        )

        // Runic Concentric Circles
        drawCircle(
            color = deity.accentColor.copy(alpha = 0.5f),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = deity.secondaryColor.copy(alpha = 0.35f),
            radius = radius * 0.85f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        )

        // Rotating Runic Nodes (24 Elder Futhark markers)
        val numNodes = 24
        val rotRad = Math.toRadians(rotation.toDouble())
        for (i in 0 until numNodes) {
            val angle = 2 * Math.PI * i / numNodes + rotRad
            val nx = cx + (radius * 0.92f) * cos(angle).toFloat()
            val ny = cy + (radius * 0.92f) * sin(angle).toFloat()
            drawCircle(
                color = if (i % 3 == 0) deity.accentColor else deity.secondaryColor.copy(alpha = 0.6f),
                radius = if (i % 3 == 0) 3.5.dp.toPx() else 2.dp.toPx(),
                center = Offset(nx, ny)
            )
        }

        // Central Deity Sigil geometry
        when (deity) {
            NorseDeity.TYR -> {
                // Tiwaz Arrow ᛏ
                val stemPath = Path().apply {
                    moveTo(cx, cy - radius * 0.65f)
                    lineTo(cx, cy + radius * 0.65f)
                }
                drawPath(stemPath, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Left Chevron
                val leftChevron = Path().apply {
                    moveTo(cx, cy - radius * 0.65f)
                    lineTo(cx - radius * 0.38f, cy - radius * 0.25f)
                }
                drawPath(leftChevron, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Right Chevron
                val rightChevron = Path().apply {
                    moveTo(cx, cy - radius * 0.65f)
                    lineTo(cx + radius * 0.38f, cy - radius * 0.25f)
                }
                drawPath(rightChevron, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Crossguard of Justice
                drawLine(
                    color = deity.secondaryColor,
                    start = Offset(cx - radius * 0.3f, cy + radius * 0.15f),
                    end = Offset(cx + radius * 0.3f, cy + radius * 0.15f),
                    strokeWidth = 3.dp.toPx()
                )

                // Gleipnir Golden Ribbon loop
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                    radius = radius * 0.22f,
                    center = Offset(cx, cy + radius * 0.28f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            NorseDeity.FREYA -> {
                // Fehu Rune ᚠ
                val stemPath = Path().apply {
                    moveTo(cx - radius * 0.1f, cy - radius * 0.65f)
                    lineTo(cx - radius * 0.1f, cy + radius * 0.65f)
                }
                drawPath(stemPath, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Upper arm
                val upperArm = Path().apply {
                    moveTo(cx - radius * 0.1f, cy - radius * 0.45f)
                    lineTo(cx + radius * 0.35f, cy - radius * 0.68f)
                }
                drawPath(upperArm, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Lower arm
                val lowerArm = Path().apply {
                    moveTo(cx - radius * 0.1f, cy - radius * 0.15f)
                    lineTo(cx + radius * 0.30f, cy - radius * 0.38f)
                }
                drawPath(lowerArm, color = deity.accentColor, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))

                // Brísingamen Golden Torc arc
                val torcPath = Path().apply {
                    moveTo(cx - radius * 0.45f, cy + radius * 0.1f)
                    cubicTo(
                        cx - radius * 0.25f, cy + radius * 0.55f,
                        cx + radius * 0.25f, cy + radius * 0.55f,
                        cx + radius * 0.45f, cy + radius * 0.1f
                    )
                }
                drawPath(torcPath, color = Color(0xFFF59E0B), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

                // Amber Tear Drops
                drawCircle(color = Color(0xFFEF4444), radius = 3.dp.toPx(), center = Offset(cx, cy + radius * 0.48f))
                drawCircle(color = Color(0xFFF59E0B), radius = 2.5.dp.toPx(), center = Offset(cx - radius * 0.2f, cy + radius * 0.4f))
                drawCircle(color = Color(0xFFF59E0B), radius = 2.5.dp.toPx(), center = Offset(cx + radius * 0.2f, cy + radius * 0.4f))
            }
            NorseDeity.SYNCRETIC -> {
                // Tiwaz Arrow (Left)
                val leftStem = Path().apply {
                    moveTo(cx - radius * 0.25f, cy - radius * 0.55f)
                    lineTo(cx - radius * 0.25f, cy + radius * 0.55f)
                }
                drawPath(leftStem, color = AsgardLight, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                drawLine(
                    color = AsgardLight,
                    start = Offset(cx - radius * 0.25f, cy - radius * 0.55f),
                    end = Offset(cx - radius * 0.45f, cy - radius * 0.3f),
                    strokeWidth = 4.dp.toPx()
                )
                drawLine(
                    color = AsgardLight,
                    start = Offset(cx - radius * 0.25f, cy - radius * 0.55f),
                    end = Offset(cx - radius * 0.05f, cy - radius * 0.3f),
                    strokeWidth = 4.dp.toPx()
                )

                // Fehu Rune (Right)
                val rightStem = Path().apply {
                    moveTo(cx + radius * 0.15f, cy - radius * 0.55f)
                    lineTo(cx + radius * 0.15f, cy + radius * 0.55f)
                }
                drawPath(rightStem, color = VanaheimLight, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                drawLine(
                    color = VanaheimLight,
                    start = Offset(cx + radius * 0.15f, cy - radius * 0.4f),
                    end = Offset(cx + radius * 0.45f, cy - radius * 0.58f),
                    strokeWidth = 4.dp.toPx()
                )
                drawLine(
                    color = VanaheimLight,
                    start = Offset(cx + radius * 0.15f, cy - radius * 0.15f),
                    end = Offset(cx + radius * 0.40f, cy - radius * 0.33f),
                    strokeWidth = 4.dp.toPx()
                )

                // Harmonizing Arc
                val unionArc = Path().apply {
                    moveTo(cx - radius * 0.4f, cy + radius * 0.35f)
                    quadraticTo(cx, cy + radius * 0.65f, cx + radius * 0.4f, cy + radius * 0.35f)
                }
                drawPath(unionArc, color = Color(0xFFA855F7), style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}
