#!/bin/bash

##############################################
# Spring Boot 高性能启动脚本
# 针对 16GB 内存环境优化
##############################################

# 配置
APP_NAME="Springbootdemo"
APP_VERSION="0.0.1-SNAPSHOT"
JAR_FILE="target/${APP_NAME}-${APP_VERSION}.jar"
PROFILE="dev"  # 可选: dev, prod

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}  启动 ${APP_NAME}${NC}"
echo -e "${GREEN}  环境: ${PROFILE}${NC}"
echo -e "${GREEN}===========================================${NC}"
echo ""

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}JAR 文件不存在，开始构建...${NC}"
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo -e "${RED}构建失败${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ 构建成功${NC}"
fi

# 检查端口是否被占用
PORT=8080
if [ "$PROFILE" = "prod" ]; then
    PORT=9090
fi

if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${RED}端口 ${PORT} 已被占用${NC}"
    echo "请先停止占用该端口的进程，或修改配置文件中的端口"
    exit 1
fi

# 检查 MySQL 和 Redis
echo -e "${YELLOW}检查依赖服务...${NC}"

# 检查 MySQL
if ! pgrep -x mysqld > /dev/null; then
    echo -e "${YELLOW}警告: MySQL 可能未运行${NC}"
    echo "请确保 MySQL 已启动: brew services start mysql"
fi

# 检查 Redis
if ! pgrep -x redis-server > /dev/null; then
    echo -e "${YELLOW}警告: Redis 可能未运行${NC}"
    echo "请确保 Redis 已启动: brew services start redis"
fi

echo ""

# JVM 参数（根据环境调整）
if [ "$PROFILE" = "prod" ]; then
    # 生产环境：大内存配置
    JVM_OPTS="-Xms4G -Xmx12G \
              -XX:+UseG1GC \
              -XX:MaxGCPauseMillis=200 \
              -XX:+HeapDumpOnOutOfMemoryError \
              -XX:HeapDumpPath=./logs/heapdump.hprof \
              -XX:+PrintGCDetails \
              -XX:+PrintGCDateStamps \
              -Xloggc:./logs/gc.log \
              -Dspring.profiles.active=prod"
else
    # 开发环境：适中配置
    JVM_OPTS="-Xms2G -Xmx8G \
              -XX:+UseG1GC \
              -XX:MaxGCPauseMillis=200 \
              -Dspring.profiles.active=dev"
fi

echo -e "${GREEN}启动参数:${NC}"
echo "  JVM 堆内存: 2G-8G (dev) / 4G-12G (prod)"
echo "  GC 策略: G1GC"
echo "  Profile: ${PROFILE}"
echo ""

# 启动应用
echo -e "${GREEN}正在启动应用...${NC}"
nohup java $JVM_OPTS -jar "$JAR_FILE" > ./logs/app.log 2>&1 &
PID=$!

echo "进程 PID: ${PID}"
echo "日志文件: ./logs/app.log"
echo ""

# 等待启动
echo -e "${YELLOW}等待服务启动...${NC}"
for i in {1..30}; do
    sleep 1
    if curl -s http://localhost:${PORT}/api/health/ping > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 服务启动成功！${NC}"
        echo ""
        echo "访问地址:"
        echo "  - 健康检查: http://localhost:${PORT}/api/health/ping"
        echo "  - 性能指标: http://localhost:${PORT}/api/health/metrics"
        echo "  - 用户列表: http://localhost:${PORT}/api/user/page"
        echo ""
        echo "查看日志:"
        echo "  tail -f ./logs/app.log"
        echo ""
        echo "停止服务:"
        echo "  kill ${PID}"
        exit 0
    fi
    echo -n "."
done

echo ""
echo -e "${RED}启动超时，请检查日志${NC}"
echo "  tail -f ./logs/app.log"
exit 1
