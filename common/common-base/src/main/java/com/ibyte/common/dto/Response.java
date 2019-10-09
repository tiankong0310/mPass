package com.ibyte.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description: <Controller的统一返回格式>
 *
 * @author li.Shangzhi
 * @Date: 2019年10月9日 21:22:44
 */
@Setter
@Getter
@Builder
public class Response<T> {

    /** 是否成功 */
    private boolean success;
    /** 成功/错误的messageKey */
    private String code;
    /** 成功/错误的提示信息 */
    private String msg;
    /** 数据 */
    private T data;

    /** 成功 */
    public static Response<?> ok() {
        return new Response<>(true, "return.optSuccess", null, null);
    }

    /** 成功+数据 */
    public static <T> Response<T> ok(T data) {
        return new Response<>(true, "return.optSuccess", null, data);
    }

    /** 成功+标题+数据 */
    public static <T> Response<T> ok(String code, T data) {
        return new Response<>(true, code, null, data);
    }

    /** 错误 */
    public static <T> Response<T> err(String code) {
        return new Response<>(false, code, null, null);
    }

    /** 错误 KmssServiceException 增加应用模块信息 */
    public static <T> Response<T> err(String code, T data) {
        return new Response<>(false, code, null, data);
    }

    /** 错误 */
    public static <T> Response<T> err(String code, String message) {
        return new Response<>(false, code, message, null);
    }

    /** 错误 KmssServiceException 增加应用模块信息 */
    public static <T> Response<T> err(String code, String message, T data) {
        return new Response<>(false, code, message, data);
    }

    /** 返回数据 **/
    public Response(boolean success, String code, String msg, T data) {
        super();
        this.success = success;
        this.code = code;
        // TODO 多语言处理
        this.msg = msg;;
        this.data = data;
    }
}
