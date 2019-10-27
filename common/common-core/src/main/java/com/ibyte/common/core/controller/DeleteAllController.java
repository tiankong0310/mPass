package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.dto.IdsDTO;
import com.ibyte.common.dto.Response;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 批量删除Controller
 *
 * @author ibyte
 */
public interface DeleteAllController<A extends IApi<V>, V extends IViewObject>
		extends IController<A, V> {
	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@PostMapping("deleteAll")
	@ApiOperation("批量删除接口")
	default Response<?> deleteAll(@RequestBody IdsDTO ids) {
		getApi().deleteAll(ids);
		return Response.ok();
	}
}
