package com.sjtu.seckill.controller;


import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.rabbitmq.MQSender;
import com.sjtu.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wendasu
 * @since 2021-12-02
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    @RequestMapping("/info")
    @ResponseBody
    public RespBean getUser(User user){
        return RespBean.success(user);
    }

    @RequestMapping("/mq")
    @ResponseBody
    public void mq(){
        mqSender.send("Hello");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq01(){
        mqSender.send("Hello");
    }
//
//    @RequestMapping("/mq/direct")
//    @ResponseBody
//    public void mq02(){
//        mqSender.send01("Hello");
//    }

}
