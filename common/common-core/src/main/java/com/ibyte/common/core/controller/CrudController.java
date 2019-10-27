package com.ibyte.common.core.controller;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.dto.IdVO;
import com.ibyte.common.dto.Response;
import com.ibyte.common.exception.NoRecordException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

/**
 * 增删查改的Controller
 *
 * @author ibyte
 */
public interface CrudController<A extends IApi<V>, V extends IViewObject>
		extends IController<A, V> {
	/**
	 * 打开新建页，获取初始化数据
	 * 
	 * @param vo
	 * @return
	 */
	@PostMapping("init")
	@ApiOperation("初始化接口")
	default Response<V> init(@RequestBody Optional<V> vo) {
		return Response.ok(getApi().init(vo));
	}

	/**
	 * 新建保存
	 * 
	 * @param vo
	 * @return
	 */
	@PostMapping("add")
	@ApiOperation("新增接口")
	default Response<?> add(@RequestBody V vo) {
		getApi().add(vo);
		return Response.ok();
	}

	/**
	 * 获取详情
	 * 
	 * @param vo
	 * @return
	 */
	@PostMapping("get")
	@ApiOperation("查看接口")
	default Response<V> get(@RequestBody IdVO vo) {
		Optional<V> result = getApi().loadById(vo);
		if (!result.isPresent()) {
			throw new NoRecordException();
		}
		return Response.ok(result.get());
	}

	/**
	 * 更新保存
	 * 
	 * @param vo
	 * @return
	 */
	@PostMapping("update")
	@ApiOperation("更新接口")
	default Response<?> update(@RequestBody V vo) {
		getApi().update(vo);
		return Response.ok();
	}

	/**
	 * 删除记录
	 * 
	 * @param vo
	 * @return
	 */
	@PostMapping("delete")
	@ApiOperation("删除接口")
	default Response<?> delete(@RequestBody IdVO vo) {
		getApi().delete(vo);
		return Response.ok();
	}
}
