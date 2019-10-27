package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.dto.QueryResult;
import com.ibyte.common.dto.Response;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 查询Controller
 *
 * @author ibyte
 */
public interface ListController<A extends IApi<V>, V extends IViewObject>
		extends IController<A, V> {
	/**
	 * 列表页数据查询
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("list")
	@ApiOperation("列表查询接口")
	default Response<QueryResult<V>> list(@RequestBody QueryRequest request) {
		return Response.ok(getApi().findAll(request));
	}
}
