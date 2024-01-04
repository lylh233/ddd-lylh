package com.lylh.common.utils;

import cn.hutool.core.date.DateUtil;

public class DateUtils extends DateUtil {

    /**
     * 将毫秒，以x时：x分：x秒形式返回
     * @param value 毫秒
     * @return xx:xx:xx
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
