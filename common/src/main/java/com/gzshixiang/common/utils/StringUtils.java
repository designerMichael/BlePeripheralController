package com.gzshixiang.common.utils;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by linjw on 2017/7/5.
 * e-mail : linjiawei3046@cvte.com
 */
@SuppressWarnings("unused")
public class StringUtils {
    /**
     * 判断字符串是否为空.
     *
     * @param str 字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空.
     *
     * @param str 字符串
     * @return 字符串是否不为空.
     */
    public static boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 获取字符串,如果为null则返回空串.
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String getString(String str) {
        return str != null ? str : "";
    }

    /**
     * 判断字符串是否相等.
     *
     * @param lhs 字符串
     * @param rhs 字符串
     * @return 是否相等
     */
    public static boolean isEquals(String lhs, String rhs) {
        if (lhs == null && rhs == null) {
            return true;
        } else if (lhs == null) {
            return false;
        } else if (rhs == null) {
            return false;
        }
        return lhs.equals(rhs);
    }

    /**
     * 判断字符串是否不相等.
     *
     * @param lhs 字符串
     * @param rhs 字符串
     * @return 是否不相等.
     */
    public static boolean notEquals(String lhs, String rhs) {
        if (lhs == null && rhs == null) {
            return false;
        } else if (lhs == null) {
            return true;
        } else if (rhs == null) {
            return true;
        }
        return !lhs.equals(rhs);
    }

    /**
     * 从InputStream转成字符串.
     *
     * @param inputStream inputStream
     * @return 字符串
     */
    public static String stringFromInputStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\n");
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.next()).append("\n");
        }
        return result.toString();
    }
}
