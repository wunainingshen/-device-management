# 设备管理系统 (Device Management System)

## 技术栈

- **前端**: Vue 3 + Element Plus + Pinia + Vue Router + Axios + WebSocket
- **后端**: Spring Boot 2.7 + Spring Security + JPA + WebSocket
- **数据库**: MySQL 8.0
- **AI**: DeepSeek API

## 功能特性

### 用户系统
- 普通用户登录 / 管理员登录
- 用户注册（仅注册为普通用户）
- 个人信息修改（昵称、邮箱、手机号）
- 密码修改

### 设备管理（管理员）
- 设备的增删改查
- 设备数据 Excel 导入导出
- 按名称、分类、状态搜索设备

### 用户管理（管理员）
- 查看所有用户
- 新增用户
- 禁用/启用用户
- 删除用户

### 在线聊天（WebSocket 实时）
- 添加/删除好友
- 拉黑/解除拉黑好友
- 黑名单管理
- 实时消息收发
- 在线状态显示（在线/多久前在线）
- 微信风格时间标签
- 消息已读功能
- 被拉黑提示"您已被对方拉黑，无法发送消息"

### AI 智能助手
- 接入 DeepSeek 大模型
- 智能问答

## 快速开始

### 前置条件

1. JDK 11+
2. Node.js 16+
3. MySQL 8.0+
4. Maven 3.6+

### 数据库配置

1. 启动 MySQL 服务
2. 创建数据库（应用会自动创建表）：

```sql
CREATE DATABASE IF NOT EXISTS device_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

### 启动后端

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | user123 |

## API 接口

### 认证接口
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册

### 用户接口
- `GET /api/user/info` - 获取当前用户信息
- `PUT /api/user/update` - 更新个人信息
- `PUT /api/user/password` - 修改密码
- `GET /api/user/search?keyword=` - 搜索用户

### 管理员接口
- `GET /api/admin/users` - 获取用户列表
- `POST /api/admin/users` - 新增用户
- `PUT /api/admin/users/{id}/toggle-status` - 切换用户状态
- `DELETE /api/admin/users/{id}` - 删除用户
- `GET /api/admin/devices` - 获取设备列表
- `POST /api/admin/devices` - 添加设备
- `PUT /api/admin/devices/{id}` - 更新设备
- `DELETE /api/admin/devices/{id}` - 删除设备

### 设备接口
- `GET /api/device/list` - 获取设备列表
- `GET /api/device/export` - 导出 Excel
- `POST /api/device/import` - 导入 Excel

### 好友接口
- `POST /api/friend/request` - 发送好友请求
- `GET /api/friend/list` - 好友列表
- `DELETE /api/friend/{friendId}` - 删除好友
- `POST /api/friend/block/{userId}` - 拉黑用户
- `POST /api/friend/unblock/{userId}` - 解除拉黑
- `GET /api/friend/blacklist` - 黑名单列表

### 聊天接口
- `GET /api/chat/conversation/{friendId}` - 获取聊天记录
- `POST /api/chat/read/{friendId}` - 标记已读

### AI 接口
- `POST /api/ai/chat` - AI 对话

## WebSocket

WebSocket 连接地址: `ws://localhost:3000/ws/chat?userId={userId}`

消息类型：
- `CHAT` - 聊天消息
- `CHAT_ACK` - 消息发送确认
- `MARK_READ` - 标记已读
- `MESSAGE_READ` - 已读回执
- `USER_STATUS` - 用户在线状态变更
- `TYPING` - 输入中
- `ERROR` - 错误消息
