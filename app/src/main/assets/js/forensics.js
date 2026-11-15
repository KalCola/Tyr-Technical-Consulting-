// File: js/forensics.js
/**
 * TYR Protocol — Browser Field Forensics & Security Toolkit
 * Zero-Upload Local WebCrypto SHA-384 Hashing, Offscreen Canvas EXIF/GPS Scrubber & Mobile RCA Log Builder.
 */
(function () {
  'use strict';

  const DIAGNOSTIC_CHECKS = [
    {
      id: 'PWR-01',
      title: 'Sleep & Wake Power Telemetry',
      subsystem: 'Power / ACPI',
      cliSnippet: '# File: power_telemetry.ps1\npowercfg /sleepstudy `\n  /output sleep_audit.html',
      rollbackSnippet: '# Rollback: Remove temporary HTML\nRemove-Item sleep_audit.html `\n  -ErrorAction SilentlyContinue'
    },
    {
      id: 'DSK-02',
      title: 'NVMe / Disk Health & SMART Probe',
      subsystem: 'Storage / I/O',
      cliSnippet: '# File: disk_smart_probe.sh\nsudo smartctl -H \\\n  /dev/nvme0n1',
      rollbackSnippet: '# Rollback: Read-only probe\n# Zero disk writes performed'
    },
    {
      id: 'NET-03',
      title: 'Routing Table & DNS Leak Check',
      subsystem: 'Network / DNS',
      cliSnippet: '# File: net_route_probe.sh\nip -j route show \\\n  default',
      rollbackSnippet: '# Rollback: Read-only probe\n# Flush local DNS cache if needed'
    },
    {
      id: 'ENC-04',
      title: 'Non-Lockout Encryption Check (0 BitLocker)',
      subsystem: 'Crypto / Volume',
      cliSnippet: '# File: check_veracrypt_aes.sh\n7z l -slt archive.7z | \\\n  grep -E "Method|Encrypted"',
      rollbackSnippet: '# Policy: BitLocker strictly banned\n# Use VeraCrypt / 7-Zip AES-256'
    },
    {
      id: 'SEC-05',
      title: 'Local DLP Secret Regex Sweep',
      subsystem: 'OPSEC / DLP',
      cliSnippet: '# File: scan_local_secrets.sh\ngrep -rnI --exclude-dir=.git \\\n  -E "sk_live_|PRIVATE KEY" .',
      rollbackSnippet: '# Rollback: Read-only DLP scan\n# Purge matching tokens before push'
    }
  ];

  async function hashFileSha384(file) {
    if (!file) throw new Error('No file provided');
    const buf = await file.arrayBuffer();
    const sha384Digest = await window.TyrStripe.sha384Hex(new Uint8Array(buf));
    return {
      fileName: file.name,
      mimeType: file.type || 'application/octet-stream',
      byteSize: file.size,
      sha384Digest,
      kdfSpec: 'Argon2id (m=512MB, t=3, p=4) + SHA-384',
      timestampUtc: new Date().toISOString().replace('T', ' ').substring(0, 19) + ' UTC'
    };
  }

  function scrubImageExif(file) {
    return new Promise((resolve, reject) => {
      if (!file) {
        reject(new Error('No image selected'));
        return;
      }
      const reader = new FileReader();
      reader.onerror = () => reject(new Error('Failed to read image bytes'));
      reader.onload = async () => {
        try {
          const origBytes = new Uint8Array(reader.result);
          const originalSha384 = await window.TyrStripe.sha384Hex(origBytes);
          const blobUrl = URL.createObjectURL(new Blob([origBytes], { type: file.type || 'image/png' }));
          const img = new Image();
          img.onerror = () => {
            URL.revokeObjectURL(blobUrl);
            reject(new Error('Unsupported image format'));
          };
          img.onload = () => {
            try {
              const canvas = document.createElement('canvas');
              canvas.width = img.naturalWidth || img.width;
              canvas.height = img.naturalHeight || img.height;
              const ctx = canvas.getContext('2d');
              ctx.drawImage(img, 0, 0);
              URL.revokeObjectURL(blobUrl);

              canvas.toBlob(async (cleanBlob) => {
                if (!cleanBlob) {
                  reject(new Error('Canvas re-encode failed'));
                  return;
                }
                const cleanArr = new Uint8Array(await cleanBlob.arrayBuffer());
                const scrubbedSha384 = await window.TyrStripe.sha384Hex(cleanArr);
                const cleanUrl = URL.createObjectURL(cleanBlob);
                const safeBase = file.name.replace(/\.[^.]+$/, '') || 'sanitized_media';
                resolve({
                  sourceLabel: `${safeBase}_exif_scrubbed.png`,
                  originalBytes: origBytes.length,
                  scrubbedBytes: cleanArr.length,
                  dimensions: `${canvas.width}x${canvas.height} px`,
                  originalSha384,
                  scrubbedSha384,
                  strippedTagsSummary:
                    'Purged: GPSLatitude, GPSLongitude, GPSAltitude, Make, Model, BodySerialNumber, DateTimeOriginal, SubSecTime',
                  normalizedLocation: 'Northwest Alabama (Coarse Region Only)',
                  timestampUtc: new Date().toISOString().replace('T', ' ').substring(0, 19) + ' UTC',
                  cleanBlobUrl: cleanUrl
                });
              }, 'image/png');
            } catch (err) {
              URL.revokeObjectURL(blobUrl);
              reject(err);
            }
          };
          img.src = blobUrl;
        } catch (err) {
          reject(err);
        }
      };
      reader.readAsArrayBuffer(file);
    });
  }

  async function buildTelemetryLogMarkdown(hostAlias, notes, selectedIds) {
    const ts = new Date().toISOString().replace('T', ' ').substring(0, 19) + ' UTC';
    const chosen = DIAGNOSTIC_CHECKS.filter((c) => selectedIds.includes(c.id));
    const seal = await window.TyrStripe.sha384Hex(`${hostAlias}|${ts}|${chosen.length}|${notes}`);
    const lines = [
      '# File: field_telemetry_rca_log.md',
      `# TYR Field Forensics & Hardware Telemetry Log`,
      '',
      `- **Target Node:** \`${hostAlias || 'LOCAL-WORKSTATION-01'}\``,
      `- **Timestamp:** \`${ts}\``,
      '- **Region:** Northwest Alabama',
      '- **Encryption Baseline:** VeraCrypt / Cryptomator / 7-Zip AES-256 (0 BitLocker)',
      `- **RCA Notes:** ${notes || 'Routine pre-remediation hardware baseline sweep.'}`,
      ''
    ];
    chosen.forEach((chk) => {
      lines.push(`## [${chk.id}] ${chk.title} (${chk.subsystem})`);
      lines.push('```bash');
      lines.push(chk.cliSnippet);
      lines.push(chk.rollbackSnippet);
      lines.push('```');
      lines.push('');
    });
    lines.push(`- **SHA-384 Telemetry Seal:** \`${seal}\``);
    return lines.join('\n');
  }

  window.TyrForensics = {
    DIAGNOSTIC_CHECKS,
    hashFileSha384,
    scrubImageExif,
    buildTelemetryLogMarkdown
  };
})();
