package ext.library.tool.util;

import java.util.HexFormat;

public class HexUtil {

    /**
     * 字节流转成十六进制表示
     */
    public static String encode(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * 十六进制字符串转成字节流
     */
    public static byte[] decode(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    /**
     * 十进制转二进制
     *
     * @param num 十进制数字
     *
     * @return {@link String } 二进制字符串
     */
    public static String decToBin(Integer num) {
        return Integer.toString(num, 2);
    }


    /**
     * 二进制转十进制
     *
     * @param data 二进制字符串
     *
     * @return {@link Integer } 十进制数字
     */
    public static Integer binToDec(String data) {
        return Integer.parseInt(data, 2);
    }

    /**
     * 十进制转八进制
     *
     * @param num 十进制数字
     *
     * @return {@link String } 八进制字符串
     */
    public static String decToOct(Integer num) {
        return Integer.toString(num, 8);
    }

    /**
     * 八进制转十进制
     *
     * @param data 八进制字符串
     *
     * @return {@link Integer } 十进制数字
     */
    public static Integer octToDec(String data) {
        return Integer.parseInt(data, 8);
    }

    /**
     * 十进制转十六进制
     *
     * @param num 十进制数字
     *
     * @return {@link String } 十六进制字符串
     */
    public static String decToHex(Integer num) {
        return Integer.toString(num, 16);
    }

    /**
     * 十六进制转十进制
     *
     * @param data 十六进制字符串
     *
     * @return {@link Integer } 十进制数字
     */
    public static Integer hexToDec(String data) {
        return Integer.parseInt(data, 16);
    }

}