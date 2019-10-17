package com.ibyte.common.i18n;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * @Description: <读取多语言信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:29
 */
public class ResourceUtil {

    private static final String LOCALE_KEY = Locale.class.getName();

    private static MessageProvider PROVIDER = new DefaultMessageProvider();

    private static Locale OFFICIAL_LANG = Locale.CHINA;

    static {
        Locale.setDefault(Locale.CHINA);
    }

    /**
     * 获取默认资源的值，格式：bundle:key
     */
    public static String getString(String key) {
        return getString(key, null, null);
    }

    /**
     * 获取指定bundle的的资源值 ，bundle为包路径去除com.ibyte，以-分隔，比如：sys-org
     */
    public static String getString(String key, String bundle) {
        return getString(key, bundle, null);
    }

    /**
     * 获取指定bundle和用户选择的语言环境的资源值
     */
    public static String getString(String key, String bundle, Locale locale) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (bundle == null) {
            int index = key.indexOf(':');
            if (index > -1) {
                bundle = key.substring(0, index);
                key = key.substring(index + 1);
            }
        }
        if (locale == null) {
            locale = currentLocale();
        }
        return PROVIDER.getString(key, bundle, locale);
    }

    /**
     * 获取默认资源的值，格式：bundle:key
     */
    public static Map<String, String> getStrings(String prefixKey) {
        return getStrings(prefixKey, null, null);
    }

    /**
     * 获取指定bundle的的资源值 ，bundle为包路径去除com.landray，以-分隔，比如：sys-org
     */
    public static Map<String, String> getStrings(String prefixKey, String bundle) {
        return getStrings(prefixKey, bundle, null);
    }

    /**
     * 获取指定bundle和用户选择的语言环境的资源值
     */
    public static Map<String, String> getStrings(String prefixKey, String bundle, Locale locale) {
        if (StringUtils.isBlank(prefixKey)) {
            return null;
        }
        if (bundle == null) {
            int index = prefixKey.indexOf(':');
            if (index > -1) {
                bundle = prefixKey.substring(0, index);
                prefixKey = prefixKey.substring(index + 1);
            }
        }
        if (locale == null) {
            locale = currentLocale();
        }
        return PROVIDER.getStrings(prefixKey, bundle, locale);
    }

    /**
     * 替换参数：{0},{1},{2}...
     */
    public static String replaceArgs(String message, String... args) {
        String result = message;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                result = StringUtils.replace(result,
                        StringHelper.join("{", i, "}"), args[i]);
            }
        }
        return result;
    }

    /**
     * 获取官方语言的资源值
     */
    public static String getOfficialString(String key) {
        return getString(key, null, OFFICIAL_LANG);
    }

    /**
     * 获取指定bundle的官方语言的资源值
     */
    public static String getOfficialString(String key, String bundle) {
        return getString(key, bundle, OFFICIAL_LANG);
    }

    /**
     * 默认当前语言
     *
     * @return
     */
    public static Locale currentLocale() {
        return currentLocale(OFFICIAL_LANG);
    }

    /**
     * 读取当前语言环境
     */
    public static Locale currentLocale(Locale defLocale) {
        Locale locale = ThreadLocalUtil.getLocalVar(LOCALE_KEY);
        if (locale == null) {
            locale = toLocale(ThreadLocalUtil.getTranVar(LOCALE_KEY));
            if (locale == null) {
                locale = defLocale == null ? OFFICIAL_LANG:defLocale;
            }
            ThreadLocalUtil.setLocalVar(LOCALE_KEY, locale);
        }
        return locale;
    }

    /**
     * 官方语言
     */
    public static Locale officialLocale() {
        return OFFICIAL_LANG;
    }

    /**
     * 切换语言
     */
    public static void switchLocale(Locale locale) {
        ThreadLocalUtil.setLocalVar(LOCALE_KEY, locale);
        ThreadLocalUtil.setTranVar(LOCALE_KEY, localeToString(locale));
    }

    /**
     * 设置官方语言
     */
    public static void setOfficalLocale(Locale locale) {
        OFFICIAL_LANG = locale == null ? Locale.CHINA : locale;
    }

    public static void setProvider(MessageProvider provider) {
        if (provider != null) {
            PROVIDER = provider;
        }
    }

    /**
     * String转Locale
     */
    public static Locale toLocale(String locale) {
        if (locale == null) {
            return null;
        }
        String formatLocale = locale.replace(NamingConstant.UNDERLINE, NamingConstant.STRIKE);
        for (Locale loc : Locale.getAvailableLocales()) {
            if (localeToString(loc).equalsIgnoreCase(formatLocale)) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Locale转String
     */
    public static String localeToString(Locale locale) {
        return StringHelper.join(locale.getLanguage(), NamingConstant.STRIKE,
                locale.getCountry());
    }

}
