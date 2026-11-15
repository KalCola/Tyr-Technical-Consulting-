package com.example.tyrconsulting.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class ReferralEventType {
    LINK_CLICKED,
    INTAKE_SUBMITTED,
    PURCHASE_COMPLETED
}

enum class ReferralLinkCategory(val id: String, val label: String, val badgeIcon: String) {
    FINTECH("FINTECH", "FinTech & Investing", "💰"),
    CREDIT("CREDIT", "Credit Monitoring", "🛡️"),
    CRYPTO("CRYPTO", "Crypto & Web3", "🪙"),
    SECURITY("SECURITY", "Security & Cloud", "🔒"),
    TYR_CONSULTING("TYR_CONSULTING", "TYR Storefront", "ᛏ")
}

data class PartnerReferralLink(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: ReferralLinkCategory,
    val icon: String,
    val referralUrl: String,
    val referralCode: String,
    val yourReward: String,
    val friendBonus: String,
    val description: String,
    val clicks: Int = 0,
    val isPinned: Boolean = false
)

data class ReferralEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: ReferralEventType,
    val title: String,
    val details: String,
    val timestamp: String,
    val bountyEarned: Double = 0.0
)

data class ReferralMetrics(
    val referralCode: String,
    val shareUrl: String,
    val totalClicks: Int,
    val totalIntakes: Int,
    val totalPurchased: Int,
    val totalRewardEarned: Double,
    val pendingReward: Double,
    val recentEvents: List<ReferralEvent> = emptyList()
) {
    val totalDidNotPurchase: Int
        get() = maxOf(0, totalClicks - totalPurchased)

    val conversionRatePercent: Double
        get() = if (totalClicks > 0) (totalPurchased.toDouble() / totalClicks.toDouble()) * 100.0 else 0.0

    val intakeRatePercent: Double
        get() = if (totalClicks > 0) (totalIntakes.toDouble() / totalClicks.toDouble()) * 100.0 else 0.0

    val tierName: String
        get() = when {
            totalPurchased >= 10 -> "ᚠ Thane of Fólkvangr (Tier 3)"
            totalPurchased >= 5 -> "ᛉ Shield-Bearer (Tier 2)"
            totalPurchased >= 1 -> "ᛏ Sovereign Scout (Tier 1)"
            else -> "Initiate Ambassador"
        }

    val bountyRate: String
        get() = "10%–50% Partner Reward & Referral Bonus Vault"
}

object ReferralManager {
    private const val PREFS_NAME = "tyr_referral_store"
    private const val KEY_CODE = "referral_code"
    private const val KEY_CLICKS = "referral_clicks"
    private const val KEY_INTAKES = "referral_intakes"
    private const val KEY_PURCHASES = "referral_purchases"
    private const val KEY_REWARDS = "referral_rewards"
    private const val KEY_PENDING = "referral_pending"
    private const val KEY_INIT = "referral_initialized"
    private const val KEY_PARTNER_LINKS_JSON = "partner_referral_links_v2"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun defaultPartnerLinks(tyrCode: String): List<PartnerReferralLink> = listOf(
        PartnerReferralLink(
            id = "sofi-invest",
            title = "SoFi Invest — Active & Automated Investing",
            category = ReferralLinkCategory.FINTECH,
            icon = "📈",
            referralUrl = "https://www.sofi.com/invite/invest?gcp=$tyrCode",
            referralCode = tyrCode,
            yourReward = "$75 in Stock",
            friendBonus = "$25 in Free Stock",
            description = "Zero-commission stocks, ETFs, and fractional shares with automated portfolio rebalancing. Customize with your personal SoFi Invest invite link.",
            clicks = 14,
            isPinned = true
        ),
        PartnerReferralLink(
            id = "sofi-banking",
            title = "SoFi Checking & High-Yield Vaults",
            category = ReferralLinkCategory.FINTECH,
            icon = "🏦",
            referralUrl = "https://www.sofi.com/invite/money?gcp=$tyrCode",
            referralCode = tyrCode,
            yourReward = "$75 Cash Bonus",
            friendBonus = "Up to $325 Welcome Bonus",
            description = "High-yield APY savings vaults with zero monthly account maintenance fees and early direct deposit.",
            clicks = 11,
            isPinned = true
        ),
        PartnerReferralLink(
            id = "credit-monitoring-shield",
            title = "Experian & 3-Bureau Credit Monitoring",
            category = ReferralLinkCategory.CREDIT,
            icon = "🛡️",
            referralUrl = "https://www.experian.com/consumer-products/credit-monitoring.html?ref=$tyrCode",
            referralCode = tyrCode,
            yourReward = "$30 Referral Credit",
            friendBonus = "Free FICO Score & Dark Web Alerts",
            description = "Real-time credit file freeze alerts, SSN dark-web surveillance, and instant inquiry notifications to block identity theft.",
            clicks = 9,
            isPinned = true
        ),
        PartnerReferralLink(
            id = "credit-builder-karma",
            title = "Credit Karma — Free Credit & Tax Filing",
            category = ReferralLinkCategory.CREDIT,
            icon = "📊",
            referralUrl = "https://www.creditkarma.com/?ref=$tyrCode",
            referralCode = tyrCode,
            yourReward = "Partner Bonus",
            friendBonus = "$0 Free Equifax & TransUnion Tracking",
            description = "Continuous two-bureau credit monitoring, credit utilization simulator, and unclaimed property lookup.",
            clicks = 7,
            isPinned = false
        ),
        PartnerReferralLink(
            id = "crypto-coinbase",
            title = "Coinbase — Digital Asset & Learning Rewards",
            category = ReferralLinkCategory.CRYPTO,
            icon = "🪙",
            referralUrl = "https://coinbase.com/join/$tyrCode",
            referralCode = tyrCode,
            yourReward = "$20 BTC/USDC Bonus",
            friendBonus = "Up to $200 Welcome Spin + Learning Rewards",
            description = "US-regulated digital asset exchange with hardware security key (YubiKey) support and instant USDC rewards.",
            clicks = 12,
            isPinned = true
        ),
        PartnerReferralLink(
            id = "crypto-ledger-vault",
            title = "Ledger Cold-Storage Hardware Vault",
            category = ReferralLinkCategory.CRYPTO,
            icon = "🔐",
            referralUrl = "https://shop.ledger.com/?r=$tyrCode",
            referralCode = tyrCode,
            yourReward = "$10 in BTC Credit",
            friendBonus = "$10 BTC Voucher with Hardware Wallet",
            description = "Air-gapped EAL6+ Secure Element hardware key storage so private keys never touch internet-connected devices.",
            clicks = 6,
            isPinned = false
        ),
        PartnerReferralLink(
            id = "security-proton",
            title = "Proton Unlimited — Zero-Access Mail, Drive & VPN",
            category = ReferralLinkCategory.SECURITY,
            icon = "🔒",
            referralUrl = "https://pr.tn/ref/$tyrCode",
            referralCode = tyrCode,
            yourReward = "$20 Hosting Credit",
            friendBonus = "30 Days Free Proton Unlimited",
            description = "Swiss end-to-end encrypted email, calendar, cloud drive, and password vault aligned with Zero-Custody operations.",
            clicks = 8,
            isPinned = false
        ),
        PartnerReferralLink(
            id = "tyr-storefront-partner",
            title = "TYR Protocol — Zero-Custody Web & Security Consulting",
            category = ReferralLinkCategory.TYR_CONSULTING,
            icon = "ᛏ",
            referralUrl = "https://tyrprotocol.pro/?ref=$tyrCode",
            referralCode = tyrCode,
            yourReward = "10%–50% Partner Bounty",
            friendBonus = "10% Priority Scope Discount",
            description = "Refer businesses or independent operators for $0/mo static websites, DMARC domain shields, B2B MSA suites, and expense audits.",
            clicks = 18,
            isPinned = true
        )
    )

    fun getPartnerLinks(context: Context): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        ensureInitialized(prefs)
        val code = prefs.getString(KEY_CODE, "TYR-843-REF") ?: "TYR-843-REF"
        val rawJson = prefs.getString(KEY_PARTNER_LINKS_JSON, null)
        if (rawJson.isNullOrBlank()) {
            val defaults = defaultPartnerLinks(code)
            savePartnerLinksList(prefs, defaults)
            return defaults
        }
        return try {
            val arr = JSONArray(rawJson)
            val list = mutableListOf<PartnerReferralLink>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val catStr = obj.optString("category", "FINTECH")
                val category = ReferralLinkCategory.entries.find { it.name == catStr }
                    ?: ReferralLinkCategory.FINTECH
                list.add(
                    PartnerReferralLink(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", "Partner Offer"),
                        category = category,
                        icon = obj.optString("icon", category.badgeIcon),
                        referralUrl = obj.optString("referralUrl", "https://tyrprotocol.pro"),
                        referralCode = obj.optString("referralCode", code),
                        yourReward = obj.optString("yourReward", "Partner Bonus"),
                        friendBonus = obj.optString("friendBonus", "Welcome Bonus"),
                        description = obj.optString("description", ""),
                        clicks = obj.optInt("clicks", 0),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            if (list.isEmpty()) defaultPartnerLinks(code) else list
        } catch (_: Exception) {
            defaultPartnerLinks(code)
        }
    }

    private fun savePartnerLinksList(prefs: SharedPreferences, links: List<PartnerReferralLink>) {
        val arr = JSONArray()
        links.forEach { link ->
            val obj = JSONObject().apply {
                put("id", link.id)
                put("title", link.title)
                put("category", link.category.name)
                put("icon", link.icon)
                put("referralUrl", link.referralUrl)
                put("referralCode", link.referralCode)
                put("yourReward", link.yourReward)
                put("friendBonus", link.friendBonus)
                put("description", link.description)
                put("clicks", link.clicks)
                put("isPinned", link.isPinned)
            }
            arr.put(obj)
        }
        prefs.edit {
            putString(KEY_PARTNER_LINKS_JSON, arr.toString())
        }
    }

    fun saveOrUpdatePartnerLink(context: Context, link: PartnerReferralLink): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        val current = getPartnerLinks(context).toMutableList()
        val index = current.indexOfFirst { it.id == link.id }
        if (index >= 0) {
            current[index] = link
        } else {
            current.add(0, link)
        }
        savePartnerLinksList(prefs, current)
        return current
    }

    fun deletePartnerLink(context: Context, linkId: String): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        val updated = getPartnerLinks(context).filterNot { it.id == linkId }
        savePartnerLinksList(prefs, updated)
        return updated
    }

    fun togglePinPartnerLink(context: Context, linkId: String): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        val updated = getPartnerLinks(context).map { link ->
            if (link.id == linkId) link.copy(isPinned = !link.isPinned) else link
        }
        savePartnerLinksList(prefs, updated)
        return updated
    }

    fun incrementPartnerLinkClick(context: Context, linkId: String): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        val updated = getPartnerLinks(context).map { link ->
            if (link.id == linkId) link.copy(clicks = link.clicks + 1) else link
        }
        savePartnerLinksList(prefs, updated)
        recordClick(context)
        return updated
    }

    fun resetPartnerLinksToDefaults(context: Context): List<PartnerReferralLink> {
        val prefs = getPrefs(context)
        val code = prefs.getString(KEY_CODE, "TYR-843-REF") ?: "TYR-843-REF"
        val defaults = defaultPartnerLinks(code)
        savePartnerLinksList(prefs, defaults)
        return defaults
    }

    fun getMetrics(context: Context): ReferralMetrics {
        val prefs = getPrefs(context)
        ensureInitialized(prefs)

        val code = prefs.getString(KEY_CODE, "TYR-843-REF") ?: "TYR-843-REF"
        val clicks = prefs.getInt(KEY_CLICKS, 18)
        val intakes = prefs.getInt(KEY_INTAKES, 6)
        val purchases = prefs.getInt(KEY_PURCHASES, 3)
        val rewards = prefs.getFloat(KEY_REWARDS, 125.0f).toDouble()
        val pending = prefs.getFloat(KEY_PENDING, 45.0f).toDouble()

        val shareUrl = "https://tyrprotocol.pro/?ref=$code"

        val events = listOf(
            ReferralEvent(
                type = ReferralEventType.PURCHASE_COMPLETED,
                title = "SoFi Invest & TYR Bundle Referral Verified",
                details = "Partner code $code • Bonus & Bounty Attributed",
                timestamp = "Today, 11:24 AM",
                bountyEarned = 24.50
            ),
            ReferralEvent(
                type = ReferralEventType.INTAKE_SUBMITTED,
                title = "New Client Intake: Asgard Domain Shield",
                details = "Commercial client verified via your referral link",
                timestamp = "Yesterday, 3:45 PM",
                bountyEarned = 0.0
            ),
            ReferralEvent(
                type = ReferralEventType.PURCHASE_COMPLETED,
                title = "Contract Signed: B2B Onboarding Suite",
                details = "Commercial firm executed engagement under $code",
                timestamp = "Nov 1, 9:15 AM",
                bountyEarned = 29.50
            ),
            ReferralEvent(
                type = ReferralEventType.LINK_CLICKED,
                title = "Partner Referral Vault Link Opened",
                details = "Visitor clicked from SMS / Web share",
                timestamp = "Oct 31, 6:12 PM",
                bountyEarned = 0.0
            )
        )

        return ReferralMetrics(
            referralCode = code,
            shareUrl = shareUrl,
            totalClicks = clicks,
            totalIntakes = intakes,
            totalPurchased = purchases,
            totalRewardEarned = rewards,
            pendingReward = pending,
            recentEvents = events
        )
    }

    private fun ensureInitialized(prefs: SharedPreferences) {
        if (!prefs.getBoolean(KEY_INIT, false)) {
            prefs.edit {
                putBoolean(KEY_INIT, true)
                putString(KEY_CODE, "TYR-843-REF")
                putInt(KEY_CLICKS, 18)
                putInt(KEY_INTAKES, 6)
                putInt(KEY_PURCHASES, 3)
                putFloat(KEY_REWARDS, 125.0f)
                putFloat(KEY_PENDING, 45.0f)
            }
        }
    }

    fun recordClick(context: Context): ReferralMetrics {
        val prefs = getPrefs(context)
        val currentClicks = prefs.getInt(KEY_CLICKS, 0)
        prefs.edit {
            putInt(KEY_CLICKS, currentClicks + 1)
        }
        return getMetrics(context)
    }

    fun recordIntake(context: Context): ReferralMetrics {
        val prefs = getPrefs(context)
        val currentIntakes = prefs.getInt(KEY_INTAKES, 0)
        val currentClicks = prefs.getInt(KEY_CLICKS, 0)
        prefs.edit {
            putInt(KEY_CLICKS, currentClicks + 1)
            putInt(KEY_INTAKES, currentIntakes + 1)
        }
        return getMetrics(context)
    }

    fun recordPurchase(context: Context, bountyEarned: Double = 35.0): ReferralMetrics {
        val prefs = getPrefs(context)
        val currentPurchases = prefs.getInt(KEY_PURCHASES, 0)
        val currentClicks = prefs.getInt(KEY_CLICKS, 0)
        val currentIntakes = prefs.getInt(KEY_INTAKES, 0)
        val currentRewards = prefs.getFloat(KEY_REWARDS, 0.0f)

        prefs.edit {
            putInt(KEY_CLICKS, currentClicks + 1)
            putInt(KEY_INTAKES, currentIntakes + 1)
            putInt(KEY_PURCHASES, currentPurchases + 1)
            putFloat(KEY_REWARDS, currentRewards + bountyEarned.toFloat())
        }

        return getMetrics(context)
    }

    fun updateReferralCode(context: Context, newCode: String): ReferralMetrics {
        val cleanCode = newCode.trim().uppercase(java.util.Locale.ROOT).replace(" ", "-")
        getPrefs(context).edit {
            putString(KEY_CODE, cleanCode)
        }
        return getMetrics(context)
    }

    fun resetMetrics(context: Context): ReferralMetrics {
        getPrefs(context).edit {
            putInt(KEY_CLICKS, 0)
            putInt(KEY_INTAKES, 0)
            putInt(KEY_PURCHASES, 0)
            putFloat(KEY_REWARDS, 0.0f)
            putFloat(KEY_PENDING, 0.0f)
        }
        return getMetrics(context)
    }
}
