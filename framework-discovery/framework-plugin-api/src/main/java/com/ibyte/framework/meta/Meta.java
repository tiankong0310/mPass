package com.ibyte.framework.meta;

import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.persistent.DesignElementApi;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: <元数据信息获取 入口，若本地获取不到，则获取远程的数据>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public class Meta {

    /**
     * 取应用列表
     */
    @SuppressWarnings("unchecked")
    public static List<MetaApplication> getApplications() {
        List<?> list = DesignElementApi.get().findApplications();
        return (List<MetaApplication>) list;
    }

    private static DiscoveryClient discoveryClient;

    /**
     * 判断节点是否存活
     */
    public static boolean isApplicationAlive(String appName) {
        if (discoveryClient == null) {
            discoveryClient = ApplicationContextHolder.getApplicationContext()
                    .getBean(DiscoveryClient.class);
        }
        for (String name : discoveryClient.getServices()) {
            if (name.equalsIgnoreCase(appName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取模块
     */
    public static MetaModule getModule(String name) {
        MetaModule module = LocalMetaContextHolder.get().getModule(name);
        if (module == null) {
            module = DesignElementApi.get().getModule(name);
        }
        return module;
    }

    /**
     * 取表
     */
    public static MetaEntity getEntity(String name) {
        MetaEntity entity = LocalMetaContextHolder.get().getEntity(name);
        if (entity == null) {
            entity = DesignElementApi.get().getEntity(name);
        }
        return entity;
    }

    /**
     * 取所有模块
     */
    public static List<MetaSummary> getModules() {
        return DesignElementApi.get().findModuleSummary();
    }

    /**
     * 获取所有的表
     */
    public static List<MetaSummary> getEntities() {
        return DesignElementApi.get().findEntitySummary();
    }

    /**
     * 获取所有的表
     */
    public static List<MetaSummary> getEntities(String module) {
        List<MetaSummary> result = DesignElementApi.get().findEntitySummary();
        if (module == null) {
            return result;
        }
        List<MetaSummary> list = new ArrayList<>();
        for (MetaSummary summary : result) {
            if (module.equals(summary.getModule())) {
                list.add(summary);
            }
        }
        return list;
    }
}
