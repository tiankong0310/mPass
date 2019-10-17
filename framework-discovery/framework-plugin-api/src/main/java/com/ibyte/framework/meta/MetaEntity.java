package com.ibyte.framework.meta;

import com.ibyte.framework.support.LocalMetaContextHolder;

import java.util.Map;

/**
 * @Description: <设计元数据>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public interface MetaEntity {

    /**
     * 取本地的Entity
     *
     * @param entityName
     * @return
     */
    public static MetaEntity localEntity(String entityName) {
        return LocalMetaContextHolder.get().getEntity(entityName);
    }

    /**
     * 读-模块路径，如：km-review
     *
     * @return
     */
    String getModule();

    /**
     * 读-VO类名
     *
     * @return
     */
    String getVoName();

    /**
     * 读-Entity类名
     *
     * @return
     */
    String getEntityName();

    /**
     * 读-中文名
     *
     * @return
     */
    String getLabel();

    /**
     * 读-MessageKey
     *
     * @return
     */
    String getMessageKey();

    /**
     * 读-显示字段
     *
     * @return
     */
    String getDisplayProperty();

    /**
     * 读-版本字段
     *
     * @return
     */
    String getVersionProperty();

    /**
     * 读-API类名
     *
     * @return
     */
    String getApiName();

    /**
     * 读-属性列表
     *
     * @return
     */
    Map<String, MetaProperty> getProperties();

    /**
     * 读-属性
     *
     * @param name
     * @return
     */
    MetaProperty getProperty(String name);

    /**
     * 取Entity的特征
     *
     * @param clazz
     * @return
     */
    <T> T getFeature(Class<T> clazz);

}
