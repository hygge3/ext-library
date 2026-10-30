---
id: ext-i18n
type: module-design
title: ext-i18n — 双源国际化
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json]
---

# ext-i18n — 双源国际化

**职责**：把翻译文案拆成多个来源，查不到就按 `order()` 回退到下一个：`I18nMessageManager`（`get` / `find` 返回 `Optional` / `contains` / `clear`，支持占位符参数）、`TranslationSource` SPI、`ResourceBundleTranslationSource`（classpath 资源文件）、`JdbcTranslationSource`（翻译表，语言降级链 `zh-CN` → `zh` → `ROOT`，按降级顺序合并）。

**缓存**：按语言缓存翻译表，`ext.i18n.cache-ttl`（默认 5m）到期重载——这是翻译改库后自动生效的机制，不要改成只在启动时加载。

**边界**：`starter-jdbc` 为 `compileOnly`，容器里没有 `JdbcClient` 时数据库源静默关闭（`jdbc-enabled` 自动失效），只有资源文件源生效。表结构 `ext_i18n_message(id, message_key, locale, message)`，`(message_key, locale)` 唯一，可由 `schema-init` 自动建表。
