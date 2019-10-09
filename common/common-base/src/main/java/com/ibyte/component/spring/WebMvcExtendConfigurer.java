package com.ibyte.component.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Description: <springMvc扩展，请求返回参数处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:22
 */
@Configuration
public class WebMvcExtendConfigurer implements WebMvcConfigurer {

    private final ObjectProvider<HttpMessageConverters> messageConvertersProvider;

    public WebMvcExtendConfigurer(ObjectProvider<HttpMessageConverters> messageConvertersProvider) {
        this.messageConvertersProvider = messageConvertersProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestBodyArgumentResolver(messageConvertersProvider.getIfAvailable().getConverters()));
    }

}

