package com.ibyte.framework.support.loader;

import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.listener.PluginListenerManager;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: <插件工厂加载器>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-15 00:55
 */
@Slf4j
public class PluginLoader {

    /** 加载事件监听管理器 */
    private PluginListenerManager listenerManager = new PluginListenerManager();
    /** 扩展点ID -> 扩展点 */
    private Map<String, ExtensionPointImpl> extensionPoints = new HashMap<>();
    /** 扩展点ID -> 扩展ID -> 扩展 */
    private Map<String, Map<String, ExtensionImpl>> extensions = new HashMap<>();

    /** 扩展点ID -> 扩展 */
    private Map<String, List<ExtensionImpl>> localExtensionByPointId = new HashMap<>();
    /** Provider的Class -> ExtensionPoint */
    private Map<Class<?>, ExtensionPointImpl> extensionPointByProvider = new HashMap<>();
    /** 需持久化的扩展点信息 */
    private Map<String, ExtensionPointImpl> extensionPoints4Save = new HashMap<>();
    /** 需持久化的扩展信息 */
    private Map<String, ExtensionImpl> extensions4Save = new HashMap<>();





}
