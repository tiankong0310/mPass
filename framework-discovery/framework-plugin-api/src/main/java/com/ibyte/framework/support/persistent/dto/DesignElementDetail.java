package com.ibyte.framework.support.persistent.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 设计详细信息<br>
 * 
 * fdContent是JSON字符串，保存的时候会自动从fdContent中抽取：label、messageKey、module的信息，作为摘要信息保存
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Getter
@Setter
@ToString
public class DesignElementDetail {
	private String fdId;

	private String fdAppName;

	private String fdModule;

	private String fdMd5;

	private String fdContent;
}
