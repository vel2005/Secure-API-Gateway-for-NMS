import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Server, 
  AlertTriangle, 
  Users, 
  Activity, 
  ShieldCheck, 
  LogOut,
  Radio
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Device Inventory', path: '/devices', icon: Server },
  { name: 'Active Alarms', path: '/alarms', icon: AlertTriangle },
  { name: 'User Management', path: '/users', icon: Users, adminOnly: true },
  { name: 'API Gateway Monitor', path: '/monitoring', icon: Activity },
];

export default function Sidebar() {
  const { user, logout } = useAuth();

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col h-screen sticky top-0">
      {/* Brand */}
      <div className="p-6 border-b border-slate-800 flex items-center space-x-3">
        <div className="bg-gradient-to-tr from-cyan-500 to-blue-600 p-2 rounded-xl shadow-lg shadow-cyan-500/20">
          <Radio className="w-6 h-6 text-white animate-pulse" />
        </div>
        <div>
          <h1 className="text-lg font-bold text-white tracking-wide">NMS Gateway</h1>
          <p className="text-xs text-cyan-400 font-mono">Northbound Interface</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isAdminRestricted = item.adminOnly && user?.role !== 'ADMIN';

          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `
                flex items-center justify-between px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200
                ${isActive 
                  ? 'bg-gradient-to-r from-cyan-500/10 to-blue-500/10 text-cyan-400 border border-cyan-500/30 shadow-sm' 
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                }
                ${isAdminRestricted ? 'opacity-50' : ''}
              `}
            >
              <div className="flex items-center space-x-3">
                <Icon className="w-5 h-5" />
                <span>{item.name}</span>
              </div>
              {item.adminOnly && (
                <span className="text-[10px] uppercase font-semibold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                  Admin
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* User profile & logout */}
      <div className="p-4 border-t border-slate-800 bg-slate-900/50">
        <div className="flex items-center justify-between p-2 rounded-xl bg-slate-800/40 border border-slate-700/50">
          <div className="flex items-center space-x-3 truncate">
            <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-slate-700 to-slate-600 flex items-center justify-center font-bold text-white uppercase text-sm">
              {user?.username?.substring(0, 2) || 'US'}
            </div>
            <div className="truncate">
              <p className="text-sm font-semibold text-white truncate">{user?.username}</p>
              <span className={`inline-flex items-center text-[11px] font-bold px-2 py-0.5 rounded-full mt-0.5 ${
                user?.role === 'ADMIN' 
                  ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' 
                  : user?.role === 'ENGINEER'
                  ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                  : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
              }`}>
                {user?.role}
              </span>
            </div>
          </div>
          <button
            onClick={logout}
            title="Log out"
            className="p-2 text-slate-400 hover:text-rose-400 hover:bg-slate-700/50 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}

