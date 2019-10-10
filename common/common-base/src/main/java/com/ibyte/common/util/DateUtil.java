package com.ibyte.common.util;

import com.ibyte.common.i18n.ResourceUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @Description: <日期辅助>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class DateUtil {

    /** 一秒 */
    public static final long ONE_SECOND = 1000L;

    /** 一分钟 */
    public static final long ONE_MINUTE = ONE_SECOND * 60;

    /** 一小时 */
    public static final long ONE_HOUR = ONE_MINUTE * 60;

    /** 一天 */
    public static final long ONE_DAY = ONE_HOUR * 24;

    /** 一周 */
    public static final long ONE_WEEK = ONE_DAY * 7;

    /** 一个月 */
    public static final long ONE_MONTH = ONE_DAY * 30;

    /** 一年 */
    public static final long ONE_YEAR = ONE_DAY * 365;

    private static final long OFFSET = Calendar.getInstance()
            .get(Calendar.ZONE_OFFSET);

    /**
     * 一天的开始
     */
    public static Date dateBegin(Date date) {
        long time = (date.getTime() + OFFSET) / ONE_DAY * ONE_DAY - OFFSET;
        return new Date(time);
    }

    /**
     * 一天的结束（即明天）
     */
    public static Date dateEnd(Date date) {
        long time = (date.getTime() + OFFSET) / ONE_DAY * ONE_DAY - OFFSET
                + ONE_DAY;
        return new Date(time);
    }

    /**
     * 字符转日期
     */
    public static Date convertStringToDate(String strDate, String pattern) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符转日期
     */
    public static Date convertStringToDate(String strDate, String type,
                                           Locale locale) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }
        String pattern = ResourceUtil.getString(
                StringHelper.join("date.format.", type.toLowerCase()), null,
                locale);
        return convertStringToDate(strDate, pattern);
    }

    /**
     * 日期转字符
     */
    public static String convertDateToString(Date aDate, String pattern) {
        if (aDate == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(aDate);
    }

    /**
     * 日期转字符
     */
    public static String convertDateToString(Date aDate, String type,
                                             Locale locale) {
        if (aDate == null) {
            return null;
        }
        String pattern = ResourceUtil.getString(
                StringHelper.join("date.format.", type.toLowerCase()), null,
                locale);
        return convertDateToString(aDate, pattern);
    }

}
