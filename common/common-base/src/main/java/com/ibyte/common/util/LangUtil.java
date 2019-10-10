package com.ibyte.common.util;

import com.ibyte.common.config.LanguageConfig;
import com.ibyte.common.config.lang.LangInfo;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 多语言信息api获取
 *
 * @author li.Shangzhi
 */
public class LangUtil {

    /**
     * 多语言配置
     */
    @Setter
    private static LanguageConfig languageConfig;
    /**
     * 多语言分隔
     */
    private final static String LANG_SPLIT = ";";

    /**
     * 对语言详情分隔
     */
    private final static String LANGINGO_SPLIT = "|";

    /**
     * 支持的多语言
     */
    private static List<LangInfo> supportLangs;

    /**
     * 支持多语言对应国家
     */
    private static Set<String> countries;

    /**
     * 官方语言
     */
    private static LangInfo officialLang;


    /**
     * 是否支持多语言
     *
     * @return
     */
    public static boolean isSuportEnabled() {
        return languageConfig.isSupportEnabled();
    }

    /**
     * @param langCofig
     * @return
     */
    public static List<LangInfo> formatLangConfig(String langCofig) {
        List<LangInfo> langList = new ArrayList<>(1);
        if (!StringUtils.isEmpty(langCofig)) {
            String[] langInfos = langCofig.split(LANG_SPLIT);
            for (String langInfo : langInfos) {
                String langCode = langInfo.substring(langInfo.indexOf(LANGINGO_SPLIT) + 1);
                String langDesc = langInfo.substring(0, langInfo.indexOf(LANGINGO_SPLIT));
                langList.add(new LangInfo(langCode, langDesc));
            }
        }
        return langList;
    }

    /**
     * 获取系统支持多语言清单
     *
     * @return
     */
    public static List<LangInfo> getSupportLangs() {
        if (languageConfig.isSupportEnabled()
                && !StringUtils.isEmpty(languageConfig.getSupport())
                && CollectionUtils.isEmpty(supportLangs)) {
            supportLangs = formatLangConfig(languageConfig.getSupport());
        }
        return supportLangs;
    }

    /**
     * 获取多语言中国家信息,首字母大写
     *
     * @return
     */
    public static Set<String> getSupportCountries() {
        if (!CollectionUtils.isEmpty(LangUtil.getSupportLangs())
                && CollectionUtils.isEmpty(countries)) {
            countries = new HashSet<>(1);
            for (LangInfo langInfo : LangUtil.getSupportLangs()) {
                countries.add(StringHelper.toFirstUpperCase(langInfo.getCountry().toLowerCase()));
            }
        }
        return countries;
    }


    /**
     * 获取系统官方语言信息
     *
     * @return
     */
    public static LangInfo getOfficialLang() {
        if (languageConfig.isSupportEnabled()
                && !StringUtils.isEmpty(languageConfig.getOfficial())
                && officialLang == null) {
            List<LangInfo> defLangs = formatLangConfig(languageConfig.getOfficial());
            if (!CollectionUtils.isEmpty(defLangs)) {
                officialLang = defLangs.get(0);
            }
        }
        return officialLang;
    }
}
