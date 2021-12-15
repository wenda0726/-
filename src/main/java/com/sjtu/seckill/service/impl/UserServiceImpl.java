package com.sjtu.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sjtu.seckill.exception.GlobalException;
import com.sjtu.seckill.mapper.UserMapper;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IUserService;
import com.sjtu.seckill.utils.CookieUtil;
import com.sjtu.seckill.utils.MD5Util;
import com.sjtu.seckill.utils.UUIDUtil;
import com.sjtu.seckill.vo.LoginVO;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-02
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVO vo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = vo.getMobile();
        String password = vo.getPassword();
//        if(mobile == null || password == null){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号码查询用户
        User user = userMapper.selectById(mobile);
        if(user == null){
           throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //将用户登录信息设置到Cookie中
        String userTicket = UUIDUtil.uuid();
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+ userTicket,user);
//        request.getSession().setAttribute(ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",userTicket);

        return RespBean.success(userTicket);
    }

    @Override
    public User getUserByTicket(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(userTicket == null){
            return null;
        }
        User user = (User)redisTemplate.opsForValue().get("user:" + userTicket);
        if(user != null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }
}
