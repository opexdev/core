package co.nilin.opex.market.ports.postgres.util;

import java.util.Date;

public class CacheValueWrapper {

    private Object value;
    private long evictionTime;
    private boolean isTimeBased;

    public CacheValueWrapper() {

    }

    public CacheValueWrapper(Object value) {
        this.value = value;
        this.evictionTime = -1;
        this.isTimeBased = false;
    }

    public CacheValueWrapper(Object value, long evictionTime) {
        this.value = value;
        if (evictionTime < 0)
            throw new IllegalStateException("Eviction time must be greater than zero");

        this.evictionTime = evictionTime;
        this.isTimeBased = true;
    }

    public boolean checkTimeToEvict() {
        return isTimeBased && evictionTime < new Date().getTime();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getEvictionTime() {
        return evictionTime;
    }

    public void setEvictionTime(long evictionTime) {
        this.evictionTime = evictionTime;
    }

    public boolean isTimeBased() {
        return isTimeBased;
    }

    public void setTimeBased(boolean timeBased) {
        isTimeBased = timeBased;
    }
}
