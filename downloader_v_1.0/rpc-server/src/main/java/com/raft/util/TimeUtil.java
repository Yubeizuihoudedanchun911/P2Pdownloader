package com.raft.util;

public class TimeUtil {
    private Long lastTime;
    private final Long overtime;
    private final Long bySecond = 1000L;

    public TimeUtil() {
        overtime = 2L + (int)(5 * Math.random());
        lastTime = System.currentTimeMillis()/bySecond;
    }

    public TimeUtil(Long overtime) {
        this.overtime = overtime;
        lastTime = System.currentTimeMillis()/bySecond;
    }

    public void updateLastTime(){
        lastTime = System.currentTimeMillis()/bySecond;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public Long getCurrentTime() {
        return System.currentTimeMillis()/bySecond;
    }

    public boolean isOvertime() {
        return getCurrentTime() > getLastTime() + overtime;
    }
}
