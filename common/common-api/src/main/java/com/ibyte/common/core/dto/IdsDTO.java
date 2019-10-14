package com.ibyte.common.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FdId列表
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
@Getter
@Setter
@ToString
public class IdsDTO {
	private List<String> fdIds;

	public static IdsDTO of(String... fdIds) {
		IdsDTO result = new IdsDTO();
		result.setFdIds(
				fdIds == null ? new ArrayList<>() : Arrays.asList(fdIds));
		return result;
	}

	public static IdsDTO of(List<String> fdIds) {
		IdsDTO result = new IdsDTO();
		result.setFdIds(fdIds);
		return result;
	}
}
