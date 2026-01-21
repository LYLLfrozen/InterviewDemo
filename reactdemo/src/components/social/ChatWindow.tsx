import React, { useState, useEffect, useRef } from 'react';
import api, { userApi } from '../../services/api';
import './ChatWindow.css';

interface Message {
  id: number;
  fromUserId: number;
  toUserId: number;
  content: string;
  isRead: number;
  timestamp: string;
}

interface User {
  id: number;
  name: string;
  username: string;
}

interface ChatWindowProps {
  currentUserId: number;
  friendId: number;
  friendName?: string;
  onBack?: () => void;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ currentUserId, friendId, friendName, onBack }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [friend, setFriend] = useState<User | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 获取好友信息
  const fetchFriendInfo = async () => {
    try {
      const response = await userApi.getUserById(friendId);
      if (response.data.code === 200) {
        setFriend(response.data.data);
      }
    } catch (err) {
      // 如果接口不存在或失败，使用默认用户信息（不使用类似 用户3 的回退显示）
      console.warn('获取好友信息失败，使用默认信息', err);
      setFriend({
        id: friendId,
        name: friendName || '未知用户',
        // 如果没有后端返回用户名，则不强行构造 user3 这类用户名，留空使界面不显示 @xxx
        username: friendName ? friendName.replace(/\s+/g, '') : ''
      });
    }
  };

  // 获取聊天消息
  const fetchMessages = async () => {
    try {
      const response = await api.get(`/social/message/chat/${friendId}`, {
        headers: { 'User-Id': currentUserId.toString() },
        params: { limit: 50 }
      });
      if (response.data.code === 200) {
        setMessages(response.data.data);
        // 标记消息为已读
        markMessagesAsRead(response.data.data);
        setTimeout(scrollToBottom, 100);
      }
    } catch (err: any) {
      console.error('获取消息失败', err);
    }
  };

  // 标记消息为已读
  const markMessagesAsRead = async (msgs: Message[]) => {
    const unreadMessages = msgs.filter(
      (msg) => msg.toUserId === currentUserId && msg.isRead === 0
    );
    for (const msg of unreadMessages) {
      try {
        await api.post(`/social/message/read/${msg.id}`, {}, { headers: { 'User-Id': currentUserId.toString() } });
      } catch (err) {
        console.error(`标记消息 ${msg.id} 为已读失败`, err);
      }
    }
  };

  // 发送消息
  const sendMessage = async () => {
    if (!newMessage.trim()) {
      setError('消息内容不能为空');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await api.post(
        '/social/message/send',
        { toUserId: friendId, content: newMessage },
        { headers: { 'User-Id': currentUserId.toString(), 'Content-Type': 'application/json' } }
      );

      if (response.data.code === 200) {
        setNewMessage('');
        // 立即刷新消息列表
        fetchMessages();
      } else {
        setError(response.data.msg || '发送失败');
      }
    } catch (err: any) {
      setError(err.response?.data?.msg || err.message || '发送失败');
    } finally {
      setLoading(false);
    }
  };

  // 处理键盘事件
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  useEffect(() => {
    fetchFriendInfo();
    fetchMessages();
    // 轮询获取新消息
    const interval = setInterval(fetchMessages, 3000); // 每3秒获取一次
    return () => clearInterval(interval);
  }, [friendId, currentUserId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 清除错误消息
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    
    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`;
    
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };
  return (
    <div className="chat-window-container">
      {/* 聊天头部 */}
      <div className="chat-header">
        {onBack && (
          <button className="chat-back-btn" onClick={onBack}>
            ← 返回
          </button>
        )}
        <div className="chat-friend-info">
          <div className="chat-friend-avatar">
            {friend?.name?.charAt(0) || friendName?.charAt(0) || '?'}
          </div>
          <div>
            <div className="chat-friend-name">
              {friend?.name || friendName || '未知用户'}
            </div>
            {friend?.username && (
              <div className="chat-friend-username">@{friend.username}</div>
            )}
          </div>
        </div>
      </div>

      {/* 消息列表 */}
      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="empty-chat">
            <p>暂无聊天记录，开始聊天吧！</p>
          </div>
        ) : (
          messages.map((message) => (
            <div
              key={message.id}
              className={`message-item ${
                message.fromUserId === currentUserId ? 'message-sent' : 'message-received'
              }`}
            >
              <div className="message-bubble">
                <div className="message-content">{message.content}</div>
                <div className="message-time">{formatTime(message.timestamp)}</div>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 错误提示 */}
      {error && <div className="chat-error">{error}</div>}

      {/* 消息输入框 */}
      <div className="chat-input-container">
        <textarea
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="输入消息... (按 Enter 发送，Shift+Enter 换行)"
          className="chat-input"
          rows={3}
        />
        <button
          onClick={sendMessage}
          disabled={loading || !newMessage.trim()}
          className="send-message-btn"
        >
          {loading ? '发送中...' : '发送'}
        </button>
      </div>
    </div>
  );
};

export default ChatWindow;
