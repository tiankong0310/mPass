package com.ibyte.common.util;

import com.ibyte.common.util.thread.ThreadLocalUtil;

import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * @Description: <当前租户信息获取>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class TenantUtil {

    private static final String KEY = "common_current_tenant";

    /** 系统租户，对于存储程序信息等租户无关的表，租户ID取该值 */
    public static final Integer SYSTEM_TENANT = 0;


    /**
     * 获取当前租户ID
     */
    public final static int getTenantId() {
        String value = ThreadLocalUtil.getTranVar(KEY);
        if (value == null) {
            return SYSTEM_TENANT;
        }
        return Integer.valueOf(value);
    }

    /** 切换多租户并执行 */
    public static <T> T switchTenantId(int tenantId, Callable<T> action)
            throws Exception {
        Stack<Integer> stack = getStack();
        stack.push(getTenantId());
        ThreadLocalUtil.setTranVar(KEY, String.valueOf(tenantId));
        try {
            return action.call();
        } finally {
            ThreadLocalUtil.setTranVar(KEY, String.valueOf(stack.pop()));
        }
    }

    /** 切换堆栈 */
    private static Stack<Integer> getStack() {
        Stack<Integer> stack = ThreadLocalUtil.getLocalVar(KEY);
        if (stack == null) {
            stack = new Stack<>();
            ThreadLocalUtil.setLocalVar(KEY, stack);
        }
        return stack;
    }
}
