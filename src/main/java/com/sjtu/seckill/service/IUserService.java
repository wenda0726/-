package com.sjtu.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.vo.LoginVO;
import com.sjtu.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-02
 */

public interface IUserService extends IService<User> {

    public RespBean doLogin(LoginVO vo, HttpServletRequest request, HttpServletResponse response);

    public User getUserByTicket(String userTicket, HttpServletRequest request, HttpServletResponse response);
}
