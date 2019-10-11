package com.ibyte.common.exception;

/**
 * 找不到相关记录异常
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 */
public class NoRecordException extends KmssRuntimeException {
	private static final long serialVersionUID = -5901228248267354848L;

	public NoRecordException() {
		super("errors.noRecord");
	}

	public NoRecordException(String message) {
		super("errors.noRecord", message);
	}
}
