package com.ibyte.common.core.dto;

import java.util.List;

/**
 * 界面展现对象接口。<br>
 * 说明：VO的字段为null表示不修改Entity，若要讲Entity字段设置为null，请调用addNullValueProps
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IViewObject extends IData {
	/**
	 * 读-空值字段
	 * 
	 * @return
	 */
	public List<String> getNullValueProps();

	/**
	 * 写-空值字段
	 * 
	 * @param props
	 */
	public void setNullValueProps(List<String> props);

	/**
	 * 加-空值字段
	 * @param props
	 */
	public void addNullValueProps(String... props);
}
