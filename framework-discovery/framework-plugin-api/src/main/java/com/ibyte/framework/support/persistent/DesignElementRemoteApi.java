package com.ibyte.framework.support.persistent;

import com.ibyte.framework.config.dto.PluginConfig;
import com.ibyte.framework.meta.MetaSummary;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.persistent.dto.DesignElementDetail;
import com.ibyte.framework.support.persistent.dto.DesignElementGroup;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description: <设计持久化远程调用接口，仅提供给DesignElementApi使用>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public interface DesignElementRemoteApi {

    /**
     * 批量保存设计元素
     *
     * @param group
     */
    @PostMapping("saveAll")
    void saveAll(@RequestBody DesignElementGroup group);

    /**
     * 读取设计详细信息，返回content内容
     *
     * @param id
     * @return
     */
    @PostMapping("get")
    String get(@RequestBody String id);

    /**
     * 读取设计摘要信息，返回id、label、md5、messageKey的JSONArray（有缓存）
     *
     * @param path
     * @return
     */
    @PostMapping("findSummary")
    String findSummary(@RequestBody String path);

    /**
     * 读取指定应用的设计摘要信息，返回id、label、md5、messageKey的JSONArray（无缓存）
     *
     * @param detail
     * @return
     */
    @PostMapping("findSummaryByApp")
    String findSummaryByApp(@RequestBody DesignElementDetail detail);

    /**
     * 读取所有应用
     *
     * @return
     */
    @PostMapping("findApplications")
    String findApplications();

    /**
     * 读取扩展信息，返回扩展详情的JSONArray（过滤禁用的扩展）
     *
     * @param pointId
     * @return
     */
    @PostMapping("findExtensions")
    String findExtensions(@RequestBody String pointId);

    /**
     * 读取可配置的扩展点
     *
     * @return
     */
    @PostMapping("findConfigurablePoints")
    List<ExtensionPointImpl> findConfigurablePoints();

    /**
     * 获取扩展点的摘要信息，不过滤禁用的扩展
     *
     * @param pointId
     * @return
     */
    @PostMapping("findAllExtensions")
    List<MetaSummary> findAllExtensions(@RequestBody String pointId);

    /**
     * 获取插件的配置信息
     *
     * @return
     */
    @PostMapping("getPluginConfig")
    PluginConfig getPluginConfig();

    /**
     * 保存插件的配置信息
     *
     * @param config
     */
    @PostMapping("savePluginConfig")
    void savePluginConfig(@RequestBody PluginConfig config);
}
