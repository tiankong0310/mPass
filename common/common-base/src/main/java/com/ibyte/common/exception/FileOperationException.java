package com.ibyte.common.exception;

/**
 * 文件操作异常
 *
 * @author li.Shangzhi
 * @Date: 2019-10-12
 *
 */
public class FileOperationException extends KmssRuntimeException {
	private static final long serialVersionUID = -1056997658177543184L;

	public FileOperationException(String messageKey) {
		super("errors.fileOperationException");
	}

	public FileOperationException(String code, String message) {
		super(code, message);
	}

	public FileOperationException(String messageKey, Throwable cause) {
		super(messageKey, cause);
	}

	public FileOperationException(String code, String message, Throwable cause) {
		super(code, message, cause);
	}
}
