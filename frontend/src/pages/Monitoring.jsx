import React, { useState, useEffect } from 'react';
import apiClient, { apiMetrics, onRateLimitUpdate, KONG_API_URL, KONG_ADMIN_URL } from '../api/client';
import { 
  Activity, 
  ShieldCheck, 
  CheckCircle2, 
  AlertOctagon, 
  Clock, 
  Zap, 
  Radio, 
  Layers, 
  Send,
  Server
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function Monitoring() {
  const [metrics, setMetrics] = useState({ ...apiMetrics });
  const [rateLimitInfo, setRateLimitInfo] = useState({
    limit: 100,
    remaining: 100,
    reset: 60,
    latency: 0,
  });
  const [testLoading, setTestLoading] = useState(false);

  useEffect(() => {
    const unsubscribe = onRateLimitUpdate((info) => {
      setRateLimitInfo((prev) => ({
        ...prev,
        ...info,
      }));
      setMetrics({ ...apiMetrics });
    });
    return unsubscribe;
  }, []);

  // Send a probe ping through Kong
  const handleProbePing = async () => {
    setTestLoading(true);
    try {
      await apiClient.get('/api/devices');
      toast.success('Gateway probe roundtrip successful!');
    } catch (err) {
      // Metrics are tracked in interceptor
    } finally {
      setTestLoading(false);
    }
  };

  const percentageRemaining = Math.round(
    ((rateLimitInfo.remaining ?? 100) / (rateLimitInfo.limit || 100)) * 100
  );

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">API Gateway Telemetry & Inspection</h1>
          <p className="text-sm text-slate-400">Live monitoring of Kong Gateway OSS rate limits, proxies, and security filters</p>
        </div>

        <button
          onClick={handleProbePing}
          disabled={testLoading}
          className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold rounded-xl text-xs shadow-lg shadow-cyan-500/20 transition-all disabled:opacity-50"
        >
          <Send className={`w-3.5 h-3.5 ${testLoading ? 'animate-ping' : ''}`} />
          <span>Send Gateway Probe Ping</span>
        </button>
      </div>

      {/* Cluster Nodes Status */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {/* Kong Proxy */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Kong Gateway Proxy</span>
            <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-bold">
              ACTIVE
            </span>
          </div>
          <p className="text-xl font-bold text-white mt-2">Port 8000</p>
          <p className="text-xs text-slate-400 mt-1 font-mono">http://localhost:8000</p>
          <div className="mt-4 pt-3 border-t border-slate-800 text-xs text-cyan-400 flex items-center space-x-1.5">
            <Radio className="w-3.5 h-3.5" />
            <span>DB-less Declarative Mode (kong.yml)</span>
          </div>
        </div>

        {/* Kong Admin API */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Kong Admin API</span>
            <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-bold">
              SECURE
            </span>
          </div>
          <p className="text-xl font-bold text-white mt-2">Port 8001</p>
          <p className="text-xs text-slate-400 mt-1 font-mono">127.0.0.1:8001 (Local Only)</p>
          <div className="mt-4 pt-3 border-t border-slate-800 text-xs text-emerald-400 flex items-center space-x-1.5">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Bound exclusively to loopback</span>
          </div>
        </div>

        {/* Spring Boot Backend */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">NMS Spring Boot</span>
            <span className="px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 text-[10px] font-bold">
              UPSTREAM
            </span>
          </div>
          <p className="text-xl font-bold text-white mt-2">Port 8080</p>
          <p className="text-xs text-slate-400 mt-1 font-mono">http://host.docker.internal:8080</p>
          <div className="mt-4 pt-3 border-t border-slate-800 text-xs text-blue-400 flex items-center space-x-1.5">
            <Server className="w-3.5 h-3.5" />
            <span>Spring Security RBAC + MySQL</span>
          </div>
        </div>
      </div>

      {/* Rate Limiting Live Gauge */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div>
            <h3 className="text-base font-bold text-white flex items-center space-x-2">
              <Zap className="w-4 h-4 text-cyan-400" />
              <span>Kong OSS Rate-Limiting Policy (100 req / minute)</span>
            </h3>
            <p className="text-xs text-slate-400">
              Headers extracted from gateway: <code className="text-cyan-300">X-RateLimit-Limit-Minute</code> & <code className="text-cyan-300">X-RateLimit-Remaining-Minute</code>
            </p>
          </div>
          <span className="text-sm font-mono font-bold text-cyan-400 bg-cyan-500/10 border border-cyan-500/20 px-3 py-1.5 rounded-xl self-start">
            {rateLimitInfo.remaining ?? 100} / {rateLimitInfo.limit} Remaining
          </span>
        </div>

        {/* Progress bar */}
        <div className="w-full bg-slate-800 h-3.5 rounded-full overflow-hidden p-0.5 border border-slate-700/60">
          <div
            className={`h-full rounded-full transition-all duration-300 ${
              percentageRemaining > 50
                ? 'bg-gradient-to-r from-emerald-500 to-cyan-400'
                : percentageRemaining > 20
                ? 'bg-gradient-to-r from-amber-500 to-orange-400'
                : 'bg-gradient-to-r from-rose-500 to-red-600 animate-pulse'
            }`}
            style={{ width: `${Math.max(percentageRemaining, 2)}%` }}
          />
        </div>

        <div className="flex items-center justify-between text-xs text-slate-400 pt-1">
          <span>Window Capacity: {percentageRemaining}% available</span>
          <span>Window Reset: {rateLimitInfo.reset ?? 60}s</span>
          <span>Last Roundtrip Latency: {rateLimitInfo.latency ?? 0}ms</span>
        </div>
      </div>

      {/* Live Request Stream Table */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center space-x-2">
            <Activity className="w-4 h-4 text-cyan-400" />
            <span>Recent API Gateway Traffic Log</span>
          </h3>
          <span className="text-xs text-slate-400 font-mono">
            {metrics.totalRequests} total requests
          </span>
        </div>

        {metrics.history.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            No requests dispatched yet. Click "Send Gateway Probe Ping" or navigate pages.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold tracking-wider">
                  <th className="py-2.5 px-3">Time</th>
                  <th className="py-2.5 px-3">Method</th>
                  <th className="py-2.5 px-3">Endpoint</th>
                  <th className="py-2.5 px-3">HTTP Status</th>
                  <th className="py-2.5 px-3">Latency</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {metrics.history.map((h, i) => (
                  <tr key={i} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-2 px-3 text-slate-400">{h.timestamp}</td>
                    <td className="py-2 px-3 font-bold text-white">{h.method}</td>
                    <td className="py-2 px-3 text-cyan-300">{h.url}</td>
                    <td className="py-2 px-3">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        h.status >= 200 && h.status < 300 ? 'bg-emerald-500/20 text-emerald-300' :
                        h.status === 401 ? 'bg-amber-500/20 text-amber-300' :
                        h.status === 403 ? 'bg-rose-500/20 text-rose-300' :
                        h.status === 429 ? 'bg-purple-500/20 text-purple-300' :
                        'bg-slate-700 text-slate-300'
                      }`}>
                        {h.status || 'ERR'}
                      </span>
                    </td>
                    <td className="py-2 px-3 text-slate-400">{h.latency ? `${h.latency}ms` : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Architecture Verification Card */}
      <div className="p-6 rounded-2xl bg-gradient-to-r from-slate-900 to-slate-850 border border-slate-800 shadow-xl space-y-3">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider flex items-center space-x-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>Security Architecture Flow Summary</span>
        </h3>
        <p className="text-xs text-slate-400 leading-relaxed">
          Every HTTP call made by this React application routes strictly to <b>Kong Gateway OSS</b> on port <b>8000</b>. 
          Kong enforces rate limits, rewrites paths, and forwards authenticated traffic to Spring Boot on port <b>8080</b>, 
          where Spring Security validates the credentials and executes Role-Based Access Control before querying MySQL.
        </p>
      </div>
    </div>
  );
}

