import React, { useState, useRef, useEffect } from 'react';
import { aiApi, conversationApi, apiKeyApi } from '../../services/api';
import './AiChat.css';

// Web Speech API 类型声明
interface SpeechRecognition extends EventTarget {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  start(): void;
  stop(): void;
  abort(): void;
  onresult: ((this: SpeechRecognition, ev: any) => any) | null;
  onerror: ((this: SpeechRecognition, ev: any) => any) | null;
  onend: ((this: SpeechRecognition, ev: any) => any) | null;
}

declare global {
  interface Window {
    SpeechRecognition: {
      new(): SpeechRecognition;
    };
    webkitSpeechRecognition: {
      new(): SpeechRecognition;
    };
  }
}

interface Message {
  id: string;
  content: string;
  role: 'user' | 'assistant';
  timestamp: Date;
}

interface AiChatProps {
  isOpen: boolean;
  onClose: () => void;
}

const AiChat: React.FC<AiChatProps> = ({ isOpen, onClose }) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      content: '你好！我是智能助手小智，有什么可以帮助你的吗？',
      role: 'assistant',
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 语音识别状态
  const [isListening, setIsListening] = useState(false);
  const recognitionRef = useRef<SpeechRecognition | null>(null);

  // 会话列表与当前会话 ID
  const [conversations, setConversations] = useState<Array<{ id: number; title: string }>>([]);
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(null);
  // 初始化语音识别
  useEffect(() => {
    // 检查浏览器是否支持 Web Speech API
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    
    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.lang = 'zh-CN'; // 设置中文识别
      recognition.continuous = true; // 启用连续识别，不会自动停止
      recognition.interimResults = true; // 显示中间结果

      // 识别结果处理
      recognition.onresult = (event: any) => {
        const transcript = Array.from(event.results)
          .map((result: any) => result[0])
          .map((result: any) => result.transcript)
          .join('');
        
        setInputValue(transcript);
      };

      // 识别结束（仅在用户主动停止或发生错误时触发）
      recognition.onend = () => {
        setIsListening(false);
      };

      // 识别错误
      recognition.onerror = (event: any) => {
        console.error('语音识别错误:', event.error);
        
        // 只有在严重错误时才停止
        if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
          setIsListening(false);
          alert('请允许麦克风权限以使用语音输入功能');
        } else if (event.error === 'network') {
          setIsListening(false);
          alert('网络错误，请检查网络连接');
        }
        // 对于 'no-speech' 等错误，不停止识别，继续监听
      };

      recognitionRef.current = recognition;
    }

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
    };
  }, []);

  // 切换语音识别
  const toggleVoiceInput = () => {
    if (!recognitionRef.current) {
      alert('抱歉，您的浏览器不支持语音识别功能。请使用 Chrome、Edge 等现代浏览器。');
      return;
    }

    if (isListening) {
      recognitionRef.current.stop();
      setIsListening(false);
    } else {
      recognitionRef.current.start();
      setIsListening(true);
    }
  };

  // 自动滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 加载会话列表
  const loadConversations = async () => {
    try {
      const res = await conversationApi.list();
      if (res.data.code === 200) setConversations(res.data.data || []);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadConversations();
  }, []);

  // 选中会话后加载历史消息
  const loadMessagesForConversation = async (id: number) => {
    try {
      const res = await conversationApi.messages(id);
      if (res.data.code === 200) {
        const msgs = (res.data.data || []).map((m: any) => ({
          id: String(m.id),
          content: m.content,
          role: m.role === 'ai' ? 'assistant' : 'user',
          timestamp: new Date(m.createdAt || m.created_at),
        }));
        setMessages(msgs);
        setCurrentConversationId(id);
      }
    } catch (e) {
      console.error(e);
    }
  };

  // 创建会话
  const handleCreateConversation = async () => {
    const title = prompt('请输入会话标题（可留空）') || '新会话';
    try {
      const res = await conversationApi.create(title);
      if (res.data.code === 200) {
        await loadConversations();
      } else {
        alert('创建失败: ' + res.data.msg);
      }
    } catch (e) {
      console.error(e);
      alert('创建失败');
    }
  };

  const handleDeleteConversation = async (id: number) => {
    if (!confirm('确认删除该会话及其消息？')) return;
    try {
      const res = await conversationApi.delete(id);
      if (res.data.code === 200) {
        setMessages([]);
        setCurrentConversationId(null);
        await loadConversations();
      } else {
        alert('删除失败: ' + res.data.msg);
      }
    } catch (e) {
      console.error(e);
      alert('删除失败');
    }
  };

  // 设置 DashScope API Key（点击按钮后输入并提交到后端）
  const handleSetApiKey = async () => {
    const apiKey = prompt('请输入 DashScope API Key（输入后将保存到服务器）');
    if (!apiKey) return;
    try {
      const res = await apiKeyApi.setDashscopeApiKey(apiKey);
      if (res?.data?.code === 200) {
        alert('API Key 设置成功');
      } else {
        alert('设置失败: ' + (res?.data?.msg || '未知错误'));
      }
    } catch (e) {
      console.error('设置 API Key 失败', e);
      alert('设置失败，请检查网络或后端是否运行');
    }
  };

  // 发送消息（包含 conversationId）
  const handleSendMessage = async () => {
    if (!inputValue.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue,
      role: 'user',
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const response = await aiApi.chat(inputValue, currentConversationId ?? undefined);
      const { code, data, message } = response.data;

      if (code === 200) {
        const aiMessage: Message = {
          id: (Date.now() + 1).toString(),
          content: data,
          role: 'assistant',
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, aiMessage]);
      } else {
        const errorMessage: Message = {
          id: (Date.now() + 1).toString(),
          content: `抱歉，出现了错误：${message || '未知错误'}`,
          role: 'assistant',
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, errorMessage]);
      }
    } catch (error) {
      console.error('AI 请求失败:', error);
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: '抱歉，AI 服务暂时不可用，请稍后再试。',
        role: 'assistant',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  // 处理键盘事件
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  if (!isOpen) return null;
  return (
    <div className="ai-chat-overlay" onClick={onClose}>
      <div className="ai-chat-container" onClick={(e) => e.stopPropagation()}>
        {/* 左侧边栏 */}
        <div className="ai-chat-sidebar">
          <div className="sidebar-header">
            <span className="ai-avatar">🤖</span>
            <h3>AI 智能助手</h3>
          </div>
          <button className="new-chat-btn" onClick={handleCreateConversation}>
            新建会话
          </button>
          <div className="conversation-list">
            {conversations.map((c) => (
              <div
                key={c.id}
                className={`conversation-item ${currentConversationId === c.id ? 'active' : ''}`}
                onClick={() => loadMessagesForConversation(c.id)}
              >
                <span className="conversation-title">{c.title}</span>
                <button
                  className="delete-btn"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteConversation(c.id);
                  }}
                >
                  🗑️
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* 右侧聊天区域 */}
        <div className="ai-chat-main">
          <div className="ai-chat-header">
            <div className="ai-chat-title">
              <span>{currentConversationId ? conversations.find(c => c.id === currentConversationId)?.title || '会话' : '新会话'}</span>
            </div>
            <button className="api-key-btn" onClick={handleSetApiKey} title="设置 DashScope API Key">设置 API Key</button>
            <button className="close-btn" onClick={onClose}>✕</button>
          </div>

          {/* 聊天消息区域 */}
          <div className="ai-chat-messages">
            {messages.map((msg) => (
              <div key={msg.id} className={`message ${msg.role}`}>
                <div className="message-avatar">{msg.role === 'assistant' ? '🤖' : '👤'}</div>
                <div className="message-content">
                  <div className="message-text">{msg.content}</div>
                  <div className="message-time">{msg.timestamp.toLocaleTimeString()}</div>
                </div>
              </div>
            ))}            {isLoading && (
              <div className="message assistant">
                <div className="message-avatar">🤖</div>
                <div className="message-content">
                  <div className="message-text typing">
                    <span className="typing-dot"></span>
                    <span className="typing-dot"></span>
                    <span className="typing-dot"></span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* 输入区域 */}
          <div className="ai-chat-input">
            <textarea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={isListening ? '正在监听...' : '输入消息，按 Enter 发送...'}
              rows={2}
              disabled={isLoading}
            />
            <div className="input-buttons">
              <button
                className={`voice-btn ${isListening ? 'listening' : ''}`}
                onClick={toggleVoiceInput}
                disabled={isLoading}
                title={isListening ? '停止语音输入' : '开始语音输入'}
              >
                {isListening ? '🎤 停止' : '🎤'}
              </button>
              <button
                className="send-btn"
                onClick={handleSendMessage}
                disabled={!inputValue.trim() || isLoading}
              >
                发送
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AiChat;
