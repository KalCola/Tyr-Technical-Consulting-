# File: STRIPE_INTEGRATION_TODO.md
# Stripe Hosted Checkout Integration — Setup & Configuration Guide

This document is the single source of truth for completing your Hosted Stripe Checkout integration in the **TYR Protocol Storefront** (`pro.tyrprotocol.storefront`).

- **Stripe Account ID:** `acct_1Ub2VYRxlL3JK5Uq`
- **Checkout Studio Draft Plan ID:** `chkplan_61VkmYCmcY0V8AnN216VkQSuURSQVGsDGT0SpcV9c`
- **Checkout Studio Implementation URL:** `https://dashboard.stripe.com/acct_1Ub2VYRxlL3JK5Uq/checkout/checkouts/draft/chkplan_61VkmYCmcY0V8AnN216VkQSuURSQVGsDGT0SpcV9c?mode=implement`

---

## Values to Replace

The following values are `sample_only` placeholders and must be updated before going live.

**Files containing placeholders:**
- [app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt](app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt)
- [.env.example](.env.example)

### 1. `mode`
- **Current Value:** `"payment"` (automatically switches to `"subscription"` when recurring retainer SKUs are selected)
- **What to Set:** Set to `"payment"` for one-time charges or `"subscription"` for recurring billing in `StripePaymentManager.buildCheckoutSessionConfig(...)`. Note: `payment_method_collection` (`"always"`) is included only when `mode` is `"subscription"`.

### 2. `success_url`
- **Current Value:** `${BuildConfig.DOMAIN}/success?session_id={CHECKOUT_SESSION_ID}` (`https://tyrprotocol.pro/success?session_id={CHECKOUT_SESSION_ID}`)
- **What to Set:** Replace with your actual post-payment success page URL or deep link. Keep the `{CHECKOUT_SESSION_ID}` template variable intact so Stripe substitutes the completed Checkout Session ID.

### 3. `cancel_url`
- **Current Value:** `${BuildConfig.DOMAIN}` (`https://tyrprotocol.pro`)
- **What to Set:** Replace with your actual cancel/return page URL.

### 4. `line_items[].price`
- **Current Value:** `"price_..."` (quantity `1`)
- **What to Set:** Replace `"price_..."` in `StripePaymentManager.buildCheckoutSessionConfig(...)` with your actual Stripe Price IDs from the [Stripe Dashboard Prices page](https://dashboard.stripe.com/prices) or API corresponding to each TYR catalog SKU (`SVC-*`, `SUB-*`, `B2B-*`).

---

## Configured Parameters

These `fixed_by_ui` parameters were configured in Checkout Studio and are set in `StripePaymentManager.buildCheckoutSessionConfig(...)` and `StripePaymentManager.encodeCheckoutSessionFormParams(...)`.

**Files containing these parameters:**
- [app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt](app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt)

- **`ui_mode`**: `"hosted_page"`
  - *Version Note:* Stripe SDK versions `>= 21.0.0` (and current Stripe REST API versions) use `"hosted_page"`. If you proxy requests through a backend using a Stripe SDK version `< 21.0.0`, change `ui_mode` to `"hosted"`.
- **`billing_address_collection`**: `"auto"`
- **`phone_number_collection`**: `{ "enabled": true }` (`phone_number_collection[enabled]=true`)
- **`automatic_tax`**: `{ "enabled": true }` (`automatic_tax[enabled]=true`)
- **`allow_promotion_codes`**: `false`
- **`payment_method_collection`**: `"always"` *(included only when `mode` is `"subscription"`)*
- **`submit_type`**: `"auto"`
- **`tax_id_collection`**: `{ "enabled": true, "required": "never" }` (`tax_id_collection[enabled]=true&tax_id_collection[required]=never`)
- **`consent_collection`**: `{ "terms_of_service": "required" }` (`consent_collection[terms_of_service]=required`)
- **`saved_payment_method_options`**: `{ "payment_method_save": "enabled" }` (`saved_payment_method_options[payment_method_save]=enabled`)
- **`integration_identifier`**: `"hosted_mobile_app_0001"`
- **`origin_context`**: `"mobile_app"`

---

## Setup and Next Steps

### 1. Environment Variables & API Keys
Configure the following variables via the **Secrets panel in AI Studio** (defined in [`.env.example`](.env.example) and exposed via `BuildConfig`):
- `STRIPE_PUBLISHABLE_KEY`: Your Stripe Publishable Key (`pk_test_...` or `pk_live_...`) from `https://dashboard.stripe.com/test/apikeys`
- `STRIPE_SECRET_KEY`: Your Stripe Secret Key (`sk_test_...` or `sk_live_...`)
- `STRIPE_WEBHOOK_SECRET`: Your Stripe Webhook Signing Secret (`whsec_...`) from `https://dashboard.stripe.com/workbench/webhooks`
- `DOMAIN`: Your storefront domain (`https://tyrprotocol.pro`)

### 2. Project Structure
- [`.env.example`](.env.example): Defines `STRIPE_PUBLISHABLE_KEY`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `DOMAIN`, and `STRIPE_CHECKOUT_BASE_URL`.
- [`app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt`](app/src/main/java/com/example/tyrconsulting/model/StripePaymentModel.kt):
  - `StripeCheckoutSessionConfig`: Encapsulates all `fixed_by_ui` and `sample_only` Checkout Session parameters.
  - `StripePaymentManager.createCheckoutSession(...)`: Dispatches `POST https://api.stripe.com/v1/checkout/sessions` with the URL-encoded Checkout Studio parameters, parses `id` (`cs_...`), `url`, `customer`, and `subscription`, and persists them to the local transaction datastore (`tyr_stripe_transaction_vault`).
- [`app/src/main/java/com/example/tyrconsulting/ui/components/StripeCheckoutView.kt`](app/src/main/java/com/example/tyrconsulting/ui/components/StripeCheckoutView.kt):
  - `StripeCheckoutSheetDialog` & `StripeCheckoutAndReceiptsView`: Launches Hosted Stripe Checkout (`session.url`) and displays `SHA-384` signed Digital Receipts.

### 3. How the Integration Works (Flow Overview)
1. A client selects one or more catalog SKUs on any Service Card, in the Estimate Cart (Tab 10), or in the **Stripe & Receipts Hub (Tab 25)**.
2. `StripePaymentManager.buildCheckoutSessionConfig(...)` constructs the Checkout Session payload with all `fixed_by_ui` parameters (`ui_mode=hosted_page`, `billing_address_collection=auto`, `phone_number_collection[enabled]=true`, `automatic_tax[enabled]=true`, `allow_promotion_codes=false`, `submit_type=auto`, `tax_id_collection[enabled]=true`, `tax_id_collection[required]=never`, `consent_collection[terms_of_service]=required`, `saved_payment_method_options[payment_method_save]=enabled`, `integration_identifier=hosted_mobile_app_0001`, `origin_context=mobile_app`).
3. `StripePaymentManager.createCheckoutSession(...)` calls `POST https://api.stripe.com/v1/checkout/sessions` and redirects the customer to `session.url` (or records the settled receipt along with `checkoutSessionId`, `customerId`, and `subscriptionId` in `StripeTransactionRecord`).

### 4. Testing in Stripe Test Mode
Use standard Stripe test card numbers in test mode (`pk_test_...` / `sk_test_...`):
- **Visa (Success):** `4242 4242 4242 4242` (any future `MM/YY`, any 3-digit CVC, any 5-digit ZIP)
- **Mastercard (Success):** `5555 5555 5555 4444`
- **3D Secure Required:** `4000 0027 6000 3184`
- **Declined (Generic):** `4000 0000 0000 0002`

### 5. Next Steps
1. Create your production or test Products and Prices in the [Stripe Dashboard](https://dashboard.stripe.com/prices) and map each SKU to its `price_...` ID in `StripePaymentManager.buildCheckoutSessionConfig(...)`.
2. Verify Terms of Service URL settings in your Stripe Dashboard Public Details (`https://dashboard.stripe.com/settings/public`) since `consent_collection[terms_of_service]="required"` is enabled.
3. Configure Stripe Tax in your Stripe Dashboard (`https://dashboard.stripe.com/tax`) since `automatic_tax[enabled]=true` is enabled.

### 6. Resources
- **Stripe Support:** https://support.stripe.com
- **Stripe MCP & Documentation:** https://docs.stripe.com/mcp
