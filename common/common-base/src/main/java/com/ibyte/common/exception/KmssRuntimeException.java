package com.ibyte.common.exception;

import com.ibyte.common.i18n.ResourceUtil;
import lombok.Getter;

/**
 * @Description: <运行时异常基类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class KmssRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2045332047069508231L;

    /** 一般使用messageKey */
    @Getter
    private String code;

    public KmssRuntimeException(String messageKey) {
        super(ResourceUtil.getString(messageKey));
        this.code = messageKey;
    }

    public KmssRuntimeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public KmssRuntimeException(String messageKey, Throwable cause) {
        super(ResourceUtil.getString(messageKey), cause);
        this.code = messageKey;
    }

    public KmssRuntimeException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}