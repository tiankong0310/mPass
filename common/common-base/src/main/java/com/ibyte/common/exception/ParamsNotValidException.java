package com.ibyte.common.exception;

/**
 * @Description: <无效参数>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class ParamsNotValidException extends KmssRuntimeException {
    private static final long serialVersionUID = -6748644292069239414L;

    public ParamsNotValidException() {
        super("errors.paramsNotValid");
    }

    public ParamsNotValidException(String message) {
        super("errors.paramsNotValid", message);
    }
}
