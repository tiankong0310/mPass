package com.ibyte.framework.support.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 远程API
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Getter
@Setter
@ToString
public class RemoteApi {
	/** 所属模块 */
	private String module;

	/** controller的全路径（含节点名） */
	private String path;

	/** 类名 */
	private String className;

	/** 接口类名 -> 泛型参数名 -> 泛型实际类名 */
	private Map<String, Map<String, String>> interfaces;
}
