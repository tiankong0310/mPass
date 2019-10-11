package com.ibyte.common.config.trace;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: <链路信息：当前服务名称>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@Slf4j
@Component
@ConfigurationProperties("spring.application")
public class TraceConfig {

    @Getter
    @Setter
    private String name;
}
