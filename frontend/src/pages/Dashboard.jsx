import React, { useState, useEffect } from 'react';
import apiClient from '../api/client';
import { 
  Server, 
  AlertTriangle, 
  ShieldCheck, 
  CheckCircle2, 
  XCircle, 
  RefreshCw,
  Activity,
  Radio,
  Layers
} from 'lucide-react';
import { 
  PieChart, 
  Pie, 
  Cell, 
  ResponsiveContainer, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  Legend 
} from 'recharts';
import toast from 'react-hot-toast';

const SEVERITY_COLORS = {
  CRITICAL: '#f43f5e',
  MAJOR: '#f97316',
  MINOR: '#eab308',
  WARNING: '#3b82f6',
};

const STATUS_COLORS = {
  ONLINE: '#10b981',
  OFFLINE: '#64748b',
};

export default function Dashboard() {
  const [devices, setDevices] = useState([]);
  const [alarms, setAlarms] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [devRes, alarmRes] = await Promise.all([
        apiClient.get('/api/devices'),
        apiClient.get('/api/alarms'),
      ]);
      setDevices(devRes.data || []);
      setAlarms(alarmRes.data || []);
    } catch (err) {
      toast.error('Failed to fetch NMS telemetry data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Compute metrics
  const totalDevices = devices.length;
  const onlineDevices = devices.filter((d) => d.status === 'ONLINE').length;
  const offlineDevices = totalDevices - onlineDevices;

  const totalAlarms = alarms.length;
  const criticalAlarms = alarms.filter((a) => a.severity === 'CRITICAL').length;
  const majorAlarms = alarms.filter((a) => a.severity === 'MAJOR').length;
  const activeAlarms = alarms.filter((a) => a.status === 'ACTIVE').length;

  const deviceStatusData = [
    { name: 'Online', value: onlineDevices, color: STATUS_COLORS.ONLINE },
    { name: 'Offline', value: offlineDevices, color: STATUS_COLORS.OFFLINE },
  ];

  const severityCounts = {
    CRITICAL: alarms.filter((a) => a.severity === 'CRITICAL').length,
    MAJOR: alarms.filter((a) => a.severity === 'MAJOR').length,
    MINOR: alarms.filter((a) => a.severity === 'MINOR').length,
    WARNING: alarms.filter((a) => a.severity === 'WARNING').length,
  };

  const alarmSeverityData = [
    { name: 'Critical', count: severityCounts.CRITICAL, fill: SEVERITY_COLORS.CRITICAL },
    { name: 'Major', count: severityCounts.MAJOR, fill: SEVERITY_COLORS.MAJOR },
    { name: 'Minor', count: severityCounts.MINOR, fill: SEVERITY_COLORS.MINOR },
    { name: 'Warning', count: severityCounts.WARNING, fill: SEVERITY_COLORS.WARNING },
  ];

  return (
    <div className="space-y-8">
      {/* Top action bar */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold text-white">NMS Telemetry Overview</h1>
          <p className="text-sm text-slate-400">Real-time status of managed network elements and alerts</p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="flex items-center space-x-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition-all disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Telemetry</span>
        </button>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Devices */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Devices</span>
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Server className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-black text-white">{totalDevices}</span>
            <span className="text-xs text-emerald-400 font-medium">
              {totalDevices > 0 ? `${Math.round((onlineDevices / totalDevices) * 100)}% Online` : '0%'}
            </span>
          </div>
          <div className="mt-3 flex items-center space-x-3 text-xs text-slate-400">
            <span className="flex items-center space-x-1">
              <span className="w-2 h-2 rounded-full bg-emerald-500" />
              <span>{onlineDevices} Online</span>
            </span>
            <span className="flex items-center space-x-1">
              <span className="w-2 h-2 rounded-full bg-slate-500" />
              <span>{offlineDevices} Offline</span>
            </span>
          </div>
        </div>

        {/* Total Alarms */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Active Alarms</span>
            <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <AlertTriangle className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-3xl font-black text-white">{activeAlarms}</span>
            <span className="text-xs text-slate-400 font-medium">of {totalAlarms} Total</span>
          </div>
          <div className="mt-3 flex items-center space-x-2 text-xs text-slate-400">
            <span className="text-rose-400 font-semibold">{criticalAlarms} Critical</span>
            <span>•</span>
            <span className="text-amber-400 font-semibold">{majorAlarms} Major</span>
          </div>
        </div>

        {/* Gateway Security Status */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Kong Security</span>
            <div className="p-2 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <ShieldCheck className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-xl font-bold text-cyan-400">Enforced</span>
          </div>
          <div className="mt-3 text-xs text-slate-400 flex items-center space-x-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
            <span>Rate Limiting & RBAC Active</span>
          </div>
        </div>

        {/* NMS Health Status */}
        <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">System State</span>
            <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Activity className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-white">
              {criticalAlarms > 0 ? 'Degraded' : 'Operational'}
            </span>
          </div>
          <div className="mt-3 text-xs text-slate-400">
            {criticalAlarms > 0 ? (
              <span className="text-rose-400 font-semibold">Immediate attention needed</span>
            ) : (
              <span className="text-emerald-400 font-semibold">All nodes operating within SLA</span>
            )}
          </div>
        </div>
      </div>

      {/* Interactive Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Device Health Chart */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Device Online Distribution</h3>
            <span className="text-xs text-slate-400">Live Status</span>
          </div>
          <div className="h-64 flex items-center justify-center">
            {totalDevices > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={deviceStatusData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {deviceStatusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem' }} 
                  />
                  <Legend verticalAlign="bottom" height={36} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-center text-slate-500 text-xs">
                No devices found in inventory. Add devices from the Device page.
              </div>
            )}
          </div>
        </div>

        {/* Alarm Severity Bar Chart */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">Alarms by Severity</h3>
            <span className="text-xs text-slate-400">Active Faults</span>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={alarmSeverityData}>
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={12} allowDecimals={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem' }} 
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {alarmSeverityData.map((entry, index) => (
                    <Cell key={`bar-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Alarms Feed */}
      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl">
        <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-4">Recent Network Alarms</h3>
        {alarms.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            No active alarms recorded on monitored network elements.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold tracking-wider">
                  <th className="py-3 px-4">Device ID</th>
                  <th className="py-3 px-4">Alarm Type</th>
                  <th className="py-3 px-4">Severity</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Description</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {alarms.slice(0, 5).map((a) => (
                  <tr key={a.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-cyan-400">Device #{a.deviceId}</td>
                    <td className="py-3 px-4 font-semibold text-white">{a.alarmType}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                        a.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' :
                        a.severity === 'MAJOR' ? 'bg-orange-500/20 text-orange-300 border border-orange-500/30' :
                        a.severity === 'MINOR' ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' :
                        'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                      }`}>
                        {a.severity}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                        a.status === 'ACTIVE' ? 'bg-rose-500/10 text-rose-400' :
                        a.status === 'ACKNOWLEDGED' ? 'bg-amber-500/10 text-amber-400' :
                        'bg-emerald-500/10 text-emerald-400'
                      }`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-400 truncate max-w-xs">{a.description}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

