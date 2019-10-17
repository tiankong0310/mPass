package com.ibyte.framework.support.domain;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: <设计元数据>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
@Setter
@ToString
public class MetaEntityImpl implements MetaEntity {
    /** 模块，样例：km-review */
    private String module;

    /** VO类名 */
    private String voName;

    /** Entity类名 */
    private String entityName;

    /** 中文名 */
    private String label;

    /** 国际化Key */
    private String messageKey;

    /** 显示属性 */
    private String displayProperty;

    /** 版本属性 */
    private String versionProperty;

    /** api */
    private String apiName;

    /** 相关字段 */
    @JsonDeserialize(contentAs = MetaPropertyImpl.class)
    private Map<String, MetaProperty> properties = new LinkedHashMap<>(16);

    /** Entity的特征 */
    @JsonSerialize(using = ClassKeyMap.Serializer.class)
    @JsonDeserialize(using = ClassKeyMap.Deserializer.class)
    private Map<String, Object> features = new HashMap<>(8);

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getVoName() {
        return voName;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getDisplayProperty() {
        return displayProperty;
    }

    @Override
    public String getVersionProperty() {
        return versionProperty;
    }

    @Override
    public String getApiName() {
        return apiName;
    }

    @Override
    public MetaProperty getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, MetaProperty> getProperties() {
        return properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(Class<T> clazz) {
        Object feature = features.get(clazz.getName());
        if (feature != null && feature instanceof JSONObject
                && clazz != JSONObject.class) {
            return (T) JsonUtil.convert(feature, clazz);
        }
        return (T) feature;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }
}
