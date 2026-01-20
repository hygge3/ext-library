package ext.library.crypto;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;

/**
 * 密码编码器工具类，提供密码哈希和验证功能
 *
 * <p>设计目的：基于 Spring Security 的 DelegatingPasswordEncoder，提供简单易用的密码编码 API</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>密码哈希：将明文密码转换为安全的哈希值</li>
 *   <li>密码验证：验证明文密码是否与哈希值匹配</li>
 *   <li>自动升级：支持检测旧算法并建议升级</li>
 * </ul>
 * </p>
 *
 * <p>支持的算法：
 * <ul>
 *   <li><strong>bcrypt</strong>（默认）- 自适应哈希函数，推荐用于密码存储</li>
 *   <li>argon2 - 2015 年密码哈希竞赛获胜算法，抗 GPU/ASIC 攻击</li>
 *   <li>scrypt - 内存密集型哈希函数</li>
 *   <li>pbkdf2 - NIST 推荐的密钥派生函数</li>
 *   <li>sha256 - 仅用于兼容旧系统</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * // 加密密码（默认使用 bcrypt）
 * String hashed = PasswordEncoder.encode("myPassword");
 * // 输出格式: {bcrypt}$2a$10$...
 *
 * // 验证密码
 * boolean matches = PasswordEncoder.matches("myPassword", hashed);
 *
 * // 检查是否需要升级
 * boolean needsUpgrade = PasswordEncoder.upgradeEncoding(hashed);
 * </pre>
 * </p>
 *
 * <p>密码格式：
 * <pre>
 * {algorithm}encodedPassword
 * 例如: {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
 * </pre>
 * </p>
 *
 * <p>安全建议：
 * <ul>
 *   <li>新系统默认使用 bcrypt 或 argon2</li>
 *   <li>定期检查 upgradeEncoding() 以升级旧密码</li>
 *   <li>切勿存储明文密码</li>
 * </ul>
 * </p>
 *
 * @see org.springframework.security.crypto.factory.PasswordEncoderFactories
 * @see org.springframework.security.crypto.password.DelegatingPasswordEncoder
 */
public final class PasswordEncoder {

    private static final org.springframework.security.crypto.password.PasswordEncoder ENCODER =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private PasswordEncoder() {
        // 私有构造函数，防止实例化
    }

    /**
     * 对密码进行哈希编码
     *
     * <p>默认使用 bcrypt 算法，输出格式为 {bcrypt}encodedPassword</p>
     *
     * @param rawPassword 明文密码
     * @return 哈希后的密码字符串（包含算法标识前缀）
     */
    public static String encode(CharSequence rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * <p>自动识别哈希值中的算法标识，使用对应算法进行验证</p>
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 哈希后的密码
     * @return 如果密码匹配返回 true，否则返回 false
     */
    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 检查密码是否需要重新编码
     *
     * <p>用于检测使用旧算法或弱参数编码的密码，建议在用户登录成功后调用</p>
     *
     * @param encodedPassword 哈希后的密码
     * @return 如果密码需要使用更强算法重新编码返回 true
     */
    public static boolean upgradeEncoding(String encodedPassword) {
        return ENCODER.upgradeEncoding(encodedPassword);
    }

    /**
     * 获取底层 Spring Security PasswordEncoder 实例
     *
     * <p>用于需要直接使用 Spring Security API 的场景</p>
     *
     * @return DelegatingPasswordEncoder 实例
     */
    public static org.springframework.security.crypto.password.PasswordEncoder getEncoder() {
        return ENCODER;
    }
}
