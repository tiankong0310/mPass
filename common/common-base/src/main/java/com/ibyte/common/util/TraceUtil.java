package com.ibyte.common.util;

import com.ibyte.common.util.thread.ThreadLocalUtil;

/**
 * @Description: <当前链路信息获取>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class TraceUtil {

    public final static String TRACE_ID = "TRACE_ID";

    public final static String FROM_SERVER = "FROM_SERVER";

    public final static String CURRENT_SERVER = "CURRENT_SERVER";

    /**
     * 获取当前租户ID
     */
    public final static String getTraceId() {
        return ThreadLocalUtil.getTranVar(TRACE_ID);
    }

    /**
     * 获取来源服务
     */
    public final static String getFromServer() {
        return ThreadLocalUtil.getLocalVar(FROM_SERVER);
    }

    /**
     * 获取来源服务
     */
    public final static String getCurrentServer() {
        return ThreadLocalUtil.getLocalVar(CURRENT_SERVER);
    }
}
