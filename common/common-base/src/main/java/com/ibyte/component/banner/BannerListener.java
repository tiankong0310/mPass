package com.ibyte.component.banner;

import com.ibyte.component.spring.listener.AbstractApplicationRunListener;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: <系统内置banner处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08 17:53
 */
public class BannerListener extends AbstractApplicationRunListener {
    /**
     * 监听器是否已执行标示,该监听器只需要执行一次
     */
    private static volatile AtomicBoolean executed = new AtomicBoolean(false);

    private final static String BANNER_PATH = "/" + SpringApplication.BANNER_LOCATION_PROPERTY_VALUE;

    public BannerListener(SpringApplication app,String[] args) {
        super(app, args);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        if(springApplication.getWebApplicationType() == WebApplicationType.NONE){
            return;
        }

        if (executed.compareAndSet(false, true)) {
            String bannerConfig = environment.getProperty(SpringApplication.BANNER_LOCATION_PROPERTY);
            if (StringUtils.isEmpty(bannerConfig)) {
                URL url = BannerListener.class.getResource(BANNER_PATH);
                springApplication.setBanner(new ResourceBanner(new UrlResource(url)));
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
}
