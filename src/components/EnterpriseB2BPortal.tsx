import React, { useState } from 'react';
import {
  Building2,
  Shield,
  FileText,
  Calendar,
  CheckCircle2,
  Copy,
  Download,
  ExternalLink,
  Cpu,
  Layers,
  ArrowRight,
  Terminal,
  Lock,
  Flame,
  Check,
  Briefcase,
  Clock,
  Sparkles,
  Radio
} from 'lucide-react';
import {
  ENTERPRISE_ENTITY,
  ENTERPRISE_TRACKS,
  EnterpriseTrack
} from '../data/enterpriseCatalog';

interface EnterpriseB2BPortalProps {
  onOpenIntakeWithTrack: (track: EnterpriseTrack) => void;
  onOpenBooking: (trackCode: string) => void;
  onOpenSignModal?: (track: EnterpriseTrack) => void;
  onOpenDispatch?: (trackCode: string) => void;
  onSwitchToStorefront: () => void;
}

export default function EnterpriseB2BPortal({
  onOpenIntakeWithTrack,
  onOpenBooking,
  onOpenSignModal,
  onOpenDispatch,
  onSwitchToStorefront
}: EnterpriseB2BPortalProps) {
  const [selectedTrackId, setSelectedTrackId] = useState<string>('ai-eval');
  const [copiedPitch, setCopiedPitch] = useState<boolean>(false);
  const [copiedEin, setCopiedEin] = useState<boolean>(false);

  const selectedTrack =
    ENTERPRISE_TRACKS.find((t) => t.id === selectedTrackId) ||
    ENTERPRISE_TRACKS[0];

  const handleCopyPitch = () => {
    navigator.clipboard.writeText(selectedTrack.pitchNote);
    setCopiedPitch(true);
    setTimeout(() => setCopiedPitch(false), 2500);
  };

  const handleCopyEin = () => {
    navigator.clipboard.writeText(ENTERPRISE_ENTITY.ein);
    setCopiedEin(true);
    setTimeout(() => setCopiedEin(false), 2000);
  };

  const handleDownloadDossier = () => {
    const content = `MASTER STATEMENT OF WORK & ONBOARDING CALIBRATION DOSSIER
================================================================================
1. ENTERPRISE COMPLIANCE & ENTITY CONTROL BLOCK
- Provider Legal Entity: ${ENTERPRISE_ENTITY.legalEntity} d/b/a ${ENTERPRISE_ENTITY.dba}
- Provider Federal EIN:  ${ENTERPRISE_ENTITY.ein} (${ENTERPRISE_ENTITY.irsNotice})
- Name Control:          ${ENTERPRISE_ENTITY.nameControl}
- Principal Address:     ${ENTERPRISE_ENTITY.principalAddress}
- Official Contact:      ${ENTERPRISE_ENTITY.email} | ${ENTERPRISE_ENTITY.phone}
- NAICS Codes:           ${ENTERPRISE_ENTITY.naicsCodes.map((n) => `${n.code} (${n.label})`).join(', ')}
- Work Order Ref:        ${selectedTrack.workOrderId}
- Version & Status:      ${selectedTrack.versionStatus}
- Statutory Authority:   ${ENTERPRISE_ENTITY.statutoryAuthority}

================================================================================
2. SCOPE & OBJECTIVE
- Role Title:            ${selectedTrack.roleTitle}
- Target Client Sector:  ${selectedTrack.targetClient}
- Published Rate Band:   ${selectedTrack.rateDescription}
- Monthly Baseline:      $${selectedTrack.rateValueMonthly.toLocaleString()} USD / month

================================================================================
3. 30 / 60 / 90-DAY CALIBRATION & VERIFICATION GATES
${selectedTrack.deliverables30_60_90
  .map(
    (phase) => `
[${phase.phase}] (${phase.targetWindow})
${phase.deliverables.map((d) => `  * ${d}`).join('\n')}
`
  )
  .join('')}

================================================================================
4. WORKSTATION ISOLATION & SOVEREIGN BOUNDARIES
${selectedTrack.workstationIsolation.map((w) => `  [Isolation] ${w}`).join('\n')}

${selectedTrack.sovereignBoundaries.map((b) => `  [Boundary]  ${b}`).join('\n')}

================================================================================
5. LINE ITEMS & COMPENSATION MATRIX
${selectedTrack.lineItems
  .map(
    (item) =>
      `  - [${item.code}] ${item.description}\n    Billing: ${item.billingType} | Rate: ${item.rateUsd} | Total: ${item.totalUsd}`
  )
  .join('\n\n')}

================================================================================
6. FINANCIAL SETTLEMENT & CASH-FLOW PROTOCOL
- Settlement Route:      ${selectedTrack.payoutFlowSummary}
- Banking Institution:   ${ENTERPRISE_ENTITY.bankingPartner}
- Settlement Model:      ${ENTERPRISE_ENTITY.achRoutingType}

================================================================================
7. SPECIALIST INTAKE PITCH TEXT
"${selectedTrack.pitchNote}"

================================================================================
EXECUTED ELECTRONICALLY UNDER 15 U.S.C. § 7001 (ESIGN) & ALA. CODE § 8-1A-1 (UETA)
Provider: Caleb Thomas Rex Holcomb — Owner & Principal Consultant
Entity:   Tyr's Technical Consultation (EIN: 42-XXX-X843)
`;

    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `TYR_${selectedTrack.code}_Master_SOW_Dossier.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-4 max-w-5xl mx-auto pb-8">
      {/* Executive Hero Banner */}
      <div className="relative rounded-2xl bg-gradient-to-br from-[#0c101d] via-[#090b12] to-[#040508] border border-cyan-500/30 p-5 shadow-2xl overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-10 -left-10 w-80 h-80 bg-blue-600/5 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[#1e2738] pb-3 mb-4">
            <div className="flex items-center space-x-2.5">
              <div className="p-2 rounded-xl bg-cyan-950/80 border border-cyan-500/40 text-cyan-300">
                <Building2 className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <h1 className="text-base sm:text-lg font-black text-white tracking-wide">
                    TYR ENTERPRISE B2B SOLUTIONS
                  </h1>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
                    SOW PORTAL
                  </span>
                </div>
                <p className="text-xs text-[#94a3b8]">
                  Verified 100% Remote Engineering, Evaluation & Scoping Retainers
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-2 text-xs">
              <button
                onClick={handleCopyEin}
                className="px-2.5 py-1 rounded-lg bg-[#111726] border border-[#232f48] text-cyan-200 hover:text-white font-mono flex items-center space-x-1.5 transition text-[11px]"
                title="Click to copy Federal EIN"
              >
                <span>EIN: {ENTERPRISE_ENTITY.ein}</span>
                {copiedEin ? (
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                ) : (
                  <Copy className="w-3.5 h-3.5 text-[#94a3b8]" />
                )}
              </button>
              <button
                onClick={onSwitchToStorefront}
                className="px-3 py-1 rounded-lg bg-[#11131a] hover:bg-[#1a1c26] text-[#cbd5e1] border border-[#232634] text-[11px] font-semibold transition"
              >
                ← View Small Biz Store
              </button>
            </div>
          </div>

          {/* Quick Entity Metadata Strip */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-[11px]">
            <div className="p-2 rounded-lg bg-[#080b14]/80 border border-[#172033]">
              <span className="text-[#64748b] block font-mono text-[9px] uppercase">
                Legal Entity
              </span>
              <span className="font-semibold text-slate-200 truncate block">
                {ENTERPRISE_ENTITY.legalEntity}
              </span>
              <span className="text-[10px] text-cyan-400/90 font-mono">
                d/b/a TYR (Sole Prop)
              </span>
            </div>
            <div className="p-2 rounded-lg bg-[#080b14]/80 border border-[#172033]">
              <span className="text-[#64748b] block font-mono text-[9px] uppercase">
                Primary NAICS
              </span>
              <span className="font-semibold text-slate-200">541512</span>
              <span className="text-[10px] text-[#94a3b8] block truncate">
                Systems Design Services
              </span>
            </div>
            <div className="p-2 rounded-lg bg-[#080b14]/80 border border-[#172033]">
              <span className="text-[#64748b] block font-mono text-[9px] uppercase">
                Execution Model
              </span>
              <span className="font-semibold text-emerald-400">
                100% Remote Async
              </span>
              <span className="text-[10px] text-[#94a3b8] block">
                Northwest Alabama Operations
              </span>
            </div>
            <div className="p-2 rounded-lg bg-[#080b14]/80 border border-[#172033]">
              <span className="text-[#64748b] block font-mono text-[9px] uppercase">
                Zero-Custody Settlement
              </span>
              <span className="font-semibold text-amber-300">Direct ACH</span>
              <span className="text-[10px] text-[#94a3b8] block truncate">
                Lili.co / Sunrise Banks
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Track Selection Tabs */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
        {ENTERPRISE_TRACKS.map((track) => {
          const isSelected = track.id === selectedTrackId;
          return (
            <button
              key={track.id}
              onClick={() => setSelectedTrackId(track.id)}
              className={`text-left p-3 rounded-xl border transition relative overflow-hidden flex flex-col justify-between ${
                isSelected
                  ? 'bg-gradient-to-b from-[#0f172a] to-[#070b14] border-cyan-400 shadow-lg shadow-cyan-950/40 ring-1 ring-cyan-400/50'
                  : 'bg-[#090c14]/80 border-[#1c2438] hover:border-[#2d3a5a] hover:bg-[#0c101a]'
              }`}
            >
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <span
                    className={`text-[10px] font-mono px-2 py-0.5 rounded font-bold ${
                      isSelected
                        ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40'
                        : 'bg-[#151c2e] text-[#94a3b8]'
                    }`}
                  >
                    {track.badge}
                  </span>
                  <span className="text-[10px] font-mono text-[#64748b]">
                    {track.code}
                  </span>
                </div>
                <h3
                  className={`text-xs font-bold leading-snug ${
                    isSelected ? 'text-white' : 'text-slate-300'
                  }`}
                >
                  {track.roleTitle}
                </h3>
              </div>

              <div className="mt-2.5 pt-2 border-t border-[#1a2336] flex items-center justify-between text-[11px]">
                <span className="font-mono text-cyan-300 font-bold">
                  {track.rateDescription.split('(')[0].trim()}
                </span>
                <span className="text-[10px] text-[#94a3b8]">
                  {track.rateUnit}
                </span>
              </div>
            </button>
          );
        })}
      </div>

      {/* Selected Track Detail Card */}
      <div className="rounded-2xl bg-[#090b14] border border-[#1e2738] p-4 sm:p-5 space-y-5">
        {/* Track Header & Actions */}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-4 border-b border-[#1b2438]">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs font-mono px-2 py-0.5 rounded bg-cyan-950 border border-cyan-500/40 text-cyan-300 font-bold">
                {selectedTrack.workOrderId}
              </span>
              <span className="text-xs font-mono px-2 py-0.5 rounded bg-emerald-950/60 border border-emerald-500/30 text-emerald-300">
                {selectedTrack.versionStatus}
              </span>
            </div>
            <h2 className="text-base sm:text-xl font-extrabold text-white">
              {selectedTrack.title}
            </h2>
            <p className="text-xs text-[#94a3b8]">
              Target: <span className="text-slate-300">{selectedTrack.targetClient}</span>
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2 shrink-0">
            <button
              onClick={handleCopyPitch}
              className="px-3 py-1.5 rounded-lg bg-[#141b2d] hover:bg-[#1c2742] text-cyan-300 border border-cyan-500/30 text-xs font-bold flex items-center space-x-1.5 transition shadow-sm"
              title="Copy intake pitch for job platforms / recruiters"
            >
              {copiedPitch ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-emerald-400">Copied Pitch Note!</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Copy Pitch Text</span>
                </>
              )}
            </button>
            <button
              onClick={handleDownloadDossier}
              className="px-3 py-1.5 rounded-lg bg-[#141b2d] hover:bg-[#1c2742] text-amber-300 border border-amber-500/30 text-xs font-bold flex items-center space-x-1.5 transition shadow-sm"
              title="Download Master SOW Dossier Plaintext File"
            >
              <Download className="w-3.5 h-3.5 text-amber-400" />
              <span>Download SOW Dossier</span>
            </button>
            <button
              onClick={() => onOpenIntakeWithTrack(selectedTrack)}
              className="px-4 py-1.5 rounded-lg bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white text-xs font-extrabold flex items-center space-x-1.5 transition shadow-md shadow-cyan-950/60"
            >
              <span>Engage Track Retainer</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* 1-Click Specialist Pitch Callout Box */}
        <div className="p-3.5 rounded-xl bg-[#060810] border border-cyan-500/20 space-y-2">
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center space-x-1.5 text-cyan-300 font-bold">
              <Terminal className="w-3.5 h-3.5" />
              <span>Specialist Intake Pitch (Copy-Paste Ready for Client Portal)</span>
            </div>
            <span className="text-[10px] font-mono text-[#64748b]">
              Zero-Cramming Verified Production Scope
            </span>
          </div>
          <p className="text-xs text-slate-300 leading-relaxed font-sans bg-[#0a0e1a] p-3 rounded-lg border border-[#182236] select-all">
            "{selectedTrack.pitchNote}"
          </p>
        </div>

        {/* 30 / 60 / 90-Day Deliverables Matrix */}
        <div className="space-y-3">
          <div className="flex items-center space-x-2">
            <Calendar className="w-4 h-4 text-cyan-400" />
            <h3 className="text-xs font-bold text-white uppercase tracking-wider">
              30 / 60 / 90-Day Calibration & Execution Deliverables
            </h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {selectedTrack.deliverables30_60_90.map((phase, idx) => (
              <div
                key={idx}
                className="p-3 rounded-xl bg-[#070a12] border border-[#1b253b] flex flex-col justify-between space-y-2"
              >
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-cyan-950/80 text-cyan-300 border border-cyan-800/40 font-bold">
                      {phase.targetWindow}
                    </span>
                    <span className="text-[10px] font-mono text-[#64748b]">
                      Gate 0{idx + 1}
                    </span>
                  </div>
                  <h4 className="text-xs font-bold text-slate-200 mb-2">
                    {phase.phase}
                  </h4>
                  <ul className="space-y-1.5 text-[11px] text-[#94a3b8]">
                    {phase.deliverables.map((item, dIdx) => (
                      <li key={dIdx} className="flex items-start space-x-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                        <span className="leading-snug">{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Workstation Isolation Envelope & Sovereign Boundaries */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-2">
          {/* Workstation Isolation Envelope */}
          <div className="p-3.5 rounded-xl bg-[#080c16] border border-[#1b253b] space-y-2.5">
            <div className="flex items-center space-x-2 text-xs font-bold text-emerald-300">
              <Shield className="w-4 h-4 text-emerald-400" />
              <span>Zero-Custody Workstation Isolation Envelope</span>
            </div>
            <ul className="space-y-1.5 text-[11px] text-[#94a3b8]">
              {selectedTrack.workstationIsolation.map((item, idx) => (
                <li key={idx} className="flex items-start space-x-1.5">
                  <Lock className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                  <span className="leading-snug">{item}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Sovereign Boundaries & Exclusions */}
          <div className="p-3.5 rounded-xl bg-[#080c16] border border-[#1b253b] space-y-2.5">
            <div className="flex items-center space-x-2 text-xs font-bold text-amber-300">
              <Flame className="w-4 h-4 text-amber-400" />
              <span>Sovereign Boundaries & Liability Limits</span>
            </div>
            <ul className="space-y-1.5 text-[11px] text-[#94a3b8]">
              {selectedTrack.sovereignBoundaries.map((item, idx) => (
                <li key={idx} className="flex items-start space-x-1.5">
                  <span className="text-amber-400 font-bold shrink-0">•</span>
                  <span className="leading-snug">{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Compensation Matrix & Lili.co Billing Protocol */}
        <div className="p-3.5 rounded-xl bg-[#060810] border border-[#1a2336] space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center space-x-2">
              <Briefcase className="w-4 h-4 text-cyan-400" />
              <h3 className="text-xs font-bold text-white uppercase tracking-wider">
                Commercial Line Items & Settlement Schedule
              </h3>
            </div>
            <span className="text-[10px] font-mono text-emerald-400">
              ● Form W-9 Sole Proprietorship Pass-Through
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-[#1f293d] text-[#64748b] text-[10px]">
                  <th className="pb-1.5">SKU / Code</th>
                  <th className="pb-1.5">Deliverable Description</th>
                  <th className="pb-1.5">Billing Model</th>
                  <th className="pb-1.5">Unit Rate</th>
                  <th className="pb-1.5 text-right">Total / Sub</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#162033]">
                {selectedTrack.lineItems.map((line, idx) => (
                  <tr key={idx} className="text-[#cbd5e1]">
                    <td className="py-2 text-cyan-300 font-bold">{line.code}</td>
                    <td className="py-2 text-[11px] font-sans pr-2">
                      {line.description}
                    </td>
                    <td className="py-2 text-[10px] text-[#94a3b8]">
                      {line.billingType}
                    </td>
                    <td className="py-2 text-[11px] text-amber-300">
                      {line.rateUsd}
                    </td>
                    <td className="py-2 text-right font-bold text-white">
                      {line.totalUsd}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="p-2.5 rounded-lg bg-[#0b0f1d] border border-[#182338] text-[11px] text-[#94a3b8] flex items-start space-x-2">
            <Sparkles className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
            <div>
              <span className="text-slate-200 font-semibold">
                Cash-Flow & Banking Facilitation:
              </span>{' '}
              {selectedTrack.payoutFlowSummary}
            </div>
          </div>
        </div>

        {/* Discovery Action Bar */}
        <div className="flex flex-col lg:flex-row items-center justify-between gap-3 pt-2">
          <div className="text-xs text-[#94a3b8] text-center lg:text-left">
            Need custom scoping, out-of-band failover dispatch, or statutory ESIGN execution?
          </div>
          <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto justify-end">
            {onOpenDispatch && (
              <button
                onClick={() => onOpenDispatch(selectedTrack.code)}
                className="px-3 py-2 rounded-lg bg-[#0e1726] hover:bg-[#18263d] text-cyan-300 border border-cyan-500/40 text-xs font-bold flex items-center justify-center space-x-1.5 transition"
                title="Direct out-of-band Google Chat dispatch"
              >
                <Radio className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
                <span>Failover Dispatch</span>
              </button>
            )}
            <button
              onClick={() => onOpenBooking(selectedTrack.code)}
              className="px-3 py-2 rounded-lg bg-[#141d30] hover:bg-[#1f2b47] text-amber-300 border border-amber-500/30 text-xs font-bold flex items-center justify-center space-x-1.5 transition"
            >
              <Calendar className="w-3.5 h-3.5 text-amber-400" />
              <span>Discovery Call</span>
            </button>
            {onOpenSignModal && (
              <button
                onClick={() => onOpenSignModal(selectedTrack)}
                className="px-3 py-2 rounded-lg bg-[#0a1f1d] hover:bg-[#10302c] text-emerald-300 border border-emerald-500/40 text-xs font-bold flex items-center justify-center space-x-1.5 transition"
              >
                <FileText className="w-3.5 h-3.5 text-emerald-400" />
                <span>Sign SOW (ESIGN)</span>
              </button>
            )}
            <button
              onClick={() => onOpenIntakeWithTrack(selectedTrack)}
              className="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-extrabold flex items-center justify-center space-x-1.5 transition shadow-md"
            >
              <span>Initiate Retainer SOW</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>

      {/* Statutory Regulatory Authority Banner */}
      <div className="text-[10px] font-mono text-[#64748b] bg-[#06080e] p-3 rounded-xl border border-[#151c2d] flex flex-col sm:flex-row items-center justify-between gap-2">
        <span>{ENTERPRISE_ENTITY.statutoryAuthority}</span>
        <span className="shrink-0 text-cyan-400/80">
          Northwest Alabama • 15 U.S.C. § 7001
        </span>
      </div>
    </div>
  );
}
