package com.ibyte.framework.support.persistent;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.framework.support.domain.*;
import lombok.Getter;

/**
 * 持久化相关常量表
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public interface PersistentConstant {
	/**
	 * 设计元素类型
	 */
	public enum ElementType {
		/** 应用 */
		MetaApplication(MetaApplicationImpl.class),
		/** 模块 */
		MetaModule(MetaModuleImpl.class),
		/** 表 */
		MetaEntity(MetaEntityImpl.class),
		/** 扩展表 */
		ExtEntity(MetaEntityImpl.class),
		/** 扩展点 */
		ExtensionPoint(ExtensionPointImpl.class),
		/** 扩展 */
		Extension(ExtensionImpl.class),
		/** 远程API */
		RemoteApi(RemoteApi.class);

		@Getter
		private Class<?> impl;

		ElementType(Class<?> impl) {
			this.impl = impl;
		}
	}

	/** 路径分隔符 */
	String PATH_SPLIT = ":";

	/** ID的长度 */
	int ID_LEN = 320;

	/** 空的JSON */
	String JSON_EMPTY = "{}";
	/** id */
	String PROP_ID = "id";
	/** label */
	String PROP_LABEL = "label";
	/** md5 */
	String PROP_MD5 = "md5";
	/** messageKey */
	String PROP_MESSAGEKEY = "messageKey";
	/** module */
	String PROP_MODULE = "module";

	/** 应用配置KEY的前缀 */
	String CONFIG_PREFIX = "Profile:";

	/** 扩展点更新订阅主题 */
	String EXTENSIONPOINT_CHANGE_TOPIC = "ExtensionPoint:Changed:Topic";
	String EXTENSIONPOINT_CHANGE_LOCK = "ExtensionPoint:Changed:Lock:";

	/**
	 * 构造ID
	 * 
	 * @param type
	 * @param paths
	 * @return
	 */
	static String toId(ElementType type, String... paths) {
		StringBuilder sb = new StringBuilder(type.name());
		for (String path : paths) {
			sb.append(PATH_SPLIT).append(NamingConstant.shortName(path));
		}
		if (sb.length() > ID_LEN) {
			throw new ParamsNotValidException("配置ID长度不能超过320个字符");
		}
		return sb.toString();
	}

	/**
	 * 根据ID还原
	 * 
	 * @param id
	 * @return
	 */
	static String fromId(String id) {
		String result = id.substring(id.indexOf(PATH_SPLIT) + 1);
		return NamingConstant.oriName(result);
	}
}
