import React, { useState, useEffect } from 'react';
import apiClient from '../api/client';
import { useAuth } from '../context/AuthContext';
import { 
  Users as UsersIcon, 
  UserPlus, 
  ShieldAlert, 
  ShieldCheck, 
  RefreshCw, 
  Lock,
  CheckCircle2,
  AlertCircle,
  UserCheck,
  Shield
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function Users() {
  const { user } = useAuth();
  const [usersList, setUsersList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [forbidden, setForbidden] = useState(false);
  const [updatingUserId, setUpdatingUserId] = useState(null);

  // New user modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [modalLoading, setModalLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    setForbidden(false);
    try {
      const res = await apiClient.get('/api/users');
      setUsersList(res.data || []);
    } catch (err) {
      if (err.response?.status === 403) {
        setForbidden(true);
      } else {
        toast.error('Failed to load user directory.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRegisterUser = async (e) => {
    e.preventDefault();
    if (!newUsername.trim() || !newPassword.trim()) return;

    setModalLoading(true);
    try {
      await apiClient.post('/api/users', {
        username: newUsername,
        password: newPassword,
      });
      toast.success(`User "${newUsername}" created with role VIEWER.`);
      setIsModalOpen(false);
      setNewUsername('');
      setNewPassword('');
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to register user.');
    } finally {
      setModalLoading(false);
    }
  };

  const handleRoleChange = async (targetUser, newRole) => {
    if (targetUser.role === newRole) return;

    const confirmed = window.confirm(
      `Change role for user "${targetUser.username}" from ${targetUser.role} to ${newRole}?`
    );
    if (!confirmed) return;

    setUpdatingUserId(targetUser.id);
    try {
      const res = await apiClient.put(`/api/users/${targetUser.id}/role`, {
        role: newRole,
      });
      toast.success(`Role for "${targetUser.username}" updated to ${res.data.role}`);
      // Update local state immediately
      setUsersList((prev) =>
        prev.map((u) => (u.id === targetUser.id ? { ...u, role: res.data.role } : u))
      );
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update user role.');
      fetchUsers();
    } finally {
      setUpdatingUserId(null);
    }
  };

  if (forbidden || user?.role !== 'ADMIN') {
    return (
      <div className="max-w-2xl mx-auto mt-12 p-8 rounded-3xl bg-slate-900 border border-rose-500/30 shadow-2xl text-center space-y-4">
        <div className="inline-flex p-4 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20">
          <ShieldAlert className="w-10 h-10 animate-bounce" />
        </div>
        <h2 className="text-xl font-bold text-white">403 Forbidden: RBAC Access Restriction</h2>
        <p className="text-sm text-slate-400 leading-relaxed">
          The endpoint <code className="text-rose-300 font-mono">GET /api/users</code> is restricted exclusively to the <span className="font-bold text-rose-400">ADMIN</span> role. 
          Your active authenticated role is <span className="font-bold text-cyan-400">{user?.role}</span>.
        </p>
        <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 text-xs text-slate-300 text-left space-y-1.5 font-mono">
          <p className="text-cyan-400 font-bold">// Spring Security Rule Enforced:</p>
          <p>.requestMatchers("/api/users/**").hasRole("ADMIN")</p>
          <p className="text-emerald-400">Status: Enforced at Spring Security & Gateway</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white">User Directory & RBAC</h1>
          <p className="text-sm text-slate-400">Manage gateway users, assign roles (VIEWER &rarr; ENGINEER / ADMIN), and enforce security privileges</p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={fetchUsers}
            disabled={loading}
            className="p-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl transition-all"
            title="Refresh Users"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold rounded-xl text-xs shadow-lg shadow-cyan-500/20 transition-all"
          >
            <UserPlus className="w-4 h-4" />
            <span>Create New User</span>
          </button>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {loading ? (
          <div className="p-12 text-center text-slate-400 text-sm flex flex-col items-center justify-center space-y-2">
            <RefreshCw className="w-6 h-6 animate-spin text-cyan-400" />
            <span>Verifying ADMIN authority and loading directory...</span>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold tracking-wider bg-slate-950/40">
                  <th className="py-3.5 px-4">User ID</th>
                  <th className="py-3.5 px-4">Username</th>
                  <th className="py-3.5 px-4">Current Role</th>
                  <th className="py-3.5 px-4">Security Level</th>
                  <th className="py-3.5 px-4">Admin Role Control</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {usersList.map((u) => {
                  const isSelf = u.username === user?.username;
                  const isUpdating = updatingUserId === u.id;

                  return (
                    <tr key={u.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="py-3.5 px-4 font-mono font-bold text-slate-400">#{u.id}</td>
                      <td className="py-3.5 px-4 font-bold text-white flex items-center space-x-2">
                        <div className="w-7 h-7 rounded-lg bg-slate-800 flex items-center justify-center text-xs font-bold text-cyan-400">
                          {u.username.substring(0, 1).toUpperCase()}
                        </div>
                        <div className="flex flex-col">
                          <span>{u.username}</span>
                          {isSelf && (
                            <span className="text-[10px] text-cyan-400 font-normal">Active Session (You)</span>
                          )}
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <span className={`px-2.5 py-0.5 rounded-full font-bold uppercase text-[10px] ${
                          u.role === 'ADMIN' ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' :
                          u.role === 'ENGINEER' ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30' :
                          'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                        }`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="text-slate-400">
                          {u.role === 'ADMIN' ? 'Full System & User Control' :
                           u.role === 'ENGINEER' ? 'Read/Write Devices & Alarms' :
                           'Read-Only Access'}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        {isSelf ? (
                          <span className="inline-flex items-center space-x-1.5 text-xs text-slate-500 italic">
                            <Lock className="w-3.5 h-3.5" />
                            <span>Self-lockout protected</span>
                          </span>
                        ) : (
                          <div className="flex items-center space-x-2">
                            <select
                              value={u.role}
                              disabled={isUpdating}
                              onChange={(e) => handleRoleChange(u, e.target.value)}
                              className="bg-slate-950 border border-slate-700 hover:border-cyan-500 text-white font-medium text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-cyan-500 transition-colors disabled:opacity-50 cursor-pointer"
                            >
                              <option value="VIEWER">VIEWER (Read-Only)</option>
                              <option value="ENGINEER">ENGINEER (Read/Write)</option>
                              <option value="ADMIN">ADMIN (Full Control)</option>
                            </select>
                            {isUpdating && (
                              <RefreshCw className="w-3.5 h-3.5 text-cyan-400 animate-spin" />
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl relative">
            <h3 className="text-lg font-bold text-white mb-1">Create User</h3>
            <p className="text-xs text-slate-400 mb-5">
              New registrations are assigned the <b>VIEWER</b> role by default. As an <b>ADMIN</b>, you can promote them to <b>ENGINEER</b> or <b>ADMIN</b> anytime from the directory table.
            </p>

            <form onSubmit={handleRegisterUser} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Username</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. operator_alex"
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl p-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Password</label>
                <input
                  type="password"
                  required
                  placeholder="Min 6 characters"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
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
                  disabled={modalLoading}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-white bg-cyan-500 hover:bg-cyan-400 shadow-lg shadow-cyan-500/20 disabled:opacity-50"
                >
                  {modalLoading ? 'Creating...' : 'Register User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
