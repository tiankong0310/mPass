package com.ibyte.common.util.thread;

import com.ibyte.common.util.StringHelper;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: <规范：必须通过该线程工厂新开线程，以更好地管理线程变量>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:04
 */
public class NamedThreadFactory implements ThreadFactory {

    /** 命名前缀 */
    private final String namePrefix;

    /** 序号 */
    private static final AtomicInteger THREADNUMBER = new AtomicInteger(1);

    public NamedThreadFactory(String namePrefix) {
        super();
        this.namePrefix = namePrefix;
    }

    /**
     * 创建新线程
     */
    @Override
    public Thread newThread(Runnable r) {
        String name = StringHelper.join(namePrefix, "-thread-",
                THREADNUMBER.getAndIncrement());
        return newThread(name, r, false);
    }

    /**
     * 创建新线程
     */
    public static Thread newThread(String name, Runnable r,
                                   boolean tranThreadLocal) {
        final Map<String, Object> tranVar = tranThreadLocal
                ? ThreadLocalHolder.getTranVars() : null;
        Thread t = new NamedThread(name, r, tranVar);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    /**
     * 启动新线程
     */
    public static Thread startThread(String name, Runnable r,
                                     boolean tranThreadLocal) {
        Thread t = newThread(name, r, tranThreadLocal);
        t.start();
        return t;
    }

    static class NamedThread extends Thread {
        private Map<String, Object> tranVar;

        public NamedThread(String name, Runnable r,
                           Map<String, Object> tranVar) {
            super(r, name);
            this.tranVar = tranVar;
        }

        @Override
        public void run() {
            try {
                ThreadLocalHolder.begin(tranVar);
                super.run();
            } finally {
                ThreadLocalHolder.end();
            }
        }
    }
}
