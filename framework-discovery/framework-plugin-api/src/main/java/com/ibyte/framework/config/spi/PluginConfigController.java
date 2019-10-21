package com.ibyte.framework.config.spi;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.dto.Response;
import com.ibyte.framework.config.dto.PluginConfig;
import com.ibyte.framework.meta.MetaSummary;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.persistent.DesignElementRemoteApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description: <插件的数据获取，供管理端页面接口使用>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public interface PluginConfigController {

    /**
     * 设计持久化远程调用接口
     *
     * @return
     */
    DesignElementRemoteApi getDesignElementRemoteApi();

    /**
     * 读取可配置的扩展点
     *
     * @return
     */
    @PostMapping("findConfigurablePoints")
    default Response<List<ExtensionPointImpl>> findConfigurablePoints() {
        return Response.ok(getDesignElementRemoteApi()
                .findConfigurablePoints());
    }

    /**
     * 获取扩展点的摘要信息
     *
     * @param json
     *            {point:''}
     * @return
     */
    @PostMapping("findAllExtensions")
    default Response<List<MetaSummary>>
    findAllExtensions(@RequestBody JSONObject json) {
        return Response.ok(getDesignElementRemoteApi()
                .findAllExtensions(json.getString("point")));
    }

    /**
     * 获取插件的配置信息
     *
     * @return
     */
    @PostMapping("getPluginConfig")
    default Response<PluginConfig> getPluginConfig() {
        return Response.ok(getDesignElementRemoteApi()
                .getPluginConfig());
    }

    /**
     * 保存插件的配置信息
     *
     * @param config
     * @return
     */
    @PostMapping("savePluginConfig")
    default Response<?> savePluginConfig(@RequestBody PluginConfig config) {
        getDesignElementRemoteApi().savePluginConfig(config);
        return Response.ok();
    }
}
