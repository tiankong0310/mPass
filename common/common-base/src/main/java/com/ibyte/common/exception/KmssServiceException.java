package com.ibyte.common.exception;

import com.ibyte.common.dto.Stack;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: <服务异常>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class KmssServiceException extends KmssRuntimeException {

    private static final long serialVersionUID = -1056997658177543184L;

    @Getter
    @Setter
    private List<Stack> stacks;

    public KmssServiceException(String code, String message,
                                List<Stack> stacks) {
        super(code, message);
        this.stacks = stacks;
    }

    public KmssServiceException(String messageKey) {
        super(messageKey);
        this.stacks = new ArrayList<>();
    }

    public KmssServiceException(String code, String message) {
        super(code, message);
        this.stacks = new ArrayList<>();
    }

    public KmssServiceException(String messageKey, Throwable cause) {
        super(messageKey, cause);
        this.stacks = new ArrayList<>();
    }

    public KmssServiceException(String code, String message, Throwable cause) {
        super(code, message, cause);
        this.stacks = new ArrayList<>();
    }
}