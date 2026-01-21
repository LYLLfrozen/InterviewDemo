import React, { useState, useEffect } from 'react';
import api, { userApi } from '../../services/api';
import './FriendRequest.css';

interface FriendRequest {
  id: number;
  fromUserId: number;
  toUserId: number;
  status: number;
  createdAt: string;
}

interface User {
  id: number;
  name: string;
  username: string;
}

interface FriendRequestProps {
  currentUserId: number;
}

const FriendRequestComponent: React.FC<FriendRequestProps> = ({ currentUserId }) => {
  const [pendingRequests, setPendingRequests] = useState<FriendRequest[]>([]);
  const [sentRequests, setSentRequests] = useState<FriendRequest[]>([]);
  const [userDetails, setUserDetails] = useState<Map<number, User>>(new Map());
  const [searchUserId, setSearchUserId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'received' | 'sent'>('received');

  // 获取待处理的好友请求
  const fetchPendingRequests = async () => {
    try {
      const response = await api.get('/social/friend-request/pending', { headers: { 'User-Id': currentUserId.toString() } });
      if (response.data.code === 200) {
        setPendingRequests(response.data.data);
        fetchUserDetails(response.data.data.map((req: FriendRequest) => req.fromUserId));
      }
    } catch (err: any) {
      console.error('获取待处理请求失败', err);
    }
  };

  // 获取已发送的好友请求
  const fetchSentRequests = async () => {
    try {
      const response = await api.get('/social/friend-request/sent', { headers: { 'User-Id': currentUserId.toString() } });
      if (response.data.code === 200) {
        setSentRequests(response.data.data);
        fetchUserDetails(response.data.data.map((req: FriendRequest) => req.toUserId));
      }
    } catch (err: any) {
      console.error('获取已发送请求失败', err);
    }
  };

  // 获取用户详细信息
  const fetchUserDetails = async (userIds: number[]) => {
    const detailsMap = new Map<number, User>(userDetails);
    for (const userId of userIds) {
      if (!detailsMap.has(userId)) {
        try {
          const response = await userApi.getUserById(userId);
          if (response.data.code === 200) detailsMap.set(userId, response.data.data);
        } catch (err) {
          // 如果接口不存在或失败，使用默认用户信息
          console.warn(`获取用户 ${userId} 信息失败，使用默认信息`, err);
          detailsMap.set(userId, {
            id: userId,
            name: `用户${userId}`,
            username: `user${userId}`
          } as User);
        }
      }
    }
    setUserDetails(detailsMap);
  };
  // 发送好友请求
  const sendFriendRequest = async () => {
    if (!searchUserId.trim()) {
      setError('请输入用户名');
      return;
    }
    setLoading(true);
    setError(null);
    setSuccessMsg(null);
    try {
      const response = await api.post('/social/friend-request/send', { toUsername: searchUserId.trim() }, { headers: { 'User-Id': currentUserId.toString(), 'Content-Type': 'application/json' } });
      if (response.data.code === 200) {
        setSuccessMsg('好友请求已发送');
        setSearchUserId('');
        fetchSentRequests();
      } else {
        setError(response.data.msg || '发送失败');
      }
    } catch (err: any) {
      setError(err.response?.data?.msg || err.message || '发送失败');
    } finally {
      setLoading(false);
    }
  };

  // 接受好友请求
  const acceptRequest = async (requestId: number) => {
    try {
      const response = await api.post(`/social/friend-request/accept/${requestId}`, {}, { headers: { 'User-Id': currentUserId.toString() } });
      if (response.data.code === 200) {
        setSuccessMsg('已接受好友请求');
        fetchPendingRequests();
      } else {
        setError(response.data.msg || '操作失败');
      }
    } catch (err: any) {
      setError(err.response?.data?.msg || '操作失败');
    }
  };

  // 拒绝好友请求
  const rejectRequest = async (requestId: number) => {
    try {
      const response = await api.post(`/social/friend-request/reject/${requestId}`, {}, { headers: { 'User-Id': currentUserId.toString() } });
      if (response.data.code === 200) {
        setSuccessMsg('已拒绝好友请求');
        fetchPendingRequests();
      } else {
        setError(response.data.msg || '操作失败');
      }
    } catch (err: any) {
      setError(err.response?.data?.msg || '操作失败');
    }
  };

  useEffect(() => {
    fetchPendingRequests();
    fetchSentRequests();
    // 定期刷新
    const interval = setInterval(() => {
      fetchPendingRequests();
      fetchSentRequests();
    }, 15000);
    return () => clearInterval(interval);
  }, [currentUserId]);

  // 清除消息
  useEffect(() => {
    if (error || successMsg) {
      const timer = setTimeout(() => {
        setError(null);
        setSuccessMsg(null);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [error, successMsg]);

  const getStatusText = (status: number) => {
    switch (status) {
      case 0: return '待处理';
      case 1: return '已接受';
      case 2: return '已拒绝';
      default: return '未知';
    }
  };

  return (
    <div className="friend-request-container">
      <h2>好友请求</h2>
        {/* 发送好友请求 */}
      <div className="send-request-section">
        <h3>添加好友</h3>
        <div className="send-request-form">
          <input
            type="text"
            value={searchUserId}
            onChange={(e) => setSearchUserId(e.target.value)}
            placeholder="输入用户名"
            className="user-id-input"
          />
          <button 
            onClick={sendFriendRequest} 
            disabled={loading}
            className="send-btn"
          >
            {loading ? '发送中...' : '发送请求'}
          </button>
        </div>
      </div>

      {/* 消息提示 */}
      {error && <div className="message error-message">{error}</div>}
      {successMsg && <div className="message success-message">{successMsg}</div>}

      {/* 标签页 */}
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'received' ? 'active' : ''}`}
          onClick={() => setActiveTab('received')}
        >
          收到的请求 ({pendingRequests.length})
        </button>
        <button
          className={`tab ${activeTab === 'sent' ? 'active' : ''}`}
          onClick={() => setActiveTab('sent')}
        >
          发送的请求 ({sentRequests.length})
        </button>
      </div>

      {/* 请求列表 */}
      <div className="request-list">
        {activeTab === 'received' ? (
          pendingRequests.length === 0 ? (
            <p className="empty-message">暂无待处理的好友请求</p>
          ) : (
            pendingRequests.map((request) => {
              const user = userDetails.get(request.fromUserId);
              return (
                <div key={request.id} className="request-item">
                  <div className="request-user-info">
                    <div className="user-avatar">
                      {user?.name?.charAt(0) || '?'}
                    </div>
                    <div>
                      <div className="user-name">{user?.name || `用户${request.fromUserId}`}</div>
                      <div className="user-username">@{user?.username || '未知'}</div>
                      <div className="request-time">{new Date(request.createdAt).toLocaleString()}</div>
                    </div>
                  </div>
                  <div className="request-actions">
                    <button 
                      onClick={() => acceptRequest(request.id)}
                      className="accept-btn"
                    >
                      接受
                    </button>
                    <button 
                      onClick={() => rejectRequest(request.id)}
                      className="reject-btn"
                    >
                      拒绝
                    </button>
                  </div>
                </div>
              );
            })
          )
        ) : (
          sentRequests.length === 0 ? (
            <p className="empty-message">暂无已发送的好友请求</p>
          ) : (
            sentRequests.map((request) => {
              const user = userDetails.get(request.toUserId);
              return (
                <div key={request.id} className="request-item">
                  <div className="request-user-info">
                    <div className="user-avatar">
                      {user?.name?.charAt(0) || '?'}
                    </div>
                    <div>
                      <div className="user-name">{user?.name || `用户${request.toUserId}`}</div>
                      <div className="user-username">@{user?.username || '未知'}</div>
                      <div className="request-time">{new Date(request.createdAt).toLocaleString()}</div>
                    </div>
                  </div>
                  <div className="request-status">
                    <span className={`status-badge status-${request.status}`}>
                      {getStatusText(request.status)}
                    </span>
                  </div>
                </div>
              );
            })
          )
        )}
      </div>
    </div>
  );
};

export default FriendRequestComponent;
