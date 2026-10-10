package com.lylh.biz.model.excel;

/**
 * 导入批大小。和用户名 IN 查询的上限对齐，避免一批里再拆多次查库。
 */
public final class UserImportLimits {

    public static final int BATCH_SIZE = 500;

    private UserImportLimits() {
    }
}
