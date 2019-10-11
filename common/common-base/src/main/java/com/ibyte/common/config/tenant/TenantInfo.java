package com.ibyte.common.config.tenant;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Description: <租户配置信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@Setter
@Getter
public class TenantInfo {

    /**
     * id信息关系
     */
    private String id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 租户访问对应域名
     */
    private String domain;

    /**
     * 租户访问对应端口
     */
    private String port;

    /**
     * 租户其他附加信息
     */
    private Map<String, String> props;
}
