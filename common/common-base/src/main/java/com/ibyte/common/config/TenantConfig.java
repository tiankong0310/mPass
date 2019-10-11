package com.ibyte.common.config;

import com.ibyte.common.config.tenant.TenantInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;

/**
 * @Description: <租户配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
@Slf4j
@ConfigurationProperties("kmss.tenant")
@RefreshScope
public class TenantConfig {

    /**s
     * 是否启用
     */
    @Setter
    @Getter
    private boolean enable = false;

    /**
     * 租户细节配置
     */
    @Setter
    private Map<String, TenantInfo> config;

    /**
     * TenantInfo id 初始化
     *
     * @return
     */
    public Map<String, TenantInfo> getConfig() {
        for (Map.Entry<String, TenantInfo> entry : config.entrySet()) {
            TenantInfo info = entry.getValue();
            if (StringUtils.isEmpty(info.getId())) {
                info.setId(entry.getKey());
            }
        }
        return config;
    }
}
