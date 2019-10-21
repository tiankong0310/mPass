package com.ibyte.framework.support.persistent.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 设计信息打包，用于批量更新<br>
 * saveList中的appName将会被这里的appName覆盖
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Getter
@Setter
@ToString
public class DesignElementGroup {
	private String fdAppName;

	private List<DesignElementDetail> saveList;

	private List<String> deleteList;
}
