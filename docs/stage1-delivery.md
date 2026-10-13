# 阶段 1 交付记录

从仅有 README 的目录建立三模块后端。未创建 Git 仓库，未修改 .env、已有数据库密码或阿里云密钥；生产依赖限定在已确认计划中。

## 分任务记录

| 任务 | 主要文件 | 已执行验证 | 剩余风险 |
| --- | --- | --- | --- |
| T0 工程与契约 | 四个 pom.xml、SkyApplication、application.yml、Result/PageResult、Entity、GlobalExceptionHandler、OpenApiConfig、AdminInitializer、sql/sky_stage1.sql、README | 三模块编译；MySQL 8.4 表结构；初始化哈希/幂等；文档接口 | 系统默认 JDK 11，运行需指定 JDK 17；开发库先执行结构 SQL |
| T1 JWT 登录 | JwtUtil、BaseContext、JwtConfig/Properties、LoginDTO/VO、EmployeeController/Service/Mapper、JwtAuthenticationFilter | 正常/错误/禁用登录；签名/过期/无效 token；删除后拒绝；失败及异常请求身份清理 | 每次鉴权增加一次员工查询；外部密钥必填 |
| T2 自动填充 | AutoFill、OperationType、AutoFillAspect、AuditedEntity、Mapper 写方法/XML | 真实 Mapper 代理 INSERT/UPDATE；缺少身份拒绝；创建字段保留、状态审计 | 后续 Mapper 写方法需继续配套注解、Entity 及审计 SQL |
| T3 员工 CRUD | 员工 Controller/Service/Mapper/XML、Create/Update/PageQuery DTO、EmployeeVO、员工测试 | CRUD、筛选分页、重复用户名、无效参数、初始化管理员/自身保护、停用/删除后 token 失效 | 物理删除保留历史 ID；无角色权限 |
| T4 分类管理 | CategoryController/Service/Mapper/XML、分类 DTO/VO、分类测试 | CRUD、重名、type/sort 校验、默认禁用、筛选、排序、审计 | 菜品/套餐加入后补充引用删除保护 |
| T5 OSS 上传 | AliOssUtil、OssConfig/Properties、FileUploadService/Impl、CommonController、上传测试 | JPEG/PNG、伪装、MIME 不一致、空/超限图片、UUID 键、元数据、SDK 错误、未配置错误 | **无真实 OSS 配置，云上传及公开 URL 尚未验收** |
| T6 整体联调 | Stage1FlowIT、DatabaseTestSupport、测试配置/reset.sql、Failsafe、README | 真实 HTTP+MySQL 的员工→分类→上传（模拟云边界）→停用→拒绝旧 token；文档和错误返回 | **真实 OSS 仍为外部验收项** |

## 环境与修正

测试使用临时目录中的 JDK 17 与 MySQL 8.4.0；MySQL 仅监听 127.0.0.1 专用端口，开发和测试 schema 分离。独立测试凭证和 JWT 密钥只用于临时环境，不写入项目；未调整系统级 JAVA_HOME 或已有数据库。

Knife4j 4.5.0 增强模式调用 springdoc 2.8.17 已变更的 getGroupConfigs() API，实际 OpenAPI 请求产生 NoSuchMethodError。保持指定版本，只关闭增强模式，保留 UI/OpenAPI，并通过真实 HTTP 回归验证。

超限上传测试发现 Tomcat 默认吞吐限制提前关闭连接；将 max-swallow-size 与 6 MiB 请求限制对齐，保留 5 MiB 文件上限，以便客户端收到 JSON 错误。

只读审查发现的初始化全空白密码、客户端格式错误返回 500 已加回归测试。密码、token、配置记录的 toString 脱敏；SDK 错误不传播内部详情。

## 最终验证

2026-10-13（Asia/Shanghai）验证结果：

- `mvn clean compile`：BUILD SUCCESS。
- `mvn clean verify`：BUILD SUCCESS，51 个测试，0 失败、0 错误、0 跳过。
- Surefire：sky-common 8 个、sky-server 37 个；Failsafe：真实 HTTP+MySQL 的 Stage1FlowIT 6 个。
- 可运行 JAR：`sky-server/target/sky-server-1.0.0-SNAPSHOT.jar`。
- 打包 JAR 使用真实启动配置，在独立开发 schema 中成功初始化管理员、登录、查询员工；`/doc.html` 和 `/v3/api-docs` 均返回 200，定义包含 11 个路径。
- 仅为验证启动的应用与 MySQL 实例已停止；源码和构建产物保留在项目目录。

本次使用的便携 JDK 位于 `$env:TEMP/sky-stage1-runtime/jdk17/jdk-17.0.20.1+1`。可在当前 PowerShell 会话设置 JAVA_HOME 并将其 bin 加入 PATH；也可使用自行安装的 JDK 17。未修改系统环境设置。

真实 OSS：未验收。未修改云端策略或用模拟测试代替真实验收。
