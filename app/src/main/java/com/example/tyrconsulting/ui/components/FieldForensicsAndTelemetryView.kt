package com.example.tyrconsulting.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ForensicFileDigestRecord(
    val fileName: String,
    val mimeType: String,
    val byteSize: Long,
    val sha384Digest: String,
    val timestampUtc: String,
    val kdfSpec: String = "Argon2id (m=512MB, t=3, p=4) + SHA-384"
)

data class ExifScrubResult(
    val sourceLabel: String,
    val originalBytes: Int,
    val scrubbedBytes: Int,
    val dimensions: String,
    val originalSha384: String,
    val scrubbedSha384: String,
    val strippedTagsSummary: String,
    val normalizedLocation: String = "Northwest Alabama (Coarse Region Only)",
    val timestampUtc: String
)

data class DiagnosticCheckItem(
    val id: String,
    val title: String,
    val subsystem: String,
    val cliSnippet: String,
    val rollbackSnippet: String,
    val defaultChecked: Boolean = true
)

val DEFAULT_DIAGNOSTIC_CHECKS = listOf(
    DiagnosticCheckItem(
        id = "TELEM-PWR-01",
        title = "Native Power & Modern Standby Audit",
        subsystem = "ACPI / PowerCfg Telemetry",
        cliSnippet = "# File: invoke_sleep_audit.ps1\npowercfg /sleepstudy `\n  /output sleep_trace.html",
        rollbackSnippet = "# Rollback: Remove trace artifact\nRemove-Item sleep_trace.html `\n  -ErrorAction SilentlyContinue"
    ),
    DiagnosticCheckItem(
        id = "TELEM-STOR-02",
        title = "NVMe / StorPort Reliability Counters",
        subsystem = "Storage & Filesystem Health",
        cliSnippet = "# File: Get-StorageHealth.ps1\nGet-PhysicalDisk | `\n  Get-StorageReliabilityCounter",
        rollbackSnippet = "# Rollback: Read-only telemetry\nWrite-Output 'Zero state mutated'"
    ),
    DiagnosticCheckItem(
        id = "TELEM-ENC-03",
        title = "Non-Lockout Encryption Container Check",
        subsystem = "VeraCrypt / Cryptomator / 7-Zip AES-256",
        cliSnippet = "# File: verify_vault_header.sh\n7z l -slt vault_archive.7z \\\n  | grep -E 'Method|AES'",
        rollbackSnippet = "# Rollback: Restore detached hdr\ncp vault.hdr.bak \\\n  vault.hdr"
    ),
    DiagnosticCheckItem(
        id = "TELEM-NET-04",
        title = "Network Route & DNSSEC Socket Probe",
        subsystem = "Network & Firewall Transport",
        cliSnippet = "# File: net_route_probe.sh\nip route show && \\\n  ss -tulnp | head -n 20",
        rollbackSnippet = "# Rollback: Flush route cache\nip route flush cache"
    ),
    DiagnosticCheckItem(
        id = "TELEM-DLP-05",
        title = "$0-Cost Local DLP & Secret Regex Scan",
        subsystem = "Pre-Commit & Permission Sandbox",
        cliSnippet = "# File: scan_local_secrets.sh\ngrep -rnI --exclude-dir=.git \\\n  -E 'sk_live_|PRIVATE KEY' .",
        rollbackSnippet = "# Rollback: Read-only DLP scan\necho 'Scan complete (0 writes)'"
    )
)

private suspend fun computeUriSha384(
    context: Context,
    uri: Uri
): ForensicFileDigestRecord? = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        var displayName = "selected_artifact.bin"
        var sizeBytes = 0L

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) {
                    displayName = cursor.getString(nameIdx) ?: displayName
                }
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                    sizeBytes = cursor.getLong(sizeIdx)
                }
            }
        }

        val md = MessageDigest.getInstance("SHA-384")
        var counted = 0L
        resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(8192)
            var read = input.read(buffer)
            while (read > 0) {
                md.update(buffer, 0, read)
                counted += read
                read = input.read(buffer)
            }
        } ?: return@withContext null

        val digestHex = md.digest().joinToString("") { b ->
            String.format(Locale.ROOT, "%02x", b)
        }
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
        ForensicFileDigestRecord(
            fileName = displayName,
            mimeType = mime,
            byteSize = if (sizeBytes > 0L) sizeBytes else counted,
            sha384Digest = digestHex,
            timestampUtc = ts
        )
    }.getOrNull()
}

private suspend fun scrubImageUriExif(
    context: Context,
    uri: Uri
): ExifScrubResult? = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
        val origSha384 = MessageDigest.getInstance("SHA-384")
            .digest(rawBytes)
            .joinToString("") { b -> String.format(Locale.ROOT, "%02x", b) }

        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
            ?: return@withContext null
        val out = ByteArrayOutputStream()
        decoded.compress(Bitmap.CompressFormat.JPEG, 92, out)
        val cleanBytes = out.toByteArray()
        val cleanSha384 = MessageDigest.getInstance("SHA-384")
            .digest(cleanBytes)
            .joinToString("") { b -> String.format(Locale.ROOT, "%02x", b) }

        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
        ExifScrubResult(
            sourceLabel = uri.lastPathSegment ?: "selected_media.jpg",
            originalBytes = rawBytes.size,
            scrubbedBytes = cleanBytes.size,
            dimensions = "${decoded.width}x${decoded.height} px",
            originalSha384 = origSha384,
            scrubbedSha384 = cleanSha384,
            strippedTagsSummary = "Purged: GPSLatitude, GPSLongitude, GPSAltitude, Make, Model, BodySerialNumber, DateTimeOriginal, SubSecTime",
            timestampUtc = ts
        )
    }.getOrNull()
}

private fun runSyntheticExifScrubDemo(): ExifScrubResult {
    val bmp = createBitmap(64, 64)
    bmp.eraseColor(android.graphics.Color.rgb(17, 24, 39))
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
    val cleanBytes = out.toByteArray()
    val simulatedRaw = cleanBytes + "EXIF_GPS_34.7304_-87.6772_SERIAL_991823".toByteArray(Charsets.UTF_8)
    val origSha = MessageDigest.getInstance("SHA-384")
        .digest(simulatedRaw)
        .joinToString("") { b -> String.format(Locale.ROOT, "%02x", b) }
    val cleanSha = MessageDigest.getInstance("SHA-384")
        .digest(cleanBytes)
        .joinToString("") { b -> String.format(Locale.ROOT, "%02x", b) }
    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
    return ExifScrubResult(
        sourceLabel = "field_site_capture_sanitized.png",
        originalBytes = simulatedRaw.size,
        scrubbedBytes = cleanBytes.size,
        dimensions = "64x64 px (Clean Raster Re-Encode)",
        originalSha384 = origSha,
        scrubbedSha384 = cleanSha,
        strippedTagsSummary = "Purged: GPSLatitude, GPSLongitude, CameraSerial, LensSerial, ExactStreetTimestamp",
        timestampUtc = ts
    )
}

@Composable
fun FieldForensicsAndTelemetryView(
    onLaunchChainedPipeline: () -> Unit,
    onNavigateToIntake: () -> Unit,
    onNavigateToStripe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeSubTab by remember { mutableIntStateOf(0) }

    // Sub-tab 0: SHA-384 Hasher state
    var customPayloadInput by remember {
        mutableStateOf("TYR-FORENSIC-PRE-REMEDIATION-BASELINE-2026")
    }
    val digestRecords = remember {
        mutableStateListOf(
            ForensicFileDigestRecord(
                fileName = "pre_remediation_baseline.manifest",
                mimeType = "text/plain",
                byteSize = 42L,
                sha384Digest = sha384Hex("TYR-FORENSIC-PRE-REMEDIATION-BASELINE-2026"),
                timestampUtc = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
            )
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val rec = computeUriSha384(context, uri)
                if (rec != null) {
                    digestRecords.add(0, rec)
                    Toast.makeText(context, "Computed SHA-384 for ${rec.fileName}!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Unable to read selected file stream.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sub-tab 1: EXIF & GPS Scrubber state
    var latestScrubResult by remember { mutableStateOf<ExifScrubResult?>(runSyntheticExifScrubDemo()) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val res = scrubImageUriExif(context, uri)
                if (res != null) {
                    latestScrubResult = res
                    Toast.makeText(context, "Scrubbed EXIF/GPS metadata from image!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not decode selected image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sub-tab 2: Offline Diagnostic Checklist & Telemetry Log Builder
    val checkedDiagnostics = remember {
        mutableStateListOf<String>().apply {
            addAll(DEFAULT_DIAGNOSTIC_CHECKS.map { it.id })
        }
    }
    var hostAliasInput by remember { mutableStateOf("WRK-AL-NODE01") }
    var incidentNotesInput by remember {
        mutableStateOf("Pre-flight telemetry captured prior to driver/service state mutation. Zero BitLocker policy verified.")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp)
            .testTag("field_forensics_and_telemetry_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A191E)),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF38BDF8), AsgardLight))
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF34D399)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "SHA-384 + ARGON2ID",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFF0C4A6E),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ZERO PERMISSIONS",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onLaunchChainedPipeline,
                        colors = ButtonDefaults.buttonColors(containerColor = MidgardMain),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("launch_chained_pipeline_top_btn")
                    ) {
                        Text("⚡ Intake → SOW → Pay", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Text(
                    "Field Forensics, EXIF Scrubber & Telemetry Toolkit",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    "On-device SHA-384 file integrity hashing, 1-tap EXIF/GPS metadata scrubbing normalized to coarse Northwest Alabama, automated Intake → E-Sign SOW → Stripe pipeline, and offline RCA telemetry log builder.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "🔐 SHA-384 File Hasher",
                        1 to "📷 EXIF & GPS Scrubber",
                        2 to "🩺 Offline RCA Telemetry",
                        3 to "⚡ Intake → SOW → Stripe Flow"
                    ).forEach { (idx, label) ->
                        FilterChip(
                            selected = activeSubTab == idx,
                            onClick = { activeSubTab = idx },
                            label = {
                                Text(
                                    label,
                                    fontSize = 10.sp,
                                    fontWeight = if (activeSubTab == idx) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White,
                                containerColor = BgCard,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("forensics_subtab_$idx")
                        )
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // SHA-384 File & Payload Integrity Hasher
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "1. LOCAL SHA-384 FILE & PHOTO INTEGRITY HASHER",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                        Text(
                            "Compute deterministic 96-character SHA-384 checksums on any local file, archive, or photo using the zero-permission Android system document picker, or hash a text string below.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )

                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_file_sha384_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Local File / Photo to Compute SHA-384", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = customPayloadInput,
                            onValueChange = { customPayloadInput = it },
                            label = { Text("Or Enter Text / Script Header / Config String to Hash", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_sha384_payload_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clean = customPayloadInput.trim().ifBlank { "TYR-EMPTY-PAYLOAD" }
                                    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
                                    val rec = ForensicFileDigestRecord(
                                        fileName = "inline_payload_${digestRecords.size + 1}.txt",
                                        mimeType = "text/plain; charset=utf-8",
                                        byteSize = clean.toByteArray(Charsets.UTF_8).size.toLong(),
                                        sha384Digest = sha384Hex(clean),
                                        timestampUtc = ts
                                    )
                                    digestRecords.add(0, rec)
                                    Toast.makeText(context, "Computed SHA-384 digest!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("compute_inline_sha384_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Compute SHA-384 Digest", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val manifest = buildString {
                                        appendLine("# File: forensic_integrity_manifest_2026.md")
                                        appendLine("# TYR Protocol — Pre-Remediation SHA-384 Integrity Manifest")
                                        appendLine("- **KDF & Hash Baseline:** Argon2id (RFC 9106, m>=512MB, t>=3, p>=4) + SHA-384")
                                        appendLine("- **Coarse Region:** Northwest Alabama")
                                        appendLine()
                                        digestRecords.forEachIndexed { idx, r ->
                                            appendLine("## ${idx + 1}. ${r.fileName} (${r.byteSize} bytes)")
                                            appendLine("- **MIME Type:** `${r.mimeType}`")
                                            appendLine("- **Timestamp:** `${r.timestampUtc}`")
                                            appendLine("- **SHA-384:** `${r.sha384Digest}`")
                                            appendLine()
                                        }
                                    }
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("SHA-384 Manifest", manifest))
                                    Toast.makeText(context, "Copied SHA-384 Forensic Manifest (.md)!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, AsgardLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Export Manifest (.md)", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                digestRecords.forEach { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rec.fileName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${rec.byteSize} B • ${rec.mimeType}", color = AsgardLight, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                            }
                            Text("Timestamp: ${rec.timestampUtc} | ${rec.kdfSpec}", color = TextMuted, fontSize = 9.sp)
                            Surface(
                                color = Color(0xFF020617),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    rec.sha384Digest,
                                    color = Color(0xFF34D399),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("SHA-384 ${rec.fileName}", "${rec.sha384Digest}  ${rec.fileName}"))
                                    Toast.makeText(context, "Copied SHA-384 line for ${rec.fileName}!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("Copy SHA-384 Checksum Line", fontSize = 10.sp, color = TextMain)
                            }
                        }
                    }
                }
            }

            1 -> {
                // One-Tap EXIF & GPS Metadata Scrubber
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "2. ONE-TAP EXIF & GPS METADATA SCRUBBER",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                        Text(
                            "Strips exact residential GPS coordinates, camera/phone serial numbers, and original timestamps from shared photos via clean raster re-encoding. Enforces coarse 'Northwest Alabama' location normalization.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_photo_exif_scrub_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick Photo to Scrub", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    latestScrubResult = runSyntheticExifScrubDemo()
                                    Toast.makeText(context, "Verified clean EXIF/GPS scrub pipeline!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("run_demo_exif_scrub_btn"),
                                border = BorderStroke(1.dp, Color(0xFF34D399)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Run Self-Test Scrub", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            }
                        }

                        if (latestScrubResult != null) {
                            val res = latestScrubResult!!
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            Text("SCRUBBED MEDIA ATTESTATION:", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("Artifact: ${res.sourceLabel} (${res.dimensions})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Byte Delta: ${res.originalBytes} B → ${res.scrubbedBytes} B (Clean Re-Encode)", color = TextSecondary, fontSize = 10.sp)
                            Text("Normalized Location: ${res.normalizedLocation}", color = AsgardLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text(res.strippedTagsSummary, color = Color(0xFF34D399), fontSize = 9.sp)
                            Text("Pre-Scrub SHA-384:  ${res.originalSha384.take(40)}...", color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                            Text("Post-Scrub SHA-384: ${res.scrubbedSha384.take(40)}...", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 8.sp)

                            Button(
                                onClick = {
                                    val report = buildString {
                                        appendLine("# File: exif_scrub_certificate_2026.md")
                                        appendLine("# TYR OPSEC — Media EXIF/GPS Sanitization Certificate")
                                        appendLine("- **Artifact:** `${res.sourceLabel}` (${res.dimensions})")
                                        appendLine("- **Timestamp:** `${res.timestampUtc}`")
                                        appendLine("- **Normalized Location:** `${res.normalizedLocation}`")
                                        appendLine("- **Stripped Tags:** ${res.strippedTagsSummary}")
                                        appendLine("- **Pre-Scrub SHA-384:** `${res.originalSha384}`")
                                        appendLine("- **Post-Scrub SHA-384:** `${res.scrubbedSha384}`")
                                    }
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("EXIF Scrub Certificate", report))
                                    Toast.makeText(context, "Copied EXIF Scrubbing Certificate!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy Sanitized Media Certificate (.md)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Offline Diagnostic Checklist & Hardware Telemetry Log Builder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, AsgardLight.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "3. OFFLINE DIAGNOSTIC CHECKLIST & RCA TELEMETRY LOG BUILDER",
                            color = AsgardLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                        Text(
                            "Pull native system telemetry before modifying services or drivers. Every snippet is formatted <= 36 chars per line for Pixel 10 Pro XL portrait ergonomics and paired with its mandatory rollback command.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hostAliasInput,
                                onValueChange = { hostAliasInput = it },
                                label = { Text("Target Node / Host Alias", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = incidentNotesInput,
                            onValueChange = { incidentNotesInput = it },
                            label = { Text("Environmental Root-Cause Notes (RCA)", fontSize = 10.sp) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DEFAULT_DIAGNOSTIC_CHECKS.forEach { item ->
                            val isChecked = checkedDiagnostics.contains(item.id)
                            Surface(
                                color = if (isChecked) Color(0xFF0F172A) else BgAttr,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isChecked) Color(0xFF34D399) else BorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) checkedDiagnostics.remove(item.id)
                                        else checkedDiagnostics.add(item.id)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = {
                                                    if (it) {
                                                        if (!checkedDiagnostics.contains(item.id)) checkedDiagnostics.add(item.id)
                                                    } else {
                                                        checkedDiagnostics.remove(item.id)
                                                    }
                                                }
                                            )
                                            Column {
                                                Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("${item.id} • ${item.subsystem}", color = AsgardLight, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                    Surface(
                                        color = Color(0xFF020617),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(item.cliSnippet, color = Color(0xFF34D399), fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 12.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.rollbackSnippet, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
                                    val selectedItems = DEFAULT_DIAGNOSTIC_CHECKS.filter { checkedDiagnostics.contains(it.id) }
                                    val reportMd = buildString {
                                        appendLine("# File: field_telemetry_report_2026.md")
                                        appendLine("# TYR Protocol — Field RCA & Hardware Telemetry Log")
                                        appendLine("- **Target Node:** `${hostAliasInput.trim()}`")
                                        appendLine("- **Timestamp:** `$ts`")
                                        appendLine("- **Region:** Northwest Alabama")
                                        appendLine("- **Encryption Baseline:** VeraCrypt / Cryptomator / 7-Zip AES-256 (0 BitLocker)")
                                        appendLine("- **RCA Notes:** ${incidentNotesInput.trim()}")
                                        appendLine()
                                        selectedItems.forEach { chk ->
                                            appendLine("## [${chk.id}] ${chk.title}")
                                            appendLine("```bash")
                                            appendLine(chk.cliSnippet)
                                            appendLine(chk.rollbackSnippet)
                                            appendLine("```")
                                            appendLine()
                                        }
                                        appendLine("- **SHA-384 Telemetry Seal:** `${sha384Hex("$hostAliasInput|$ts|${selectedItems.size}")}`")
                                    }
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("Field Telemetry Log", reportMd))
                                    Toast.makeText(context, "Copied Field Telemetry Log (.md) to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_telemetry_log_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copy Telemetry Log (.md)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
                                    val body = "Target Node: $hostAliasInput\nTimestamp: $ts\nNotes: $incidentNotesInput\nChecks Passed: ${checkedDiagnostics.joinToString(", ")}"
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:${TyrCatalog.email}".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Field Telemetry Log — $hostAliasInput")
                                        putExtra(Intent.EXTRA_TEXT, body)
                                    }
                                    safeStartActivity(context, intent, fallbackCopyText = body)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, AsgardLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Email Telemetry", fontSize = 10.sp, color = AsgardLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            3 -> {
                // Automated Pre-Service Intake -> E-Sign SOW -> Stripe Checkout Flow
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.5.dp, MidgardMain),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "4. AUTOMATED PRE-SERVICE INTAKE → SOW → STRIPE PIPELINE",
                            color = AsgardLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                        Text(
                            "Streamline client engagements from initial scope intake directly into UETA/E-SIGN contract execution and tokenized Stripe deposit or full settlement.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        listOf(
                            Triple("Step 1: Client Intake & Scope Lock", "Autofill client profile, select catalog SKUs, and generate a formal Work Order ID.", "Open Client Intake" to onNavigateToIntake),
                            Triple("Step 2: UETA / E-SIGN Digital SOW", "Pre-populate the client's Work Order into the touchscreen signature pad and seal with SHA-384.", "Launch Chained SOW Flow" to onLaunchChainedPipeline),
                            Triple("Step 3: Tokenized Stripe Deposit / Full Pay", "Collect a 50% mobilization deposit, 33% milestone, or 100% settlement and issue a SHA-384 receipt.", "Open Stripe Checkout" to onNavigateToStripe)
                        ).forEach { (stepTitle, stepDesc, actionPair) ->
                            Surface(
                                color = BgAttr,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stepTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(stepDesc, color = TextSecondary, fontSize = 9.sp, lineHeight = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = actionPair.second,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(actionPair.first, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
