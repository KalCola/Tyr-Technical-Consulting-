package com.example.tyrconsulting.ui.components

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

/**
 * Custom FlowRow layout built on standard Compose Layout primitives.
 */
@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = horizontalSpacing.roundToPx()
        val vGapPx = verticalSpacing.roundToPx()
        val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE

        val rows = mutableListOf<List<Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        for (measurable in measurables) {
            val placeable = measurable.measure(childConstraints)
            val neededWidth = if (currentRow.isEmpty()) placeable.width else currentRowWidth + hGapPx + placeable.width
            if (neededWidth > maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
                currentRowHeight = placeable.height
            } else {
                currentRow.add(placeable)
                currentRowWidth = neededWidth
                currentRowHeight = maxOf(currentRowHeight, placeable.height)
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val totalWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            (rowWidths.maxOrNull() ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
        }
        val totalHeight = if (rows.isEmpty()) {
            constraints.minHeight
        } else {
            (rowHeights.sum() + vGapPx * (rows.size - 1)).coerceIn(constraints.minHeight, constraints.maxHeight)
        }

        layout(totalWidth, totalHeight) {
            var yPosition = 0
            for (i in rows.indices) {
                var xPosition = 0
                val row = rows[i]
                val rowHeight = rowHeights[i]
                for (placeable in row) {
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    xPosition += placeable.width + hGapPx
                }
                yPosition += rowHeight + vGapPx
            }
        }
    }
}

private val ALLOWED_EXTERNAL_SCHEMES = setOf("https", "mailto", "tel", "smsto", "sms")

/**
 * Safely launches an external Activity Intent after enforcing OWASP Mobile intent security controls:
 * 1. Strips explicit component/selector to prevent internal component hijacking.
 * 2. Strips URI permission grant flags so internal app data is never leaked to external apps.
 * 3. Enforces a strict URI scheme allowlist (https, mailto, tel, smsto, sms) when data URI is present.
 * 4. Catches ActivityNotFoundException and copies fallbackCopyText to the clipboard if no handler is installed.
 */
fun safeStartActivity(
    context: Context,
    intent: Intent,
    fallbackCopyText: String? = null
) {
    try {
        val dataUri = intent.data
        if (dataUri != null) {
            val scheme = dataUri.scheme?.lowercase( java.util.Locale.ROOT) ?: ""
            if (scheme !in ALLOWED_EXTERNAL_SCHEMES) {
                Toast.makeText(
                    context,
                    "Blocked untrusted external URI scheme ($scheme).",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (scheme == "https" && intent.action == Intent.ACTION_VIEW) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
            }
        }
        intent.component = null
        intent.selector = null
        val forbiddenFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        intent.flags = intent.flags and forbiddenFlags.inv()
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        if (!fallbackCopyText.isNullOrBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("TYR Protocol", fallbackCopyText))
            Toast.makeText(
                context,
                "No app found to handle action; copied details to clipboard.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "No compatible application installed for this action.",
                Toast.LENGTH_SHORT
            ).show()
        }
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Unable to launch external application.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * Computes a deterministic 96-character lowercase hexadecimal SHA-384 cryptographic digest.
 */
fun sha384Hex(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-384").digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

/**
 * Escapes HTML special characters to neutralize injection in rich text / CardV2 payloads.
 */
fun escapeHtmlEntities(raw: String): String {
    return raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
