# ShortLink 项目说明

> 本文件主要提供 Claude Code 使用的项目背景与开发约定，通用贡献规范以 `AGENTS.md` 为准。

## 项目概述

SaaS 短链接系统，将长 URL 转化为短链接，支持跳转、统计分析、多租户隔离。

## 模块结构

```
shortlink/
├── admin/      # 管理后台（当前开发模块）
├── gateway/    # 网关服务
└── project/    # 核心业务服务
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 / 框架 | Java 21、Spring Boot 3.x、Spring Cloud |
| 网关 | Spring Cloud Gateway、Nginx |
| 注册 / 配置中心 | Nacos（集群） |
| 熔断 / 限流 | Sentinel |
| 缓存 | Redis 集群（读写分离） |
| 数据库 | MySQL（读写分离） |
| 消息队列 | Redis Stream |
| ORM | MyBatis-Plus |

## 命名规范

| 层级 | 命名示例 | 说明 |
|------|---------|------|
| 数据库实体 | `UserDO` | DO = Data Object |
| 响应DTO | `UserRespDTO` | Resp = Response |
| 请求DTO | `UserReqDTO` | Req = Request |

## 错误码规范

### 架构
- `IErrorCode` — 错误码接口
- `BaseErrorCode` — 平台级错误码（校验类）
- `XxxErrorCodeEnum` — 模块级错误码（业务类）

### 错误码前缀
| 前缀 | 分类 |
|------|------|
| A0xxxx | 客户端错误 |
| B0xxxx | 服务端错误 |
| C0xxxx | 远程调用错误 |

### 使用原则
- `BaseErrorCode` 定义校验类错误（注册、用户名格式、密码格式等）
- `XxxErrorCodeEnum` 定义业务状态类错误（用户不存在、用户被禁用等）
- 两者错误码不能重复

### 已有错误码
```java
// BaseErrorCode
CLIENT_ERROR("A000001", "用户端错误")
USER_REGISTER_ERROR("A000100", "用户注册错误")
USER_NAME_EXIST_ERROR("A000111", "用户名已存在")

// UserErrorCodeEnum
USER_NOT_FOUND("A000002", "用户不存在")
USER_DISABLED("A000005", "用户已被禁用")
```

## 异常规范

```
AbstractException
├── ClientException   # 客户端异常
├── ServiceException  # 服务端异常
└── RemoteException   # 远程调用异常
```

```java
throw new ServiceException(UserErrorCodeEnum.USER_NOT_FOUND);
```

## 统一返回结构

```java
Result<T> { code, message, data, requestId }
// code "0" = 成功

Results.success(data);                    // 成功
Results.failure(errorCode, msg);         // 失败
Results.failure(abstractException);       // 从异常构建
```

## 全局异常处理器

`GlobalExceptionHandler` 统一处理：
1. `MethodArgumentNotValidException` — 参数校验
2. `AbstractException` — 业务异常
3. `Throwable` — 兜底

## 开发规范

- 依赖注入：`@RequiredArgsConstructor` + `final`
- DO 转 DTO 必须判空，否则 `BeanUtils.copyProperties` 抛异常
- 版本：Spring Boot 3.x、MyBatis-Plus 3.5.12、mybatis-spring 3.0.4

## 常用命令

```bash
mvn clean package -DskipTests          # 编译
java -jar admin/target/admin-*.jar     # 启动 admin
```
