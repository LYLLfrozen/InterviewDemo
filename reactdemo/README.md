# 现代登录界面

这是一个使用 React + TypeScript + Vite 构建的现代化登录界面。

## 功能特性

✨ **现代设计**
- 渐变背景与动画效果
- 毛玻璃卡片设计
- 流畅的过渡动画
- 响应式布局

🎨 **界面元素**
- 用户名和密码输入框
- 记住我复选框
- 忘记密码链接
- 社交登录按钮（Google、GitHub）
- 加载状态动画

💻 **技术栈**
- React 18
- TypeScript
- Vite
- CSS3 动画

## 安装依赖

\`\`\`bash
npm install
\`\`\`

## 启动开发服务器

\`\`\`bash
npm run dev
\`\`\`

启动后在浏览器中打开 `http://localhost:5173`

## 构建生产版本

\`\`\`bash
npm run build
\`\`\`

## 预览生产版本

\`\`\`bash
npm run preview
\`\`\`

## 项目结构

\`\`\`
reactdemo/
├── src/
│   ├── components/
│   │   ├── Login.tsx      # 登录组件
│   │   └── Login.css      # 登录样式
│   ├── App.tsx            # 主应用组件
│   ├── App.css            # 全局样式
│   ├── main.tsx           # 应用入口
│   └── vite-env.d.ts      # TypeScript 类型定义
├── index.html             # HTML 模板
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
└── vite.config.ts         # Vite 配置
\`\`\`

## 特性说明

### 设计特点
- **渐变背景**：紫色渐变背景配合浮动动画球体
- **毛玻璃效果**：卡片使用半透明背景和模糊效果
- **图标系统**：使用 SVG 图标，简洁美观
- **动画效果**：输入框聚焦、按钮悬停、加载动画等
- **响应式**：适配移动端和桌面端

### 功能说明
- 表单验证
- 记住我功能
- 加载状态显示
- 社交登录入口
- 注册入口

## 自定义

你可以在 [Login.css](src/components/Login.css) 中修改颜色主题和动画效果。

主要颜色变量：
- 主色：`#667eea` - `#764ba2`
- 背景渐变可以根据需要调整
