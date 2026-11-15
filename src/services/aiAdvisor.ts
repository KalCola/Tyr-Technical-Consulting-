import { GoogleGenAI } from '@google/genai';
import { ORGANIZATION, SERVICES, REALMS } from '../data/catalog';

export interface AiChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  recommendedSkus?: string[];
  timestamp: string;
}

const SYSTEM_INSTRUCTION = `
You are the TYR & Freyja Technical Consulting Advisor — an AI scoping and architecture assistant for CALEB TR HOLCOMB d/b/a TYR (TYR Technical Consulting).
You embody the Norse ethos of:
- TYR (ᛏ Tiwaz): God of justice, truth, sworn oaths, security, anti-spoofing, and zero-custody data integrity.
- FREYJA (ᚠ Fehu): Goddess of sovereignty, wealth, capital flow, financial audit clarity, and resilience.

Organization Details:
- Name: ${ORGANIZATION.name} (${ORGANIZATION.legalName})
- Phone: ${ORGANIZATION.phone}
- Email: ${ORGANIZATION.email}
- NAICS Codes: ${ORGANIZATION.naics.join(', ')}
- Zero-Custody Privacy Rule: We NEVER keep raw financial/bank statements or client credentials. Complete master files are delivered to the client, and all source working files are purged on day 0 after verified client handover. Zero ongoing SaaS lock-in or platform extortion.
- Pricing Philosophy: 100% transparent flat-rate services ($65 to $495). No surprise hourly bills or runaway retainers.

Catalog of Services (use these exact SKUs when recommending):
${SERVICES.map(s => `- [${s.sku}] ${s.title} (${s.price}) [Rune: ${s.runePill}]: ${s.bullets.slice(0, 2).join('; ')}`).join('\n')}

Guidelines:
1. Provide concise, expert, and actionable advice on web architecture, anti-spoofing DNS (SPF/DKIM/DMARC), fillable contracts, budget ledgering, and monthly care plans.
2. If relevant, recommend 1 to 3 specific SKUs in brackets like [SVC-WEB-01] or [SVC-DOM-02] so the user can easily add them to their estimate cart.
3. Maintain a dignified, professional, yet distinctive Norse-inspired tone (honorable, precise, clear, steadfast).
4. Emphasize sovereign client ownership: the client owns 100% of their domain, code, design, and documents.
`;

export async function askAiAdvisor(
  userQuery: string,
  history: AiChatMessage[] = []
): Promise<{ text: string; recommendedSkus: string[] }> {
  const apiKey = (process.env as any).GEMINI_API_KEY || '';

  // If API key is available, call Google Gen AI
  if (apiKey && apiKey.trim().length > 0) {
    try {
      const ai = new GoogleGenAI({ apiKey });
      const prompt = `Client Question: "${userQuery}"\n\nProvide technical consulting guidance, recommend any matching SKUs from the catalog, and explain how the service addresses their needs with zero-custody security.`;

      const response = await ai.models.generateContent({
        model: 'gemini-3.8-flash',
        contents: prompt,
        config: {
          systemInstruction: SYSTEM_INSTRUCTION,
          temperature: 0.4
        }
      });

      const responseText = response.text || '';
      const recommendedSkus = extractSkus(responseText);

      return {
        text: responseText,
        recommendedSkus
      };
    } catch (err: any) {
      console.warn('AI call failed, falling back to local deterministic engine:', err);
      return generateDeterministicResponse(userQuery);
    }
  }

  // Fallback if no API key is provided
  return generateDeterministicResponse(userQuery);
}

function extractSkus(text: string): string[] {
  const allKnownSkus = SERVICES.map(s => s.sku);
  const matched = new Set<string>();
  
  for (const sku of allKnownSkus) {
    if (text.includes(sku)) {
      matched.add(sku);
    }
  }

  return Array.from(matched);
}

function generateDeterministicResponse(query: string): { text: string; recommendedSkus: string[] } {
  const lower = query.toLowerCase();
  const matchedSkus: string[] = [];
  let responseText = '';

  if (lower.includes('web') || lower.includes('site') || lower.includes('portfolio') || lower.includes('online')) {
    matchedSkus.push('SVC-WEB-01', 'SVC-DOM-02');
    responseText = `ᛏ TYR Architecture Recommendation:
For your web presence, we recommend the [SVC-WEB-01] Responsive Web Presence ($245 Flat) paired with [SVC-DOM-02] Asgard Domain & Anti-Spoof Shield ($165 Flat).

Key Deliverables:
• 100% custom lightweight code with mobile-first responsiveness and sub-second load times.
• Hardened DNS with SPF, DKIM, and strict DMARC enforcement to prevent spam and phishing spoofing of your domain.
• Zero platform hostage fees: you receive all source files, master credentials, and deploy instructions directly.`;
  } else if (lower.includes('email') || lower.includes('spam') || lower.includes('spoof') || lower.includes('dns') || lower.includes('domain')) {
    matchedSkus.push('SVC-DOM-02');
    responseText = `ᛉ Asgard Email Shield Recommendation:
We recommend [SVC-DOM-02] Domain & Email Anti-Spoof Shield ($165 Flat).

Anti-Spoofing & Security Protections:
• Full DNS audit and configuration of SPF (Sender Policy Framework), DKIM (DomainKeys Identified Mail), and DMARC alignment.
• Protects your brand reputation so bad actors cannot impersonate your business email.
• Prevents your legitimate client communications from ending up in customer spam folders.`;
  } else if (lower.includes('contract') || lower.includes('sign') || lower.includes('legal') || lower.includes('proposal') || lower.includes('invoice')) {
    matchedSkus.push('SVC-CON-05');
    responseText = `ᚲ Nidavellir Forge Recommendation:
We recommend [SVC-CON-05] 3-Document Fillable Contract & Invoicing Suite ($195 Flat).

Deliverables:
• Master Service Agreement (MSA), Statement of Work (SOW), and Payment Schedule.
• Fillable PDF and digital e-sign-ready templates that protect your scope, prevent scope creep, and enforce timely payment.
• Zero subscription dependencies: keep these master templates forever with unlimited usage.`;
  } else if (lower.includes('budget') || lower.includes('tax') || lower.includes('expense') || lower.includes('audit') || lower.includes('money') || lower.includes('freya') || lower.includes('freyja')) {
    matchedSkus.push('SVC-BUD-07', 'SVC-EXP-13');
    responseText = `ᚠ FREYJA Sovereign Financial Recommendation:
Under the blessing of Freyja (Fehu — Sovereign Wealth), we recommend [SVC-BUD-07] Budget & Tax Preparation Ledger ($195 Flat) or [SVC-EXP-13] Monthly Expense & SaaS Watchdog ($95/mo).

Zero-Custody Guarantee:
• We audit and categorize your expense patterns without storing raw bank records.
• Identification of zombie SaaS subscriptions, dual charges, and vendor rate creeps.
• 0-day file purge upon completion: your financial privacy remains strictly yours.`;
  } else if (lower.includes('care') || lower.includes('maintenance') || lower.includes('monthly') || lower.includes('retainer')) {
    matchedSkus.push('SVC-CARE-10', 'SVC-CARE-11');
    responseText = `🛡️ Ongoing Peace of Mind:
For continuous monitoring and peace of mind, we provide [SVC-CARE-10] Tech Care Core ($65/mo) and [SVC-CARE-11] Tech Care Pro ($125/mo).

Includes:
• Monthly domain uptime & SSL expiration monitoring.
• Quarterly DNS health & DMARC compliance reports.
• Dedicated change allowance hours for text, pricing, and staff updates.`;
  } else {
    matchedSkus.push('SVC-WEB-01', 'SVC-DOM-02', 'SVC-CON-05');
    responseText = `ᛏ TYR Technical Consulting Overview:
CALEB TR HOLCOMB d/b/a TYR provides sovereign, flat-rate technology, admin, and security services across the Nine Realms ($65 – $495).

Core Pillars:
1. Sovereign Ownership: $0 monthly platform lock-in. You own every line of code, design, and document.
2. Anti-Spoof & Shield: Rigorous DNS authentication (SPF, DKIM, DMARC) so your identity cannot be faked.
3. Zero-Custody Privacy: Zero raw bank or credential custody; 0-day file purge post delivery.

Popular Starting Bundle:
• [SVC-WEB-01] High-Speed Web Presence ($245)
• [SVC-DOM-02] Domain & Email Anti-Spoof Shield ($165)
• [SVC-CON-05] 3-Doc Fillable Contract Suite ($195)`;
  }

  return {
    text: responseText,
    recommendedSkus: matchedSkus
  };
}
