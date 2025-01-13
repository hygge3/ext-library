package ext.library.security.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import ext.library.tool.core.Exceptions;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * 加密器
 */
@UtilityClass
public class Encipher {

    /**
     * 通过 bcrypt 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    @NotNull
    public String encryptByBCrypt(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt());
    }

    /**
     * 通过 bcrypt 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByBCrypt(String plaintext, String passwordHashed) {
        return BCrypt.checkpw(plaintext, passwordHashed);
    }

    /**
     * 通过 PKCS7 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    @NotNull
    public String encryptByPKCS7(@NotNull String plaintext) {
        byte[] encode = PKCS7.encode(plaintext.getBytes());
        return new String(encode);
    }

    /**
     * 通过 PKCS7 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByPKCS7(String plaintext, @NotNull String passwordHashed) {
        byte[] decode = PKCS7.decode(passwordHashed.getBytes());
        return new String(decode).equals(plaintext);
    }

    /**
     * 通过 PBKDF2 加密
     *
     * @param plaintext 纯文本
     * @return {@code String }
     */
    @NotNull
    public String encryptByPBKDF2(@NotNull String plaintext, String salt) {
        try {
            return PBKDF2.getEncryptedPassword(plaintext, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw Exceptions.throwOut(e, "使用 PBKDF2 加密失败");
        }
    }

    /**
     * 通过 PBKDF2 检查
     *
     * @param plaintext      纯文本
     * @param passwordHashed 密码散列
     * @return boolean
     */
    public boolean checkByPBKDF2(String plaintext, @NotNull String passwordHashed, String salt) {
        try {
            return PBKDF2.authenticate(plaintext, passwordHashed, salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw Exceptions.throwOut(e, "使用 PBKDF2 加密失败");
        }
    }

}
