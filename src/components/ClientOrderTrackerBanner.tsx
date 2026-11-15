import React, { useState, useEffect } from 'react';
import { Shield, Clock, CheckCircle2, ChevronRight, X, AlertCircle } from 'lucide-react';
import { getClientLastOrderId, getOrderById, WorkOrder } from '../data/intakeStore';

interface ClientOrderTrackerBannerProps {
  onOpenIntake: () => void;
}

export default function ClientOrderTrackerBanner({ onOpenIntake }: ClientOrderTrackerBannerProps) {
  const [activeOrder, setActiveOrder] = useState<WorkOrder | null>(null);
  const [showLookup, setShowLookup] = useState(false);
  const [lookupIdInput, setLookupIdInput] = useState('');
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [isDismissed, setIsDismissed] = useState(false);

  useEffect(() => {
    refreshTracking();
    // Poll every 10 seconds to catch when Caleb accepts the request
    const interval = setInterval(refreshTracking, 10000);
    return () => clearInterval(interval);
  }, []);

  const refreshTracking = () => {
    const lastId = getClientLastOrderId();
    if (lastId) {
      const order = getOrderById(lastId);
      if (order) {
        setActiveOrder(order);
      }
    }
  };

  const handleLookup = (e: React.FormEvent) => {
    e.preventDefault();
    if (!lookupIdInput.trim()) return;
    const found = getOrderById(lookupIdInput.trim());
    if (found) {
      setActiveOrder(found);
      setShowLookup(false);
      setLookupError(null);
      setIsDismissed(false);
    } else {
      setLookupError('Work Order not found. Please verify the ID (e.g. WO-2026-TYR-XXXX).');
    }
  };

  if (isDismissed || !activeOrder) {
    return (
      <div className="flex items-center justify-between px-3 py-1 bg-[#090a0f] border-b border-[#232634] text-[10px] text-[#94a3b8]">
        <span className="flex items-center space-x-1">
          <Shield className="w-3 h-3 text-emerald-400" />
          <span>Zero-Custody Protocol Active • Flat-Rate Pricing</span>
        </span>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setShowLookup(true)}
            className="text-amber-400 hover:text-amber-300 font-semibold underline"
          >
            Track Work Order
          </button>
          <span>&bull;</span>
          <button
            onClick={onOpenIntake}
            className="text-white hover:text-amber-300 font-bold"
          >
            + New Intake
          </button>
        </div>
      </div>
    );
  }

  const isAccepted = activeOrder.status === 'ACCEPTED' || activeOrder.status === 'IN_PROGRESS';
  const isCompleted = activeOrder.status === 'COMPLETED' || activeOrder.status === 'DELIVERED';

  return (
    <div className="bg-gradient-to-r from-[#0d1612] via-[#09110d] to-[#070b09] border-b border-emerald-500/40 px-3 py-2 text-xs relative shadow-md">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 max-w-4xl mx-auto">
        <div className="flex items-start sm:items-center space-x-2.5">
          <div
            className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 mt-0.5 sm:mt-0 ${
              isCompleted
                ? 'bg-blue-950 text-blue-400 border border-blue-500'
                : isAccepted
                ? 'bg-emerald-950 text-emerald-400 border border-emerald-400 animate-pulse'
                : 'bg-amber-950 text-amber-300 border border-amber-500'
            }`}
          >
            {isCompleted ? (
              <CheckCircle2 className="w-4 h-4" />
            ) : isAccepted ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            ) : (
              <Clock className="w-4 h-4" />
            )}
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <span className="font-mono text-[10px] font-black text-amber-300 tracking-wider">
                {activeOrder.id}
              </span>
              <span
                className={`px-1.5 py-0.2 rounded text-[9px] font-extrabold uppercase ${
                  isCompleted
                    ? 'bg-blue-900/60 text-blue-300 border border-blue-500/50'
                    : isAccepted
                    ? 'bg-emerald-900/80 text-emerald-300 border border-emerald-400 shadow-sm'
                    : 'bg-amber-950 text-amber-300 border border-amber-600/50'
                }`}
              >
                {isAccepted ? 'ACCEPTED & IN PROGRESS' : activeOrder.status}
              </span>
            </div>

            <p className="text-[11px] text-white font-medium leading-snug mt-0.5">
              {isAccepted ? (
                <span>
                  <strong>Request Accepted!</strong> Caleb TR Holcomb is actively working on your project.
                  {activeOrder.estimatedDelivery && (
                    <span className="text-emerald-300 ml-1">
                      Target completion: {activeOrder.estimatedDelivery}.
                    </span>
                  )}
                </span>
              ) : isCompleted ? (
                <span>
                  <strong>Work Order Complete!</strong> All master deliverables have been verified and transferred.
                </span>
              ) : (
                <span>
                  <strong>Intake Received:</strong> Awaiting Caleb's review. You will receive immediate notification once accepted.
                </span>
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2 self-end sm:self-center shrink-0">
          <button
            onClick={() => setShowLookup(true)}
            className="text-[10px] px-2 py-1 rounded bg-[#11131a] hover:bg-[#1a1c26] text-amber-300 border border-[#232634] transition"
          >
            Lookup Order
          </button>
          <button
            onClick={() => setIsDismissed(true)}
            className="text-[#94a3b8] hover:text-white p-1"
            title="Dismiss notice"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Lookup Modal */}
      {showLookup && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#11131a] border border-amber-500/50 rounded-xl p-4 max-w-sm w-full space-y-3 shadow-2xl relative">
            <button
              onClick={() => {
                setShowLookup(false);
                setLookupError(null);
              }}
              className="absolute top-3 right-3 text-[#94a3b8] hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>

            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Look Up Work Order Status
            </h4>

            {lookupError && (
              <p className="text-[10px] text-red-400 bg-red-950/40 p-2 rounded border border-red-800">
                {lookupError}
              </p>
            )}

            <form onSubmit={handleLookup} className="space-y-2">
              <input
                type="text"
                value={lookupIdInput}
                onChange={e => setLookupIdInput(e.target.value)}
                placeholder="e.g. WO-2026-TYR-1082"
                className="w-full bg-[#07080d] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-1.5 text-xs text-white outline-none"
              />
              <button
                type="submit"
                className="w-full py-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs transition"
              >
                Track Status
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
