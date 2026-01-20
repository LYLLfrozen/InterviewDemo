import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/login/Login';
import Register from './components/register/Register';
import AdminLogin from './components/login/AdminLogin';
import AdminDashboard from './components/dashboard/AdminDashboard';
import MainPage from './components/mainpage/MainPage';
import ShopDashboard from './components/shoppage/ShopDashboard';
import ProductList from './components/shoppage/ProductList';
import ProductDetail from './components/shoppage/ProductDetail';
import './App.css';

const UserPage: React.FC = () => {
  const [isLogin, setIsLogin] = useState(true);

  return (
    <>
      {isLogin ? (
        <Login onSwitchToRegister={() => setIsLogin(false)} />
      ) : (
        <Register onSwitchToLogin={() => setIsLogin(true)} />
      )}
    </>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<UserPage />} />
        <Route path="/mainPage" element={<MainPage />} />
        <Route path="/shop/products" element={<ProductList />} />
        <Route path="/shop/products/:id" element={<ProductDetail />} />
        <Route path="/admin" element={<AdminLogin />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/shop" element={<ShopDashboard />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
};

export default App;
