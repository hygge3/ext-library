package ext.library.security.crypto;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * <a href="https://zh.wikipedia.org/wiki/PBKDF2">基于密码的密钥派生函数 2(Password-Based Key Derivation Function 2)</a>
 */
@UtilityClass
public class PBKDF2 {

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    /** 盐的长度 */
    public static final int SALT_BYTE_SIZE = 32 / 2;
    /** 生成密文的长度 */
    public static final int HASH_BIT_SIZE = 128 * 4;
    /** 迭代次数 */
    public static final int PBKDF2_ITERATIONS = 1000;

    /**
     * 证实
     *
     * @param attemptedPassword 待验证密码
     * @param encryptedPassword 密文
     * @param salt              盐值
     * @return boolean
     * @throws NoSuchAlgorithmException 没有这样算法例外
     * @throws InvalidKeySpecException  无效 key 规范例外
     */
    public boolean authenticate(String attemptedPassword, String encryptedPassword, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 用相同的盐值对用户输入的密码进行加密
        String encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
        // 把加密后的密文和原密文进行比较，相同则验证成功，否则失败
        return encryptedAttemptedPassword.equals(encryptedPassword);
    }


    /**
     * 生成密文
     *
     * @param password 明文密码
     * @param salt     盐值
     * @return {@code String }
     * @throws NoSuchAlgorithmException 没有这样算法例外
     * @throws InvalidKeySpecException  无效 key 规范例外
     */
    public String getEncryptedPassword(@NotNull String password, String salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(salt), PBKDF2_ITERATIONS, HASH_BIT_SIZE);
        SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return toHex(f.generateSecret(spec).getEncoded());
    }


    /**
     * 通过加密的强随机数生成盐 (最后转换为 16 进制)
     *
     * @return {@code String }
     * @throws NoSuchAlgorithmException 没有这样算法例外
     */
    public String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        return toHex(salt);
    }


    /**
     * 十六进制字符串转二进制字符串
     *
     * @param hex 十六进制
     * @return {@code byte[] }
     */
    private byte @NotNull [] fromHex(@NotNull String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }


    /**
     * 二进制字符串转十六进制字符串
     *
     * @param array 数组
     * @return {@code String }
     */
    @NotNull
    private String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {return String.format("%0" + paddingLength + "d", 0) + hex;} else {return hex;}
    }
}
