#!/bin/bash

##############################################
# Spring Boot 高并发压测脚本
# 使用 wrk 工具进行负载测试
##############################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
API_BASE_URL="http://localhost:8080/api"
THREADS=12
DURATION=30  # 秒

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}  Spring Boot 高并发压测工具${NC}"
echo -e "${GREEN}===========================================${NC}"
echo ""

# 检查 wrk 是否安装
if ! command -v wrk &> /dev/null; then
    echo -e "${RED}错误: wrk 未安装${NC}"
    echo "请先安装 wrk:"
    echo "  macOS: brew install wrk"
    echo "  Ubuntu: sudo apt-get install wrk"
    echo "  或从源码编译: https://github.com/wg/wrk"
    exit 1
fi

# 检查服务是否运行
echo -e "${YELLOW}检查服务状态...${NC}"
if ! curl -s "${API_BASE_URL}/user/page?pageNum=1&pageSize=10" > /dev/null; then
    echo -e "${RED}错误: 服务未运行或无法访问 ${API_BASE_URL}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 服务运行正常${NC}"
echo ""

# 压测场景 1: 用户列表查询（读操作，有缓存）
echo -e "${YELLOW}[场景 1] 用户列表查询 (读操作 + 缓存)${NC}"
echo "并发: 200, 持续: ${DURATION}秒"
wrk -t${THREADS} -c200 -d${DURATION}s "${API_BASE_URL}/user/page?pageNum=1&pageSize=10"
echo ""

# 压测场景 2: 中等并发
echo -e "${YELLOW}[场景 2] 中等并发测试${NC}"
echo "并发: 500, 持续: ${DURATION}秒"
wrk -t${THREADS} -c500 -d${DURATION}s "${API_BASE_URL}/user/page?pageNum=1&pageSize=10"
echo ""

# 压测场景 3: 高并发（目标 1000）
echo -e "${YELLOW}[场景 3] 高并发测试 (目标 1000)${NC}"
echo "并发: 1000, 持续: ${DURATION}秒"
wrk -t${THREADS} -c1000 -d${DURATION}s "${API_BASE_URL}/user/page?pageNum=1&pageSize=10"
echo ""

# 压测场景 4: 极限并发测试
echo -e "${YELLOW}[场景 4] 极限并发测试${NC}"
echo "并发: 1500, 持续: ${DURATION}秒"
wrk -t${THREADS} -c1500 -d${DURATION}s "${API_BASE_URL}/user/page?pageNum=1&pageSize=10"
echo ""

# --------------------------------------------------
# 混合读写场景（80% 读，20% 写）
# 使用 wrk 的 Lua 脚本 wrk_mix.lua，脚本会对不同路径发起请求
# 运行方法示例：wrk -t12 -c1000 -d60s -s wrk_mix.lua 'http://localhost:8080'
# 注意：在 zsh 中请对 URL 加引号
# --------------------------------------------------
echo -e "${YELLOW}[场景 5] 混合读写 (80% 读 / 20% 写)${NC}"
echo "并发: 1000, 持续: 60 秒"
if [ -f "../wrk_mix.lua" ]; then
    # 如果在仓库根目录运行 load_test.sh，脚本位于 ../wrk_mix.lua
    wrk -t${THREADS} -c1000 -d60s -s ../wrk_mix.lua 'http://localhost:8080'
else
    # 如果在项目根目录运行
    wrk -t${THREADS} -c1000 -d60s -s wrk_mix.lua 'http://localhost:8080'
fi
echo ""

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}  压测完成${NC}"
echo -e "${GREEN}===========================================${NC}"
echo ""
echo -e "${YELLOW}分析建议:${NC}"
echo "1. 关注 Latency (延迟) 指标:"
echo "   - Avg < 100ms: 优秀"
echo "   - Avg < 500ms: 良好"
echo "   - Avg > 1s: 需要优化"
echo ""
echo "2. 关注 Requests/sec (QPS):"
echo "   - 目标: 1000+ QPS @ 1000并发"
echo ""
echo "3. 检查错误率:"
echo "   - Non-2xx or 3xx responses 应该为 0"
echo ""
echo "4. 监控系统资源:"
echo "   - CPU 使用率应 < 80%"
echo "   - 内存使用应 < 12GB (16GB 环境)"
echo "   - 数据库连接池未耗尽"
echo ""
echo -e "${YELLOW}下一步:${NC}"
echo "- 查看应用日志: tail -f logs/spring.log"
echo "- 监控 JVM: jconsole 或 VisualVM"
echo "- 查看数据库慢查询"
echo "- 检查 Redis 性能: redis-cli --stat"
