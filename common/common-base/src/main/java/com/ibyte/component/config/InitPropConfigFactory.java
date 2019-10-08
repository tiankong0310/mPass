package com.ibyte.component.config;

import com.ibyte.component.spring.listener.AbstractApplicationRunListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Description: <init文件扫描处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08 23:32
 */
@Slf4j
public class InitPropConfigFactory implements DefaultConfigFactory {
    /**
     * 初始参数路径
     */
    private final static String DEFAULT_INIT_CONFIG_FILE = "init.properties";

    /**
     * 配置文件文件路径
     *
     * @return
     */
    protected String getPropFilePath() {
        return DEFAULT_INIT_CONFIG_FILE;
    }

    /**
     * 配置文件信息获取
     *
     * @return
     */
    @Override
    public Map<String, Object> defaultConfig() {
        Map<String, Object> rtnMap = new HashMap<>(1);
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            Enumeration<URL> urls = (classLoader != null ?
                    classLoader.getResources(getPropFilePath()) :
                    ClassLoader.getSystemResources(getPropFilePath()));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    rtnMap.put((String) entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            log.error("加载初始配置错误.", e);
        }
        return rtnMap;
    }
}
