package com.lylh.biz.model.excel;

import com.lylh.biz.model.enums.UserTypeEnum;
import org.springframework.util.StringUtils;

/**
 * 导入行的格式校验。用户名是否已存在留给批量查库，这里不访问数据库。
 */
public final class UserImportRowChecker {

    public static final int MAX_TEXT_LENGTH = 50;
    public static final int MAX_SOURCE_LENGTH = 1000;
    /** 用户表密码列只有 50，密文超长的行不能写入。 */
    public static final int MAX_PASSWORD_CIPHER_LENGTH = 50;

    private UserImportRowChecker() {
    }

    public static boolean isBlank(UserImportRow row) {
        return row == null
                || (!StringUtils.hasText(row.getUsername())
                && !StringUtils.hasText(row.getName())
                && !StringUtils.hasText(row.getPassword())
                && !StringUtils.hasText(row.getUserType()));
    }

    /**
     * @param row 文件中的一行
     * @return 跳过、失败或通过。通过时用户名和姓名已去掉首尾空白
     */
    public static CheckOutcome check(UserImportRow row) {
        if (isBlank(row)) {
            return CheckOutcome.skip();
        }
        if (!StringUtils.hasText(row.getUsername())) {
            return CheckOutcome.fail("用户名不能为空");
        }
        String username = row.getUsername().trim();
        if (username.length() > MAX_TEXT_LENGTH) {
            return CheckOutcome.fail("用户名长度不能超过50");
        }
        if (!StringUtils.hasText(row.getName())) {
            return CheckOutcome.fail("姓名不能为空");
        }
        String name = row.getName().trim();
        if (name.length() > MAX_TEXT_LENGTH) {
            return CheckOutcome.fail("姓名长度不能超过50");
        }
        if (!StringUtils.hasText(row.getPassword())) {
            return CheckOutcome.fail("密码不能为空");
        }
        Integer userType = parseUserType(row.getUserType());
        if (userType == null) {
            return CheckOutcome.fail("用户类型仅支持1（公众用户）或2（工作人员）");
        }
        return CheckOutcome.pass(username, name, userType);
    }

    /**
     * 失败行要能对照原文件修改，所以保留原始单元格而不是修正后的值。
     *
     * @param row 原始行
     * @return 截断后的源数据
     */
    public static String sourceOf(UserImportRow row) {
        String source = text(row == null ? null : row.getUsername())
                + "," + text(row == null ? null : row.getName())
                + "," + text(row == null ? null : row.getPassword())
                + "," + text(row == null ? null : row.getUserType());
        if (source.length() <= MAX_SOURCE_LENGTH) {
            return source;
        }
        return source.substring(0, MAX_SOURCE_LENGTH);
    }

    private static Integer parseUserType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String text = raw.trim();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
        if (value == UserTypeEnum.PUBLIC_USER.getType() || value == UserTypeEnum.STAFF.getType()) {
            return value;
        }
        return null;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    /**
     * 一行的校验结论。
     */
    public static final class CheckOutcome {

        private final boolean skipped;
        private final String reason;
        private final String username;
        private final String name;
        private final Integer userType;

        private CheckOutcome(boolean skipped, String reason, String username, String name, Integer userType) {
            this.skipped = skipped;
            this.reason = reason;
            this.username = username;
            this.name = name;
            this.userType = userType;
        }

        static CheckOutcome skip() {
            return new CheckOutcome(true, null, null, null, null);
        }

        static CheckOutcome fail(String reason) {
            return new CheckOutcome(false, reason, null, null, null);
        }

        static CheckOutcome pass(String username, String name, Integer userType) {
            return new CheckOutcome(false, null, username, name, userType);
        }

        public boolean isSkipped() {
            return skipped;
        }

        public boolean isPassed() {
            return !skipped && reason == null;
        }

        public String getReason() {
            return reason;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public Integer getUserType() {
            return userType;
        }
    }
}
