package com.ibyte.common.core.util;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.data.IData;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.service.IService;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.*;
import com.ibyte.framework.plugin.Plugin;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.*;

/**
 * 数据库实体工具类
 *
 * @author li.shangzhi
 *
 */
public class EntityUtil {
    static final String HIBERNATE_FEATURE = "$HibernateProxy$";
    static final Map<String, Class<?>> TYPEMAP = new HashMap<>();
    static {
        TYPEMAP.put(MetaConstant.TYPE_BIGDECIMAL, BigDecimal.class);
        TYPEMAP.put(MetaConstant.TYPE_BLOB, byte[].class);
        TYPEMAP.put(MetaConstant.TYPE_BOOLEAN, Boolean.class);
        TYPEMAP.put(MetaConstant.TYPE_DATE, Date.class);
        TYPEMAP.put(MetaConstant.TYPE_DATETIME, Date.class);
        TYPEMAP.put(MetaConstant.TYPE_DOUBLE, Double.class);
        TYPEMAP.put(MetaConstant.TYPE_FLOAT, Float.class);
        TYPEMAP.put(MetaConstant.TYPE_SHORT, Short.class);
        TYPEMAP.put(MetaConstant.TYPE_INTEGER, Integer.class);
        TYPEMAP.put(MetaConstant.TYPE_LONG, Long.class);
        TYPEMAP.put(MetaConstant.TYPE_RTF, String.class);
        TYPEMAP.put(MetaConstant.TYPE_STRING, String.class);
        TYPEMAP.put(MetaConstant.TYPE_TIME, Date.class);
    }

    /** 获取实际类型 */
    public static Class<?> getPropertyType(String type) {
        Class<?> clazz = TYPEMAP.get(type);
        if (clazz == null) {
            int i = type.indexOf(HIBERNATE_FEATURE);
            if (i > -1) {
                return ReflectUtil.classForName(type.substring(0, i));
            } else {
                return ReflectUtil.classForName(type);
            }
        }
        return clazz;
    }

    /** 判断是否是基础类型 */
    public static boolean isBaseType(String type) {
        return TYPEMAP.containsKey(type);
    }

    /**
     * 获取实体类名（处理hibernate的延迟加载）
     *
     * @param entity
     * @return
     */
    public static String getEntityClassName(Object entity) {
        if (entity instanceof String) {
            return (String) entity;
        }
        return Hibernate.getClass(entity).getName();
    }

    /**
     * 返回HQL语句中用于查询使用的表名，如：sysOrgElement
     *
     * @param mainModel
     * @return
     */
    public static String getEntityTableName(Object mainModel) {
        String rtnVal = getEntityClassName(mainModel);
        int i = rtnVal.lastIndexOf('.');
        if (i > -1) {
            rtnVal = rtnVal.substring(i + 1);
        }
        return rtnVal.substring(0, 1).toLowerCase() + rtnVal.substring(1);
    }

    /**
     * 获取数据字典属性，property为a.b.c的格式
     */
    public static List<MetaProperty> getMetaProperty(MetaEntity entity,
                                                     String propertyName) {
        String[] names = propertyName.split("\\.");
        List<MetaProperty> result = new ArrayList<>(names.length);
        MetaEntity meta = entity;
        for (int i = 0; i < names.length;) {
            String name = names[i];
            String entityName = meta.getEntityName();
            MetaProperty prop = meta.getProperty(name);
            if (prop == null) {
                throw new ParamsNotValidException(StringHelper
                        .join(entityName, "中不存在属性：", name));
            }
            result.add(prop);
            i++;
            if (i < names.length) {
                meta = Meta.getEntity(prop.getType());
                if (meta == null) {
                    throw new ParamsNotValidException(StringHelper
                            .join(entityName, "的", name, "属性对应的数据字典不存在"));
                }
            }
        }
        return result;
    }

    /**
     * 获取数据字典属性，property为a.b.c的格式
     */
    public static List<MetaProperty> getMetaProperty(String entityName,
                                                     String propertyName) {
        return getMetaProperty(Meta.getEntity(entityName),
                propertyName);
    }

    /**
     * 获取entityName对应的service
     */
    public static IService<?, ?> getEntityService(String entityName) {
        return getEntityService(Meta.getEntity(entityName));
    }

    /**
     * 获取entity对应的service
     */
    public static IService<?, ?> getEntityService(MetaEntity entity) {
        IApi<?> api = getEntityApi(entity);
        if (api != null && api instanceof IService) {
            return (IService<?, ?>) api;
        }
        return null;
    }

    /**
     * 获取entity对应的api
     */
    public static IApi<?> getEntityApi(MetaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Plugin.getApi(entity.getApiName());
    }

    /**
     * 添加机制信息
     */
    public static void appendMechanism(IData data, String key, Object value) {
        Map<String, Object> map = data.getMechanisms();
        if (map == null) {
            map = new HashMap<>(16);
            data.setMechanisms(map);
        }
        map.put(key, value);
    }

    /**
     * 添加动态属性
     */
    public static void appendDynamicProp(IEntity data, String key,
                                         Object value) {
        Map<String, Object> map = data.getDynamicProps();
        if (map == null) {
            map = new HashMap<>(16);
            data.setDynamicProps(map);
        }
        map.put(key, value);
    }

    /**
     * 获取entity对应的URL
     */
    public static String getEntityUrl(IEntity entity) {
        String entityName = getEntityClassName(entity);
        return getEntityUrl(entityName,entity.getFdId());
    }

    /**
     * 根据Name和Id获取entity对应的URL
     */
    public static String getEntityUrl(String entityName,String entityId) {
        String shortName = StringUtils.uncapitalize(entityName
                .substring(entityName.lastIndexOf('.') + 1));
        MetaEntity meta = Meta.getEntity(entityName);
        return StringHelper.join("/index.html#/current/", meta.getModule(),
                "/", shortName, "/view/", entityId);
    }

    /**
     * 根据模块名和类名获取完整类名
     */
    public static String getEntityClass(String moduleName, String entityName) {
        String entityClass = "";
        List<MetaSummary> mss = Meta.getEntities(moduleName);
        for(MetaSummary ms : mss) {
            if(ms.getId().endsWith(StringHelper.toFirstUpperCase(entityName))) {
                entityClass = ms.getId();
            }
        }
        return entityClass;
    }

    /**
     * 获取entity的显示值
     */
    public static Object getDisplayValue(IEntity entity)
            throws ReflectiveOperationException {
        MetaEntity meta = Meta.getEntity(getEntityClassName(entity));
        if (meta.getDisplayProperty() == null) {
            return null;
        }
        MetaProperty prop = meta.getProperty(meta.getDisplayProperty());
        if (prop == null) {
            return null;
        }
        if (prop.isDynamic()) {
            if (entity.getDynamicProps() == null) {
                return null;
            }
            return entity.getDynamicProps().get(prop.getName());
        }
        return ReflectUtil.getProperty(entity, prop.getName());
    }
}
