/**
 * ext-api-crypto - API 加解密模块
 * <p>
 * 提供 HTTP 请求参数自动解密和响应数据自动加密功能，支持多种加密算法：
 * <ul>
 *   <li>RSA - 非对称加密</li>
 *   <li>SM2 - 国密非对称加密</li>
 *   <li>AES - 对称加密</li>
 *   <li>SM4 - 国密对称加密</li>
 *   <li>Base64 - 编码（非加密）</li>
 * </ul>
 *
 * <h2>快速开始</h2>
 * <pre>{@code
 * // 配置密钥
 * ext:
 *   api-crypto:
 *     algorithm: RSA
 *     public-key: "..."
 *     private-key: "..."
 *
 * // 使用注解
 * @PostMapping("/login")
 * @RequestDecrypt
 * @ResponseEncrypt
 * public R<LoginVO> login(@RequestBody LoginDTO dto) {
 *     return R.ok(authService.login(dto));
 * }
 * }</pre>
 *
 * @since 4.0.0
 * @see ext.library.apicrypto.annotation.RequestDecrypt
 * @see ext.library.apicrypto.annotation.ResponseEncrypt
 */
@NullMarked
package ext.library.apicrypto;

import org.jspecify.annotations.NullMarked;
