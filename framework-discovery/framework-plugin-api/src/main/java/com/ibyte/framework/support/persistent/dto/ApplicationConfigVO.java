package com.ibyte.framework.support.persistent.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 配置信息VO
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Getter
@Setter
@ToString
public class ApplicationConfigVO {
	private String fdId;

	private Integer fdTenantId;

	private String fdContent;
}
