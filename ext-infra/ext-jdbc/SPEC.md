---
id: ext-jdbc
type: module-design
title: ext-jdbc — JdbcClient 轻量 DAO
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-servlet]
---

# ext-jdbc — JdbcClient 轻量 DAO

**职责**：在 Spring `JdbcClient` 之上补一层注解驱动的轻量 DAO，**不引入 ORM**：`@Table` / `@Id` 标注实体，`JdbcDaoFactory#of(Class)` 产出 `JdbcDao<T>`，`EntityMetaData` + `EntityRowMapper` 反射解析表名、主键与列名（驼峰↔下划线），`QueryWrapper<T>` 链式条件（eq/ne/lt/le/gt/ge/in/notIn/between/isNull/like/groupBy/having/orderBy/limit/last），`PageQuery` / `PageResult<T>` 分页——**这两个类型属于 [ext-servlet](../../ext-common/ext-servlet/SPEC.md) 而非本模块**，`selectPage` 直接用基座那一套，全库只有一份定义。

**决策（名字转换与反查）**：列名↔属性名的互转全部复用 `ext-tool` 的 `StringUtil.underlineToCamelCase` / `camelToUnderline`，`EntityMetaData` 只做委托，不再自己写一遍字符循环——下划线风格全库只有一处定义，改风格只改一处。反查（拿到数据库返回的列名找 setter）不靠每次反射：`EntityRowMapper` 构造时用 JavaBeans 内省（`Introspector`）一次性建好「小写属性名 → setter + 属性类型」的不可变 `Map`，回填时只做一次查表加类型转换。

**不变量**：

- 所有值以 `?` 占位符绑定，绝不拼接进 SQL 文本。
- 无条件的 `update` / `delete` 直接拒绝执行（抛 `ExtException`）——必须带 `QueryWrapper` 或主键。这是防误删全表的硬约束，不要为了"方便"放开。

**边界**：`starter-jdbc` 为 `compileOnly`（由消费方的数据源提供）；不管理事务、不做 DDL 迁移。
