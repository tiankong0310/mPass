package com.ibyte.framework.support.listener.impl;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.plugin.annotation.ListenerConfig;
import com.ibyte.framework.plugin.spi.PluginListener;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.RemoteApi;
import com.ibyte.framework.support.listener.ClassContext;
import com.ibyte.framework.support.persistent.DesignElementApi;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: <远程api接口扫描>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
@Slf4j
public class RemoteApiScanListener implements PluginListener {

    private String apiPath = NamingConstant.PATH_PREFIX_API + "/";
    private String pathPrefix = "http://";
    private Map<String, RemoteApi> apis = new HashMap<>();

    @Override
    public void onClassStart(ClassContext clazz) {
        // 查找API信息
        RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
        String[] paths = mapping.value();
        for (String path : paths) {
            if (path.startsWith(apiPath)) {
                String className = clazz.getRefClass().getName();
                RemoteApi api = buildApi(clazz.getRefClass(), path);
                // 有接口，并且是本地模块的，才需要持久化
                if (!api.getInterfaces().isEmpty() && api.getModule() != null) {
                    apis.put(className, api);
                }
                break;
            }
        }
    }

    /** 构造API信息 */
    private RemoteApi buildApi(Class<?> clazz, String path) {
        Map<String, Map<String, String>> interfaces = new HashMap<>(16);
        appendInterface(clazz, clazz, interfaces);
        String className = clazz.getName();

        RemoteApi api = new RemoteApi();
        api.setModule(LocalMetaContextHolder.get().matchModule(className));
        api.setPath(StringHelper.join(pathPrefix,
                ApplicationContextHolder.getApplicationName(), path));
        api.setClassName(className);
        api.setInterfaces(interfaces);

        return api;
    }

    /** 查找添加接口 */
    private void appendInterface(Class<?> root, Class<?> clazz,
                                 Map<String, Map<String, String>> interfaces) {
        for (Class<?> iface : clazz.getInterfaces()) {
            String name = iface.getName();
            if (name.startsWith(NamingConstant.BASE_PACKAGE)) {
                Map<String, String> varTypes = interfaces.get(name);
                if (varTypes == null) {
                    varTypes = new HashMap<>(4);
                    appendVarType(root, iface, varTypes);
                    interfaces.put(name, varTypes);
                    appendInterface(root, iface, interfaces);
                }
            }
        }
        if (!clazz.isInterface()) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz.getName().startsWith(NamingConstant.BASE_PACKAGE)) {
                appendInterface(root, superClazz, interfaces);
            }
        }
    }

    /** 添加泛型参数实际类型 */
    private void appendVarType(Class<?> root, Class<?> iface,
                               Map<String, String> varTypes) {
        TypeVariable<?>[] params = iface.getTypeParameters();
        for (TypeVariable<?> param : params) {
            String name = param.getName();
            Class<?> clazz = PluginReflectUtil.getActualClass(root, iface,
                    name);
            if (clazz != null) {
                varTypes.put(name, clazz.getName());
            }
        }
    }

    @Override
    public void save() {
        // 保存API
        DesignElementApi.get().saveApis(apis);
    }

    /**
     * 扫描信息注册
     *
     */
    @ListenerConfig(classAnnotation = RequestMapping.class, listener = RemoteApiScanListener.class)
    public @interface RemoteApiConfig {
    }

}
