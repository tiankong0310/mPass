package com.ibyte.framework.support;

import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description: <插件工厂上下文>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public class PluginContextHolder {

    /**
     * 扩展点ID -> 扩展
     */
    private static Map<String, List<ExtensionImpl>> localExtensionByPointId;

    public static List<ExtensionImpl> getLocalExtension(String pointId) {
        return localExtensionByPointId == null ? null
                : localExtensionByPointId.get(pointId);
    }

    public static void setLocalExtensionByPointId(
            Map<String, List<ExtensionImpl>> localExtensionByPointId) {
        if (PluginContextHolder.localExtensionByPointId == null) {
            PluginContextHolder.localExtensionByPointId = localExtensionByPointId;
        }
    }

    /**
     * Provider的Class -> ExtensionPoint
     */
    private static Map<Class<?>, ExtensionPointImpl> extensionPointByProvider;

    public static ExtensionPointImpl
    getExtensionPointByProvider(Class<?> clazz) {
        return extensionPointByProvider == null ? null
                : extensionPointByProvider.get(clazz);
    }

    public static void setExtensionPointByProvider(
            Map<Class<?>, ExtensionPointImpl> extensionPointByProvider) {
        if (PluginContextHolder.extensionPointByProvider == null) {
            PluginContextHolder.extensionPointByProvider = extensionPointByProvider;
        }
    }

    /**
     * 扩展点ID -> 扩展点
     */
    private static Map<String, ExtensionPointImpl> extensionPoints;

    public static ExtensionPointImpl getExtensionPoint(String pointId) {
        return extensionPoints == null ? null : extensionPoints.get(pointId);
    }

    public static Collection<ExtensionPointImpl> getExtensionPoints() {
        return extensionPoints.values();
    }

    public static void setExtensionPoints(
            Map<String, ExtensionPointImpl> extensionPoints) {
        if (PluginContextHolder.extensionPoints == null) {
            PluginContextHolder.extensionPoints = extensionPoints;
        }
    }
}
