package com.ibyte.common.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * @Description: <多语言信息获取接口>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:33
 */
public interface MessageProvider {

    /**
     * 获取多语言信息
     *
     * @param key
     * @param bundle
     * @param locale
     * @return
     */
    String getString(String key, String bundle, Locale locale);

    /**
     * 更具前缀获取多语言信息
     *
     * @param prefixKey
     * @param bundle
     * @param locale
     * @return
     */
    Map<String, String> getStrings(String prefixKey, String bundle, Locale locale);
}
