# Repository Guidelines

## 项目结构与模块划分

本仓库是 Java 21 + Spring Boot 的多模块 Maven 项目：

- `admin/`：管理后台，负责用户、分组、短链接管理与远程调用。
- `project/`：核心短链接服务，包含创建、更新、跳转、统计及分片相关逻辑。
- `gateway/`：网关入口与统一流量转发。
- 源码位于 `*/src/main/java`，配置位于 `*/src/main/resources`，测试位于 `*/src/test/java`。

## 构建、测试与运行

- `./mvnw clean compile`：编译全部模块。
- `./mvnw test`：运行全部测试。
- `./mvnw -pl project test`：仅测试 `project` 模块。
- `./mvnw -pl admin -am compile`：编译 `admin` 及其依赖模块。
- `./mvnw -pl project spring-boot:run`：本地启动核心短链接服务。
- `./mvnw -pl admin spring-boot:run`：本地启动后台服务。

提交前至少运行受影响模块的测试；`-DskipTests` 只用于临时编译检查。

## 编码规范

- 缩进 4 个空格，类名使用 `UpperCamelCase`，方法/字段使用 `lowerCamelCase`。
- 控制层保持轻量，业务逻辑放在 `service/impl`。
- 命名约定：`UserDO`、`UserReqDTO`、`UserRespDTO`。
- 依赖注入统一使用 `@RequiredArgsConstructor` + `final`。
- 优先使用枚举和错误码，避免魔法值；分片表禁止直接更新分片键，改用“插入新记录 + 逻辑删除旧记录”。

## 错误码与异常

- 错误码前缀：`A` 客户端错误，`B` 服务端错误，`C` 远程调用错误。
- `BaseErrorCode` 定义通用校验类错误，`XxxErrorCodeEnum` 定义模块业务错误。
- 异常体系：`ClientException`、`ServiceException`、`RemoteException`，统一由 `GlobalExceptionHandler` 处理。

## 测试要求

- 测试类放在对应模块的 `src/test/java` 下，命名如 `ShortLinkServiceImplTest`。
- 优先覆盖 service 层业务分支，其次补 controller 层请求/响应测试。
- 涉及分库分表、Redis、唯一索引或更新迁移逻辑时，需重点验证边界场景。

## 提交与合并请求

- 遵循 Conventional Commits：如 `feat(admin): ...`、`fix(dto): ...`、`feat(shortlink): ...`。
- 每次提交保持单一主题，避免混入无关改动。
- PR 应说明影响模块、测试结果、接口或数据库变更；涉及分片、缓存、索引时需明确说明风险。
