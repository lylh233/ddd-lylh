package com.lylh.app.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@MapperScan("com.lylh.repository.dao")
public class MybatisPlusConfig {

    private static final String UPDATE_COLUMN = "lastModified";
    private static final String CREATE_COLUMN = "created";

    @Component
    public static class MybatisObjectHandler implements MetaObjectHandler {

        /**
         * 插入数据时，默认插入当前时间做为created，lastModified
         * @param metaObject metaObject
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            Long time = System.currentTimeMillis();
            this.setFieldValByName(CREATE_COLUMN, time, metaObject);
            this.setFieldValByName(UPDATE_COLUMN, time, metaObject);
        }

        /**
         * 更新数据时，默认插入当前时间做为lastModified
         * @param metaObject metaObject
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            Long time = System.currentTimeMillis();
            this.setFieldValByName(UPDATE_COLUMN, time, metaObject);
        }
    }


}
