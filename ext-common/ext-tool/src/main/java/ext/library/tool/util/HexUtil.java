package ext.library.tool.util;

import java.util.HexFormat;

/**
 * 十六进制与进制转换工具类
 *
 * <p>提供以下功能：
 * <ul>
 *   <li>字节数组与十六进制字符串的相互转换</li>
 *   <li>十进制与二进制、八进制、十六进制之间的转换</li>
 * </ul>
 * </p>
 */
public final class HexUtil {

    private static final HexFormat hexFormat = HexFormat.of();

    private HexUtil() {
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     *
     * @return 十六进制字符串（小写）
     */
    public static String encode(byte[] bytes) {
        return hexFormat.formatHex(bytes);
    }

    /**
     * 十六进制字符串转字节数组
     *
     * @param hex 十六进制字符串
     *
     * @return 字节数组
     */
    public static byte[] decode(String hex) {
        return hexFormat.parseHex(hex);
    }

    /**
     * 十进制转二进制
     *
     * @param num 十进制数字
     *
     * @return 二进制字符串
     */
    public static String decToBin(int num) {
        return Integer.toString(num, 2);
    }

    /**
     * 二进制转十进制
     *
     * @param data 二进制字符串
     *
     * @return 十进制数字
     */
    public static int binToDec(String data) {
        return Integer.parseInt(data, 2);
    }

    /**
     * 十进制转八进制
     *
     * @param num 十进制数字
     *
     * @return 八进制字符串
     */
    public static String decToOct(int num) {
        return Integer.toString(num, 8);
    }

    /**
     * 八进制转十进制
     *
     * @param data 八进制字符串
     *
     * @return 十进制数字
     */
    public static int octToDec(String data) {
        return Integer.parseInt(data, 8);
    }

    /**
     * 十进制转十六进制
     *
     * @param num 十进制数字
     *
     * @return 十六进制字符串
     */
    public static String decToHex(int num) {
        return Integer.toString(num, 16);
    }

    /**
     * 十六进制转十进制
     *
     * @param data 十六进制字符串
     *
     * @return 十进制数字
     */
    public static int hexToDec(String data) {
        return Integer.parseInt(data, 16);
    }

}
