# 星云智联科技有限公司 SpringBoot 开发规范

| 版本 | 适用 |
| --- | --- |
| V1.0 | 研发中心 · Spring Boot 后端开发 |

---

## 一、分层与职责

| 层 | 说明 | 命名（例） |
| --- | --- | --- |
| Controller | 接收请求、参数校验、调用 Service、返回结果 | `UserController` |
| Service | 业务逻辑、事务控制 | `UserService` / `UserServiceImpl` |
| DAO/Mapper | 数据访问操作 | `UserMapper` |
| Entity | 数据库实体 | `UserDO` |
| DTO/VO | 传输对象 / 视图对象 | `UserDTO`、`UserVO` |

### 职责原则
- Controller 不做复杂业务，只做编排与转发。
- Service 之间通过接口调用，避免循环依赖。
- 禁止在 Entity 上暴露过多不必要字段到前端。

---

## 二、Controller 规范

- **Controller 命名**：`XxxController`，不重复加 Controller 的业务动词（如 `UserController#getUser`）。
- 提供 REST 风格接口，路径见《06-04 REST接口规范》。
- 入参校验使用 `@Valid` + 注解，如 `@NotBlank`、`@NotNull`。

---

## 三、Service 规范

- 事务：写操作标注 `@Transactional`，异构数据源注意边界。
- 业务校验失败抛业务异常，由全局异常处理捕获。

---

## 四、DTO / VO

| 对象 | 用途 |
| --- | --- |
| DTO | 入参/出参传输，避免与 Entity 直接耦合 |
| VO | 前端展示视图对象，按页面需要裁剪字段 |

- 敏感字段（密码等）不出现在 VO/DTO 返回给前端。

---

## 五、异常处理

- 定义统一**业务异常** `BusinessException` 与错误码枚举。
- 实现**全局异常处理器** `@RestControllerAdvice`：
  - `BusinessException` → 返回业务错误码与提示。
  - 参数校验异常 → 返回字段错误信息。
  - 兜底 Exception → 返回统一错误并记录日志。

---

## 六、统一返回

统一返回结构（Result 封装）：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 字段 | 含义 |
| --- | --- |
| code | 业务状态码（200 成功，非 200 失败） |
| message | 提示信息 |
| data | 业务数据负载 |

- 所有接口返回统一结构，异常也走统一错误体（见《06-04 REST接口规范》）。

---

## 七、依赖注入

- 构造器或字段注入统一风格（推荐构造器注入）。
- 禁止 `new` 装配 Bean，统一由 Spring 容器管理。
- 使用 `@ConfigurationProperties` 管理配置项。

---

## 相关文档
- 《06-01 Java编码规范》《06-04 REST接口规范》