package com.ibyte.common.util;

import com.github.promeg.pinyinhelper.Pinyin;
import com.ibyte.common.i18n.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * @Description: <汉语拼音辅助工具（中文转拼音）>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class PinyinHelper {

    /**
     * 字符串比较
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int compare(String str1, String str2) {
        /* null 最小 */
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null && str2 != null) {
            return -1;
        }
        if (str1 != null && str2 == null) {
            return 1;
        }

        /* 一些特殊情况 */
        if (str1.equals(str2)) {
            return 0;
        }
        if (str1.startsWith(str2)) {
            return 1;
        }
        if (str2.startsWith(str1)) {
            return -1;
        }

        char[] charArray1 = str1.toCharArray();
        char[] charArray2 = str2.toCharArray();

        for (int i = 0; i < charArray1.length; i++) {
            char c1 = charArray1[i];
            char c2 = charArray2[i];
            if (c1 == c2) {
                // 两个字符相等，则比较下一个字符
                continue;
            }
            String p1 = getPinyin(c1, true);
            String p2 = getPinyin(c2, true);

            // 两个字符都不是中文字符，则直接进行减法操作
            if (p1 == null && p2 == null) {
                return c1 - c2;
            }
            // 中文字符排最后
            if (p1 == null && p2 != null) {
                return -1;
            }
            // 中文字符排最后
            if (p1 != null && p2 == null) {
                return 1;
            }
            // 都是中文字符，则比较其拼音字符串
            return p1.compareTo(p2);
        }

        // 理论上不可能运行到此处
        return 168;
    }

    /**
     * 将输入的汉字字符转换成拼音字符串返回，设置返回格式为小写不带音标， 如果是多音字则返回第一个读音，如果不是汉字则返回原文
     *
     * @param message 要转换的汉字字符
     * @return
     * @author limh
     */
    public static String getPinyinStringWithDefaultFormat(String message) {
        if (StringUtils.isBlank(message)) {
            return "";
        }
        char[] chars = message.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            try {
                if (!Pinyin.isChinese(chars[i])) {
                    sb.append(Character.toLowerCase(chars[i]));
                } else {
                    String pinYin = getPinyin(chars[i], true);
                    sb.append(pinYin);
                }
            } catch (Exception e) {
                log.warn("字符串转化为拼音出现异常：" + e);
            }
        }
        return sb.toString();
    }

    /**
     * 获取指定资源的拼音首字母，如果资源不是中文字符串则返回null
     *
     * @param messageKey <bundle>:<key>
     * @param locale
     * @return
     */
    public static Character getFirstPinyinChar(String messageKey, Locale locale) {
        return getFirstPinyinChar(ResourceUtil.getString(messageKey, null, locale));
    }

    /**
     * 获取第1个字符串的拼音
     *
     * @param str
     * @return
     */
    public static Character getFirstPinyinChar(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        String pinyin = getPinyin(str.charAt(0), true);
        if (pinyin == null || pinyin.length() < 1) {
            return null;
        }
        char c = pinyin.charAt(0);
        c = Character.toUpperCase(c);
        return new Character(c);
    }

    /**
     * 按照原来的转换逻辑转换拼音
     *
     * @param c           要转换字符
     * @param isLowercase 是否小写
     * @return
     */
    public static String getPinyin(char c, boolean isLowercase) {
        String pinyin = Pinyin.toPinyin(c);
        if (pinyin != null && isLowercase) {
            return pinyin.toLowerCase();
        } else {
            return pinyin;
        }
    }

    /**
     * 按照原来的转换逻辑转换拼音
     *
     * @param str         要转换的字符串
     * @param speartor    每个拼音的分隔符
     * @param isLowercase 是否小写
     * @return
     */
    public static String getPinyin(String str, String speartor, boolean isLowercase) {
        String pinyin = Pinyin.toPinyin(str, speartor);
        if (pinyin != null && isLowercase) {
            return pinyin.toLowerCase();
        } else {
            return pinyin;
        }
    }

    /**
     * 获取字符串简拼
     *
     * @param str
     * @return
     */
    public static String getSimplePinyin(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (char c : str.toCharArray()) {
            String pin = getPinyinStringWithDefaultFormat(String.valueOf(c));
            if (pin != null && pin.length() > 0) {
                buf.append(pin.charAt(0));
            }
        }
        return buf.toString().toLowerCase();
    }

    /**
     * 判断字符是否是中文
     *
     * @param c
     * @return
     */
    public static boolean isChinese(char c) {
        return Pinyin.isChinese(c);
    }

    /**
     * 判断字符串中所有字符是否是中文
     *
     * @param str
     * @return
     */
    public static boolean isAllChinese(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isChinese(chars[i])) {
                return false;
            }
        }
        return true;
    }
}
