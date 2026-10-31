---
id: ext-bom
type: module-design
title: ext-bom — 版本约束发布单元
status: draft
parent: ext-library-architecture
---

# ext-bom — 版本约束发布单元

**职责**：唯一的版本约束来源（`java-platform`）。导入 `spring-boot-dependencies` 覆盖 Spring 全家桶，导入 `software.amazon.awssdk:bom` 覆盖 [ext-oss](../ext-infra/ext-oss/SPEC.md) 的 S3 客户端，逐个约束不在任何 BOM 的第三方（bouncycastle、zxing、swagger-annotations、springdoc、sshd），并把 26 个 ext 模块的版本一并约束。消费方只需 `platform('ext.library:ext-bom:<version>')`，再按需引模块、不写版本号。

**优先复用官方 BOM**：第三方自己带 BOM（如 `software.amazon.awssdk:bom`）就 `platform(...)` 导入，不要把它的几十个 artifact 拆成 constraint——逐个约束跟不上官方拆包节奏，还会诱使模块混引不同版本的 service client。

**边界**：不含任何代码，不套 `java-library`；不是聚合 starter——导 BOM 不等于引入模块（见 [ext-library-goal](../goal-and-requirements.md) 的非目标）。

**不变量**：

- 第三方版本值只出现在 `gradle.properties`，约束只出现在这里。新增第三方依赖必须先加 `gradle.properties` 键，再加 constraint，否则版本无处收敛。
- 新增模块必须同时登记在 `settings.gradle` 与这里的 constraints，否则不进依赖管理。
- 所有叶子通过根 `build.gradle` 的 `api platform(project(':ext-bom'))` 继承约束；模块 `build.gradle` 不得自写版本号。

**分发**：构建侧只配置了 `publishToMavenLocal`。
