package com.example.tyrconsulting.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

data class SavedClientProfile(
    val clientName: String = "",
    val businessName: String = "",
    val email: String = "",
    val phone: String = "",
    val domain: String = "",
    val preferredContact: String = "Email"
) {
    val hasData: Boolean
        get() = clientName.isNotBlank() || email.isNotBlank() || phone.isNotBlank()

    fun isEmpty(): Boolean = !hasData

    val fullName: String
        get() = clientName

    val organization: String
        get() = businessName

    val preferredChannel: String
        get() = preferredContact
}

object ClientQolManager {
    private const val PREFS_NAME = "tyr_client_qol_prefs"
    private const val KEY_FONT_SCALE = "font_scale_multiplier"
    private const val KEY_HIGH_CONTRAST = "high_contrast_enabled"
    private const val KEY_COMPACT_CARDS = "compact_cards_enabled"
    private const val KEY_STOREFRONT_MODE = "storefront_mode"
    private const val KEY_RECENT_TABS = "recent_tabs_csv"
    private const val KEY_FAV_SKUS = "favorite_skus"

    private const val KEY_PROF_NAME = "profile_client_name"
    private const val KEY_PROF_BIZ = "profile_business_name"
    private const val KEY_PROF_EMAIL = "profile_email"
    private const val KEY_PROF_PHONE = "profile_phone"
    private const val KEY_PROF_DOMAIN = "profile_domain"
    private const val KEY_PROF_CONTACT = "profile_preferred_contact"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getStorefrontMode(context: Context): String {
        val raw = prefs(context).getString(KEY_STOREFRONT_MODE, "PERSONAL") ?: "PERSONAL"
        return if (raw.equals("BUSINESS", ignoreCase = true)) "BUSINESS" else "PERSONAL"
    }

    fun setStorefrontMode(context: Context, mode: String) {
        val normalized = if (mode.equals("BUSINESS", ignoreCase = true)) "BUSINESS" else "PERSONAL"
        prefs(context).edit {
            putString(KEY_STOREFRONT_MODE, normalized)
        }
    }

    fun getFontScale(context: Context): Float =
        prefs(context).getFloat(KEY_FONT_SCALE, 1.0f).coerceIn(0.9f, 1.35f)

    fun setFontScale(context: Context, scale: Float) {
        prefs(context).edit {
            putFloat(KEY_FONT_SCALE, scale.coerceIn(0.9f, 1.35f))
        }
    }

    fun isHighContrast(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HIGH_CONTRAST, false)

    fun setHighContrast(context: Context, enabled: Boolean) {
        prefs(context).edit {
            putBoolean(KEY_HIGH_CONTRAST, enabled)
        }
    }

    fun isCompactCardMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_COMPACT_CARDS, false)

    fun setCompactCardMode(context: Context, enabled: Boolean) {
        prefs(context).edit {
            putBoolean(KEY_COMPACT_CARDS, enabled)
        }
    }

    fun getRecentTabs(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_RECENT_TABS, "0,1,25,26") ?: "0,1,25,26"
        return raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .distinct()
            .take(6)
    }

    fun recordRecentTab(context: Context, tabIndex: Int): List<Int> {
        val updated = (listOf(tabIndex) + getRecentTabs(context))
            .distinct()
            .take(6)
        prefs(context).edit {
            putString(KEY_RECENT_TABS, updated.joinToString(","))
        }
        return updated
    }

    fun getFavoriteSkus(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_FAV_SKUS, emptySet())?.toSet() ?: emptySet()

    fun toggleFavoriteSku(context: Context, sku: String): Set<String> {
        val current = getFavoriteSkus(context).toMutableSet()
        if (current.contains(sku)) {
            current.remove(sku)
        } else {
            current.add(sku)
        }
        prefs(context).edit {
            putStringSet(KEY_FAV_SKUS, current)
        }
        return current
    }

    fun getSavedProfile(context: Context): SavedClientProfile {
        val p = prefs(context)
        return SavedClientProfile(
            clientName = p.getString(KEY_PROF_NAME, "") ?: "",
            businessName = p.getString(KEY_PROF_BIZ, "") ?: "",
            email = p.getString(KEY_PROF_EMAIL, "") ?: "",
            phone = p.getString(KEY_PROF_PHONE, "") ?: "",
            domain = p.getString(KEY_PROF_DOMAIN, "") ?: "",
            preferredContact = p.getString(KEY_PROF_CONTACT, "Email") ?: "Email"
        )
    }

    fun saveProfile(context: Context, profile: SavedClientProfile) {
        prefs(context).edit {
            putString(KEY_PROF_NAME, profile.clientName.trim())
            putString(KEY_PROF_BIZ, profile.businessName.trim())
            putString(KEY_PROF_EMAIL, profile.email.trim())
            putString(KEY_PROF_PHONE, profile.phone.trim())
            putString(KEY_PROF_DOMAIN, profile.domain.trim())
            putString(KEY_PROF_CONTACT, profile.preferredContact.trim())
        }
    }

    fun clearProfile(context: Context) {
        prefs(context).edit {
            remove(KEY_PROF_NAME)
            remove(KEY_PROF_BIZ)
            remove(KEY_PROF_EMAIL)
            remove(KEY_PROF_PHONE)
            remove(KEY_PROF_DOMAIN)
            remove(KEY_PROF_CONTACT)
        }
    }
}

