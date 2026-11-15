import React, { useState } from 'react';
import {
  Shield,
  CheckCircle2,
  Clock,
  Zap,
  ArrowRight,
  Phone,
  Send,
  Calendar,
  AlertCircle
} from 'lucide-react';
import { RETAINER_PLANS, RetainerPlanInfo, ORGANIZATION } from '../data/catalog';

interface ClientRetainerPortalProps {
  onOpenIntakeWithPlan: (sku: string) => void;
  onOpenBooking: (sku: string) => void;
}

export default function ClientRetainerPortal({
  onOpenIntakeWithPlan,
  onOpenBooking
}: ClientRetainerPortalProps) {
  const [selectedPlanId, setSelectedPlanId] = useState<string>('web_shield');

  return (
    <div className="space-y-5 animate-fadeIn">
      {/* Top Hero Banner */}
      <div className="rounded-xl bg-gradient-to-br from-[#120a17] via-[#0d0f17] to-[#171209] border border-purple-500/40 p-4 shadow-xl space-y-2">
        <div className="flex items-center space-x-2 text-xs">
          <span className="font-mono uppercase px-2 py-0.5 rounded bg-purple-950/80 text-purple-300 border border-purple-700/60 font-bold">
            ᛏ ᚠ RECURRING CARE PORTAL
          </span>
          <span className="text-amber-400 font-semibold text-[11px]">$0 Platform Lock-In Guarantee</span>
        </div>
        <h2 className="text-base sm:text-lg font-black text-white">
          Sovereign Maintenance, Threat Shield &amp; Audit Retainers
        </h2>
        <p className="text-xs text-[#cbd5e1] leading-relaxed max-w-2xl">
          Continuous protection without long-term hostage contracts. Every retainer operates on a transparent month-to-month basis. 
          Pause, adjust, or terminate anytime with 24 hours notice.
        </p>

        {/* 3 Core Tenets */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 pt-2 border-t border-[#232634]/80 text-[11px]">
          <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] flex items-center space-x-2 text-[#cbd5e1]">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>Month-to-Month • 0 Cancellation Fees</span>
          </div>
          <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] flex items-center space-x-2 text-[#cbd5e1]">
            <Shield className="w-4 h-4 text-purple-400 shrink-0" />
            <span>24/7 Threat &amp; DNSSEC Surveillance</span>
          </div>
          <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] flex items-center space-x-2 text-[#cbd5e1]">
            <Zap className="w-4 h-4 text-amber-400 shrink-0" />
            <span>Direct Priority Line to Caleb TR Holcomb</span>
          </div>
        </div>
      </div>

      {/* Retainer Plans Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5">
        {RETAINER_PLANS.map(plan => {
          const isSelected = selectedPlanId === plan.id;

          return (
            <div
              key={plan.id}
              onClick={() => setSelectedPlanId(plan.id)}
              className={`rounded-xl bg-[#11131a] border p-4 flex flex-col justify-between cursor-pointer transition relative shadow-lg ${
                isSelected
                  ? 'border-purple-500 ring-1 ring-purple-500/60 bg-gradient-to-b from-[#170e1e] to-[#11131a]'
                  : 'border-[#232634] hover:border-purple-900/60'
              }`}
            >
              <div>
                {/* Header */}
                <div className="flex items-center justify-between pb-2 mb-2 border-b border-[#232634]">
                  <span className="font-mono text-[10px] text-amber-400 font-bold">
                    {plan.sku}
                  </span>
                  <span className="px-2 py-0.5 rounded text-[9px] uppercase font-bold bg-[#090a0f] text-purple-300 border border-purple-800/40">
                    {plan.badge}
                  </span>
                </div>

                {/* Name & Pricing */}
                <h3 className="text-base font-extrabold text-white">{plan.name}</h3>
                <div className="py-2">
                  <span className="text-2xl font-black text-amber-400">{plan.price}</span>
                  <span className="text-xs text-[#94a3b8] ml-1.5">{plan.cadence}</span>
                </div>
                <p className="text-xs text-[#cbd5e1] mb-3 leading-relaxed">{plan.description}</p>

                {/* SLA Tag */}
                <div className="bg-[#090a0f] px-2.5 py-1.5 rounded-lg border border-[#232634] flex items-center space-x-1.5 text-[10px] text-emerald-400 font-semibold mb-3">
                  <Clock className="w-3 h-3 text-emerald-400 shrink-0" />
                  <span>SLA: {plan.sla}</span>
                </div>

                {/* Features List */}
                <ul className="space-y-1.5 text-xs text-[#cbd5e1] mb-4">
                  {plan.features.map((feat, idx) => (
                    <li key={idx} className="flex items-start space-x-1.5">
                      <CheckCircle2 className="w-3.5 h-3.5 text-purple-400 shrink-0 mt-0.5" />
                      <span className="text-[11px] leading-snug">{feat}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Action Buttons */}
              <div className="space-y-1.5 pt-3 border-t border-[#232634]">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onOpenIntakeWithPlan(plan.sku);
                  }}
                  className="w-full py-2.5 rounded-lg bg-gradient-to-r from-purple-700 to-red-600 hover:from-purple-600 hover:to-red-500 text-white font-extrabold text-xs flex items-center justify-center space-x-1.5 shadow-md shadow-purple-950/60 transition"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Subscribe to {plan.name}</span>
                </button>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onOpenBooking(plan.sku);
                  }}
                  className="w-full py-1.5 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-[#cbd5e1] text-xs font-semibold border border-[#232634] flex items-center justify-center space-x-1 transition"
                >
                  <Calendar className="w-3 h-3 text-amber-400" />
                  <span>Book Intake Call First</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
