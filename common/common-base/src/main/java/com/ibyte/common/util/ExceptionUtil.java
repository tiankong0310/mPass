package com.ibyte.common.util;

import com.ibyte.common.dto.Determine;
import com.ibyte.common.dto.Stack;
import com.ibyte.common.exception.KmssException;
import com.ibyte.common.exception.KmssRuntimeException;
import com.ibyte.common.exception.KmssServiceException;
import com.ibyte.common.i18n.ResourceUtil;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @Description: <错误处理工具类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class ExceptionUtil {

    public static Map<String, Determine> types = new HashMap<>();

    static {
        types.put("global.accessDenied", Determine.builder().name("没有权限").status(HttpStatus.FORBIDDEN).build());
        types.put("status.401", Determine.builder().name("未授权").status(HttpStatus.UNAUTHORIZED).build());
        types.put("status.403", Determine.builder().name("没有权限").status(HttpStatus.FORBIDDEN).build());
        types.put("status.404", Determine.builder().name("请求不存在").status(HttpStatus.NOT_FOUND).build());
        types.put("errors.noRecord", Determine.builder().name("没有记录").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        types.put("errors.recordExist", Determine.builder().name("记录已存在").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        types.put("errors.paramsNotValid", Determine.builder().name("参数无效").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        types.put("errors.treeCycle", Determine.builder().name("死循环").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        types.put("errors.unexpectedRequest", Determine.builder().name("请求不合法").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        types.put("errors.feignClientException", Determine.builder().name("Feign调用异常").status(HttpStatus.SERVICE_UNAVAILABLE).build());
        types.put("errors.feignErrorDecodeException", Determine.builder().name("Feign调用异常").status(HttpStatus.SERVICE_UNAVAILABLE).build());
        types.put("errors.restErrorHandleException", Determine.builder().name("Rest请求异常").status(HttpStatus.SERVICE_UNAVAILABLE).build());
        types.put("errors.unkown", Determine.builder().name("未知异常").status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /** 读取异常code的堆栈 */
    public static LinkedList<Stack> getStacks(Throwable t, String app, String uuid) {
        LinkedList<Stack> stacks = new LinkedList<>();
        appendStack(stacks, t, app, uuid);
        return stacks;
    }

    /** 读取异常code的堆栈 */
    private static void appendStack(LinkedList<Stack> stacks, Throwable t, String app, String uuid) {
        if (t instanceof KmssException) {
            stacks.add(Stack.builder().app(app).code(((KmssException) t).getCode()).message(t.getMessage()).build());
        } else if (t instanceof KmssRuntimeException) {
            stacks.add(Stack.builder().app(app).code(((KmssRuntimeException) t).getCode()).message(t.getMessage()).build());
        } else {
            stacks.add(Stack.builder().app(app).code("errors.unkown").message(ResourceUtil.getString("errors.unkownException")).build());
        }

        if (t instanceof KmssServiceException) {
            stacks.addAll(((KmssServiceException) t).getStacks());
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            appendStack(stacks, cause, app, uuid);
        }
    }

    /** 判断异常是否包含code */
    public static boolean containsCode(Throwable t, String code) {
        if (t instanceof KmssException) {
            if (code.equals(((KmssException) t).getCode())) {
                return true;
            }
        } else if (t instanceof KmssRuntimeException) {
            if (code.equals(((KmssRuntimeException) t).getCode())) {
                return true;
            }
        }
        if (t instanceof KmssServiceException) {
            return ((KmssServiceException) t).getStacks().stream().anyMatch(stack->{
                return stack.getCode().contentEquals(code);
            });
        }

        Throwable cause = t.getCause();
        if (cause != null) {
            return containsCode(cause, code);
        }
        return false;
    }

    /**
     * 判断异常类型
     * @param t
     * @return	包含异常描述和状态码
     */
    public static Determine determineType(Throwable t) {
        Determine determine = Determine.builder().name("未知异常").status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        for(String key: types.keySet()) {
            if(containsCode(t, key)) {
                determine = types.get(key);
            }
        }
        return determine;
    }
}
