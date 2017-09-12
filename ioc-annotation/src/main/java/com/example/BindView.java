package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解模块的实现
@Retention(RetentionPolicy.CLASS)//在编译成class文件时作用
@Target(ElementType.FIELD)//作用的类型
public @interface BindView {
    int value();
}
