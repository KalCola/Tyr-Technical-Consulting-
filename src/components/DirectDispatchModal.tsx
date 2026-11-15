import React, { useState, useEffect } from 'react';
import {
  X,
  Radio,
  Send,
  Shield,
  CheckCircle2,
  AlertTriangle,
  Download,
  Copy,
  ExternalLink,
  MessageSquare,
  Lock,
  Mail,
  Phone,
  Check
} from 'lucide-react';
import {
  transmitDispatch,
  computeSha256,
  generateEmlContent,
  DirectDispatchMessage
} from '../data/dispatchStore';

interface DirectDispatchModalProps {
  isOpen: boolean;
  onClose: () => void;
  originMode?: 'storefront' | 'enterprise';
  initialSubject?: string;
}

export default function DirectDispatchModal({
  isOpen,
  onClose,
  originMode = 'enterprise',
  initialSubject = ''
}: DirectDispatchModalProps) {
  const [clientName, setClientName] = useState('');
  const [clientContact, setClientContact] = useState('');
  const [preferredChannel, setPreferredChannel] = useState<'google_chat' | 'email' | 'phone' | 'in_app'>('google_chat');
  const [severity, setSeverity] = useState<'SEV-1' | 'SEV-2' | 'SEV-3' | 'SEV-4'>('SEV-2');
  const [subject, setSubject] = useState(initialSubject || '');
  const [messageBody, setMessageBody] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submittedDispatch, setSubmittedDispatch] = useState<DirectDispatchMessage | null>(null);
  const [copiedId, setCopiedId] = useState(false);
  const [copiedTranscript, setCopiedTranscript] = useState(false);

  useEffect(() => {
    if (initialSubject) setSubject(initialSubject);
  }, [initialSubject]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim() || !clientContact.trim() || !messageBody.trim()) return;

    setIsSubmitting(true);
    const hash = await computeSha256(`${clientName}|${clientContact}|${messageBody}|${Date.now()}`);

    const result = await transmitDispatch({
      clientName: clientName.trim(),
      clientContact: clientContact.trim(),
      preferredChannel,
      severity,
      subject: subject.trim() || `[${severity}] Out-of-Band Dispatch from ${clientName}`,
      messageBody: messageBody.trim(),
      telemetry: {
        clientSha256: hash,
        userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : 'TYR-Client',
        originMode,
        failoverTriggered: true
      }
    });

    setIsSubmitting(false);
    setSubmittedDispatch(result);
  };

  const handleDownloadEml = () => {
    if (!submittedDispatch) return;
    const eml = generateEmlContent(submittedDispatch);
    const blob = new Blob([eml], { type: 'message/rfc822;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${submittedDispatch.id}_Dispatch_Receipt.eml`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleCopyId = () => {
    if (!submittedDispatch) return;
    navigator.clipboard.writeText(submittedDispatch.id);
    setCopiedId(true);
    setTimeout(() => setCopiedId(false), 2000);
  };

  const handleCopyTranscript = () => {
    if (!submittedDispatch) return;
    const transcript = generateEmlContent(submittedDispatch);
    navigator.clipboard.writeText(transcript);
    setCopiedTranscript(true);
    setTimeout(() => setCopiedTranscript(false), 2000);
  };

  const handleOpenGoogleChat = () => {
    // Direct link to Google Chat web interface (accessible via any Google account)
    window.open('https://chat.google.com/', '_blank');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#080b12] border border-cyan-500/40 rounded-2xl max-w-xl w-full shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header with Live Telemetry */}
        <div className="bg-[#0c111e] border-b border-[#1b253b] px-4 py-3 flex items-center justify-between shrink-0">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-xl bg-cyan-950/80 border border-cyan-500/40 text-cyan-300">
              <Radio className="w-4 h-4 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-sm font-bold text-white tracking-wide">
                  DIRECT REDUNDANT DISPATCH
                </h3>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-300 border border-emerald-500/30">
                  CARRIER FAILOVER ACTIVE
                </span>
              </div>
              <p className="text-[11px] text-[#94a3b8]">
                Zero-Cost Out-of-Band Channel • Google Chat / Direct Operations Relay
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#94a3b8] hover:text-white hover:bg-[#151c2e] transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-4 sm:p-5 overflow-y-auto space-y-4">
          {submittedDispatch ? (
            /* Success & Proof Screen */
            <div className="space-y-4 py-2">
              <div className="text-center space-y-2">
                <div className="w-12 h-12 rounded-full bg-emerald-950/80 border border-emerald-500/40 text-emerald-400 mx-auto flex items-center justify-center shadow-lg shadow-emerald-950/50">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h4 className="text-base font-extrabold text-white">
                  Dispatch Successfully Transmitted
                </h4>
                <p className="text-xs text-[#94a3b8] max-w-md mx-auto">
                  Your out-of-band message has been pushed to Caleb TR Holcomb's operations terminal and logged to the zero-custody audit vault.
                </p>
              </div>

              {/* Receipt Block */}
              <div className="p-3.5 rounded-xl bg-[#05070e] border border-[#1b253b] space-y-2.5">
                <div className="flex items-center justify-between text-xs font-mono">
                  <span className="text-[#64748b]">Dispatch Reference:</span>
                  <div className="flex items-center space-x-1.5">
                    <span className="text-cyan-300 font-bold">{submittedDispatch.id}</span>
                    <button
                      onClick={handleCopyId}
                      className="p-1 rounded bg-[#101626] hover:bg-[#19233c] text-slate-300"
                      title="Copy Dispatch ID"
                    >
                      {copiedId ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs font-mono">
                  <span className="text-[#64748b]">Severity Level:</span>
                  <span
                    className={`font-bold px-2 py-0.5 rounded text-[10px] ${
                      submittedDispatch.severity === 'SEV-1'
                        ? 'bg-red-950 text-red-300 border border-red-500/40'
                        : submittedDispatch.severity === 'SEV-2'
                        ? 'bg-amber-950 text-amber-300 border border-amber-500/40'
                        : 'bg-cyan-950 text-cyan-300 border border-cyan-500/40'
                    }`}
                  >
                    {submittedDispatch.severity}
                  </span>
                </div>

                <div className="flex items-center justify-between text-xs font-mono">
                  <span className="text-[#64748b]">Preferred Channel:</span>
                  <span className="text-slate-200 capitalize">{submittedDispatch.preferredChannel.replace('_', ' ')}</span>
                </div>

                <div className="pt-2 border-t border-[#141b2a] text-[10px] font-mono text-[#64748b] break-all">
                  SHA-256: {submittedDispatch.telemetry.clientSha256}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="space-y-2 pt-1">
                <button
                  onClick={handleOpenGoogleChat}
                  className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-xs flex items-center justify-center space-x-2 shadow-lg shadow-emerald-950/50 transition"
                >
                  <MessageSquare className="w-4 h-4" />
                  <span>Open in Google Chat / Gmail Workspace</span>
                  <ExternalLink className="w-3.5 h-3.5 ml-1" />
                </button>

                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={handleDownloadEml}
                    className="py-2 px-3 rounded-xl bg-[#111625] hover:bg-[#1a2136] border border-[#222e48] text-amber-300 text-xs font-semibold flex items-center justify-center space-x-1.5 transition"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Download .eml File</span>
                  </button>

                  <button
                    onClick={handleCopyTranscript}
                    className="py-2 px-3 rounded-xl bg-[#111625] hover:bg-[#1a2136] border border-[#222e48] text-cyan-300 text-xs font-semibold flex items-center justify-center space-x-1.5 transition"
                  >
                    {copiedTranscript ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedTranscript ? 'Copied Transcript!' : 'Copy Transcript'}</span>
                  </button>
                </div>

                <button
                  onClick={onClose}
                  className="w-full py-2 rounded-xl bg-[#0d101a] hover:bg-[#141926] text-[#94a3b8] text-xs font-semibold transition"
                >
                  Close Window
                </button>
              </div>
            </div>
          ) : (
            /* Input Form */
            <form onSubmit={handleSubmit} className="space-y-3.5">
              {/* Notice Box */}
              <div className="p-3 rounded-xl bg-[#060a14] border border-cyan-500/20 text-xs text-[#94a3b8] flex items-start space-x-2">
                <Shield className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <div className="leading-relaxed">
                  <span className="text-white font-semibold">Zero-Custody Carrier Bypass:</span> Use this dedicated channel if standard email or cell phone carriers are experiencing delays or outages. Dispatches push straight to Caleb's Google Chat terminal with full cryptographic receipt logging.
                </div>
              </div>

              {/* Severity Level Picker */}
              <div>
                <label className="block text-xs font-bold text-white mb-1.5">
                  Severity / Urgency Classification
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-1.5 text-xs">
                  {[
                    { id: 'SEV-1', label: 'SEV-1 Critical', desc: 'Outage / Prod Down', color: 'red' },
                    { id: 'SEV-2', label: 'SEV-2 Urgent', desc: 'Active SOW / Deadline', color: 'amber' },
                    { id: 'SEV-3', label: 'SEV-3 Standard', desc: 'Inquiry / Discovery', color: 'cyan' },
                    { id: 'SEV-4', label: 'SEV-4 Admin', desc: 'Scheduling / Info', color: 'slate' }
                  ].map((s) => {
                    const isSelected = severity === s.id;
                    return (
                      <button
                        type="button"
                        key={s.id}
                        onClick={() => setSeverity(s.id as any)}
                        className={`p-2 rounded-lg border text-left transition ${
                          isSelected
                            ? s.id === 'SEV-1'
                              ? 'bg-red-950/80 border-red-500 text-white'
                              : s.id === 'SEV-2'
                              ? 'bg-amber-950/80 border-amber-500 text-white'
                              : 'bg-cyan-950/80 border-cyan-400 text-white'
                            : 'bg-[#0a0e19] border-[#182338] text-[#94a3b8] hover:border-[#243350]'
                        }`}
                      >
                        <div className="font-bold text-[11px] font-mono">{s.label}</div>
                        <div className="text-[10px] text-[#64748b] truncate">{s.desc}</div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Contact Information */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-[11px] font-mono text-[#94a3b8] mb-1">
                    Your Name / Organization *
                  </label>
                  <input
                    type="text"
                    required
                    value={clientName}
                    onChange={(e) => setClientName(e.target.value)}
                    placeholder="e.g., Elena Rostova"
                    className="w-full px-3 py-2 rounded-lg bg-[#0a0e19] border border-[#1b273f] text-white text-xs placeholder-[#475569] focus:outline-none focus:border-cyan-400"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-mono text-[#94a3b8] mb-1">
                    Your Email or Phone *
                  </label>
                  <input
                    type="text"
                    required
                    value={clientContact}
                    onChange={(e) => setClientContact(e.target.value)}
                    placeholder="elena@example.com or phone"
                    className="w-full px-3 py-2 rounded-lg bg-[#0a0e19] border border-[#1b273f] text-white text-xs placeholder-[#475569] focus:outline-none focus:border-cyan-400"
                  />
                </div>
              </div>

              {/* Preferred Channel */}
              <div>
                <label className="block text-[11px] font-mono text-[#94a3b8] mb-1">
                  Preferred Reply Pathway
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-1.5 text-xs">
                  {[
                    { id: 'google_chat', label: 'Google Chat', icon: MessageSquare, badge: 'Recommended' },
                    { id: 'email', label: 'Email', icon: Mail, badge: 'Standard' },
                    { id: 'phone', label: 'Phone Call', icon: Phone, badge: 'Voice' },
                    { id: 'in_app', label: 'In-App Only', icon: Lock, badge: 'Vault' }
                  ].map((ch) => {
                    const isSelected = preferredChannel === ch.id;
                    const Icon = ch.icon;
                    return (
                      <button
                        type="button"
                        key={ch.id}
                        onClick={() => setPreferredChannel(ch.id as any)}
                        className={`p-2 rounded-lg border text-center transition flex flex-col items-center justify-center space-y-1 ${
                          isSelected
                            ? 'bg-cyan-950/70 border-cyan-400 text-white'
                            : 'bg-[#0a0e19] border-[#182338] text-[#94a3b8] hover:border-[#243350]'
                        }`}
                      >
                        <Icon className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-semibold">{ch.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Subject */}
              <div>
                <label className="block text-[11px] font-mono text-[#94a3b8] mb-1">
                  Incident Subject / Topic
                </label>
                <input
                  type="text"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  placeholder="e.g., Emergency DNS Cutover / Active SOW Question"
                  className="w-full px-3 py-2 rounded-lg bg-[#0a0e19] border border-[#1b273f] text-white text-xs placeholder-[#475569] focus:outline-none focus:border-cyan-400"
                />
              </div>

              {/* Message Body */}
              <div>
                <label className="block text-[11px] font-mono text-[#94a3b8] mb-1">
                  Message / Outage Details *
                </label>
                <textarea
                  required
                  rows={4}
                  value={messageBody}
                  onChange={(e) => setMessageBody(e.target.value)}
                  placeholder="Describe the issue, requested milestone, or emergency circumstances..."
                  className="w-full px-3 py-2 rounded-lg bg-[#0a0e19] border border-[#1b273f] text-white text-xs placeholder-[#475569] focus:outline-none focus:border-cyan-400 resize-none font-sans"
                />
              </div>

              {/* Submit Button */}
              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 disabled:opacity-50 text-white font-extrabold text-xs flex items-center justify-center space-x-2 shadow-lg shadow-cyan-950/60 transition"
                >
                  <Send className="w-4 h-4" />
                  <span>{isSubmitting ? 'Securing & Transmitting...' : 'Dispatch Out-of-Band Message'}</span>
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
