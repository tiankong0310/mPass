package com.ibyte.common.config.lang;

import com.ibyte.common.i18n.ResourceUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description: <>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@AllArgsConstructor
@Setter
@Getter
public class LangInfo {
    /**
     * 多语言编码
     */
    private String langCode;

    /**
     * 多语言名称
     */
    private String langDesc;

    /**
     * 计算国家信息
     *
     * @return
     */
    public String getCountry() {
        return ResourceUtil.toLocale(langCode).getCountry();
    }

}
