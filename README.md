<<<<<<< HEAD
﻿# course-cloud (微服务版校园选课系统)

**项目名称**: course-cloud  
**版本**: v1.1.0  
**基于**: course-cloud v1.0.0 (单体应用)

## 项目简介

基于单体选课系统拆分的微服务实现，保持 JDK 17：catalog-service（8081）负责课程目录，user-service（8083）负责用户/学生，enrollment-service（8082）负责选课，服务注册发现使用 Nacos。

## 架构图

```text
客户端
↓
├─→ catalog-service (8081) → catalog_db (3307)
│ └── 课程管理 + /api/courses/ping 返回端口
│
├─→ user-service (8083) → user_db (3309)
│ └── 学生/用户管理 + /api/students/ping 返回端口
│
└─→ enrollment-service (8082) → enrollment_db (3308)
├── 选课管理（RestTemplate @LoadBalanced 通过服务名调用 catalog-service 验证容量、更新已选人数）
├── 学生接口：本地存储并同步到 user-service
└── /api/enrollments/test 返回端口
Nacos (8848/9848) 提供服务注册发现
```
包名规范：`com.zjsu.gjh.*`。

## 技术栈

- **Spring Boot**: 3.5.7，Spring Web，Spring Data JPA，Validation，Actuator
- **Spring Cloud Alibaba Nacos Discovery**: 2023.0.1.0
- **Java**: 17
- **MySQL**: 8.4
- **Docker & Docker Compose**: 容器化部署
- **RestTemplate + @LoadBalanced**: 基于服务名调用

## 环境要求

- JDK 17+
- Maven 3.8+
- Docker 20.10+，Docker Compose 2.0+
- 可选：`jq`（解析测试脚本输出）

## 服务说明

### catalog-service (课程目录服务)

- **端口**: 8081
- **数据库**: catalog_db (3307)
- **功能**: 课程管理
- **API端点**:
    - `GET /api/courses` - 获取所有课程
    - `GET /api/courses/{id}` - 获取单个课程
    - `GET /api/courses/code/{code}` - 按课程代码查询
    - `POST /api/courses` - 创建课程
    - `PUT /api/courses/{id}` - 更新课程（支持 enrolled 部分更新）
    - `DELETE /api/courses/{id}` - 删除课程
    - `GET /api/courses/ping` - 返回端口，用于负载均衡验证

### user-service (用户/学生服务)

- **端口**: 8083
- **数据库**: user_db (3309)
- **功能**: 学生/用户管理
- **API端点**:
    - `GET /api/students` - 获取所有学生
    - `GET /api/students/{id}` - 获取单个学生
    - `GET /api/students/studentId/{studentId}` - 按学号查询
    - `POST /api/students` - 创建学生
    - `PUT /api/students/{id}` - 更新学生
    - `DELETE /api/students/{id}` - 删除学生
    - `GET /api/students/ping` - 返回端口，用于负载均衡验证

### enrollment-service (选课服务)

- **端口**: 8082
- **数据库**: enrollment_db (3308)
- **功能**: 选课管理，通过@LoadBalanced RestTemplate调用其他服务，验证课程容量并更新已选人数
- **API端点**:
    - `GET /api/students` - 获取所有学生（本地存储）
    - `GET /api/students/{id}` - 获取单个学生（本地存储）
    - `POST /api/students` - 创建学生（同步到 user-service）
    - `PUT /api/students/{id}` - 更新学生（同步到 user-service）
    - `DELETE /api/students/{id}` - 删除学生（同步到 user-service）
    - `GET /api/enrollments` - 获取所有选课记录
    - `GET /api/enrollments/course/{courseId}` - 按课程查询选课
    - `GET /api/enrollments/student/{studentId}` - 按学生查询选课
    - `POST /api/enrollments` - 学生选课（通过 Nacos 调用 catalog-service 校验课程；调用 user-service 校验学生）
    - `DELETE /api/enrollments/{id}` - 学生退课
    - `GET /api/enrollments/test` - 返回端口，用于负载均衡验证

## 本地运行（单服务调试）

需先启动 Nacos（如 `docker run -d -p 8848:8848 nacos/nacos-server:v3.1.0 --mode standalone`）。

### catalog-service（8081）

```text
cd catalog-service
mvn clean package -DskipTests
java -jar target/catalog-service-1.0.0.jar
--spring.profiles.active=prod
--DB_URL=jdbc:mysql://localhost:3306/catalog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
--DB_USERNAME=catalog_user --DB_PASSWORD=catalog_pass
--NACOS_ADDR=localhost:8848
```

### user-service（8083）
```text
cd ../user-service
mvn clean package -DskipTests
java -jar target/user-service-1.0.0.jar
--spring.profiles.active=prod
--DB_URL=jdbc:mysql://localhost:3306/user_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
--DB_USERNAME=user_user --DB_PASSWORD=user_pass
--NACOS_ADDR=localhost:8848
```

### enrollment-service（8082）
```text
cd ../enrollment-service
mvn clean package -DskipTests
java -jar target/enrollment-service-1.0.0.jar
--spring.profiles.active=prod
--DB_URL=jdbc:mysql://localhost:3306/enrollment_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
--DB_USERNAME=enrollment_user --DB_PASSWORD=enrollment_pass
--NACOS_ADDR=localhost:8848
```

## Docker Compose 一键启动
```text
cd course-cloud
docker compose up -d --build
```

**访问地址**：
- **Nacos 控制台**：http://localhost:8848/nacos（默认账号密码：nacos / nacos）
- **catalog-service**: http://localhost:8081
- **enrollment-service**: http://localhost:8082
- **user-service**: http://localhost:8083

**健康检查**：`/actuator/health`（三服务均开启）

## 测试

- **hw06 API 验证**：`bash test-services.sh`
- **hw07 Nacos/负载均衡验证**：`bash scripts/nacos-test.sh`

## 目录结构
```text
course-cloud/
├── catalog-service/            # 课程目录微服务（8081）
├── enrollment-service/         # 选课微服务（8082）
├── user-service/               # 用户/学生微服务（8083）
├── docker-compose.yml          # Nacos + 3 个 MySQL + 3 个服务
├── scripts/nacos-test.sh       # Nacos 注册/负载均衡验证脚本
├── test-services.sh            # 端到端 API 测试脚本（hw06）
├── docs/week07-reflection.md   # hw07 思考
├── VERSION                     # 1.1.0
└── README.md
```
=======
# 校园选课系统 - 微服务版

**项目名称**: course-cloud
**版本**: v1.0.0
**基于**: course v1.1.0 (单体应用)

## 项目简介

本项目是将单体选课系统拆分为微服务架构的实践项目。通过服务拆分、独立数据库、HTTP通信等技术，实现了课程管理、学生管理和选课管理的解耦。

## 架构图

```
客户端
  ↓
  ├─→ user-service (8081) → user_db (3306)
  │   └── 学生/用户管理
  │
  ├─→ catalog-service (8082) → catalog_db (3307)
  │   └── 课程管理
  │
  └─→ enrollment-service (8083) → enrollment_db (3308)
      ├── 选课管理
      ├── HTTP调用 → user-service（验证学生）
      └── HTTP调用 → catalog-service（验证课程）
```

## 技术栈

- **Spring Boot**: 3.5.7
- **Java**: 25
- **MySQL**: 8.4
- **Docker & Docker Compose**: 容器化部署
- **RestTemplate**: 服务间通信

## 服务说明

### user-service (用户服务)

- **端口**: 8081
- **数据库**: user_db (3306)
- **功能**: 学生/用户管理
- **API端点**:
  - `GET /api/students` - 获取所有学生
  - `GET /api/students/{id}` - 获取单个学生
  - `GET /api/students/studentId/{studentId}` - 按学号查询
  - `POST /api/students` - 创建学生
  - `PUT /api/students/{id}` - 更新学生
  - `DELETE /api/students/{id}` - 删除学生

### catalog-service (课程目录服务)

- **端口**: 8082
- **数据库**: catalog_db (3307)
- **功能**: 课程管理
- **API端点**:
  - `GET /api/courses` - 获取所有课程
  - `GET /api/courses/{id}` - 获取单个课程
  - `GET /api/courses/code/{code}` - 按课程代码查询
  - `POST /api/courses` - 创建课程
  - `PUT /api/courses/{id}` - 更新课程
  - `DELETE /api/courses/{id}` - 删除课程

### enrollment-service (选课服务)

- **端口**: 8083
- **数据库**: enrollment_db (3308)
- **功能**: 选课管理，通过RestTemplate调用user-service和catalog-service
- **API端点**:
  - `GET /api/enrollments` - 获取所有选课记录
  - `GET /api/enrollments/course/{courseId}` - 按课程查询选课
  - `GET /api/enrollments/student/{studentId}` - 按学生查询选课
  - `POST /api/enrollments` - 学生选课
  - `DELETE /api/enrollments/{id}` - 学生退课

## 环境要求

- JDK 25+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+

## 构建和运行步骤

### 快速启动（推荐）

使用 `run.sh` 脚本一键构建并启动所有服务：

```bash
# 赋予执行权限（首次运行需要）
chmod +x run.sh

# 构建并启动所有服务
./run.sh
```

脚本会自动完成以下操作：
1. 编译所有服务的 JAR 文件
2. 构建 Docker 镜像并启动容器
3. 等待服务启动完成
4. 显示服务状态和访问地址

### 手动构建

#### 1. 构建所有服务

```bash
# 构建 user-service
cd user-service
mvn clean package -DskipTests
cd ..

# 构建 catalog-service
cd catalog-service
mvn clean package -DskipTests
cd ..

# 构建 enrollment-service
cd enrollment-service
mvn clean package -DskipTests
cd ..
```

#### 2. 使用 Docker Compose 部署

```bash
# 启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

#### 3. 验证服务

```bash
# 检查 user-service
curl http://localhost:8081/api/students

# 检查 catalog-service
curl http://localhost:8082/api/courses

# 检查 enrollment-service
curl http://localhost:8083/api/enrollments
```

## 测试说明

运行测试脚本：

```bash
chmod +x test-services.sh
./test-services.sh
```

测试脚本会执行以下操作：

1. 创建学生（user-service）
2. 获取所有学生
3. 创建课程（catalog-service）
4. 获取所有课程
5. 学生选课（验证服务间通信）
6. 查询选课记录
7. 测试学生不存在的错误处理
8. 测试课程不存在的错误处理

## 服务间通信示例

enrollment-service 通过 RestTemplate 调用其他服务：

```java
// 验证学生是否存在
String userUrl = userServiceUrl + "/api/students/studentId/" + studentId;
Map<String, Object> studentResponse = restTemplate.getForObject(userUrl, Map.class);

// 验证课程是否存在
String courseUrl = catalogServiceUrl + "/api/courses/" + courseId;
Map<String, Object> courseResponse = restTemplate.getForObject(courseUrl, Map.class);
```

## 数据库配置

| 服务 | 数据库 | 端口 | 用户名 | 密码 |
|------|--------|------|--------|------|
| user-service | user_db | 3306 | user_user | user_pass |
| catalog-service | catalog_db | 3307 | catalog_user | catalog_pass |
| enrollment-service | enrollment_db | 3308 | enrollment_user | enrollment_pass |

## 常见问题

### Q: 服务启动失败？

A: 检查端口是否被占用，确保 8081/8082/8083 和 3306/3307/3308 端口可用。

### Q: 服务间调用失败？

A: 确保所有服务都已启动，检查 docker logs 查看具体错误。

### Q: 数据库连接失败？

A: 等待数据库健康检查完成，通常需要 10-15 秒。

## 项目结构

```
course-cloud/
├── README.md
├── docker-compose.yml
├── run.sh                # 一键启动脚本
├── test-services.sh
├── user-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── catalog-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
└── enrollment-service/
    ├── src/
    ├── Dockerfile
    └── pom.xml
```


>>>>>>> 668c9acc40df4baace1d588ee8a747e9f8a21abc
