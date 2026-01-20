import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productApi } from '../../services/api';
import './ProductList.css';

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  status: number;
  createTime: string;
}

const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // 加载上架商品列表
  const loadProducts = async () => {
    setLoading(true);
    try {
      const response = await productApi.getOnShelfProducts();
      if (response.data.code === 200) {
        setProducts(response.data.data || []);
      } else {
        alert('加载商品列表失败: ' + response.data.msg);
      }
    } catch (error: any) {
      console.error('加载商品列表失败:', error);
      alert('加载商品列表失败: ' + (error.response?.data?.msg || error.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  // 跳转到商品详情页
  const handleViewDetail = (id: number) => {
    navigate(`/shop/products/${id}`);
  };

  // 返回主页
  const handleBackToMain = () => {
    navigate('/mainPage');
  };

  return (
    <div className="product-list-page">
      <div className="product-list-header">
        <h1>商品列表</h1>
        <button className="back-btn" onClick={handleBackToMain}>
          返回主页
        </button>
      </div>

      {loading ? (
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      ) : (
        <div className="products-grid">
          {products.length === 0 ? (
            <div className="empty-state">
              <p>暂无上架商品</p>
            </div>
          ) : (
            products.map((product) => (
              <div key={product.id} className="product-card">
                <div className="product-card-header">
                  <h3 className="product-name">{product.name}</h3>
                  <span className="product-status">上架中</span>
                </div>
                
                <div className="product-card-body">
                  <div className="product-info">
                    <div className="info-item">
                      <span className="info-label">价格</span>
                      <span className="info-value price">¥{product.price.toFixed(2)}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">库存</span>
                      <span className={`info-value stock ${product.stock < 10 ? 'low-stock' : ''}`}>
                        {product.stock} 件
                      </span>
                    </div>
                  </div>
                </div>

                <div className="product-card-footer">
                  <button 
                    className="detail-btn" 
                    onClick={() => handleViewDetail(product.id)}
                  >
                    查看详情
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default ProductList;
