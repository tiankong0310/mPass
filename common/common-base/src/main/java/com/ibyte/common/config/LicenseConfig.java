package com.ibyte.common.config;

import com.ibyte.common.config.license.LicenseCustom;
import com.ibyte.common.config.license.LicenseSign;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description: <授权配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@Slf4j
@ConfigurationProperties("license")
@Setter
@Getter
public class LicenseConfig {

    /**
     * 授权类型，Official/ryout
     */
    private String type;
    /**
     * 授权截止时间
     */
    private String expire;

    /**
     * 授权方
     */
    private String by;

    /**
     * 被授权方
     */
    private String to;

    /**
     * 授权版本
     */
    private String version;

    /**
     * 授权用户信息
     */
    private LicenseCustom customer;

    /**
     * 授权签名
     */
    private LicenseSign sign;

    /**
     * 授权绑定网卡信息
     */
    private String hwaddr;

    /**
     * 授权校验信息
     */
    private String verify;

}
