/**
 * API 加解密策略实现
 * <p>
 * 提供各种加密算法的策略实现，包括：
 * <ul>
 *   <li>{@link ext.library.apicrypto.strategy.AESStrategy} - AES 对称加密</li>
 *   <li>{@link ext.library.apicrypto.strategy.RSAStrategy} - RSA 非对称加密</li>
 *   <li>{@link ext.library.apicrypto.strategy.SM2Strategy} - SM2 国密非对称加密</li>
 *   <li>{@link ext.library.apicrypto.strategy.SM4Strategy} - SM4 国密对称加密</li>
 *   <li>{@link ext.library.apicrypto.strategy.Base64Strategy} - Base64 编码</li>
 * </ul>
 */
@NullMarked
package ext.library.apicrypto.strategy;

import org.jspecify.annotations.NullMarked;
