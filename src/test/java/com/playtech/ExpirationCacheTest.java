package com.playtech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpirationCacheTest {

    private final static int LIFE_TIME_SECONDS = 5;
    private final static int EXPIRATION_INTERVAL_SECONDS = 1;

    private final static String KEY_ONE = "key1";
    private final static String KEY_TWO = "key2";
    private final static String KEY_THREE = "key3";

    private final static String VALUE_ONE = "value1";
    private final static String VALUE_TWO = "value2";
    private final static String VALUE_THREE = "value3";

    @Test
    public void shouldKeepItemsBeforeTheirLifeTimeEnd() throws InterruptedException {
        ExpirationCache<String, String> cache = new ExpirationCache<>(LIFE_TIME_SECONDS, EXPIRATION_INTERVAL_SECONDS);

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
        ExpirationCache<String, String> cache = new ExpirationCache<>(LIFE_TIME_SECONDS, EXPIRATION_INTERVAL_SECONDS);

        cache.put(KEY_ONE, VALUE_ONE);
        cache.put(KEY_TWO, VALUE_TWO);
        cache.put(KEY_THREE, VALUE_THREE);
        assertEquals(3, cache.size());

        Thread.sleep(6000);

        assertTrue(cache.isEmpty());
    }

    @Test
    public void shouldEvictItemsPartiallyAfterTheirLifeTimeEnd() throws InterruptedException {
        ExpirationCache<String, String> cache = new ExpirationCache<>(LIFE_TIME_SECONDS, EXPIRATION_INTERVAL_SECONDS);

        cache.put(KEY_ONE, VALUE_ONE);
        cache.put(KEY_TWO, VALUE_TWO);

        Thread.sleep(3000);

        cache.put(KEY_THREE, VALUE_THREE);

        Thread.sleep(3000);

        assertEquals(1, cache.size());
        assertEquals(VALUE_THREE, cache.get(KEY_THREE));
    }
}
