package com.lylh.common.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 组装请求参数日志。密码和令牌只保留字段名，避免访问日志把凭据落盘。
 */
public class HandlerUtils {

    public static String getParamsFromHttpRequest(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String param = enumeration.nextElement();
            String[] value = request.getParameterValues(param);
            String raw = value == null || value.length == 0 || value[0] == null ? "" : value[0];
            String shown = isSensitive(param) ? "***" : raw;
            params.append(param).append(":").append(shown).append("; ");
        }
        return params.toString();
    }

    public static String lineSeparatorStr() {
        return System.getProperty("line.separator");
    }

    private static boolean isSensitive(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("token") || lower.contains("secret");
    }
}
