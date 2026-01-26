# 快速开始指南 - 高并发优化版

## 🚀 快速启动

### 1. 确保依赖服务运行

```bash
# 启动 MySQL
brew services start mysql

# 启动 Redis  
brew services start redis
```

### 2. 执行数据库索引优化

```bash
mysql -u root -p 0813-demo < Springbootdemo/src/main/resources/db/optimize_indexes.sql
```

### 3. 启动应用

```bash
cd Springbootdemo
./start.sh
```

或手动启动：

```bash
mvn clean package -DskipTests
java -Xms2G -Xmx8G -XX:+UseG1GC -jar target/Springbootdemo-0.0.1-SNAPSHOT.jar
```

### 4. 验证服务

```bash
# 健康检查
curl http://localhost:8080/api/health/ping

# 性能指标
curl http://localhost:8080/api/health/metrics

# 测试接口
curl http://localhost:8080/api/user/page?pageNum=1&pageSize=10
```

## 📊 性能压测

### 使用压测脚本（推荐）

```bash
./load_test.sh
```

### 手动压测

```bash
# 1000 并发测试（zsh 下请对 URL 加引号或转义，避免 glob 报错）
# 推荐：用单引号包裹 URL
wrk -t12 -c1000 -d30s 'http://localhost:8080/api/user/page?pageNum=1&pageSize=10'

# 或者对特殊字符转义：
# wrk -t12 -c1000 -d30s http://localhost:8080/api/user/page\?pageNum=1\&pageSize=10
```

## 📈 目标性能指标

- ✅ **并发用户数**: 1000+
- ✅ **QPS**: 1000+
- ✅ **平均响应时间**: < 100ms
- ✅ **P99 响应时间**: < 1s
- ✅ **错误率**: < 0.01%

## 📚 完整文档

详细的优化说明、配置参数、监控指南请查看：

- [性能优化完整文档](PERFORMANCE_OPTIMIZATION.md)

## 🔧 已实施的优化

1. ✅ **Tomcat 线程池**: 500 最大线程
2. ✅ **Hikari 连接池**: 80-100 连接
3. ✅ **Redis 连接池**: 200-300 连接
4. ✅ **缓存策略**: 多级缓存，命中率 70-90%
5. ✅ **数据库索引**: 组合索引优化
6. ✅ **异步处理**: 非关键操作异步化
7. ✅ **JVM 调优**: G1GC，4-12GB 堆内存

## ⚙️ 关键配置文件

- `src/main/resources/application-dev.yml` - 开发环境配置
- `src/main/resources/application-prod.yml` - 生产环境配置
- `src/main/resources/db/optimize_indexes.sql` - 数据库索引优化
- `start.sh` - 快速启动脚本
- `load_test.sh` - 压测脚本

## 🔍 监控接口

| 接口 | 说明 |
|------|------|
| `/api/health/ping` | 快速健康检查 |
| `/api/health/check` | 详细健康检查（DB + Redis） |
| `/api/health/metrics` | JVM 性能指标 |

## 💡 常见问题

### 端口被占用？
```bash
# 查找占用端口的进程
lsof -i :8080

# 停止进程
kill -9 <PID>
```

### MySQL 连接失败？
```bash
# 检查 MySQL 状态
brew services list | grep mysql

# 重启 MySQL
brew services restart mysql
```

### Redis 连接失败？
```bash
# 检查 Redis 状态
brew services list | grep redis

# 重启 Redis
brew services restart redis
```

## 📞 技术支持

遇到问题？查看完整文档：[PERFORMANCE_OPTIMIZATION.md](PERFORMANCE_OPTIMIZATION.md)
