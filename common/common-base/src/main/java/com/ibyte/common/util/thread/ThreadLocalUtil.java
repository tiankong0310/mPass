package com.ibyte.common.util.thread;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.ibyte.common.util.thread.ThreadLocalHolder.LOCAL_VAR;
import static com.ibyte.common.util.thread.ThreadLocalHolder.TRAN_VAR;

/**
 * @Description: <线程变量通用方法>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:56
 */
public class ThreadLocalUtil {

    /**
     * 需跨应用传递参数在header信息中的前缀
     */
    public static final String TRAN_PREFIX = "_tran_";
    /**
     * 当前请求
     */
    public static final String REQUEST_KEY = HttpServletRequest.class
            .getName();

    /**
     * 获取当前请求
     *
     * @return
     */
    public static HttpServletRequest currentRequest() {
        return getLocalVar(REQUEST_KEY);
    }

    /** 是否有效 */
    public static boolean isAvailable() {
        return LOCAL_VAR.get() != null;
    }

    /** 读本地线程变量 */
    public static <T> T getLocalVar(String key) {
        return get(LOCAL_VAR, key);
    }

    /** 写本地线程变量 */
    public static void setLocalVar(String key, Object value) {
        set(LOCAL_VAR, key, value);
    }

    /** 删本地线程变量 */
    public static void removeLocalVar(String key) {
        remove(LOCAL_VAR, key);
    }

    /** 读可传播的线程变量 */
    public static String getTranVar(String key) {
        return get(TRAN_VAR, key.toLowerCase());
    }

    /** 写可传播的线程变量 */
    public static void setTranVar(String key, String value) {
        set(TRAN_VAR, key.toLowerCase(), value);
    }

    /** 删可传播的线程变量 */
    public static void removeTranVar(String key) {
        remove(TRAN_VAR, key.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(ThreadLocal<Map<String, Object>> var, String key) {
        Map<String, Object> map = var.get();
        if (map == null) {
            return null;
        }
        return (T) map.get(key);
    }

    private static void set(ThreadLocal<Map<String, Object>> var, String key,
                            Object value) {
        Map<String, Object> map = var.get();
        if (map == null) {
            return;
        }
        map.put(key, value);
    }

    private static void remove(ThreadLocal<Map<String, Object>> var,
                               String key) {
        Map<String, Object> map = var.get();
        if (map == null) {
            return;
        }
        map.remove(key);
    }
}
