package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;

/**
 * 所有常用的Controller组合
 *
 * @author ibyte
 *
 */
public interface CombineController<A extends IApi<V>, V extends IViewObject>
		extends MetaController<A, V>, CrudController<A, V>,
		DeleteAllController<A, V>, ListController<A, V> {

}
