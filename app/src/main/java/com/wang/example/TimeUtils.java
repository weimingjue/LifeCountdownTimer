package com.wang.example;

import android.support.annotation.Size;

public class TimeUtils {
    //天时分秒毫秒的倍数
    public static final long TIME_DAY = 86400000L, TIME_HOUR = 3600000L, TIME_MINUTE = 60000L, TIME_SECOND = 1000L;

    @Size(3)
    public static String[] getMillsTimeSt(long timeInMillis) {
        int hour = (int) (timeInMillis / TIME_HOUR);
        int minute = (int) ((timeInMillis % TIME_HOUR) / TIME_MINUTE);
        int second = (int) (((timeInMillis % TIME_HOUR) % TIME_MINUTE) / TIME_SECOND);
        return new String[]{get2TimeSt(hour), get2TimeSt(minute), get2TimeSt(second)};
    }

    /**
     * 9变成09
     */
    public static String get2TimeSt(int t) {
        return t > 9 ? Long.toString(t) : "0" + t;
    }
}
