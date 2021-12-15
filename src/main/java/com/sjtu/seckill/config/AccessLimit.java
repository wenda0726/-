package com.sjtu.seckill.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //运行时注解
@Target(ElementType.METHOD)
public @interface AccessLimit {
    int seconds();
    int maxCounts();
    boolean needLogin() default true;
}
