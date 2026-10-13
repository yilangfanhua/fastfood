# 苍穹外卖：阶段 1 管理端基础

三模块后端，包含员工 JWT 登录、员工 CRUD、分类管理、OSS 图片上传和公共字段自动填充。

## 工程结构与约定

- `sky-common/`：Result、PageResult、异常、JWT、身份上下文、审计注解、OSS 工具。
- `sky-pojo/`：Entity、校验 DTO、无密码的 VO。
- `sky-server/`：Controller → Service → Mapper、过滤器、配置、审计切面。
- `sql/sky_stage1.sql`：员工及分类表结构，不包含初始密码。
- `sky-server/src/test/`：单元、真实 MySQL 和真实 HTTP 测试。
- `docs/stage1-delivery.md`：分任务交付及验收记录。

严格遵循 Controller → Service → Mapper 分层。上传通过 Service 调用 OSS，无数据库写入。业务接口统一返回 Result<T>；新增接口必须配 Knife4j/OpenAPI 3 的 @Tag、@Operation。金额使用 BigDecimal，禁止 double。

保留工程红线：不改 `.env`、已有数据库密码或阿里云密钥；不新增未指定的生产依赖；不重构无关代码；不跳过或删除失败测试。每项交付说明修改文件、验证结果及剩余风险。

## 环境与启动

Java **17**、Maven 3.6.3+、MySQL **8**。Spring Boot 3.5.16、MyBatis Starter 3.0.5、Knife4j Jakarta Starter 4.5.0、springdoc 2.8.17、JJWT 0.13.0、OSS SDK 3.18.5；其余版本由 Boot BOM 管理。使用 jakarta.*。

Knife4j 保留文档 UI，`knife4j.enable=false` 关闭增强模式：4.5.0 增强调用了已变更的 springdoc API，不能与指定的 2.8.17 一起开启。`/doc.html`、OpenAPI 定义仍可使用。

通过系统环境或启动器提供配置，真实值不写入项目：

| 环境变量 | 要求 / 默认行为 |
| --- | --- |
| DB_URL | 必填，例如 jdbc:mysql://127.0.0.1:3306/sky_stage1?serverTimezone=Asia/Shanghai |
| DB_USERNAME、DB_PASSWORD | 必填，使用已有连接凭证；空密码需显式设为空 |
| JWT_SECRET | 必填，Base64 编码的 **32 个随机字节**，无兜底密钥 |
| ADMIN_BOOTSTRAP_ENABLED | 默认 false；首次初始化设为 true |
| ADMIN_INITIAL_PASSWORD | 首次初始化必填，非空白，至少 8 字符、最多 72 个 UTF-8 字节 |
| SERVER_PORT | 默认 8080 |
| API_DOCS_ENABLED | 默认 true，false 关闭文档访问 |
| OSS_ENABLED | 默认 false；未配置时上传明确返回 502 |
| OSS_ENDPOINT、OSS_BUCKET | 启用 OSS 时必填 |
| OSS_ACCESS_KEY_ID、OSS_ACCESS_KEY_SECRET | 启用 OSS 时必填，只引用外部凭证 |
| OSS_PUBLIC_BASE_URL | 启用时必填，公开 HTTPS 地址，无凭证、查询串或片段，例如 https://images.example.com |

在专用开发数据库中执行结构 SQL；脚本不创建数据库、不修改账号或密码。例如进入 MySQL 客户端，选中已准备的数据库：

```sql
USE sky_stage1;
SOURCE E:/WorkPlace/Agent/project_2/sql/sky_stage1.sql;
```

首次启动开启初始化，用外部初始密码建立 admin，数据库只存 BCrypt 哈希。已有初始化管理员时不重置密码。初始化操作人 ID 为 0，业务写入必须有登录员工身份；初始化后关闭开关并移除进程中的初始密码变量。

```powershell
java -version
mvn -version
mvn clean compile
# 设置测试变量后，verify 执行全部测试并生成可运行 JAR。
mvn clean verify
java -jar sky-server/target/sky-server-1.0.0-SNAPSHOT.jar
```

也可在提供测试变量后执行 `mvn install`，再使用原启动命令 `mvn spring-boot:run -pl sky-server`。多模块依赖需先安装到本地仓库，不在聚合模块执行 spring-boot:run -am。

文档：http://localhost:8080/doc.html；定义：http://localhost:8080/v3/api-docs。

## 接口契约

登录提交 JSON username、password，返回 id、username、name、token。后续请求使用 `Authorization: Bearer <token>`；HS256 token 默认有效期 2 小时。每次请求检查员工仍存在且启用，停用或删除后的旧 token 立即失效。请求结束清理身份上下文。

成功：`{"code":1,"msg":null,"data":...}`；失败：`{"code":0,"msg":"错误说明","data":null}`。HTTP 400 参数/格式错误，401 认证失败，403 禁用账户/受保护操作，404 不存在，405 方法不支持，409 唯一值冲突，502 OSS 不可用；数据库及未知内部错误返回 500，不暴露内部详情。

| 方法 | 路径 | 请求 / 结果 |
| --- | --- | --- |
| POST | /admin/employee/login | 匿名登录，返回 token |
| POST | /admin/employee | username、name、password、phone；可选 sex、idNumber；返回 ID |
| GET | /admin/employee/page | page、pageSize、可选 name；返回 total、records |
| GET | /admin/employee/{id} | 详情，不返回密码或哈希 |
| PUT | /admin/employee | id、username、name、phone；可选 sex、idNumber；不修改密码 |
| DELETE | /admin/employee/{id} | 物理删除 |
| POST | /admin/employee/status/{status}?id=... | 0 禁用、1 启用 |
| POST | /admin/category | type、name、sort；返回 ID |
| GET | /admin/category/page | page、pageSize、可选 name、type；返回 total、records |
| GET | /admin/category/list | 可选 type，只返回启用分类，按 sort、id 升序 |
| GET | /admin/category/{id} | 详情 |
| PUT | /admin/category | id、type、name、sort |
| DELETE | /admin/category/{id} | 物理删除 |
| POST | /admin/category/status/{status}?id=... | 0 禁用、1 启用 |
| POST | /admin/common/upload | multipart 字段 file，返回公开 HTTPS URL |

分页默认第 1 页、每页 10 条，最大 100；非法及溢出参数拒绝。员工分页按 ID 降序；名称筛选为包含关系。

员工 username 为 3–32 位字母、数字或下划线且唯一；name 必填，最多 64 字符；phone 为 11 位中国大陆手机格式。新增默认启用，初始密码至少 8 字符、最多 72 个 UTF-8 字节。sex 可为 0/1，idNumber 可为 18 位格式。默认数据库排序规则下用户名唯一约束不区分大小写。admin 为保留用户名；初始化管理员用户名不可修改，初始化管理员和当前账户不能删除或停用。

分类 type 为 1=菜品、2=套餐；name 全局唯一；sort 非负；新增默认禁用。阶段 1 无菜品/套餐引用，后续加入关联实体时必须补充删除保护。

INSERT 自动填充 createTime、updateTime、createUser、updateUser；UPDATE 和状态切换只更新后两个修改字段，创建字段保留。客户端审计字段不进入写入 Entity。审计操作人不设员工外键，保留删除员工后的历史 ID。

所有启用员工可使用管理接口。本阶段不增加角色权限、刷新 token、密码重置、服务端登出、Redis 或前端。

## 上传与测试

上传接受实际可解码且 MIME 一致的 JPEG/PNG；最大 **5 MiB**、最多 2000 万像素，拒绝空文件、伪装格式和超限图片。对象键采用上海日期目录+UUID，忽略原文件名。OSS 客户端复用，关闭应用时释放；Bucket 公开读取策略及域名由外部提供，项目不调整云端策略。

测试必须使用独立 MySQL 数据库，名称以 `_test` 结尾。测试会创建所需表并清理该库中的员工、分类数据；不符合后缀要求时拒绝清理。不要复用业务数据库。

| 测试变量 | 要求 |
| --- | --- |
| TEST_DB_URL | 独立 *_test MySQL 数据库连接串 |
| TEST_DB_USERNAME、TEST_DB_PASSWORD | 测试库外部凭证 |
| TEST_JWT_SECRET | Base64 编码的 32 个随机字节 |

```powershell
mvn clean compile
mvn clean verify
```

verify 不跳过测试；Failsafe 的 Stage1FlowIT 用真实 HTTP+MySQL 联调，只模拟 OSS 云边界。未提供测试环境时验证失败，不自动跳过。

真实 OSS 验收需提供外部配置，启动真实应用后登录、上传图片，确认对象存在且返回 URL 可匿名 HTTPS 访问。模拟测试不代替此验收；状态见 docs/stage1-delivery.md。
