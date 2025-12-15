# week09-notes.md

## 一、Gateway 路由配置说明

在本周的课程项目中，我们引入了 **Spring Cloud Gateway** 作为系统的统一入口，替代了原有的直接服务调用方式。所有外部请求将通过 Gateway（端口 8090）进行统一的 JWT 认证和路由转发。

**核心路由配置如下：**

```yaml
spring:
  cloud:
    gateway:
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
```

**说明：**

- 使用 `lb://` 前缀实现负载均衡，自动从 Nacos 发现服务实例。
- `StripPrefix=1` 用于移除路径中的 `/api` 前缀，确保请求能正确转发至后端服务。
- 白名单路径（如 `/api/auth/login`）在 JWT 过滤器中进行放行。

---

## 二、JWT 认证流程说明

本次实现了基于 JWT 的统一认证机制，流程如下：

1. **用户登录**：
    - 客户端发送 POST 请求至 `/api/auth/login`，携带用户名和密码。
    - User Service 验证用户身份，生成 JWT Token 并返回。

2. **Token 格式**：
    - 使用 HS512 算法，密钥长度至少 256 位。
    - Token 有效期为 24 小时。

3. **Gateway 认证过滤**：
    - 除白名单外，所有请求需在 `Authorization` 头中携带 `Bearer <token>`。
    - Gateway 解析并验证 Token，将用户信息（userId、username、role）添加到请求头：
        - `X-User-Id`
        - `X-Username`
        - `X-User-Role`

4. **后端服务获取用户信息**：
    - 后端服务通过 `@RequestHeader` 直接获取用户信息，无需再次验证 Token。

**流程图示意：**
```text
客户端 → Gateway（验证Token） → 后端服务（获取用户信息） → 返回结果
```

---

## 三、测试结果截图

### 1. 登录测试（返回 Token）


**说明：**  
POST `/api/auth/login` 成功返回 JWT Token 和用户信息。

**请求示例：**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**返回示例：**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "1",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### 2. 未认证访问（返回 401）
![未认证访问返回 401](img_unauthorized_401.png)

**说明：**  
未携带 Token 访问受保护接口 `/api/users/students`，返回 401 Unauthorized。

**请求示例：**
```bash
curl http://localhost:8090/api/users/students
```

**返回示例：**
```json
{
  "timestamp": "2025-12-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid token"
}
```

### 3. 认证访问（返回 200）

**说明：**  
携带有效 Token 访问受保护接口，返回 200 OK 和用户信息

**请求示例：**
```bash
curl http://localhost:8090/api/users/students \ -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**返回示例：**
```json
[
  {
    "id": "1",
    "username": "student1",
    "role": "STUDENT"
  },
  {
    "id": "2",
    "username": "student2",
    "role": "STUDENT"
  }
]
```

### 4. 后端服务日志显示接收到的用户信息


**说明：**  
后端服务（如 `enrollment-service`）的 Controller 中打印日志，显示从请求头中获取到的用户信息。

**日志示例：**
```text
2025-12-12 10:30:15 INFO c.c.e.controller.EnrollmentController:
用户 admin (ID: 1) 发起选课请求
```


---

## 四、Gateway 启动与 Nacos 注册验证

### Gateway 启动日志
![Gateway 启动日志](img_gateway_startup.png)

**说明：**  
Gateway 成功启动于端口 8090，并注册到 Nacos 服务注册中心。

### Nacos 控制台服务列表
![Nacos 控制台 Gateway 注册](img_nacos_gateway.png)

**说明：**  
Nacos 控制台显示 `gateway-service` 实例已注册，健康状态为 UP。

---

## 五、遇到的问题与解决方案

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| Gateway 路由转发 404 | 未配置 `StripPrefix` 过滤器 | 在路由配置中添加 `StripPrefix=1` |
| JWT Token 验证失败 | 密钥长度不足或算法不匹配 | 确保密钥至少 256 位，使用 HS512 算法 |
| CORS 跨域问题 | Gateway 未配置跨域支持 | 在 `application.yml` 中添加全局 CORS 配置 |
| 请求头用户信息丢失 | 过滤器未正确传递请求头 | 在 Gateway 过滤器中显式添加用户信息请求头 |

---

## 六、总结与反思

1. **架构演进**：引入 API Gateway 后，系统实现了统一的认证入口和请求路由，架构更加清晰。

2. **安全提升**：基于 JWT 的统一认证机制替代了各服务独立认证，提高了安全性和维护性。

3. **测试覆盖**：通过登录、未认证、认证三种场景的测试，验证了 Gateway 和 JWT 过滤器的完整性。

4. **可扩展性**：Gateway 的路由和过滤器配置支持灵活扩展，便于后续添加限流、日志、监控等功能。

5. **改进空间**：
    - 可集成 Spring Security 进一步增强认证授权能力
    - 可添加 Token 刷新机制提升用户体验
    - 可配置更细粒度的路由规则和过滤器链