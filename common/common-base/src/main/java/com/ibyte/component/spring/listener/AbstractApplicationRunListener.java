package com.ibyte.component.spring.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;

/**
 * @Description: <系统启动监听器虚拟类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08 17:57
 */
public abstract class AbstractApplicationRunListener implements SpringApplicationRunListener, Ordered {

    protected SpringApplication springApplication;

    public AbstractApplicationRunListener(SpringApplication app, String[] args) {
        this.springApplication = app;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
