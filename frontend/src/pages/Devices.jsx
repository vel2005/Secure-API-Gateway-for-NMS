import React, { useState, useEffect } from 'react';
import apiClient from '../api/client';
import { useAuth } from '../context/AuthContext';
import { 
  Server, 
  Plus, 
  Search, 
  Filter, 
  Edit3, 
  Trash2, 
  RefreshCw,
  CheckCircle2,
  XCircle,
  AlertCircle,
  ShieldAlert
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function Devices() {
  const { user } = useAuth();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterType, setFilterType] = useState('ALL');

  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [formData, setFormData] = useState({
    deviceName: '',
    deviceType: 'Router',
    ipAddress: '',
    status: 'ONLINE',
  });
  const [formLoading, setFormLoading] = useState(false);

  const canModify = user?.role === 'ADMIN' || user?.role === 'ENGINEER';

  const fetchDevices = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/api/devices');
      setDevices(res.data || []);
    } catch (err) {
      toast.error('Failed to load devices from gateway.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const handleOpenAddModal = () => {
    setEditingDevice(null);
    setFormData({
      deviceName: '',
      deviceType: 'Router',
      ipAddress: '',
      status: 'ONLINE',
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (device) => {
    setEditingDevice(device);
    setFormData({
      deviceName: device.deviceName,
      deviceType: device.deviceType,
      ipAddress: device.ipAddress,
      status: device.status,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);

    // Simple IPv4 validation regex
    const ipv4Regex = /^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$/;
    if (!ipv4Regex.test(formData.ipAddress)) {
      toast.error('Please enter a valid IPv4 address (e.g. 192.168.1.1)');
      setFormLoading(false);
      return;
    }

    try {
      if (editingDevice) {
        await apiClient.put(`/api/devices/${editingDevice.id}`, formData);
        toast.success(`Device "${formData.deviceName}" updated!`);
      } else {
        await apiClient.post('/api/devices', formData);
        toast.success(`Device "${formData.deviceName}" added!`);
      }
      setIsModalOpen(false);
      fetchDevices();
    } catch (err) {
      if (err.response?.status === 403) {
        toast.error('Forbidden: Your role (VIEWER) does not permit device modifications.');
      } else {
        toast.error(err.response?.data?.message || 'Operation failed.');
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete "${name}"? Associated alarms will also be removed.`)) {
      return;
    }
    try {
      await apiClient.delete(`/api/devices/${id}`);
      toast.success(`Device "${name}" deleted.`);
      fetchDevices();
    } catch (err) {
      if (err.response?.status === 403) {
        toast.error('Forbidden: VIEWER role cannot delete devices.');
      } else {
        toast.error(err.response?.data?.message || 'Failed to delete device.');
      }
    }
  };

  // Filtered devices
  const filteredDevices = devices.filter((d) => {
    const matchesSearch = 
      d.deviceName.toLowerCase().includes(search.toLowerCase()) ||
      d.ipAddress.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = filterStatus === 'ALL' || d.status === filterStatus;
    const matchesType = filterType === 'ALL' || d.deviceType === filterType;
    return matchesSearch && matchesStatus && matchesType;
  });

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">Device Inventory</h1>
          <p className="text-sm text-slate-400">Manage edge routers, switches, servers, and gateways</p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={fetchDevices}
            disabled={loading}
            className="p-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl transition-all"
            title="Refresh Devices"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          {canModify ? (
            <button
              onClick={handleOpenAddModal}
              className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold rounded-xl text-xs shadow-lg shadow-cyan-500/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Register New Device</span>
            </button>
          ) : (
            <div className="flex items-center space-x-1.5 px-3 py-2 bg-slate-800/80 border border-slate-700/80 rounded-xl text-xs text-slate-400">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
              <span>Read-Only Mode ({user?.role})</span>
            </div>
          )}
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="p-4 bg-slate-900 border border-slate-800 rounded-2xl flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by name or IPv4..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-800/60 border border-slate-700 rounded-xl py-2 pl-10 pr-4 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>

        <div className="flex items-center space-x-3 w-full md:w-auto">
          {/* Status filter */}
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="bg-slate-800/60 border border-slate-700 rounded-xl py-2 px-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="ONLINE">ONLINE</option>
            <option value="OFFLINE">OFFLINE</option>
          </select>

          {/* Type filter */}
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-slate-800/60 border border-slate-700 rounded-xl py-2 px-3 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Device Types</option>
            <option value="Router">Router</option>
            <option value="Switch">Switch</option>
            <option value="Firewall">Firewall</option>
            <option value="Server">Server</option>
            <option value="Core-Router">Core-Router</option>
          </select>
        </div>
      </div>

      {/* Devices Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-sm flex flex-col items-center justify-center space-y-2">
            <RefreshCw className="w-6 h-6 animate-spin text-cyan-400" />
            <span>Retrieving devices through Kong Gateway...</span>
          </div>
        ) : filteredDevices.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            No devices matched the selected filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold tracking-wider bg-slate-950/40">
                  <th className="py-3.5 px-4">ID</th>
                  <th className="py-3.5 px-4">Device Name</th>
                  <th className="py-3.5 px-4">Type</th>
                  <th className="py-3.5 px-4">IPv4 Address</th>
                  <th className="py-3.5 px-4">Operational Status</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {filteredDevices.map((d) => (
                  <tr key={d.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-slate-400">#{d.id}</td>
                    <td className="py-3.5 px-4 font-bold text-white flex items-center space-x-2">
                      <Server className="w-4 h-4 text-cyan-400" />
                      <span>{d.deviceName}</span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2.5 py-1 rounded-lg bg-slate-800 text-slate-300 border border-slate-700 font-mono text-[11px]">
                        {d.deviceType}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-cyan-300">{d.ipAddress}</td>
                    <td className="py-3.5 px-4">
                      <span className={`inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                        d.status === 'ONLINE' 
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' 
                          : 'bg-slate-500/10 text-slate-400 border border-slate-500/20'
                      }`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${d.status === 'ONLINE' ? 'bg-emerald-400 animate-pulse' : 'bg-slate-400'}`} />
                        <span>{d.status}</span>
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      {canModify ? (
                        <div className="flex items-center justify-end space-x-2">
                          <button
                            onClick={() => handleOpenEditModal(d)}
                            className="p-1.5 text-slate-400 hover:text-cyan-400 hover:bg-slate-800 rounded-lg transition-colors"
                            title="Edit Device"
                          >
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(d.id, d.deviceName)}
                            className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition-colors"
                            title="Delete Device"
                          >
                            <Trash2 className="w-4 h-4" />
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

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl relative">
            <h3 className="text-lg font-bold text-white mb-1">
              {editingDevice ? 'Edit Device Details' : 'Register New Device'}
            </h3>
            <p className="text-xs text-slate-400 mb-5">
              Updates will pass through Kong Gateway rate limiter and RBAC validation.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Device Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Core-Switch-01"
                  value={formData.deviceName}
                  onChange={(e) => setFormData({ ...formData, deviceName: e.target.value })}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Device Type</label>
                  <select
                    value={formData.deviceType}
                    onChange={(e) => setFormData({ ...formData, deviceType: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                  >
                    <option value="Router">Router</option>
                    <option value="Core-Router">Core-Router</option>
                    <option value="Switch">Switch</option>
                    <option value="Firewall">Firewall</option>
                    <option value="Server">Server</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Status</label>
                  <select
                    value={formData.status}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                  >
                    <option value="ONLINE">ONLINE</option>
                    <option value="OFFLINE">OFFLINE</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">IPv4 Address</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. 192.168.1.1"
                  value={formData.ipAddress}
                  onChange={(e) => setFormData({ ...formData, ipAddress: e.target.value })}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white font-mono focus:outline-none focus:border-cyan-500"
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
                  className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-cyan-500 hover:bg-cyan-400 shadow-lg shadow-cyan-500/20 disabled:opacity-50"
                >
                  {formLoading ? 'Saving...' : editingDevice ? 'Save Changes' : 'Register Device'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

