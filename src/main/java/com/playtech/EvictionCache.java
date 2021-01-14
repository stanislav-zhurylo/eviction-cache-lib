package com.playtech;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EvictionCache<K, V> implements Map<K, V> {

    private long expirationTimeValue;

    private TimeUnit expirationTimeUnit;

    private ThreadPoolExecutor executor;

    private Map<K, EntryValue<V>> storage;

    public EvictionCache(int concurrencyLevel, long expirationTimeValue, TimeUnit expirationTimeUnit) {
        this.expirationTimeValue = expirationTimeValue;
        this.expirationTimeUnit = expirationTimeUnit;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(concurrencyLevel);
        this.storage = new ConcurrentHashMap<>();
    }

    @Override
    public int size() {
        return this.storage.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.storage.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.storage.containsValue(value);
    }

    @Override
    public synchronized V get(Object key) {
        EntryValue<V> value = this.storage.get(key);
        return value != null ? value.getValue() : null;
    }

    @Override
    public synchronized V put(K key, V value) {
        long currentTime = System.nanoTime();
        long timeToLiveInNanoseconds = TimeUnit.NANOSECONDS.convert(this.expirationTimeValue, this.expirationTimeUnit);
        this.storage.put(key, new EntryValue<>(value, currentTime + timeToLiveInNanoseconds));
        this.executor.submit(new CleaningWorkerTask());
        return value;
    }

    @Override
    public synchronized V remove(Object key) {
        V value = null;
        EntryValue<V> entryValue = this.storage.get(key);
        if (entryValue != null) {
            value = entryValue.getValue();
            this.storage.remove(key);
        }
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void clear() {
        this.storage.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.storage.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.storage.values().stream().map(EntryValue::getValue).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.storage.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()))
                .entrySet();
    }

    class CleaningWorkerTask implements Runnable {

        @Override
        public void run() {
            while (!storage.isEmpty()) {
                doCleanUp();
            }
        }

        private Set<K> getKeysToRemove() {
            long currentTime = System.nanoTime();
            return storage.entrySet()
                    .stream()
                    .filter(entry -> currentTime >= entry.getValue().getLastTimestampInNanoseconds())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }

        private void doCleanUp() {
            getKeysToRemove().forEach(key -> storage.remove(key));
        }
    }
}
