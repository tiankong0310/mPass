package com.ibyte.common.exception;

/**
 * 无访问权限
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 */
public class AccessDeniedException extends KmssRuntimeException {
	private static final long serialVersionUID = 6084486928183353506L;

	public AccessDeniedException() {
		super("global.accessDenied");
	}
}
