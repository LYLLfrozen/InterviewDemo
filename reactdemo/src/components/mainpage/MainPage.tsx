import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { userApi } from '../../services/api';
import AiChat from '../aichat/AiChat';
import SocialPage from '../social/SocialPage';
import './MainPage.css';

interface Article {
  id: string;
  title: string;
  content: string;
  author: string;
  category?: string;
  position?: number;
  createTime: string;
  updateTime: string;
}

interface User {
  id: number;
  username: string;
  email?: string;
}

const MainPage: React.FC = () => {
  const navigate = useNavigate();
  const [articles, setArticles] = useState<Article[]>([]);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showAiChat, setShowAiChat] = useState(false);
  const [newArticle, setNewArticle] = useState({
    title: '',
    content: '',
    author: '',
    category: '',
  });
  const [isMobile, setIsMobile] = useState(false);
  const [showSocial, setShowSocial] = useState(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  // 分类相关
  const [categories, setCategories] = useState<string[]>([]);
  const [newCategory, setNewCategory] = useState('');

  // 编辑模式
  const [editingArticle, setEditingArticle] = useState<Article | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  // 获取当前用户信息
  const loadCurrentUser = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.warn('未找到登录 token');
        return;
      }

      const response = await userApi.getCurrentUser();
      const { code, data, msg } = response.data;

      if (code === 200 && data) {
        setCurrentUser(data as User);
      } else {
        // 如果后端提示 token 无效或已过期，清除本地 token 并跳转到登录页
        const lowerMsg = (msg || '').toString().toLowerCase();
        if (lowerMsg.includes('token') || lowerMsg.includes('过期') || lowerMsg.includes('无效')) {
          console.warn('登录信息已失效，正在登出：', msg);
          localStorage.removeItem('token');
          localStorage.removeItem('adminToken');
          alert('登录已过期或无效，请重新登录');
          navigate('/');
          return;
        }

        console.warn('获取当前用户信息失败:', msg);
      }
    } catch (error: any) {
      console.error('获取当前用户信息出错:', error);
      // 如果返回的错误对象里包含 token 过期提示，也进行清理
      const message = (error?.response?.data?.msg || error?.message || '').toString().toLowerCase();
      if (message.includes('token') || message.includes('过期') || message.includes('无效')) {
        localStorage.removeItem('token');
        localStorage.removeItem('adminToken');
        alert('登录已过期或无效，请重新登录');
        navigate('/');
      }
    }
  };

  // 加载文章列表
  const loadArticles = async () => {
    setIsLoading(true);
    try {
      const response = await api.get('/mainPage');
      const { code, data } = response.data;

      if (code === 200) {
        const list: Article[] = data || [];
        setArticles(list);
        const cats = Array.from(new Set(list.map(a => a.category || '未分类')));
        setCategories(cats);
      } else {
        console.error('加载文章列表失败');
      }
    } catch (error) {
      console.error('加载文章列表出错:', error);
    } finally {
      setIsLoading(false);
    }
  };
  useEffect(() => {
    loadCurrentUser();
    loadArticles();
  }, []);

  // 响应式：移动端检测
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 选择文章
  const handleSelectArticle = async (articleId: string) => {
    try {
      if (editingArticle && editingArticle.id === articleId) {
        setSelectedArticle(editingArticle);
        return;
      }

      const response = await api.get(`/mainPage/${articleId}`);
      const { code, data } = response.data;

      if (code === 200) {
        setSelectedArticle(data as Article);
        setEditingArticle(null);
      } else {
        alert('加载文章失败');
      }
    } catch (error) {
      console.error('加载文章详情出错:', error);
      alert('加载文章详情失败');
    }
  };

  // 创建文章
  const handleCreateArticle = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!newArticle.title.trim() || !newArticle.content.trim()) {
      alert('标题和内容不能为空');
      return;
    }

    try {
      const response = await api.post('/mainPage', newArticle);
      const { code } = response.data;

      if (code === 200) {
        alert('文章创建成功！');
        setShowCreateForm(false);
        setNewArticle({ title: '', content: '', author: '', category: '' });
        loadArticles();
      } else {
        alert('创建文章失败');
      }
    } catch (error) {
      console.error('创建文章出错:', error);
      alert('创建文章失败');
    }
  };

  // 删除文章
  const handleDeleteArticle = async (articleId: string) => {
    if (!confirm('确定要删除这篇文章吗？')) return;

    try {
      const response = await api.delete(`/mainPage/${articleId}`);
      const { code } = response.data;

      if (code === 200) {
        alert('文章删除成功！');
        if (selectedArticle?.id === articleId) setSelectedArticle(null);
        loadArticles();
      } else {
        alert('删除文章失败');
      }
    } catch (error) {
      console.error('删除文章出错:', error);
      alert('删除文章失败');
    }
  };

  // 创建分类
  const handleCreateCategory = (catParam?: string) => {
    const cat = (catParam !== undefined ? catParam : newCategory).trim();
    if (!cat) return '';
    if (!categories.includes(cat)) setCategories(prev => [...prev, cat]);
    setNewCategory('');
    return cat;
  };

  // 编辑文章
  const handleEditArticle = (article: Article) => {
    setEditingArticle({ ...article });
  };

  const handleCancelEdit = () => setEditingArticle(null);

  const handleSaveEdit = async () => {
    if (!editingArticle) return;
    if (!editingArticle.title?.trim() || !editingArticle.content?.trim()) {
      alert('标题和内容不能为空');
      return;
    }

    try {
      const payload: any = {
        title: editingArticle.title,
        content: editingArticle.content,
        author: editingArticle.author,
        category: editingArticle.category,
      };
      if (editingArticle.position !== undefined) payload.position = editingArticle.position;

      const response = await api.put(`/mainPage/${editingArticle.id}`, payload);
      const { code, data } = response.data;
      if (code === 200) {
        alert('保存成功');
        setSelectedArticle(data as Article);
        setEditingArticle(null);
        loadArticles();
      } else {
        alert('保存失败');
      }
    } catch (error) {
      console.error('保存编辑出错:', error);
      alert('保存失败');
    }
  };

  // 移动文章
  const handleMoveArticle = async (articleId: string, direction: 'up' | 'down') => {
    const idx = articles.findIndex(a => a.id === articleId);
    if (idx === -1) return;
    const targetIdx = direction === 'up' ? idx - 1 : idx + 1;
    if (targetIdx < 0 || targetIdx >= articles.length) return;

    const a = articles[idx];
    const b = articles[targetIdx];
    const posA = a.position ?? (idx + 1);
    const posB = b.position ?? (targetIdx + 1);

    try {
      await api.put(`/mainPage/${a.id}`, { position: -1 });
      await api.put(`/mainPage/${b.id}`, { position: posA });
      await api.put(`/mainPage/${a.id}`, { position: posB });
      loadArticles();
    } catch (error) {
      console.error('移动文章出错:', error);
      alert('移动失败');
    }
  };

  return (
    <div className="main-page">
      {/* Header */}
      <div className="main-header">
        <h1>GU系统</h1>
        <div className="header-actions">
          <button className="shop-btn" onClick={() => navigate('/shop/products')}>🛒 商品中心</button>
          <button className="ai-chat-btn" onClick={() => setShowAiChat(true)}>🤖 AI助手</button>
          <button className="social-btn" onClick={() => setShowSocial(prev => !prev)}>👥 社交</button>
          <button className="create-btn" onClick={() => setShowCreateForm(true)}>+ 新建文章</button>
        </div>
      </div>

      {/* Main Content */}
      <div className="main-content">
        {showSocial ? (
          <div style={{ width: '100%' }}>
            <div style={{ display: 'flex', alignItems: 'center', padding: '15px 20px', background: 'white', borderRadius: '10px', marginBottom: '20px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
              <button 
                onClick={() => setShowSocial(false)}
                style={{ 
                  padding: '8px 16px', 
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
                  color: 'white', 
                  border: 'none', 
                  borderRadius: '5px', 
                  cursor: 'pointer',
                  fontSize: '14px',
                  transition: 'transform 0.2s',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '5px'
                }}
                onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
                onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}              >
                ← 返回主界面
              </button>
              <h2 style={{ margin: '0 0 0 20px', color: '#333', fontSize: '20px' }}>社交中心</h2>
            </div>
            {currentUser ? (
              <SocialPage currentUserId={currentUser.id} />
            ) : (
              <div style={{ 
                padding: '40px', 
                textAlign: 'center', 
                background: 'white', 
                borderRadius: '10px', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
              }}>
                <p style={{ fontSize: '16px', color: '#666' }}>正在加载用户信息...</p>
                <p style={{ fontSize: '14px', color: '#999', marginTop: '10px' }}>如果长时间无响应，请尝试重新登录</p>
              </div>
            )}
          </div>
        ) : (
          <div className="content-split">
            {/* 左侧列表 */}
            {(!isMobile || !selectedArticle) && (
              <div className="article-list">
                <h2>文章目录</h2>
                {isLoading ? (
                  <div className="loading">加载中...</div>
                ) : articles.length === 0 ? (
                  <div className="empty">暂无文章</div>
                ) : selectedCategory === '' ? (
                  <div className="category-list">
                    <ul>
                      {categories.map(cat => (
                        <li key={cat}>
                          <button className="move-btn" onClick={() => setSelectedCategory(cat)}>
                            {cat} <span style={{ marginLeft: 8, color: '#666' }}>({articles.filter(a => (a.category || '未分类') === cat).length})</span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                      <strong>{selectedCategory}</strong>
                      <button className="move-btn" onClick={() => setSelectedCategory('')}>← 返回分类</button>
                    </div>
                    <ul>
                      {articles.filter(a => (a.category || '未分类') === selectedCategory).length === 0 ? (
                        <li className="empty">该分类下暂无文章</li>
                      ) : (
                        articles.filter(a => (a.category || '未分类') === selectedCategory).map((article, index) => (
                          <li key={article.id} className={selectedArticle?.id === article.id ? 'active' : ''}>
                            <div className="article-item" onClick={() => handleSelectArticle(article.id)}>
                              <h3>{article.title}</h3>
                              <p className="article-meta">
                                <span>作者: {article.author || '匿名'}</span>
                                <span>分类: {article.category || '未分类'}</span>
                                <span>{new Date(article.createTime).toLocaleDateString()}</span>
                              </p>
                            </div>
                            <div className="article-actions">
                              <button className="move-btn" title="上移" onClick={(e) => { e.stopPropagation(); handleMoveArticle(article.id, 'up'); }} disabled={index === 0}>↑</button>
                              <button className="move-btn" title="下移" onClick={(e) => { e.stopPropagation(); handleMoveArticle(article.id, 'down'); }} disabled={index === articles.length - 1}>↓</button>
                              <button className="edit-btn" title="编辑" onClick={(e) => { e.stopPropagation(); setSelectedArticle(article); handleEditArticle(article); }}>编辑</button>
                              <button className="delete-btn" onClick={(e) => { e.stopPropagation(); handleDeleteArticle(article.id); }}>删除</button>
                            </div>
                          </li>
                        ))
                      )}
                    </ul>
                  </div>
                )}
              </div>
            )}

            {/* 右侧文章详情/编辑 */}
            {(!isMobile || selectedArticle) && (
              <div className={`article-detail ${editingArticle ? 'editing' : ''}`}>
                {isMobile && selectedArticle && (
                  <button className="back-btn" onClick={() => setSelectedArticle(null)}>← 返回</button>
                )}

                {selectedArticle ? (
                  <>
                    {!editingArticle && (
                      <div>
                        <h2>{selectedArticle.title}</h2>
                        <div className="article-meta">
                          <span>作者: {selectedArticle.author || '匿名'}</span>
                          <span>分类: {selectedArticle.category || '未分类'}</span>
                          <span>创建时间: {new Date(selectedArticle.createTime).toLocaleString()}</span>
                        </div>
                        <div className="article-content">{selectedArticle.content}</div>
                      </div>
                    )}

                    {editingArticle && (
                      <div className="edit-form">
                        <h2>编辑文章</h2>
                        <div className="form-group">
                          <label>标题 *</label>
                          <input type="text" value={editingArticle?.title ?? ''} onChange={(e) => setEditingArticle(prev => prev ? { ...prev, title: e.target.value } : prev)} />
                        </div>
                        <div className="form-group">
                          <label>作者</label>
                          <input type="text" value={editingArticle?.author ?? ''} onChange={(e) => setEditingArticle(prev => prev ? { ...prev, author: e.target.value } : prev)} />
                        </div>
                        <div className="form-group">
                          <label>分类</label>
                          <select value={editingArticle?.category ?? ''} onChange={(e) => setEditingArticle(prev => prev ? { ...prev, category: e.target.value } : prev)}>
                            <option value="">未分类</option>
                            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                          </select>
                          <div style={{ display: 'flex', marginTop: 6 }}>
                            <input type="text" placeholder="新分类名" value={newCategory} onChange={(e) => setNewCategory(e.target.value)} />
                            <button type="button" className="category-add-btn" onClick={() => {
                              const cat = newCategory.trim();
                              if (!cat) return;
                              const created = handleCreateCategory(cat);
                              if (created) setEditingArticle(prev => prev ? { ...prev, category: created } : prev);
                            }}>添加分类并使用</button>
                          </div>
                        </div>
                        <div className="form-group">
                          <label>内容 *</label>
                          <textarea value={editingArticle?.content ?? ''} onChange={(e) => setEditingArticle(prev => prev ? { ...prev, content: e.target.value } : prev)} rows={10} />
                        </div>
                        <div className="form-actions">
                          <button onClick={handleCancelEdit}>取消</button>
                          <button onClick={handleSaveEdit}>保存</button>
                        </div>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="no-selection"><p>请从左侧选择一篇文章查看详情</p></div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {/* 创建文章弹窗 */}
      {showCreateForm && (
        <div className="modal-overlay" onClick={() => setShowCreateForm(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>创建新文章</h2>
            <form onSubmit={handleCreateArticle}>
              <div className="form-group">
                <label>标题 *</label>
                <input type="text" value={newArticle.title} onChange={(e) => setNewArticle({ ...newArticle, title: e.target.value })} placeholder="请输入文章标题" required />
              </div>
              <div className="form-group">
                <label>作者</label>
                <input type="text" value={newArticle.author} onChange={(e) => setNewArticle({ ...newArticle, author: e.target.value })} placeholder="请输入作者名称（可选）" />
              </div>
              <div className="form-group">
                <label>分类</label>
                <select value={newArticle.category} onChange={(e) => setNewArticle({ ...newArticle, category: e.target.value })}>
                  <option value="">未分类</option>
                  {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                </select>
                <div style={{ display: 'flex', marginTop: 6 }}>
                  <input type="text" placeholder="新分类名" value={newCategory} onChange={(e) => setNewCategory(e.target.value)} />
                  <button type="button" className="category-add-btn" onClick={() => {
                    const cat = newCategory.trim();
                    if (!cat) return;
                    const created = handleCreateCategory(cat);
                    if (created) setNewArticle({ ...newArticle, category: created });
                  }}>添加分类并使用</button>
                </div>
              </div>
              <div className="form-group">
                <label>内容 *</label>
                <textarea value={newArticle.content} onChange={(e) => setNewArticle({ ...newArticle, content: e.target.value })} placeholder="请输入文章内容" rows={10} required />
              </div>
              <div className="form-actions">
                <button type="button" onClick={() => setShowCreateForm(false)}>取消</button>
                <button type="submit">创建</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AI 聊天 */}
      <AiChat isOpen={showAiChat} onClose={() => setShowAiChat(false)} />
    </div>
  );
};

export default MainPage;
