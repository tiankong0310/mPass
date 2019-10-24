package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;

/**
 * Controller的最底层接口
 * 
 * @author ibyte
 *
 * @param <A>
 * @param <V>
 */
public interface IController<A extends IApi<V>, V extends IViewObject> {
	/**
	 * 返回API的实现
	 * 
	 * @return
	 */
	A getApi();

	/**
	 * 取Entity的名称
	 * 
	 * @return
	 */
	String getEntityName();
}
