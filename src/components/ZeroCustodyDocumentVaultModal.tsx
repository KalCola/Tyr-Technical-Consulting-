import React, { useState, useEffect } from 'react';
import {
  X,
  UploadCloud,
  FileCheck,
  Shield,
  Trash2,
  Lock,
  Clock,
  AlertCircle,
  FileText
} from 'lucide-react';
import {
  VaultDocument,
  loadVaultDocuments,
  stageDocument,
  purgeDocument
} from '../data/intakeStore';

interface ZeroCustodyDocumentVaultModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ZeroCustodyDocumentVaultModal({
  isOpen,
  onClose
}: ZeroCustodyDocumentVaultModalProps) {
  const [docs, setDocs] = useState<VaultDocument[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<VaultDocument['category']>('DNS_RECORDS');
  const [isHashing, setIsHashing] = useState(false);
  const [statusNotice, setStatusNotice] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setDocs(loadVaultDocuments());
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // Client-side SHA-256 calculation
  const computeFileHash = async (file: File): Promise<string> => {
    const buffer = await file.arrayBuffer();
    const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setIsHashing(true);
    setStatusNotice('Computing SHA-256 cryptographic fingerprint in memory...');

    try {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const hash = await computeFileHash(file);
        stageDocument(file, selectedCategory, hash);
      }
      setDocs(loadVaultDocuments());
      setStatusNotice('Document(s) successfully staged under zero-custody transit protocol.');
      setTimeout(() => setStatusNotice(null), 3000);
    } catch (err) {
      setStatusNotice('Error generating hash. File staging aborted.');
    } finally {
      setIsHashing(false);
      e.target.value = '';
    }
  };

  const handlePurge = (id: string) => {
    purgeDocument(id);
    setDocs(loadVaultDocuments());
    setStatusNotice('Document purged immediately. Local memory zeroed out.');
    setTimeout(() => setStatusNotice(null), 2500);
  };

  return (
    <div className="fixed inset-0 bg-black/85 backdrop-blur-md z-50 flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-[#11131a] border border-emerald-500/50 rounded-2xl max-w-lg w-full p-5 space-y-4 shadow-2xl relative my-auto">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94a3b8] hover:text-white transition"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Header */}
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-lg bg-emerald-950/80 border border-emerald-500/60 flex items-center justify-center text-emerald-400">
            <Shield className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-extrabold text-sm text-white uppercase tracking-wider">
              ZERO-CUSTODY CLIENT DOCUMENT VAULT
            </h3>
            <p className="text-[10px] text-emerald-400 font-medium">
              Client-Side SHA-256 Verification • Guaranteed Day-0 Memory Purge
            </p>
          </div>
        </div>

        {/* Policy Explainer */}
        <div className="p-3 rounded-xl bg-[#090a0f] border border-[#232634] space-y-1.5 text-xs">
          <div className="flex items-center space-x-2 text-white font-bold">
            <Lock className="w-3.5 h-3.5 text-emerald-400" />
            <span>Strict Zero-Custody Pledge (NAICS 541512 / 541611)</span>
          </div>
          <p className="text-[11px] text-[#cbd5e1] leading-relaxed">
            Upload domain zone files, statement extracts, or brand identity packages with zero third-party cloud exposure.
            Files are hashed directly in your browser session and permanently purged upon verified handover.
          </p>
        </div>

        {statusNotice && (
          <div className="p-2.5 rounded-lg bg-emerald-950/60 border border-emerald-800 text-emerald-300 text-xs flex items-center space-x-2">
            <FileCheck className="w-4 h-4 shrink-0" />
            <span>{statusNotice}</span>
          </div>
        )}

        {/* Category Picker */}
        <div className="space-y-1.5">
          <label className="text-[11px] font-bold text-[#cbd5e1] block">
            Select Asset Classification
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-1.5 text-xs">
            {(['DNS_RECORDS', 'STATEMENT_EXPORT', 'BRAND_ASSET', 'CONTRACT_DRAFT'] as const).map(cat => (
              <button
                key={cat}
                type="button"
                onClick={() => setSelectedCategory(cat)}
                className={`py-1.5 px-2 rounded-lg text-center text-[10px] font-bold border transition ${
                  selectedCategory === cat
                    ? 'bg-emerald-950/80 text-emerald-300 border-emerald-500'
                    : 'bg-[#090a0f] text-[#94a3b8] border-[#232634] hover:bg-[#161822]'
                }`}
              >
                {cat.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {/* Upload Dropzone */}
        <div className="rounded-xl border-2 border-dashed border-[#232634] hover:border-emerald-500/60 bg-[#090a0f] p-4 text-center space-y-2 transition relative">
          <input
            type="file"
            multiple
            onChange={handleFileUpload}
            disabled={isHashing}
            className="absolute inset-0 opacity-0 cursor-pointer w-full h-full"
          />
          <UploadCloud className="w-8 h-8 text-emerald-400 mx-auto" />
          <div>
            <span className="text-xs font-bold text-white block">
              {isHashing ? 'Hashing file in memory...' : 'Drag & drop or tap to stage project file'}
            </span>
            <span className="text-[10px] text-[#94a3b8] block">
              PNG, JPG, PDF, TXT, CSV, JSON (Up to 25MB per file)
            </span>
          </div>
        </div>

        {/* Staged Files List */}
        <div className="space-y-2">
          <span className="text-[11px] font-bold text-[#cbd5e1] flex items-center justify-between">
            <span>Staged Transit Files ({docs.length})</span>
            <span className="text-[9px] text-[#94a3b8]">Local Session Only</span>
          </span>

          {docs.length === 0 ? (
            <div className="p-4 rounded-xl bg-[#090a0f] border border-[#232634] text-center text-xs text-[#94a3b8]">
              No documents currently staged. All previous files purged.
            </div>
          ) : (
            <div className="space-y-1.5 max-h-44 overflow-y-auto pr-1">
              {docs.map(doc => (
                <div
                  key={doc.id}
                  className="p-2.5 rounded-lg bg-[#090a0f] border border-[#232634] flex items-center justify-between text-xs"
                >
                  <div className="space-y-0.5 truncate max-w-[280px]">
                    <div className="flex items-center space-x-1.5">
                      <span className="font-bold text-white truncate block">{doc.name}</span>
                      <span className="text-[9px] px-1 rounded bg-[#161822] text-amber-300 border border-[#232634]">
                        {doc.category}
                      </span>
                    </div>
                    <span className="font-mono text-[9px] text-[#64748b] block truncate">
                      SHA256: {doc.sha256Hash.slice(0, 16)}...{doc.sha256Hash.slice(-8)}
                    </span>
                  </div>

                  <div className="flex items-center space-x-2 shrink-0">
                    <span
                      className={`text-[9px] font-bold px-1.5 py-0.5 rounded ${
                        doc.status === 'PURGED_DAY_0'
                          ? 'bg-red-950 text-red-400 border border-red-900'
                          : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                      }`}
                    >
                      {doc.status === 'PURGED_DAY_0' ? 'PURGED' : 'STAGED'}
                    </span>

                    {doc.status !== 'PURGED_DAY_0' && (
                      <button
                        onClick={() => handlePurge(doc.id)}
                        className="text-red-400 hover:text-red-300 p-1 transition"
                        title="Purge Now (Day-0 Rule)"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <button
          onClick={onClose}
          className="w-full py-2.5 rounded-lg bg-[#090a0f] hover:bg-[#1a1c26] text-[#cbd5e1] text-xs font-semibold border border-[#232634] transition"
        >
          Close Document Vault
        </button>
      </div>
    </div>
  );
}
