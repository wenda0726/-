package com.sjtu.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sjtu.seckill.exception.GlobalException;
import com.sjtu.seckill.mapper.OrderMapper;
import com.sjtu.seckill.mapper.SeckillOrderMapper;
import com.sjtu.seckill.pojo.Order;
import com.sjtu.seckill.pojo.SeckillGoods;
import com.sjtu.seckill.pojo.SeckillOrder;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.service.IOrderService;
import com.sjtu.seckill.service.ISeckillGoodsService;
import com.sjtu.seckill.service.ISeckillOrderService;
import com.sjtu.seckill.utils.MD5Util;
import com.sjtu.seckill.utils.UUIDUtil;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.OrderVO;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional
    public Order seckill(User user, GoodsVO goods) {
        //秒杀商品表的库存减一
        SeckillGoods seckillGoods = seckillGoodsService.getOne(
                new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        if(seckillGoods.getStockCount() < 1){
            redisTemplate.opsForValue().set("isEmpty:"+goods.getId(),"0");
            return null;
        }
        //更新商品库存
        boolean seckill = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
        .setSql("stock_count=stock_count - 1").eq("goods_id",goods.getId()).gt("stock_count",0));
        if(!seckill){
            return null;
        }
        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);

        //将生成秒杀的订单信息存入redis
        redisTemplate.opsForValue().set("order:" + user.getId() +":"+ goods.getId(),seckillOrder);
        return order;

    }

    @Override
    public OrderVO detail(Long orderId) {
        if(orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_ERROR);
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null){
            throw new GlobalException(RespBeanEnum.ORDER_ERROR);
        }
        GoodsVO goods = goodsService.findGoodsByGoodsId(order.getGoodsId());
        OrderVO orderVO = new OrderVO();
        orderVO.setOrder(order);
        orderVO.setGoodsVO(goods);
        return orderVO;
    }

    /**
     * @param goodsId 秒杀商品的商品ID
     * @param user
     * @return 0L 排队处理中， -1L商品库存已经为空
     */
    @Override
    public Long getSeckillOrder(Long goodsId, User user) {
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()
        ).eq("goods_id", goodsId));
        if(seckillOrder != null){
            return seckillOrder.getOrderId();
        }else if(redisTemplate.hasKey("isEmpty:" + goodsId)){
            return -1L;
        }else {
            return 0L;
        }
    }

    @Override
    public String createPath(User user, Long goodsId) {
        if(user == null){
            return "";
        }
        String path = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+goodsId,path,60, TimeUnit.SECONDS);
        return path;
    }

    @Override
    public boolean checkPath(String path, User user, Long goodsId) {
        if (user == null || path == null || goodsId < 0){
            return false;
        }
        String s = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + goodsId);
        return path.equals(s);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(user == null || goodsId < 0 || captcha == null || captcha.length() == 0){
            return false;
        }
        String s = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(s);
    }
}
