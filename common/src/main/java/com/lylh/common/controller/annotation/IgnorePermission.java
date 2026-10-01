package com.lylh.common.controller.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记无需登录的接口。加在类上时，该类的全部接口都跳过令牌校验。
 */
@Inherited
@Retention(RUNTIME)
@Target({TYPE,METHOD})
public @interface IgnorePermission {

}
