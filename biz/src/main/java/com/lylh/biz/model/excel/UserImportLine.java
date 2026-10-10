package com.lylh.biz.model.excel;

/**
 * 带上 Excel 行号的导入行。行号从 1 开始，和表格里看到的一致。
 */
public class UserImportLine {

    private final int rowNum;
    private final UserImportRow row;

    public UserImportLine(int rowNum, UserImportRow row) {
        this.rowNum = rowNum;
        this.row = row;
    }

    public int getRowNum() {
        return rowNum;
    }

    public UserImportRow getRow() {
        return row;
    }
}
