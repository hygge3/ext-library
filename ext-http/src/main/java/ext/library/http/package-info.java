/**
 * ext-http 模块提供基于 JDK 11+ HttpClient 的 HTTP 客户端工具类。
 * <p>
 * 本模块提供流畅的链式调用 API（Fluent API）。
 * </p>
 *
 * <h2>主要特性</h2>
 * <ul>
 *   <li>支持 GET、POST、PUT、DELETE、PATCH 等 HTTP 方法</li>
 *   <li>流畅的链式调用 API</li>
 *   <li>同步和异步请求支持</li>
 *   <li>请求头和查询参数设置</li>
 *   <li>多种请求体格式支持（JSON、表单、文件等）</li>
 *   <li>响应数据便捷转换</li>
 *   <li>超时配置</li>
 *   <li>文件下载支持</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <h3>链式调用（推荐）</h3>
 * <pre>{@code
 * // 同步 GET 请求
 * String result = HttpUtil.get("https://api.example.com/data")
 *     .header("Authorization", "Bearer token")
 *     .query("page", "1")
 *     .query("size", "10")
 *     .execute()
 *     .asString();
 *
 * // 同步 POST 请求
 * String result = HttpUtil.post("https://api.example.com/users")
 *     .contentTypeJson()
 *     .body("{\"name\":\"test\"}")
 *     .execute()
 *     .asString();
 *
 * // 使用表单数据
 * String result = HttpUtil.post("https://api.example.com/login")
 *     .form("username", "admin")
 *     .form("password", "123456")
 *     .execute()
 *     .asString();
 *
 * // 异步请求
 * HttpUtil.post("https://api.example.com/users")
 *     .contentTypeJson()
 *     .body("{\"name\":\"test\"}")
 *     .executeAsync()
 *     .thenAccept(response -> System.out.println(response.asString()));
 *
 * // 下载文件
 * Path filePath = HttpUtil.get("https://example.com/file.pdf")
 *     .execute()
 *     .download("/path/to/save/file.pdf");
 * }</pre>
 *
 * <h2>主要类</h2>
 * <ul>
 *   <li>{@link HttpUtil} - 主工具类，提供静态方法和链式调用入口</li>
 *   <li>{@link HttpUtil.Request} - 链式请求构建器</li>
 *   <li>{@link HttpUtil.Response} - 响应包装类</li>
 *   <li>{@link HttpUtil.HttpMethod} - HTTP 方法枚举</li>
 * </ul>
 *
 * @since 1.0.0
 */
package ext.library.http;
