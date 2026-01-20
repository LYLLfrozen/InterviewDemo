import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi } from '../../services/api';
import './ProductDetail.css';

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  status: number;
  createTime: string;
}

const ProductDetail: React.FC = () => {
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(false);
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // 加载商品详情
  const loadProductDetail = async () => {
    if (!id) return;
    
    setLoading(true);
    try {
      const response = await productApi.getOnShelfProductById(Number(id));
      if (response.data.code === 200) {
        setProduct(response.data.data);
      } else {
        alert('加载商品详情失败: ' + response.data.msg);
        navigate('/shop/products');
      }
    } catch (error: any) {
      console.error('加载商品详情失败:', error);
      alert('加载商品详情失败: ' + (error.response?.data?.msg || error.message));
      navigate('/shop/products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProductDetail();
  }, [id]);

  // 返回商品列表
  const handleBackToList = () => {
    navigate('/shop/products');
  };

  if (loading) {
    return (
      <div className="product-detail-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    );
  }

  if (!product) {
    return null;
  }

  return (
    <div className="product-detail-page">
      <div className="detail-header">
        <button className="back-to-list-btn" onClick={handleBackToList}>
          ← 返回商品列表
        </button>
      </div>

      <div className="detail-container">
        <div className="detail-card">
          <div className="detail-title-section">
            <h1 className="detail-product-name">{product.name}</h1>
            <span className="detail-product-status">上架中</span>
          </div>

          <div className="detail-divider"></div>

          <div className="detail-info-grid">
            <div className="detail-info-item">
              <div className="detail-info-icon price-icon">¥</div>
              <div className="detail-info-content">
                <span className="detail-info-label">商品价格</span>
                <span className="detail-info-value price-value">
                  ¥{product.price.toFixed(2)}
                </span>
              </div>
            </div>

            <div className="detail-info-item">
              <div className="detail-info-icon stock-icon">📦</div>
              <div className="detail-info-content">
                <span className="detail-info-label">库存数量</span>
                <span className={`detail-info-value stock-value ${product.stock < 10 ? 'low-stock' : ''}`}>
                  {product.stock} 件
                  {product.stock < 10 && <span className="stock-warning"> (库存紧张)</span>}
                </span>
              </div>
            </div>

            <div className="detail-info-item">
              <div className="detail-info-icon id-icon">#</div>
              <div className="detail-info-content">
                <span className="detail-info-label">商品ID</span>
                <span className="detail-info-value">{product.id}</span>
              </div>
            </div>

            <div className="detail-info-item">
              <div className="detail-info-icon time-icon">📅</div>
              <div className="detail-info-content">
                <span className="detail-info-label">上架时间</span>
                <span className="detail-info-value">
                  {new Date(product.createTime).toLocaleString('zh-CN')}
                </span>
              </div>
            </div>
          </div>

          <div className="detail-divider"></div>

          <div className="detail-actions">
            <button 
              className="action-btn purchase-btn"
              disabled={product.stock === 0}
            >
              {product.stock === 0 ? '暂无库存' : '立即购买'}
            </button>
            <button 
              className="action-btn cart-btn"
              disabled={product.stock === 0}
            >
              {product.stock === 0 ? '无法加入购物车' : '加入购物车'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
