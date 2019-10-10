package com.ibyte.common.web;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * cors安全
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class CorsSecurityFilter extends CorsFilter {

    /**
     * 构造函数
     *
     * @param configSource
     */
    public CorsSecurityFilter(CorsConfigurationSource configSource) {
        super(configSource);
    }

    /**
     * 避免类不被执行
     *
     * @return
     */
    @Override
    protected String getFilterName() {
        return null;
    }
}
