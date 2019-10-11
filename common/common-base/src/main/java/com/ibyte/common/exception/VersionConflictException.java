package com.ibyte.common.exception;

/**
 * 版本冲突异常
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 */
public class VersionConflictException extends KmssRuntimeException {
	/**  */
	private static final long serialVersionUID = -9147092293425702423L;

	public VersionConflictException() {
		super("errors.versionConflict");
	}
}
