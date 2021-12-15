package com.sjtu.seckill.vo;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum  RespBeanEnum {
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登录模块
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号码错误"),
    BIND_ERROR(500213,"参数校验异常"),
    SESSION_ERROR(500214,"用户不存在"),
    //秒杀模块
    EMPTY_STOCK(500510,"商品库存不足"),
    REPEAT_BUY(500511,"该商品每个用户限购一件"),
    PATH_ILLEGAL(500512,"秒杀路径错误"),
    CAPTCHA_ERROR(500513,"验证码错误，请重新输入！"),
    ACCESS_LIMIT_ERROR(500514,"请求过于频繁请稍后！"),
    //订单mok
    ORDER_ERROR(500610,"订单编号错误"),
    ;

    private final Integer code;
    private final String message;

}
