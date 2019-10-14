package com.ibyte.common.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机制类数据DTO
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
@Getter
@Setter
@ToString
public class MechanismDTO {
	private String fdEntityName;

	private String fdEntityId;

	private String fdEntityKey;

	public static MechanismDTO of(String fdEntityName, String fdEntityId) {
		MechanismDTO dto = new MechanismDTO();
		dto.setFdEntityName(fdEntityName);
		dto.setFdEntityId(fdEntityId);
		return dto;
	}

	public static MechanismDTO of(String fdEntityName, String fdEntityId,
			String fdEntityKey) {
		MechanismDTO dto = new MechanismDTO();
		dto.setFdEntityName(fdEntityName);
		dto.setFdEntityId(fdEntityId);
		dto.setFdEntityKey(fdEntityKey);
		return dto;
	}
}
