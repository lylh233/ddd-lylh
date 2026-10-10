package com.lylh.biz.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 导入文件的一行。按列序号读取，表头文字可以是中文或英文。
 * 列顺序：用户名、姓名、密码、用户类型。
 */
@Data
public class UserImportRow {

    @ExcelProperty(index = 0)
    private String username;

    @ExcelProperty(index = 1)
    private String name;

    @ExcelProperty(index = 2)
    private String password;

    @ExcelProperty(index = 3)
    private String userType;
}
