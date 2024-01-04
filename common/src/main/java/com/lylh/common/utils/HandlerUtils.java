package com.lylh.common.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class HandlerUtils {

    public static String getParamsFromHttpRequest(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String param = enumeration.nextElement();
            String[] value = request.getParameterValues(param);

            params.append(param).append(":").append(value[0]).append("; ");
        }
        return params.toString();
    }

    public static String lineSeparatorStr() {
        return System.getProperty("line.separator");
    }
}
