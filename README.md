# course-cloud (微服务版校园选课系统)

**项目名称**: course-cloud  
**版本**: v2.0.0 (API 网关与统一认证)  
**基于**: course-cloud v1.2.0 (服务间通信与负载均衡)

## 项目简介

基于 Spring Cloud 的微服务架构选课系统，集成了 Nacos 服务注册与发现、OpenFeign 声明式服务调用、Resilience4j 熔断降级机制，支持多实例负载均衡部署。系统包含三个核心服务：catalog-service（课程管理）、user-service（用户管理）、enrollment-service（选课管理），以及新增的 gateway-service（API 网关服务）。

## 系统架构 (v2.0.0)

```text
客户端 → Gateway (8090) → [ User Service / Catalog Service / Enrollment Service ]
```

### 架构说明：
- 所有外部请求必须经过 Gateway（端口 8090）
- Gateway 负责统一的 JWT 认证与请求转发
- 后端服务从请求头中获取用户信息，无需重复验证 Token
- 认证白名单：`/api/auth/login`、`/api/auth/register`

## 版本演进

| 版本 | 核心特性 | 状态 |
|------|----------|------|
| v1.0.0 | 微服务拆分，RestTemplate 服务间调用 | ✅ 完成 |
| v1.1.0 | 集成 Nacos 服务注册与发现 | ✅ 完成 |
| v1.2.0 | OpenFeign + Resilience4j 熔断 + 多实例负载均衡 | ✅ 完成 |
| **v2.0.0** | **API Gateway + JWT 统一认证** | ✅ 当前版本 |

## 技术栈

- **Spring Boot**: 3.5.7
- **Spring Cloud**: 2023.0.1
- **服务网关**: Spring Cloud Gateway
- **JWT 认证**: jjwt (0.11.5)
- **服务注册发现**: Spring Cloud Alibaba Nacos Discovery
- **服务间通信**: Spring Cloud OpenFeign
- **熔断降级**: Spring Cloud CircuitBreaker Resilience4j
- **负载均衡**: Spring Cloud LoadBalancer
- **数据库**: MySQL 8.4
- **容器化**: Docker & Docker Compose
- **Java**: 17
- **构建工具**: Maven 3.8+

## 服务说明

### gateway-service (API 网关服务) - 新增
- **端口**: 8090
- **功能**:
    - 统一请求入口
    - JWT 认证与 Token 验证
    - 路由转发
    - CORS 跨域配置
- **核心配置**:
    - 路由规则：`/api/users/**` → `user-service`
    - 路由规则：`/api/courses/**` → `catalog-service`
    - 路由规则：`/api/enrollments/**` → `enrollment-service`
    - 白名单：`/api/auth/login`、`/api/auth/register`

### catalog-service (课程目录服务)
- **端口范围**: 8084-8086（多实例）
- **数据库**: catalog_db (3307)
- **功能**: 课程管理，支持负载均衡验证
- **核心API端点**:
    - `GET /api/courses` - 获取所有课程
    - `GET /api/courses/{id}` - 获取单个课程
    - `POST /api/courses` - 创建课程
    - `PUT /api/courses/{id}` - 更新课程（支持 enrolled 部分更新）
    - `GET /api/courses/ping` - 返回实例端口，用于负载均衡验证

### user-service (用户/学生服务)
- **端口范围**: 8081-8083（多实例）
- **数据库**: user_db (3309)
- **功能**: 学生/用户管理，支持负载均衡验证
- **新增认证接口**:
    - `POST /api/auth/login` - 用户登录，返回 JWT Token
- **核心API端点**:
    - `GET /api/students` - 获取所有学生
    - `GET /api/students/{id}` - 获取单个学生
    - `POST /api/students` - 创建学生
    - `GET /api/students/ping` - 返回实例端口，用于负载均衡验证

### enrollment-service (选课服务)
- **端口**: 8082
- **数据库**: enrollment_db (3308)
- **功能**:
    - 选课管理（通过 OpenFeign 调用其他服务）
    - 集成 Resilience4j 熔断降级保护
    - 支持多实例负载均衡
- **核心API端点**:
    - `GET /api/enrollments` - 获取所有选课记录
    - `POST /api/enrollments` - 学生选课（验证课程容量，更新已选人数）
    - `DELETE /api/enrollments/{id}` - 学生退课
    - `GET /api/enrollments/test` - 返回端口，用于负载均衡验证

## v2.0.0 新增特性

### 1. API 网关统一入口
- 创建 Gateway 服务（8090端口）
- 集成 Spring Cloud Gateway + Nacos 服务发现
- 所有外部请求必须经过网关

### 2. JWT 统一认证
- 登录接口 `/api/auth/login` 返回 JWT Token
- Token 算法：HS512，有效期 24 小时
- Gateway 中实现认证过滤器，验证 Token 并传递用户信息

### 3. 认证过滤器 (JwtAuthenticationFilter)
- 白名单路径直接放行
- 从 `Authorization: Bearer <token>` 请求头获取 Token
- 验证 Token 有效性，解析用户信息
- 将用户信息添加到请求头：`X-User-Id`、`X-Username`、`X-User-Role`

### 4. 后端服务获取用户信息
- 在 Controller 中使用 `@RequestHeader` 获取用户信息
- 无需重复验证 Token，提高系统安全性

### 5. CORS 跨域配置
- 在 Gateway 中配置全局 CORS，支持跨域请求

## Docker Compose 部署

### 一键启动

```bash
cd course-cloud
docker compose up -d --build
```

## 服务实例配置

- **Nacos**: 1个实例 (8848:8848)
- **gateway-service**: 1个实例 (8090:8090) - 新增
- **user-service**: 3个实例 (8081-8083)
- **catalog-service**: 3个实例 (8084-8086)
- **enrollment-service**: 1个实例 (8082)
- **MySQL**: 3个独立数据库实例

## 访问地址

| 服务 | URL | 说明 |
|------|-----|------|
| Nacos 控制台 | http://localhost:8848/nacos | 账号/密码: nacos/nacos |
| **Gateway 服务** | **http://localhost:8090** | **统一入口（新增）** |
| user-service 实例1 | http://localhost:8081 | 直连服务 |
| user-service 实例2 | http://localhost:8082 | 直连服务 |
| user-service 实例3 | http://localhost:8083 | 直连服务 |
| catalog-service 实例1 | http://localhost:8084 | 直连服务 |
| catalog-service 实例2 | http://localhost:8085 | 直连服务 |
| catalog-service 实例3 | http://localhost:8086 | 直连服务 |
| enrollment-service | http://localhost:8087 | 直连服务 |

## 测试验证

### 1. 登录认证测试

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 使用 Token 访问受保护 API
curl http://localhost:8090/api/students \
  -H "Authorization: Bearer <token>"

# 3. 未携带 Token 访问（预期 401）
curl http://localhost:8090/api/students

# 4. 测试白名单路径（无需认证）
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**预期结果：**
- 登录成功返回 Token
- 携带有效 Token 访问返回 200
- 未携带 Token 访问返回 401
- 白名单路径无需认证

### 2. 路由转发测试

```bash
# 访问课程服务（通过网关）
curl http://localhost:8090/api/courses

# 访问选课服务（通过网关）
curl http://localhost:8090/api/enrollments
```

### 3. 负载均衡测试
```bash
./test.sh
```

### 4. 熔断降级测试
```bash
./test2.sh
```

## 配置文件说明

### gateway-service 关键配置
```yaml
server:
  port: 8090

spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/courses/**
          filters:
            - StripPrefix=1
        - id: enrollment-service
          uri: lb://enrollment-service
          predicates:
            - Path=/api/enrollments/**
          filters:
            - StripPrefix=1

jwt:
  secret: "your-256-bit-secret-key-must-be-at-least-32-chars"
  expiration: 86400000  # 24小时
```

## 项目结构
```text
course-cloud/
├── .idea/                         # IntelliJ IDEA 配置文件
├── gateway-service/               # API 网关服务（新增）
│   ├── src/main/java/com/zjgsu/coursecloud/gateway/
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── util/
│   │       └── JwtUtil.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── catalog-service/               # 课程服务（多实例）
├── docs/                          # 文档目录
│   ├── week08-notes.md
│   └── week09-notes.md           # 本周任务文档（新增）
├── enrollment-service/            # 选课服务
├── user-service/                  # 用户服务（多实例）
│   ├── src/main/java/com/zjgsu/coursecloud/user/
│   │   ├── controller/
│   │   │   └── AuthController.java  # 新增
│   │   └── util/
│   │       └── JwtUtil.java         # 复制自 Gateway
│   └── src/main/resources/
├── nacos/                         # Nacos 相关配置
├── docker-compose.yml             # 基础 Docker Compose 配置
├── docker-compose-multi-instance.yml
├── README.md                      # 项目说明文档（本文件）
├── run.sh                         # 一键启动脚本
├── test.sh                        # 负载均衡测试脚本
├── test2.sh                       # 熔断降级测试脚本
└── VERSION                        # 版本文件（v2.0.0）
```

## 快速开始

### 1. 构建项目
```bash
# 赋予脚本执行权限
chmod +x run.sh test.sh test2.sh

# 构建并启动所有服务（包括 Gateway）
./run.sh
```

## 验证服务

### 2. 验证服务
```bash
# 检查 Gateway 是否启动
curl http://localhost:8090/actuator/health

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=gateway-service
```

### 3. API 测试（通过网关）
```bash
# 登录获取 Token
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 使用 Token 创建学生
curl -X POST http://localhost:8090/api/students \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"李四","studentId":"2023002","email":"lisi@example.com"}'

# 使用 Token 创建课程
curl -X POST http://localhost:8090/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"courseCode":"CS102","courseName":"数据结构","capacity":50,"enrolled":0}'

# 使用 Token 选课
curl -X POST http://localhost:8090/api/enrollments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"studentId":"2023002","courseId":1}'
```