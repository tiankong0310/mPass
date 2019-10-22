package com.ibyte.framework.support.proxy;

import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.domain.RemoteApi;
import com.ibyte.framework.support.persistent.DesignElementApi;
import com.ibyte.framework.support.util.PluginReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @Description: <远程API代理生成工厂>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public class RemoteApiProxyFactory {

    private static RemoteApiProxyFactory INSTANCE = new RemoteApiProxyFactory();

    private RemoteApiProxyFactory() {
    }

    public static RemoteApiProxyFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 创建远程访问代理
     */
    @SuppressWarnings("unchecked")
    public <T> T create(String apiName) {
        // 读取API信息
        RemoteApi api = DesignElementApi.get().getApi(apiName);
        if (api == null) {
            return null;
        }
        // 根据API信息，读取本地存在的接口和接口泛型参数
        List<Class<?>> interfaces = new ArrayList<>();
        Map<String, Map<String, Class<?>>> ifaceVarTypes = new HashMap<>(16);
        for (Map.Entry<String, Map<String, String>> entry : api.getInterfaces()
                .entrySet()) {
            // api.getInterfaces() :: 接口类名 -> 泛型参数名 -> 泛型实际类名
            Class<?> iface = PluginReflectUtil.classForName(entry.getKey());
            if (iface == null) {
                continue;
            }
            interfaces.add(iface);
            // 读取泛型参数
            if (entry.getValue().isEmpty()) {
                continue;
            }
            Map<String, Class<?>> varTypes = new HashMap<>(4);
            for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
                Class<?> type = PluginReflectUtil.classForName(e.getValue());
                if (type != null) {
                    varTypes.put(e.getKey(), type);
                }
            }
            if (!varTypes.isEmpty()) {
                ifaceVarTypes.put(entry.getKey(), varTypes);
            }
        }
        // 本地无接口，无法代理
        if (interfaces.isEmpty()) {
            return null;
        }
        // 创建代理并返回
        RestTemplate template = ApplicationContextHolder.getApplicationContext()
                .getBean(RestTemplate.class);
        InvocationHandler handler = new RemoteInvocationHandler(api.getPath(),
                ifaceVarTypes, template);
        return (T) Proxy.newProxyInstance(PluginReflectUtil.getClassLoader(),
                interfaces.toArray(new Class<?>[] {}), handler);
    }

    /**
     * 根据制定接口创建远程访问代理
     */
    @SuppressWarnings("unchecked")
    public <T> T create(String apiName, Class<?> iface) {
        // 读取API信息
        RemoteApi api = DesignElementApi.get().getApi(apiName);
        if (api == null) {
            return null;
        }
        // 创建代理并返回
        RestTemplate template = ApplicationContextHolder.getApplicationContext()
                .getBean(RestTemplate.class);
        InvocationHandler handler = new RemoteInvocationHandler(api.getPath(),
                Collections.emptyMap(), template);
        return (T) Proxy.newProxyInstance(PluginReflectUtil.getClassLoader(),
                new Class<?>[] { iface }, handler);
    }

    static class RemoteInvocationHandler implements InvocationHandler {
        private String path;

        private Map<String, Map<String, Class<?>>> ifaceVarTypes;

        private RestTemplate template;

        public RemoteInvocationHandler(String path,
                                       Map<String, Map<String, Class<?>>> ifaceVarTypes,
                                       RestTemplate template) {
            this.path = path;
            this.ifaceVarTypes = ifaceVarTypes;
            this.template = template;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            // URL
            String url = StringHelper.join(path, '/', method.getName());
            // 准备提交参数：body和param
            Object body = null;
            Map<String, Object> uriVars = new HashMap<>(16);
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    Class<? extends Annotation> type = annotation
                            .annotationType();
                    if (RequestBody.class == type) {
                        body = args[i];
                    } else if (RequestParam.class == type) {
                        RequestParam param = (RequestParam) annotation;
                        String name = param.name();
                        if (StringUtils.isBlank(name)) {
                            name = param.value();
                        }
                        Object value = args[i];
                        if (value == null) {
                            value = param.defaultValue();
                        }
                        uriVars.put(name, value);
                    }
                }
            }
            HttpEntity<Object> entity = (body == null) ? null
                    : new HttpEntity<Object>(body);
            // 根据返回参数是否泛型，调用不同接口
            ResponseEntity<?> result = null;
            Type type = method.getGenericReturnType();
            if (type instanceof Class) {
                result = template.exchange(url, HttpMethod.POST, entity,
                        (Class<?>) type, uriVars);
            } else {
                Class<?> clazz = method.getDeclaringClass();
                result = template.exchange(url, HttpMethod.POST, entity,
                        parameterize(clazz, type), uriVars);
            }
            return result.getBody();
        }

        /**
         * 填充泛型参数的变量
         */
        private ParameterizedTypeReference<?> parameterize(Class<?> root,
                                                           Type type) {
            Map<String, Class<?>> varTypes = ifaceVarTypes.get(root.getName());
            if (varTypes == null) {
                return ParameterizedTypeReference.forType(type);
            }
            Type newType = PluginReflectUtil.parameterize(type, var -> {
                return varTypes.get(var.getName());
            });
            return ParameterizedTypeReference.forType(newType);
        }
    }


}
