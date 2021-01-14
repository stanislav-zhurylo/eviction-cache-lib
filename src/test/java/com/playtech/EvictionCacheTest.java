package com.playtech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class EvictionCacheTest {

    private final static long LIFE_TIME_MILLISECONDS = 5000L;

    private final static int CONCURRENCY_LEVEL = 3;

    private final static String KEY_ONE = "key1";
    private final static String KEY_TWO = "key2";
    private final static String KEY_THREE = "key3";

    private final static String VALUE_ONE = "value1";
    private final static String VALUE_TWO = "value2";
    private final static String VALUE_THREE = "value3";

    @Test
    public void shouldKeepItemsBeforeTheirLifeTimeEnd() throws InterruptedException {
        EvictionCache<String, String> cache = new EvictionCache<>(
                CONCURRENCY_LEVEL,
                LIFE_TIME_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        cache.put(KEY_ONE, VALUE_ONE);
        cache.put(KEY_TWO, VALUE_TWO);
        cache.put(KEY_THREE, VALUE_THREE);
        assertEquals(3, cache.size());

        Thread.sleep(3000);

        assertEquals(VALUE_ONE, cache.get(KEY_ONE));
        assertEquals(VALUE_TWO, cache.get(KEY_TWO));
        assertEquals(VALUE_THREE, cache.get(KEY_THREE));
        assertEquals(3, cache.size());
    }

    @Test
    public void shouldNotKeepItemsAfterTheirLifeTimeEnd() throws InterruptedException {
        EvictionCache<String, String> cache = new EvictionCache<>(
                CONCURRENCY_LEVEL,
                LIFE_TIME_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        cache.put(KEY_ONE, VALUE_ONE);
        cache.put(KEY_TWO, VALUE_TWO);
        cache.put(KEY_THREE, VALUE_THREE);
        assertEquals(3, cache.size());

        Thread.sleep(6000);

        assertTrue(cache.isEmpty());
    }

    @Test
    public void shouldEvictItemsPartiallyAfterTheirLifeTimeEnd() throws InterruptedException {
        EvictionCache<String, String> cache = new EvictionCache<>(
                CONCURRENCY_LEVEL,
                LIFE_TIME_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        cache.put(KEY_ONE, VALUE_ONE);
        cache.put(KEY_TWO, VALUE_TWO);

        Thread.sleep(3000);

        cache.put(KEY_THREE, VALUE_THREE);

        Thread.sleep(3000);

        assertEquals(1, cache.size());
        assertEquals(VALUE_THREE, cache.get(KEY_THREE));
    }
}
