package com.ibyte.common.core.data.handler;


import com.ibyte.common.core.data.IData;
import com.ibyte.framework.plugin.annotation.ProviderProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段处理管理器
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public class FieldHandlerManager {
	@ProviderProperty
	private static List<FieldHandler> handlers = new ArrayList<>();

	/**
	 * 字段初始化
	 */
	public static void doInit(IData entity) {
		List<FieldHandler> handlers = FieldHandlerManager.handlers;
		for (FieldHandler handler : handlers) {
			if (handler.support(entity)) {
				handler.doInit(entity);
			}
		}
	}

	/**
	 * 字段更新
	 */
	public static void beforeSaveOrUpdate(IData entity, boolean isAdd) {
		List<FieldHandler> handlers = FieldHandlerManager.handlers;
		for (FieldHandler handler : handlers) {
			if (handler.support(entity)) {
				handler.beforeSaveOrUpdate(entity, isAdd);
			}
		}
	}

	/**
	 * 字段更新
	 */
	public static void beforeVOUpdate(IData entity) {
		List<FieldHandler> handlers = FieldHandlerManager.handlers;
		for (FieldHandler handler : handlers) {
			if (handler.support(entity)) {
				handler.beforeVOUpdate(entity);
			}
		}
	}
}
