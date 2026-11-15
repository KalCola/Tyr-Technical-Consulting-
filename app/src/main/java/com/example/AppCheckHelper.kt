package com.example

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        val app: FirebaseApp? = if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseApp.getInstance()
        } else {
            FirebaseApp.initializeApp(context)
        }

        if (app == null) {
            Log.d(TAG, "FirebaseApp is not configured; skipping App Check installation.")
            return
        }

        val appCheck = FirebaseAppCheck.getInstance(app)

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = app.persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit {
                    putString(DEBUG_SECRET_KEY, debugToken)
                }
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.d(TAG, "Using default DebugAppCheckProviderFactory instance.")
            }

            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val siteKey = context.getString(R.string.recaptcha_site_key)
            if (siteKey.isNotBlank()) {
                appCheck.installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                )
                Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
            }
        }
    } catch (e: Exception) {
        Log.d(TAG, "Firebase App Check initialization note: ${e.message}")
    }
}
