---
id: ext-captcha
type: module-design
title: ext-captcha — 图形验证码
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-cache]
---

# ext-captcha — 图形验证码

**职责**：验证码的生成与校验：`CaptchaDraw` 绘制接口及实现（随机字符 `RandomCaptchaDraw`、数学表达式 `MathCaptchaDraw`、背景 `BackgroundDraw`/`SmallCharBackgroundDraw`、干扰线 `InterferenceDraw`、曲线干扰 `CubicCurveInterferenceDraw`）、`Captcha`/`DefaultCaptcha`/`CaptchaUtil`、`CaptchaService`/`DefaultCaptchaService`/`CaptchaWebService`、`CaptchaResponse` 响应模型、`CaptchaProperties`。

**存储**：答案经 `CaptchaCache` 存进 [ext-cache](../../ext-infra/ext-cache/SPEC.md)，因此自动继承缓存后端选择——单机用内存、多实例必须用 Redis / PostgreSQL，否则跨实例校验失败。

**边界**：依赖 `ext-cache` 承担存储；不实现登录流程与风控规则，只负责"生成一张图 + 校验一次答案"。
