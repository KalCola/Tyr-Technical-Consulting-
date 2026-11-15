import React, { useState } from 'react';
import {
  X,
  Calendar,
  Clock,
  Phone,
  CheckCircle2,
  Download,
  Shield,
  Sparkles,
  ArrowRight
} from 'lucide-react';
import { ORGANIZATION, SERVICES } from '../data/catalog';
import {
  bookAppointment,
  generateIcsCalendarFile,
  AppointmentBooking
} from '../data/intakeStore';

interface AppointmentSchedulerModalProps {
  isOpen: boolean;
  onClose: () => void;
  preSelectedSku?: string;
}

export default function AppointmentSchedulerModal({
  isOpen,
  onClose,
  preSelectedSku
}: AppointmentSchedulerModalProps) {
  const [clientName, setClientName] = useState('');
  const [clientEmail, setClientEmail] = useState('');
  const [clientPhone, setClientPhone] = useState('');
  
  // Calculate next 5 weekdays
  const getUpcomingWeekdays = () => {
    const days: { dateStr: string; display: string; dayName: string }[] = [];
    const now = new Date();
    let current = new Date(now);
    current.setDate(current.getDate() + 1); // Start tomorrow

    while (days.length < 5) {
      const dayOfWeek = current.getDay();
      if (dayOfWeek !== 0 && dayOfWeek !== 6) { // Skip Sat/Sun
        const iso = current.toISOString().slice(0, 10);
        const display = current.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
        const dayName = current.toLocaleDateString('en-US', { weekday: 'short' });
        days.push({ dateStr: iso, display, dayName });
      }
      current.setDate(current.getDate() + 1);
    }
    return days;
  };

  const weekdays = getUpcomingWeekdays();
  const [selectedDate, setSelectedDate] = useState<string>(weekdays[0]?.dateStr || '');
  const [selectedSlot, setSelectedSlot] = useState<string>('10:30 AM CST');
  const [callDuration, setCallDuration] = useState<'15m Discovery' | '30m Architecture Review'>('30m Architecture Review');
  const [callType, setCallType] = useState<'Phone Call' | 'Encrypted Signal' | 'Email Prep'>('Phone Call');
  const [topic, setTopic] = useState('');
  const [confirmedBooking, setConfirmedBooking] = useState<AppointmentBooking | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!isOpen) return null;

  const timeSlots = [
    '09:00 AM CST',
    '10:30 AM CST',
    '01:00 PM CST',
    '02:30 PM CST',
    '04:00 PM CST'
  ];

  const handleBook = (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim() || !clientPhone.trim() || !clientEmail.trim()) {
      setErrorMsg('Please provide your name, phone number, and email.');
      return;
    }

    try {
      const booking = bookAppointment({
        clientName: clientName.trim(),
        clientEmail: clientEmail.trim(),
        clientPhone: clientPhone.trim(),
        date: selectedDate,
        timeSlot: selectedSlot,
        callDuration,
        callType,
        topic: topic.trim() || (preSelectedSku ? `Discussing ${preSelectedSku}` : 'Discovery and scope alignment.'),
        serviceSku: preSelectedSku
      });
      setConfirmedBooking(booking);
      setErrorMsg(null);
    } catch (err) {
      setErrorMsg('Failed to record booking. Please call Caleb directly.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-50 flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-[#11131a] border border-amber-500/50 rounded-2xl max-w-lg w-full p-5 space-y-4 shadow-2xl relative my-auto">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94a3b8] hover:text-white transition"
        >
          <X className="w-5 h-5" />
        </button>

        {confirmedBooking ? (
          /* Confirmation View */
          <div className="space-y-4 py-2">
            <div className="w-12 h-12 rounded-full bg-emerald-950/80 border border-emerald-500/80 flex items-center justify-center mx-auto text-emerald-400">
              <CheckCircle2 className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded bg-emerald-950 text-emerald-300 border border-emerald-800">
                {confirmedBooking.id}
              </span>
              <h3 className="text-base font-extrabold text-white">
                Consultation Confirmed with Caleb TR Holcomb
              </h3>
              <p className="text-xs text-[#cbd5e1] max-w-sm mx-auto">
                Caleb will initiate a direct phone call to{' '}
                <strong className="text-amber-300">{confirmedBooking.clientPhone}</strong> at your chosen time.
              </p>
            </div>

            <div className="bg-[#090a0f] p-3.5 rounded-xl border border-[#232634] space-y-2 text-xs">
              <div className="flex justify-between items-center text-[#94a3b8]">
                <span>Date &amp; Time:</span>
                <span className="font-bold text-white">
                  {confirmedBooking.date} • {confirmedBooking.timeSlot}
                </span>
              </div>
              <div className="flex justify-between items-center text-[#94a3b8]">
                <span>Duration / Format:</span>
                <span className="font-bold text-amber-400">
                  {confirmedBooking.callDuration} ({confirmedBooking.callType})
                </span>
              </div>
              <div className="flex justify-between items-center text-[#94a3b8]">
                <span>Direct Line:</span>
                <span className="font-mono text-emerald-400 font-bold">{ORGANIZATION.phone}</span>
              </div>
            </div>

            <div className="space-y-2 pt-1">
              <button
                onClick={() => generateIcsCalendarFile(confirmedBooking)}
                className="w-full py-2.5 rounded-lg bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-extrabold text-xs flex items-center justify-center space-x-2 shadow-lg transition"
              >
                <Download className="w-4 h-4" />
                <span>Add to Apple / Google / Outlook Calendar (.ics)</span>
              </button>

              <button
                onClick={onClose}
                className="w-full py-2 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-[#cbd5e1] text-xs font-semibold border border-[#232634] transition"
              >
                Return to Storefront
              </button>
            </div>
          </div>
        ) : (
          /* Booking Form */
          <form onSubmit={handleBook} className="space-y-3.5">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-lg bg-amber-950/80 border border-amber-500/60 flex items-center justify-center text-amber-300">
                <Calendar className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-extrabold text-sm text-white uppercase tracking-wider">
                  SCHEDULE PRIVATE TECHNICAL CONSULTATION
                </h3>
                <p className="text-[10px] text-amber-400 font-medium">
                  Direct phone appointment with Caleb TR Holcomb • Zero Obligation
                </p>
              </div>
            </div>

            {errorMsg && (
              <div className="p-2.5 rounded-lg bg-red-950/60 border border-red-800 text-red-300 text-xs">
                {errorMsg}
              </div>
            )}

            {/* Select Day */}
            <div>
              <label className="text-[11px] font-bold text-[#cbd5e1] block mb-1.5">
                1. Select Available Day (Central Time)
              </label>
              <div className="grid grid-cols-5 gap-1.5">
                {weekdays.map(d => (
                  <button
                    key={d.dateStr}
                    type="button"
                    onClick={() => setSelectedDate(d.dateStr)}
                    className={`p-2 rounded-lg text-center border transition ${
                      selectedDate === d.dateStr
                        ? 'bg-amber-500/20 text-amber-300 border-amber-400 shadow-sm'
                        : 'bg-[#090a0f] text-[#94a3b8] border-[#232634] hover:bg-[#161822]'
                    }`}
                  >
                    <span className="text-[9px] uppercase font-bold block">{d.dayName}</span>
                    <span className="text-xs font-black text-white">{d.display}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Select Time Slot */}
            <div>
              <label className="text-[11px] font-bold text-[#cbd5e1] block mb-1.5">
                2. Select Preferred Time Slot
              </label>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-1.5">
                {timeSlots.map(slot => (
                  <button
                    key={slot}
                    type="button"
                    onClick={() => setSelectedSlot(slot)}
                    className={`py-1.5 px-2 rounded-lg text-xs font-medium border text-center transition flex items-center justify-center space-x-1 ${
                      selectedSlot === slot
                        ? 'bg-amber-500/20 text-amber-300 border-amber-400 font-bold'
                        : 'bg-[#090a0f] text-[#cbd5e1] border-[#232634] hover:bg-[#161822]'
                    }`}
                  >
                    <Clock className="w-3 h-3 text-[#94a3b8]" />
                    <span>{slot}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Format & Duration */}
            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-[10px] font-bold text-[#cbd5e1] block mb-1">Duration</label>
                <select
                  value={callDuration}
                  onChange={e => setCallDuration(e.target.value as any)}
                  className="w-full px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white focus:outline-none focus:border-amber-400"
                >
                  <option value="15m Discovery">15-min Discovery</option>
                  <option value="30m Architecture Review">30-min Architecture Review</option>
                </select>
              </div>

              <div>
                <label className="text-[10px] font-bold text-[#cbd5e1] block mb-1">Format</label>
                <select
                  value={callType}
                  onChange={e => setCallType(e.target.value as any)}
                  className="w-full px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white focus:outline-none focus:border-amber-400"
                >
                  <option value="Phone Call">Direct Phone Call</option>
                  <option value="Encrypted Signal">Signal Encrypted</option>
                  <option value="Email Prep">Email Scope Summary</option>
                </select>
              </div>
            </div>

            {/* Contact Details */}
            <div className="space-y-2 pt-1 border-t border-[#232634]">
              <label className="text-[11px] font-bold text-[#cbd5e1] block">
                3. Your Contact Coordinates
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <input
                  type="text"
                  required
                  placeholder="Your Full Name *"
                  value={clientName}
                  onChange={e => setClientName(e.target.value)}
                  className="px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white placeholder-[#64748b] focus:outline-none focus:border-amber-400"
                />
                <input
                  type="tel"
                  required
                  placeholder="Direct Phone (e.g. 205-555-0100) *"
                  value={clientPhone}
                  onChange={e => setClientPhone(e.target.value)}
                  className="px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white placeholder-[#64748b] focus:outline-none focus:border-amber-400"
                />
              </div>
              <input
                type="email"
                required
                placeholder="Email Address for Calendar Invite *"
                value={clientEmail}
                onChange={e => setClientEmail(e.target.value)}
                className="w-full px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white placeholder-[#64748b] focus:outline-none focus:border-amber-400"
              />
              <input
                type="text"
                placeholder="Specific project focus or questions (optional)"
                value={topic}
                onChange={e => setTopic(e.target.value)}
                className="w-full px-2.5 py-1.5 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white placeholder-[#64748b] focus:outline-none focus:border-amber-400"
              />
            </div>

            {/* Zero-Custody Note */}
            <div className="p-2 rounded-lg bg-[#090a0f] border border-[#232634] flex items-center space-x-2 text-[10px] text-[#94a3b8]">
              <Shield className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              <span>Zero-Custody: Your phone and email are never shared or added to marketing newsletters.</span>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              className="w-full py-2.5 rounded-lg bg-gradient-to-r from-red-600 via-amber-600 to-amber-500 hover:from-red-500 hover:to-amber-400 text-white font-extrabold text-xs flex items-center justify-center space-x-2 shadow-lg shadow-red-950/60 transition"
            >
              <Phone className="w-4 h-4 text-amber-200" />
              <span>Confirm Appointment ({selectedDate} @ {selectedSlot})</span>
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
