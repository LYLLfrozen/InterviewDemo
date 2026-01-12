# 清理 Vite 缓存脚本
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   Vite 缓存清理工具" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否在正确的目录
if (Test-Path "package.json") {
    Write-Host "[1/3] 正在清理 Vite 缓存..." -ForegroundColor Yellow
    
    # 删除 .vite 缓存目录
    if (Test-Path "node_modules\.vite") {
        Remove-Item -Recurse -Force "node_modules\.vite"
        Write-Host "✓ Vite 缓存已清理" -ForegroundColor Green
    } else {
        Write-Host "✓ 未找到缓存目录（可能已经是干净的）" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "[2/3] 清理完成！" -ForegroundColor Green
    Write-Host ""
    Write-Host "[3/3] 接下来请执行以下命令：" -ForegroundColor Cyan
    Write-Host "      npm run dev" -ForegroundColor White
    Write-Host ""
    Write-Host "提示：如果问题仍然存在，请尝试：" -ForegroundColor Yellow
    Write-Host "  1. 在浏览器中按 Ctrl+F5 强制刷新" -ForegroundColor White
    Write-Host "  2. 清空浏览器缓存" -ForegroundColor White
    Write-Host "  3. 运行: npm run dev -- --force" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host "❌ 错误：未找到 package.json" -ForegroundColor Red
    Write-Host "   请在项目根目录运行此脚本" -ForegroundColor Red
    Write-Host ""
    Write-Host "   正确的路径应该是：" -ForegroundColor Yellow
    Write-Host "   f:\code\javacode\interviewdemo\reactdemo" -ForegroundColor White
    Write-Host ""
}
