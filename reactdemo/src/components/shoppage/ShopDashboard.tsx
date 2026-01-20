import React, { useEffect, useState } from 'react';
import { productApi } from '../../services/api';
import './ShopDashboard.css';

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  status: number;
  createTime: string;
}

const ShopDashboard: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    price: '',
    stock: '',
    status: 0
  });

  // 加载商品列表
  const loadProducts = async () => {
    setLoading(true);
    try {
      const response = await productApi.getAllProducts();
      if (response.data.code === 200) {
        setProducts(response.data.data);
      } else {
        alert('加载商品列表失败: ' + response.data.msg);
      }
    } catch (error: any) {
      alert('加载商品列表失败: ' + (error.response?.data?.msg || error.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  // 打开新增/编辑弹窗
  const openModal = (product?: Product) => {
    if (product) {
      setEditingProduct(product);
      setFormData({
        name: product.name,
        price: product.price.toString(),
        stock: product.stock.toString(),
        status: product.status
      });
    } else {
      setEditingProduct(null);
      setFormData({ name: '', price: '', stock: '', status: 0 });
    }
    setShowModal(true);
  };

  // 关闭弹窗
  const closeModal = () => {
    setShowModal(false);
    setEditingProduct(null);
    setFormData({ name: '', price: '', stock: '', status: 0 });
  };

  // 提交表单
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const productData = {
      name: formData.name,
      price: parseFloat(formData.price),
      stock: parseInt(formData.stock),
      status: formData.status
    };

    try {
      if (editingProduct) {
        // 更新商品
        const response = await productApi.updateProduct(editingProduct.id, productData);
        if (response.data.code === 200) {
          alert('商品更新成功');
          closeModal();
          loadProducts();
        } else {
          alert('更新失败: ' + response.data.msg);
        }
      } else {
        // 新增商品
        const response = await productApi.createProduct(productData);
        if (response.data.code === 200) {
          alert('商品创建成功');
          closeModal();
          loadProducts();
        } else {
          alert('创建失败: ' + response.data.msg);
        }
      }
    } catch (error: any) {
      alert('操作失败: ' + (error.response?.data?.msg || error.message));
    }
  };

  // 删除商品
  const handleDelete = async (id: number) => {
    if (!window.confirm('确定要删除这个商品吗？')) {
      return;
    }

    try {
      const response = await productApi.deleteProduct(id);
      if (response.data.code === 200) {
        alert('商品删除成功');
        loadProducts();
      } else {
        alert('删除失败: ' + response.data.msg);
      }
    } catch (error: any) {
      alert('删除失败: ' + (error.response?.data?.msg || error.message));
    }
  };

  // 上架商品
  const handleOnShelf = async (id: number) => {
    try {
      const response = await productApi.onShelf(id);
      if (response.data.code === 200) {
        alert('商品上架成功');
        loadProducts();
      } else {
        alert('上架失败: ' + response.data.msg);
      }
    } catch (error: any) {
      alert('上架失败: ' + (error.response?.data?.msg || error.message));
    }
  };

  // 下架商品
  const handleOffShelf = async (id: number) => {
    try {
      const response = await productApi.offShelf(id);
      if (response.data.code === 200) {
        alert('商品下架成功');
        loadProducts();
      } else {
        alert('下架失败: ' + response.data.msg);
      }
    } catch (error: any) {
      alert('下架失败: ' + (error.response?.data?.msg || error.message));
    }
  };

  return (
    <div className="shop-dashboard">
      <div className="dashboard-header">
        <h1>商品管理</h1>
        <button className="btn-primary" onClick={() => openModal()}>
          新增商品
        </button>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="products-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>商品名称</th>
                <th>价格</th>
                <th>库存</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {products.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center' }}>暂无商品</td>
                </tr>
              ) : (
                products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.id}</td>
                    <td>{product.name}</td>
                    <td>¥{product.price.toFixed(2)}</td>
                    <td>{product.stock}</td>
                    <td>
                      <span className={`status-badge ${product.status === 1 ? 'on-shelf' : 'off-shelf'}`}>
                        {product.status === 1 ? '上架' : '下架'}
                      </span>
                    </td>
                    <td>{new Date(product.createTime).toLocaleString()}</td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-edit" onClick={() => openModal(product)}>
                          编辑
                        </button>
                        {product.status === 0 ? (
                          <button className="btn-shelf" onClick={() => handleOnShelf(product.id)}>
                            上架
                          </button>
                        ) : (
                          <button className="btn-shelf" onClick={() => handleOffShelf(product.id)}>
                            下架
                          </button>
                        )}
                        <button className="btn-delete" onClick={() => handleDelete(product.id)}>
                          删除
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* 新增/编辑弹窗 */}
      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingProduct ? '编辑商品' : '新增商品'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>商品名称</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>价格</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>库存</label>
                <input
                  type="number"
                  value={formData.stock}
                  onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>状态</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: parseInt(e.target.value) })}
                >
                  <option value={0}>下架</option>
                  <option value={1}>上架</option>
                </select>
              </div>
              <div className="form-actions">
                <button type="button" className="btn-cancel" onClick={closeModal}>
                  取消
                </button>
                <button type="submit" className="btn-submit">
                  {editingProduct ? '更新' : '创建'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ShopDashboard;
