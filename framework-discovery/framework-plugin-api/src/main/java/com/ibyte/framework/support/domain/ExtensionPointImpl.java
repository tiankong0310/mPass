package com.ibyte.framework.support.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ibyte.framework.plugin.ExtensionPoint;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * 扩展点
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Getter
@Setter
@JsonInclude
public class ExtensionPointImpl implements ExtensionPoint {
	/** ID，取注解类名 */
	private String id;

	/** 中文名 */
	private String label;

	/** 模块 */
	private String module;

	/** 是否全局扩展点 */
	private boolean global;

	/** 是否可配置 */
	private boolean configurable;

	/** 是否有序 */
	private boolean ordered;

	/** 是否单例 */
	private boolean singleton;

	/** 配置类名 */
	private String config;

	/** 注解所在类接口名 */
	private String baseOn;

	// ========== 以下属性不持久化 ==========
	/** 配置类 */
	@JsonIgnore
	private Class<?> configClass;

	/** 注解所在类接口 */
	@JsonIgnore
	private Class<?> baseOnClass;

	/** 扩展点涵盖的所有注解类型，相当于扩展点的继承关系 */
	@JsonIgnore
	private List<String> annotationTypes;

	/** 扩展管理器 */
	@JsonIgnore
	private Class<?> manager;

	/** 插件更新监听器 */
	@JsonIgnore
	private String listener;

	/** config类中，写入注解所在类信息的属性 */
	@JsonIgnore
	private AnnotatedElement baseOnProperty;

	/** manager类总，写入config信息的属性 */
	@JsonIgnore
	private AnnotatedElement providerProperty;

	// ========== 特殊的setter ==========

	public void setConfig(String config) {
		this.config = config;
		if (config != null) {
			this.configClass = PluginReflectUtil.classForName(config);
		} else {
			this.configClass = null;
		}
	}

	public void setConfigClass(Class<?> configClass) {
		this.configClass = configClass;
		if (configClass != null) {
			this.config = configClass.getName();
		} else {
			this.config = null;
		}
	}

	public void setBaseOn(String baseOn) {
		this.baseOn = baseOn;
		if (baseOn != null) {
			this.baseOnClass = PluginReflectUtil.classForName(baseOn);
		} else {
			this.baseOnClass = null;
		}
	}

	public void setBaseOnClass(Class<?> baseOnClass) {
		this.baseOnClass = baseOnClass;
		if (baseOnClass != null) {
			this.baseOn = baseOnClass.getName();
		} else {
			this.baseOn = null;
		}
	}

	// ========== 接口的getter ==========

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getModule() {
		return module;
	}

	@Override
	public boolean isConfigurable() {
		return configurable;
	}

	@Override
	public boolean isOrdered() {
		return ordered;
	}

	@Override
	public boolean isSingleton() {
		return singleton;
	}

	@Override
	public String getConfig() {
		return config;
	}

	@Override
	public String getBaseOn() {
		return baseOn;
	}
}
