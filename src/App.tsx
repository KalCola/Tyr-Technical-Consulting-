import React, { useState, useEffect } from 'react';
import {
  Phone,
  Mail,
  Shield,
  Search,
  ShoppingCart,
  Trash2,
  Send,
  X,
  ChevronLeft,
  ChevronRight,
  CheckCircle,
  Sparkles,
  Wifi,
  BatteryCharging,
  Maximize2,
  ArrowRight,
  Sliders,
  Bot,
  MessageSquare,
  RefreshCw,
  Power,
  Info,
  Share2,
  Lock,
  FileText,
  ClipboardCheck,
  Calendar,
  UploadCloud,
  Layers,
  Building2
} from 'lucide-react';
import {
  ORGANIZATION,
  REALMS,
  SERVICES,
  FLAGSHIP_BUNDLE,
  WORKFLOW_STEPS,
  ServiceItem
} from './data/catalog';
import { ENTERPRISE_ENTITY, EnterpriseTrack } from './data/enterpriseCatalog';
import { askAiAdvisor, AiChatMessage } from './services/aiAdvisor';
import ClientIntakeModal from './components/ClientIntakeModal';
import AdminWorkOrdersView from './components/AdminWorkOrdersView';
import ClientOrderTrackerBanner from './components/ClientOrderTrackerBanner';
import AppointmentSchedulerModal from './components/AppointmentSchedulerModal';
import ServiceTierMatrix from './components/ServiceTierMatrix';
import ClientRetainerPortal from './components/ClientRetainerPortal';
import ZeroCustodyDocumentVaultModal from './components/ZeroCustodyDocumentVaultModal';
import EnterpriseB2BPortal from './components/EnterpriseB2BPortal';

export default function App() {
  const [portalMode, setPortalMode] = useState<'storefront' | 'enterprise'>('storefront');
  const [activeTab, setActiveTab] = useState<number>(0);
  const [showBookingModal, setShowBookingModal] = useState<boolean>(false);
  const [bookingPreSku, setBookingPreSku] = useState<string | undefined>(undefined);
  const [showVaultModal, setShowVaultModal] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedRealmFilter, setSelectedRealmFilter] = useState<string | null>(null);
  const [cart, setCart] = useState<ServiceItem[]>([]);
  const [inquiryModalItem, setInquiryModalItem] = useState<ServiceItem | null>(null);
  const [showInquiryModal, setShowInquiryModal] = useState<boolean>(false);
  const [showZeroCustodyModal, setShowZeroCustodyModal] = useState<boolean>(false);
  const [showCardFullscreen, setShowCardFullscreen] = useState<boolean>(false);

  // 3D Card Share Link Feedback
  const [copiedShare, setCopiedShare] = useState<boolean>(false);

  // Client Intake & Work Order Submissions
  const [showIntakeModal, setShowIntakeModal] = useState<boolean>(false);
  const [intakePreServices, setIntakePreServices] = useState<ServiceItem[]>([]);

  // Redundant Out-of-Band Dispatch (Google Chat Relay)
  const [showDispatchModal, setShowDispatchModal] = useState<boolean>(false);
  const [dispatchInitialSubject, setDispatchInitialSubject] = useState<string>('');

  // Electronic Contract & SOW Signing (15 U.S.C. § 7001 & Alabama UETA)
  const [showSignModal, setShowSignModal] = useState<boolean>(false);
  const [signDocTitle, setSignDocTitle] = useState<string>('');
  const [signDocCode, setSignDocCode] = useState<string>('');
  const [signContractValue, setSignContractValue] = useState<string>('');

  // Secret Admin Work Orders Terminal (Hidden from regular client view)
  const [isAdminView, setIsAdminView] = useState<boolean>(false);
  const [showAdminPasscodeModal, setShowAdminPasscodeModal] = useState<boolean>(false);
  const [adminPasscodeInput, setAdminPasscodeInput] = useState<string>('');
  const [adminPasscodeError, setAdminPasscodeError] = useState<string | null>(null);

  // 3D Card Helper Overlay (Subtle, semi-transparent, auto-fading after 3s)
  const [showCardGuide, setShowCardGuide] = useState<boolean>(true);
  const [cardGuideFading, setCardGuideFading] = useState<boolean>(false);

  // AI Integration: Strictly OPTIONAL with explicit persistent toggle
  const [aiEnabled, setAiEnabled] = useState<boolean>(() => {
    try {
      const stored = localStorage.getItem('tyr_ai_enabled');
      return stored === 'true'; // Defaults to false (Strict Zero AI / Opt-in)
    } catch {
      return false;
    }
  });
  const [showSettingsModal, setShowSettingsModal] = useState<boolean>(false);

  // AI Chat Messages & Input
  const [aiMessages, setAiMessages] = useState<AiChatMessage[]>([
    {
      id: 'init-1',
      sender: 'assistant',
      text: 'Hail traveler. I am the TYR & Freyja Technical Oracle. How can I assist with your web presence, email security, 3-doc contracts, or budget ledgering under the Nine Realms?',
      timestamp: 'Now'
    }
  ]);
  const [aiInput, setAiInput] = useState<string>('');
  const [aiLoading, setAiLoading] = useState<boolean>(false);
  const [aiScopeSummary, setAiScopeSummary] = useState<string | null>(null);
  const [aiScopeLoading, setAiScopeLoading] = useState<boolean>(false);

  // Check URL parameters for direct card view or secret admin vault
  useEffect(() => {
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get('admin') === 'tyr_vault_2026' || params.get('vault') === 'admin') {
        setIsAdminView(true);
      }
      if (params.get('view') === 'card' || params.get('tab') === '0' || params.get('tab') === 'card') {
        setActiveTab(0);
      }
    } catch {
      // Ignore
    }
  }, []);

  // Auto-fade 3D card guide overlay after 3 seconds
  useEffect(() => {
    setShowCardGuide(true);
    setCardGuideFading(false);
    const fadeTimer = setTimeout(() => {
      setCardGuideFading(true);
      const hideTimer = setTimeout(() => {
        setShowCardGuide(false);
      }, 700);
      return () => clearTimeout(hideTimer);
    }, 3000);

    return () => clearTimeout(fadeTimer);
  }, [activeTab, showCardFullscreen]);

  const handleShareCard = async () => {
    const shareUrl = `${window.location.origin}${cardAssetPath}`;
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Caleb TR Holcomb — TYR 3D Omni-Directional Business Card',
          text: 'Interactive 3D Business Card: Caleb TR Holcomb d/b/a TYR Technical Consulting',
          url: shareUrl
        });
        return;
      } catch (e) {
        // Fallback to clipboard
      }
    }
    try {
      await navigator.clipboard.writeText(shareUrl);
      setCopiedShare(true);
      setTimeout(() => setCopiedShare(false), 2500);
    } catch (err) {
      window.prompt('Direct link to 3D Business Card:', shareUrl);
    }
  };

  const handleVerifyPasscode = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = adminPasscodeInput.trim().toLowerCase();
    if (clean === 'tyr2026' || clean === 'tyrvault' || clean === 'caleb') {
      setIsAdminView(true);
      setShowAdminPasscodeModal(false);
      setAdminPasscodeInput('');
      setAdminPasscodeError(null);
    } else {
      setAdminPasscodeError('Invalid sovereign passcode. Access denied.');
    }
  };

  const toggleAiEnabled = () => {
    setAiEnabled((prev) => {
      const next = !prev;
      try {
        localStorage.setItem('tyr_ai_enabled', String(next));
      } catch (e) {
        console.warn('Could not save to localStorage', e);
      }
      return next;
    });
  };

  // Tabs
  const tabTitles = [
    { label: '🪪 3D Card (ᛏ ᚠ ᚲ)', sub: 'Omni-Directional Business Card' },
    { label: 'Page 1 (ᛗ ᛉ ᚱ)', sub: 'Web, Shield, QR' },
    { label: 'Page 2 (ᚨ ᚷ ᚲ)', sub: 'Resumes, Contracts, Buyer' },
    { label: 'Page 3 (ᚠ ᛟ ᚦ)', sub: 'Budgets, Vaults, Disputes' },
    { label: 'Page 4 (Tech Care)', sub: 'Web & Domain Shield' },
    { label: 'Page 5 (Audit Care)', sub: 'Expense, Watchdog & Retainer' },
    { label: 'Page 6 (3-Step)', sub: 'Bifröst Workflow' },
    { label: 'All Realms (18)', sub: 'Full 18-Service Catalog' },
    { label: `Estimate (${cart.length})`, sub: 'Custom Quote Builder' },
    { label: '✦ AI Advisor (ᛏ ᚠ)', sub: 'Scope Oracle & Rune Guide' },
    { label: '⚖️ Tier Matrix', sub: 'Solo vs Pro vs Fortress' },
    { label: '🛡️ Retainer Care', sub: 'Monthly Care & Subscriptions' }
  ];

  const cardAssetPath = '/assets/tyr-3d-business-card.html';

  const toggleCart = (item: ServiceItem) => {
    if (cart.some((c) => c.sku === item.sku)) {
      setCart(cart.filter((c) => c.sku !== item.sku));
    } else {
      setCart([...cart, item]);
    }
  };

  const clearCart = () => setCart([]);

  const openInquiry = (item: ServiceItem | null = null) => {
    if (item) {
      setIntakePreServices([item]);
    } else {
      setIntakePreServices(cart.length > 0 ? cart : []);
    }
    setShowIntakeModal(true);
  };

  const handleDial = () => {
    window.location.href = `tel:${ORGANIZATION.phoneRaw}`;
  };

  const handleEmail = (subject: string, body: string) => {
    window.location.href = `mailto:${ORGANIZATION.email}?subject=${encodeURIComponent(
      subject
    )}&body=${encodeURIComponent(body)}`;
  };

  const handleSendAiMessage = async (textToSend?: string) => {
    const query = (textToSend || aiInput).trim();
    if (!query || aiLoading) return;

    const userMsg: AiChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: query,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    setAiMessages((prev) => [...prev, userMsg]);
    setAiInput('');
    setAiLoading(true);

    try {
      const response = await askAiAdvisor(query, aiMessages);
      const assistantMsg: AiChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'assistant',
        text: response.text,
        recommendedSkus: response.recommendedSkus,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setAiMessages((prev) => [...prev, assistantMsg]);
    } catch (err) {
      console.error(err);
      const errorMsg: AiChatMessage = {
        id: `err-${Date.now()}`,
        sender: 'assistant',
        text: 'The runes encountered a disruption. Please dial Caleb directly at (659) 257-8043 for immediate sovereign consultation.',
        timestamp: 'Now'
      };
      setAiMessages((prev) => [...prev, errorMsg]);
    } finally {
      setAiLoading(false);
    }
  };

  const handleGenerateScopeSummary = async () => {
    if (cart.length === 0 || aiScopeLoading) return;
    setAiScopeLoading(true);
    try {
      const prompt = `Review this project estimate cart for TYR Technical Consulting:
Items: ${cart.map(c => `${c.sku} - ${c.title} (${c.price})`).join(', ')}.
Total One-Time: $${cartOneTimeTotal}. Monthly Recurring: $${cartMonthlyTotal}.
Provide a concise executive scope briefing, highlighting the anti-spoof/security benefits, zero-custody handover guarantee, and recommended next steps under the blessing of Tyr (ᛏ) and Freyja (ᚠ).`;

      const res = await askAiAdvisor(prompt);
      setAiScopeSummary(res.text);
    } catch (e) {
      setAiScopeSummary('Unable to generate scope summary. Please contact Caleb directly at (659) 257-8043.');
    } finally {
      setAiScopeLoading(false);
    }
  };

  // Filtered services for All Realms
  const filteredServices = SERVICES.filter((s) => {
    const matchesQuery =
      searchQuery === '' ||
      s.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.meaning.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.bullets.some((b) => b.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesRealm = selectedRealmFilter === null || s.realmId === selectedRealmFilter;
    return matchesQuery && matchesRealm;
  });

  const cartOneTimeTotal = cart
    .filter((c) => !c.isRecurring)
    .reduce((sum, c) => sum + c.priceValue, 0);

  const cartMonthlyTotal = cart
    .filter((c) => c.isRecurring)
    .reduce((sum, c) => sum + c.priceValue, 0);

  // Sovereign Admin Intake Vault View (Hidden from public clients)
  if (isAdminView) {
    return <AdminWorkOrdersView onBackToStorefront={() => setIsAdminView(false)} />;
  }

  return (
    <div className={`min-h-screen bg-[#060609] text-[#f4f5f8] flex flex-col items-center ${portalMode === 'storefront' && activeTab === 0 ? 'overflow-hidden h-screen' : ''}`}>
      {/* Android Device Container with Max Width for Tablet & Desktop */}
      <div className={`w-full max-w-3xl flex flex-col shadow-2xl relative ${portalMode === 'storefront' && activeTab === 0 ? 'h-screen overflow-hidden' : 'min-h-screen pb-20'}`}>
        {/* Android Simulated Status Bar */}
        <header className="bg-[#060609]/95 backdrop-blur-md px-4 py-1.5 flex items-center justify-between text-xs text-[#94a3b8] border-b border-[#232634]/50 shrink-0 z-50">
          <span className="font-semibold text-white tracking-wider">9:41 AM</span>
          <div className="flex items-center space-x-2">
            <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-red-950/60 text-red-400 border border-red-800/40">
              TYR PRO
            </span>
            <Wifi className="w-3.5 h-3.5 text-gray-300" />
            <BatteryCharging className="w-4 h-4 text-emerald-400" />
          </div>
        </header>

        {/* Material 3 App Bar */}
        <div className="bg-[#0c0d13] px-4 py-2.5 border-b border-[#232634] flex items-center justify-between shrink-0 z-40 shadow-lg">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-red-600 to-red-900 border border-amber-400 flex items-center justify-center font-black text-base text-white shadow-md shadow-red-900/40">
              ᛏ
            </div>
            <div>
              <h1 className="text-sm font-extrabold text-white leading-tight tracking-wide">
                TYR TECHNICAL CONSULTING
              </h1>
              <p className="text-[10px] text-amber-400 font-medium leading-none">
                {portalMode === 'enterprise'
                  ? 'B2B Enterprise & AI Evaluation Portal'
                  : activeTab === 0
                  ? '3D Omni-Directional Card'
                  : 'Nine Realms Storefront'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-1">
            <button
              onClick={() => setActiveTab(0)}
              className={`p-2 rounded-lg border transition ${
                activeTab === 0
                  ? 'bg-purple-950 text-white border-amber-400'
                  : 'bg-[#11131a] hover:bg-[#1f2330] text-purple-400 border-[#232634]'
              }`}
              title="3D Business Card"
            >
              <span className="text-sm">🪪</span>
            </button>
            <button
              onClick={() => setShowSettingsModal(true)}
              className={`p-2 rounded-lg border transition flex items-center space-x-1 ${
                aiEnabled
                  ? 'bg-amber-950/80 text-amber-300 border-amber-500/80 shadow-md shadow-amber-950/60'
                  : 'bg-[#11131a] hover:bg-[#1f2330] text-[#94a3b8] border-[#232634]'
              }`}
              title={aiEnabled ? 'AI Oracle Active (Click for Settings)' : 'AI Disabled (Strict Zero-AI Privacy)'}
            >
              {aiEnabled ? (
                <Sparkles className="w-4 h-4 text-amber-400" />
              ) : (
                <Sliders className="w-4 h-4 text-[#94a3b8]" />
              )}
            </button>
            <button
              onClick={() => setShowZeroCustodyModal(true)}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-emerald-400 border border-[#232634] flex items-center text-xs space-x-1 transition"
              title="Zero-Custody Privacy"
            >
              <Shield className="w-4 h-4 text-emerald-400" />
            </button>
            <button
              onClick={() => setShowBookingModal(true)}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-amber-300 border border-[#232634] transition"
              title="Schedule Consult Call"
            >
              <Calendar className="w-4 h-4 text-amber-300" />
            </button>
            <button
              onClick={() => setShowVaultModal(true)}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-emerald-400 border border-[#232634] transition"
              title="Zero-Custody Document Vault"
            >
              <UploadCloud className="w-4 h-4 text-emerald-400" />
            </button>
            <button
              onClick={() => {
                setDispatchInitialSubject('');
                setShowDispatchModal(true);
              }}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-cyan-400 border border-cyan-500/40 transition"
              title="Direct Failover Dispatch (Google Chat Relay • Carrier Outage Backup)"
            >
              <Radio className="w-4 h-4 text-cyan-400 animate-pulse" />
            </button>
            <button
              onClick={handleDial}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-amber-400 border border-[#232634] transition"
              title="Call Caleb TR Holcomb"
            >
              <Phone className="w-4 h-4" />
            </button>
            <button
              onClick={() => setActiveTab(8)}
              className="p-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-cyan-400 border border-[#232634] relative transition"
              title="Estimate Cart"
            >
              <ShoppingCart className="w-4 h-4" />
              {cart.length > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-600 text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                  {cart.length}
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Mode Switcher Pill Bar (Storefront vs Enterprise B2B) */}
        <div className="bg-[#090b12] border-b border-[#232634] px-3 py-1.5 flex items-center justify-between shrink-0 z-35">
          <div className="flex bg-[#05070d] p-0.5 rounded-lg border border-[#1e2738] w-full sm:w-auto">
            <button
              onClick={() => setPortalMode('storefront')}
              className={`flex-1 sm:flex-none px-3 py-1 rounded-md text-xs font-bold transition flex items-center justify-center space-x-1.5 ${
                portalMode === 'storefront'
                  ? 'bg-gradient-to-r from-red-600 to-amber-600 text-white shadow-md'
                  : 'text-[#94a3b8] hover:text-white'
              }`}
            >
              <span>ᛏ Small Business Storefront</span>
            </button>
            <button
              onClick={() => setPortalMode('enterprise')}
              className={`flex-1 sm:flex-none px-3 py-1 rounded-md text-xs font-bold transition flex items-center justify-center space-x-1.5 ${
                portalMode === 'enterprise'
                  ? 'bg-gradient-to-r from-cyan-600 to-blue-600 text-white shadow-md shadow-cyan-950/60'
                  : 'text-[#94a3b8] hover:text-white'
              }`}
            >
              <Building2 className="w-3.5 h-3.5 text-cyan-300" />
              <span>⚡ B2B Enterprise Portal</span>
            </button>
          </div>

          <div className="hidden sm:flex items-center space-x-2 text-[10px] font-mono text-[#94a3b8]">
            <span className="text-emerald-400">● 100% Remote Async</span>
            <span>EIN: 42-XXX-X843</span>
          </div>
        </div>

        {/* Deck Tabs Scrollable Bar - Only shown when in Small Business Storefront Mode */}
        {portalMode === 'storefront' && (
          <div className="bg-[#0c0d13]/90 border-b border-[#232634] px-3 py-1.5 shrink-0 z-30">
            <div className="flex space-x-1.5 overflow-x-auto scrollbar-none">
              {tabTitles.map((t, idx) => {
                const active = activeTab === idx;
                return (
                  <button
                    key={idx}
                    onClick={() => setActiveTab(idx)}
                    className={`px-2.5 py-1 rounded-md text-xs font-bold whitespace-nowrap transition border ${
                      active
                        ? idx === 0
                          ? 'bg-purple-700 text-white border-amber-400 shadow-md shadow-purple-950/80'
                          : idx === 9
                          ? 'bg-amber-600 text-black border-amber-300 font-extrabold shadow-md shadow-amber-950/70'
                          : 'bg-red-600 text-white border-amber-400 shadow-md shadow-red-950/60'
                        : 'bg-[#11131a] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
                    }`}
                  >
                    {t.label}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* MAIN VIEW: ENTERPRISE B2B PORTAL vs 3D CARD vs 9 REALMS STOREFRONT */}
        {portalMode === 'enterprise' ? (
          <main className="p-3 sm:p-4 flex-1 overflow-y-auto">
            <EnterpriseB2BPortal
              onOpenIntakeWithTrack={(track) => {
                const syntheticService: ServiceItem = {
                  sku: track.code,
                  page: 1,
                  cardIndex: 1,
                  realmId: 'asgard',
                  runePill: track.badge,
                  meaning: track.targetClient,
                  title: track.title,
                  price: track.rateDescription.split('(')[0].trim(),
                  priceSub: track.rateUnit,
                  priceValue: track.rateValueMonthly,
                  isRecurring: true,
                  attrs: [
                    ['Client Track', track.targetClient.slice(0, 16)],
                    ['Execution', '100% Remote Async'],
                    ['Monthly Retainer', `$${track.rateValueMonthly.toLocaleString()}`]
                  ],
                  bullets: track.deliverables30_60_90[0].deliverables,
                  badge: track.roleTitle,
                  specTag: `EIN: ${ENTERPRISE_ENTITY.ein} • ${track.code}`
                };
                setIntakePreServices([syntheticService]);
                setShowIntakeModal(true);
              }}
              onOpenBooking={(trackCode) => {
                setBookingPreSku(trackCode);
                setShowBookingModal(true);
              }}
              onOpenSignModal={(track) => {
                setSignDocTitle(track.title);
                setSignDocCode(track.code);
                setSignContractValue(track.rateDescription.split('(')[0].trim());
                setShowSignModal(true);
              }}
              onOpenDispatch={(trackCode) => {
                setDispatchInitialSubject(`Out-of-Band Dispatch: ${trackCode}`);
                setShowDispatchModal(true);
              }}
              onSwitchToStorefront={() => setPortalMode('storefront')}
            />
          </main>
        ) : activeTab === 0 ? (
          <div className="flex-1 flex flex-col p-3 overflow-hidden bg-[#060609] select-none touch-none">
            <div className="flex items-center justify-between pb-2 text-xs">
              <div className="flex items-center space-x-2">
                <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-amber-500/10 text-amber-300 border border-amber-500/30 uppercase font-bold">
                  OMNI-DIRECTIONAL 3D CARD
                </span>
                <span className="text-[11px] text-[#94a3b8] hidden sm:inline">
                  Drag in any direction • Double-click to flip
                </span>
              </div>
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => {
                    setDispatchInitialSubject('Inquiry via 3D Business Card');
                    setShowDispatchModal(true);
                  }}
                  className="px-2.5 py-1 rounded bg-[#11131a] hover:bg-[#1f2330] text-cyan-300 border border-cyan-500/40 text-[11px] font-bold flex items-center space-x-1 transition shadow-sm"
                  title="Direct Redundant Failover Dispatch (Google Chat Relay)"
                >
                  <Radio className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
                  <span>Dispatch</span>
                </button>
                <button
                  onClick={handleShareCard}
                  className="px-2.5 py-1 rounded bg-[#11131a] hover:bg-[#1f2330] text-purple-300 border border-purple-500/40 text-[11px] font-bold flex items-center space-x-1 transition shadow-sm"
                  title="Copy direct link to 3D Business Card"
                >
                  <Share2 className="w-3.5 h-3.5 text-purple-400" />
                  <span>{copiedShare ? 'Copied Link!' : 'Share'}</span>
                </button>
                <button
                  onClick={() => setShowCardFullscreen(true)}
                  className="px-2 py-1 rounded bg-[#11131a] hover:bg-[#1f2330] text-amber-300 border border-[#232634] text-[11px] font-bold flex items-center space-x-1 transition"
                >
                  <Maximize2 className="w-3.5 h-3.5" />
                  <span>Fullscreen</span>
                </button>
                <button
                  onClick={() => setActiveTab(1)}
                  className="px-2 py-1 rounded bg-red-600 hover:bg-red-700 text-white text-[11px] font-bold flex items-center space-x-1 shadow-md transition"
                >
                  <span>Services</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            {/* Standalone 3D Card Stage (Zero scrollbars, fits viewport cleanly) */}
            <div className="flex-1 w-full rounded-2xl overflow-hidden border border-[#232634] bg-[#060609] relative shadow-2xl">
              {/* Subtle semi-transparent animated helper overlay targeting iframe (fades after 3s) */}
              {showCardGuide && (
                <div
                  onClick={() => {
                    setCardGuideFading(true);
                    setTimeout(() => setShowCardGuide(false), 300);
                  }}
                  className={`absolute top-3 left-1/2 -translate-x-1/2 z-20 pointer-events-auto cursor-pointer transition-all duration-700 ${
                    cardGuideFading ? 'opacity-0 -translate-y-2' : 'opacity-100 translate-y-0'
                  }`}
                  aria-label="3D card interaction instructions"
                >
                  <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#060609]/90 backdrop-blur-md border border-amber-400/80 shadow-2xl shadow-black text-amber-200 text-xs font-bold animate-pulse">
                    <span className="text-cyan-400 font-mono text-sm">⇄</span>
                    <span>Drag in any direction to rotate in 3D • Double-click to flip</span>
                  </div>
                </div>
              )}

              {/* Floating Quick-Share Button */}
              <button
                onClick={handleShareCard}
                className="absolute bottom-3 right-3 z-20 px-3 py-1.5 rounded-full bg-[#060609]/90 backdrop-blur-md border border-purple-500/60 shadow-xl text-purple-200 hover:text-white text-xs font-bold flex items-center space-x-1.5 transition active:scale-95"
                title="Share 3D Card Link"
              >
                <Share2 className="w-3.5 h-3.5 text-purple-400" />
                <span>{copiedShare ? '✓ Link Copied' : 'Share 3D Card'}</span>
              </button>

              <iframe
                src={cardAssetPath}
                title="Caleb Holcomb 3D Omni-Directional Business Card"
                className="w-full h-full border-0 block"
                allow="accelerometer; gyroscope"
              />
            </div>

            {/* Bottom Trust & Navigation Footer */}
            <div className="pt-2 flex items-center justify-between text-[11px] text-[#94a3b8] shrink-0">
              <span>🔒 <strong>Verified Identity:</strong> CALEB TR HOLCOMB (d/b/a TYR) • CP 575 G</span>
              <button
                onClick={() => setActiveTab(1)}
                className="text-amber-400 hover:text-amber-300 font-bold flex items-center space-x-1"
              >
                <span>Browse 9 Realms Services</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ) : (
          /* VIEW 2: SCROLLABLE STOREFRONT & CATALOG PAGES */
          <main className="p-4 space-y-4 flex-1 overflow-y-auto">
            {/* Real-Time Client Work Order Intake Tracking Banner */}
            <ClientOrderTrackerBanner
              onOpenIntake={() => {
                setIntakePreServices([]);
                setShowIntakeModal(true);
              }}
            />

            {/* Executive Hero Banner */}
            <div className="rounded-xl bg-gradient-to-br from-[#160a0d] via-[#0b0c12] to-[#171108] border border-red-900/50 p-4 shadow-xl relative overflow-hidden">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-[#232634]/70">
                <div className="space-y-1">
                  <div className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-red-950/80 text-red-400 border border-red-800/40">
                    <Sparkles className="w-3 h-3 text-amber-400" />
                    <span>CALEB TR HOLCOMB d/b/a TYR</span>
                  </div>
                  <h2 className="text-base sm:text-lg font-black text-white">
                    Flat-Rate Tech, Email Shield & Admin Services
                  </h2>
                  <p className="text-xs text-[#cbd5e1] leading-relaxed max-w-xl">
                    Custom high-speed websites, private anti-spoof email, 3-doc fillable contracts, and statement audit care plans. Complete master file handover with $0 ongoing platform lock-in.
                  </p>
                </div>

                <div className="flex flex-wrap sm:flex-col gap-2 shrink-0">
                  <button
                    onClick={() => setActiveTab(0)}
                    className="px-3 py-1.5 rounded-lg bg-purple-950/80 hover:bg-purple-900/80 text-purple-200 border border-purple-500/40 font-bold text-xs flex items-center justify-center space-x-1.5 transition"
                  >
                    <span>🪪 View 3D Card</span>
                  </button>
                  <button
                    onClick={() => {
                      setIntakePreServices([]);
                      setShowIntakeModal(true);
                    }}
                    className="px-3 py-1.5 rounded-lg bg-gradient-to-r from-red-600 to-amber-600 hover:from-red-500 hover:to-amber-500 text-white font-extrabold text-xs flex items-center justify-center space-x-1.5 shadow-md shadow-red-900/50 transition"
                  >
                    <FileText className="w-3.5 h-3.5" />
                    <span>Submit Project Intake</span>
                  </button>
                  <button
                    onClick={() => setShowBookingModal(true)}
                    className="px-3 py-1.5 rounded-lg bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 font-bold text-xs flex items-center justify-center space-x-1.5 transition"
                  >
                    <Calendar className="w-3.5 h-3.5 text-amber-400" />
                    <span>Schedule Consult Call</span>
                  </button>
                  <div className="flex gap-1.5">
                    <button
                      onClick={() => setActiveTab(10)}
                      className="flex-1 px-2 py-1.5 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-cyan-300 border border-[#232634] font-bold text-[11px] flex items-center justify-center space-x-1 transition"
                    >
                      <span>⚖️ Tier Matrix</span>
                    </button>
                    <button
                      onClick={() => setActiveTab(11)}
                      className="flex-1 px-2 py-1.5 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-purple-300 border border-[#232634] font-bold text-[11px] flex items-center justify-center space-x-1 transition"
                    >
                      <span>🛡️ Retainers</span>
                    </button>
                  </div>
                </div>
              </div>

              {/* Entity Attributes Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-3">
                <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] border-l-2 border-l-red-600">
                  <span className="text-[9px] uppercase font-bold text-[#94a3b8] block">LEGAL ENTITY</span>
                  <span className="text-xs font-bold text-[#e2e8f0] truncate block">CALEB TR HOLCOMB</span>
                </div>
                <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] border-l-2 border-l-amber-600">
                  <span className="text-[9px] uppercase font-bold text-[#94a3b8] block">NAICS CODES</span>
                  <span className="text-xs font-bold text-[#e2e8f0] truncate block">541512 • 541611</span>
                </div>
                <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] border-l-2 border-l-emerald-600">
                  <span className="text-[9px] uppercase font-bold text-[#94a3b8] block">SERVICE AREA</span>
                  <span className="text-xs font-bold text-[#e2e8f0] truncate block">Northwest AL / US</span>
                </div>
                <div className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] border-l-2 border-l-purple-600">
                  <span className="text-[9px] uppercase font-bold text-[#94a3b8] block">FLAT RATES</span>
                  <span className="text-xs font-bold text-[#e2e8f0] truncate block">$65 – $495 Flat</span>
                </div>
              </div>
            </div>

            {/* Page 1: Midgard, Asgard, Vanaheim */}
            {activeTab === 1 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-amber-400 px-1 font-semibold">
                  <span>ᛗ Midgard • ᛉ Asgard • ᚱ Vanaheim</span>
                  <span className="text-[#94a3b8]">Public Presence & Intake</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {SERVICES.filter((s) => s.page === 1).map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 2: Alfheim, Nidavellir, Muspelheim */}
            {activeTab === 2 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-amber-400 px-1 font-semibold">
                  <span>ᚨ Alfheim • ᚷ Nidavellir • ᚲ Muspelheim</span>
                  <span className="text-[#94a3b8]">Resumes, E-Sign & Buyer Guide</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {SERVICES.filter((s) => s.page === 2).map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 3: Svartalfheim, Niflheim, Jotunheim */}
            {activeTab === 3 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-amber-400 px-1 font-semibold">
                  <span>ᚠ Svartalfheim • ᛟ Niflheim • ᚦ Jotunheim</span>
                  <span className="text-[#94a3b8]">Budgets, Vaults & Disputes</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {SERVICES.filter((s) => s.page === 3).map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 4: Tech Care Plans */}
            {activeTab === 4 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-amber-400 px-1 font-semibold">
                  <span>Monthly Technology & Domain Caretaker Plans</span>
                  <span className="text-[#94a3b8]">Continuous Shielding</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {SERVICES.filter((s) => s.page === 4).map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 5: Monthly Expense Optimization & Flagship Retainer */}
            {activeTab === 5 && (
              <div className="space-y-4">
                {/* Flagship Bundle Banner */}
                <div className="rounded-xl bg-gradient-to-r from-red-950/70 via-[#10121b] to-amber-950/70 border border-amber-500/40 p-4 shadow-xl space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <Sparkles className="w-5 h-5 text-amber-400" />
                      <span className="text-xs font-black uppercase text-amber-400 tracking-wider">
                        FLAGSHIP RET-ALL-07
                      </span>
                    </div>
                    <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/40">
                      {FLAGSHIP_BUNDLE.threeRealmSavings}
                    </span>
                  </div>

                  <div>
                    <h3 className="text-base font-bold text-white">{FLAGSHIP_BUNDLE.title}</h3>
                    <p className="text-xs text-[#cbd5e1]">
                      Full stewardship across Web (Midgard), Email Watchtower (Asgard), Forms (Vanaheim), and Blank Vault (Niflheim).
                    </p>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                    <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] border-l-2 border-l-amber-400">
                      <span className="text-[10px] text-amber-400 font-bold block uppercase">MONTHLY STEWARD</span>
                      <span className="text-sm font-extrabold text-white">{FLAGSHIP_BUNDLE.stewardPrice}</span>
                      <span className="text-[10px] text-[#94a3b8] block">Personal $165 / Biz $215</span>
                    </div>
                    <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] border-l-2 border-l-purple-400">
                      <span className="text-[10px] text-purple-400 font-bold block uppercase">3-REALM LAUNCH</span>
                      <span className="text-sm font-extrabold text-white">{FLAGSHIP_BUNDLE.threeRealmLaunch}</span>
                      <span className="text-[10px] text-[#94a3b8] block">Web + Domain + Intake Standee</span>
                    </div>
                  </div>

                  <div className="space-y-1">
                    {FLAGSHIP_BUNDLE.features.map((f, i) => (
                      <div key={i} className="flex items-start text-xs text-[#cbd5e1] space-x-1.5">
                        <CheckCircle className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                        <span>{f}</span>
                      </div>
                    ))}
                  </div>

                  <button
                    onClick={() => openInquiry(null)}
                    className="w-full py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs shadow-lg transition"
                  >
                    Inquire for Retainer Availability
                  </button>
                </div>

                {/* Subscriptions */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {SERVICES.filter((s) => s.page === 5).map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 6: Bifröst 3-Step Workflow */}
            {activeTab === 6 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-amber-400 px-1 font-semibold">
                  <span>ᚱ Bifröst 3-Step Ordering & Zero-Custody Protocol</span>
                  <span className="text-[#94a3b8]">24–48h Delivery</span>
                </div>

                <div className="space-y-3">
                  {WORKFLOW_STEPS.map((step) => {
                    const borderCol =
                      step.step === 1 ? 'border-l-red-600' : step.step === 2 ? 'border-l-amber-500' : 'border-l-emerald-500';
                    return (
                      <div
                        key={step.step}
                        className={`rounded-xl bg-[#11131a] border border-[#232634] p-4 border-l-4 ${borderCol} space-y-2`}
                      >
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-mono font-bold text-amber-400">STEP 0{step.step}</span>
                          <span className="text-[#94a3b8]">{step.realm}</span>
                        </div>
                        <h4 className="text-sm font-bold text-white">{step.title}</h4>
                        <p className="text-xs text-[#cbd5e1] leading-relaxed">{step.description}</p>
                        <div className="pt-2">
                          <button
                            onClick={() => {
                              if (step.step === 1) setActiveTab(7);
                              if (step.step === 2) setShowZeroCustodyModal(true);
                              if (step.step === 3) handleDial();
                            }}
                            className="px-3 py-1.5 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-amber-400 border border-[#232634] text-xs font-bold transition"
                          >
                            {step.actionLabel}
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Page 7: All Realms Catalog with Search & Filter */}
            {activeTab === 7 && (
              <div className="space-y-4">
                {/* Search & Realm Chips */}
                <div className="space-y-2">
                  <div className="relative">
                    <Search className="w-4 h-4 text-[#94a3b8] absolute left-3 top-3" />
                    <input
                      type="text"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      placeholder="Search 15 services, SKUs, or features..."
                      className="w-full pl-9 pr-4 py-2 rounded-lg bg-[#090a0f] border border-[#232634] text-xs text-white placeholder-[#94a3b8] focus:outline-none focus:border-red-600 transition"
                    />
                  </div>

                  <div className="flex items-center space-x-1.5 overflow-x-auto pb-1 scrollbar-none text-xs">
                    <button
                      onClick={() => setSelectedRealmFilter(null)}
                      className={`px-2.5 py-1 rounded-md font-bold whitespace-nowrap transition ${
                        selectedRealmFilter === null
                          ? 'bg-amber-500 text-black'
                          : 'bg-[#11131a] text-[#94a3b8] hover:text-white'
                      }`}
                    >
                      All 9 Realms
                    </button>
                    {Object.values(REALMS).map((r) => (
                      <button
                        key={r.id}
                        onClick={() => setSelectedRealmFilter(selectedRealmFilter === r.id ? null : r.id)}
                        className={`px-2.5 py-1 rounded-md font-bold whitespace-nowrap transition border ${
                          selectedRealmFilter === r.id
                            ? 'border-amber-400 bg-red-950 text-white'
                            : 'border-[#232634] bg-[#11131a] text-[#94a3b8] hover:text-white'
                        }`}
                      >
                        {r.rune} {r.name}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Services Grid */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  {filteredServices.map((service) => (
                    <ServiceCardComponent
                      key={service.sku}
                      service={service}
                      onInquire={() => openInquiry(service)}
                      onToggleCart={() => toggleCart(service)}
                      isInCart={cart.some((c) => c.sku === service.sku)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Page 8: Estimate Calculator & Cart */}
            {activeTab === 8 && (
              <div className="space-y-4">
                <div className="rounded-xl bg-[#11131a] border border-amber-500/40 p-4 space-y-4 shadow-xl">
                  <div className="flex items-center justify-between pb-3 border-b border-[#232634]">
                    <div>
                      <h3 className="text-sm font-extrabold text-white">CUSTOM QUOTE ESTIMATE BUILDER</h3>
                      <p className="text-xs text-[#94a3b8]">Select services across realms to calculate project packages</p>
                    </div>
                    {cart.length > 0 && (
                      <button
                        onClick={clearCart}
                        className="text-xs text-red-400 hover:text-red-300 font-bold flex items-center space-x-1"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Clear All</span>
                      </button>
                    )}
                  </div>

                  {cart.length === 0 ? (
                    <div className="text-center py-8 text-xs text-[#94a3b8] space-y-2">
                      <ShoppingCart className="w-8 h-8 mx-auto text-[#94a3b8]/40" />
                      <p>No services added to your estimate yet.</p>
                      <p className="text-[11px] text-[#cbd5e1]">
                        Click <span className="text-amber-400 font-bold">+ Estimate</span> on any service card in Pages 1–5 or "All Realms".
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      <div className="space-y-2">
                        {cart.map((item) => (
                          <div
                            key={item.sku}
                            className="bg-[#090a0f] p-3 rounded-lg border border-[#232634] flex items-center justify-between text-xs"
                          >
                            <div>
                              <span className="font-bold text-white block">{item.title}</span>
                              <span className="font-mono text-[10px] text-[#94a3b8]">
                                {item.sku} • {item.priceSub}
                              </span>
                            </div>
                            <div className="flex items-center space-x-3">
                              <span className="font-bold text-amber-400">{item.price}</span>
                              <button
                                onClick={() => toggleCart(item)}
                                className="text-red-400 hover:text-red-300 p-1"
                              >
                                <X className="w-4 h-4" />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Totals */}
                      <div className="bg-[#0c0d13] p-3 rounded-lg border border-[#232634] space-y-1.5 text-xs">
                        <div className="flex justify-between text-[#cbd5e1]">
                          <span>One-Time Builds:</span>
                          <span className="font-bold text-white">${cartOneTimeTotal}</span>
                        </div>
                        <div className="flex justify-between text-[#cbd5e1]">
                          <span>Monthly Recurring Care:</span>
                          <span className="font-bold text-white">${cartMonthlyTotal} / mo</span>
                        </div>
                        <div className="pt-2 border-t border-[#232634] flex justify-between font-extrabold text-sm text-amber-400">
                          <span>Combined Initial Investment:</span>
                          <span>${cartOneTimeTotal + cartMonthlyTotal}</span>
                        </div>
                      </div>

                      {/* Optional AI Scope Analysis Button in Cart */}
                      {aiEnabled ? (
                        <div className="space-y-2 pt-1">
                          <button
                            onClick={handleGenerateScopeSummary}
                            disabled={aiScopeLoading}
                            className="w-full py-2 rounded-lg bg-gradient-to-r from-amber-600 to-amber-700 hover:from-amber-500 hover:to-amber-600 text-black font-extrabold text-xs flex items-center justify-center space-x-1.5 shadow-md transition disabled:opacity-50"
                          >
                            <Sparkles className="w-3.5 h-3.5 text-black" />
                            <span>
                              {aiScopeLoading ? 'Consulting Runic Scope...' : '✦ Generate AI Scope Analysis & Proposal Note'}
                            </span>
                          </button>

                          {aiScopeSummary && (
                            <div className="p-3 rounded-lg bg-[#090a0f] border border-amber-500/50 text-xs text-[#cbd5e1] space-y-2 animate-fadeIn">
                              <div className="flex items-center justify-between text-amber-300 font-bold border-b border-[#232634] pb-1">
                                <span className="flex items-center space-x-1">
                                  <span>ᛏ ᚠ</span>
                                  <span>Executive Scope Briefing</span>
                                </span>
                                <button
                                  onClick={() => setAiScopeSummary(null)}
                                  className="text-[#94a3b8] hover:text-white"
                                >
                                  <X className="w-3.5 h-3.5" />
                                </button>
                              </div>
                              <p className="whitespace-pre-line leading-relaxed text-[11px] text-[#e2e8f0]">
                                {aiScopeSummary}
                              </p>
                            </div>
                          )}
                        </div>
                      ) : (
                        <div className="p-2 rounded-lg bg-[#090a0f] border border-[#232634] text-[10px] text-[#94a3b8] flex items-center justify-between">
                          <span className="flex items-center space-x-1.5">
                            <Shield className="w-3 h-3 text-emerald-400" />
                            <span>Strict Zero-AI mode active (AI Scope Review disabled)</span>
                          </span>
                          <button
                            onClick={() => setShowSettingsModal(true)}
                            className="text-amber-400 hover:underline font-bold"
                          >
                            Settings
                          </button>
                        </div>
                      )}

                      <div className="space-y-2 pt-1">
                        <button
                          onClick={() => {
                            setIntakePreServices(cart);
                            setShowIntakeModal(true);
                          }}
                          className="w-full py-3 rounded-lg bg-gradient-to-r from-amber-500 via-amber-600 to-red-600 hover:from-amber-400 hover:to-red-500 text-white font-black text-xs flex items-center justify-center space-x-2 shadow-xl shadow-amber-950/80 transition active:scale-[0.98]"
                        >
                          <FileText className="w-4 h-4 text-amber-200" />
                          <span>Submit Work Order Intake (${cartOneTimeTotal + cartMonthlyTotal})</span>
                        </button>

                        <button
                          onClick={() => {
                            setSignDocTitle('Small Business Service Estimate');
                            setSignDocCode(`EST-${Date.now().toString().slice(-6)}`);
                            setSignContractValue(`$${cartOneTimeTotal + cartMonthlyTotal} USD`);
                            setShowSignModal(true);
                          }}
                          className="w-full py-2 rounded-lg bg-[#0d1626] hover:bg-[#14233c] text-cyan-300 font-bold text-xs flex items-center justify-center space-x-2 border border-cyan-500/40 transition"
                        >
                          <FileText className="w-3.5 h-3.5 text-cyan-400" />
                          <span>Review &amp; Electronically Sign SOW (ESIGN)</span>
                        </button>

                        <button
                          onClick={() => openInquiry(null)}
                          className="w-full py-2 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-[#cbd5e1] font-semibold text-xs flex items-center justify-center space-x-2 border border-[#232634] transition"
                        >
                          <Mail className="w-3.5 h-3.5 text-[#94a3b8]" />
                          <span>Or Email Direct Inquiry to Caleb</span>
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Page 9: AI Technical Scope Advisor (Optional - Tyr & Freyja Oracle) */}
            {activeTab === 9 && (
              <div className="space-y-4">
                <div className="rounded-xl bg-[#11131a] border border-amber-500/40 p-4 space-y-4 shadow-xl">
                  {/* Top Header */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-[#232634]">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="px-1.5 py-0.5 rounded text-[10px] font-extrabold bg-amber-500/20 text-amber-300 border border-amber-500/40 uppercase">
                          ᛏ TIWAZ &bull; ᚠ FEHU ORACLE
                        </span>
                        <span className="text-xs font-mono text-emerald-400">Zero-Custody</span>
                      </div>
                      <h3 className="text-base font-black text-white mt-1">
                        TYR &amp; FREYJA TECHNICAL SCOPE ADVISOR
                      </h3>
                      <p className="text-xs text-[#94a3b8]">
                        Architectural guidance, anti-spoof scoping, and flat-rate service recommendations.
                      </p>
                    </div>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => setShowSettingsModal(true)}
                        className="px-2.5 py-1 rounded bg-[#090a0f] hover:bg-[#1a1c26] text-amber-300 border border-[#232634] text-xs font-bold flex items-center space-x-1 transition"
                      >
                        <Sliders className="w-3 h-3" />
                        <span>Settings</span>
                      </button>
                    </div>
                  </div>

                  {/* If AI is disabled: show zero-AI privacy shield banner with toggle */}
                  {!aiEnabled ? (
                    <div className="p-6 rounded-xl bg-[#090a0f] border border-emerald-500/30 text-center space-y-3">
                      <div className="w-12 h-12 mx-auto rounded-full bg-emerald-950/80 border border-emerald-500/50 flex items-center justify-center text-emerald-400">
                        <Shield className="w-6 h-6" />
                      </div>
                      <div className="space-y-1">
                        <h4 className="text-sm font-extrabold text-white">
                          AI Technical Advisor is Currently Disabled
                        </h4>
                        <p className="text-xs text-[#cbd5e1] max-w-md mx-auto leading-relaxed">
                          We recognize that many clients prefer strict deterministic software with zero external AI processing. In accordance with sovereign privacy, all AI models remain inactive.
                        </p>
                        <p className="text-[11px] text-[#94a3b8]">
                          All 15 flat-rate catalog items, direct phone calling, encrypted email, and 3D card work 100% without AI.
                        </p>
                      </div>

                      <div className="pt-2 flex justify-center gap-3">
                        <button
                          onClick={toggleAiEnabled}
                          className="px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs flex items-center space-x-1.5 shadow-lg transition"
                        >
                          <Sparkles className="w-4 h-4" />
                          <span>Enable AI Technical Advisor (Optional)</span>
                        </button>
                        <button
                          onClick={() => setActiveTab(1)}
                          className="px-4 py-2 rounded-lg bg-[#11131a] hover:bg-[#1f2330] text-[#cbd5e1] border border-[#232634] text-xs font-bold transition"
                        >
                          Browse Services Directly
                        </button>
                      </div>
                    </div>
                  ) : (
                    /* AI is Enabled: Interactive Consultation Console */
                    <div className="space-y-3">
                      {/* Suggestion Chips */}
                      <div className="space-y-1.5">
                        <span className="text-[10px] uppercase font-bold text-[#94a3b8] tracking-wider block">
                          Suggested Technical Inquiries:
                        </span>
                        <div className="flex flex-wrap gap-1.5">
                          {[
                            'Recommend web & anti-spoof email setup',
                            'Audit monthly recurring SaaS & software expenses',
                            'Build me a 3-doc fillable contract package',
                            'Explain zero-custody 0-day statement purge',
                            'What do the Tyr (ᛏ) and Freyja (ᚠ) runes mean?'
                          ].map((chip) => (
                            <button
                              key={chip}
                              onClick={() => handleSendAiMessage(chip)}
                              className="px-2.5 py-1 rounded-full bg-[#090a0f] hover:bg-amber-950/40 text-amber-300 border border-amber-500/30 text-[11px] font-medium transition"
                            >
                              ✦ {chip}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Chat Messages Log */}
                      <div className="bg-[#08090e] border border-[#232634] rounded-xl p-3.5 h-80 overflow-y-auto space-y-3">
                        {aiMessages.map((msg) => {
                          const isUser = msg.sender === 'user';
                          return (
                            <div
                              key={msg.id}
                              className={`flex flex-col ${isUser ? 'items-end' : 'items-start'} space-y-1`}
                            >
                              <div className="flex items-center space-x-1 text-[10px] text-[#94a3b8]">
                                <span>{isUser ? 'You' : 'ᛏ ᚠ Oracle'}</span>
                                <span>&bull;</span>
                                <span>{msg.timestamp}</span>
                              </div>

                              <div
                                className={`p-3 rounded-xl max-w-[92%] sm:max-w-[85%] text-xs leading-relaxed ${
                                  isUser
                                    ? 'bg-red-950/70 border border-red-700/60 text-white rounded-tr-none'
                                    : 'bg-[#12141c] border border-amber-500/40 text-[#f1f5f9] rounded-tl-none shadow-md'
                                }`}
                              >
                                <p className="whitespace-pre-line text-[11px] sm:text-xs">
                                  {msg.text}
                                </p>

                                {/* If response recommends specific catalog SKUs, render interactive cards */}
                                {msg.recommendedSkus && msg.recommendedSkus.length > 0 && (
                                  <div className="mt-3 pt-2 border-t border-[#232634] space-y-1.5">
                                    <span className="text-[10px] uppercase font-bold text-amber-400 block tracking-wider">
                                      Matching Flat-Rate Services:
                                    </span>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-1.5">
                                      {msg.recommendedSkus.map((sku) => {
                                        const found = SERVICES.find((s) => s.sku === sku);
                                        if (!found) return null;
                                        const isInCart = cart.some((c) => c.sku === found.sku);
                                        return (
                                          <div
                                            key={sku}
                                            className="bg-[#090a0f] p-2 rounded-lg border border-[#232634] flex items-center justify-between"
                                          >
                                            <div className="truncate mr-2">
                                              <span className="text-[10px] font-bold text-white block truncate">
                                                {found.title}
                                              </span>
                                              <span className="text-[9px] text-amber-400 font-mono">
                                                {found.sku} &bull; {found.price}
                                              </span>
                                            </div>
                                            <button
                                              onClick={() => toggleCart(found)}
                                              className={`px-2 py-1 rounded text-[10px] font-bold shrink-0 transition ${
                                                isInCart
                                                  ? 'bg-emerald-950 text-emerald-400 border border-emerald-500'
                                                  : 'bg-red-600 hover:bg-red-700 text-white'
                                              }`}
                                            >
                                              {isInCart ? 'In Cart' : '+ Estimate'}
                                            </button>
                                          </div>
                                        );
                                      })}
                                    </div>
                                  </div>
                                )}
                              </div>
                            </div>
                          );
                        })}

                        {aiLoading && (
                          <div className="flex items-center space-x-2 text-xs text-amber-400 bg-[#12141c] p-2.5 rounded-lg border border-amber-500/30 max-w-xs">
                            <Sparkles className="w-4 h-4 animate-spin text-amber-400" />
                            <span>Consulting Tyr &amp; Freyja runes...</span>
                          </div>
                        )}
                      </div>

                      {/* Input Bar */}
                      <form
                        onSubmit={(e) => {
                          e.preventDefault();
                          handleSendAiMessage();
                        }}
                        className="flex items-center gap-2"
                      >
                        <input
                          type="text"
                          value={aiInput}
                          onChange={(e) => setAiInput(e.target.value)}
                          placeholder="Ask about web architecture, anti-spoofing, contracts, or audits..."
                          className="flex-1 bg-[#090a0f] border border-[#232634] focus:border-amber-400 rounded-lg px-3 py-2 text-xs text-white placeholder-[#64748b] outline-none"
                        />
                        <button
                          type="submit"
                          disabled={!aiInput.trim() || aiLoading}
                          className="px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-600 disabled:opacity-50 text-black font-extrabold text-xs flex items-center space-x-1 shadow-md transition"
                        >
                          <Send className="w-3.5 h-3.5" />
                          <span>Consult</span>
                        </button>
                      </form>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Page 10: Service Tier Matrix & Recommendations */}
            {activeTab === 10 && (
              <ServiceTierMatrix
                onSelectTierSkus={(skus) => {
                  const items = SERVICES.filter(s => skus.includes(s.sku));
                  setCart(prev => {
                    const existingSkus = new Set(prev.map(p => p.sku));
                    const newItems = items.filter(it => !existingSkus.has(it.sku));
                    return [...prev, ...newItems];
                  });
                  setActiveTab(8); // Switch to estimate cart
                }}
                onOpenIntakeWithSkus={(skus) => {
                  const items = SERVICES.filter(s => skus.includes(s.sku));
                  setIntakePreServices(items);
                  setShowIntakeModal(true);
                }}
                cartSkus={cart.map(c => c.sku)}
              />
            )}

            {/* Page 11: Recurring Care & Retainer Subscriptions */}
            {activeTab === 11 && (
              <ClientRetainerPortal
                onOpenIntakeWithPlan={(sku) => {
                  const item = SERVICES.find(s => s.sku === sku);
                  setIntakePreServices(item ? [item] : []);
                  setShowIntakeModal(true);
                }}
                onOpenBooking={(sku) => {
                  setBookingPreSku(sku);
                  setShowBookingModal(true);
                }}
              />
            )}

            {/* Pager Indicator & Stepper (For Pages 1–6) */}
            {activeTab >= 1 && activeTab <= 6 && (
              <div className="bg-[#0c0d13] p-2.5 rounded-xl border border-[#232634] flex items-center justify-between text-xs">
                <button
                  onClick={() => setActiveTab(activeTab > 1 ? activeTab - 1 : 6)}
                  className="px-2.5 py-1 rounded-md bg-[#11131a] hover:bg-[#1a1c26] text-[#cbd5e1] flex items-center space-x-1 border border-[#232634] transition"
                >
                  <ChevronLeft className="w-3.5 h-3.5" />
                  <span>Prev</span>
                </button>
                <div className="text-center">
                  <span className="font-bold text-amber-400">Page {activeTab} of 6</span>
                  <span className="text-[10px] text-[#94a3b8] block">Swipe or tap tabs to navigate</span>
                </div>
                <button
                  onClick={() => setActiveTab(activeTab < 6 ? activeTab + 1 : 1)}
                  className="px-2.5 py-1 rounded-md bg-[#11131a] hover:bg-[#1a1c26] text-[#cbd5e1] flex items-center space-x-1 border border-[#232634] transition"
                >
                  <span>Next</span>
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            )}

            {/* Footer */}
            <footer className="pt-4 border-t border-[#232634]/60 text-center space-y-1 text-[11px] text-[#94a3b8]">
              <p className="font-semibold text-white">
                © {ORGANIZATION.name} • {ORGANIZATION.legalName}
              </p>
              <p>NAICS 541512 • 541611 • Northwest Alabama & Nationwide</p>
              <div className="flex items-center justify-center space-x-2 text-[10px] text-[#cbd5e1]">
                <span>Strict Zero-Custody Privacy Rule: 0 raw statements kept. 24h purge guarantee.</span>
                <button
                  onClick={() => setShowAdminPasscodeModal(true)}
                  className="text-[#334155] hover:text-amber-400 transition p-0.5 rounded"
                  title="Sovereign Intake Vault"
                  aria-label="Admin Access"
                >
                  <Lock className="w-2.5 h-2.5" />
                </button>
              </div>
            </footer>
          </main>
        )}

        {/* Floating Action Bar (Only shown on catalog tabs) */}
        {activeTab !== 0 && (
          <div className="fixed bottom-3 right-3 z-30 flex items-center space-x-2">
            <button
              onClick={() => setShowBookingModal(true)}
              className="flex items-center space-x-1.5 px-3.5 py-3 rounded-full bg-[#11131a] text-amber-300 font-bold text-xs shadow-xl shadow-black/80 border border-amber-500/60 hover:scale-105 active:scale-95 transition backdrop-blur-md"
              title="Schedule Consultation Call"
            >
              <Calendar className="w-4 h-4 text-amber-400" />
              <span className="hidden sm:inline">Book Call</span>
            </button>
            <button
              onClick={handleDial}
              className="flex items-center space-x-2 px-4 py-3 rounded-full bg-gradient-to-r from-red-600 to-red-800 text-white font-bold text-xs shadow-xl shadow-red-950/80 border border-amber-400 hover:scale-105 active:scale-95 transition"
            >
              <Phone className="w-4 h-4 text-amber-300" />
              <span>Call Consult</span>
            </button>
          </div>
        )}

        {/* 3D Card Fullscreen Modal */}
        {showCardFullscreen && (
          <div className="fixed inset-0 bg-black/95 backdrop-blur-md z-50 flex flex-col p-2 sm:p-4">
            <div className="flex items-center justify-between pb-2 px-2 text-white border-b border-[#232634]">
              <div className="flex items-center space-x-2">
                <span className="text-lg">🪪</span>
                <span className="text-xs font-bold text-amber-300">
                  OMNI-DIRECTIONAL 3D CARD — FULLSCREEN IMMERSION
                </span>
              </div>
              <div className="flex items-center space-x-2">
                <button
                  onClick={handleShareCard}
                  className="px-2.5 py-1 rounded bg-[#11131a] hover:bg-[#1f2330] text-purple-300 border border-purple-500/40 text-xs font-bold flex items-center space-x-1 transition"
                  title="Copy link to 3D Card"
                >
                  <Share2 className="w-3.5 h-3.5 text-purple-400" />
                  <span>{copiedShare ? 'Copied Link!' : 'Share'}</span>
                </button>
                <button
                  onClick={() => setShowCardFullscreen(false)}
                  className="p-1.5 rounded-lg bg-[#11131a] hover:bg-red-900 text-white border border-[#232634] transition"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="flex-1 w-full mt-2 rounded-xl overflow-hidden border border-[#232634] bg-[#060609] relative">
              {showCardGuide && (
                <div
                  onClick={() => {
                    setCardGuideFading(true);
                    setTimeout(() => setShowCardGuide(false), 300);
                  }}
                  className={`absolute top-4 left-1/2 -translate-x-1/2 z-30 pointer-events-auto cursor-pointer transition-all duration-700 ${
                    cardGuideFading ? 'opacity-0 -translate-y-2' : 'opacity-100 translate-y-0'
                  }`}
                >
                  <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#060609]/90 backdrop-blur-md border border-amber-400/80 shadow-2xl shadow-black text-amber-200 text-xs font-bold animate-pulse">
                    <span className="text-cyan-400 font-mono text-sm">⇄</span>
                    <span>Drag in any direction to rotate in 3D • Double-click to flip</span>
                  </div>
                </div>
              )}
              <iframe
                src={cardAssetPath}
                title="Fullscreen 3D Card"
                className="w-full h-full border-0"
                allow="accelerometer; gyroscope"
              />
            </div>
          </div>
        )}

        {/* Settings & Privacy Modal (Strict Optional AI Toggle) */}
        {showSettingsModal && (
          <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-[#11131a] border border-amber-500/50 rounded-2xl max-w-md w-full p-5 space-y-4 shadow-2xl relative">
              <button
                onClick={() => setShowSettingsModal(false)}
                className="absolute top-4 right-4 text-[#94a3b8] hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="flex items-center space-x-2 text-amber-400">
                <Sliders className="w-5 h-5" />
                <h3 className="font-black text-sm uppercase tracking-wide">SYSTEM &amp; PRIVACY SETTINGS</h3>
              </div>

              <div className="p-3 rounded-xl bg-[#090a0f] border border-[#232634] space-y-3">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-xs font-extrabold text-white block">
                      AI Technical Scope Advisor
                    </span>
                    <span className="text-[10px] text-[#94a3b8] block">
                      Powered by Gemini 2.5 Flash
                    </span>
                  </div>

                  {/* Toggle Switch */}
                  <button
                    onClick={toggleAiEnabled}
                    className={`w-12 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300 ${
                      aiEnabled ? 'bg-amber-500' : 'bg-[#232634]'
                    }`}
                    role="switch"
                    aria-checked={aiEnabled}
                  >
                    <div
                      className={`w-4 h-4 rounded-full shadow-md transform transition-transform duration-300 ${
                        aiEnabled ? 'translate-x-6 bg-black' : 'translate-x-0 bg-gray-400'
                      }`}
                    />
                  </button>
                </div>

                <div className="text-[11px] leading-relaxed text-[#cbd5e1] border-t border-[#232634] pt-2 space-y-1">
                  <p>
                    {aiEnabled ? (
                      <span className="text-amber-300 font-semibold block">
                        ✨ AI Advisor is ENABLED. You can consult with the TYR &amp; Freyja technical oracle and receive automated proposal summaries.
                      </span>
                    ) : (
                      <span className="text-emerald-400 font-semibold block">
                        🛡️ AI Advisor is DISABLED. Strict Zero-AI privacy preserved. Zero AI API calls or external processing.
                      </span>
                    )}
                  </p>
                  <p className="text-[10px] text-[#94a3b8]">
                    This setting gives you 100% control over whether artificial intelligence features are used in your session.
                  </p>
                </div>
              </div>

              <div className="space-y-2 text-xs">
                <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] text-[#cbd5e1]">
                  <strong className="text-white block">Sovereign Data Guarantee</strong>
                  No client inputs or quote carts are retained for model training. Zero cookies or third-party trackers.
                </div>
              </div>

              <button
                onClick={() => setShowSettingsModal(false)}
                className="w-full py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs transition shadow-md"
              >
                Save &amp; Close Settings
              </button>
            </div>
          </div>
        )}

        {/* Zero-Custody Modal */}
        {showZeroCustodyModal && (
          <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-[#11131a] border border-emerald-500/50 rounded-2xl max-w-md w-full p-5 space-y-4 shadow-2xl relative">
              <button
                onClick={() => setShowZeroCustodyModal(false)}
                className="absolute top-4 right-4 text-[#94a3b8] hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="flex items-center space-x-2 text-emerald-400">
                <Shield className="w-5 h-5" />
                <h3 className="font-black text-sm uppercase tracking-wide">ZERO-CUSTODY PRIVACY RULE</h3>
              </div>

              <p className="text-xs text-[#cbd5e1] leading-relaxed">
                TYR Technical Consulting operates under a strict Zero-Custody confidentiality guarantee:
              </p>

              <div className="space-y-2 text-xs">
                <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] text-[#cbd5e1]">
                  <strong className="text-white block">0 Raw Financial Statements Retained</strong>
                  Source bank and utility invoices are purged from local RAM within 24 hours of deliverable dispatch.
                </div>
                <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] text-[#cbd5e1]">
                  <strong className="text-white block">100% Client Ownership</strong>
                  All master code, fillable contracts, and Excel hubs are handed over entirely. Zero vendor lock-in.
                </div>
                <div className="bg-[#090a0f] p-2.5 rounded-lg border border-[#232634] text-[#cbd5e1]">
                  <strong className="text-white block">Local Verified Identity</strong>
                  Sole proprietorship registered in Northwest Alabama by CALEB TR HOLCOMB (d/b/a TYR). W-9 on request.
                </div>
              </div>

              <button
                onClick={() => setShowZeroCustodyModal(false)}
                className="w-full py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs transition"
              >
                Understood &amp; Verified
              </button>
            </div>
          </div>
        )}

        {/* Inquiry Modal */}
        {showInquiryModal && (
          <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-[#11131a] border border-red-600/50 rounded-2xl max-w-md w-full p-5 space-y-4 shadow-2xl relative">
              <button
                onClick={() => setShowInquiryModal(false)}
                className="absolute top-4 right-4 text-[#94a3b8] hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="flex items-center space-x-2 text-red-400">
                <Mail className="w-5 h-5" />
                <h3 className="font-black text-sm uppercase tracking-wide">DIRECT CONSULTATION DISPATCH</h3>
              </div>

              {inquiryModalItem ? (
                <div className="bg-[#090a0f] p-3 rounded-lg border border-[#232634] space-y-1 text-xs">
                  <div className="flex justify-between items-start">
                    <span className="font-bold text-white">{inquiryModalItem.title}</span>
                    <span className="font-bold text-amber-400">{inquiryModalItem.price}</span>
                  </div>
                  <span className="font-mono text-[10px] text-[#94a3b8] block">
                    {inquiryModalItem.sku} • {inquiryModalItem.priceSub}
                  </span>
                  <p className="text-[11px] text-[#cbd5e1]">{inquiryModalItem.meaning}</p>
                </div>
              ) : cart.length > 0 ? (
                <div className="bg-[#090a0f] p-3 rounded-lg border border-[#232634] space-y-1 text-xs max-h-36 overflow-y-auto">
                  <span className="font-bold text-white block">{cart.length} Services in Estimate Order</span>
                  {cart.map((c) => (
                    <div key={c.sku} className="flex justify-between text-[11px] text-[#cbd5e1]">
                      <span>{c.sku}: {c.title}</span>
                      <span className="text-amber-400 font-bold">{c.price}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-[#cbd5e1]">
                  Direct inquiry to Caleb TR Holcomb for custom technical consulting or project scopes.
                </p>
              )}

              <div className="space-y-2">
                <button
                  onClick={() => {
                    setShowInquiryModal(false);
                    handleDial();
                  }}
                  className="w-full py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-xs flex items-center justify-center space-x-2 transition"
                >
                  <Phone className="w-4 h-4" />
                  <span>Call {ORGANIZATION.phone} Now</span>
                </button>

                <button
                  onClick={() => {
                    setShowInquiryModal(false);
                    const subject = inquiryModalItem
                      ? `Inquiry: ${inquiryModalItem.sku} - ${inquiryModalItem.title}`
                      : `Inquiry: Custom Service Package (${cart.length} items)`;
                    const body = inquiryModalItem
                      ? `Hello Caleb,\n\nI am interested in ${inquiryModalItem.title} (${inquiryModalItem.sku}, ${inquiryModalItem.price}). Please let me know how to start.\n\nThank you!`
                      : `Hello Caleb,\n\nI would like an estimate on the following items:\n${cart
                          .map((c) => `- ${c.sku}: ${c.title} (${c.price})`)
                          .join('\n')}\n\nThank you!`;
                    handleEmail(subject, body);
                  }}
                  className="w-full py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white font-extrabold text-xs flex items-center justify-center space-x-2 transition"
                >
                  <Mail className="w-4 h-4" />
                  <span>Send Encrypted Email</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Client Work Order Intake Modal */}
        <ClientIntakeModal
          isOpen={showIntakeModal}
          onClose={() => setShowIntakeModal(false)}
          preSelectedServices={intakePreServices}
        />

        {/* Secret Sovereign Admin Passcode Modal (Not viewable by clients) */}
        {showAdminPasscodeModal && (
          <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-50 flex items-center justify-center p-4">
            <div className="bg-[#11131a] border border-amber-500/60 rounded-2xl max-w-sm w-full p-5 space-y-4 shadow-2xl relative">
              <button
                onClick={() => {
                  setShowAdminPasscodeModal(false);
                  setAdminPasscodeError(null);
                  setAdminPasscodeInput('');
                }}
                className="absolute top-4 right-4 text-[#94a3b8] hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>

              <div className="flex items-center space-x-2.5">
                <div className="w-8 h-8 rounded-lg bg-red-950 border border-amber-400 flex items-center justify-center text-amber-300 font-black text-sm">
                  ᛏ
                </div>
                <div>
                  <h3 className="font-black text-sm text-white uppercase tracking-wider">
                    Sovereign Intake Vault
                  </h3>
                  <p className="text-[10px] text-amber-400 font-mono">Restricted Operator Access</p>
                </div>
              </div>

              <form onSubmit={handleVerifyPasscode} className="space-y-3">
                <div>
                  <label className="text-[11px] font-bold text-[#cbd5e1] block mb-1">
                    Passcode Authentication
                  </label>
                  <input
                    type="password"
                    autoFocus
                    value={adminPasscodeInput}
                    onChange={(e) => setAdminPasscodeInput(e.target.value)}
                    placeholder="Enter security key..."
                    className="w-full px-3 py-2 rounded-lg bg-[#090a0f] border border-[#232634] text-white text-xs focus:border-amber-400 focus:outline-none font-mono"
                  />
                  {adminPasscodeError && (
                    <p className="text-[10px] text-red-400 mt-1 font-medium">{adminPasscodeError}</p>
                  )}
                  <p className="text-[9px] text-[#64748b] mt-1">
                    Default sovereign key: <code className="text-amber-300">tyr2026</code>
                  </p>
                </div>

                <div className="flex space-x-2 pt-1">
                  <button
                    type="button"
                    onClick={() => {
                      setShowAdminPasscodeModal(false);
                      setAdminPasscodeError(null);
                      setAdminPasscodeInput('');
                    }}
                    className="flex-1 py-2 rounded-lg bg-[#090a0f] text-[#cbd5e1] border border-[#232634] text-xs font-semibold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="flex-1 py-2 rounded-lg bg-gradient-to-r from-red-600 to-amber-600 text-white text-xs font-extrabold shadow-md hover:from-red-500 hover:to-amber-500 transition"
                  >
                    Unlock Console
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Consult Appointment Booking Scheduler Modal */}
        <AppointmentSchedulerModal
          isOpen={showBookingModal}
          onClose={() => {
            setShowBookingModal(false);
            setBookingPreSku(undefined);
          }}
          preSelectedSku={bookingPreSku}
        />

        {/* Zero-Custody Document Vault Modal */}
        <ZeroCustodyDocumentVaultModal
          isOpen={showVaultModal}
          onClose={() => setShowVaultModal(false)}
        />

        {/* Direct Redundant Failover Dispatch Modal (Google Chat Relay) */}
        <DirectDispatchModal
          isOpen={showDispatchModal}
          onClose={() => setShowDispatchModal(false)}
          originMode={portalMode}
          initialSubject={dispatchInitialSubject}
        />

        {/* Digital Contract Execution Modal (15 U.S.C. § 7001 & Alabama UETA) */}
        <DigitalContractSignModal
          isOpen={showSignModal}
          onClose={() => setShowSignModal(false)}
          documentTitle={signDocTitle}
          documentCode={signDocCode}
          contractValue={signContractValue}
        />
      </div>
    </div>
  );
}

// Individual Service Card Component
function ServiceCardComponent({
  service,
  onInquire,
  onToggleCart,
  isInCart
}: {
  service: ServiceItem;
  onInquire: () => void;
  onToggleCart: () => void;
  isInCart: boolean;
}) {
  const realm = REALMS[service.realmId] || REALMS.midgard;

  return (
    <div
      className="rounded-xl bg-[#11131a] border border-[#232634] p-3.5 flex flex-col justify-between hover:border-red-900/60 transition shadow-lg relative overflow-hidden"
      style={{
        borderTopWidth: '3px',
        borderTopColor: realm.main
      }}
    >
      <div className="space-y-2">
        {/* Top Header */}
        <div className="flex items-start justify-between gap-2">
          <div>
            <div className="flex items-center space-x-1.5 mb-1">
              <span
                className="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase"
                style={{
                  backgroundColor: `${realm.main}25`,
                  color: realm.light,
                  border: `1px solid ${realm.main}60`
                }}
              >
                {service.runePill}
              </span>
              <span className="font-mono text-[10px] text-[#94a3b8] font-bold">{service.sku}</span>
            </div>
            <span className="text-[10px] font-medium block" style={{ color: realm.light }}>
              {service.meaning}
            </span>
            <h3 className="text-sm font-bold text-white leading-snug">{service.title}</h3>
          </div>

          <div className="text-right shrink-0">
            <span className="text-base font-extrabold block" style={{ color: realm.light }}>
              {service.price}
            </span>
            <span className="text-[9px] text-[#94a3b8] uppercase tracking-wider block">
              {service.priceSub}
            </span>
          </div>
        </div>

        {/* 3-Cell Attribute Strip */}
        <div
          className="grid grid-cols-3 gap-1 bg-[#090a0f] p-1.5 rounded-lg border border-[#232634]"
          style={{ borderLeft: `2px solid ${realm.main}` }}
        >
          {service.attrs.map(([k, v], idx) => (
            <div key={k} className="truncate">
              <span className="text-[8px] uppercase text-[#94a3b8] font-bold block">{k}</span>
              <span
                className="text-[10px] font-bold truncate block"
                style={{ color: idx === 0 ? realm.light : '#e2e8f0' }}
              >
                {v}
              </span>
            </div>
          ))}
        </div>

        {/* Bullets */}
        <ul className="space-y-1 text-xs text-[#cbd5e1] leading-relaxed pt-1">
          {service.bullets.map((b, i) => (
            <li key={i} className="flex items-start space-x-1.5">
              <span className="text-xs" style={{ color: realm.light }}>
                •
              </span>
              <span className="text-[11px]">{b}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Footer & Actions */}
      <div className="pt-3 mt-3 border-t border-[#232634] space-y-2">
        <div className="flex items-center justify-between text-[10px]">
          <span className="px-1.5 py-0.5 rounded bg-[#090a0f] border border-[#232634] text-[#cbd5e1] font-semibold truncate max-w-[180px]">
            {service.badge}
          </span>
          <span className="font-mono font-bold" style={{ color: realm.light }}>
            {service.specTag}
          </span>
        </div>

        <div className="grid grid-cols-2 gap-2 pt-1">
          <button
            onClick={onToggleCart}
            className={`py-1.5 rounded-lg text-xs font-bold border flex items-center justify-center space-x-1 transition ${
              isInCart
                ? 'bg-emerald-950 text-emerald-400 border-emerald-500'
                : 'bg-[#090a0f] text-[#cbd5e1] border-[#232634] hover:bg-[#1a1c26]'
            }`}
          >
            <ShoppingCart className="w-3.5 h-3.5" />
            <span>{isInCart ? 'In Cart' : '+ Estimate'}</span>
          </button>

          <button
            onClick={onInquire}
            className="py-1.5 rounded-lg text-xs font-bold text-white flex items-center justify-center space-x-1 transition shadow-md"
            style={{ backgroundColor: realm.main }}
          >
            <Send className="w-3.5 h-3.5" />
            <span>Order SKU</span>
          </button>
        </div>
      </div>
    </div>
  );
}
