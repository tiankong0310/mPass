package com.ibyte.framework.meta;

import com.ibyte.framework.meta.MetaConstant.ShowType;

import java.util.List;

/**
 * 元数据字段
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public interface MetaProperty {
	/**
	 * 读-字段名
	 * 
	 * @return
	 */
	String getName();

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
	 * 读-类型
	 * 
	 * @return
	 */
	String getType();

	/**
	 * 读-级联风格
	 * 
	 * @return
	 */
	String getCascade();

	/**
	 * 读-是否延迟加载
	 * 
	 * @return
	 */
	boolean isLazy();

	/**
	 *  读-创建人
	 *
	 * @return
	 */
	String getMappedBy();

	/**
	 * 读-是否数组
	 * 
	 * @return
	 */
	boolean isCollection();

	/**
	 * 读-是否非空
	 * 
	 * @return
	 */
	boolean isNotNull();

	/**
	 * 读-是否只读
	 * 
	 * @return
	 */
	boolean isReadOnly();

	/**
	 * 读-是否支持多语言
	 *
	 * @return
	 */
	boolean isLangSupport();

	/**
	 * 读-字符串长度
	 * 
	 * @return
	 */
	int getLength();

	/**
	 * 读-数字精度
	 * 
	 * @return
	 */
	int getPrecision();

	/**
	 * 读-数字精度
	 * 
	 * @return
	 */
	int getScale();

	/**
	 * 读-枚举类
	 * 
	 * @return
	 */
	String getEnumClass();

    /**
     * 读-EnumList
     *
     * @return
     */
    List<EnumItem> getEnumList();

    /**
     * 读-VoProperties
     *
     * @return
     */
    List<String> getVoProperties();

	/**
	 * 读-显示类型
	 * 
	 * @return
	 */
	ShowType getShowType();

	/**
	 * 读-是否动态属性
	 * 
	 * @return
	 */
	boolean isDynamic();

	/**
	 * 取属性的特征
	 * 
	 * @param clazz
	 * @return
	 */
	<T> T getFeature(Class<T> clazz);
}