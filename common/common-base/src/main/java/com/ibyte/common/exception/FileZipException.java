package com.ibyte.common.exception;

/**
 * @Description: <压缩\解压 异常类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class FileZipException extends  Exception {

    private static final long serialVersionUID = 6855404979231787699L;

    public FileZipException() {
    }

    public FileZipException(String message) {
        super(message);
    }

    public FileZipException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileZipException(Throwable cause) {
        super(cause);
    }

    public FileZipException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
