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
  const justSentMessageIds = useRef<Set<number>>(new Set()); // 跟踪刚发送的消息ID

  // 消息容器引用与底部状态标记
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const isAtBottomRef = useRef<boolean>(true);

  // 滚动到底部（只有当用户在底部或强制时才滚动）
  const scrollToBottom = (force: boolean = false) => {
    if (force || isAtBottomRef.current) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // 滚动事件处理：更新是否在底部的标记
  const handleMessagesScroll = () => {
    const el = messagesContainerRef.current;
    if (!el) return;
    const threshold = 100; // px，接近底部的容忍距离
    const atBottom = el.scrollHeight - el.scrollTop - el.clientHeight <= threshold;
    isAtBottomRef.current = atBottom;
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
        const serverMessages = response.data.data;
        // 智能合并：保留刚发送的消息（如果服务器还没返回）
        setMessages(prevMessages => {
          // 找出刚发送但服务器还没返回的消息
          const localOnlyMessages = prevMessages.filter(msg => 
            justSentMessageIds.current.has(msg.id) && 
            !serverMessages.find((sm: Message) => sm.id === msg.id)
          );
          // 合并服务器消息和本地临时消息
          const merged = [...serverMessages, ...localOnlyMessages];
          // 清理已经在服务器列表中的消息ID
          serverMessages.forEach((sm: Message) => {
            justSentMessageIds.current.delete(sm.id);
          });
          return merged;
        });
        // 标记消息为已读
        markMessagesAsRead(serverMessages);
        setTimeout(() => scrollToBottom(false), 100);
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
    const messageContent = newMessage.trim();
    
    console.log('准备发送消息:', {
      fromUserId: currentUserId,
      toUserId: friendId,
      content: messageContent
    });
    
    try {
      const response = await api.post(
        '/social/message/send',
        { toUserId: friendId, content: messageContent },
        { headers: { 'User-Id': currentUserId.toString(), 'Content-Type': 'application/json' } }
      );

      console.log('服务器响应:', response.data);

      if (response.data.code === 200) {
        console.log('✅ 消息发送成功');
        
        // 发送成功后，立即将后端返回的消息添加到列表
        const returnedMessage = response.data.data;
        if (returnedMessage && returnedMessage.id) {
          // 标记这是刚发送的消息，防止轮询时被删除
          justSentMessageIds.current.add(returnedMessage.id);
          // 5秒后清理标记（给服务器足够时间同步）
          setTimeout(() => {
            justSentMessageIds.current.delete(returnedMessage.id);
          }, 5000);
          
          // 使用后端返回的完整消息对象
          setMessages(prev => [...prev, returnedMessage]);
          console.log('✅ 消息已添加到列表:', returnedMessage);
        } else {
          console.warn('⚠️ 后端返回的消息对象无效:', returnedMessage);
        }
        setNewMessage('');
        // 立即触发一次消息获取，确保对方能快速看到
        setTimeout(() => fetchMessages(), 500);
        // 滚动到底部显示新消息（发送时强制滚动）
        setTimeout(() => scrollToBottom(true), 100);
      } else {
        const errorMsg = response.data.msg || '发送失败';
        console.error('❌ 发送消息失败:', errorMsg);
        setError(errorMsg);
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.msg || err.message || '发送失败';
      console.error('❌ 发送消息异常:', {
        error: err,
        response: err.response?.data,
        message: errorMsg
      });
      setError(errorMsg);
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
    // 轮询获取新消息（保留轮询但降低频率，且不强制滚动）
    const interval = setInterval(fetchMessages, 1500); // 每1.5秒获取一次
    return () => clearInterval(interval);
  }, [friendId, currentUserId]);

  useEffect(() => {
    scrollToBottom(false);
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
      <div className="chat-messages" ref={messagesContainerRef} onScroll={handleMessagesScroll}>
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
