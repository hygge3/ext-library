package ext.library.security.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

/**
 * 加密器
 */
@UtilityClass
public class Encryptor {
     final static PasswordEncoder DEFAULT_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

     final static Argon2PasswordEncoder ARGON2_ENCODER = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
     final static BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();
     final static Pbkdf2PasswordEncoder PBKDF2_ENCODER = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
     final static SCryptPasswordEncoder SCRYPT_ENCODER = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();

    /**
     * 通过 默认加密器 BCrypt 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    public String encrypt(String plaintext) {
        return DEFAULT_ENCODER.encode(plaintext);
    }

    /**
     * 通过 默认加密器 BCrypt 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean check(String plaintext, String passwordHashed) {
        return DEFAULT_ENCODER.matches(plaintext, passwordHashed);
    }

    /**
     * 通过 Argon2 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    public String encryptByArgon2(String plaintext) {
        return ARGON2_ENCODER.encode(plaintext);
    }

    /**
     * 通过 Argon2 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByArgon2(String plaintext, String passwordHashed) {
        return ARGON2_ENCODER.matches(plaintext, passwordHashed);
    }

    /**
     * 通过 BCrypt 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    public String encryptByBCrypt(String plaintext) {
        return BCRYPT_ENCODER.encode(plaintext);
    }

    /**
     * 通过 BCrypt 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByBCrypt(String plaintext, String passwordHashed) {
        return BCRYPT_ENCODER.matches(plaintext, passwordHashed);
    }

    /**
     * 通过 PBKDF2 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    public String encryptByPBKDF2(String plaintext) {
        return PBKDF2_ENCODER.encode(plaintext);
    }

    /**
     * 通过 PBKDF2 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByPBKDF2(String plaintext, String passwordHashed) {
        return PBKDF2_ENCODER.matches(plaintext, passwordHashed);
    }

    /**
     * 通过 SCrypt 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    public String encryptBySCrypt(String plaintext) {
        return SCRYPT_ENCODER.encode(plaintext);
    }

    /**
     * 通过 SCrypt 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkBySCrypt(String plaintext, String passwordHashed) {
        return SCRYPT_ENCODER.matches(plaintext, passwordHashed);
    }
}
