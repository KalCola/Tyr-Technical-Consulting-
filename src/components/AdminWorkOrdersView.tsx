import React, { useState, useEffect } from 'react';
import {
  Shield,
  CheckCircle,
  Clock,
  Download,
  Mail,
  Search,
  Filter,
  ArrowLeft,
  FileText,
  AlertCircle,
  ExternalLink,
  ChevronDown,
  User,
  Phone,
  Building,
  Calendar,
  Send,
  Eye,
  Trash2,
  Radio,
  MessageSquare,
  Copy,
  Check
} from 'lucide-react';
import {
  WorkOrder,
  loadWorkOrders,
  acceptWorkOrder,
  updateOrderStatus,
  exportOrdersToJson,
  exportOrdersToCsv,
  generateAcceptanceEmail,
  AppointmentBooking,
  loadAppointments,
  generateIcsCalendarFile,
  VaultDocument,
  loadVaultDocuments,
  purgeDocument
} from '../data/intakeStore';
import {
  DirectDispatchMessage,
  loadDispatches,
  updateDispatchStatus,
  deleteDispatch,
  getGoogleChatWebhookUrl,
  saveGoogleChatWebhookUrl,
  generateEmlContent
} from '../data/dispatchStore';
import { SERVICES, ORGANIZATION } from '../data/catalog';

interface AdminWorkOrdersViewProps {
  onBackToStorefront: () => void;
}

export default function AdminWorkOrdersView({ onBackToStorefront }: AdminWorkOrdersViewProps) {
  const [adminTab, setAdminTab] = useState<'ORDERS' | 'APPOINTMENTS' | 'VAULT' | 'DISPATCHES'>('ORDERS');
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [appointments, setAppointments] = useState<AppointmentBooking[]>([]);
  const [vaultDocs, setVaultDocs] = useState<VaultDocument[]>([]);
  const [dispatches, setDispatches] = useState<DirectDispatchMessage[]>([]);
  const [googleChatWebhookUrl, setGoogleChatWebhookUrl] = useState<string>('');
  const [webhookSavedFeedback, setWebhookSavedFeedback] = useState<boolean>(false);
  const [searchFilter, setSearchFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedOrder, setSelectedOrder] = useState<WorkOrder | null>(null);
  const [adminNotesInput, setAdminNotesInput] = useState('');
  const [estDeliveryInput, setEstDeliveryInput] = useState('');
  const [actionSuccessMsg, setActionSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    refreshAll();
    setGoogleChatWebhookUrl(getGoogleChatWebhookUrl());
  }, []);

  const refreshAll = () => {
    refreshOrders();
    setAppointments(loadAppointments());
    setVaultDocs(loadVaultDocuments());
    setDispatches(loadDispatches());
  };

  const handleSaveWebhookUrl = () => {
    saveGoogleChatWebhookUrl(googleChatWebhookUrl);
    setWebhookSavedFeedback(true);
    setTimeout(() => setWebhookSavedFeedback(false), 2500);
  };

  const refreshOrders = () => {
    const list = loadWorkOrders();
    setOrders(list);
    if (selectedOrder) {
      const refreshed = list.find(o => o.id === selectedOrder.id);
      setSelectedOrder(refreshed || null);
    }
  };

  const handleAcceptOrder = (order: WorkOrder) => {
    const updated = acceptWorkOrder(order.id, adminNotesInput.trim() || undefined, estDeliveryInput.trim() || undefined);
    if (updated) {
      refreshOrders();
      setSelectedOrder(updated);

      // Trigger acceptance notification email dispatch
      const { subject, body } = generateAcceptanceEmail(updated);
      const mailtoUrl = `mailto:${encodeURIComponent(updated.email)}?subject=${encodeURIComponent(
        subject
      )}&body=${encodeURIComponent(body)}`;

      setActionSuccessMsg(`Work Order ${order.id} ACCEPTED! Client notification generated.`);

      // Open email client
      window.open(mailtoUrl, '_blank');
    }
  };

  const handleMarkCompleted = (orderId: string) => {
    const updated = updateOrderStatus(orderId, 'COMPLETED', 'All deliverables verified and master files handed over. Zero-custody purge initiated.');
    if (updated) {
      refreshOrders();
      setSelectedOrder(updated);
      setActionSuccessMsg(`Order ${orderId} marked as COMPLETED.`);
    }
  };

  const filteredOrders = orders.filter(o => {
    const matchesSearch =
      searchFilter === '' ||
      o.id.toLowerCase().includes(searchFilter.toLowerCase()) ||
      o.clientName.toLowerCase().includes(searchFilter.toLowerCase()) ||
      (o.businessName && o.businessName.toLowerCase().includes(searchFilter.toLowerCase())) ||
      o.email.toLowerCase().includes(searchFilter.toLowerCase()) ||
      o.selectedSkus.some(s => s.toLowerCase().includes(searchFilter.toLowerCase()));

    const matchesStatus = statusFilter === 'ALL' || o.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const pendingCount = orders.filter(o => o.status === 'PENDING').length;
  const activeCount = orders.filter(o => o.status === 'ACCEPTED' || o.status === 'IN_PROGRESS').length;

  return (
    <div className="min-h-screen bg-[#060609] text-[#f4f5f8] flex flex-col items-center p-3 sm:p-5 select-none">
      <div className="w-full max-w-5xl flex flex-col space-y-4">
        {/* Top Secret Admin Bar */}
        <div className="bg-[#0c0d13] border border-amber-500/40 rounded-xl px-4 py-3 flex items-center justify-between shadow-2xl">
          <div className="flex items-center space-x-3">
            <button
              onClick={onBackToStorefront}
              className="p-1.5 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-amber-300 border border-[#232634] transition flex items-center space-x-1 text-xs font-bold"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Client Storefront</span>
            </button>
            <div className="h-4 w-px bg-[#232634]" />
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-red-950/80 text-red-400 border border-red-800/40 uppercase font-black">
                  ᛏ SOVEREIGN ADMIN VAULT
                </span>
                <span className="text-[11px] text-amber-400 font-semibold hidden sm:inline">
                  Client Intake &amp; Work Order Terminal
                </span>
              </div>
              <h1 className="text-sm font-extrabold text-white leading-tight">
                CALEB TR HOLCOMB &bull; WORK ORDER CONTROL
              </h1>
            </div>
          </div>

          {/* Export to Folder Actions */}
          <div className="flex items-center space-x-2">
            <button
              onClick={() => exportOrdersToJson(orders)}
              className="px-2.5 py-1.5 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-amber-300 border border-[#232634] text-xs font-bold flex items-center space-x-1.5 transition"
              title="Export all client intakes to a JSON file in your Downloads folder"
            >
              <Download className="w-3.5 h-3.5 text-amber-400" />
              <span className="hidden sm:inline">Export JSON</span>
            </button>
            <button
              onClick={() => exportOrdersToCsv(orders)}
              className="px-2.5 py-1.5 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-emerald-400 border border-[#232634] text-xs font-bold flex items-center space-x-1.5 transition"
              title="Export all client intakes to CSV spreadsheet"
            >
              <Download className="w-3.5 h-3.5 text-emerald-400" />
              <span className="hidden sm:inline">Export CSV</span>
            </button>
          </div>
        </div>

        {/* Action feedback notification banner */}
        {actionSuccessMsg && (
          <div className="p-3 rounded-lg bg-emerald-950/80 border border-emerald-500/60 text-xs text-emerald-200 flex items-center justify-between shadow-lg">
            <div className="flex items-center space-x-2">
              <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>{actionSuccessMsg}</span>
            </div>
            <button onClick={() => setActionSuccessMsg(null)} className="text-emerald-400 hover:text-white font-bold">
              Dismiss
            </button>
          </div>
        )}

        {/* Navigation Sub-Tabs for Sovereign Vault Console */}
        <div className="flex space-x-2 border-b border-[#232634] pb-2 overflow-x-auto">
          <button
            onClick={() => setAdminTab('ORDERS')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition border whitespace-nowrap ${
              adminTab === 'ORDERS'
                ? 'bg-red-600 text-white border-amber-400 shadow-md'
                : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
            }`}
          >
            📋 Work Orders ({orders.length})
          </button>
          <button
            onClick={() => setAdminTab('APPOINTMENTS')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition border whitespace-nowrap ${
              adminTab === 'APPOINTMENTS'
                ? 'bg-amber-500 text-black border-amber-400 font-black shadow-md'
                : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
            }`}
          >
            📅 Consult Appointments ({appointments.length})
          </button>
          <button
            onClick={() => setAdminTab('VAULT')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition border whitespace-nowrap ${
              adminTab === 'VAULT'
                ? 'bg-emerald-600 text-white border-emerald-400 shadow-md'
                : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
            }`}
          >
            🔒 Zero-Custody Staged Assets ({vaultDocs.length})
          </button>
          <button
            onClick={() => setAdminTab('DISPATCHES')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition border whitespace-nowrap flex items-center space-x-1.5 ${
              adminTab === 'DISPATCHES'
                ? 'bg-cyan-600 text-white border-cyan-400 shadow-md shadow-cyan-950/60'
                : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
            }`}
          >
            <Radio className="w-3.5 h-3.5 text-cyan-300" />
            <span>Redundant Dispatches ({dispatches.length})</span>
            {dispatches.filter(d => d.status === 'PENDING').length > 0 && (
              <span className="bg-red-500 text-white text-[9px] font-black px-1.5 py-0.2 rounded-full">
                {dispatches.filter(d => d.status === 'PENDING').length}
              </span>
            )}
          </button>
        </div>

        {adminTab === 'DISPATCHES' ? (
          /* REDUNDANT DISPATCHES & GOOGLE CHAT RELAY VIEW */
          <div className="space-y-4">
            {/* Google Chat Webhook Configuration Box */}
            <div className="p-4 rounded-xl bg-[#080d18] border border-cyan-500/30 space-y-3 shadow-xl">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center space-x-2">
                  <div className="p-1.5 rounded-lg bg-cyan-950 border border-cyan-500/40 text-cyan-300">
                    <MessageSquare className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-xs font-bold text-white">
                      Google Chat Instant Push Relay (Zero-Cost Carrier Failover)
                    </h3>
                    <p className="text-[11px] text-[#94a3b8]">
                      Free real-time phone alerts for client out-of-band and emergency dispatches
                    </p>
                  </div>
                </div>
                <span className={`text-[10px] font-mono px-2 py-0.5 rounded border ${
                  googleChatWebhookUrl ? 'bg-emerald-950 text-emerald-300 border-emerald-500/40' : 'bg-[#141b2b] text-[#94a3b8] border-[#202c42]'
                }`}>
                  {googleChatWebhookUrl ? '● Relay Armed' : '○ Standby (Local Vault Only)'}
                </span>
              </div>

              <div className="flex flex-col sm:flex-row gap-2">
                <input
                  type="text"
                  value={googleChatWebhookUrl}
                  onChange={(e) => setGoogleChatWebhookUrl(e.target.value)}
                  placeholder="Paste Google Chat Webhook URL (https://chat.googleapis.com/v1/spaces/...)"
                  className="flex-1 bg-[#05070d] border border-[#1b263b] focus:border-cyan-400 rounded-lg px-3 py-1.5 text-xs text-white placeholder-[#475569] outline-none font-mono"
                />
                <button
                  onClick={handleSaveWebhookUrl}
                  className="px-4 py-1.5 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-bold transition flex items-center justify-center space-x-1.5 shrink-0"
                >
                  {webhookSavedFeedback ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-300" />
                      <span>Saved!</span>
                    </>
                  ) : (
                    <span>Save Webhook URL</span>
                  )}
                </button>
              </div>

              <div className="text-[10px] text-[#64748b] leading-relaxed">
                * Quick Setup: In Google Chat, open any private Space (e.g. "TYR Ops") &rarr; click Space title &rarr; <strong>Apps &amp; integrations</strong> &rarr; <strong>Webhooks</strong> &rarr; <strong>Add Webhook</strong> &rarr; paste URL here. Zero SMS fees, zero 3rd-party software required.
              </div>
            </div>

            {/* Dispatches List */}
            <div className="space-y-3">
              <div className="flex justify-between items-center text-xs">
                <span className="font-bold text-white">
                  Active Inbound Client Dispatches ({dispatches.length})
                </span>
                <span className="text-[11px] font-mono text-cyan-400">
                  {dispatches.filter(d => d.status === 'PENDING').length} Unacknowledged
                </span>
              </div>

              {dispatches.length === 0 ? (
                <div className="p-12 text-center text-xs text-[#94a3b8] bg-[#0c0d13] rounded-xl border border-[#232634]">
                  No dispatches received yet. Out-of-band dispatches from clients will appear here immediately.
                </div>
              ) : (
                <div className="space-y-3">
                  {dispatches.map((disp) => {
                    const isPending = disp.status === 'PENDING';
                    const isResolved = disp.status === 'RESOLVED';
                    return (
                      <div
                        key={disp.id}
                        className={`p-4 rounded-xl border transition space-y-3 shadow-lg ${
                          isPending
                            ? disp.severity === 'SEV-1'
                              ? 'bg-[#12080a] border-red-500/60 ring-1 ring-red-500/30'
                              : 'bg-[#120f08] border-amber-500/50'
                            : 'bg-[#0a0d15] border-[#1e2738]'
                        }`}
                      >
                        <div className="flex flex-wrap items-start justify-between gap-2">
                          <div>
                            <div className="flex flex-wrap items-center gap-2 mb-1">
                              <span className="font-mono text-xs font-black text-cyan-300">
                                {disp.id}
                              </span>
                              <span
                                className={`text-[10px] font-mono px-2 py-0.5 rounded font-black ${
                                  disp.severity === 'SEV-1'
                                    ? 'bg-red-950 text-red-300 border border-red-500/50'
                                    : disp.severity === 'SEV-2'
                                    ? 'bg-amber-950 text-amber-300 border border-amber-500/50'
                                    : 'bg-cyan-950 text-cyan-300 border border-cyan-500/50'
                                }`}
                              >
                                {disp.severity}
                              </span>
                              <span
                                className={`text-[9px] font-bold px-2 py-0.5 rounded uppercase ${
                                  isPending
                                    ? 'bg-amber-950 text-amber-300 border border-amber-700/60'
                                    : isResolved
                                    ? 'bg-blue-950 text-blue-300 border border-blue-700/60'
                                    : 'bg-emerald-950 text-emerald-300 border border-emerald-700/60'
                                }`}
                              >
                                {disp.status}
                              </span>
                              <span className="text-[10px] text-[#64748b]">
                                {new Date(disp.createdAt).toLocaleString()}
                              </span>
                            </div>
                            <h4 className="text-sm font-bold text-white">
                              {disp.subject}
                            </h4>
                            <div className="text-xs text-[#94a3b8] flex items-center space-x-2 mt-0.5">
                              <span className="text-slate-200 font-semibold">{disp.clientName}</span>
                              <span>&bull;</span>
                              <span className="text-cyan-400 font-mono">{disp.clientContact}</span>
                              <span>&bull;</span>
                              <span className="text-[10px] bg-[#141d2f] px-1.5 py-0.2 rounded text-[#94a3b8]">
                                Via: {disp.preferredChannel.replace('_', ' ')}
                              </span>
                            </div>
                          </div>

                          <div className="flex items-center space-x-1.5">
                            {isPending && (
                              <button
                                onClick={() => {
                                  updateDispatchStatus(disp.id, 'ACKNOWLEDGED');
                                  setDispatches(loadDispatches());
                                  setActionSuccessMsg(`Dispatch ${disp.id} acknowledged.`);
                                }}
                                className="px-2.5 py-1 rounded bg-amber-500 hover:bg-amber-400 text-black text-xs font-black transition"
                              >
                                Acknowledge
                              </button>
                            )}
                            {!isResolved && (
                              <button
                                onClick={() => {
                                  updateDispatchStatus(disp.id, 'RESOLVED');
                                  setDispatches(loadDispatches());
                                  setActionSuccessMsg(`Dispatch ${disp.id} marked as resolved.`);
                                }}
                                className="px-2.5 py-1 rounded bg-emerald-700 hover:bg-emerald-600 text-white text-xs font-bold transition"
                              >
                                Mark Resolved
                              </button>
                            )}
                            <button
                              onClick={() => {
                                deleteDispatch(disp.id);
                                setDispatches(loadDispatches());
                                setActionSuccessMsg(`Dispatch ${disp.id} deleted.`);
                              }}
                              className="p-1 rounded bg-red-950/60 hover:bg-red-900 text-red-400 transition"
                              title="Delete dispatch"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </div>

                        {/* Message Content */}
                        <div className="p-3 rounded-lg bg-[#060810] border border-[#161f30] text-xs text-slate-200 font-sans leading-relaxed whitespace-pre-wrap">
                          {disp.messageBody}
                        </div>

                        {/* Telemetry & Quick Action Bar */}
                        <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-[#151d2c] text-[11px]">
                          <span className="font-mono text-[10px] text-[#64748b] truncate max-w-[280px]">
                            SHA: {disp.telemetry.clientSha256}
                          </span>

                          <div className="flex items-center space-x-2">
                            <a
                              href="https://chat.google.com/"
                              target="_blank"
                              rel="noopener noreferrer"
                              className="px-2 py-1 rounded bg-[#101726] hover:bg-[#1a253d] text-cyan-300 font-bold flex items-center space-x-1"
                            >
                              <MessageSquare className="w-3 h-3" />
                              <span>Open Google Chat</span>
                            </a>
                            {disp.clientContact.includes('@') && (
                              <a
                                href={`mailto:${disp.clientContact}?subject=${encodeURIComponent(`RE: [${disp.severity}] ${disp.subject}`)}`}
                                className="px-2 py-1 rounded bg-[#101726] hover:bg-[#1a253d] text-slate-300 font-bold flex items-center space-x-1"
                              >
                                <Mail className="w-3 h-3" />
                                <span>Email Client</span>
                              </a>
                            )}
                            <button
                              onClick={() => {
                                const eml = generateEmlContent(disp);
                                const blob = new Blob([eml], { type: 'message/rfc822;charset=utf-8' });
                                const url = URL.createObjectURL(blob);
                                const a = document.createElement('a');
                                a.href = url;
                                a.download = `${disp.id}_Dispatch.eml`;
                                document.body.appendChild(a);
                                a.click();
                                document.body.removeChild(a);
                                URL.revokeObjectURL(url);
                              }}
                              className="px-2 py-1 rounded bg-[#101726] hover:bg-[#1a253d] text-amber-300 font-bold flex items-center space-x-1"
                            >
                              <Download className="w-3 h-3" />
                              <span>.eml</span>
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        ) : adminTab === 'APPOINTMENTS' ? (
          /* APPOINTMENTS VIEW */
          <div className="space-y-3">
            <div className="flex justify-between items-center text-xs">
              <span className="font-bold text-white">Scheduled Client Phone Consultations ({appointments.length})</span>
              <span className="text-[#94a3b8]">Timezone: Central Standard Time (CST)</span>
            </div>

            {appointments.length === 0 ? (
              <div className="p-12 text-center text-xs text-[#94a3b8] bg-[#0c0d13] rounded-xl border border-[#232634]">
                No consultation calls currently booked.
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {appointments.map(apt => (
                  <div
                    key={apt.id}
                    className="p-4 rounded-xl bg-[#0c0d13] border border-[#232634] space-y-3 hover:border-amber-500/50 transition shadow-lg"
                  >
                    <div className="flex justify-between items-start">
                      <div>
                        <span className="font-mono text-[10px] text-amber-400 font-bold block">{apt.id}</span>
                        <h4 className="text-sm font-extrabold text-white">{apt.clientName}</h4>
                        <span className="text-xs text-[#cbd5e1]">{apt.clientPhone} • {apt.clientEmail}</span>
                      </div>
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">
                        {apt.callDuration}
                      </span>
                    </div>

                    <div className="p-2.5 rounded-lg bg-[#07080d] border border-[#232634] text-xs space-y-1">
                      <div className="flex justify-between text-[#cbd5e1]">
                        <span className="text-[#94a3b8]">Scheduled Slot:</span>
                        <span className="font-bold text-amber-300">{apt.date} • {apt.timeSlot}</span>
                      </div>
                      <div className="flex justify-between text-[#cbd5e1]">
                        <span className="text-[#94a3b8]">Call Format:</span>
                        <span>{apt.callType}</span>
                      </div>
                      {apt.serviceSku && (
                        <div className="flex justify-between text-[#cbd5e1]">
                          <span className="text-[#94a3b8]">Topic SKU:</span>
                          <span className="font-mono text-cyan-400">{apt.serviceSku}</span>
                        </div>
                      )}
                      <p className="text-[11px] text-[#94a3b8] pt-1 italic">"{apt.topic}"</p>
                    </div>

                    <div className="flex space-x-2 pt-1">
                      <a
                        href={`tel:${apt.clientPhone}`}
                        className="flex-1 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center justify-center space-x-1 transition"
                      >
                        <Phone className="w-3.5 h-3.5" />
                        <span>Call Client Now</span>
                      </a>
                      <button
                        onClick={() => generateIcsCalendarFile(apt)}
                        className="px-3 py-1.5 rounded-lg bg-[#161822] hover:bg-[#232634] text-amber-300 border border-[#232634] text-xs font-semibold flex items-center space-x-1 transition"
                        title="Download .ics calendar event"
                      >
                        <Download className="w-3.5 h-3.5" />
                        <span>.ics</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : adminTab === 'VAULT' ? (
          /* ZERO-CUSTODY VAULT ASSETS VIEW */
          <div className="space-y-3">
            <div className="flex justify-between items-center text-xs">
              <span className="font-bold text-white">Staged Client Transit Documents ({vaultDocs.length})</span>
              <span className="text-emerald-400 text-[11px]">Strict Day-0 Purge Protocol Active</span>
            </div>

            {vaultDocs.length === 0 ? (
              <div className="p-12 text-center text-xs text-[#94a3b8] bg-[#0c0d13] rounded-xl border border-[#232634]">
                All client documents purged from session memory.
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {vaultDocs.map(doc => (
                  <div
                    key={doc.id}
                    className="p-4 rounded-xl bg-[#0c0d13] border border-[#232634] space-y-2.5 shadow-lg"
                  >
                    <div className="flex justify-between items-start">
                      <div className="truncate max-w-[260px]">
                        <span className="font-mono text-[10px] text-emerald-400 font-bold block">{doc.id}</span>
                        <h4 className="text-sm font-bold text-white truncate">{doc.name}</h4>
                        <span className="text-[11px] text-[#94a3b8]">
                          {(doc.size / 1024).toFixed(1)} KB • {doc.category}
                        </span>
                      </div>
                      <span
                        className={`text-[9px] font-bold px-2 py-0.5 rounded ${
                          doc.status === 'PURGED_DAY_0'
                            ? 'bg-red-950 text-red-400 border border-red-900'
                            : 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                        }`}
                      >
                        {doc.status}
                      </span>
                    </div>

                    <div className="p-2 rounded bg-[#07080d] border border-[#232634] text-[10px] font-mono text-[#64748b] truncate">
                      SHA256: {doc.sha256Hash}
                    </div>

                    {doc.status !== 'PURGED_DAY_0' && (
                      <button
                        onClick={() => {
                          purgeDocument(doc.id);
                          setVaultDocs(loadVaultDocuments());
                          setActionSuccessMsg(`Document ${doc.name} purged from memory.`);
                        }}
                        className="w-full py-1.5 rounded-lg bg-red-950/80 hover:bg-red-900 text-red-300 border border-red-800 text-xs font-bold flex items-center justify-center space-x-1.5 transition"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Purge File Now (Day-0 Rule)</span>
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          /* WORK ORDERS VIEW */
          <>
            {/* Metric Badges */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
              <div className="p-3 rounded-xl bg-[#0e1017] border border-[#232634]">
                <span className="text-[10px] uppercase font-bold text-[#94a3b8] block">Total Intakes</span>
                <span className="text-lg font-black text-white">{orders.length}</span>
              </div>
              <div className="p-3 rounded-xl bg-[#0e1017] border border-amber-500/40">
                <span className="text-[10px] uppercase font-bold text-amber-400 block">Pending Review</span>
                <span className="text-lg font-black text-amber-300">{pendingCount}</span>
              </div>
              <div className="p-3 rounded-xl bg-[#0e1017] border border-emerald-500/40">
                <span className="text-[10px] uppercase font-bold text-emerald-400 block">Active In Production</span>
                <span className="text-lg font-black text-emerald-300">{activeCount}</span>
              </div>
              <div className="p-3 rounded-xl bg-[#0e1017] border border-[#232634]">
                <span className="text-[10px] uppercase font-bold text-[#94a3b8] block">Confirmed Pipeline</span>
                <span className="text-lg font-black text-white">
                  ${orders.reduce((sum, o) => sum + (o.status !== 'DELIVERED' ? o.totalPrice : 0), 0)}
                </span>
              </div>
            </div>

            {/* Filter & Search Bar */}
            <div className="bg-[#0c0d13] border border-[#232634] rounded-xl p-3 flex flex-col sm:flex-row gap-2.5 items-stretch sm:items-center justify-between">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-[#94a3b8] absolute left-3 top-2.5" />
                <input
                  type="text"
                  value={searchFilter}
                  onChange={e => setSearchFilter(e.target.value)}
                  placeholder="Search by client, business, order ID, email, or SKU..."
                  className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg pl-9 pr-3 py-1.5 text-xs text-white placeholder-[#64748b] outline-none"
                />
              </div>

              <div className="flex items-center space-x-1.5 overflow-x-auto">
                {['ALL', 'PENDING', 'ACCEPTED', 'COMPLETED'].map(status => (
                  <button
                    key={status}
                    onClick={() => setStatusFilter(status)}
                    className={`px-2.5 py-1 rounded-md text-xs font-bold transition border ${
                      statusFilter === status
                        ? 'bg-amber-500 text-black border-amber-400'
                        : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
                    }`}
                  >
                    {status}
                  </button>
                ))}
              </div>
            </div>

            {/* Two-Column Workspace: Work Orders List + Detailed Inspection Drawer */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
          {/* Work Orders List (5 cols on lg) */}
          <div className="lg:col-span-5 space-y-2 max-h-[600px] overflow-y-auto pr-1">
            {filteredOrders.length === 0 ? (
              <div className="p-8 text-center text-xs text-[#94a3b8] bg-[#0c0d13] rounded-xl border border-[#232634]">
                No work orders matching query.
              </div>
            ) : (
              filteredOrders.map(order => {
                const isSelected = selectedOrder?.id === order.id;
                const isPending = order.status === 'PENDING';
                return (
                  <div
                    key={order.id}
                    onClick={() => {
                      setSelectedOrder(order);
                      setAdminNotesInput(order.adminNotes || '');
                      setEstDeliveryInput(order.estimatedDelivery || '');
                    }}
                    className={`p-3.5 rounded-xl border cursor-pointer transition ${
                      isSelected
                        ? 'bg-[#161824] border-amber-400 shadow-lg'
                        : 'bg-[#0e1017] border-[#232634] hover:border-[#383d52]'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <div className="flex items-center space-x-1.5">
                          <span className="font-mono text-xs font-extrabold text-amber-400">{order.id}</span>
                          <span
                            className={`px-1.5 py-0.5 rounded text-[9px] font-bold uppercase ${
                              order.status === 'PENDING'
                                ? 'bg-amber-950 text-amber-300 border border-amber-600/50'
                                : order.status === 'ACCEPTED'
                                ? 'bg-emerald-950 text-emerald-300 border border-emerald-600/50'
                                : 'bg-blue-950 text-blue-300 border border-blue-600/50'
                            }`}
                          >
                            {order.status}
                          </span>
                        </div>
                        <h4 className="text-xs font-bold text-white mt-1">{order.clientName}</h4>
                        <span className="text-[10px] text-[#94a3b8] block truncate max-w-[200px]">
                          {order.businessName || order.email}
                        </span>
                      </div>

                      <div className="text-right">
                        <span className="text-xs font-extrabold text-emerald-400 block">${order.totalPrice}</span>
                        <span className="text-[9px] text-[#94a3b8] font-mono">
                          {new Date(order.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>

                    <div className="mt-2 pt-2 border-t border-[#232634]/60 flex items-center justify-between text-[10px] text-[#cbd5e1]">
                      <span className="truncate max-w-[180px] font-mono">{order.selectedSkus.join(', ')}</span>
                      <span className="text-amber-400 font-semibold">{order.timeline}</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Detailed Order Inspector & Acceptance Workflow (7 cols on lg) */}
          <div className="lg:col-span-7">
            {selectedOrder ? (
              <div className="bg-[#0e1017] border border-[#232634] rounded-xl p-4 sm:p-5 space-y-4 shadow-xl">
                {/* Header */}
                <div className="flex items-start justify-between pb-3 border-b border-[#232634]">
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-sm font-extrabold text-amber-400">
                        {selectedOrder.id}
                      </span>
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                          selectedOrder.status === 'PENDING'
                            ? 'bg-amber-950 text-amber-300 border border-amber-600/50'
                            : selectedOrder.status === 'ACCEPTED'
                            ? 'bg-emerald-950 text-emerald-300 border border-emerald-600/50'
                            : 'bg-blue-950 text-blue-300 border border-blue-600/50'
                        }`}
                      >
                        {selectedOrder.status}
                      </span>
                    </div>
                    <h3 className="text-base font-extrabold text-white mt-1">
                      {selectedOrder.clientName}
                      {selectedOrder.businessName && (
                        <span className="text-xs font-normal text-[#94a3b8] ml-2">
                          ({selectedOrder.businessName})
                        </span>
                      )}
                    </h3>
                  </div>

                  <div className="text-right">
                    <span className="text-base font-black text-amber-400">${selectedOrder.totalPrice}</span>
                    <span className="text-[10px] text-[#94a3b8] block">Confirmed Flat Rate</span>
                  </div>
                </div>

                {/* Client Contact Strip */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs bg-[#07080d] p-3 rounded-lg border border-[#232634]">
                  <div className="flex items-center space-x-2 truncate">
                    <Mail className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                    <a href={`mailto:${selectedOrder.email}`} className="text-white hover:underline truncate">
                      {selectedOrder.email}
                    </a>
                  </div>
                  <div className="flex items-center space-x-2 truncate">
                    <Phone className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                    <a href={`tel:${selectedOrder.phone}`} className="text-white hover:underline truncate">
                      {selectedOrder.phone || 'No phone provided'}
                    </a>
                  </div>
                  {selectedOrder.existingDomain && (
                    <div className="sm:col-span-2 flex items-center space-x-2 text-[11px] text-[#cbd5e1] truncate pt-1 border-t border-[#232634]">
                      <ExternalLink className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                      <span>Existing Domain: <strong>{selectedOrder.existingDomain}</strong></span>
                    </div>
                  )}
                </div>

                {/* Services Scope */}
                <div className="space-y-1.5">
                  <span className="text-[10px] uppercase font-bold text-[#94a3b8] tracking-wider block">
                    Scope of Services
                  </span>
                  <div className="grid grid-cols-1 gap-1.5">
                    {selectedOrder.selectedSkus.map(sku => {
                      const item = SERVICES.find(s => s.sku === sku);
                      return (
                        <div
                          key={sku}
                          className="bg-[#07080d] p-2 rounded-lg border border-[#232634] flex items-center justify-between text-xs"
                        >
                          <div>
                            <span className="font-bold text-white block">{item ? item.title : sku}</span>
                            <span className="text-[10px] font-mono text-amber-400">{sku}</span>
                          </div>
                          <span className="font-bold text-white">{item ? item.price : ''}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Client Project Directives */}
                {selectedOrder.projectDescription && (
                  <div className="space-y-1">
                    <span className="text-[10px] uppercase font-bold text-[#94a3b8] tracking-wider block">
                      Client Intake Notes &amp; Specifications
                    </span>
                    <p className="p-3 bg-[#07080d] rounded-lg border border-[#232634] text-xs text-[#cbd5e1] leading-relaxed whitespace-pre-line">
                      {selectedOrder.projectDescription}
                    </p>
                  </div>
                )}

                {/* Administrative Production Controls & Automatic Client Notification */}
                <div className="p-3.5 rounded-xl bg-amber-950/20 border border-amber-500/40 space-y-3">
                  <div className="flex items-center space-x-2 text-amber-300">
                    <Shield className="w-4 h-4 text-amber-400" />
                    <span className="text-xs font-bold uppercase tracking-wider">
                      Work Order Acceptance &amp; Automatic Notification
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                    <div>
                      <label className="block text-[10px] text-[#cbd5e1] mb-1 font-semibold">
                        Estimated Target Delivery
                      </label>
                      <input
                        type="text"
                        value={estDeliveryInput}
                        onChange={e => setEstDeliveryInput(e.target.value)}
                        placeholder="e.g. Nov 10, 2026 (7-10 Days)"
                        className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-2.5 py-1.5 text-xs text-white outline-none"
                      />
                    </div>

                    <div>
                      <label className="block text-[10px] text-[#cbd5e1] mb-1 font-semibold">
                        Production Status Notes
                      </label>
                      <input
                        type="text"
                        value={adminNotesInput}
                        onChange={e => setAdminNotesInput(e.target.value)}
                        placeholder="e.g. Scope confirmed, DNS audit initiated"
                        className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-2.5 py-1.5 text-xs text-white outline-none"
                      />
                    </div>
                  </div>

                  {/* Accept & Dispatch Notification Action */}
                  <div className="flex flex-col sm:flex-row gap-2 pt-1">
                    <button
                      onClick={() => handleAcceptOrder(selectedOrder)}
                      className="flex-1 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-black text-xs flex items-center justify-center space-x-2 shadow-md transition"
                    >
                      <Send className="w-3.5 h-3.5" />
                      <span>
                        {selectedOrder.status === 'PENDING'
                          ? '✓ Accept Work Order & Send Notification'
                          : 'Update Status & Resend Notification'}
                      </span>
                    </button>

                    {selectedOrder.status === 'ACCEPTED' && (
                      <button
                        onClick={() => handleMarkCompleted(selectedOrder.id)}
                        className="py-2 px-3 rounded-lg bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs flex items-center justify-center space-x-1 transition"
                      >
                        <CheckCircle className="w-3.5 h-3.5" />
                        <span>Mark Delivered</span>
                      </button>
                    )}
                  </div>

                  <p className="text-[10px] text-[#94a3b8] leading-tight">
                    * Clicking Accept updates the work order in the system, triggers the in-app client status notification to <strong>ACCEPTED &amp; IN PROGRESS</strong>, and opens an official sovereign confirmation email ready to send to the client.
                  </p>
                </div>
              </div>
            ) : (
              <div className="h-full flex items-center justify-center p-12 text-center text-xs text-[#94a3b8] bg-[#0c0d13] rounded-xl border border-[#232634]">
                Select a work order from the list on the left to inspect specifications and dispatch acceptance.
              </div>
            )}
          </div>
        </div>
        </>
        )}
      </div>
    </div>
  );
}
