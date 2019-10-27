package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * controller基类
 *
 * @author ibyte
 *
 * @param <A>
 * @param <V>
 */
public abstract class AbstractController<A extends IApi<V>, V extends IViewObject>
		implements IController<A, V> {
	@Autowired
	protected A api;

	protected String entityName;

	@Override
	public A getApi() {
		return api;
	}

	@Override
	public String getEntityName() {
		if (entityName == null) {
			entityName = getApi().getEntityName();
		}
		return entityName;
	}
}
