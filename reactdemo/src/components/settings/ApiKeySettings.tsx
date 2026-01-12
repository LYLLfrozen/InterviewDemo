import React, { useState, useEffect } from 'react';
import { apiKeyApi } from '../../services/api';
import './ApiKeySettings.css';

interface ApiKeyStatus {
  configured: boolean;
  maskedKey?: string;
}

const ApiKeySettings: React.FC = () => {
  const [apiKey, setApiKey] = useState('');
  const [status, setStatus] = useState<ApiKeyStatus>({ configured: false });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // 加载 API Key 状态
  const loadStatus = async () => {
    try {
      const response = await apiKeyApi.getDashscopeApiKeyStatus();
      if (response.data.code === 200) {
        setStatus(response.data.data);
      }
    } catch (error) {
      console.error('加载 API Key 状态失败', error);
    }
  };

  useEffect(() => {
    loadStatus();
  }, []);

  // 设置 API Key
  const handleSetApiKey = async () => {
    if (!apiKey.trim()) {
      setMessage('请输入 API Key');
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const response = await apiKeyApi.setDashscopeApiKey(apiKey);
      if (response.data.code === 200) {
        setMessage('API Key 设置成功！');
        setApiKey('');
        await loadStatus();
      } else {
        setMessage(`设置失败: ${response.data.msg}`);
      }
    } catch (error: any) {
      setMessage(`设置失败: ${error.message || '未知错误'}`);
    } finally {
      setLoading(false);
    }
  };

  // 删除 API Key
  const handleDeleteApiKey = async () => {
    if (!window.confirm('确定要删除 API Key 吗？')) {
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const response = await apiKeyApi.deleteDashscopeApiKey();
      if (response.data.code === 200) {
        setMessage('API Key 已删除');
        await loadStatus();
      } else {
        setMessage(`删除失败: ${response.data.msg}`);
      }
    } catch (error: any) {
      setMessage(`删除失败: ${error.message || '未知错误'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="api-key-settings">
      <h2>AI 模型配置</h2>
      
      <div className="status-section">
        <h3>当前状态</h3>
        {status.configured ? (
          <div className="status-info">
            <p className="status-configured">✓ 已配置</p>
            {status.maskedKey && (
              <p className="masked-key">当前 API Key: {status.maskedKey}</p>
            )}
            <button 
              onClick={handleDeleteApiKey} 
              disabled={loading}
              className="delete-button"
            >
              删除 API Key
            </button>
          </div>
        ) : (
          <p className="status-not-configured">✗ 未配置</p>
        )}
      </div>

      <div className="input-section">
        <h3>设置 DashScope API Key</h3>
        <p className="help-text">
          请在 <a href="https://dashscope.console.aliyun.com/apiKey" target="_blank" rel="noopener noreferrer">
            阿里云 DashScope 控制台
          </a> 获取您的 API Key
        </p>
        
        <div className="input-group">
          <input
            type="password"
            value={apiKey}
            onChange={(e) => setApiKey(e.target.value)}
            placeholder="sk-xxxxxxxxxxxxxxxxxxxxxxxx"
            disabled={loading}
            className="api-key-input"
          />
          <button 
            onClick={handleSetApiKey} 
            disabled={loading || !apiKey.trim()}
            className="set-button"
          >
            {loading ? '设置中...' : '设置 API Key'}
          </button>
        </div>

        {message && (
          <div className={`message ${message.includes('成功') ? 'success' : 'error'}`}>
            {message}
          </div>
        )}
      </div>

      <div className="info-section">
        <h3>说明</h3>
        <ul>
          <li>API Key 安全地存储在服务器的 Redis 中</li>
          <li>不会在代码中泄露或保存到版本控制系统</li>
          <li>每次使用 AI 功能时会自动从 Redis 读取</li>
          <li>您可以随时更新或删除 API Key</li>
        </ul>
      </div>
    </div>
  );
};

export default ApiKeySettings;