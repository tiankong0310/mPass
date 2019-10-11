package com.ibyte.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.security.encryption.provider.Md5EncryptProvider;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.web.IWebFilterDef;
import com.ibyte.common.web.WebThreadFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @Description: <系统内置配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@Configuration
@EnableConfigurationProperties({SystemConfig.class, TenantConfig.class, LicenseConfig.class, LanguageConfig.class, CorsConfig.class})
public class WebConfig {

    /**
     * 自定义过滤器定义
     */
    @Autowired(required = false)
    private List<IWebFilterDef> filterDefs;

    /**
     * 系统内置filter，优先级高于spring security的身份认证
     * @return
     */
    @Bean
    public FilterRegistrationBean<WebThreadFilter> filterRegistrationBean(@SuppressWarnings("SpringJavaAutowiringInspection") Environment environment) {
        FilterRegistrationBean<WebThreadFilter> registration = new FilterRegistrationBean<WebThreadFilter>();
        registration.addUrlPatterns("/*");
        WebThreadFilter filterProxy = new WebThreadFilter(filterDefs);
        filterProxy.setEnvironment(environment);
        registration.setFilter(filterProxy);
        registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER - 1);
        return registration;
    }

    /**
     * 默认md5提供器
     *
     * @return
     */
    @Bean
    public Md5EncryptProvider md5Encrypt() {
        return new Md5EncryptProvider(null);
    }

    /**
     * jsonutil 初始化处理
     * jsonutil 初始化处理
     *
     * @param mapper
     */
    @Autowired
    public void setObjectMapper(@SuppressWarnings("SpringJavaAutowiringInspection") @Autowired ObjectMapper mapper) {
        JsonUtil.setMapper(mapper);
    }

    /**
     * 多语言设置
     *
     * @param langConfig
     */
    @Autowired
    public void setLangConfig(@Autowired LanguageConfig langConfig) {
        LangUtil.setLanguageConfig(langConfig);
        if (LangUtil.getOfficialLang() != null) {
            //设置系统官方语言
            ResourceUtil.setOfficalLocale(ResourceUtil.toLocale(LangUtil.getOfficialLang().getLangCode()));
        }
    }
}
