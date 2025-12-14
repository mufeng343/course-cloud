# course-cloud (微服务版校园选课系统)

**项目名称**: course-cloud  
**版本**: v1.2.0 (服务间通信与负载均衡)  
**基于**: course-cloud v1.1.0 (服务注册与发现)

## 项目简介

基于 Spring Cloud 的微服务架构选课系统，集成了 Nacos 服务注册与发现、OpenFeign 声明式服务调用、Resilience4j 熔断降级机制，支持多实例负载均衡部署。系统包含三个核心服务：catalog-service（课程管理）、user-service（用户管理）、enrollment-service（选课管理）。

## 系统架构

```text
客户端
  ↓
  ├─→ Nacos (8848/9848) - 服务注册与发现中心
  │
  ├─→ catalog-service (多实例: 8084-8086) → catalog_db (3307)
  │   └── 课程管理 + 负载均衡验证端点
  │
  ├─→ user-service (多实例: 8081-8083) → user_db (3309)
  │   └── 学生/用户管理 + 负载均衡验证端点
  │
  └─→ enrollment-service (8082)
      ├── 选课管理（通过 OpenFeign 调用其他服务）
      ├── Resilience4j 熔断降级保护
      ├── 负载均衡客户端（Ribbon）
      └── enrollment_db (3308)
```
**包名规范**: `com.zjgsu.gjh.*`

## 版本演进

| 版本 | 核心特性 | 状态 |
|------|----------|------|
| v1.0.0 | 微服务拆分，RestTemplate 服务间调用 | ✅ 完成 |
| v1.1.0 | 集成 Nacos 服务注册与发现 | ✅ 完成 |
| **v1.2.0** | **OpenFeign + Resilience4j 熔断 + 多实例负载均衡** | ✅ 当前版本 |

## 技术栈

- **Spring Boot**: 3.5.7
- **Spring Cloud**: 2023.0.1
- **服务注册发现**: Spring Cloud Alibaba Nacos Discovery
- **服务间通信**: Spring Cloud OpenFeign
- **熔断降级**: Spring Cloud CircuitBreaker Resilience4j
- **负载均衡**: Spring Cloud LoadBalancer
- **数据库**: MySQL 8.4
- **容器化**: Docker & Docker Compose
- **Java**: 17
- **构建工具**: Maven 3.8+

## 服务说明

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

## v1.2.0 新增特性

### 1. OpenFeign 声明式服务调用

- 替换原有的 RestTemplate，采用声明式接口调用
- 创建 `UserClient` 和 `CatalogClient` Feign 接口
- 简化服务间调用代码，提高可读性

### 2. Resilience4j 熔断降级

- 配置失败率阈值 50%，滑动窗口大小 10 次
- 实现 Fallback 降级处理机制
- 服务不可用时触发降级逻辑，防止级联故障

### 3. 多实例负载均衡

- 每个服务启动 3 个实例
- 通过 Nacos 服务发现实现客户端负载均衡
- 请求均匀分发到不同实例

### 4. 增强配置

- 连接超时：3秒
- 读取超时：5秒
- 熔断器按服务独立配置

## Docker Compose 部署

### 一键启动

```bash
cd course-cloud
docker compose up -d --build
```

## 服务实例配置

- **Nacos**: 1个实例 (8848:8848)
- **user-service**: 3个实例 (8081-8083)
- **catalog-service**: 3个实例 (8084-8086)
- **enrollment-service**: 1个实例 (8082)
- **MySQL**: 3个独立数据库实例

## 访问地址

| 服务 | URL | 说明 |
|------|-----|------|
| Nacos 控制台 | http://localhost:8848/nacos | 账号/密码: nacos/nacos |
| user-service | http://localhost:8081 | 实例1 |
| user-service | http://localhost:8082 | 实例2 |
| user-service | http://localhost:8083 | 实例3 |
| catalog-service | http://localhost:8084 | 实例1 |
| catalog-service | http://localhost:8085 | 实例2 |
| catalog-service | http://localhost:8086 | 实例3 |
| enrollment-service | http://localhost:8087 | 主服务 |

## 测试验证

### 1. 负载均衡测试

```bash
# 执行负载均衡测试脚本
bash test.sh
```

**预期结果:**

- 请求均匀分配到不同实例
- user-service 3个实例接收近似相等的请求数
- catalog-service 3个实例接收近似相等的请求数

### 2. 熔断降级测试

```bash
# 执行熔断器测试脚本
bash test2.sh
```

**测试步骤:**

1. 停止所有 user-service 实例
2. 发送选课请求
3. 观察熔断器触发和 Fallback 执行
4. 重启服务验证恢复

### 3. 健康检查

```bash
# 检查各服务健康状态
curl http://localhost:8087/actuator/health
```

## 配置文件说明

### enrollment-service 关键配置

```yaml
# application.yml
feign:
  circuitbreaker:
    enabled: true
  client:
    config:
      default:
        connectTimeout: 3000
        readTimeout: 5000

resilience4j:
  circuitbreaker:
    instances:
      user-service:
        failureRateThreshold: 50
        slidingWindowSize: 10
      catalog-service:
        failureRateThreshold: 50
        slidingWindowSize: 10
```

## 项目结构
```text
course-cloud/
├── .idea/                         # IntelliJ IDEA 配置文件
├── catalog-service/               # 课程服务（多实例）
│   ├── src/main/java/com/zjgsu/coursecloud/catalog/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── pom.xml
├── docs/                          # 文档目录
│   └── week08-notes.md           # 本周任务文档
├── enrollment-service/            # 选课服务
│   ├── src/main/java/com/zjgsu/coursecloud/enrollment/
│   │   ├── client/               # OpenFeign 客户端
│   │   │   ├── UserClient.java
│   │   │   ├── UserClientFallback.java
│   │   │   ├── CatalogClient.java
│   │   │   └── CatalogClientFallback.java
│   │   ├── dto/
│   │   │   ├── StudentDto.java
│   │   │   └── CourseDto.java
│   │   └── service/
│   │       └── EnrollmentService.java
│   ├── src/main/resources/
│   │   └── application.yml       # Feign + Resilience4j 配置
│   ├── Dockerfile
│   └── pom.xml
├── nacos/                         # Nacos 相关配置
├── user-service/                  # 用户服务（多实例）
│   ├── src/main/java/com/zjgsu/coursecloud/user/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml             # 基础 Docker Compose 配置
├── docker-compose-multi-instance.yml # 多实例部署配置
├── README.md                      # 项目说明文档（本文件）
├── run.sh                         # 一键启动脚本
├── test.sh                        # 负载均衡测试脚本
├── test2.sh                       # 熔断降级测试脚本
└── VERSION                        # 版本文件（v1.2.0）
```

## 快速开始
### 1.构建项目
```bash
# 赋予脚本执行权限
chmod +x run.sh test.sh test2.sh

# 构建并启动所有服务
./run.sh
```
### 2.验证服务
```bash
# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service

# 测试负载均衡
./test.sh

# 测试熔断降级
./test2.sh
```
### 3.API测试
```bash
# 创建学生
curl -X POST http://localhost:8087/api/students \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","studentId":"2023001","email":"zhangsan@example.com"}'

# 创建课程
curl -X POST http://localhost:8084/api/courses \
  -H "Content-Type: application/json" \
  -d '{"courseCode":"CS101","courseName":"计算机基础","capacity":30,"enrolled":0}'

# 学生选课
curl -X POST http://localhost:8087/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId":"2023001","courseId":1}'
```