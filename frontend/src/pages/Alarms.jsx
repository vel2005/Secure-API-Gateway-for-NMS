import React, { useState, useEffect } from 'react';
import apiClient from '../api/client';
import { useAuth } from '../context/AuthContext';
import { 
  AlertTriangle, 
  Plus, 
  Search, 
  Filter, 
  CheckCircle2, 
  Clock, 
  Trash2, 
  RefreshCw,
  ShieldAlert,
  Server
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function Alarms() {
  const { user } = useAuth();
  const [alarms, setAlarms] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterSeverity, setFilterSeverity] = useState('ALL');
  const [filterStatus, setFilterStatus] = useState('ALL');

  // Create Alarm Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    deviceId: '',
    alarmType: 'LinkDown',
    description: '',
    severity: 'CRITICAL',
    status: 'ACTIVE',
  });
  const [formLoading, setFormLoading] = useState(false);

  const canModify = user?.role === 'ADMIN' || user?.role === 'ENGINEER';

  const fetchData = async () => {
    setLoading(true);
    try {
      const [alarmRes, devRes] = await Promise.all([
        apiClient.get('/api/alarms'),
        apiClient.get('/api/devices'),
      ]);
      setAlarms(alarmRes.data || []);
      setDevices(devRes.data || []);
      if (devRes.data?.length > 0 && !formData.deviceId) {
        setFormData((prev) => ({ ...prev, deviceId: devRes.data[0].id }));
      }
    } catch (err) {
      toast.error('Failed to load alarm telemetry.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateAlarm = async (e) => {
    e.preventDefault();
    if (!formData.description.trim()) {
      toast.error('Alarm description cannot be empty.');
      return;
    }
    setFormLoading(true);
    try {
      await apiClient.post('/api/alarms', {
        ...formData,
        deviceId: parseInt(formData.deviceId, 10),
      });
      toast.success('Alarm created successfully!');
      setIsModalOpen(false);
      fetchData();
    } catch (err) {
      if (err.response?.status === 403) {
        toast.error('Forbidden: Your role does not allow raising alarms.');
      } else {
        toast.error(err.response?.data?.message || 'Failed to create alarm.');
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateStatus = async (alarmId, nextStatus) => {
    try {
      await apiClient.put(`/api/alarms/${alarmId}/status`, { status: nextStatus });
      toast.success(`Alarm #${alarmId} status updated to ${nextStatus}`);
      fetchData();
    } catch (err) {
      if (err.response?.status === 403) {
        toast.error('Forbidden: VIEWER role cannot modify alarms.');
      } else {
        toast.error(err.response?.data?.message || 'Failed to update alarm status.');
      }
    }
  };

  const handleDeleteAlarm = async (id) => {
    if (!window.confirm(`Delete alarm #${id}?`)) return;
    try {
      await apiClient.delete(`/api/alarms/${id}`);
      toast.success(`Alarm #${id} deleted.`);
      fetchData();
    } catch (err) {
      if (err.response?.status === 403) {
        toast.error('Forbidden: VIEWER role cannot delete alarms.');
      } else {
        toast.error(err.response?.data?.message || 'Failed to delete alarm.');
      }
    }
  };

  const filteredAlarms = alarms.filter((a) => {
    const matchesSev = filterSeverity === 'ALL' || a.severity === filterSeverity;
    const matchesStat = filterStatus === 'ALL' || a.status === filterStatus;
    return matchesSev && matchesStat;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Alarm Management</h1>
          <p className="text-sm text-slate-400">Fault monitoring, severity triage, and incident resolution</p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={fetchData}
            disabled={loading}
            className="p-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl transition-all"
            title="Refresh Alarms"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          {canModify ? (
            <button
              onClick={() => setIsModalOpen(true)}
              className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-rose-500 to-amber-600 hover:from-rose-400 hover:to-amber-500 text-white font-bold rounded-xl text-xs shadow-lg shadow-rose-500/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Raise Test Alarm</span>
            </button>
          ) : (
            <div className="flex items-center space-x-1.5 px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-slate-400">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
              <span>Read-Only Mode ({user?.role})</span>
            </div>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="p-4 bg-slate-900 border border-slate-800 rounded-2xl flex flex-wrap gap-4 items-center justify-between">
        <div className="flex items-center space-x-3">
          <select
            value={filterSeverity}
            onChange={(e) => setFilterSeverity(e.target.value)}
            className="bg-slate-800 border border-slate-700 rounded-xl py-2 px-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Severities</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="MAJOR">MAJOR</option>
            <option value="MINOR">MINOR</option>
            <option value="WARNING">WARNING</option>
          </select>

          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="bg-slate-800 border border-slate-700 rounded-xl py-2 px-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
            <option value="CLEARED">CLEARED</option>
          </select>
        </div>

        <span className="text-xs text-slate-400 font-medium">
          Showing {filteredAlarms.length} of {alarms.length} Alarms
        </span>
      </div>

      {/* Alarms Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-sm flex flex-col items-center justify-center space-y-2">
            <RefreshCw className="w-6 h-6 animate-spin text-rose-400" />
            <span>Fetching alarms...</span>
          </div>
        ) : filteredAlarms.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No alarms found matching your filter selection.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold tracking-wider bg-slate-950/40">
                  <th className="py-3.5 px-4">Alarm ID</th>
                  <th className="py-3.5 px-4">Target Device</th>
                  <th className="py-3.5 px-4">Type</th>
                  <th className="py-3.5 px-4">Severity</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4">Description</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredAlarms.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-slate-400">#{a.id}</td>
                    <td className="py-3.5 px-4 font-mono font-semibold text-cyan-300 flex items-center space-x-1.5">
                      <Server className="w-3.5 h-3.5 text-slate-500" />
                      <span>Device #{a.deviceId}</span>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-white">{a.alarmType}</td>
                    <td className="py-3.5 px-4">
                      <span className={`px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                        a.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' :
                        a.severity === 'MAJOR' ? 'bg-orange-500/20 text-orange-300 border border-orange-500/30' :
                        a.severity === 'MINOR' ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' :
                        'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                      }`}>
                        {a.severity}
                      </span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className={`px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                        a.status === 'ACTIVE' ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' :
                        a.status === 'ACKNOWLEDGED' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                        'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-300 truncate max-w-xs">{a.description}</td>
                    <td className="py-3.5 px-4 text-right">
                      {canModify ? (
                        <div className="flex items-center justify-end space-x-2">
                          {a.status === 'ACTIVE' && (
                            <button
                              onClick={() => handleUpdateStatus(a.id, 'ACKNOWLEDGED')}
                              className="px-2 py-1 rounded bg-amber-500/10 text-amber-300 hover:bg-amber-500/20 border border-amber-500/30 text-[10px] font-bold"
                            >
                              Ack
                            </button>
                          )}
                          {a.status !== 'CLEARED' && (
                            <button
                              onClick={() => handleUpdateStatus(a.id, 'CLEARED')}
                              className="px-2 py-1 rounded bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20 border border-emerald-500/30 text-[10px] font-bold"
                            >
                              Clear
                            </button>
                          )}
                          <button
                            onClick={() => handleDeleteAlarm(a.id)}
                            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition-colors"
                            title="Delete Alarm"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      ) : (
                        <span className="text-slate-500 italic text-[11px]">Read-only</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create Alarm Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl relative">
            <h3 className="text-lg font-bold text-white mb-1">Raise Incident / Alarm</h3>
            <p className="text-xs text-slate-400 mb-5">
              Simulate network fault events on an active inventory element.
            </p>

            <form onSubmit={handleCreateAlarm} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Target Device</label>
                <select
                  value={formData.deviceId}
                  onChange={(e) => setFormData({ ...formData, deviceId: e.target.value })}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                >
                  {devices.map((d) => (
                    <option key={d.id} value={d.id}>
                      #{d.id} - {d.deviceName} ({d.ipAddress})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Alarm Type</label>
                  <select
                    value={formData.alarmType}
                    onChange={(e) => setFormData({ ...formData, alarmType: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                  >
                    <option value="LinkDown">LinkDown</option>
                    <option value="HighCPUUsage">HighCPUUsage</option>
                    <option value="PowerSupplyFail">PowerSupplyFail</option>
                    <option value="BGPFlap">BGPFlap</option>
                    <option value="InterfaceDrop">InterfaceDrop</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Severity</label>
                  <select
                    value={formData.severity}
                    onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                  >
                    <option value="CRITICAL">CRITICAL</option>
                    <option value="MAJOR">MAJOR</option>
                    <option value="MINOR">MINOR</option>
                    <option value="WARNING">WARNING</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Description</label>
                <textarea
                  rows="3"
                  required
                  placeholder="e.g. Primary optical uplink fiber link failure detected on ge-0/0/1"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="flex items-center justify-end space-x-3 mt-6 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white hover:bg-slate-800"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={formLoading}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-500 hover:bg-rose-400 shadow-lg shadow-rose-500/20 disabled:opacity-50"
                >
                  {formLoading ? 'Submitting...' : 'Dispatch Alarm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

