package com.ibyte.common.util.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: <线程变量存储>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:59
 */
public class ThreadLocalHolder {

    /** 临时缓存线程变量 */
    protected static final ThreadLocal<Map<String, Object>> LOCAL_VAR = new ThreadLocal<>();

    /** 可传播的线程变量 */
    protected static final ThreadLocal<Map<String, Object>> TRAN_VAR = new ThreadLocal<>();

    public static Map<String, Object> getTranVars() {
        return TRAN_VAR.get();
    }

    public static void begin() {
        if (LOCAL_VAR.get() == null) {
            LOCAL_VAR.set(new HashMap<String, Object>(16));
            TRAN_VAR.set(new HashMap<String, Object>(16));
        }
    }

    public static void begin(Map<String, Object> tranVar) {
        if (LOCAL_VAR.get() == null) {
            LOCAL_VAR.set(new HashMap<String, Object>(16));
            TRAN_VAR.set(new HashMap<String, Object>(16));
        }
        if (tranVar != null) {
            TRAN_VAR.get().putAll(tranVar);
        }
    }

    public static void end() {
        LOCAL_VAR.remove();
        TRAN_VAR.remove();
    }

}
