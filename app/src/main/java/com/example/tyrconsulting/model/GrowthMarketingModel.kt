package com.example.tyrconsulting.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.net.URLEncoder

enum class GrowthChannel(val id: String, val label: String) {
    NEXTDOOR("NEXTDOOR", "Nextdoor"),
    FACEBOOK("FACEBOOK", "Facebook"),
    SMS("SMS", "SMS / Text"),
    EMAIL("EMAIL", "Email / Brief")
}

data class QuickAuditOption(
    val label: String,
    val scorePenalty: Int,
    val skus: List<String>
)

data class QuickAuditQuestion(
    val id: String,
    val prompt: String,
    val options: List<QuickAuditOption>
)

data class QuickAuditResult(
    val score: Int,
    val riskFlags: List<String>,
    val recommendedSkus: List<String>,
    val estAnnualLeakUsd: Int
)

data class CaseStudyProof(
    val id: String,
    val region: String,
    val headline: String,
    val sku: String,
    val beforeState: String,
    val deliverable: String,
    val verifiedOutcome: String
)

data class GbpChecklistItem(
    val id: String,
    val title: String,
    val cost: String,
    val detail: String
)

/**
 * Pure-Kotlin ISO/IEC 18004 QR Code Matrix Encoder (Versions 1-6, ECC-L, Byte Mode).
 * Generates crisp offline vector QR codes for Canvas & Android PrintManager with zero external dependencies.
 */
object GrowthQrEncoder {
    private data class QrSpec(
        val version: Int,
        val size: Int,
        val dataCw: Int,
        val eccPerBlock: Int,
        val blocks: Int,
        val align: IntArray
    )

    private val specs = listOf(
        QrSpec(1, 21, 19, 7, 1, intArrayOf()),
        QrSpec(2, 25, 34, 10, 1, intArrayOf(6, 18)),
        QrSpec(3, 29, 55, 15, 1, intArrayOf(6, 22)),
        QrSpec(4, 33, 80, 20, 1, intArrayOf(6, 26)),
        QrSpec(5, 37, 108, 26, 1, intArrayOf(6, 30)),
        QrSpec(6, 41, 136, 18, 2, intArrayOf(6, 34))
    )

    private val gfExp = IntArray(512)
    private val gfLog = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            gfExp[i] = x
            gfLog[x] = i
            x = x shl 1
            if ((x and 0x100) != 0) x = x xor 0x11d
        }
        for (i in 255 until 512) {
            gfExp[i] = gfExp[i - 255]
        }
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return gfExp[gfLog[a] + gfLog[b]]
    }

    private fun rsGeneratorPoly(deg: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until deg) {
            val next = IntArray(poly.size + 1)
            val factor = gfExp[i]
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor gfMul(poly[j], factor)
            }
            poly = next
        }
        return poly
    }

    private fun rsComputeEcc(data: IntArray, eccLen: Int): IntArray {
        val gen = rsGeneratorPoly(eccLen)
        val res = IntArray(data.size + eccLen)
        data.copyInto(res, 0)
        for (i in data.indices) {
            val coef = res[i]
            if (coef != 0) {
                for (j in 1 until gen.size) {
                    res[i + j] = res[i + j] xor gfMul(gen[j], coef)
                }
            }
        }
        return res.copyOfRange(data.size, res.size)
    }

    fun buildMatrix(text: String): Array<BooleanArray> {
        val rawBytes = text.ifBlank { "https://tyrprotocol.pro" }.toByteArray(Charsets.UTF_8)
        val spec = specs.firstOrNull { rawBytes.size + 2 <= it.dataCw } ?: specs.last()
        val maxPayload = minOf(rawBytes.size, spec.dataCw - 2)

        val bits = ArrayList<Int>(spec.dataCw * 8)
        fun pushBits(value: Int, count: Int) {
            for (i in (count - 1) downTo 0) {
                bits.add((value shr i) and 1)
            }
        }

        pushBits(0b0100, 4) // Byte mode
        pushBits(maxPayload, 8)
        for (i in 0 until maxPayload) {
            pushBits(rawBytes[i].toInt() and 0xFF, 8)
        }
        val totalDataBits = spec.dataCw * 8
        val termLen = minOf(4, totalDataBits - bits.size)
        pushBits(0, termLen)
        while (bits.size % 8 != 0) bits.add(0)

        val dataCw = ArrayList<Int>(spec.dataCw)
        var idx = 0
        while (idx < bits.size) {
            var b = 0
            for (j in 0 until 8) {
                b = (b shl 1) or bits[idx + j]
            }
            dataCw.add(b)
            idx += 8
        }
        var padToggle = 0
        while (dataCw.size < spec.dataCw) {
            dataCw.add(if (padToggle % 2 == 0) 0xEC else 0x11)
            padToggle++
        }

        val blockSize = spec.dataCw / spec.blocks
        val dataBlocks = ArrayList<IntArray>(spec.blocks)
        val eccBlocks = ArrayList<IntArray>(spec.blocks)
        for (b in 0 until spec.blocks) {
            val slice = IntArray(blockSize) { offset -> dataCw[b * blockSize + offset] }
            dataBlocks.add(slice)
            eccBlocks.add(rsComputeEcc(slice, spec.eccPerBlock))
        }

        val codewords = ArrayList<Int>(spec.dataCw + spec.blocks * spec.eccPerBlock)
        for (i in 0 until blockSize) {
            for (b in 0 until spec.blocks) codewords.add(dataBlocks[b][i])
        }
        for (i in 0 until spec.eccPerBlock) {
            for (b in 0 until spec.blocks) codewords.add(eccBlocks[b][i])
        }

        val n = spec.size
        val mod = Array(n) { IntArray(n) { -1 } }
        val isFunc = Array(n) { BooleanArray(n) }

        fun setFunc(r: Int, c: Int, on: Boolean) {
            if (r in 0 until n && c in 0 until n) {
                mod[r][c] = if (on) 1 else 0
                isFunc[r][c] = true
            }
        }

        fun drawFinder(r0: Int, c0: Int) {
            for (dr in -1..7) {
                for (dc in -1..7) {
                    val r = r0 + dr
                    val c = c0 + dc
                    if (r !in 0 until n || c !in 0 until n) continue
                    val on = dr in 0..6 && dc in 0..6 &&
                        (dr == 0 || dr == 6 || dc == 0 || dc == 6 || (dr in 2..4 && dc in 2..4))
                    setFunc(r, c, on)
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, n - 7)
        drawFinder(n - 7, 0)

        for (i in 8 until (n - 8)) {
            setFunc(6, i, i % 2 == 0)
            setFunc(i, 6, i % 2 == 0)
        }

        if (spec.align.isNotEmpty()) {
            for (ar in spec.align) {
                for (ac in spec.align) {
                    if (isFunc[ar][ac]) continue
                    for (dr in -2..2) {
                        for (dc in -2..2) {
                            val on = kotlin.math.abs(dr) == 2 || kotlin.math.abs(dc) == 2 || (dr == 0 && dc == 0)
                            setFunc(ar + dr, ac + dc, on)
                        }
                    }
                }
            }
        }

        setFunc(n - 8, 8, true)
        for (i in 0 until 9) {
            if (i != 6) {
                isFunc[8][i] = true
                isFunc[i][8] = true
            }
        }
        for (i in 0 until 8) {
            isFunc[8][n - 1 - i] = true
            if (i < 7) isFunc[n - 1 - i][8] = true
        }

        val allBits = ArrayList<Int>(codewords.size * 8)
        for (cw in codewords) {
            for (b in 7 downTo 0) allBits.add((cw shr b) and 1)
        }
        var bitIdx = 0
        var up = true
        var col = n - 1
        while (col >= 1) {
            if (col == 6) col--
            for (step in 0 until n) {
                val row = if (up) n - 1 - step else step
                for (dx in 0..1) {
                    val c = col - dx
                    if (!isFunc[row][c]) {
                        mod[row][c] = if (bitIdx < allBits.size) allBits[bitIdx++] else 0
                    }
                }
            }
            up = !up
            col -= 2
        }

        for (r in 0 until n) {
            for (c in 0 until n) {
                if (!isFunc[r][c] && (r + c) % 2 == 0) {
                    mod[r][c] = mod[r][c] xor 1
                }
            }
        }

        val fmt = 0b111011111000100
        for (i in 0 until 15) {
            val bit = (fmt shr (14 - i)) and 1
            when {
                i < 6 -> mod[8][i] = bit
                i == 6 -> mod[8][7] = bit
                i == 7 -> mod[8][8] = bit
                i == 8 -> mod[7][8] = bit
                else -> mod[14 - i][8] = bit
            }
            if (i < 7) mod[n - 1 - i][8] = bit
            else mod[8][n - 15 + i] = bit
        }

        return Array(n) { r -> BooleanArray(n) { c -> mod[r][c] == 1 } }
    }

    fun renderSvgString(text: String, sizePx: Int = 180): String {
        val matrix = buildMatrix(text)
        val n = matrix.size
        val quiet = 2
        val total = n + quiet * 2
        val sb = StringBuilder()
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (matrix[r][c]) {
                    sb.append("M").append(c + quiet).append(",").append(r + quiet).append("h1v1h-1z")
                }
            }
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 $total $total\" width=\"$sizePx\" height=\"$sizePx\"><rect width=\"$total\" height=\"$total\" fill=\"#ffffff\"/><path d=\"$sb\" fill=\"#090a0f\"/></svg>"
    }
}

object GrowthMarketingManager {
    private const val PREFS_NAME = "tyr_growth_marketing_prefs"
    private const val KEY_INBOUND_REF = "active_inbound_ref_code"
    private const val KEY_GBP_PREFIX = "gbp_chk_"
    private const val KEY_OUTBOUND_SHARES = "outbound_shares_count"

    const val MECARD_QR_STRING =
        "MECARD:N:Holcomb,Caleb;TEL:+16592578043;EMAIL:tyr@tyrprotocol.pro;URL:https://tyrprotocol.pro;NOTE:TYR Technical Consulting;;"

    val vCardPayload: String = listOf(
        "BEGIN:VCARD",
        "VERSION:3.0",
        "N:Holcomb;Caleb;TR;;",
        "FN:Caleb Holcomb — TYR Technical Consulting",
        "ORG:TYR Technical Consulting (CALEB TR HOLCOMB d/b/a TYR)",
        "TITLE:Principal Systems Engineer & Expense Auditor",
        "TEL;TYPE=WORK,VOICE,CELL,MSG:+16592578043",
        "EMAIL;TYPE=WORK,PREF,INTERNET:tyr@tyrprotocol.pro",
        "EMAIL;TYPE=HOME,INTERNET:colacal43@gmail.com",
        "URL;TYPE=WORK:https://tyrprotocol.pro",
        "URL;TYPE=GitHub:https://github.com/kalcola/tyrs-technical-consultation-",
        "ADR;TYPE=WORK:;;;Northwest Alabama;AL;;United States",
        "NOTE:NAICS 541512 & 541611. 100% Remote & Northwest Alabama. Zero-Custody 0-Day Data Purge.",
        "END:VCARD"
    ).joinToString("\r\n")

    val quizQuestions: List<QuickAuditQuestion> = listOf(
        QuickAuditQuestion(
            id = "q_web",
            prompt = "1. How much do you pay monthly for your website & hosting?",
            options = listOf(
                QuickAuditOption("$30–$150/mo (Wix, Squarespace, GoDaddy, agency)", 25, listOf("SVC-WEB-01")),
                QuickAuditOption("No website yet — only Facebook/word of mouth", 20, listOf("SVC-WEB-01", "SVC-KIT-03")),
                QuickAuditOption("$0/mo static site I fully control", 0, emptyList())
            )
        ),
        QuickAuditQuestion(
            id = "q_email",
            prompt = "2. Is your business email shielded with custom DMARC, SPF & DKIM?",
            options = listOf(
                QuickAuditOption("Using @gmail.com / @yahoo.com or unsure about DMARC", 25, listOf("SVC-DOM-02", "SVC-B2B-17")),
                QuickAuditOption("Domain exists but spoofing/spam filters are weak", 15, listOf("SVC-DOM-02")),
                QuickAuditOption("Hardened p=reject DMARC & DKIM enforced", 0, emptyList())
            )
        ),
        QuickAuditQuestion(
            id = "q_legal",
            prompt = "3. Do you have written SOWs/Contracts & E-Sign ready before deposits?",
            options = listOf(
                QuickAuditOption("Verbal handshakes or recycled Word templates", 25, listOf("SVC-OPS-05", "SVC-B2B-16")),
                QuickAuditOption("Basic form, no cryptographic receipt", 15, listOf("SVC-OPS-05")),
                QuickAuditOption("Signed MSA/SOW + Stripe milestone splits active", 0, emptyList())
            )
        ),
        QuickAuditQuestion(
            id = "q_bills",
            prompt = "4. When did you last audit ISP, cell, SaaS creep & Schedule C logs?",
            options = listOf(
                QuickAuditOption("Over a year ago — creeping telecom/vendor fees", 25, listOf("SVC-AUD-09", "SVC-CMD-07")),
                QuickAuditOption("Need organized tax-ready Schedule C & legacy vault", 15, listOf("SVC-TAX-13", "SVC-VLT-08")),
                QuickAuditOption("Audited quarterly with 0-day statement purge", 0, emptyList())
            )
        )
    )

    val caseStudies: List<CaseStudyProof> = listOf(
        CaseStudyProof(
            id = "CS-NWA-01",
            region = "Northwest Alabama — Independent Field Trades",
            headline = "Eliminated $588/yr Hosting Rent + Doubled Callbacks",
            sku = "SVC-WEB-01 + SVC-KIT-03",
            beforeState = "Paying $49/mo for a slow builder with no offline QR/contact card.",
            deliverable = "Static $0/mo mobile-first storefront, truck-magnet QR flyer & .vcf intake.",
            verifiedOutcome = "$588/yr fixed cost cut • 48h turnaround • 100% master Git repo ownership."
        ),
        CaseStudyProof(
            id = "CS-NWA-02",
            region = "Muscle Shoals Corridor — B2B Service Operator",
            headline = "Stopped Vendor Spoofing & Locked Down Wire Protocols",
            sku = "SVC-DOM-02 + SVC-B2B-17",
            beforeState = "Sending invoices from free webmail; invoices landing in spam.",
            deliverable = "SPF/DKIM/DMARC p=reject DNS lockdown + wire SOP + 2FA key runbook.",
            verifiedOutcome = "100% DMARC enforcement • Zero SaaS retainers added • Delivered in 36h."
        ),
        CaseStudyProof(
            id = "CS-NWA-03",
            region = "Northwest Alabama — Sole Proprietor (1099)",
            headline = "Clawed Back $74/mo ISP Creep + Clean Schedule C Ledger",
            sku = "SVC-AUD-09 + SVC-TAX-13",
            beforeState = "Unchecked telecom surcharges & uncategorized receipts at tax time.",
            deliverable = "ISP dispute escalation script + offline CSV/MD Schedule C ledger (0-day purge).",
            verifiedOutcome = "$888/yr overhead eliminated • Zero raw bank files retained."
        ),
        CaseStudyProof(
            id = "CS-NWA-04",
            region = "Remote US Subcontractor — Technical Consulting",
            headline = "Standardized E-Sign SOW & 50% Deposit Split Flow",
            sku = "SVC-OPS-05 + SVC-B2B-16",
            beforeState = "Scope creep and slow invoice payouts without signed change orders.",
            deliverable = "3-Doc E-Sign SOW/MSA Kit with SHA-384 receipts & Stripe 50/50 split.",
            verifiedOutcome = "0 unpaid kickoff hours • Verifiably hashed contracts from day one."
        )
    )

    val gbpChecklistItems: List<GbpChecklistItem> = listOf(
        GbpChecklistItem(
            id = "gbp_sab",
            title = "Google Business Profile — Service-Area Mode (Hide Street Address)",
            cost = "$0",
            detail = "Select Service-Area Business (No physical storefront), hide residential address, and set Northwest Alabama & United States."
        ),
        GbpChecklistItem(
            id = "gbp_cats",
            title = "Primary Category: Computer Support & IT Consultant",
            cost = "$0",
            detail = "Add Web Designer & Management Consultant (NAICS 541512 & 541611) + add all 31 SKUs under Products."
        ),
        GbpChecklistItem(
            id = "apple_connect",
            title = "Apple Business Connect (Apple Maps, Siri & Spotlight)",
            cost = "$0",
            detail = "Claim CALEB TR HOLCOMB d/b/a TYR with Service-Area posture, tyrprotocol.pro, and +1-659-257-8043."
        ),
        GbpChecklistItem(
            id = "bing_places",
            title = "Bing Places for Business (Edge, Windows & Copilot Search)",
            cost = "$0",
            detail = "Sync directly from your verified Google Business Profile in under 2 minutes."
        ),
        GbpChecklistItem(
            id = "nextdoor_biz",
            title = "Nextdoor Free Business Page + Local Recommendations",
            cost = "$0",
            detail = "Claim Northwest Alabama Business Page and post 1 practical cost-cut/anti-scam tip weekly with your ?sku= link."
        ),
        GbpChecklistItem(
            id = "nap_audit",
            title = "NAP + GitHub Citation Consistency Audit",
            cost = "$0",
            detail = "Match verbatim: \"TYR Technical Consulting\", \"(659) 257-8043\", \"tyr@tyrprotocol.pro\", \"https://tyrprotocol.pro\"."
        ),
        GbpChecklistItem(
            id = "review_qr",
            title = "Print 3.5x2 Leave-Behind QR Pocket Cards on Completion",
            cost = "$0-Ink",
            detail = "Hand closed clients a pocket leave-behind card stamped with your 10% referral code & direct storefront QR."
        )
    )

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getActiveInboundRefCode(context: Context): String =
        (prefs(context).getString(KEY_INBOUND_REF, "") ?: "").trim().uppercase()

    fun setActiveInboundRefCode(context: Context, code: String): String {
        val clean = code.trim().uppercase().replace(Regex("[^A-Z0-9_-]"), "-")
        prefs(context).edit {
            if (clean.isBlank()) remove(KEY_INBOUND_REF) else putString(KEY_INBOUND_REF, clean)
        }
        return getActiveInboundRefCode(context)
    }

    fun applyInboundReferralDiscountCents(rawOneTimeCents: Long, refCode: String): Long {
        if (refCode.isBlank() || rawOneTimeCents <= 0L) return 0L
        return (rawOneTimeCents * 10L) / 100L
    }

    fun getOutboundShareCount(context: Context): Int =
        prefs(context).getInt(KEY_OUTBOUND_SHARES, 0)

    fun recordOutboundShare(context: Context): Int {
        val next = getOutboundShareCount(context) + 1
        prefs(context).edit { putInt(KEY_OUTBOUND_SHARES, next) }
        return next
    }

    fun getChecklistState(context: Context): Map<String, Boolean> {
        val p = prefs(context)
        return gbpChecklistItems.associate { it.id to p.getBoolean("$KEY_GBP_PREFIX${it.id}", false) }
    }

    fun toggleChecklistItem(context: Context, id: String): Map<String, Boolean> {
        val p = prefs(context)
        val curr = p.getBoolean("$KEY_GBP_PREFIX$id", false)
        p.edit { putBoolean("$KEY_GBP_PREFIX$id", !curr) }
        return getChecklistState(context)
    }

    fun evaluateQuickAudit(answers: Map<String, Int>): QuickAuditResult {
        var penalty = 0
        val matched = LinkedHashSet<String>()
        val flags = mutableListOf<String>()
        quizQuestions.forEach { q ->
            val idx = answers[q.id] ?: 0
            val opt = q.options.getOrElse(idx) { q.options.first() }
            penalty += opt.scorePenalty
            if (opt.scorePenalty > 0) flags.add(opt.label)
            matched.addAll(opt.skus)
        }
        val skus = matched.take(3).ifEmpty { listOf("SVC-SEC-12", "SVC-CMD-07") }
        return QuickAuditResult(
            score = (100 - penalty).coerceAtLeast(0),
            riskFlags = flags,
            recommendedSkus = skus,
            estAnnualLeakUsd = penalty * 18
        )
    }

    fun buildShareUrl(sku: String = "", ownRefCode: String = "TYR-843-REF", includeRef: Boolean = true): String {
        val params = mutableListOf<String>()
        if (sku.isNotBlank()) params.add("sku=${URLEncoder.encode(sku.trim(), "UTF-8")}")
        if (includeRef && ownRefCode.isNotBlank()) params.add("ref=${URLEncoder.encode(ownRefCode.trim(), "UTF-8")}")
        return if (params.isEmpty()) "https://tyrprotocol.pro/" else "https://tyrprotocol.pro/?${params.joinToString("&")}"
    }

    fun buildPitchText(
        channel: GrowthChannel,
        service: ServiceItem?,
        ownRefCode: String,
        includeRef: Boolean = true
    ): String {
        val sku = service?.sku.orEmpty()
        val title = service?.title ?: "Zero-Lock-In Websites, Domain Shield & Expense Audits"
        val price = service?.price ?: "$135–$295 One-Time"
        val link = buildShareUrl(sku, ownRefCode, includeRef)
        val refLine = if (includeRef) "Use referral code $ownRefCode for a 10% Welcome Credit." else ""

        return when (channel) {
            GrowthChannel.NEXTDOOR -> buildString {
                appendLine("Hey Northwest Alabama neighbors — if you're a local tradesperson, small business, or independent contractor tired of $30–$100/mo website builders or creeping vendor bills, I run TYR Technical Consulting (NAICS 541512 & 541611).")
                appendLine()
                appendLine("Featured offer: $title ($price) — zero recurring platform hostage fees and 100% master code ownership.")
                if (refLine.isNotBlank()) appendLine(refLine)
                appendLine()
                append("Inspect full transparent pricing & run a free 60s audit: $link or call/text (659) 257-8043.")
            }
            GrowthChannel.FACEBOOK -> buildString {
                appendLine("[Northwest Alabama & Remote] Hardened Technical & Admin Deliverables — Zero Monthly Hostage Fees")
                appendLine()
                appendLine("⚡ $title — $price")
                appendLine("✓ 100% client ownership of code & master files")
                appendLine("✓ Zero-Custody guarantee (0-day raw statement purge)")
                appendLine("✓ Optional 50% deposit / milestone split via Stripe")
                if (refLine.isNotBlank()) appendLine("✓ $refLine")
                appendLine()
                append("Tap to inspect specs or run a 60s self-audit: $link | 📞 (659) 257-8043")
            }
            GrowthChannel.EMAIL -> buildString {
                appendLine("Subject: TYR Technical Consulting — $title ($price)")
                appendLine()
                appendLine("Sharing a direct brief for $title ($price, NAICS 541512 / 541611). Every deliverable ships with 100% client master ownership, SHA-384 receipts, and zero mandatory recurring platform fees.")
                if (refLine.isNotBlank()) {
                    appendLine()
                    appendLine(refLine)
                }
                appendLine()
                append("Review scope & schedule: $link | Direct: (659) 257-8043 | tyr@tyrprotocol.pro")
            }
            GrowthChannel.SMS ->
                "TYR Technical Consulting (NW Alabama & Remote): $title is $price with $0/mo hosting lock-in. $refLine Specs & 60s audit: $link or call (659) 257-8043.".trim()
        }
    }

    fun buildPrintableHtml(
        mode: String,
        targetUrl: String,
        skuTitle: String,
        skuPrice: String,
        refCode: String,
        includeRef: Boolean
    ): String {
        val isCard = mode == "CARD"
        val qrSvg = GrowthQrEncoder.renderSvgString(targetUrl, if (isCard) 125 else 210)
        val offerTitle = skuTitle.ifBlank { "Zero-Lock-In Websites, Domain Shield & Expense Audits" }
        val offerPrice = if (skuPrice.isNotBlank()) " — $skuPrice" else ""
        val badge = if (includeRef) "REFERRAL CODE: $refCode (10% WELCOME CREDIT)" else "DIRECT CONSULTING BOOKING"

        return """
            <!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>TYR Printable</title>
            <style>
              body { font-family: sans-serif; color: #090a0f; margin: 0; padding: 18px; background: #fff; }
              .sheet { max-width: ${if (isCard) "420px" else "740px"}; margin: 0 auto; border: 2px solid #090a0f; border-radius: 10px; padding: ${if (isCard) "14px" else "26px"}; }
              .top { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #dc2626; padding-bottom: 8px; margin-bottom: 12px; }
              .brand { font-size: ${if (isCard) "14px" else "22px"}; font-weight: 900; }
              .sub { font-size: ${if (isCard) "10px" else "12px"}; color: #475569; font-weight: 600; }
              .grid { display: flex; justify-content: space-between; align-items: center; gap: 14px; }
              .offer { font-size: ${if (isCard) "13px" else "18px"}; font-weight: 800; color: #991b1b; margin-bottom: 6px; }
              ul { margin: 6px 0; padding-left: 16px; font-size: ${if (isCard) "10px" else "13px"}; line-height: 1.45; }
              .badge { display: inline-block; background: #090a0f; color: #fbbf24; font-family: monospace; font-size: ${if (isCard) "9px" else "11px"}; font-weight: 700; padding: 4px 8px; border-radius: 4px; margin-top: 6px; }
              .qr { text-align: center; border: 1px solid #cbd5e1; padding: 6px; border-radius: 6px; }
              .foot { border-top: 1px dashed #94a3b8; margin-top: 12px; padding-top: 6px; font-size: ${if (isCard) "9px" else "11px"}; display: flex; justify-content: space-between; font-weight: 700; }
            </style></head><body>
            <div class="sheet">
              <div class="top">
                <div><div class="brand">ᛏ TYR TECHNICAL CONSULTING</div><div class="sub">CALEB TR HOLCOMB d/b/a TYR • Northwest Alabama &amp; Remote</div></div>
                <div style="font-family:monospace;font-size:10px;font-weight:800;">NAICS 541512 / 541611</div>
              </div>
              <div class="grid">
                <div style="flex:1;">
                  <div class="offer">$offerTitle$offerPrice</div>
                  <ul>
                    <li><strong>$0/mo Lock-In:</strong> 100% master code &amp; file ownership.</li>
                    <li><strong>Zero-Custody:</strong> 0-Day statement purge &amp; SHA-384 receipts.</li>
                    <li><strong>Split Billing:</strong> 100%, 50% Deposit, or Milestone Stripe splits.</li>
                  </ul>
                  <div class="badge">$badge</div>
                </div>
                <div class="qr">$qrSvg<div style="font-family:monospace;font-size:8px;font-weight:700;margin-top:3px;">SCAN TO LAUNCH</div></div>
              </div>
              <div class="foot"><span>📞 (659) 257-8043</span><span>✉️ tyr@tyrprotocol.pro</span><span>🌐 tyrprotocol.pro</span></div>
            </div></body></html>
        """.trimIndent()
    }
}
