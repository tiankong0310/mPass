package com.ibyte.common.exception;

/**
 * 无效的请求异常
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 */
public class UnexpectedRequestException extends KmssRuntimeException {
	private static final long serialVersionUID = 4083273216078165302L;

	public UnexpectedRequestException() {
		super("errors.unexpectedRequest");
	}
}
