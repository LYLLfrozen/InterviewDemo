# interviewdemo

项目根说明：前端（React + TypeScript + Vite）和后端（Spring Boot）示例的集合。此文档将前后端的 README 合并，便于一处查看启动与配置说明。

目录
- [reactdemo](reactdemo) — 现代登录界面（React + TypeScript + Vite）
- [Springbootdemo](Springbootdemo) — Spring Boot 文章管理系统（MySQL + Redis + AI 聊天）

快速开始

- 前端（开发）：

  1. 进入目录并安装依赖：

	  ```bash
	  cd reactdemo
	  npm install
	  npm run dev
	  ```

  2. 打开浏览器：`http://localhost:5173`

- 后端（开发）：

  1. 准备数据库：在 MySQL 中创建数据库并导入示例表（示例数据库名：`0813-demo`）：

	  ```sql
	  CREATE DATABASE IF NOT EXISTS `0813-demo` DEFAULT CHARACTER SET utf8mb4;
	  USE `0813-demo`;
	  -- 创建 user 表 并插入测试数据（详见 Springbootdemo/README.md 中的 SQL）
	  ```

  2. 确保 Redis 运行（默认 localhost:6379）：

	  macOS：
	  ```bash
	  brew install redis
	  brew services start redis
	  # 或者临时运行
	  redis-server
	  ```

  3. 运行后端：

	  ```bash
	  cd Springbootdemo
	  mvn clean compile
	  mvn spring-boot:run
	  # 或者打包后运行：
	  mvn clean package
	  java -jar target/Springbootdemo-0.0.1-SNAPSHOT.jar
	  ```

  4. 服务启动后基础路径：`http://localhost:8080/api`

前端（reactdemo）概述

- 功能：现代登录界面，渐变背景、毛玻璃卡片、表单验证、记住我、社交登录入口占位、加载动画。
- 技术栈：React 18、TypeScript、Vite、CSS3 动画。
- 主要文件与目录：详见 [reactdemo/README.md](reactdemo/README.md)
- 常用命令：`npm install`、`npm run dev`、`npm run build`、`npm run preview`

后端（Springbootdemo）概述

- 功能：文章/用户管理、登录（生成 UUID token 并存 Redis）、AI 聊天（需动态配置 API Key）、用户 CRUD 接口。
- 技术栈：Spring Boot 4.0.1、MyBatis-Plus、MySQL、Redis、Lombok、Maven。
- 重要说明：AI 功能需先通过前端或接口 `POST /api/api-key/dashscope` 配置 DashScope API Key，Key 会安全存储在 Redis 中。
- 主要 API：
  - 登录：`POST /api/user/login`
  - 用户增删改查：`/api/user`（GET/POST/PUT/DELETE）
  - API Key 管理：`POST /api/api-key/dashscope`、`GET /api/api-key/dashscope/status`、`DELETE /api/api-key/dashscope`

配置说明（后端）

- 修改数据库连接：编辑 [Springbootdemo/src/main/resources/application-dev.yml](Springbootdemo/src/main/resources/application-dev.yml) 中的 `spring.datasource.username` 与 `spring.datasource.password`。
- Redis 配置：默认连接 `localhost:6379`，可在配置中调整。

常见问题与排查

- 启动失败：找不到数据库 → 请确认创建数据库并修改 `application-dev.yml` 中的连接信息。
- Redis 连接失败 → 启动 Redis 并用 `redis-cli ping` 验证返回 `PONG`。
- AI 聊天失败：API Key 未配置 → 在前端设置页或使用接口 `POST /api/api-key/dashscope` 设置 API Key。

参考与详细说明

- 前端完整说明：请查看 [reactdemo/README.md](reactdemo/README.md)
- 后端完整说明：请查看 [Springbootdemo/README.md](Springbootdemo/README.md)



