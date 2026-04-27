# Repository Guidelines

## 项目结构与模块划分

本仓库是 Java 21 + Spring Boot 3.5.12 的多模块 Maven 项目：

- `admin/`：管理后台（用户、分组、短链接管理），**依赖 `project` 模块**。
- `project/`：核心短链接服务（创建、更新、跳转、统计、分片逻辑）。
- `gateway/`：网关入口，当前仅有骨架（无 Java 源码，仅 `application.yaml` 设置端口 8000）。
- 源码位于 `*/src/main/java`，配置位于 `*/src/main/resources`，测试位于 `*/src/test/java`。

## 构建、测试与运行

- `./mvnw clean compile`：编译全部模块。
- `./mvnw test`：运行全部测试。
- `./mvnw -pl project test`：仅测试 `project` 模块。
- `./mvnw -pl admin -am test`：测试 `admin` 及其依赖（`-am` 自动包含 `project`）。
- `./mvnw -pl project spring-boot:run`：本地启动核心短链接服务（端口 8001）。
- `./mvnw -pl admin spring-boot:run`：本地启动后台服务（端口 8002）。

提交前至少运行受影响模块的测试；`-DskipTests` 只用于临时编译检查。

## 模块依赖关系（关键）

`admin` 模块通过 Maven 依赖 `project` 模块（`compile` scope）。编译/测试 `admin` 时必须使用 `-am` 参数包含依赖模块，否则会失败。

## 代码重复模式（注意）

`common/convention/` 包（含 `errorcode/`、`exception/`、`result/`）在 `admin/` 和 `project/` 模块中**各有一份完整副本**，未抽取为共享模块。修改此类代码时需同步两份。

## 编码规范

- 缩进 4 个空格，`UpperCamelCase` 类名，`lowerCamelCase` 方法/字段。
- 控制层保持轻量，业务逻辑放在 `service/impl`。
- 命名约定：`UserDO`、`UserReqDTO`、`UserRespDTO`（DO/ReqDTO/RespDTO）。
- 依赖注入统一使用 `@RequiredArgsConstructor` + `final`。
- DO 转 DTO 前必须判空，否则 `BeanUtils.copyProperties` 抛异常。
- 分片表禁止直接更新分片键，改用“插入新记录 + 逻辑删除旧记录”。

## 错误码与异常

### 错误码前缀
| 前缀 | 分类 | 示例 |
|------|------|------|
| `A` | 客户端错误 | `A000001` |
| `B` | 服务端错误 | `B000001` |
| `C` | 远程调用错误 | `C000001` |

### 错误码文件
- `BaseErrorCode`（通用校验类）：位于各模块 `common/convention/errorcode/`
- `UserErrorCodeEnum`（admin 模块业务错误）：`admin/common/enums/`
- `ProjectErrorCodeEnum`（project 模块业务错误）：`project/common/convention/errorcode/`

### 已注册的实际错误码（仅供参考，写代码时以源码为准）
```
BaseErrorCode:        CLIENT_ERROR(A000001), USER_REGISTER_ERROR(A000100),
                      USER_NAME_EXIST_ERROR(A000111), SERVICE_ERROR(B000001),
                      REMOTE_ERROR(C000001) 等
UserErrorCodeEnum:    USER_TOKEN_FAIL(A000200), USER_NULL(B000200),
                      USER_NAME_EXIST(B000201), USER_EXISTS(B000202),
                      USER_SAVE_ERROR(B000203)
ProjectErrorCodeEnum: LINK_EXIST(A000300), LINK_NOT_FOUND(A000301),
                      LINK_GENERATE_TOO_MANY(B000300) 等
```

### 异常体系
```
AbstractException
├── ClientException   # 客户端异常
├── ServiceException  # 服务端异常
└── RemoteException   # 远程调用异常
```
由 `GlobalExceptionHandler`（`@RestControllerAdvice`）统一拦截处理：
`MethodArgumentNotValidException` → `AbstractException` → `Throwable`.

### 统一返回结构
```java
Result<T> { code, message, data, requestId }
// code "0" = 成功
Results.success(data);                    // 成功
Results.failure(errorCodeEnum);          // 从错误码枚举构建
Results.failure(new ClientException(...)); // 从异常构建
```

## 配置注意事项

### ShardingSphere
- 配置文件：`*/src/main/resources/shardingsphere-config.yaml`
- Maven resource filtering 对 `shardingsphere-config.yaml` 启用（`filtering=true`），可通过 Maven profile 注入数据库密码等变量。
- admin 分片表：`t_user`（按 `username`）、`t_group`（按 `username`）。
- project 分片表：`t_link`（按 `gid`）、`t_link_goto`（按 `full_short_url`）。
- 所有分片表均为 16 片，算法 `HASH_MOD`，数据源 `jdbc:mysql://127.0.0.1:3306/link`。

### 数据库初始化
DDL 脚本位于 `resources/database/link.sql`（项目根目录，非模块内）。

### POM Profile
- `dev`（默认激活）：`db.password=root`
- `prod`：`db.password=${env.DB_PASSWORD}`

## 测试

- 当前测试覆盖极少：仅 `project` 和 `gateway` 各有一个骨架 `contextLoads()` 测试，`admin` 无测试。
- `.gitignore` 已忽略 `/admin/src/test/` 和 `/gateway/src/test/` 目录。
- 涉及分库分表、Redis、唯一索引或分片键迁移逻辑时，需重点验证边界场景。

## 提交与合并请求

- 遵循 Conventional Commits：如 `feat(admin): ...`、`fix(dto): ...`、`feat(shortlink): ...`。
- 每次提交保持单一主题，避免混入无关改动。
- PR 应说明影响模块、测试结果、接口或数据库变更；涉及分片、缓存、索引时需明确说明风险。
