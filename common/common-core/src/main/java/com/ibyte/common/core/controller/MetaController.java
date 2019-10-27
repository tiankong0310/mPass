package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.dto.Response;
import com.ibyte.framework.meta.MetaEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 元数据读取Controller
 *
 * @author ibyte
 *
 * @param <A>
 * @param <V>
 */
public interface MetaController<A extends IApi<V>, V extends IViewObject>
		extends IController<A, V> {
	/**
	 * 读取数据字典
	 *
	 * @param
	 * @return
	 */
	@PostMapping("meta")
	@ApiOperation("元数据获取接口")
	default Response<Object> meta() {
		return Response.ok(MetaEntity.localEntity(getEntityName()));
	}
}
