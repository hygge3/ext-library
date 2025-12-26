/**
 * User-Agent 解析模块
 * <p>
 * 提供 User-Agent 字符串的解析功能，支持识别浏览器、操作系统、平台、渲染引擎等信息。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>浏览器识别与版本解析</li>
 *   <li>操作系统识别与版本解析</li>
 *   <li>设备平台识别（移动/桌面/iOS/Android）</li>
 *   <li>渲染引擎识别与版本解析</li>
 *   <li>移动设备判断</li>
 * </ul>
 *
 * <h2>类结构</h2>
 * <ul>
 *   <li>{@link UserAgentUtil} - 工具入口，提供便捷的解析方法</li>
 *   <li>{@link UserAgentParser} - 解析器，协调各组件完成解析</li>
 *   <li>{@link UserAgent} - 解析结果数据模型</li>
 *   <li>{@link UserAgentInfo} - 信息基类，提供模式匹配基础功能</li>
 *   <li>{@link Browser} - 浏览器信息组件</li>
 *   <li>{@link OS} - 操作系统信息组件</li>
 *   <li>{@link Platform} - 平台信息组件</li>
 *   <li>{@link Engine} - 渲染引擎信息组件</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 解析 User-Agent
 * String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)...";
 * UserAgent ua = UserAgentUtil.parse(userAgent);
 *
 * // 获取解析结果
 * if (ua != null) {
 *     ua.getBrowser().getName();      // 浏览器名称
 *     ua.getVersion();                // 浏览器版本
 *     ua.getOs().getName();           // 操作系统
 *     ua.getOsVersion();              // 系统版本
 *     ua.getPlatform().getName();     // 平台类型
 *     ua.isMobile();                  // 是否为移动设备
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
package ext.library.http.useragent;
