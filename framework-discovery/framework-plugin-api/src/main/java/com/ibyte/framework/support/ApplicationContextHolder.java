package com.ibyte.framework.support;

import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.support.proxy.LocalApiProxyFactory;
import com.ibyte.framework.support.proxy.RemoteApiProxyFactory;
import com.ibyte.framework.support.util.PluginReflectUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import com.ibyte.common.util.thread.KeyLockFactory;
import com.ibyte.common.util.thread.KeyLockFactory.KeyLock;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: <应用上下文>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public class ApplicationContextHolder {
    /** Spring代理类的标记 */
    public static final String SPRING_PROXY_FLAG = "$$";
    /** Spring上下文 */
    private static ApplicationContext applicationContext;
    /** 应用名 */
    private static String applicationName;

    public static String getApplicationName() {
        return applicationName;
    }

    public static void setApplicationName(String appName) {
        if (applicationName == null) {
            applicationName = appName;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext context) {
        if (applicationContext == null
                || applicationContext == context.getParent()) {
            applicationContext = context;
        }
    }

    private static final Map<String, Object> API_CACHE = new ConcurrentHashMap<>();

    private static final KeyLockFactory LOCK_FACTORY = new KeyLockFactory();

    private static final Object NULL = new Object();

    /**
     * 优先查找Spring中是否有该Bean，若没有，则从new一个
     */
    public static <T> T findOrCreateInstance(Class<T> clazz) {
        if (clazz == null || clazz == Void.class) {
            return null;
        }
        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
        }
        if (!clazz.isInterface()
                && !Modifier.isAbstract(clazz.getModifiers())) {
            return PluginReflectUtil.newInstance(clazz);
        }
        return null;
    }

    /**
     * 根据API类名，获取API
     */
    @SuppressWarnings("unchecked")
    public static <T> T getApi(String apiName) {
        Object api = API_CACHE.get(apiName);
        if (api == null) {
            KeyLock lock = LOCK_FACTORY.getKeyLock(apiName).lock();
            try {
                Class<?> clazz = PluginReflectUtil.classForName(apiName);
                if (clazz != null) {
                    // 从本地找
                    api = findOrCreateInstance(clazz);
                } else {
                    // 本地没有，使用远程代理
                    api = RemoteApiProxyFactory.getInstance().create(apiName);
                }
                if (api == null) {
                    api = NULL;
                }
                API_CACHE.put(apiName, api);
            } finally {
                lock.unlock();
            }
        }
        return api == NULL ? null : (T) api;
    }

    /**
     * 使用指定接口访问API
     */
    @SuppressWarnings("unchecked")
    public static <T> T getApi(String apiName, Class<T> iface) {
        String key = StringHelper.join(apiName, '#', iface.getName());
        Object api = API_CACHE.get(key);
        if (api == null) {
            api = getApi(apiName);
            if (api != null && iface.isAssignableFrom(api.getClass())) {
                // 默认接口
                return (T) api;
            }
            KeyLock lock = LOCK_FACTORY.getKeyLock(key).lock();
            try {
                Class<?> clazz = PluginReflectUtil.classForName(apiName);
                if (clazz != null) {
                    // 代理本地接口
                    if (api != null) {
                        // 本地代理接口实现
                        api = LocalApiProxyFactory.getInstance().create(api,
                                iface);
                    }
                } else {
                    // 代理远程接口
                    api = RemoteApiProxyFactory.getInstance().create(apiName,
                            iface);
                }
                if (api == null) {
                    api = NULL;
                }
                API_CACHE.put(key, api);
            } finally {
                lock.unlock();
            }
        }
        return api == NULL ? null : (T) api;
    }
}
