import { ServiceItem, SERVICES, ORGANIZATION } from './catalog';

export interface WorkOrder {
  id: string; // e.g. WO-2026-TYR-8492
  createdAt: string;
  clientName: string;
  businessName: string;
  email: string;
  phone: string;
  preferredContact: 'email' | 'phone' | 'text';
  selectedSkus: string[];
  totalPrice: number;
  timeline: 'Standard (7-14 Days)' | 'Rush (48-72h)' | 'Flexible';
  existingDomain?: string;
  projectDescription: string;
  zeroCustodyAcknowledged: boolean;
  status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'DELIVERED';
  acceptedAt?: string;
  adminNotes?: string;
  estimatedDelivery?: string;
  notificationSent?: boolean;
}

const STORAGE_KEY = 'tyr_work_orders_v1';
const CLIENT_LAST_ORDER_KEY = 'tyr_client_last_order_id';

// Default sample orders so the admin portal looks great and demonstrative immediately
const DEFAULT_ORDERS: WorkOrder[] = [
  {
    id: 'WO-2026-TYR-1082',
    createdAt: '2026-10-29T14:30:00Z',
    clientName: 'Marcus Vance',
    businessName: 'Vance Logistics & Freight',
    email: 'm.vance@vancefreight.example.com',
    phone: '(205) 555-0198',
    preferredContact: 'email',
    selectedSkus: ['SVC-WEB-01', 'SVC-DOM-02'],
    totalPrice: 410,
    timeline: 'Standard (7-14 Days)',
    existingDomain: 'vancefreight.com',
    projectDescription: 'Need modern website rebuild with anti-spoof email setup for 4 dispatch operators. Our current emails are landing in client spam.',
    zeroCustodyAcknowledged: true,
    status: 'ACCEPTED',
    acceptedAt: '2026-10-30T09:15:00Z',
    estimatedDelivery: 'Nov 8, 2026',
    adminNotes: 'DNS SPF and DKIM keys configured. Wireframe draft in staging.',
    notificationSent: true
  }
];

export function loadWorkOrders(): WorkOrder[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      saveWorkOrders(DEFAULT_ORDERS);
      return DEFAULT_ORDERS;
    }
    return JSON.parse(raw);
  } catch (err) {
    console.warn('Error loading work orders:', err);
    return DEFAULT_ORDERS;
  }
}

export function saveWorkOrders(orders: WorkOrder[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(orders));
  } catch (err) {
    console.warn('Error saving work orders:', err);
  }
}

export function submitWorkOrder(intake: Omit<WorkOrder, 'id' | 'createdAt' | 'status' | 'notificationSent'>): WorkOrder {
  const orders = loadWorkOrders();
  const randomSuffix = Math.floor(1000 + Math.random() * 9000);
  const newOrder: WorkOrder = {
    ...intake,
    id: `WO-2026-TYR-${randomSuffix}`,
    createdAt: new Date().toISOString(),
    status: 'PENDING',
    notificationSent: false
  };

  const updated = [newOrder, ...orders];
  saveWorkOrders(updated);

  try {
    localStorage.setItem(CLIENT_LAST_ORDER_KEY, newOrder.id);
  } catch (e) {
    // Ignore
  }

  return newOrder;
}

export function acceptWorkOrder(orderId: string, adminNotes?: string, estimatedDelivery?: string): WorkOrder | null {
  const orders = loadWorkOrders();
  const idx = orders.findIndex(o => o.id === orderId);
  if (idx === -1) return null;

  const now = new Date();
  const defaultEst = estimatedDelivery || new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });

  const updatedOrder: WorkOrder = {
    ...orders[idx],
    status: 'ACCEPTED',
    acceptedAt: now.toISOString(),
    estimatedDelivery: defaultEst,
    adminNotes: adminNotes || orders[idx].adminNotes || 'Scope confirmed. Master file repository initialized under zero-custody protocol.',
    notificationSent: true
  };

  orders[idx] = updatedOrder;
  saveWorkOrders(orders);
  return updatedOrder;
}

export function updateOrderStatus(orderId: string, status: WorkOrder['status'], notes?: string): WorkOrder | null {
  const orders = loadWorkOrders();
  const idx = orders.findIndex(o => o.id === orderId);
  if (idx === -1) return null;

  orders[idx] = {
    ...orders[idx],
    status,
    adminNotes: notes !== undefined ? notes : orders[idx].adminNotes
  };

  saveWorkOrders(orders);
  return orders[idx];
}

export function getClientLastOrderId(): string | null {
  try {
    return localStorage.getItem(CLIENT_LAST_ORDER_KEY);
  } catch {
    return null;
  }
}

export function getOrderById(id: string): WorkOrder | null {
  const orders = loadWorkOrders();
  return orders.find(o => o.id.trim().toUpperCase() === id.trim().toUpperCase()) || null;
}

export function generateAcceptanceEmail(order: WorkOrder): { subject: string; body: string } {
  const subject = `[ACCEPTED] TYR Work Order ${order.id} — Work Has Commenced`;
  const skusDetail = order.selectedSkus.map(s => {
    const item = SERVICES.find(srv => srv.sku === s);
    return item ? `- [${item.sku}] ${item.title} (${item.price})` : `- ${s}`;
  }).join('\n');

  const body = `Dear ${order.clientName},

Thank you for trusting TYR Technical Consulting.

Your Work Order Request (${order.id}) has been REVIEWED and OFFICIALLY ACCEPTED. Caleb TR Holcomb has begun active production on your project.

WORK ORDER SUMMARY:
• Order ID: ${order.id}
• Organization: ${order.businessName || order.clientName}
• Status: ACCEPTED & IN PROGRESS
• Estimated Delivery: ${order.estimatedDelivery || 'Within 7–14 days'}
• Confirmed Total: $${order.totalPrice} Flat Rate ($0 recurring lock-in)

SERVICES COMMENCED:
${skusDetail}

ZERO-CUSTODY PRIVACY GUARANTEE:
Under the TYR Sovereign Protocol, your project files and credentials are held under strict zero-custody rules. No raw bank statements are kept, and all source assets are delivered with complete master ownership handed to you.

NEXT STEPS:
You will receive live delivery updates as milestones complete. You do not need to follow up; we will contact you directly at ${order.email} or ${order.phone}.

Direct Line: ${ORGANIZATION.phone}
Direct Email: ${ORGANIZATION.email}

Sincerely,
CALEB TR HOLCOMB (d/b/a TYR)
TYR Technical Consulting • Northwest Alabama & Nationwide`;

  return { subject, body };
}

export function exportOrdersToJson(orders: WorkOrder[]): void {
  const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(orders, null, 2));
  const downloadAnchor = document.createElement('a');
  downloadAnchor.setAttribute('href', dataStr);
  downloadAnchor.setAttribute('download', `tyr_work_orders_${new Date().toISOString().slice(0, 10)}.json`);
  document.body.appendChild(downloadAnchor);
  downloadAnchor.click();
  downloadAnchor.remove();
}

export function exportOrdersToCsv(orders: WorkOrder[]): void {
  const headers = ['Order ID', 'Created At', 'Client Name', 'Business', 'Email', 'Phone', 'Status', 'Total Price', 'Timeline', 'Services', 'Notes'];
  const rows = orders.map(o => [
    o.id,
    o.createdAt,
    `"${o.clientName.replace(/"/g, '""')}"`,
    `"${(o.businessName || '').replace(/"/g, '""')}"`,
    o.email,
    o.phone,
    o.status,
    o.totalPrice,
    o.timeline,
    `"${o.selectedSkus.join('; ')}"`,
    `"${(o.adminNotes || '').replace(/"/g, '""')}"`
  ]);

  const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
  const downloadAnchor = document.createElement('a');
  downloadAnchor.setAttribute('href', encodeURI(csvContent));
  downloadAnchor.setAttribute('download', `tyr_work_orders_${new Date().toISOString().slice(0, 10)}.csv`);
  document.body.appendChild(downloadAnchor);
  downloadAnchor.click();
  downloadAnchor.remove();
}

// -------------------------------------------------------------
// CONSULTATION APPOINTMENT BOOKING ENGINE
// -------------------------------------------------------------
export interface AppointmentBooking {
  id: string; // e.g. APT-2026-TYR-7482
  createdAt: string;
  clientName: string;
  clientEmail: string;
  clientPhone: string;
  date: string; // YYYY-MM-DD
  timeSlot: string; // e.g. "10:00 AM CST"
  callDuration: '15m Discovery' | '30m Architecture Review';
  callType: 'Phone Call' | 'Encrypted Signal' | 'Email Prep';
  topic: string;
  serviceSku?: string;
  status: 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
}

const BOOKINGS_STORAGE_KEY = 'tyr_appointment_bookings_v1';

const DEFAULT_BOOKINGS: AppointmentBooking[] = [
  {
    id: 'APT-2026-TYR-4891',
    createdAt: '2026-10-30T16:00:00Z',
    clientName: 'Sarah Jenkins',
    clientEmail: 's.jenkins@clearwater-supply.example.com',
    clientPhone: '(205) 555-0842',
    date: '2026-11-03',
    timeSlot: '11:00 AM CST',
    callDuration: '30m Architecture Review',
    callType: 'Phone Call',
    topic: 'Reviewing domain migration from legacy registrar and email anti-spoof hardening.',
    serviceSku: 'SVC-MIG-01',
    status: 'CONFIRMED'
  }
];

export function loadAppointments(): AppointmentBooking[] {
  try {
    const raw = localStorage.getItem(BOOKINGS_STORAGE_KEY);
    if (!raw) {
      saveAppointments(DEFAULT_BOOKINGS);
      return DEFAULT_BOOKINGS;
    }
    return JSON.parse(raw);
  } catch (err) {
    console.warn('Error loading appointments:', err);
    return DEFAULT_BOOKINGS;
  }
}

export function saveAppointments(bookings: AppointmentBooking[]): void {
  try {
    localStorage.setItem(BOOKINGS_STORAGE_KEY, JSON.stringify(bookings));
  } catch (err) {
    console.error('Error saving appointments:', err);
  }
}

export function bookAppointment(
  data: Omit<AppointmentBooking, 'id' | 'createdAt' | 'status'>
): AppointmentBooking {
  const current = loadAppointments();
  const randomSuffix = Math.floor(1000 + Math.random() * 9000);
  const newBooking: AppointmentBooking = {
    ...data,
    id: `APT-2026-TYR-${randomSuffix}`,
    createdAt: new Date().toISOString(),
    status: 'CONFIRMED'
  };

  const updated = [newBooking, ...current];
  saveAppointments(updated);
  return newBooking;
}

export function generateIcsCalendarFile(booking: AppointmentBooking): void {
  // Convert date & timeslot into synthetic ICS date
  const cleanDate = booking.date.replace(/-/g, '');
  const hourMap: Record<string, string> = {
    '09:00 AM CST': '150000Z',
    '10:30 AM CST': '163000Z',
    '01:00 PM CST': '190000Z',
    '02:30 PM CST': '203000Z',
    '04:00 PM CST': '220000Z'
  };
  const startTime = hourMap[booking.timeSlot] || '160000Z';
  const endTime = '170000Z';

  const icsLines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//TYR Technical Consulting//Sovereign Scheduler//EN',
    'CALSCALE:GREGORIAN',
    'METHOD:REQUEST',
    'BEGIN:VEVENT',
    `UID:${booking.id}@tyrprotocol.pro`,
    `DTSTAMP:${new Date().toISOString().replace(/[-:]/g, '').slice(0, 15)}Z`,
    `DTSTART:${cleanDate}T${startTime}`,
    `DTEND:${cleanDate}T${endTime}`,
    `SUMMARY:Consultation Call: ${booking.clientName} & Caleb TR Holcomb (TYR)`,
    `DESCRIPTION:Sovereign technical consultation with Caleb TR Holcomb (d/b/a TYR).\\nBooking ID: ${booking.id}\\nTopic: ${booking.topic}\\nDirect Phone: ${ORGANIZATION.phone}\\nConfidentiality: Zero-Custody Policy Active`,
    `LOCATION:Phone Call to ${booking.clientPhone}`,
    'STATUS:CONFIRMED',
    'BEGIN:VALARM',
    'TRIGGER:-PT15M',
    'ACTION:DISPLAY',
    'DESCRIPTION:Reminder: TYR Consultation in 15 minutes',
    'END:VALARM',
    'END:VEVENT',
    'END:VCALENDAR'
  ];

  const icsContent = icsLines.join('\r\n');
  const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `TYR_Consultation_${booking.id}.ics`;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1500);
}

// -------------------------------------------------------------
// ZERO-CUSTODY DOCUMENT VAULT STAGING ENGINE
// -------------------------------------------------------------
export interface VaultDocument {
  id: string;
  name: string;
  size: number;
  type: string;
  stagedAt: string;
  sha256Hash: string;
  category: 'DNS_RECORDS' | 'STATEMENT_EXPORT' | 'BRAND_ASSET' | 'CONTRACT_DRAFT';
  status: 'STAGED' | 'DELIVERED' | 'PURGED_DAY_0';
}

const VAULT_STORAGE_KEY = 'tyr_zero_custody_vault_docs_v1';

export function loadVaultDocuments(): VaultDocument[] {
  try {
    const raw = localStorage.getItem(VAULT_STORAGE_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch (err) {
    console.warn('Error loading vault documents:', err);
    return [];
  }
}

export function saveVaultDocuments(docs: VaultDocument[]): void {
  try {
    localStorage.setItem(VAULT_STORAGE_KEY, JSON.stringify(docs));
  } catch (err) {
    console.error('Error saving vault documents:', err);
  }
}

export function stageDocument(
  file: File,
  category: VaultDocument['category'],
  hash: string
): VaultDocument {
  const current = loadVaultDocuments();
  const newDoc: VaultDocument = {
    id: `DOC-TYR-${Date.now().toString(36).toUpperCase()}`,
    name: file.name,
    size: file.size,
    type: file.type || 'application/octet-stream',
    stagedAt: new Date().toISOString(),
    sha256Hash: hash,
    category,
    status: 'STAGED'
  };

  const updated = [newDoc, ...current];
  saveVaultDocuments(updated);
  return newDoc;
}

export function purgeDocument(docId: string): void {
  const current = loadVaultDocuments();
  const updated = current.map(d =>
    d.id === docId ? { ...d, status: 'PURGED_DAY_0' as const } : d
  );
  saveVaultDocuments(updated);
}
