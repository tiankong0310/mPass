package com.ibyte.common.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用展现对象：fdId,fdSubject
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IdSubjectProperty extends IdProperty {
	private String fdSubject;

	public static IdSubjectProperty of(String fdId, String fdSubject) {
		IdSubjectProperty result = new IdSubjectProperty();
		result.setFdId(fdId);
		result.setFdSubject(fdSubject);
		return result;
	}
}
