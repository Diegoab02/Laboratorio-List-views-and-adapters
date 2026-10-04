import axios from 'axios';

const API = 'http://localhost:8080/api/auth';

export const register = async (data) => {
  const response = await axios.post(`${API}/register`, data);
  return response.data;
};

export const login = async (data) => {
  const response = await axios.post(`${API}/login`, data);
  return response.data;
};

export const forgotPassword = async (email) => {
  const response = await axios.post(`${API}/forgot-password`, { email });
  return response.data;
};

export const resetPassword = async (token, password) => {
  const response = await axios.post(`${API}/reset-password`, { token, password });
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const getToken = () => localStorage.getItem('token');

export const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export const isAuthenticated = () => !!getToken();
