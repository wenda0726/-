package com.sjtu.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sjtu.seckill.pojo.Order;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.OrderVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVO goods);

    OrderVO detail(Long orderId);

    Long getSeckillOrder(Long goodsId, User user);

    String createPath(User user, Long goodsId);

    boolean checkPath(String path, User user, Long goodsId);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
