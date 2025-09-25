package ext.library.crypto;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * 摘要算法工具
 *
 * @since 2025.08.19
 */
public class DigestUtil {

    /**
     * Calculates the MD5 digest.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex array
     */
    public static byte[] md5(final byte[] data) {
        return Hashing.md5().hashBytes(data).asBytes();
    }

    /**
     * Calculates the MD5 digest.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex array
     */
    public static byte[] md5(final String data) {
        return Hashing.md5().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(final String data) {
        return Hashing.md5().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     *
     * @param bytes the bytes to calculate the digest over
     *
     * @return a hexadecimal digest string
     */
    public static String md5Hex(final byte[] bytes) {
        return Hashing.md5().hashBytes(bytes).toString();
    }

    /**
     * sha1
     *
     * @param data Data to digest
     *
     * @return digest as a hex array
     */
    public static byte[] sha1(String data) {
        return Hashing.sha1().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha1
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex array
     */
    public static byte[] sha1(final byte[] bytes) {
        return Hashing.sha1().hashBytes(bytes).asBytes();
    }

    /**
     * sha1Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha1Hex(String data) {
        return Hashing.sha1().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha1Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha1Hex(final byte[] bytes) {
        return Hashing.sha1().hashBytes(bytes).toString();
    }

    /**
     * sha256Hex
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha256(String data) {
        return Hashing.sha256().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha256Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha256(final byte[] bytes) {
        return Hashing.sha256().hashBytes(bytes).asBytes();
    }

    /**
     * sha256Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha256Hex(String data) {
        return Hashing.sha256().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha256Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha256Hex(final byte[] bytes) {
        return Hashing.sha256().hashBytes(bytes).toString();
    }

    /**
     * sha384
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha384(String data) {
        return Hashing.sha384().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha384
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha384(final byte[] bytes) {
        return Hashing.sha384().hashBytes(bytes).asBytes();
    }

    /**
     * sha384Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha384Hex(String data) {
        return Hashing.sha384().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha384Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha384Hex(final byte[] bytes) {
        return Hashing.sha384().hashBytes(bytes).toString();
    }

    /**
     * sha512Hex
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha512(String data) {
        return Hashing.sha512().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha512Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public static byte[] sha512(final byte[] bytes) {
        return Hashing.sha512().hashBytes(bytes).asBytes();
    }

    /**
     * sha512Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha512Hex(String data) {
        return Hashing.sha512().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha512Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public static String sha512Hex(final byte[] bytes) {
        return Hashing.sha512().hashBytes(bytes).toString();
    }

    /**
     * hmacMd5
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacMd5(String data, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacMd5
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacMd5(final byte[] bytes, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacMd5 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public static String hmacMd5Hex(String data, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacMd5 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public static String hmacMd5Hex(final byte[] bytes, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha1
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacSha1(String data, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha1
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacSha1(final byte[] bytes, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha1 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public static String hmacSha1Hex(String data, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha1 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public static String hmacSha1Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha256
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public static byte[] hmacSha256(String data, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha256
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacSha256(final byte[] bytes, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha256 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public static String hmacSha256Hex(String data, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha256 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public static String hmacSha256Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha512
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacSha512(String data, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha512
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public static byte[] hmacSha512(final byte[] bytes, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha512 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public static String hmacSha512Hex(String data, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha512 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public static String hmacSha512Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }
}