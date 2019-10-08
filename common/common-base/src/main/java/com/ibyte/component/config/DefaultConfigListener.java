package com.ibyte.component.config;

import com.ibyte.component.spring.listener.AbstractApplicationRunListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: <系统内置默认配置处理类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08 23:31
 */
@Slf4j
public class DefaultConfigListener extends AbstractApplicationRunListener {

    /**
     * app对象
     */
    private SpringApplication springApplication;

    /**
     * 监听器是否已执行标示,该监听器只需要执行一次
     */
    private static volatile AtomicBoolean executed = new AtomicBoolean(false);

    public DefaultConfigListener(SpringApplication app, String[] args) {
        super(app, args);
        this.springApplication = app;

    }

    @Override
    public void starting() {
        if (executed.compareAndSet(false, true)) {
            List<DefaultConfigFactory> defaultConfigs = SpringFactoriesLoader.loadFactories(DefaultConfigFactory.class, this.getClass().getClassLoader());
            Map<String, Object> defaultConfig = new Hashtable<>(1);
            for (DefaultConfigFactory defaultConfigFactory : defaultConfigs) {
                defaultConfig.putAll(defaultConfigFactory.defaultConfig());
            }
            springApplication.setDefaultProperties(defaultConfig);
        }
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
}
