package com.ibyte.common.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用展现对象：fdId,fdName
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IdNameProperty extends IdProperty {
	private String fdName;

	public static IdNameProperty of(String fdId, String fdName) {
		IdNameProperty result = new IdNameProperty();
		result.setFdId(fdId);
		result.setFdName(fdName);
		return result;
	}
}
