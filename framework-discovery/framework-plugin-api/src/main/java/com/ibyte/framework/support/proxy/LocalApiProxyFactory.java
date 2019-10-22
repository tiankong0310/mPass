package com.ibyte.framework.support.proxy;

import com.fasterxml.jackson.databind.JavaType;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.AllArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: <本地API代理生成工厂>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public class LocalApiProxyFactory {

    private static LocalApiProxyFactory INSTANCE = new LocalApiProxyFactory();

    private LocalApiProxyFactory() {
    }

    public static LocalApiProxyFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 根据制定接口创建远程访问代理
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Object api, Class<?> iface) {
        Map<Method, MethodDescriptor> methodMap = new HashMap<>(16);
        Class<?> apiClazz = api.getClass();
        for (Method ifaceMethod : iface.getMethods()) {
            Method apiMethod = findMethod(apiClazz, ifaceMethod);
            methodMap.put(ifaceMethod,
                    buildDescriptor(apiClazz, apiMethod, ifaceMethod));
        }
        InvocationHandler handler = new LocalInvocationHandler(api, methodMap);
        return (T) Proxy.newProxyInstance(PluginReflectUtil.getClassLoader(),
                new Class<?>[]{iface}, handler);
    }

    /**
     * 在api的接口中找类似的方法
     */
    private Method findMethod(Class<?> apiClazz, Method ifaceMethod) {
        // 找方法名相同，参数个数相同的方法
        List<Method> probableMethods = new ArrayList<>();
        String name = ifaceMethod.getName();
        int count = ifaceMethod.getParameterCount();
        for (Class<?> curClazz = apiClazz; curClazz != null; curClazz = curClazz
                .getSuperclass()) {
            for (Class<?> apiIface : curClazz.getInterfaces()) {
                for (Method apiMethod : apiIface.getMethods()) {
                    if (name.equals(apiMethod.getName())
                            && count == apiMethod.getParameterCount()
                            && !probableMethods.contains(apiMethod)) {
                        probableMethods.add(apiMethod);
                    }
                }
            }
        }
        // 找不到
        if (probableMethods.isEmpty()) {
            throw new ParamsNotValidException(
                    "无法在被代理的类中获取对应方法：" + ifaceMethod.getName());
        }
        // 找到1个
        if (probableMethods.size() == 1) {
            return probableMethods.get(0);
        }
        // 找到多个，匹配注解
        Annotation[][] anns = ifaceMethod.getParameterAnnotations();
        for (Method probableMethod : probableMethods) {
            if (matchAnnotations(anns,
                    probableMethod.getParameterAnnotations())) {
                return probableMethod;
            }
        }
        throw new ParamsNotValidException(
                "被代理的类中有超过1个的同名参数个数相同的方法：" + ifaceMethod.getName());
    }

    /**
     * 参数的注解匹配
     */
    private boolean matchAnnotations(Annotation[][] srcAnn,
                                     Annotation[][] tarAnns) {
        for (int i = 0; i < srcAnn.length; i++) {
            if (srcAnn[i].length != tarAnns[i].length) {
                return false;
            }
            for (int j = 0; j < srcAnn[i].length; j++) {
                if (!srcAnn[i][j].equals(tarAnns[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 构造方法描述信息
     */
    private MethodDescriptor buildDescriptor(Class<?> apiClazz,
                                             Method apiMethod, Method ifaceMethod) {
        Class<?> methodClass = apiMethod.getDeclaringClass();
        // 参数的目标是实现类的参数
        Type[] apiTypes = apiMethod.getGenericParameterTypes();
        Type[] ifaceTypes = ifaceMethod.getGenericParameterTypes();
        JavaType[] paramTypes = new JavaType[apiTypes.length];
        for (int i = 0; i < apiTypes.length; i++) {
            paramTypes[i] = toJavaType(ifaceTypes[i], apiTypes[i], methodClass,
                    apiClazz);
        }
        // 返回值的目标是接口的返回值
        JavaType returnType = toJavaType(apiMethod.getGenericReturnType(),
                ifaceMethod.getGenericReturnType(),
                ifaceMethod.getDeclaringClass(), apiClazz);
        return new MethodDescriptor(apiMethod, paramTypes, returnType);
    }

    /**
     * 转换成ObjectMapper需要的JavaType类型
     */
    private JavaType toJavaType(Type srcType, Type tarType,
                                Class<?> tarClass, Class<?> apiClazz) {
        if (tarType.equals(srcType)) {
            return null;
        }
        return JsonUtil.getMapper().constructType(
                PluginReflectUtil.parameterize(apiClazz, tarClass, tarType));
    }

    @AllArgsConstructor
    static class MethodDescriptor {
        /** 方法名 */
        Method method;
        /** 参数类型，null表示不需要转换 */
        JavaType[] paramTypes;
        /** 结果类型，null表示不需要转换 */
        JavaType returnType;
    }

    static class LocalInvocationHandler implements InvocationHandler {
        private Object api;
        /** key：接口方法，value：目标方法描述信息 */
        private Map<Method, MethodDescriptor> methodMap;

        public LocalInvocationHandler(Object api,
                                      Map<Method, MethodDescriptor> methodMap) {
            this.api = api;
            this.methodMap = methodMap;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] srcArgs)
                throws Throwable {
            MethodDescriptor desc = methodMap.get(method);
            // 转换参数和结果
            Object[] tarArgs = new Object[desc.paramTypes.length];
            for (int i = 0; i < desc.paramTypes.length; i++) {
                tarArgs[i] = JsonUtil.convert(srcArgs[i], desc.paramTypes[i]);
            }
            return JsonUtil.convert(desc.method.invoke(api, tarArgs),
                    desc.returnType);
        }

    }

}
