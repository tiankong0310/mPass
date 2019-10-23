package com.ibyte.framework.support.domain;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ibyte.framework.plugin.Extension;
import com.ibyte.framework.support.builder.ProviderBuilder;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.annotation.ElementType;

/**
 * 扩展
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Setter
@Getter
@ToString
public class ExtensionImpl implements Extension, Comparable<ExtensionImpl> {
	/** 模块 */
	private String module;

	/** 配置信息 */
	private JSONObject config;

	/** 扩展作用的类名 */
	private String refName;

	/** FIELD/METHOD的名称 */
	private String elementName;

	/** 扩展作用的类名 */
	private ElementType elementType;


	// ========== 以下属性不持久化 ==========
	/** 扩展作用的类 */
	@JsonIgnore
	private Class<?> refClass;

	@JsonIgnore
	private Object provider;

	@JsonIgnore
	private ExtensionPointImpl point;

	// ========== 特殊的getter/setter ==========

	public void setRefName(String refName) {
		this.refName = refName;
		if (refName != null) {
			this.refClass = PluginReflectUtil.classForName(refName);
		} else {
			this.refClass = null;
		}
	}

	public void setRefClass(Class<?> refClass) {
		this.refClass = refClass;
		if (refClass != null) {
			this.refName = refClass.getName();
		} else {
			this.refName = null;
		}
	}

	@JsonIgnore
	@Override
	public String getId() {
		if (config != null) {
			return config.getString("id");
		}
		return null;
	}

	// label和messageKey在持久化时作为摘要信息，因此存双份，不打@JsonIgnore

	@Override
	public String getLabel() {
		if (config != null) {
			return config.getString("label");
		}
		return null;
	}

	@Override
	public String getMessageKey() {
		if (config != null) {
			return config.getString("messageKey");
		}
		return null;
	}

	@Override
	public String getModule() {
		return module;
	}

	@JsonIgnore
	@Override
	public Integer getOrder() {
		if (config != null) {
			return config.getInteger("order");
		}
		return null;
	}

	@Override
	public String getRefName() {
		return refName;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProvider() {
		if (provider == null) {
			// 构造Provider
			provider = ProviderBuilder.buildProvider(this);
		}
		return (T) provider;
	}

	@Override
	public int compareTo(ExtensionImpl o) {
		Integer thisOrder = getOrder();
		Integer thatOrder = o.getOrder();
		if (thisOrder == null) {
			if (thatOrder == null) {
				return 0;
			}
			return 1;
		} else {
			if (thatOrder == null) {
				return -1;
			}
			return thisOrder.compareTo(thatOrder);
		}
	}
}
