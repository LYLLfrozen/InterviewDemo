# Spring Boot 文章管理系统

## 项目简介

这是一个基于 Spring Boot 4.0.1 的文章管理，实现了用户的 CRUD 操作和登录功能，内置AI对话。使用 MySQL 存储用户数据，使用 Redis 缓存登录令牌。

## 技术栈

- **Spring Boot 4.0.1** - 应用框架
- **MyBatis-Plus 3.5.15** - ORM 框架
- **MySQL 8.0.33** - 数据库
- **Redis** - 缓存（登录令牌、API Key 配置）
- **LangChain4j** - AI 对话功能
- **Lombok** - 简化实体类代码
- **Maven** - 项目构建工具

## 重要说明：API Key 配置 🔐

本项目采用 **动态配置 API Key** 的方式，API Key 不在代码中写死，而是：
- ✅ 通过前端界面输入
- ✅ 安全存储在 Redis 中
- ✅ 不会泄露到版本控制系统
- ✅ 可随时更新或删除

**首次使用前，请先配置 API Key：**
1. 启动项目后，访问前端设置页面
2. 在点击AI聊天后可以看见API接口陪你
3. 在"API Key 配置"中输入您的 DashScope API Key
4. 或通过 API 接口设置（见下文）

## 环境准备

### 1. 安装并启动 MySQL

确保 MySQL 服务运行在 `localhost:3306`，创建数据库和表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `0813-demo` DEFAULT CHARACTER SET utf8mb4;

-- 使用数据库
USE `0813-demo`;

-- 创建用户表
CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(30) NULL DEFAULT NULL COMMENT '姓名',
  `age` INT(11) NULL DEFAULT NULL COMMENT '年龄',
  `email` VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
  `username` VARCHAR(50) NULL DEFAULT NULL COMMENT '用户名',
  `password` VARCHAR(100) NULL DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` (`name`, `age`, `email`, `username`, `password`) VALUES
('张三', 25, 'zhangsan@example.com', 'zhangsan', '123456'),
('李四', 30, 'lisi@example.com', 'lisi', '123456'),
('王五', 28, 'wangwu@example.com', 'wangwu', '123456');
```

**修改数据库连接配置**（如需要）：
编辑 `src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    username: root  # 修改为你的MySQL用户名
    password: root  # 修改为你的MySQL密码
```

### 2. 安装并启动 Redis

确保 Redis 服务运行在 `localhost:6379`。

**macOS 安装 Redis：**
```bash
# 使用 Homebrew 安装
brew install redis

# 启动 Redis 服务
brew services start redis

# 或者前台启动（用于测试）
redis-server
```

**检查 Redis 是否运行：**
```bash
redis-cli ping
# 应返回：PONG
```

## 如何运行项目

### 方式一：使用 Maven 命令运行

```bash
# 1. 进入项目目录
cd /Users/lyll/Documents/code/java/java/Springbootdemo

# 2. 清理并编译项目
mvn clean compile

# 3. 运行项目
mvn spring-boot:run
```

### 方式二：打包后运行

```bash
# 1. 打包项目
mvn clean package

# 2. 运行 JAR 包
java -jar target/Springbootdemo-0.0.1-SNAPSHOT.jar
```

**启动成功标志：**
控制台显示：
```
Tomcat started on port(s): 8080 (http)
Started SpringbootdemoApplication in X.XXX seconds
```

项目将运行在：`http://localhost:8080/api`

## API 接口文档

### 基础路径
```
http://localhost:8080/api
```

### API Key 管理接口 🔐

**在使用 AI 聊天功能前，必须先配置 API Key！**

#### 1. 设置 DashScope API Key ⭐
- **接口地址**：`POST /api/api-key/dashscope`
- **请求体**：
```json
{
  "apiKey": "sk-03bd6aaa934d4a0880baf3b55ead642d"
}
```
- **成功响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "API Key 设置成功"
}
```

#### 2. 获取 API Key 状态
- **接口地址**：`GET /api/api-key/dashscope/status`
- **响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "configured": true,
    "maskedKey": "sk-03****642d"
  }
}
```

#### 3. 删除 API Key
- **接口地址**：`DELETE /api/api-key/dashscope`
- **响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "API Key 已删除"
}
```

### 用户相关接口

#### 1. 用户登录 ⭐
- **接口地址**：`POST /api/user/login`
- **请求体**：
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```
- **成功响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
  }
}
```
- **失败响应**：
```json
{
  "code": 400,
  "msg": "用户名或密码错误",
  "data": null
}
```

#### 2. 新增用户
- **接口地址**：`POST /api/user`
- **请求体**：
```json
{
  "name": "测试用户",
  "age": 25,
  "email": "test@example.com",
  "username": "testuser",
  "password": "123456"
}
```

#### 3. 查询所有用户
- **接口地址**：`GET /api/user`
- **响应示例**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "age": 25,
      "email": "zhangsan@example.com",
      "username": "zhangsan",
      "password": "123456"
    }
  ]
}
```

#### 4. 查询单个用户
- **接口地址**：`GET /api/user/{id}`
- **示例**：`GET /api/user/1`

#### 5. 修改用户
- **接口地址**：`PUT /api/user`
- **请求体**：
```json
{
  "id": 1,
  "name": "张三",
  "age": 26,
  "email": "zhangsan@example.com",
  "username": "zhangsan",
  "password": "123456"
}
```

#### 6. 删除用户
- **接口地址**：`DELETE /api/user/{id}`
- **示例**：`DELETE /api/user/1`

## 使用 Apifox 测试接口

### 步骤一：创建项目和环境变量

1. **打开 Apifox**，创建新项目 "Spring Boot 用户系统"

2. **设置环境变量**：
   - 点击右上角 "环境" → "添加环境" → 命名为 "本地开发"
   - 添加环境变量：
     ```
     变量名: baseUrl
     当前值: http://localhost:8080/api
     ```

### 步骤二：创建登录接口

1. **新建接口**：
   - 接口名称：`用户登录`
   - 请求方法：`POST`
   - 请求路径：`{{baseUrl}}/user/login`

2. **设置请求 Body**：
   - 选择 `Body` → `JSON`
   - 输入：
   ```json
   {
     "username": "zhangsan",
     "password": "123456"
   }
   ```

3. **设置请求头**：
   - 添加 `Content-Type: application/json`

4. **点击"发送"按钮**

5. **查看响应结果**：
   - 成功时会返回 token
   - 失败时会提示 "用户名或密码错误"

### 步骤三：创建其他接口

可以按照上述方法创建其他接口：

- **查询所有用户**：`GET {{baseUrl}}/user`
- **查询单个用户**：`GET {{baseUrl}}/user/1`
- **新增用户**：`POST {{baseUrl}}/user`
- **修改用户**：`PUT {{baseUrl}}/user`
- **删除用户**：`DELETE {{baseUrl}}/user/1`

### 步骤四：保存 token（可选）

如果后续需要验证登录状态，可以在 Apifox 中：
1. 登录接口的 "后置操作" 中提取 token
2. 保存到环境变量中供后续接口使用

## 程序运行流程详解

### 整体架构

```
前端请求 → Controller 层 → Service 层 → Mapper 层 → MySQL 数据库
                                    ↓
                              Redis 缓存
```

### 登录流程详解

1. **用户发起登录请求**
   ```
   POST /api/user/login
   { "username": "zhangsan", "password": "123456" }
   ```

2. **Controller 层处理**（`UserController.login()`）
   - 接收 HTTP 请求
   - 解析请求体中的 username 和 password
   - 调用 `UserService.login()` 方法

3. **Service 层业务逻辑**（`UserServiceImpl.login()`）
   - 使用 MyBatis-Plus 的 LambdaQueryWrapper 查询数据库
   - 根据 username 查找用户：
     ```java
     wrapper.eq(User::getUsername, username);
     ```
   - 验证密码是否匹配
   - 如果验证失败，返回 null
   - 如果验证成功：
     - 生成 UUID 作为 token
     - 将 token 和 userId 存入 Redis，过期时间 30 分钟：
       ```
       Key: login:token:a1b2c3d4...
       Value: 用户ID
       TTL: 30分钟
       ```

4. **返回响应**
   - 成功：返回 token 给前端
   - 失败：返回错误信息

### Redis 缓存机制

**存储格式：**
```
Key: login:token:{token值}
Value: {userId}
TTL: 1800秒 (30分钟)
```

**查看 Redis 中的数据：**
```bash
# 连接 Redis
redis-cli

# 查看所有登录令牌
KEYS login:token:*

# 查看某个 token 的值
GET login:token:a1b2c3d4e5f6...

# 查看 token 剩余过期时间
TTL login:token:a1b2c3d4e5f6...
```

### 配置文件说明

**application.yml**（主配置）：
```yaml
spring:
  profiles:
    active: dev  # 激活 dev 环境配置
```

**application-dev.yml**（开发环境配置）：
- 服务器端口：8080
- 上下文路径：/api
- MySQL 连接配置
- Redis 连接配置
- MyBatis-Plus mapper 文件位置

## 常见问题

### 1. 启动失败：找不到数据库

**错误信息**：`Unknown database '0813-demo'`

**解决方法**：
```bash
mysql -u root -p
CREATE DATABASE `0813-demo` DEFAULT CHARACTER SET utf8mb4;
```

### 2. 启动失败：Redis 连接失败

**错误信息**：`Unable to connect to Redis`

**解决方法**：
```bash
# macOS
brew services start redis

# 或直接启动
redis-server
```

### 3. 登录失败：用户名或密码错误

**解决方法**：
- 检查数据库中是否有该用户数据
- 确认 username 和 password 字段是否正确

### 4. 端口被占用

**错误信息**：`Port 8080 was already in use`

**解决方法**：
- 修改 `application-dev.yml` 中的 `server.port`
- 或者关闭占用 8080 端口的程序

### 5. AI 聊天失败：API Key 未配置 🆕

**错误信息**：`API Key 未配置，请先在前端设置 DashScope API Key`

**解决方法**：
1. 访问 [阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/apiKey) 获取 API Key
2. 使用以下任一方式配置：
   - **方式一（推荐）**：在前端设置页面输入 API Key
   - **方式二**：使用 Apifox 调用 `POST /api/api-key/dashscope` 接口
   ```json
   {
     "apiKey": "sk-xxxxxxxxxxxxxxxxxxxxxxxx"
   }
   ```

### 6. 如何查看 Redis 中的 API Key

```bash
# 连接 Redis
redis-cli

# 查看 API Key
GET config:dashscope:api-key

# 删除 API Key（如需要）
DEL config:dashscope:api-key
```

## 项目结构

```
Springbootdemo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/springbootdemo/
│   │   │       ├── SpringbootdemoApplication.java  # 启动类
│   │   │       ├── common/
│   │   │       │   └── Result.java                 # 统一返回结果
│   │   │       ├── config/
│   │   │       │   ├── RedisConfig.java            # Redis 配置
│   │   │       │   ├── WebConfig.java              # Web 配置（跨域等）
│   │   │       │   ├── AiChatConfig.java           # AI Chat 配置 🆕
│   │   │       │   └── DynamicQwenChatModel.java   # 动态 API Key 模型 🆕
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java         # 用户控制器
│   │   │       │   ├── AiChatController.java       # AI 聊天控制器
│   │   │       │   └── ApiKeyController.java       # API Key 管理控制器 🆕
│   │   │       ├── entity/
│   │   │       │   ├── User.java                   # 用户实体
│   │   │       │   ├── Conversation.java           # 会话实体
│   │   │       │   └── MessageRecord.java          # 消息记录实体
│   │   │       ├── mapper/
│   │   │       │   ├── UserMapper.java             # 用户数据访问层
│   │   │       │   ├── ConversationMapper.java     # 会话数据访问层
│   │   │       │   └── MessageRecordMapper.java    # 消息记录数据访问层
│   │   │       └── service/
│   │   │           ├── UserService.java            # 用户服务接口
│   │   │           ├── AiChatService.java          # AI 聊天服务
│   │   │           ├── ConversationService.java    # 会话服务
│   │   │           ├── ApiKeyService.java          # API Key 管理服务 🆕
│   │   │           └── impl/
│   │   │               ├── UserServiceImpl.java    # 用户服务实现
│   │   │               ├── AiChatServiceImpl.java  # AI 聊天服务实现
│   │   │               └── ConversationServiceImpl.java  # 会话服务实现
│   │   └── resources/
│   │       ├── application.yml                      # 主配置文件
│   │       ├── application-dev.yml                  # 开发环境配置
│   │       └── application-prod.yml                 # 生产环境配置
│   └── test/                                        # 测试代码
├── pom.xml                                          # Maven 配置
└── README.md                                        # 项目说明文档
```

## 下一步优化建议

1. **密码加密**：使用 BCrypt 对密码进行加密存储
2. **JWT Token**：使用 JWT 替代 UUID，携带更多用户信息
3. **登录拦截器**：添加拦截器验证 token 有效性
4. **参数校验**：使用 @Valid 注解进行参数校验
5. **异常处理**：添加全局异常处理器
6. **日志记录**：使用 Logback 记录操作日志
7. **接口文档**：集成 Swagger/Knife4j 自动生成 API 文档
8. ~~**API Key 安全性**~~：✅ 已实现动态配置，存储在 Redis 中

## API Key 安全性说明 🔒

本项目实现了以下安全措施：

### 1. **不在代码中硬编码**
- ❌ 旧方式：API Key 写在 `application.yml` 中
- ✅ 新方式：通过接口动态配置，存储在 Redis 中

### 2. **存储位置**
- Redis Key: `config:dashscope:api-key`
- 不会保存到版本控制系统
- 不会在日志中打印

### 3. **访问控制**
- API Key 只在后端使用
- 前端只能设置和查看状态（脱敏显示）
- 建议为 API Key 管理接口添加管理员权限验证

### 4. **如何安全开源**
确保以下文件不包含敏感信息即可开源：
- ✅ `application.yml` - 不包含 API Key
- ✅ `application-dev.yml` - 不包含 API Key
- ✅ `.gitignore` - 已忽略本地配置文件
- ⚠️ 部署时通过前端界面或 API 配置 API Key

# 社交功能修改说明

## 修改概述
将添加好友和与好友聊天的方式从使用**用户ID**改为使用**用户名**。

## 后端修改

### 1. UserService 新增方法
**文件**: `Springbootdemo/src/main/java/com/example/springbootdemo/service/UserService.java`

新增方法：
```java
/**
 * 根据用户名查找用户
 */
User getUserByUsername(String username);
```

### 2. UserServiceImpl 实现新方法
**文件**: `Springbootdemo/src/main/java/com/example/springbootdemo/service/impl/UserServiceImpl.java`

实现通过用户名查找用户的方法。

### 3. SocialService 新增方法
**文件**: `Springbootdemo/src/main/java/com/example/springbootdemo/service/SocialService.java`

新增方法：
```java
/**
 * 发送好友请求（通过用户名）
 */
void sendFriendRequestByUsername(Long fromUserId, String toUsername);

/**
 * 发送消息（通过用户名）
 */
Message sendMessageByUsername(Long fromUserId, String toUsername, String content);
```

### 4. SocialServiceImpl 实现新方法
**文件**: `Springbootdemo/src/main/java/com/example/springbootdemo/service/impl/SocialServiceImpl.java`

- 注入 `UserMapper` 用于查询用户
- 实现 `sendFriendRequestByUsername`: 先根据用户名查找用户，再调用原有的发送好友请求方法
- 实现 `sendMessageByUsername`: 先根据用户名查找用户，再调用原有的发送消息方法

### 5. SocialController 修改接口
**文件**: `Springbootdemo/src/main/java/com/example/springbootdemo/controller/SocialController.java`

#### 发送好友请求接口 `/api/social/friend-request/send`
- **修改前**: 只接受 `toUserId` 参数
- **修改后**: 同时支持 `toUsername` 和 `toUserId` 参数
  - 优先使用 `toUsername`（如果提供）
  - 否则使用 `toUserId`（向后兼容）

示例请求：
```json
{
  "toUsername": "zhangsan"
}
```

#### 发送消息接口 `/api/social/message/send`
- **修改前**: 只接受 `toUserId` 参数
- **修改后**: 同时支持 `toUsername` 和 `toUserId` 参数
  - 优先使用 `toUsername`（如果提供）
  - 否则使用 `toUserId`（向后兼容）

示例请求：
```json
{
  "toUsername": "lisi",
  "content": "你好！"
}
```

## 前端修改

### 1. FriendRequest 组件
**文件**: `reactdemo/src/components/social/FriendRequest.tsx`

- 将输入框从 `type="number"` 改为 `type="text"`
- 将 placeholder 从 "输入用户ID" 改为 "输入用户名"
- 修改发送请求时的参数从 `toUserId: parseInt(searchUserId)` 改为 `toUsername: searchUserId.trim()`
- 修改错误提示从 "请输入用户ID" 改为 "请输入用户名"

### 2. ChatWindow 组件
**不需要修改**，因为：
- ChatWindow 通过好友列表选择好友，获取的是 friendId
- 发送消息时仍然使用 toUserId，后端接口向后兼容
- 如果需要通过用户名聊天，需要额外实现用户搜索功能

## 向后兼容性

所有修改都保持了向后兼容：
- 原有的通过用户ID添加好友和发送消息的功能仍然可用
- 新增的通过用户名的功能作为额外选项
- 如果同时提供用户名和用户ID，优先使用用户名

## 测试建议

1. 测试通过用户名添加好友
2. 测试通过用户名发送消息（需要先成为好友）
3. 测试用户名不存在的情况
4. 测试空用户名的情况
5. 测试向后兼容：使用原有的用户ID方式

## 数据库说明

本修改**不需要**修改数据库表结构。所有表保持不变：
- `user` 表已包含 `username` 字段（唯一索引）
- `friend_request` 和 `friend` 表仍使用用户ID
- `message` 表仍使用用户ID

用户名只是作为查找用户的一种方式，内部仍然使用用户ID进行关联。


## 联系方式

如有问题，请联系开发团队。

---

**最后更新时间**：2026年1月5日
