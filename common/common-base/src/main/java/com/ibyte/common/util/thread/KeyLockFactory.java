package com.ibyte.common.util.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 根据某个key进行多线程锁，使用方法：<br>
 *
 * <pre>
 * class X {
 * 	KeyLockFactory factory = new KeyLockFactory();
 *
 * 	void m() {
 * 		KeyLock lock = factory.getKeyLock("myKey").lock();
 * 		try {
 * 			// ...
 * 		} finally {
 * 			lock.unlock();
 * 		}
 * 	}
 * }
 * </pre>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:03
 */
public class KeyLockFactory {

    private ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>(
            16);

    /**
     * 获取一个锁
     */
    public KeyLock getKeyLock(String key) {
        return new KeyLock(key, createLock(key));
    }

    private ReentrantLock createLock(String key) {
        ReentrantLock newLock = new ReentrantLock();
        ReentrantLock lock = lockMap.putIfAbsent(key, newLock);
        if (lock == null) {
            lock = newLock;
        }
        return lock;
    }

    private void removeLock(String key) {
        lockMap.remove(key);
    }

    public class KeyLock {
        private boolean used = false;
        private String key;
        private ReentrantLock lock;

        private KeyLock(String key, ReentrantLock lock) {
            this.key = key;
            this.lock = lock;
        }

        /**
         * 加锁，只能调用一次
         */
        public KeyLock lock() {
            if (used) {
                throw new IllegalStateException("KeyLock不能多次执行锁");
            }
            used = true;
            lock.lock();
            return this;
        }

        /**
         * 解锁
         */
        public void unlock() {
            removeLock(key);
            lock.unlock();
        }
    }
}
