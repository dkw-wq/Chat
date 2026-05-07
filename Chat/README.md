# Chat 在线聊天系统

本项目采用前后端分离结构：

- 后端：Spring Boot + Spring Security + MyBatis + MySQL
- 前端：Vue 3 + Vite

## 目录结构

```text
Chat/
  src/                 后端 Spring Boot 代码
  frontend/            前端 Vue 代码
  pom.xml              后端 Maven 配置
```

## 1. 准备 MySQL

先在 MySQL 中创建数据库和项目账号：

```sql
CREATE DATABASE IF NOT EXISTS chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'chat_user'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON chat.* TO 'chat_user'@'localhost';
FLUSH PRIVILEGES;
```

项目通过环境变量读取数据库账号密码。PowerShell 中执行：

```powershell
[Environment]::SetEnvironmentVariable("CHAT_DB_USERNAME", "chat_user", "User")
[Environment]::SetEnvironmentVariable("CHAT_DB_PASSWORD", "你的密码", "User")
```

设置后请重新打开 PowerShell，让新的环境变量生效。

## 2. 启动后端

进入后端项目目录：

```powershell
cd C:\Users\dkw\.a_dkwrtc\Chat\Chat
```

启动 Spring Boot：

```powershell
.\mvnw.cmd spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

如果只是本地演示，不想连接 MySQL，可以用 H2 内存数据库启动：

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## 3. 启动前端

另开一个 PowerShell，进入 Vue 项目目录：

```powershell
cd C:\Users\dkw\.a_dkwrtc\Chat\Chat\frontend
```

首次启动前安装依赖：

```powershell
npm install
```

启动 Vue：

```powershell
npm run dev
```

前端地址：

```text
http://localhost:5173
```

前端会通过 Vite 代理把 `/api` 请求转发到后端 `http://localhost:8080`。

## 4. 常用命令

后端测试：

```powershell
cd C:\Users\dkw\.a_dkwrtc\Chat\Chat
.\mvnw.cmd test
```

前端构建：

```powershell
cd C:\Users\dkw\.a_dkwrtc\Chat\Chat\frontend
npm run build
```

查看 8080 和 5173 端口：

```powershell
Get-NetTCPConnection -LocalPort 8080,5173 -ErrorAction SilentlyContinue
```

停止指定进程：

```powershell
Stop-Process -Id 进程ID
```

## 5. 当前已实现功能

- 用户注册
- 用户登录
- 获取当前登录用户
- 退出登录
- Vue 登录页、注册页、首页
- 搜索用户
- 发送好友申请
- 查看收到和发出的好友申请
- 同意或拒绝好友申请
- 好友列表
- 删除好友
- 好友聊天窗口
- 发送聊天消息
- 查询聊天记录
- MySQL 用户表初始化

## 6. 访问入口

开发时请优先访问 Vue 前端：

```text
http://localhost:5173
```

后端接口服务运行在：

```text
http://localhost:8080
```
