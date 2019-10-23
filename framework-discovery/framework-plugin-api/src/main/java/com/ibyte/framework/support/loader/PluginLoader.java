package com.ibyte.framework.support.loader;
import static com.ibyte.common.constant.NamingConstant.BASE_PACKAGE;
import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.plugin.annotation.ExtendByJson;
import com.ibyte.framework.plugin.annotation.GlobalExtensionPoint;
import com.ibyte.framework.plugin.annotation.ListenerConfig;
import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;
import com.ibyte.framework.support.PluginContextHolder;
import com.ibyte.framework.support.builder.ExtensionBuilder;
import com.ibyte.framework.support.builder.ExtensionPointBuilder;
import com.ibyte.framework.support.builder.ProviderBuilder;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.listener.ClassContext;
import com.ibyte.framework.support.listener.PluginListenerManager;
import com.ibyte.framework.support.persistent.DesignElementApi;
import com.ibyte.framework.support.persistent.PersistentConstant;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    /**
     * 加载插件
     */
    @SuppressWarnings("unchecked")
    public void load() {
        List<Class<?>> clazzList = new ArrayList<>();
        LinkedList<Class<?>> stack = new LinkedList<>();
        // 加载扩展点
        Reflections reflections = new Reflections(NamingConstant.BASE_PACKAGE,
                new SubTypesScanner(false));
        for (String className : reflections.getAllTypes()) {
            Class<?> clazz = PluginReflectUtil.classForName(className);
            if (clazz.isAnnotation()) {
                findExtensionPoint((Class<? extends Annotation>) clazz, stack);
            } else {
                clazzList.add(clazz);
            }
        }
        // 加载扩展
        listenerManager.prepare();
        for (Class<?> clazz : clazzList) {
            findExtension(clazz);
        }
    }

    /**
     * 保存插件
     */
    public void save() {
        listenerManager.save();
        // 扩展点在本地的
        for (ExtensionPointImpl point : extensionPoints.values()) {
            storeExtensionPoint(point);
            Map<String, ExtensionImpl> map = extensions.get(point.getId());
            if (map == null || map.isEmpty()) {
                continue;
            }
            extensions.remove(point.getId());
            if (point.isGlobal()) {
                // 全局扩展，存储扩展点信息，本地模块持久化
                storeGlobalExtension(point.getId(), map.values());
            } else {
                // 本地扩展，写入管理器或上下文
                storeLocalExtension(point, new ArrayList<>(map.values()));
            }
        }
        // 扩展点不在本地的
        for (Map.Entry<String, Map<String, ExtensionImpl>> entry : extensions
                .entrySet()) {
            storeGlobalExtension(entry.getKey(), entry.getValue().values());
        }

        // 保存到上下文
        PluginContextHolder.setLocalExtensionByPointId(localExtensionByPointId);
        PluginContextHolder
                .setExtensionPointByProvider(extensionPointByProvider);
        PluginContextHolder.setExtensionPoints(extensionPoints);

        // 持久化扩展和扩展点
        DesignElementApi.get().saveExtensionPoints(extensionPoints4Save);
        DesignElementApi.get().saveExtensions(extensions4Save);
    }

    /**
     * 由于注解无继承关系，因此这里的设计为：A注解上打上B注解，就视为A注解继承了B注解。
     * 由此循环遍历，当注解“继承”了ExtensionPoint，就视为ExtensionPoint。
     * 注解的注解规避不了循环嵌套的问题，因此采用stack记录堆栈，避免死循环。
     */
    private ExtensionPointImpl findExtensionPoint(
            Class<? extends Annotation> clazz, LinkedList<Class<?>> stack) {
        // 扩展点找到过，直接跳过
        ExtensionPointImpl point = extensionPoints.get(clazz.getName());
        if (point != null) {
            return point;
        }
        // 扩展点的没有注解，跳过
        Annotation[] annotations = AnnotationUtils.getAnnotations(clazz);
        if (annotations == null || annotations.length == 0) {
            return null;
        }
        // 记录循环层级
        stack.add(clazz);
        try {
            List<String> types = new ArrayList<>();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ListenerConfig) {
                    // 监听器注册
                    listenerManager.addListener((ListenerConfig) annotation);
                } else if (annotation instanceof LocalExtensionPoint) {
                    // 有LocalExtensionPoint的注解，说明clazz就是扩展点，读取扩展点信息
                    point = ExtensionPointBuilder.newExtensionPoint(
                            clazz.getName(), (LocalExtensionPoint) annotation);
                    listenerManager.addListener(
                            (LocalExtensionPoint) annotation, clazz);
                } else if (annotation instanceof GlobalExtensionPoint) {
                    // 有GlobalExtensionPoint的注解，说明clazz就是扩展点，读取扩展点信息
                    point = ExtensionPointBuilder.newExtensionPoint(
                            clazz.getName(), (GlobalExtensionPoint) annotation);
                    listenerManager.addListener(
                            (GlobalExtensionPoint) annotation, clazz);
                } else {
                    // 若stack包含了注解类，说明递归循环嵌套了，直接忽略
                    Class<? extends Annotation> annotationType = annotation
                            .annotationType();
                    if (!annotationType.getName().startsWith(BASE_PACKAGE)
                            || stack.contains(annotationType)) {
                        continue;
                    }
                    ExtensionPointImpl superPoint = findExtensionPoint(
                            annotationType, stack);
                    // 注解的注解是扩展点，则注解也是扩展点，复制父类属性，再用当前注解信息覆盖父类信息。
                    if (superPoint == null) {
                        continue;
                    }
                    if (point == null) {
                        point = ExtensionPointBuilder.newExtensionPoint(
                                clazz.getName(), superPoint, annotation);
                    }
                    // 继承父类的关系
                    types.addAll(superPoint.getAnnotationTypes());
                }
            }
            // 设置关联关系并记录到extensionPoints
            if (point != null) {
                types.add(clazz.getName());
                point.setAnnotationTypes(types);
                extensionPoints.put(clazz.getName(), point);
            }
        } catch (Exception e) {
            log.error("加载扩展点信息时发生错误：" + clazz.getName(), e);
        }
        // 循环层级退出
        stack.removeLast();
        return point;
    }

    /** 查找并加载扩展信息 */
    private void findExtension(Class<?> clazz) {
        Annotation[] annotations = AnnotationUtils.getAnnotations(clazz);
        if (annotations == null || annotations.length == 0) {
            return;
        }
        ClassContext context = new ClassContext(clazz);
        for (Annotation annotation : annotations) {
            try {
                if (annotation instanceof ExtendByJson) {
                    // 注解是非依赖的扩展声明
                    ExtendByJson config = (ExtendByJson) annotation;
                    String content = IOUtils.resourceToString(config.value(),
                            StandardCharsets.UTF_8);
                    JSONObject json = JSONObject.parseObject(content);
                    JSONObject jPoint = json.getJSONObject("extensionPoint");
                    JSONObject jExtension = json.getJSONObject("extension");
                    ExtensionPointImpl point = extensionPoints
                            .get(jPoint.getString("id"));
                    if (point == null) {
                        point = ExtensionPointBuilder
                                .newVirtualExtensionPoint(jPoint);
                    }
                    context.addExtension(ExtensionBuilder.newExtension(point,
                            clazz, jExtension));
                } else {
                    // 注解可能是扩展点
                    ExtensionPointImpl point = extensionPoints
                            .get(annotation.annotationType().getName());
                    if (point != null) {
                        context.addExtension(ExtensionBuilder.newExtension(
                                point, clazz, annotation));
                    }
                }
            } catch (Exception e) {
                log.error("加载扩展信息时发生错误：" + clazz.getName(), e);
            }
        }
        // 触发监听
        listenerManager.onClass(context, extensionPoints);
        // 注册到扩展点
        flushToExtensionPoint(context);
    }

    /** 将类上下文的扩展信息注册到扩展点 */
    private void flushToExtensionPoint(ClassContext context) {
        for (ExtensionImpl extension : context.getExtensions()) {
            Map<String, ExtensionImpl> map = extensions
                    .get(extension.getPoint().getId());
            if (map == null) {
                map = new HashMap<>(16);
                extensions.put(extension.getPoint().getId(), map);
            }
            if (map.containsKey(extension.getId())) {
                String message = "扩展的ID重复，自动抛弃其中一个，扩展点："
                        + extension.getPoint().getId() + "，扩展："
                        + extension.getId();
                log.error(message);
            } else {
                map.put(extension.getId(), extension);
            }
        }
    }

    /** 将扩展信息保存到本地 */
    private void storeLocalExtension(ExtensionPointImpl point,
                                     List<ExtensionImpl> extensions) {
        if (point.isOrdered()) {
            Collections.sort(extensions);
        }
        try {
            if (point.getManager() == null) {
                localExtensionByPointId.put(point.getId(),
                        Collections.unmodifiableList(extensions));
            } else {
                ProviderBuilder.writeToManager(point, extensions);
            }
        } catch (Exception e) {
            log.error("写入扩展信息时发生错误：" + point.getId(), e);
        }
    }

    /** 添加扩展点 */
    private void storeExtensionPoint(ExtensionPointImpl point) {
        Class<?> type = ProviderBuilder.getProviderType(point);
        if (type == null) {
            return;
        }
        extensionPointByProvider.put(type, point);
        if (point.isGlobal() && point.getModule() != null) {
            extensionPoints4Save.put(point.getId(), point);
        }
    }

    /** 添加全局扩展 */
    private void storeGlobalExtension(String pointId,
                                      Collection<ExtensionImpl> extensionList) {
        for (ExtensionImpl extension : extensionList) {
            if (extension.getModule() != null) {
                String id = StringHelper.join(pointId,
                        PersistentConstant.PATH_SPLIT,
                        NamingConstant.shortName(extension.getId()));
                extensions4Save.put(id, extension);
            }
        }
    }



}
