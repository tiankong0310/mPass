package com.ibyte.framework.support.domain;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.framework.meta.EnumItem;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import com.ibyte.framework.meta.MetaProperty;
import com.ibyte.framework.serializer.ClassKeyMap;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: <元数据字段>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Setter
@ToString
public class MetaPropertyImpl implements MetaProperty {
    /** 字段名 */
    private String name;

    /** 中文名 */
    private String label;

    /** 国际化Key */
    private String messageKey;

    /** 字段类型 */
    private String type;

    /** 级联风格 */
    private String cascade;

    /** 延迟加载 */
    private boolean lazy;

    /** 创建人 */
    private String mappedBy;

    /** 是否数组 */
    private boolean collection;

    /** 非空 */
    private boolean notNull;

    /** 只读 */
    private boolean readOnly;

    /**支持多语言*/
    private boolean langSupport;

    /** 长度 */
    private int length;

    /** 精度 */
    private int precision;
    private int scale;

    /** 枚举 */
    private String enumClass;

    /** 属性 */
    private List<String> voProperties;

    /** 显示类型 */
    private MetaConstant.ShowType showType;

    /** 是否动态属性 */
    private boolean dynamic;

    /** 属性的特征 */
    @JsonSerialize(using = ClassKeyMap.Serializer.class)
    @JsonDeserialize(using = ClassKeyMap.Deserializer.class)
    private Map<String, Object> features = new HashMap<>(8);

    @Override
    public String getName() {
        return name;
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
    public String getType() {
        return type;
    }

    @Override
    public String getCascade() {
        return cascade;
    }

    @Override
    public boolean isLazy() {
        return lazy;
    }

    @Override
    public String getMappedBy() {
        return mappedBy;
    }

    @Override
    public boolean isCollection() {
        return collection;
    }

    @Override
    public boolean isNotNull() {
        return notNull;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isLangSupport() {
        return langSupport;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getPrecision() {
        return precision;
    }

    @Override
    public int getScale() {
        return scale;
    }

    @Override
    public String getEnumClass() {
        return enumClass;
    }

    @Override
    public List<EnumItem> getEnumList() {
        return null;
    }

    @Override
    public List<String> getVoProperties() {
        return voProperties;
    }

    @Override
    public ShowType getShowType() {
        return showType;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
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
