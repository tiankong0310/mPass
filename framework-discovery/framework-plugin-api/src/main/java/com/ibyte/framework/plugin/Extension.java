package com.ibyte.framework.plugin;

import java.lang.annotation.ElementType;

/**
 * 扩展
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public interface Extension {
	/**
	 * ID，若扩展点未定义ID，则使用注解所在类类名
	 * 
	 * @return
	 */
	String getId();

	/**
	 * 中文名
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * 国际化标识
	 * 
	 * @return
	 */
	String getMessageKey();

	/**
	 * 模块
	 * 
	 * @return
	 */
	String getModule();

	/**
	 * 排序号，小的在前面
	 * 
	 * @return
	 */
	Integer getOrder();

	/**
	 * 扩展作用的类名
	 * 
	 * @return
	 */
	String getRefName();

	/**
	 * 扩展作用的成员
	 * 
	 * @return
	 */
	String getElementName();

	/**
	 * 扩展作用点类型
	 * 
	 * @return
	 */
	ElementType getElementType();

	/**
	 * Provider，若扩展点定义了config，则返回config的实体，否则返回作用类的实体
	 * 
	 * @return
	 */
	<T> T getProvider();
}
