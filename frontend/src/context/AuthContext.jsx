import React, { createContext, useContext, useState, useEffect } from 'react';
import apiClient from '../api/client';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('auth_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(false);

  // Helper to encode basic auth token
  const createBasicAuthToken = (username, password) => {
    return 'Basic ' + btoa(`${username}:${password}`);
  };

  const login = async (username, password) => {
    setLoading(true);
    const token = createBasicAuthToken(username, password);
    try {
      // Validate credentials against protected endpoint through Kong
      const res = await apiClient.get('/api/devices', {
        headers: { Authorization: token },
      });

      // Determine role:
      // Try to read /api/users to test if ADMIN; otherwise test if ENGINEER vs VIEWER
      let role = 'VIEWER';
      try {
        await apiClient.get('/api/users', {
          headers: { Authorization: token },
        });
        role = 'ADMIN';
      } catch (err) {
        // If not admin, check if user can create or is engineer
        if (username.toLowerCase().includes('engineer')) {
          role = 'ENGINEER';
        } else {
          role = 'VIEWER';
        }
      }

      const userData = { username, role };
      localStorage.setItem('auth_token', token);
      localStorage.setItem('auth_user', JSON.stringify(userData));
      setUser(userData);
      toast.success(`Welcome back, ${username}! (${role})`);
      return true;
    } catch (error) {
      if (error.response?.status === 401) {
        toast.error('Invalid username or password.');
      } else if (error.response?.status === 429) {
        toast.error('Rate limit exceeded. Please wait a moment.');
      } else {
        toast.error(error.response?.data?.message || 'Authentication failed.');
      }
      return false;
    } finally {
      setLoading(false);
    }
  };

  const register = async (username, password) => {
    setLoading(true);
    try {
      const res = await apiClient.post('/api/users', {
        username,
        password,
      });
      toast.success(`Account created! Assigned role: ${res.data.role}`);
      return res.data;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed.');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    setUser(null);
    toast.success('Logged out successfully.');
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

