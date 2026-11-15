package com.example.tyrconsulting.ui.components

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "GeminiChat"

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
    val isStreaming: Boolean = false,
    val recommendedSkus: List<String> = emptyList()
)

enum class MessageSender {
    USER, ASSISTANT
}

enum class GeminiModelOption(val modelId: String, val label: String, val description: String) {
    GENERAL("gemini-3.8-flash", "3.8 Flash (Balanced)", "General scoping & consulting"),
    FAST("gemini-3.5-flash-lite", "3.5 Flash-Lite (Fast)", "Low-latency rapid answers"),
    COMPLEX("gemini-4-argon", "4 Argon (Complex)", "Deep technical & contract reasoning")
}

enum class AdvisorPersona(val title: String, val rune: String, val instruction: String) {
    MYTHIC_SAGE("Norse Sage", "ᛏ ᚠ", "Speak with the noble, steadfast, alliterative voice of an ancient Norse law-giver and wealth sovereign, quoting runes and wisdom alongside precise technical specs."),
    SYSTEMS_ARCHITECT("Tech Architect", "⚙️", "Speak with the razor-sharp, objective clarity of an enterprise solutions architect, zero-trust security engineer, and cloud infrastructure auditor.")
}

private const val SYSTEM_INSTRUCTION_TEXT = """
You are the TYR & Freyja Technical Consulting Advisor — an AI scoping and architecture assistant for CALEB TR HOLCOMB d/b/a TYR (TYR Technical Consulting).
You embody the Norse ethos of:
- TYR (ᛏ Tiwaz): God of justice, truth, sworn oaths, security, anti-spoofing, and zero-custody data integrity.
- FREYJA (ᚠ Fehu): Goddess of sovereignty, wealth, capital flow, financial audit clarity, and resilience.

Organization Details:
- Name: TYR Technical Consulting (CALEB TR HOLCOMB d/b/a TYR)
- Phone: (659) 257-8043
- Email: tyr@tyrprotocol.pro
- NAICS Codes: 541512 (Computer Systems Design) & 541611 (Admin & Mgmt Consulting)
- Zero-Custody Privacy Rule: We NEVER keep raw financial/bank statements or client credentials. Complete master files are delivered to the client, and all source working files are purged on day 0 after verified client handover. Zero ongoing SaaS lock-in or platform extortion.
- Pricing: 100% transparent flat-rate services ($65 to $495).

Catalog of Services:
- [SVC-WEB-01] Midgard Website Launch ($245)
- [SVC-DOM-02] Asgard Domain & Email Shield ($165)
- [SVC-KIT-03] Vanaheim QR & Form Kit ($150)
- [SVC-CAR-04] Alfheim Resume & Bid Overhaul ($145)
- [SVC-OPS-05] Nidavellir 3-Doc E-Sign Kit ($185)
- [SVC-BUY-06] Muspelheim Buyer's Dossier ($150)
- [SVC-CMD-07] Svartalfheim Budget & Schedule C Hub ($195)
- [SVC-VLT-08] Niflheim Insurance & Legacy Vault ($155)
- [SVC-AUD-09] Jotunheim Billing & ISP Dispute Kit ($175)
- [SVC-LOK-10] Loki Digital Flyer & Ad Pack ($85 / $115)
- [SVC-GRM-11] Garm Privacy Scrub & Freeze Shield ($95 / $125)
- [SVC-FEN-12] Fenrir Free-Software Switch Kit ($115 / $145)
- [SVC-SOP-11] Asynchronous Operational SOP Vault ($195)
- [SVC-SEC-12] Hardened Workstation & DNS Profile Kit ($145)
- [SVC-TAX-13] 1099 Contractor Schedule C Ledger ($165)
- [SVC-BCK-14] Cold-Storage 3-2-1 Backup Runbook ($160)
- [SVC-DCK-15] Executive Proposal & Pitch Deck Kit ($140)
- [SVC-B2B-16] B2B Vendor Onboarding & Master MSA Suite ($295)
- [SVC-B2B-17] Corporate Anti-Phishing & Wire Protocol ($245)
- [B2B-SEC-01] B2B Monthly Cyber Hygiene Retainer ($125–$175/mo)
- [B2B-SLA-02] Cloud & SaaS Spend Drift Watchdog ($145–$195/mo)
- [B2B-LGL-03] Vendor Contract Renewal Sentinel ($95–$135/mo)
- [B2B-SEO-04] B2B Synthetic Uptime & Link Health ($85–$120/mo)
- [B2B-SOP-05] Quarterly SOP Maintenance & Onboarding Desk ($165–$225/mo)
- [RET-ALL-07] Yggdrasil Complete Retainer ($495 / $165–$215 mo)
- [RET-B2B-08] Corporate Sentry & Infrastructure Retainer ($345–$495 mo)

Guidelines:
1. Provide concise, expert, and actionable advice on web architecture, anti-spoofing DNS, fillable contracts, budget ledgering, and monthly care plans.
2. Recommend 1 to 3 specific SKUs in brackets like [SVC-WEB-01] or [SVC-DOM-02] when matching client needs.
3. Maintain an honorable, precise, clear, steadfast tone. Emphasize sovereign client data ownership.
"""

class GeminiChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedModel = MutableStateFlow(GeminiModelOption.GENERAL)
    val selectedModel: StateFlow<GeminiModelOption> = _selectedModel.asStateFlow()

    private val _useCloudAi = MutableStateFlow(true)
    val useCloudAi: StateFlow<Boolean> = _useCloudAi.asStateFlow()

    private val _selectedPersona = MutableStateFlow(AdvisorPersona.MYTHIC_SAGE)
    val selectedPersona: StateFlow<AdvisorPersona> = _selectedPersona.asStateFlow()

    init {
        // Initial greeting
        _messages.value = listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Hail traveler. I am the TYR & Freyja Technical Consulting Advisor.\n\nUnder the covenant of Týr (ᛏ Justice & Defensive Armor) and Freyja (ᚠ Sovereign Wealth & Expense Audit), how may I assist your business or technical systems today?",
                recommendedSkus = listOf("SVC-WEB-01", "SVC-DOM-02")
            )
        )
    }

    fun setModel(model: GeminiModelOption) {
        _selectedModel.value = model
    }

    fun setUseCloudAi(useCloud: Boolean) {
        _useCloudAi.value = useCloud
    }

    fun setPersona(persona: AdvisorPersona) {
        _selectedPersona.value = persona
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isLoading.value) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = userText.trim())
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        val assistantMsgId = UUID.randomUUID().toString()

        if (!_useCloudAi.value) {
            viewModelScope.launch {
                val placeholderMsg = ChatMessage(
                    id = assistantMsgId,
                    sender = MessageSender.ASSISTANT,
                    text = "",
                    isStreaming = false
                )
                _messages.value = _messages.value + placeholderMsg
                fallbackToKnowledgeBase(assistantMsgId, userText)
                _isLoading.value = false
            }
            return
        }

        viewModelScope.launch {
            val placeholderMsg = ChatMessage(
                id = assistantMsgId,
                sender = MessageSender.ASSISTANT,
                text = "",
                isStreaming = true
            )
            _messages.value = _messages.value + placeholderMsg

            try {
                // Initialize model lazily with system instruction and active persona
                val modelName = _selectedModel.value.modelId
                val fullInstruction = SYSTEM_INSTRUCTION_TEXT + "\n\nActive Persona Tone: " + _selectedPersona.value.instruction
                val generativeModel = Firebase.ai.generativeModel(
                    modelName = modelName,
                    systemInstruction = content { text(fullInstruction) }
                )

                // Build conversation history for multi-turn chat
                val historyContents = _messages.value.dropLast(1).map { msg ->
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    content(role) { text(msg.text) }
                }

                val chatSession = generativeModel.startChat(historyContents)

                val accumulated = StringBuilder()
                withTimeout(60_000L) {
                    chatSession.sendMessageStream(userText).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        val skus = extractSkus(accumulated.toString())
                        updateAssistantMessage(assistantMsgId, accumulated.toString(), isStreaming = true, skus = skus)
                    }
                }

                val finalOutput = accumulated.toString()
                if (finalOutput.isNotBlank()) {
                    val finalSkus = extractSkus(finalOutput)
                    updateAssistantMessage(assistantMsgId, finalOutput, isStreaming = false, skus = finalSkus)
                } else {
                    fallbackToKnowledgeBase(assistantMsgId, userText)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Firebase AI inference fallback triggered: ${e.message}")
                fallbackToKnowledgeBase(assistantMsgId, userText)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateAssistantMessage(id: String, text: String, isStreaming: Boolean, skus: List<String>) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == id) {
                msg.copy(text = text, isStreaming = isStreaming, recommendedSkus = skus)
            } else {
                msg
            }
        }
    }

    private fun fallbackToKnowledgeBase(assistantId: String, query: String) {
        val lower = query.lowercase(Locale.US)
        val matchedSkus = mutableListOf<String>()
        val response = when {
            lower.contains("web") || lower.contains("site") || lower.contains("launch") -> {
                matchedSkus.addAll(listOf("SVC-WEB-01", "SVC-DOM-02"))
                "ᛏ TYR Architecture Guidance:\nFor modern business web presence, we recommend the [SVC-WEB-01] Midgard Website Launch ($245 Flat) paired with [SVC-DOM-02] Asgard Domain & Email Shield ($165 Flat).\n\n• Zero bloated CMS databases or vulnerable WordPress plugins.\n• Sub-second load times with clean semantic HTML5.\n• 100% sovereign client asset ownership with no ongoing SaaS extortion."
            }
            lower.contains("email") || lower.contains("spam") || lower.contains("spoof") || lower.contains("dns") || lower.contains("domain") -> {
                matchedSkus.add("SVC-DOM-02")
                "ᛉ Asgard Email Shield Guidance:\nWe configure [SVC-DOM-02] Domain & Email Shield ($165 Flat) with complete SPF, 2048-bit DKIM, and strict DMARC reject policies.\n\n• Stops criminals from spoofing your brand name.\n• Ensures client communications bypass spam filters directly into inboxes.\n• Zero mailbox maintenance fees using secure serverless routing."
            }
            lower.contains("contract") || lower.contains("sign") || lower.contains("sow") || lower.contains("msa") -> {
                matchedSkus.add("SVC-OPS-05")
                "ᚷ Nidavellir Contract Vault:\nWe recommend [SVC-OPS-05] 3-Doc E-Sign Kit ($185 Flat), including Master Services Agreement (MSA), Statement of Work (SOW), and Mutual NDA.\n\n• Compliant with 15 U.S.C. § 7001 (E-SIGN) & Alabama UETA.\n• Form-fillable PDF templates configured for any e-signature platform.\n• Ironclad intellectual property protections and milestone billing guardrails."
            }
            lower.contains("budget") || lower.contains("tax") || lower.contains("expense") || lower.contains("audit") || lower.contains("bill") || lower.contains("freya") -> {
                matchedSkus.addAll(listOf("SVC-CMD-07", "SUB-FIN-04"))
                "ᚠ Freyja Sovereign Wealth Guidance:\nUnder the blessing of Freyja (Fehu — Sovereign Capital), we recommend [SVC-CMD-07] Budget & Schedule C Hub ($195) or [SUB-FIN-04] Expense Optimizer Retainer ($85–$115/mo).\n\n• Zero-Custody Guarantee: 0 raw statements stored; 0-day file purge upon completion.\n• Automated Schedule C tax categorization and zombie SaaS subscription termination.\n• Forensic auditing clawing back unauthorized telecom and merchant fees."
            }
            lower.contains("zero") || lower.contains("custody") || lower.contains("privacy") -> {
                matchedSkus.add("SVC-GRM-11")
                "🛡️ Zero-Custody Privacy Covenant:\nTYR Technical Consulting enforces absolute data sovereignty: 0 Raw Financial Data Stored. Raw bank/utility statements are purged immediately (0-Day) upon audit completion; only the sanitized flat-grid ledger and savings report are retained by the client."
            }
            else -> {
                matchedSkus.addAll(listOf("SVC-WEB-01", "SVC-DOM-02", "SVC-OPS-05"))
                "ᛏ TYR Technical Consulting Overview:\nCALEB TR HOLCOMB d/b/a TYR provides defensive cybersecurity, administrative automation, and consumer expense auditing under flat-rate pricing ($65–$495).\n\n1. Sovereign Ownership: Zero recurring platform lock-in.\n2. Defensive Posture: Hardened DNSSEC & anti-spoofing.\n3. Zero-Custody: 0-day file purge post delivery.\n\nRecommended starting packages: [SVC-WEB-01] ($245) or [SVC-DOM-02] ($165)."
            }
        }
        updateAssistantMessage(assistantId, response, isStreaming = false, skus = matchedSkus)
    }

    private fun extractSkus(text: String): List<String> {
        val knownSkus = TyrCatalog.services.map { it.sku }
        return knownSkus.filter { text.contains(it) }
    }
}

@Composable
fun GeminiChatView(
    onSelectSku: (String) -> Unit = {},
    onLaunchIntake: (List<String>) -> Unit = {},
    onNavigateToRelay: () -> Unit = {},
    modifier: Modifier = Modifier,
    chatViewModel: GeminiChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val useCloudAi by chatViewModel.useCloudAi.collectAsState()
    val selectedPersona by chatViewModel.selectedPersona.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .testTag("gemini_chat_view")
    ) {
        // Chat Header with Model Selector & Google Chat Relay Dispatch
        Surface(
            color = BgSurface,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MidgardMain),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedPersona.rune, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("TYR & Freyja AI Advisor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (useCloudAi) "Gemini Cloud AI Active • ${selectedModel.label}" else "Sovereign Offline Knowledge Base Active",
                                color = if (useCloudAi) AsgardLight else VanaheimLight,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Google Chat / Lead Architect Relay Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Master Gemini Cloud Toggle Switch
                        Switch(
                            checked = useCloudAi,
                            onCheckedChange = { chatViewModel.setUseCloudAi(it) },
                            modifier = Modifier.testTag("switch_chat_cloud_ai"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MidgardMain,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BgAttr
                            )
                        )

                        Button(
                            onClick = onNavigateToRelay,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("📡 Relay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val transcript = messages.joinToString("\n\n") { msg ->
                                    "${if (msg.sender == MessageSender.USER) "CLIENT" else "TYR_ADVISOR"} [${msg.timestamp}]:\n${msg.text}"
                                }
                                val subject = "Google Chat Consultation Transcript Relay"
                                val body = "Hello Caleb Holcomb,\n\nHere is the active AI consultation transcript from the TYR mobile app:\n\n$transcript\n\nPlease reach out to coordinate client engagement."
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${TyrCatalog.email}".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, body)
                                }
                                safeStartActivity(context, intent, fallbackCopyText = body)
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Email", modifier = Modifier.size(12.dp), tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Gemini Model & Persona Option Toggles Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (useCloudAi) {
                        Text("Model:", color = TextMuted, fontSize = 9.sp)
                        GeminiModelOption.values().forEach { opt ->
                            val isSelected = selectedModel == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { chatViewModel.setModel(opt) },
                                label = { Text(opt.label, fontSize = 8.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MidgardMain,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Persona Toggle Chips
                    AdvisorPersona.values().forEach { persona ->
                        val isSelected = selectedPersona == persona
                        FilterChip(
                            selected = isSelected,
                            onClick = { chatViewModel.setPersona(persona) },
                            label = { Text("${persona.rune} ${persona.title}", fontSize = 8.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AsgardMain.copy(alpha = 0.5f),
                                selectedLabelColor = AsgardLight
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onSelectSku = onSelectSku,
                    onLaunchIntake = onLaunchIntake
                )
            }

            if (isLoading && (messages.isEmpty() || !messages.last().isStreaming)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MidgardLight,
                            strokeWidth = 2.dp
                        )
                        Text("Týr & Freyja are consulting the runes...", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }

        // Suggested Queries Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Web & Security Pack?",
                "How does Zero-Custody work?",
                "Asgard Email Shield",
                "Contract E-Sign Kit"
            ).forEach { promptChip ->
                Surface(
                    color = BgCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.clickable {
                        inputPrompt = promptChip
                        chatViewModel.sendMessage(promptChip)
                        inputPrompt = ""
                    }
                ) {
                    Text(
                        promptChip,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Chat Input Bar
        Surface(
            color = BgCard,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask Týr & Freyja about architecture, audit, or SKUs...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = MidgardMain,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputPrompt.isNotBlank()) {
                            val textToSend = inputPrompt
                            inputPrompt = ""
                            chatViewModel.sendMessage(textToSend)
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank()) {
                            val textToSend = inputPrompt
                            inputPrompt = ""
                            chatViewModel.sendMessage(textToSend)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputPrompt.isNotBlank()) MidgardMain else BgAttr)
                        .testTag("chat_send_button"),
                    enabled = inputPrompt.isNotBlank() && !isLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputPrompt.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSelectSku: (String) -> Unit,
    onLaunchIntake: (List<String>) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                if (isUser) "You" else "Týr & Freyja Advisor",
                color = if (isUser) MidgardLight else AsgardLight,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(message.timestamp, color = TextMuted, fontSize = 9.sp)
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(if (isUser) Color(0xFF261216) else BgCard)
                .border(
                    1.dp,
                    if (isUser) MidgardMain.copy(alpha = 0.5f) else BorderSubtle,
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (message.text.isBlank() && message.isStreaming) "Consulting the runes..." else message.text,
                    color = TextMain,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // Recommended SKUs Cards inside Assistant Response
                if (message.recommendedSkus.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgAttr, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "RECOMMENDED SERVICE PACK:",
                            color = AsgardLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )

                        message.recommendedSkus.forEach { sku ->
                            val s = TyrCatalog.services.find { it.sku == sku }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSku(sku) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("•", color = MidgardLight, fontSize = 11.sp)
                                    Text(sku, color = MidgardLight, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(s?.title ?: "", color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                                }
                                Text(s?.price ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { onLaunchIntake(message.recommendedSkus) },
                            colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text("Launch Intake with Recommended SKUs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
