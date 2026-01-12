import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../../services/api';
import './AdminDashboard.css';

interface User {
  id: string | number;
  username: string;
  password?: string;
  name?: string;
  age?: number;
  email?: string;
  phone?: string;
  status?: number;
  createTime?: string;
}

const AdminDashboard: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'add' | 'edit'>('add');
  const [currentUser, setCurrentUser] = useState<User>({
    id: '',
    username: '',
    email: '',
    phone: '',
  });
  const [originalId, setOriginalId] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    // 检查登录状态
    const adminToken = localStorage.getItem('adminToken');
    if (!adminToken) {
      navigate('/admin');
      return;
    }
    
    // 加载用户列表
    fetchUsers();
  }, [navigate]);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const response = await userApi.getAllUsers();
      const { code, data } = response.data;
      if (code === 200) {
        // 将后端返回的 id 转为字符串，避免大整数在 JS 中丢失精度
        const safeData = (data || []).map((u: any) => ({ ...u, id: String(u.id) }));
        setUsers(safeData);
      } else {
        alert('获取用户列表失败');
      }
    } catch (error) {
      console.error('获取用户列表错误:', error);
      alert('获取用户列表失败，请检查网络或后端服务');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUsername');
    navigate('/admin');
  };

  const handleAdd = () => {
    setModalType('add');
    setCurrentUser({
      id: 0,
      username: '',
      password: '',
      name: '',
      age: undefined,
      email: '',
      phone: '',
      status: 0,
    });
    setShowModal(true);
  };

  const handleEdit = (user: User) => {
    setModalType('edit');
    setCurrentUser({ ...user });
    setOriginalId(String(user.id));
    setShowModal(true);
  };

  const handleDelete = async (userId: string | number) => {
    if (!confirm('确定要删除该用户吗？')) {
      return;
    }

    try {
      const response = await userApi.deleteUser(userId);
      const { code, msg } = response.data;
      if (code === 200) {
        alert('删除成功');
        fetchUsers();
      } else {
        alert(msg || '删除失败');
      }
    } catch (error) {
      console.error('删除用户错误:', error);
      alert('删除用户失败');
    }
  };

  const handleSave = async () => {
    if (!currentUser.username) {
      alert('用户名不能为空');
      return;
    }

    if (modalType === 'add' && !currentUser.password) {
      alert('添加用户时密码不能为空');
      return;
    }

    try {
      let response;
      if (modalType === 'add') {
        const payload: any = { ...currentUser };
        // 新增时不要携带 id，交由后端自增生成
        delete payload.id;
        console.log('添加用户 payload:', payload);
        response = await userApi.addUser(payload);
      } else {
        // 编辑时将 id 以字符串处理以避免大整数精度丢失
        const userData: any = { ...currentUser };
        const newIdStr = String(userData.id);

        // 如果管理员修改了 id，需要先调用 changeUserId（传字符串）
        if (originalId != null && newIdStr !== originalId) {
          try {
            const changeResp = await userApi.changeUserId(originalId, newIdStr);
            if (changeResp.data?.code !== 200) {
              alert(changeResp.data?.msg || '修改 ID 失败');
              return;
            }
            // 更新 originalId 为新的 id
            setOriginalId(newIdStr);
          } catch (err) {
            console.error('修改 ID 错误:', err);
            alert('修改 ID 失败，请检查后端或网络');
            return;
          }
        }

        // 保留id字段用于后端更新，将age转换为数字类型
        if (!userData.password) {
          delete userData.password;
        }
        // 确保age是数字类型
        if (userData.age !== undefined && userData.age !== null && userData.age !== '') {
          userData.age = parseInt(userData.age);
        }
        console.log('更新用户 payload:', userData);
        response = await userApi.updateUser(userData);
      }

      const { code, msg } = response.data;
      if (code === 200) {
        alert(modalType === 'add' ? '添加成功' : '更新成功');
        setShowModal(false);
        fetchUsers();
      } else {
        alert(msg || '操作失败');
      }
    } catch (error) {
      console.error('保存用户错误:', error);
      alert('保存用户失败');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCurrentUser(prev => ({
      ...prev,
      [name]: name === 'age' ? (value === '' ? undefined : parseInt(value)) : value,
    }));
  };

  return (
    <div className="admin-dashboard">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>用户管理系统</h1>
          <div className="header-actions">
            <span className="admin-name">管理员: {localStorage.getItem('adminUsername')}</span>
            <button onClick={handleLogout} className="logout-button">退出登录</button>
          </div>
        </div>
      </header>

      <main className="dashboard-content">
        <div className="content-header">
          <h2>用户列表</h2>
          <button onClick={handleAdd} className="add-button">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            添加用户
          </button>
        </div>

        {isLoading ? (
          <div className="loading">加载中...</div>
        ) : (
          <div className="table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>姓名</th>
                  <th>年龄</th>
                  <th>邮箱</th>
                  <th>手机号</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {users.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="empty-message">暂无用户数据</td>
                  </tr>
                ) : (
                  users.map(user => (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>{user.username}</td>
                      <td>{user.name || '-'}</td>
                      <td>{user.age || '-'}</td>
                      <td>{user.email || '-'}</td>
                      <td>{user.phone || '-'}</td>
                      <td>
                        <span className={`status-badge ${user.status === 0 ? 'status-active' : 'status-disabled'}`}>
                          {user.status === 0 ? '正常' : '封号'}
                        </span>
                      </td>
                      <td className="actions">
                        <button onClick={() => handleEdit(user)} className="edit-button">编辑</button>
                        <button onClick={() => handleDelete(user.id)} className="delete-button">删除</button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{modalType === 'add' ? '添加用户' : '编辑用户'}</h3>
              <button onClick={() => setShowModal(false)} className="close-button">×</button>
            </div>
            <div className="modal-body">
              {modalType === 'edit' && (
                <div className="form-group">
                  <label>ID</label>
                  <input
                    type="text"
                    name="id"
                    value={currentUser.id}
                    onChange={(e) => setCurrentUser(prev => ({ ...prev, id: e.target.value }))}
                    placeholder="请输入ID"
                  />
                </div>
              )}
              <div className="form-group">
                <label>用户名 *</label>
                <input
                  type="text"
                  name="username"
                  value={currentUser.username}
                  onChange={handleInputChange}
                  placeholder="请输入用户名"
                  required
                />
              </div>
              <div className="form-group">
                <label>密码 {modalType === 'add' && '*'}</label>
                <input
                  type="password"
                  name="password"
                  value={currentUser.password || ''}
                  onChange={handleInputChange}
                  placeholder={modalType === 'add' ? '请输入密码' : '留空表示不修改密码'}
                  required={modalType === 'add'}
                />
              </div>
              <div className="form-group">
                <label>姓名</label>
                <input
                  type="text"
                  name="name"
                  value={currentUser.name || ''}
                  onChange={handleInputChange}
                  placeholder="请输入姓名"
                />
              </div>
              <div className="form-group">
                <label>年龄</label>
                <input
                  type="number"
                  name="age"
                  value={currentUser.age || ''}
                  onChange={handleInputChange}
                  placeholder="请输入年龄"
                />
              </div>
              <div className="form-group">
                <label>邮箱</label>
                <input
                  type="email"
                  name="email"
                  value={currentUser.email || ''}
                  onChange={handleInputChange}
                  placeholder="请输入邮箱"
                />
              </div>
              <div className="form-group">
                <label>手机号</label>
                <input
                  type="text"
                  name="phone"
                  value={currentUser.phone || ''}
                  onChange={handleInputChange}
                  placeholder="请输入手机号"
                />
              </div>
              <div className="form-group">
                <label>状态</label>
                <select
                  name="status"
                  value={currentUser.status ?? 0}
                  onChange={(e) => setCurrentUser(prev => ({ ...prev, status: parseInt(e.target.value) }))}
                >
                  <option value={0}>正常</option>
                  <option value={1}>封号</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button onClick={() => setShowModal(false)} className="cancel-button">取消</button>
              <button onClick={handleSave} className="save-button">保存</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
