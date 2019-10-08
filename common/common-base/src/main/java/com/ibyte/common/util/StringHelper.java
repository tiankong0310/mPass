package com.ibyte.common.util;

/**
 * @Description: <字符处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08
 */
public class StringHelper {

    private static char[] firstChars = "az".toCharArray();

    /**
     * 用StringBuilder拼接字符，获得更高性能
     */
    public static String join(Object... objs) {
        if (objs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            if (obj != null) {
                sb.append(obj);
            }
        }
        return sb.toString();
    }

    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String toFirstUpperCase(String str) {
        char[] chars = str.toCharArray();
        if (chars[0] >= firstChars[0] && chars[0] <= firstChars[1]) {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }
}
