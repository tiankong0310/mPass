package com.ibyte.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

/**
 * @Description: <>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@ConfigurationProperties("kmss.security")
@RefreshScope
public class CorsConfig implements CorsConfigurationSource {

    /**
     * 是否启用
     */
    @Getter
    @Setter
    private boolean enable = false;

    /**
     * 允许访问域名列表
     */
    @Getter
    @Setter
    private String[] allowedOrigins;

    /**
     * 允许方法列表
     */
    @Setter
    @Getter
    private String[] allowedMethods;

    /**
     * 允许头访问列表
     */
    @Setter
    @Getter
    private String[] allowedHeaders;

    /**
     * 系统配置
     */
    @Autowired
    private SystemConfig systemConfig;

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration configuration = null;
        if (systemConfig.isDebugger()) {
            configuration = new CorsConfiguration();
            String origin = request.getHeader(HttpHeaders.ORIGIN);
            if (StringUtils.isEmpty(origin)) {
                configuration.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
            } else {
                configuration.setAllowCredentials(true);
                configuration.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList(origin)));
            }
            configuration.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
            configuration.setAllowedMethods(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
        } else {
            if (enable) {
                configuration = new CorsConfiguration();
                if (allowedOrigins != null && allowedOrigins.length > 0) {
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList(allowedOrigins)));
                } else {
                    configuration.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
                }
                if (allowedMethods != null && allowedMethods.length > 0) {
                    configuration.setAllowedMethods(Collections.unmodifiableList(Arrays.asList(allowedMethods)));
                } else {
                    configuration.setAllowedMethods(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
                }
                if (allowedHeaders != null && allowedHeaders.length > 0) {
                    configuration.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList(allowedHeaders)));
                } else {
                    configuration.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList(CorsConfiguration.ALL)));
                }
            }
        }
        return configuration;
    }
}
