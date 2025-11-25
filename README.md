# 校园选课系统（微服务版）

## 项目简介

- **项目名称**: course-cloud
- **版本**: v1.0.0
- **基于版本**: course:v1.1.0（hw04b）拆分
- **架构**: 微服务架构

## 架构说明
```text
客户端
│
├── catalog-service (8081) → catalog_db (3307)
│   └── 课程管理
│
└── enrollment-service (8082) → enrollment_db (3308)
    ├── 学生管理
    ├── 选课管理
    └── HTTP调用 → catalog-service（验证课程）
```

### 微服务拆分说明

| 服务名称 | 端口 | 数据库 | 主要功能 |
|---------|------|--------|----------|
| catalog-service | 8081 | catalog_db (3307) | 课程管理、课程信息维护 |
| enrollment-service | 8082 | enrollment_db (3308) | 学生管理、选课管理 |

## 技术栈

- **Spring Boot**: 3.5.6
- **Java**: 17
- **MySQL**: 8.4
- **Docker & Docker Compose**: 容器化部署
- **RestTemplate**: 服务间HTTP通信

## 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

## 项目结构

```text
course-cloud/
├── README.md
├── docker-compose.yml
├── test-services.sh
├── VERSION
├── catalog-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/zjsu/gjh/catalog/
│           │   ├── model/
│           │   │   ├── Course.java
│           │   │   ├── Instructor.java
│           │   │   └── ScheduleSlot.java
│           │   ├── repository/
│           │   │   └── CourseRepository.java
│           │   ├── service/
│           │   │   └── CourseService.java
│           │   ├── controller/
│           │   │   └── CourseController.java
│           │   ├── common/
│           │   │   └── ApiResponse.java
│           │   ├── exception/
│           │   │   ├── GlobalExceptionHandler.java
│           │   │   └── ResourceNotFoundException.java
│           │   └── CatalogServiceApplication.java
│           └── resources/
│               ├── application.yml
│               └── application-prod.yml
└── enrollment-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        └── main/
            ├── java/com/zjsu/gjh/enrollment/
            │   ├── model/
            │   │   ├── Student.java
            │   │   ├── Enrollment.java
            │   │   └── EnrollmentStatus.java
            │   ├── repository/
            │   │   ├── StudentRepository.java
            │   │   └── EnrollmentRepository.java
            │   ├── service/
            │   │   ├── StudentService.java
            │   │   └── EnrollmentService.java
            │   ├── controller/
            │   │   ├── StudentController.java
            │   │   └── EnrollmentController.java
            │   ├── common/
            │   │   └── ApiResponse.java
            │   ├── exception/
            │   │   ├── GlobalExceptionHandler.java
            │   │   └── ResourceNotFoundException.java
            │   └── EnrollmentServiceApplication.java
            └── resources/
                ├── application.yml
                └── application-prod.yml
```

## 构建和运行步骤

### 1. 克隆项目
```bash
git clone https://github.com/你的用户名/course-cloud
cd course-cloud
```

### 2. 构建项目
```bash
# 构建 catalog-service
cd catalog-service
mvn clean package
cd ..

# 构建 enrollment-service
cd enrollment-service
mvn clean package
cd ..
```

### 3. 启动所有服务
```bash
docker compose up -d --build
```

### 4. 检查服务状态
```bash
docker compose ps
```

### 5. 运行测试
```bash
chmod +x test-services.sh
./test-services.sh
```

## API 文档

### 课程目录服务 (8081)

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/courses` | 获取所有课程 |
| GET | `/api/courses/{code}` | 获取单个课程 |
| POST | `/api/courses` | 创建课程 |
| PUT | `/api/courses/{code}` | 更新课程 |
| DELETE | `/api/courses/{code}` | 删除课程 |

### 选课服务 (8082)

#### 学生管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/students` | 获取所有学生 |
| GET | `/api/students/{id}` | 获取单个学生 |
| POST | `/api/students` | 创建学生 |
| PUT | `/api/students/{id}` | 更新学生 |
| DELETE | `/api/students/{id}` | 删除学生 |

#### 选课管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/enrollments` | 获取所有选课记录 |
| GET | `/api/enrollments/course/{courseId}` | 按课程查询选课 |
| GET | `/api/enrollments/student/{studentId}` | 按学生查询选课 |
| POST | `/api/enrollments` | 学生选课 |
| DELETE | `/api/enrollments/{id}` | 学生退课 |

### 测试说明

#### 测试脚本

项目提供了完整的测试脚本 `test-services.sh`，包含以下测试场景：

- 课程创建测试 - 验证 catalog-service 功能
- 学生创建测试 - 验证 enrollment-service 功能
- 选课流程测试 - 验证服务间通信
- 错误处理测试 - 验证异常情况处理

#### 手动测试示例

```bash
# 创建课程
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CS101",
    "title": "计算机科学导论",
    "instructor": {
      "id": "T001",
      "name": "张教授",
      "email": "zhang@example.edu.cn"
    },
    "schedule": {
      "dayOfWeek": "MONDAY",
      "startTime": "08:00",
      "endTime": "10:00",
      "expectedAttendance": 50
    },
    "capacity": 60,
    "enrolled": 0
  }'

# 创建学生
curl -X POST http://localhost:8082/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "2024001",
    "name": "张三",
    "major": "计算机科学与技术",
    "grade": 2024,
    "email": "zhangsan@example.edu.cn"
  }'

# 选课（服务间通信）
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "CS101",
    "studentId": "2024001"
  }'
```


## 测试地址

- **课程目录服务**: http://localhost:8081/api/courses
- **选课服务（学生）**: http://localhost:8082/api/students
- **选课服务（选课）**: http://localhost:8082/api/enrollments  
