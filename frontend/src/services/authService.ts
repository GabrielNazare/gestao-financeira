import api from './api';

export const authService = {
  login: async (payload: any) => {
    const response = await api.post('/auth/login', payload);
    return response.data;
  },
  register: async (payload: any) => {
    const response = await api.post('/auth/register', payload);
    return response.data;
  },
  googleLogin: async (token: string) => {
    const response = await api.post('/auth/google', { token });
    return response.data;
  }
};
