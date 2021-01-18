package com.playtech;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExpirationCache<K, V> implements Map<K, V> {

    private final ConcurrentHashMap<K, ExpiringObject<K, V>> delegate;

    private final CleanUpWorker cleanUpWorker;

    ExpirationCache(int timeToLive, int expirationInterval) {
        this(new ConcurrentHashMap<>(), timeToLive, expirationInterval);
    }

    private ExpirationCache(
            ConcurrentHashMap<K, ExpiringObject<K, V>> delegate,
            int timeToLive,
            int expirationInterval) {
        this.delegate = delegate;
        this.cleanUpWorker = new CleanUpWorker();
        cleanUpWorker.setTimeToLive(timeToLive);
        cleanUpWorker.setExpirationInterval(expirationInterval);
        cleanUpWorker.startExpiringIfNotStarted();
    }

    public V put(K key, V value) {
        ExpiringObject<K, V> answer = delegate.put(key,
                new ExpiringObject<>(
                        key,
                        value,
                        System.currentTimeMillis()));
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    public V get(Object key) {
        ExpiringObject<K, V> object = delegate.get(key);
        if (object != null) {
            object.setLastAccessTime(System.currentTimeMillis());
            return object.getValue();
        }
        return null;
    }

    public V remove(Object key) {
        ExpiringObject<K, V> answer = delegate.remove(key);
        if (answer == null) {
            return null;
        }

        return answer.getValue();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public void clear() {
        delegate.clear();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public void putAll(Map<? extends K, ? extends V> inMap) {
        for (Entry<? extends K, ? extends V> e : inMap.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public class CleanUpWorker implements Runnable {

        private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

        private long expirationIntervalMillis;

        private long timeToLiveMillis;

        private boolean running = false;

        private final Thread thread;

        CleanUpWorker() {
            thread = new Thread(this);
            thread.setDaemon(true);
        }

        public void run() {
            while (running) {
                doCleanUp();
                try {
                    Thread.sleep(expirationIntervalMillis);
                } catch (InterruptedException e) {
                }
            }
        }

        private void doCleanUp() {
            long timeNow = System.currentTimeMillis();
            for (ExpiringObject expiringObject : delegate.values()) {
                if (timeToLiveMillis <= 0) {
                    continue;
                }
                long timeIdle = timeNow - expiringObject.getLastAccessTime();
                if (timeIdle >= timeToLiveMillis) {
                    delegate.remove(expiringObject.getKey());
                }
            }
        }

        void startExpiringIfNotStarted() {
            stateLock.readLock().lock();
            try {
                if (running) {
                    return;
                }
            } finally {
                stateLock.readLock().unlock();
            }

            stateLock.writeLock().lock();
            try {
                if (!running) {
                    running = true;
                    thread.start();
                }
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        void setTimeToLive(long timeToLive) {
            stateLock.writeLock().lock();
            try {
                this.timeToLiveMillis = timeToLive * 1000;
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        void setExpirationInterval(long expirationInterval) {
            stateLock.writeLock().lock();
            try {
                this.expirationIntervalMillis = expirationInterval * 1000;
            } finally {
                stateLock.writeLock().unlock();
            }
        }
    }
}
