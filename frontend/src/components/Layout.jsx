import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const titleMap = {
  '/': 'Network Operations Center Dashboard',
  '/devices': 'Device Inventory & Health',
  '/alarms': 'Alarm & Fault Monitoring',
  '/users': 'Role-Based Access Control & User Directory',
  '/monitoring': 'Kong API Gateway Telemetry & Rate Limiting',
};

export default function Layout() {
  const location = useLocation();
  const title = titleMap[location.pathname] || 'NMS Secure Gateway';

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Header title={title} />
        <main className="flex-1 p-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

