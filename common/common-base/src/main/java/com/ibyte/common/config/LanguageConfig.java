package com.ibyte.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * 多语言配置信息
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@ConfigurationProperties("kmss.lang")
public class LanguageConfig {
    /**
     * 启用多语言
     */
    @Setter
    @Getter
    private boolean supportEnabled;

    /**
     * 系统支持多语言
     */
    @Setter
    @Getter
    private String support;

    /**
     * 官方多语言
     */
    @Setter
    @Getter
    private String official;

}
