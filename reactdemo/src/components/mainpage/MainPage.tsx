import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import AiChat from '../aichat/AiChat';
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

const MainPage: React.FC = () => {
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

  // 新增：分类相关
  const [categories, setCategories] = useState<string[]>([]);
  const [newCategory, setNewCategory] = useState('');

  // 新增：编辑模式
  const [editingArticle, setEditingArticle] = useState<Article | null>(null);
  // 新增：已选中的分类（空字符串表示尚未选择，先展示分类列表）
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  // 加载文章列表
  const loadArticles = async () => {
    setIsLoading(true);
    try {
      const response = await api.get('/mainPage');
      const { code, data } = response.data;
      
      if (code === 200) {
        const list: Article[] = data || [];
        setArticles(list);
        // 从文章中提取分类
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

  // 组件挂载时加载文章列表
  useEffect(() => {
    loadArticles();
  }, []);

  // 响应式：检测是否为移动端视口
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 选择文章
  const handleSelectArticle = async (articleId: string) => {
    try {
      // 如果当前正在编辑且编辑对象就是要打开的文章，则直接使用本地编辑对象，避免被远程加载覆盖
      if (editingArticle && editingArticle.id === articleId) {
        setSelectedArticle(editingArticle);
        return;
      }

      const response = await api.get(`/mainPage/${articleId}`);
      const { code, data } = response.data;
      
      if (code === 200) {
        setSelectedArticle(data);
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
        loadArticles(); // 重新加载文章列表
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
    if (!confirm('确定要删除这篇文章吗？')) {
      return;
    }

    try {
      const response = await api.delete(`/mainPage/${articleId}`);
      const { code } = response.data;
      
      if (code === 200) {
        alert('文章删除成功！');
        if (selectedArticle?.id === articleId) {
          setSelectedArticle(null);
        }
        loadArticles();
      } else {
        alert('删除文章失败');
      }
    } catch (error) {
      console.error('删除文章出错:', error);
      alert('删除文章失败');
    }
  };

  // 新增：创建分类（仅前端保留分类列表，通过创建/编辑文章提交 category 字段到后端）
  const handleCreateCategory = (catParam?: string) => {
    const cat = (catParam !== undefined ? catParam : newCategory).trim();
    if (!cat) return '';
    if (!categories.includes(cat)) {
      setCategories(prev => [...prev, cat]);
    }
    // 清空输入框
    setNewCategory('');
    // 返回创建的分类，方便调用方使用
    return cat;
  };

  // 新增：进入编辑模式
  const handleEditArticle = (article: Article) => {
    setEditingArticle({ ...article });
  };

  // 新增：取消编辑
  const handleCancelEdit = () => {
    setEditingArticle(null);
  };

  // 新增：保存编辑（调用后端 PUT）
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
        setSelectedArticle(data);
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

  // 新增：移动文章位置（向上/向下），通过与相邻文章交换 position 实现
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
      // 先更新 a 为临时位置，避免冲突（将 a -> -1），再将 b -> posA，再将 a -> posB
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
      <div className="main-header">
        <h1>面试系统</h1>
        <div className="header-actions">
          <button 
            className="ai-chat-btn"
            onClick={() => setShowAiChat(true)}
          >
            🤖 AI助手
          </button>
          <button 
            className="create-btn"
            onClick={() => setShowCreateForm(true)}
          >
            + 新建文章
          </button>
        </div>
      </div>

      <div className="main-content">
        {/* 左侧：先展示分类，点击分类后展示该分类下的文章（移动端：选择文章时隐藏列表） */}
        {(!isMobile || !selectedArticle) && (
          <div className="article-list">
            <h2>文章目录</h2>
            {isLoading ? (
              <div className="loading">加载中...</div>
            ) : articles.length === 0 ? (
              <div className="empty">暂无文章</div>
            ) : (
              // 未选分类：显示分类列表；已选分类：显示该分类下文章并提供返回分类按钮
              (selectedCategory === '') ? (
                <div className="category-list">
                  <ul>
                    {categories.map((cat) => (
                      <li key={cat}>
                        <button
                          className="move-btn"
                          onClick={() => setSelectedCategory(cat)}
                        >
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
                        <li 
                          key={article.id}
                          className={selectedArticle?.id === article.id ? 'active' : ''}
                        >
                          <div 
                            className="article-item"
                            onClick={() => handleSelectArticle(article.id)}
                          >
                            <h3>{article.title}</h3>
                            <p className="article-meta">
                              <span>作者: {article.author || '匿名'}</span>
                              <span>分类: {article.category || '未分类'}</span>
                              <span>{new Date(article.createTime).toLocaleDateString()}</span>
                            </p>
                          </div>
                          <div className="article-actions">
                            <button
                              className="move-btn"
                              title="上移"
                              onClick={(e) => { e.stopPropagation(); handleMoveArticle(article.id, 'up'); }}
                              disabled={index === 0}
                            >↑</button>
                            <button
                              className="move-btn"
                              title="下移"
                              onClick={(e) => { e.stopPropagation(); handleMoveArticle(article.id, 'down'); }}
                              disabled={index === articles.length - 1}
                            >↓</button>
                            <button
                              className="edit-btn"
                              title="编辑"
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedArticle(article);
                                handleEditArticle(article);
                              }}
                            >编辑</button>
                            <button
                              className="delete-btn"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteArticle(article.id);
                              }}
                            >
                              删除
                            </button>
                          </div>
                        </li>
                      ))
                    )}
                  </ul>
                </div>
              )
            )}
          </div>
        )}

        {/* 右侧文章详情/编辑（移动端：未选择时隐藏） */}
        {(!isMobile || selectedArticle) && (
          <div className={`article-detail ${editingArticle ? 'editing' : ''}`}>
          {isMobile && selectedArticle && (
            <button className="back-btn" onClick={() => setSelectedArticle(null)}>
              ← 返回
            </button>
          )}
          {selectedArticle ? (
            <>
              {!editingArticle ? (
                <>
                  <h2>{selectedArticle.title}</h2>
                  <div className="article-meta">
                    <span>作者: {selectedArticle.author || '匿名'}</span>
                    <span>分类: {selectedArticle.category || '未分类'}</span>
                    <span>创建时间: {new Date(selectedArticle.createTime).toLocaleString()}</span>
                  </div>
                  <div className="article-content">
                    {selectedArticle.content}
                  </div>
                  {/* 详情页底部不再显示编辑按钮（编辑在左侧列表中触发） */}
                </>
              ) : (
                // 编辑表单
                <div className="edit-form">
                  <h2>编辑文章</h2>
                  <div className="form-group">
                    <label>标题 *</label>
                    <input
                      type="text"
                      value={editingArticle.title}
                      onChange={(e) => setEditingArticle({ ...editingArticle, title: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>作者</label>
                    <input
                      type="text"
                      value={editingArticle.author}
                      onChange={(e) => setEditingArticle({ ...editingArticle, author: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>分类</label>
                    <select
                      value={editingArticle.category || ''}
                      onChange={(e) => setEditingArticle({ ...editingArticle, category: e.target.value })}
                    >
                      <option value="">未分类</option>
                      {categories.map(cat => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                    <div style={{ display: 'flex', marginTop: 6 }}>
                      <input
                        type="text"
                        placeholder="新分类名"
                        value={newCategory}
                        onChange={(e) => setNewCategory(e.target.value)}
                      />
                      <button type="button" className="category-add-btn" onClick={() => {
                        const cat = newCategory.trim();
                        if (!cat) return;
                        const created = handleCreateCategory(cat);
                        if (created && editingArticle) {
                          setEditingArticle({ ...editingArticle, category: created });
                        }
                      }}>
                        添加分类并使用
                      </button>
                    </div>
                  </div>
                  <div className="form-group">
                    <label>内容 *</label>
                    <textarea
                      value={editingArticle.content}
                      onChange={(e) => setEditingArticle({ ...editingArticle, content: e.target.value })}
                      rows={10}
                    />
                  </div>
                  <div className="form-actions">
                    <button onClick={handleCancelEdit}>取消</button>
                    <button onClick={handleSaveEdit}>保存</button>
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="no-selection">
              <p>请从左侧选择一篇文章查看详情</p>
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
                <input
                  type="text"
                  value={newArticle.title}
                  onChange={(e) => setNewArticle({ ...newArticle, title: e.target.value })}
                  placeholder="请输入文章标题"
                  required
                />
              </div>
              <div className="form-group">
                <label>作者</label>
                <input
                  type="text"
                  value={newArticle.author}
                  onChange={(e) => setNewArticle({ ...newArticle, author: e.target.value })}
                  placeholder="请输入作者名称（可选）"
                />
              </div>
              <div className="form-group">
                <label>分类</label>
                <select
                  value={newArticle.category}
                  onChange={(e) => setNewArticle({ ...newArticle, category: e.target.value })}
                >
                  <option value="">未分类</option>
                  {categories.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
                <div style={{ display: 'flex', marginTop: 6 }}>
                  <input
                    type="text"
                    placeholder="新分类名"
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                  />
                  <button type="button" className="category-add-btn" onClick={() => {
                    const cat = newCategory.trim();
                    if (!cat) return;
                    const created = handleCreateCategory(cat);
                    if (created) {
                      setNewArticle({ ...newArticle, category: created });
                    }
                  }}>
                    添加分类并使用
                  </button>
                </div>
              </div>
              <div className="form-group">
                <label>内容 *</label>
                <textarea
                  value={newArticle.content}
                  onChange={(e) => setNewArticle({ ...newArticle, content: e.target.value })}
                  placeholder="请输入文章内容"
                  rows={10}
                  required
                />
              </div>
              <div className="form-actions">
                <button type="button" onClick={() => setShowCreateForm(false)}>
                  取消
                </button>
                <button type="submit">创建</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AI 聊天组件 */}
      <AiChat isOpen={showAiChat} onClose={() => setShowAiChat(false)} />
    </div>
  );
};

export default MainPage;
