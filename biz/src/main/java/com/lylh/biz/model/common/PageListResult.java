package com.lylh.biz.model.common;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageListResult<T> extends CommonResult<List<T>>{

    @JsonProperty("page_info")
    private Map<String, Object> pageInfo;

    public PageListResult(final IPage<T> page) {
        super();
        pageInfo = new HashMap<>();
        //当前页
        pageInfo.put("page_num", page.getCurrent());
        //记录总行数
        pageInfo.put("items", page.getTotal());
        //总页数
        pageInfo.put("total_page", page.getPages());
        //每页记录数
        pageInfo.put("page_size", page.getSize());
    }

}
