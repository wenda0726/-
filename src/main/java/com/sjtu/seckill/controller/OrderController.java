package com.sjtu.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sjtu.seckill.pojo.Order;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.service.IOrderService;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.OrderVO;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;


    @RequestMapping("/detail")
    @ResponseBody
    public RespBean orderDetail(User user,long orderId){
        if(user == null){
            return  RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderVO orderVO = orderService.detail(orderId);
        return RespBean.success(orderVO);
    }

}
