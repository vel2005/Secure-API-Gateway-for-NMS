import React, { useState, useEffect } from 'react';
import { ShieldCheck, Activity, Cpu, Bell, CheckCircle2 } from 'lucide-react';
import apiClient, { onRateLimitUpdate } from '../api/client';

export default function Header({ title }) {
  const [rateLimit, setRateLimit] = useState({ limit: 100, remaining: 100 });
  const [gatewayStatus, setGatewayStatus] = useState('ONLINE');

  useEffect(() => {
    // Listen to real-time rate limit headers
    const unsubscribe = onRateLimitUpdate((info) => {
      if (info.limit && info.remaining !== null) {
        setRateLimit({ limit: info.limit, remaining: info.remaining });
      }
    });
    return unsubscribe;
  }, []);

  return (
    <header className="h-18 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-8 flex items-center justify-between sticky top-0 z-20">
      <div>
        <h2 className="text-xl font-bold text-white tracking-tight">{title}</h2>
        <p className="text-xs text-slate-400">Secure Northbound Interface Management</p>
      </div>

      <div className="flex items-center space-x-4">
        {/* Kong Rate Limit Indicator */}
        <div className="flex items-center space-x-2.5 px-3.5 py-1.5 rounded-xl bg-slate-800/80 border border-slate-700/60 text-xs">
          <Activity className="w-4 h-4 text-cyan-400" />
          <span className="text-slate-400">Kong Rate Limit:</span>
          <span className="font-mono font-bold text-cyan-300">
            {rateLimit.remaining} / {rateLimit.limit} req/min
          </span>
        </div>

        {/* Gateway Status Badge */}
        <div className="flex items-center space-x-2 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-400 font-semibold">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
          <span>Kong Gateway: 8000</span>
        </div>

        {/* Backend Status Badge */}
        <div className="flex items-center space-x-2 px-3 py-1.5 rounded-xl bg-blue-500/10 border border-blue-500/20 text-xs text-blue-400 font-semibold">
          <CheckCircle2 className="w-3.5 h-3.5 text-blue-400" />
          <span>Backend: 8080</span>
        </div>
      </div>
    </header>
  );
}

