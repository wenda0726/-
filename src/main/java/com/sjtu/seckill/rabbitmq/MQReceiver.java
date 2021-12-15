package com.sjtu.seckill.rabbitmq;

import com.sjtu.seckill.pojo.SeckillMessage;
import com.sjtu.seckill.pojo.SeckillOrder;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.service.IOrderService;
import com.sjtu.seckill.utils.JsonUtil;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;
//    @RabbitListener(queues = "queue")
//    public void receive(Object msg) {
//        log.info("接受消息" + msg);
//
//    }
//
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02接受消息：" + msg);
//    }
//
//    @RabbitListener(queues = "direct_Queue01")
//    public void receiver03(Object msg){
//        log.info("direct_Queue01接受消息：" + msg);
//    }

    @RabbitListener(queues = "seckillQueue")
    public void receiver(String message){
        SeckillMessage seckillMessage = JsonUtil.jsonToPojo(message, SeckillMessage.class);
        long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        GoodsVO goodsVO = goodsService.findGoodsByGoodsId(goodsId);
        if(goodsVO.getStockCount() < 1){
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVO.getId());
        if (seckillOrder != null) {
            return;
        }
        orderService.seckill(user,goodsVO);
    }
}
