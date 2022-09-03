package com.raft.util;

public class TimeUtil {
    private Long lastTime;
    private final Long overtime;
//    private final Long bySecond = 1000L;


    public TimeUtil(Long overtime) {
        this.overtime = overtime;
        lastTime = System.currentTimeMillis();
    }


    public void updateLastTime(){
        lastTime = System.currentTimeMillis();
    }

    public Long getLastTime() {
        return lastTime;
    }

    public Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean isOvertime() {
        return getCurrentTime() > getLastTime() + overtime;
    }
}
