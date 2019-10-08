package com.ibyte.component.logback;

import com.ibyte.component.spring.listener.AbstractApplicationRunListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  logbak统一标准的配置.
 *  该监听器生效配置位于META-INF/spring.factories文件配置中.
 *  当自己指定了logback配置时，该默认配置失效。
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08 22:39
 */
public class LogbackListener extends AbstractApplicationRunListener {
    /**
     * 监听器排序号，需高于LoggingApplicationListener的默认优先级
     */
    private final int LOGBACK_ORDER = LoggingApplicationListener.DEFAULT_ORDER - 1;

    /**
     * 默认logback配置文件名
     */
    private final static String LOGBACK_CFG_NAME = "/logback-config.xml";

    /**
     * 监听器是否已执行标示,该监听器只需要执行一次
     */
    private static volatile AtomicBoolean executed = new AtomicBoolean(false);

    public LogbackListener(SpringApplication app, String[] args) {
        super(app,args);
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        if (executed.compareAndSet(false, true)) {
            String logConfig = environment.getProperty(LoggingApplicationListener.CONFIG_PROPERTY);
            if (StringUtils.isEmpty(logConfig)) {
                URL url = LogbackListener.class.getResource(LOGBACK_CFG_NAME);
                String filePath = url.getPath();
                if (filePath.indexOf(ResourceUtils.JAR_FILE_EXTENSION) > -1) {
                    filePath = ResourceUtils.JAR_URL_PREFIX + filePath;
                }
                System.setProperty(LoggingApplicationListener.CONFIG_PROPERTY, filePath);
            }
        }
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

    @Override
    public int getOrder() {
        return LOGBACK_ORDER;
    }
}
