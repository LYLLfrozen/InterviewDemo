# 解决 504 Outdated Optimize Dep 错误

## 问题原因
这是 Vite 开发服务器的常见问题，当你修改代码后，Vite 的依赖预构建缓存过期，但浏览器还在尝试加载旧的缓存文件。

## 解决方案（按顺序尝试）

### 方案 1：清除 Vite 缓存并重启（推荐）
```powershell
# 1. 停止当前运行的开发服务器（Ctrl + C）

# 2. 删除 Vite 缓存
Remove-Item -Recurse -Force node_modules\.vite

# 3. 重新启动开发服务器
npm run dev
```

### 方案 2：强制重新构建
```powershell
# 停止服务器后执行
npm run dev -- --force
```

### 方案 3：清除浏览器缓存
1. 在浏览器中按 `Ctrl + Shift + Delete`
2. 选择清除缓存
3. 或者在开发者工具中右键刷新按钮，选择"清空缓存并硬性重新加载"

### 方案 4：完全清理并重新安装（最彻底）
```powershell
# 1. 停止开发服务器

# 2. 删除所有缓存和依赖
Remove-Item -Recurse -Force node_modules
Remove-Item -Recurse -Force node_modules\.vite
Remove-Item package-lock.json

# 3. 重新安装依赖
npm install

# 4. 启动服务器
npm run dev
```

## 快速修复步骤（推荐）

### 步骤 1：停止当前服务器
在运行 `npm run dev` 的终端窗口中按 `Ctrl + C`

### 步骤 2：执行清理命令
```powershell
cd f:\code\javacode\interviewdemo\reactdemo
Remove-Item -Recurse -Force node_modules\.vite
```

### 步骤 3：重新启动
```powershell
npm run dev
```

### 步骤 4：刷新浏览器
在浏览器中按 `Ctrl + F5` 强制刷新

## 预防措施

### 1. 配置 Vite 优化选项
在 `vite.config.ts` 中添加：
```typescript
export default defineConfig({
  // ...existing config
  optimizeDeps: {
    force: true // 每次启动都强制重新构建
  },
  server: {
    force: true
  }
})
```

### 2. 开发时的最佳实践
- 修改依赖或添加新包后，重启开发服务器
- 遇到奇怪的错误时，先清除缓存
- 定期清理 `node_modules/.vite` 目录

## 常见问题

### Q: 为什么会出现这个错误？
A: 当你修改代码（特别是添加语音识别等新功能）后，Vite 需要重新预构建依赖，但缓存没有正确更新。

### Q: 这个错误影响生产环境吗？
A: 不会。这只是开发环境的问题，生产构建（`npm run build`）不受影响。

### Q: 每次都需要清除缓存吗？
A: 不需要。只有在遇到这类错误时才需要清除。

## 自动化脚本

创建一个快速清理脚本 `clean-cache.ps1`：
```powershell
# clean-cache.ps1
Write-Host "正在清理 Vite 缓存..." -ForegroundColor Yellow
Remove-Item -Recurse -Force node_modules\.vite -ErrorAction SilentlyContinue
Write-Host "缓存已清理！" -ForegroundColor Green
Write-Host "请重新运行: npm run dev" -ForegroundColor Cyan
```

使用方法：
```powershell
.\clean-cache.ps1
npm run dev
```

## 检查是否修复成功

1. 打开浏览器开发者工具（F12）
2. 切换到 Console 标签
3. 应该看不到 504 错误
4. Network 标签中所有资源都应该是 200 状态码
5. AI 聊天功能正常，可以打开和使用

## 联系支持
如果以上方法都无效，可能是其他问题：
- 检查 Node.js 版本（推荐 16.x 或更高）
- 检查端口占用（默认 5173）
- 查看完整的终端错误日志
