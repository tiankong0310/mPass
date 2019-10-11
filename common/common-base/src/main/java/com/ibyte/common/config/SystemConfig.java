package com.ibyte.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: <系统配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@ConfigurationProperties("kmss.system")
public class SystemConfig {

    /**
     * 是否启用调试模式
     */
    @Getter
    @Setter
    private boolean debugger = false;

    /**
     * RSA公钥
     */
    @Getter
    @Setter
    private String publicCode;


    /**
     * RSA私钥
     */
    @Getter
    @Setter
    private String privateCode;
}
