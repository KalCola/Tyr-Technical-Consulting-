package com.example

import com.example.tyrconsulting.model.ReferralLinkCategory
import com.example.tyrconsulting.model.SavedClientProfile
import com.example.tyrconsulting.model.StripePaymentManager
import com.example.tyrconsulting.model.StripeTransactionRecord
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.components.ZeroCustodyScrubber
import com.example.tyrconsulting.ui.components.escapeHtmlEntities
import com.example.tyrconsulting.ui.components.sha384Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify sha384Hex produces 96-character SHA-384 digest`() {
        val digest = sha384Hex("TYR-PROTOCOL-ZERO-CUSTODY-AUDIT")
        assertEquals(96, digest.length)
        assertTrue(digest.matches(Regex("^[0-9a-f]{96}$")))
    }

    @Test
    fun `verify ZeroCustodyScrubber redacts sensitive financial and credential tokens`() {
        val rawInput = "Card 4532-1111-2222-3333 SSN 123-45-6789 API AIzaSyTestToken12345678901234567890123"
        val result = ZeroCustodyScrubber.scrub(rawInput)
        assertFalse(result.sanitizedText.contains("4532-1111-2222-3333"))
        assertFalse(result.sanitizedText.contains("123-45-6789"))
        assertTrue(result.totalRedactions >= 2)
    }

    @Test
    fun `verify TyrCatalog services have valid SKUs and OPSEC compliance`() {
        val services = TyrCatalog.services
        assertTrue(services.isNotEmpty())
        assertEquals("Northwest Alabama & Nationwide Remote", TyrCatalog.location)
        assertEquals("42-XXX-X843 (CP 575 G)", TyrCatalog.ein)
        services.forEach { item ->
            assertTrue(
                item.sku.startsWith("SVC-") ||
                    item.sku.startsWith("SUB-") ||
                    item.sku.startsWith("B2B-")
            )
            assertTrue(item.title.isNotBlank())
            assertTrue(item.price.startsWith("$"))
        }
    }

    @Test
    fun `verify escapeHtmlEntities neutralizes script injection vectors`() {
        val untrusted = "<script>alert('xss')</script> & \"quotes\""
        val escaped = escapeHtmlEntities(untrusted)
        assertFalse(escaped.contains("<script>"))
        assertTrue(escaped.contains("&lt;script&gt;"))
    }

    @Test
    fun `verify SavedClientProfile and ReferralLinkCategory coverage`() {
        val profile = SavedClientProfile(
            clientName = "Caleb Holcomb",
            businessName = "TYR Protocol",
            email = "tyr@tyrprotocol.pro",
            phone = "(659) 257-8043",
            domain = "https://tyrprotocol.pro"
        )
        assertTrue(profile.hasData)
        assertFalse(profile.isEmpty())
        assertEquals(5, ReferralLinkCategory.values().size)
    }

    @Test
    fun `verify StripePaymentManager pricing and SHA-384 digital receipt ledger`() {
        val webSvc = TyrCatalog.services.first { it.sku == "SVC-WEB-01" }
        assertEquals(24500L, StripePaymentManager.parseServicePriceToCents(webSvc.price))
        assertFalse(StripePaymentManager.isMonthlyRecurringService(webSvc))

        val oneTimeConfig = StripePaymentManager.buildCheckoutSessionConfig(listOf(webSvc))
        assertEquals("hosted_page", oneTimeConfig.uiMode)
        assertEquals("payment", oneTimeConfig.mode)
        assertEquals("auto", oneTimeConfig.billingAddressCollection)
        assertTrue(oneTimeConfig.phoneNumberCollectionEnabled)
        assertTrue(oneTimeConfig.automaticTaxEnabled)
        assertFalse(oneTimeConfig.allowPromotionCodes)
        assertEquals(null, oneTimeConfig.paymentMethodCollection)
        assertEquals("auto", oneTimeConfig.submitType)
        assertTrue(oneTimeConfig.taxIdCollectionEnabled)
        assertEquals("never", oneTimeConfig.taxIdCollectionRequired)
        assertEquals("required", oneTimeConfig.consentTermsOfService)
        assertEquals("enabled", oneTimeConfig.savedPaymentMethodSave)
        assertEquals("hosted_mobile_app_0001", oneTimeConfig.integrationIdentifier)
        assertEquals("mobile_app", oneTimeConfig.originContext)

        val encodedOneTime = StripePaymentManager.encodeCheckoutSessionFormParams(oneTimeConfig)
        assertTrue(encodedOneTime.contains("ui_mode=hosted_page"))
        assertFalse(encodedOneTime.contains("payment_method_collection="))

        val subSvc = TyrCatalog.services.first { it.sku == "SUB-WEB-01" }
        val subConfig = StripePaymentManager.buildCheckoutSessionConfig(listOf(subSvc))
        assertEquals("subscription", subConfig.mode)
        assertEquals("always", subConfig.paymentMethodCollection)
        val encodedSub = StripePaymentManager.encodeCheckoutSessionFormParams(subConfig)
        assertTrue(encodedSub.contains("payment_method_collection=always"))

        val liveEncodedOneTime = StripePaymentManager.encodeLiveCatalogCheckoutFormParams(
            config = oneTimeConfig,
            services = listOf(webSvc),
            clientEmail = "client@tyrprotocol.pro"
        )
        assertTrue(liveEncodedOneTime.contains("unit_amount%5D=24500"))
        assertTrue(liveEncodedOneTime.contains("SVC-WEB-01"))

        val liveEncodedSub = StripePaymentManager.encodeLiveCatalogCheckoutFormParams(
            config = subConfig,
            services = listOf(subSvc),
            clientEmail = "client@tyrprotocol.pro"
        )
        assertTrue(liveEncodedSub.contains("recurring%5D%5Binterval%5D=month"))

        val hash = StripeTransactionRecord.computeCanonicalHash(
            transactionId = "pi_3TYRTEST",
            receiptNumber = "RCPT-20261113-ABCD",
            timestampIso = "2026-11-13 09:35:00 UTC",
            clientEmail = "client@tyrprotocol.pro",
            skus = listOf("SVC-WEB-01"),
            totalChargedTodayCents = 24500L,
            maskedInstrument = "Card •••• 4242",
            stripeTokenId = "tok_card_12345"
        )
        assertEquals(96, hash.length)

        // Verify 50% Deposit Split-Payment Calculation ($245.00 -> 12250 cents)
        val depositEncoded = StripePaymentManager.encodeLiveCatalogCheckoutFormParams(
            config = oneTimeConfig,
            services = listOf(webSvc),
            clientEmail = "client@tyrprotocol.pro",
            splitPercent = 50
        )
        assertTrue(depositEncoded.contains("unit_amount%5D=12250"))

        // Verify IRS Schedule C CSV Export
        val sampleRecord = StripeTransactionRecord(
            transactionId = "pi_3TYRTEST",
            checkoutSessionId = "cs_test_3TYRTEST",
            receiptNumber = "RCPT-20261113-ABCD",
            timestampIso = "2026-11-13 09:35:00 UTC",
            clientName = "Caleb Holcomb",
            businessName = "TYR Protocol",
            clientEmail = "client@tyrprotocol.pro",
            skus = listOf("SVC-WEB-01"),
            itemTitles = listOf("Midgard Web Presence [50% Split/Deposit]"),
            oneTimeAmountCents = 12250L,
            monthlyRecurringCents = 0L,
            totalChargedTodayCents = 12250L,
            paymentMethodLabel = "Credit / Debit Card (Stripe PCI Vault)",
            maskedInstrument = "Card •••• 4242",
            stripeTokenId = "tok_card_12345",
            gatewayMode = "Stripe Checkout Live API",
            sha384IntegrityHash = hash
        )
        val csvExport = StripePaymentManager.exportTransactionAuditLedgerCsv(listOf(sampleRecord))
        assertTrue(csvExport.contains("receipt_number,stripe_transaction_id,checkout_session_id,timestamp_utc"))
        assertTrue(csvExport.contains("\"RCPT-20261113-ABCD\",\"pi_3TYRTEST\""))
        assertTrue(csvExport.contains("122.50"))
    }
}





