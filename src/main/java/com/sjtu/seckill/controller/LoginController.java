package com.sjtu.seckill.controller;

import com.sjtu.seckill.service.IUserService;
import com.sjtu.seckill.vo.LoginVO;
import com.sjtu.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    /**
     * 跳转到登录页面
     */
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @ResponseBody
    @RequestMapping("/doLogin")
    public RespBean doLogin(@Valid LoginVO vo, HttpServletRequest request, HttpServletResponse response){
        log.info("{}",vo);
        return userService.doLogin(vo,request,response);
    }



}
