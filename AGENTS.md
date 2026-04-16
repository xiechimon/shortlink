# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Spring Boot multi-module Maven project.

- `admin/`: management APIs, user/group administration, and remote calls into short-link services.
- `project/`: core short-link APIs, persistence logic, sharding-aware link operations, and DTOs.
- `gateway/`: gateway module for request routing and cross-cutting entry concerns.
- Source code lives under `*/src/main/java`; configuration lives under `*/src/main/resources`.
- Tests belong under `*/src/test/java`, mirroring the package structure of production code.
- Root `pom.xml` manages shared versions and modules; each module has its own `pom.xml`.

## Build, Test, and Development Commands

- `./mvnw clean compile`: compile all modules.
- `./mvnw test`: run all module tests.
- `./mvnw -pl project test`: run tests for one module.
- `./mvnw -pl admin -am compile`: compile `admin` and required upstream modules.
- `./mvnw -pl project spring-boot:run`: run the core short-link service locally.
- `./mvnw -pl admin spring-boot:run`: run the admin service locally.

Use `-DskipTests` only for quick local compilation checks, not before final verification.

## Coding Style & Naming Conventions

- Use 4-space indentation and standard Java naming: `UpperCamelCase` classes, `lowerCamelCase` methods/fields.
- Keep controllers thin; put business logic in `service/impl`.
- DTOs belong in `dto/req`, `dto/resp`, or `remote/dto`.
- Prefer constructor injection with Lombok `@RequiredArgsConstructor` and `final` dependencies.
- Use project enums for business constants instead of magic numbers, for example valid-date types and error codes.
- For sharded tables, do not update sharding keys directly; use insert-plus-logical-delete migration patterns.

## Testing Guidelines

- Add tests under the matching module’s `src/test/java`.
- Name unit tests with clear behavior, e.g. `ShortLinkServiceImplTest`.
- Prefer focused service-level tests for business rules and controller tests for request/response wiring.
- Run at least the affected module tests before committing: `./mvnw -pl project test` or `./mvnw -pl admin test`.

## Commit & Pull Request Guidelines

- Follow Conventional Commits as used in history: `feat(admin): ...`, `fix(dto): ...`, `feat(shortlink): ...`.
- Keep commits focused on one logical change.
- PRs should include a concise summary, affected modules, test results, and any API request/response changes.
- Mention sharding, Redis, or database migration implications when relevant.

## Security & Configuration Tips

- Do not commit real credentials. Use Maven profiles and environment variables for production secrets.
- Treat Redis, database, and sharding configuration changes as high-impact; document local verification steps.
