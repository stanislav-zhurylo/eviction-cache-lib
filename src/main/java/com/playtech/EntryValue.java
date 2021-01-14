package com.playtech;

class EntryValue<V> {

    private final V value;

    private final long lastTimestampInNanoseconds;

    EntryValue(V value, long lastTimestampInNanoseconds) {
        this.value = value;
        this.lastTimestampInNanoseconds = lastTimestampInNanoseconds;
    }

    V getValue() {
        return value;
    }

    long getLastTimestampInNanoseconds() {
        return lastTimestampInNanoseconds;
    }
}
