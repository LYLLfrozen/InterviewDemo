import axios from 'axios';

const API_CONTEXT = '/api';
const API_BASE_URL = `${window.location.origin}${API_CONTEXT}`; // 使用网站所在的 origin（主机/端口），确保请求发向托管该页面的服务器

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 120000, // 增加超时时间到 120s，确保后端慢响应也能收到
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token
    const token = localStorage.getItem('token') || localStorage.getItem('adminToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 未授权，清除 token 并跳转到登录页
      localStorage.removeItem('token');
      localStorage.removeItem('adminToken');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// 用户相关 API
export const userApi = {
  // 用户登录
  login: (username: string, password: string) => {
    return api.post('/user/login', { username, password });
  },

  // 用户注册
  register: (userData: any) => {
    return api.post('/user/register', userData);
  },

  // 获取所有用户
  getAllUsers: () => {
    return api.get('/user');
  },

  // 获取单个用户
  getUserById: (id: string | number) => {
    return api.get(`/user/${id}`);
  },

  // 添加用户
  addUser: (userData: any) => {
    return api.post('/user', userData);
  },

  // 更新用户
  updateUser: (userData: any) => {
    return api.put('/user', userData);
  },

  // 修改用户 id（oldId -> newId）
  changeUserId: (oldId: string | number, newId: string | number) => {
    return api.put('/user/change-id', { oldId, newId });
  },

  // 删除用户（实际是封号）
  deleteUser: (id: string | number) => {
    return api.delete(`/user/${id}`);
  },

  // 分页查询用户
  getUsersByPage: (pageNum: number, pageSize: number) => {
    return api.get('/user/page', {
      params: { pageNum, pageSize }
    });
  },
};

// AI 相关 API
export const aiApi = {
  // 发送消息到 AI，可传入可选的 conversationId，单次请求也使用较长的超时时间作为保险
  chat: (message: string, conversationId?: number | null) => {
    return api.post('/ai/chat', { message, conversationId }, { timeout: 120000 });
  }
};

// API Key 管理 API
export const apiKeyApi = {
  // 设置 DashScope API Key
  setDashscopeApiKey: (apiKey: string) => {
    return api.post('/api-key/dashscope', { apiKey });
  },
  
  // 获取 API Key 配置状态
  getDashscopeApiKeyStatus: () => {
    return api.get('/api-key/dashscope/status');
  },
  
  // 删除 API Key
  deleteDashscopeApiKey: () => {
    return api.delete('/api-key/dashscope');
  }
};

// 会话相关 API
export const conversationApi = {
  list: () => api.get('/conversation/list'),
  create: (title: string, userId?: number | null) => api.post('/conversation/create', { title, userId }),
  delete: (id: number) => api.delete(`/conversation/delete/${id}`),
  messages: (id: number) => api.get(`/conversation/${id}/messages`),
};

export default api;
