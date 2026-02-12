package ext.library.captcha.draw;

import ext.library.captcha.core.CaptchaUtil;

import java.util.Random;

/**
 * 数学表达式生成和计算
 */
class Expression {

    private static final char plus = '+';

    private static final char minus = '-';

    private static final char multiply = '×';

    /**
     * 执行表达式
     *
     * @param expr 表达式
     *
     * @return 结果，-1 为 expr 表达式不合法
     */
    public static int eval(String expr) {
        char[] chars = expr.toCharArray();
        int length = expr.length();
        for (int i = 0; i < chars.length; i++) {
            char operator = chars[i];
            if (plus == operator || minus == operator || multiply == operator) {
                int num1 = findInt(expr, 0, i);
                int num2 = findInt(expr, i + 1, length);
                return eval(num1, operator, num2);
            }
        }
        return -1;
    }

    private static int eval(int num1, char operator, int num2) {
        return switch (operator) {
            case plus -> num1 + num2;
            case minus -> num1 - num2;
            case multiply -> num1 * num2;
            default -> -1;
        };
    }

    /**
     * 随机表达式
     *
     * @return 表达式
     */
    public static String randomExpr(Random random) {
        char[] chars = new char[]{plus, minus, multiply};
        char operator = chars[random.nextInt(chars.length)];
        int num1;
        int num2;
        // 乘法减少数值
        if (multiply == operator) {
            num1 = CaptchaUtil.randNum(random, 1, 10);
            num2 = CaptchaUtil.randNum(random, 1, 10);
        } else {
            num1 = CaptchaUtil.randNum(random, 1, 20);
            num2 = CaptchaUtil.randNum(random, 1, 20);
        }
        // 保证减法的结果不会出现负数
        if (minus == operator && num2 > num1) {
            int num = num1;
            num1 = num2;
            num2 = num;
        }
        return String.valueOf(num1) + operator + num2;
    }

    private static int findInt(String expr, int start, int end) {
        return Integer.parseInt(expr.substring(start, end));
    }

}
