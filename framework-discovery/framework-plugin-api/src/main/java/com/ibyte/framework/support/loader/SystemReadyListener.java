package com.ibyte.framework.support.loader;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.constant.ReadyEventOrder;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.thread.NamedThreadFactory;
import com.ibyte.framework.plugin.ExtensionChangedListener;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.PluginContextHolder;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.persistent.PersistentConstant;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.api.listener.StatusListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: <监听ApplicationReadyEvent事件，保存插件和元数据>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-15 00:55
 */
@Slf4j
@Component
public class SystemReadyListener implements Ordered,
        ApplicationListener<ApplicationReadyEvent>, DisposableBean {
    @Autowired
    private RedissonClient redisson;

    @Override
    public int getOrder() {
        return ReadyEventOrder.FRAMEWORK;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 保存插件
        if (SystemLoadListener.LOADER.pluginLoader != null) {
            SystemLoadListener.LOADER.pluginLoader.save();
            SystemLoadListener.LOADER.pluginLoader = null;
        }
        // 保存模块
        if (SystemLoadListener.LOADER.moduleLoader != null) {
            SystemLoadListener.LOADER.moduleLoader.save();
            SystemLoadListener.LOADER.moduleLoader = null;
        }
        // 激活插件变化监听
        activeListener();
    }

    private ExecutorService executorService;

    /**
     * 激活插件变化监听
     */
    private void activeListener() {
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new NamedThreadFactory("extension-change"));
        // 创建redis的消息监听器
        MessageListener<Set<String>> listener = new MessageListener<Set<String>>() {
            @Override
            public void onMessage(CharSequence channel, Set<String> pointIds) {
                for (String pointId : pointIds) {
                    ExtensionPointImpl point = PluginContextHolder.getExtensionPoint(NamingConstant.oriName(pointId));
                    // 判断扩展点是否在本地并且有监听器
                    if (point == null || point.getListener() == null
                            || point.getModule() == null) {
                        continue;
                    }
                    executorService.submit(new ExtensionChangedJob(point));
                }
            }
        };
        // 订阅消息
        RTopic topic = redisson
                .getTopic(PersistentConstant.EXTENSIONPOINT_CHANGE_TOPIC);
        topic.addListener((StatusListener) listener);
        Set<String> set = new HashSet<>();
        for (ExtensionPointImpl point : PluginContextHolder
                .getExtensionPoints()) {
            if (point == null || point.getListener() == null
                    || point.getModule() == null) {
                continue;
            }
            set.add(NamingConstant.shortName(point.getId()));
        }
        if (!set.isEmpty()) {
            listener.onMessage("", set);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    /**
     * 插件变化执行
     *
     * @author 李尚志
     */
    class ExtensionChangedJob implements Runnable {
        ExtensionPointImpl point;

        public ExtensionChangedJob(ExtensionPointImpl point) {
            super();
            this.point = point;
        }

        @Override
        public void run() {
            RLock lock = redisson.getLock(StringHelper.join(
                    PersistentConstant.EXTENSIONPOINT_CHANGE_LOCK,
                    NamingConstant
                            .shortName(point.getListener())));
            if (lock.tryLock() && lock.isHeldByCurrentThread()) {
                try {
                    doRun();
                } finally {
                    lock.unlock();
                }
            }
        }

        private void doRun() {
            Class<?> clazz = PluginReflectUtil
                    .classForName(point.getListener());
            if (clazz == null) {
                return;
            }
            Object handler = ApplicationContextHolder
                    .findOrCreateInstance(clazz);
            if (handler == null
                    || !(handler instanceof ExtensionChangedListener)) {
                log.error("无法找到监听器的实现类："
                        + point.getListener());
                return;
            }
            try {
                ((ExtensionChangedListener) handler).onExtensionChanged();
            } catch (Exception e) {
                log.error("扩展变更监听事件执行失败", e);
            }
        }
    }
}
