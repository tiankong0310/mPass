package com.ibyte.common.exception;

import com.ibyte.common.i18n.ResourceUtil;
import lombok.Getter;

/**
 * @Description: <异常基类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class KmssException extends Exception {
    private static final long serialVersionUID = 6157898958166229413L;

    /** 一般使用messageKey */
    @Getter
    private String code;

    public KmssException(String messageKey) {
        super(ResourceUtil.getString(messageKey));
        this.code = messageKey;
    }

    public KmssException(String code, String message) {
        super(message);
        this.code = code;
    }

    public KmssException(String messageKey, Throwable cause) {
        super(ResourceUtil.getString(messageKey), cause);
        this.code = messageKey;
    }

    public KmssException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
