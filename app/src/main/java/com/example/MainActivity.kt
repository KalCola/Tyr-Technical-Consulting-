package com.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.tyrconsulting.ui.StorefrontApp
import com.example.tyrconsulting.ui.theme.TYRTechnicalConsultingTheme

class MainActivity : ComponentActivity() {
    private var incomingDeepLinkUri by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)
        incomingDeepLinkUri = intent?.dataString

        // OWASP Mobile M1 / Tapjacking defense: reject touches when another window overlays the app
        window.decorView.filterTouchesWhenObscured = true

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TYRTechnicalConsultingTheme {
                StorefrontApp(initialDeepLinkUri = incomingDeepLinkUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingDeepLinkUri = intent.dataString
    }
}
