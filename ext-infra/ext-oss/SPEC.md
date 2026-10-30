---
id: ext-oss
type: module-design
title: ext-oss — 对象存储门面
status: draft
parent: ext-library-architecture
depends-on: [ext-core]
---

# ext-oss — 对象存储门面

**职责**：`OssTemplate` 作为唯一门面（`put` / `putBytes` / `putFile` / `copy` / `move` / `get` / `getString` / `getBytes` / `stat` / `delete` / `exists` / `list` / `presign` / `presignPut` / `generateKey`），背后是可替换的 `ObjectStorage` SPI，三份实现：`LocalObjectStorage`（写本地目录，可配对外访问前缀）、`S3ObjectStorage`（AWS SDK for Java v2，分片上传 + 预签名链接 + 服务端复制）与 `SftpObjectStorage`（SSH/SFTP 远端目录）。返回模型 `OssObject`（`Closeable`，含 bucket/key/contentType/size/流）、`ObjectMeta`（`stat` 结果：size/contentType/lastModified，不含内容）与 `ObjectItem`（列表项）。

**装配约束**：按 `ext.oss.type` 选 `local` / `s3` / `sftp`。各实现的厂商类型（AWS SDK 的 `S3Client` / `S3Presigner`、SFTP 客户端）只出现在自动配置类的方法体内部，且各自是 `compileOnly`，因此**容器里没有对应 SDK 时其他模式仍能启动**——新增存储实现必须沿用这个约束，不能把厂商类型泄进门面或 SPI 签名。

**S3 模式**：`ext.oss.type=s3`，配置 `ext.oss.*` 的 `endpoint` / `access-key` / `secret-key` / `region` / `bucket` / `auto-create-bucket` / `part-size`（默认 10MB）/ `presign-expiry`，外加两个 S3 兼容性开关：`path-style-access`（默认 `true`——MinIO、Ceph、R2 这类自建端点几乎都要求 path-style，AWS 官方端点要关掉）与 `chunked-encoding`（默认 `true`，个别旧网关不认 `aws-chunked` 时要关）。

**复制语义**：`copy(sourceKey, targetKey)` 与跨桶重载 `copy(sourceBucket, sourceKey, targetBucket, targetKey)` 走服务端复制（`CopyObject`），数据不经过应用进程；`move` 是 `copy` + `delete` 的组合，因为 S3 协议里没有原子 rename。本地实现用 `Files.copy` / `Files.move`，SFTP 实现优先用 `cp` 扩展、不支持时退化为流式复制。跨**实现**复制（如 local → s3）不做：那是数据迁移工具的事，门面不为它引入双客户端装配。

**SFTP 模式**：`ext.oss.type=sftp`，配置 `ext.oss.sftp.*`——`host` / `port`（默认 `22`）/ `username` / `password` 或 `private-key`（私钥路径）/ `base-path`（根目录）/ `access-prefix`（对外访问前缀，有则 `presign` 返回拼接直链）。基于 Apache MINA SSHD（`sshd-core`，`compileOnly`），连接池复用会话、上传走临时文件 + `rename` 保证不出现半写对象；`presign` / `presignPut` 在无 `access-prefix` 时抛 `UnsupportedOperationException`（SFTP 本身没有预签名，直传也无从谈起）。

**边界**：不实现 CDN 回源、图片处理、配额管理，也不做跨实现复制；对象生命周期（过期清理）由消费方负责。
