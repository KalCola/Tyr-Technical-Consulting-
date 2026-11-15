import React, { useState } from 'react';
import {
  CheckCircle2,
  Shield,
  Sparkles,
  ArrowRight,
  HelpCircle,
  Zap,
  DollarSign,
  ShoppingCart,
  Send
} from 'lucide-react';
import { SERVICE_TIERS, ServiceTier, SERVICES, ServiceItem } from '../data/catalog';

interface ServiceTierMatrixProps {
  onSelectTierSkus: (skus: string[]) => void;
  onOpenIntakeWithSkus: (skus: string[]) => void;
  cartSkus: string[];
}

export default function ServiceTierMatrix({
  onSelectTierSkus,
  onOpenIntakeWithSkus,
  cartSkus
}: ServiceTierMatrixProps) {
  const [selectedBottleneck, setSelectedBottleneck] = useState<string | null>(null);

  const bottlenecks = [
    {
      id: 'no_web',
      label: '🌐 No website or outdated domain',
      recommendedTier: 'solo_builder',
      reason: 'You need an immediate high-speed public beacon without monthly hosting bills.'
    },
    {
      id: 'spam_contracts',
      label: '✉️ Emails hitting spam & contract disputes',
      recommendedTier: 'operator_pro',
      reason: 'You need DMARC anti-spoofing armor and legally airtight 3-document contracts.'
    },
    {
      id: 'high_overhead',
      label: '📉 High SaaS costs & zero technical support',
      recommendedTier: 'sovereign_fortress',
      reason: 'Continuous uptime monitoring, bi-monthly software audits, and direct priority CTO line.'
    }
  ];

  return (
    <div className="space-y-5 animate-fadeIn">
      {/* Top Banner */}
      <div className="rounded-xl bg-gradient-to-br from-[#180a0e] via-[#0d0f17] to-[#1a1408] border border-amber-500/40 p-4 shadow-xl space-y-2">
        <div className="flex items-center space-x-2 text-xs">
          <span className="font-mono uppercase px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/40 font-bold">
            TIER COMPARISON MATRIX
          </span>
          <span className="text-[#94a3b8] text-[11px]">Nine Realms Flat-Rate Benchmarks</span>
        </div>
        <h2 className="text-base sm:text-lg font-black text-white">
          Sovereign Value vs. Traditional Agency Lock-in
        </h2>
        <p className="text-xs text-[#cbd5e1] leading-relaxed max-w-2xl">
          TYR replaces predatory 12-month agency contracts with clear flat-rate deliverables. 
          You receive complete master source files, unencrypted credentials, and $0 ongoing platform dependency.
        </p>

        {/* Interactive Bottleneck Recommender */}
        <div className="pt-3 border-t border-[#232634]/80 space-y-2">
          <div className="flex items-center space-x-1.5 text-xs text-amber-400 font-bold">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Interactive Guide: What is your primary business bottleneck right now?</span>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
            {bottlenecks.map(b => (
              <button
                key={b.id}
                type="button"
                onClick={() => setSelectedBottleneck(b.id === selectedBottleneck ? null : b.id)}
                className={`p-2.5 rounded-lg text-left text-xs border transition ${
                  selectedBottleneck === b.id
                    ? 'bg-amber-500/20 border-amber-400 text-white shadow-md'
                    : 'bg-[#090a0f] border-[#232634] text-[#cbd5e1] hover:bg-[#161822]'
                }`}
              >
                <span className="font-bold block text-xs">{b.label}</span>
                <span className="text-[10px] text-[#94a3b8] block mt-0.5">{b.reason}</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* 3 Tier Cards Side-by-Side Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5">
        {SERVICE_TIERS.map(tier => {
          const isHighlighted = selectedBottleneck
            ? bottlenecks.find(b => b.id === selectedBottleneck)?.recommendedTier === tier.id
            : tier.id === 'operator_pro';

          return (
            <div
              key={tier.id}
              className={`rounded-xl bg-[#11131a] border p-4 flex flex-col justify-between transition relative shadow-lg ${
                isHighlighted
                  ? 'border-amber-400 ring-1 ring-amber-400/50 shadow-amber-950/40 bg-gradient-to-b from-[#16130a] to-[#11131a]'
                  : 'border-[#232634] hover:border-red-900/60'
              }`}
            >
              {/* Badge */}
              <div className="flex items-center justify-between pb-2 mb-2 border-b border-[#232634]">
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                    isHighlighted
                      ? 'bg-amber-500 text-black font-extrabold'
                      : 'bg-[#090a0f] text-[#cbd5e1] border border-[#232634]'
                  }`}
                >
                  {tier.badge}
                </span>
                <span className="font-mono text-[10px] text-[#94a3b8]">
                  {tier.includedSkus.length} Core SKUs
                </span>
              </div>

              {/* Title & Price */}
              <div className="space-y-1 mb-3">
                <h3 className="text-base font-extrabold text-white">{tier.name}</h3>
                <p className="text-[11px] text-[#94a3b8] leading-tight">{tier.tagline}</p>
                <div className="pt-2">
                  <span className="text-xl font-black text-amber-400 block">{tier.price}</span>
                  <span className="text-[10px] text-[#cbd5e1] block">{tier.priceSub}</span>
                </div>
              </div>

              {/* Target Persona */}
              <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] text-[11px] text-[#cbd5e1] mb-3">
                <strong className="text-white block text-[10px] uppercase text-[#94a3b8] mb-0.5">
                  Best Suited For:
                </strong>
                {tier.bestFor}
              </div>

              {/* Highlights */}
              <div className="space-y-2 mb-4 flex-1">
                <span className="text-[10px] uppercase font-bold text-[#94a3b8] block">
                  Guaranteed Deliverables:
                </span>
                <ul className="space-y-1.5 text-xs text-[#cbd5e1]">
                  {tier.keyHighlights.map((hl, idx) => (
                    <li key={idx} className="flex items-start space-x-1.5">
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                      <span className="text-[11px] leading-snug">{hl}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Agency Comparison Box */}
              <div className="p-2 rounded-lg bg-red-950/30 border border-red-900/40 text-[10px] text-red-300 mb-3">
                <strong className="text-red-200 block">Contrast Against Agency:</strong>
                {tier.agencyComparison}
              </div>

              {/* Action Buttons */}
              <div className="space-y-1.5 pt-2 border-t border-[#232634]">
                <button
                  onClick={() => onOpenIntakeWithSkus(tier.includedSkus)}
                  className={`w-full py-2.5 rounded-lg font-extrabold text-xs flex items-center justify-center space-x-1.5 shadow-md transition ${
                    isHighlighted
                      ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black'
                      : 'bg-red-600 hover:bg-red-700 text-white'
                  }`}
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Order {tier.name}</span>
                </button>

                <button
                  onClick={() => onSelectTierSkus(tier.includedSkus)}
                  className="w-full py-1.5 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-[#cbd5e1] text-xs font-semibold border border-[#232634] flex items-center justify-center space-x-1 transition"
                >
                  <ShoppingCart className="w-3 h-3 text-cyan-400" />
                  <span>Add All SKUs to Estimate Cart</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
