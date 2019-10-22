package com.ibyte.framework.support.listener.impl;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import com.ibyte.framework.meta.annotation.Feature;
import com.ibyte.framework.meta.annotation.MetaEntity;
import com.ibyte.framework.meta.annotation.MetaProperty;
import com.ibyte.framework.plugin.spi.PluginListener;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.domain.MetaEntityImpl;
import com.ibyte.framework.support.domain.MetaPropertyImpl;
import com.ibyte.framework.support.listener.ClassContext;
import com.ibyte.framework.support.listener.MemberContext;
import com.ibyte.framework.support.persistent.DesignElementApi;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity信息扫描填充
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Slf4j
public class MetaEntityScanListener implements PluginListener {
    private static final char DOT = '.';
    private static final String[] DISPLAY_PROPERTY = {"fdName", "fdSubject"};
    private static final String SERVICE_CLASS = "com.ibyte.common.core.service.IService";

    @Override
    public void onClassStart(ClassContext clazz) {
        MetaEntity ann = clazz.getAnnotation(MetaEntity.class);
        List<ExtensionImpl> extensions = clazz
                .getAndRemoveExtensions(Feature.class.getName());
        if (ann == null && extensions.isEmpty()) {
            return;
        }
        // 找类对应的Entity
        MetaEntityImpl entity = (MetaEntityImpl) LocalMetaContextHolder.get()
                .getOrCreateEntity(clazz.getRefClass().getName());
        // 填充注解
        if (ann != null) {
            fillEntityAnnotation(entity, ann);
        }
        // 填充Feature
        for (ExtensionImpl extension : extensions) {
            addToFeature(entity.getFeatures(), extension);
        }
    }

    @Override
    public void onMember(ClassContext clazz, MemberContext member) {
        MetaProperty ann = member.getAnnotation(MetaProperty.class);
        List<ExtensionImpl> extensions = member
                .getAndRemoveExtensions(Feature.class.getName());
        if (ann == null && extensions.isEmpty()) {
            return;
        }
        // 查找对应的entity、property
        String name = member.getPropertyName();
        if (name == null) {
            return;
        }
        MetaEntityImpl entity = (MetaEntityImpl) LocalMetaContextHolder.get()
                .getOrCreateEntity(clazz.getRefClass().getName());
        MetaPropertyImpl property = (MetaPropertyImpl) entity.getProperty(name);
        if (property == null) {
            property = new MetaPropertyImpl();
            property.setName(name);
            entity.getProperties().put(name, property);
        }
        // 填充注解
        if (ann != null) {
            fillPropertyAnnotation(property, ann);
        }
        // 填充Feature
        for (ExtensionImpl extension : extensions) {
            addToFeature(property.getFeatures(), extension);
        }
    }

    /** 填充feature信息 */
    private void addToFeature(Map<String, Object> features,
                              ExtensionImpl extension) {
        ExtensionPointImpl point = extension.getPoint();
        if (point.getConfig() == null) {
            return;
        }
        features.put(point.getConfig(), extension.getConfig());
    }

    @Override
    public void save() {
        // 查找service并填充
        fillService();
        // 填充默认值，然后放入持久化队列
        LocalMetaContextHolder context = LocalMetaContextHolder.get();
        Map<String, MetaEntityImpl> configs = new HashMap<>(
                context.getEntities().size());
        for (Object entity : context.getEntities().values()) {
            MetaEntityImpl entityImpl = (MetaEntityImpl) entity;
            fillEntityDefault(entityImpl);
            if (entityImpl.getModule() != null) {
                configs.put(entityImpl.getEntityName(), entityImpl);
            }
        }
        // 保存
        DesignElementApi.get().saveEntities(configs);
    }

    /** 将注解信息填充到Entity */
    private void fillEntityAnnotation(MetaEntityImpl entity, MetaEntity ann) {
        if (StringUtils.isNotBlank(ann.messageKey())) {
            entity.setMessageKey(ann.messageKey());
        }
        if (StringUtils.isNotBlank(ann.displayProperty())) {
            entity.setDisplayProperty(ann.displayProperty());
        }
        if (ann.voClass() != Void.class) {
            entity.setVoName(ann.voClass().getName());
        }
    }

    /** 将注解信息填充到Property */
    private void fillPropertyAnnotation(MetaPropertyImpl prop,
                                        MetaProperty ann) {
        if (StringUtils.isNotBlank(ann.messageKey())) {
            prop.setMessageKey(ann.messageKey());
        }
        prop.setNotNull(prop.isNotNull() || ann.notNull());
        prop.setReadOnly(prop.isReadOnly() || ann.readOnly());
        if (ann.showType() != ShowType.AUTO) {
            prop.setShowType(ann.showType());
        }
    }

    /** 将service信息填充到Entity */
    private void fillService() {
        Class<?> serviceClass = PluginReflectUtil.classForName(SERVICE_CLASS);
        if (serviceClass == null) {
            return;
        }
        ApplicationContext ctx = ApplicationContextHolder
                .getApplicationContext();
        String[] names = ctx.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = ctx.getBean(name);
            // System.out.println(name + " : " + bean.getClass().getName());
            if (serviceClass.isAssignableFrom(bean.getClass())) {
                try {
                    Class<?> entityClass = (Class<?>) invoke(bean,
                            "getEntityClass");
                    Class<?> voClass = (Class<?>) invoke(bean,
                            "getViewObjectClass");
                    MetaEntityImpl meta = (MetaEntityImpl) LocalMetaContextHolder
                            .get().getEntity(entityClass.getName());
                    if (meta != null) {
                        meta.setApiName(PluginReflectUtil
                                .getBeanClassName(bean.getClass().getName()));
                        meta.setVoName(voClass.getName());
                    }
                } catch (Exception e) {
                    log.error("加载Service信息失败", e);
                }
            }
        }
    }

    private Object invoke(Object bean, String getter)
            throws ReflectiveOperationException {
        Method method = bean.getClass().getMethod(getter);
        return method.invoke(bean, new Object[]{});
    }

    /** 自动填充Entity信息 */
    private void fillEntityDefault(MetaEntityImpl entity) {
        String simpleName = entity.getEntityName();
        int index = simpleName.lastIndexOf(DOT);
        if (index == -1) {
            return;
        }
        entity.setModule(LocalMetaContextHolder.get()
                .matchModule(entity.getEntityName()));
        // KmReviewMain -> kmReviewMain
        simpleName = StringUtils.uncapitalize(simpleName.substring(index + 1));
        // messageKey/label
        if (entity.getMessageKey() == null) {
            if (entity.getModule() != null) {
                String messageKey = StringHelper.join(entity.getModule(),
                        ":table.", simpleName);
                String label = ResourceUtil.getString(messageKey);
                if (label != null) {
                    entity.setMessageKey(messageKey);
                    entity.setLabel(label);
                }
            }
        } else {
            entity.setLabel(ResourceUtil.getString(entity.getMessageKey()));
        }
        // displayProperty
        if (entity.getDisplayProperty() == null) {
            for (String displayProperty : DISPLAY_PROPERTY) {
                if (entity.getProperty(displayProperty) != null) {
                    entity.setDisplayProperty(displayProperty);
                    break;
                }
            }
        }
        // feature
        tranFeatures(entity.getFeatures());
        // properties
        fillPropertyDefault(entity, simpleName);
    }

    /** 自动填充Property信息 */
    private void fillPropertyDefault(MetaEntityImpl entity, String simpleName) {
        Class<?> viewClass = StringUtils.isBlank(entity.getVoName()) ? null
                : PluginReflectUtil.classForName(entity.getVoName());
        for (Object prop : entity.getProperties().values()) {
            MetaPropertyImpl propImp = (MetaPropertyImpl) prop;
            // messageKey/label
            if (propImp.getMessageKey() == null) {
                if (entity.getModule() != null) {
                    String messageKey = StringHelper.join(entity.getModule(),
                            ':', simpleName, DOT, propImp.getName());
                    String label = ResourceUtil.getString(messageKey);
                    if (label != null) {
                        propImp.setMessageKey(messageKey);
                        propImp.setLabel(label);
                    }
                }
                if (propImp.getMessageKey() == null) {
                    String messageKey = StringHelper.join("entity.", simpleName,
                            DOT, propImp.getName());
                    String label = ResourceUtil.getString(messageKey);
                    if (label != null) {
                        propImp.setMessageKey(messageKey);
                        propImp.setLabel(label);
                    }
                }

            } else {
                propImp.setLabel(
                        ResourceUtil.getString(propImp.getMessageKey()));
            }
            // showType
            if (propImp.getShowType() == null
                    || propImp.getShowType() == ShowType.AUTO) {
                if (propImp.isLazy() || propImp.isCollection()
                        || MetaConstant.isLob(propImp.getType())) {
                    propImp.setShowType(ShowType.DETAIL_ONLY);
                } else {
                    propImp.setShowType(ShowType.ALWAYS);
                }
            }
            if (viewClass != null) {
                if (BeanUtils.getPropertyDescriptor(viewClass,
                        propImp.getName()) == null) {
                    propImp.setShowType(ShowType.NONE);
                }
            }
            // feature
            tranFeatures(propImp.getFeatures());
        }
    }

    /** 将Feature转换成实现类 */
    private void tranFeatures(Map<String, Object> features) {
        if (features.isEmpty()) {
            return;
        }
        List<String> keys = new ArrayList<>(features.keySet());
        for (String key : keys) {
            Class<?> clazz = PluginReflectUtil.classForName(key);
            if (clazz == null) {
                continue;
            }
            Object feature = features.get(key);
            if (feature instanceof JSONObject && clazz != JSONObject.class) {
                try {
                    features.put(key, JsonUtil.convert(feature, clazz));
                } catch (Exception e) {
                    log.error("无法装配Feature：" + key, e);
                }
            }
        }
    }
}
