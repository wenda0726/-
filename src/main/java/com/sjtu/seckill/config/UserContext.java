package com.sjtu.seckill.config;

import com.sjtu.seckill.pojo.User;

public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static User getUser(){
        return userThreadLocal.get();
    }

    public static void setUser(User user){
        userThreadLocal.set(user);
    }

}
