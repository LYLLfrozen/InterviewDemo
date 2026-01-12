import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // 监听所有地址，允许局域网访问
    host: '0.0.0.0',
    port: 8081,
    proxy: {
      // 将开发期间的 /api 请求转发到后端服务（本地运行的 Spring Boot）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
    },
  },
})
