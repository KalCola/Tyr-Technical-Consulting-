import React, { useState } from 'react';
import { X, Send, CheckCircle2, Shield, AlertCircle, ShoppingCart, Clock, Building, User, Mail, Phone } from 'lucide-react';
import { SERVICES, ServiceItem, ORGANIZATION } from '../data/catalog';
import { submitWorkOrder, WorkOrder } from '../data/intakeStore';

interface ClientIntakeModalProps {
  isOpen: boolean;
  onClose: () => void;
  preSelectedServices?: ServiceItem[];
  onOrderSubmitted?: (order: WorkOrder) => void;
}

export default function ClientIntakeModal({
  isOpen,
  onClose,
  preSelectedServices = [],
  onOrderSubmitted
}: ClientIntakeModalProps) {
  const [clientName, setClientName] = useState('');
  const [businessName, setBusinessName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [preferredContact, setPreferredContact] = useState<'email' | 'phone' | 'text'>('email');
  const [selectedSkus, setSelectedSkus] = useState<string[]>(
    preSelectedServices.length > 0 ? preSelectedServices.map(s => s.sku) : ['SVC-WEB-01']
  );
  const [timeline, setTimeline] = useState<'Standard (7-14 Days)' | 'Rush (48-72h)' | 'Flexible'>('Standard (7-14 Days)');
  const [existingDomain, setExistingDomain] = useState('');
  const [projectDescription, setProjectDescription] = useState('');
  const [zeroCustodyAcknowledged, setZeroCustodyAcknowledged] = useState(true);

  const [submittedOrder, setSubmittedOrder] = useState<WorkOrder | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  React.useEffect(() => {
    if (isOpen) {
      if (preSelectedServices && preSelectedServices.length > 0) {
        setSelectedSkus(preSelectedServices.map(s => s.sku));
      }
      setSubmittedOrder(null);
      setErrorMsg(null);
    }
  }, [isOpen, preSelectedServices]);

  if (!isOpen) return null;

  const toggleSku = (sku: string) => {
    if (selectedSkus.includes(sku)) {
      if (selectedSkus.length === 1) {
        setErrorMsg('Please select at least one service for your work order.');
        return;
      }
      setSelectedSkus(selectedSkus.filter(s => s !== sku));
      setErrorMsg(null);
    } else {
      setSelectedSkus([...selectedSkus, sku]);
      setErrorMsg(null);
    }
  };

  const calculateTotal = () => {
    return selectedSkus.reduce((sum, sku) => {
      const s = SERVICES.find(item => item.sku === sku);
      return sum + (s ? s.priceValue : 0);
    }, 0);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim()) {
      setErrorMsg('Please provide your name.');
      return;
    }
    if (!email.trim() && !phone.trim()) {
      setErrorMsg('Please provide at least an email or phone number for work order notifications.');
      return;
    }
    if (selectedSkus.length === 0) {
      setErrorMsg('Please select at least one service item.');
      return;
    }
    if (!zeroCustodyAcknowledged) {
      setErrorMsg('Please acknowledge the Zero-Custody Privacy Rule.');
      return;
    }

    const order = submitWorkOrder({
      clientName: clientName.trim(),
      businessName: businessName.trim() || clientName.trim(),
      email: email.trim(),
      phone: phone.trim(),
      preferredContact,
      selectedSkus,
      totalPrice: calculateTotal(),
      timeline,
      existingDomain: existingDomain.trim(),
      projectDescription: projectDescription.trim(),
      zeroCustodyAcknowledged
    });

    setSubmittedOrder(order);
    if (onOrderSubmitted) {
      onOrderSubmitted(order);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-50 flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-[#0e0f17] border border-red-900/60 rounded-2xl max-w-xl w-full p-4 sm:p-6 space-y-4 shadow-2xl relative my-auto max-h-[92vh] flex flex-col">
        {/* Modal Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94a3b8] hover:text-white p-1 rounded-lg hover:bg-[#1a1c26] transition"
        >
          <X className="w-5 h-5" />
        </button>

        {submittedOrder ? (
          /* Submission Success State */
          <div className="space-y-4 text-center py-4">
            <div className="w-16 h-16 rounded-full bg-emerald-950/80 border-2 border-emerald-500/80 flex items-center justify-center mx-auto text-emerald-400">
              <CheckCircle2 className="w-8 h-8" />
            </div>

            <div>
              <span className="text-[10px] font-mono font-bold uppercase tracking-widest text-emerald-400 block">
                INTAKE SUBMISSION REGISTERED
              </span>
              <h2 className="text-lg font-black text-white mt-1">Work Order Initialized</h2>
              <p className="text-xs text-[#94a3b8] mt-1">
                Your request has been filed directly with Caleb TR Holcomb.
              </p>
            </div>

            <div className="bg-[#07080d] p-4 rounded-xl border border-[#232634] text-left space-y-2 text-xs">
              <div className="flex justify-between items-center pb-2 border-b border-[#232634]">
                <span className="text-[#94a3b8]">Tracking ID:</span>
                <span className="font-mono font-extrabold text-amber-400 text-sm">{submittedOrder.id}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#94a3b8]">Status:</span>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">
                  PENDING REVIEW
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#94a3b8]">Confirmed Flat Rate:</span>
                <span className="font-bold text-white">${submittedOrder.totalPrice}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#94a3b8]">Selected Services:</span>
                <span className="text-white font-mono">{submittedOrder.selectedSkus.join(', ')}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#94a3b8]">Notification Delivery:</span>
                <span className="text-emerald-400 font-medium">Automatic on Acceptance</span>
              </div>
            </div>

            <div className="p-3 rounded-lg bg-emerald-950/40 border border-emerald-500/30 text-[11px] text-emerald-300 leading-relaxed text-left flex items-start space-x-2">
              <Shield className="w-4 h-4 shrink-0 text-emerald-400 mt-0.5" />
              <span>
                <strong>Automatic Acceptance Notification:</strong> As soon as Caleb accepts your request and commences work, you will receive an acceptance notice. You will also see your status updated in the in-app tracker.
              </span>
            </div>

            <div className="flex gap-2 pt-2">
              <button
                onClick={onClose}
                className="flex-1 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs transition"
              >
                Done
              </button>
            </div>
          </div>
        ) : (
          /* Intake Form */
          <>
            <div className="border-b border-[#232634] pb-3">
              <div className="flex items-center space-x-2">
                <span className="px-1.5 py-0.5 rounded text-[10px] font-mono font-extrabold bg-red-950/80 text-red-400 border border-red-800/40 uppercase">
                  ᛏ CLIENT INTAKE
                </span>
                <span className="text-xs text-amber-400 font-mono">Zero-Custody Guaranteed</span>
              </div>
              <h2 className="text-base font-black text-white mt-1">
                Client Intake &amp; Work Order Request
              </h2>
              <p className="text-xs text-[#94a3b8]">
                Fill out your project specifications to initiate production with CALEB TR HOLCOMB (d/b/a TYR).
              </p>
            </div>

            {errorMsg && (
              <div className="p-2.5 rounded-lg bg-red-950/60 border border-red-700/60 text-xs text-red-200 flex items-center space-x-2">
                <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3.5 overflow-y-auto pr-1 text-xs">
              {/* Contact Information */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Your Full Name *
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      required
                      value={clientName}
                      onChange={e => setClientName(e.target.value)}
                      placeholder="e.g. Marcus Vance"
                      className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Business / Entity Name
                  </label>
                  <input
                    type="text"
                    value={businessName}
                    onChange={e => setBusinessName(e.target.value)}
                    placeholder="e.g. Vance Logistics LLC"
                    className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Email Address *
                  </label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    placeholder="client@yourbusiness.com"
                    className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    placeholder="(205) 555-0198"
                    className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                  />
                </div>
              </div>

              {/* Service Selection */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="block text-[11px] font-semibold text-[#cbd5e1]">
                    Services Requested * (Select one or more)
                  </label>
                  <span className="font-extrabold text-amber-400">
                    Est. Total: ${calculateTotal()}
                  </span>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-3 gap-1.5 max-h-36 overflow-y-auto p-1 bg-[#07080d] rounded-lg border border-[#232634]">
                  {SERVICES.map(s => {
                    const isSelected = selectedSkus.includes(s.sku);
                    return (
                      <button
                        type="button"
                        key={s.sku}
                        onClick={() => toggleSku(s.sku)}
                        className={`p-1.5 rounded text-left border transition text-[10px] flex flex-col justify-between ${
                          isSelected
                            ? 'bg-red-950/80 border-amber-400 text-white'
                            : 'bg-[#0f1118] border-[#232634] text-[#94a3b8] hover:text-white'
                        }`}
                      >
                        <span className="font-bold truncate">{s.title}</span>
                        <span className="font-mono text-amber-300 font-semibold">{s.sku} &bull; {s.price}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Timeline & Existing Domain */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Requested Timeline
                  </label>
                  <select
                    value={timeline}
                    onChange={e => setTimeline(e.target.value as any)}
                    className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white outline-none"
                  >
                    <option value="Standard (7-14 Days)">Standard (7–14 Days)</option>
                    <option value="Rush (48-72h)">Rush (48–72h Expedited)</option>
                    <option value="Flexible">Flexible / Ongoing</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                    Existing Domain / Host (Optional)
                  </label>
                  <input
                    type="text"
                    value={existingDomain}
                    onChange={e => setExistingDomain(e.target.value)}
                    placeholder="e.g. mybusiness.com (GoDaddy / Cloudflare)"
                    className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                  />
                </div>
              </div>

              {/* Project Scope Description */}
              <div>
                <label className="block text-[11px] font-semibold text-[#cbd5e1] mb-1">
                  Project Scope &amp; Special Directives
                </label>
                <textarea
                  rows={2}
                  value={projectDescription}
                  onChange={e => setProjectDescription(e.target.value)}
                  placeholder="Detail your requirements, current pain points, email deliverability issues, or contract terms..."
                  className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-white placeholder-[#64748b] outline-none"
                />
              </div>

              {/* Zero-Custody Checkbox */}
              <label className="flex items-start space-x-2 p-2 rounded-lg bg-[#07080d] border border-[#232634] cursor-pointer">
                <input
                  type="checkbox"
                  checked={zeroCustodyAcknowledged}
                  onChange={e => setZeroCustodyAcknowledged(e.target.checked)}
                  className="mt-0.5 rounded border-gray-700 text-amber-500 focus:ring-0"
                />
                <span className="text-[10px] text-[#cbd5e1] leading-tight">
                  I understand TYR's <strong>Zero-Custody Privacy Rule</strong>: All deliverables and source files become 100% my sovereign property upon delivery, with zero raw bank statement retention and 0-day post-delivery purging.
                </span>
              </label>

              {/* Submit Button */}
              <div className="pt-2">
                <button
                  type="submit"
                  className="w-full py-2.5 rounded-lg bg-gradient-to-r from-red-600 to-red-800 hover:from-red-500 hover:to-red-700 text-white font-extrabold text-xs flex items-center justify-center space-x-2 shadow-lg shadow-red-950/80 border border-amber-400 transition"
                >
                  <Send className="w-3.5 h-3.5 text-amber-300" />
                  <span>Submit Work Order Intake (${calculateTotal()} Flat)</span>
                </button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
