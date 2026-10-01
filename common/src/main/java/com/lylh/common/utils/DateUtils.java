package com.lylh.common.utils;

import cn.hutool.core.date.DateUtil;

public class DateUtils extends DateUtil {

    /**
     * 把耗时格式化成定宽的时分秒，方便日志对齐。不足两位补零。
     *
     * @param value 毫秒，null 时原样返回 null
     * @return HH:mm:ss
     */
    public static String changeMillisecondToTimeStr(Long value) {
        if (value == null) {
            return null;
        }

        int millisecond = value.intValue();
        int h = millisecond / 3600000;
        int m = (millisecond - h * 3600000) / 60000;
        int s = (millisecond - h * 3600000 - m * 60000) / 1000;
        StringBuilder str = new StringBuilder();

        if (h < 10) {
            str.append(0);
        }
        str.append(h).append(":");

        if (m < 10) {
            str.append(0);
        }
        str.append(m).append(":");

        if (s < 10) {
            str.append(0);
        }
        str.append(s);
        return str.toString();
    }
}
