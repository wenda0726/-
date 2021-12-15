package com.sjtu.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.sjtu.seckill.config.AccessLimit;
import com.sjtu.seckill.exception.GlobalException;
import com.sjtu.seckill.pojo.Order;
import com.sjtu.seckill.pojo.SeckillMessage;
import com.sjtu.seckill.pojo.SeckillOrder;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.rabbitmq.MQSender;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.service.IOrderService;
import com.sjtu.seckill.service.ISeckillOrderService;
import com.sjtu.seckill.utils.JsonUtil;
import com.sjtu.seckill.vo.DetailVO;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
@Slf4j
public class SecKillController implements InitializingBean { //在初始化时将数据库中的库存数量存入redis

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    //利用内存，在商品库存已经为空时，减少对redis的访问
    Map<Long,Boolean> isEmpty = new HashMap<>();

    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId){
        if(user == null){
            return "login";
        }
        GoodsVO goods = goodsService.findGoodsByGoodsId(goodsId);
        if(goods.getStockCount() < 1){
            model.addAttribute("errorMsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断该用户是否已经购买过当前商品
        SeckillOrder seckillOrder = seckillOrderService.getOne(
                new QueryWrapper<SeckillOrder>()
                        .eq("user_id", user.getId())
                        .eq("goods_id", goodsId));

        if(seckillOrder != null){
            model.addAttribute("errorMsg",RespBeanEnum.REPEAT_BUY.getMessage());
            return "secKillFail";
        }
        //可以下单购买
        Order order = orderService.seckill(user,goods);
        model.addAttribute("goods",goods);
        model.addAttribute("order",order);

        return "orderDetail";
    }


    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable("path") String path, User user, Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkPath(path,user,goodsId);
        if(!check){
            return RespBean.error(RespBeanEnum.PATH_ILLEGAL);
        }
        GoodsVO goods = goodsService.findGoodsByGoodsId(goodsId);
        //如果库存已经为空，则直接返回，减少对redis的访问
        if(isEmpty.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复购物
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goods.getId());
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_BUY);
        }
        //利用redis进行库存预先扣减的时候没有原子行，实现redis分布式锁
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);

//        Long stock = redisTemplate.opsForValue().decrement("secKillGoods:" + goodsId);

        if(stock < 0){
            isEmpty.put(goodsId,true);
            redisTemplate.opsForValue().increment("secKillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
//        GoodsVO goods = goodsService.findGoodsByGoodsId(goodsId);
//        if(goods.getStockCount() < 1){
////            model.addAttribute("errorMsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //判断该用户是否已经购买过当前商品
////        SeckillOrder seckillOrder = seckillOrderService.getOne(
////                new QueryWrapper<SeckillOrder>()
////                        .eq("user_id", user.getId())
////                        .eq("goods_id", goodsId));
//
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:"+user.getId()+":"+goods.getId());
//        if(seckillOrder != null){
//            return RespBean.error(RespBeanEnum.REPEAT_BUY);
//        }
//        //可以下单购买
//        Order order = orderService.seckill(user,goods);
//        return RespBean.success(order);
        SeckillMessage seckillMessage = new SeckillMessage(goodsId,user);
        String message = JsonUtil.objectToJson(seckillMessage);
        mqSender.send(message);

        return RespBean.success(0);
    }

    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user == null){
            return  RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long oderId = orderService.getSeckillOrder(goodsId,user);
        return RespBean.success(oderId);
    }

    /**
     * 查询秒杀地址，用户ID和商品ID唯一绑定
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(seconds = 5, maxCounts = 5, needLogin = true)
    @RequestMapping(value = "/getPath",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId,String captcha){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }
        String path = orderService.createPath(user,goodsId);
        return RespBean.success(path);
    }


    @RequestMapping(value = "/captcha",method = RequestMethod.GET)
    public void captcha(User user, Long goodsId, HttpServletResponse response){
        if(user == null || goodsId < 0){
            throw new GlobalException(RespBeanEnum.SESSION_ERROR);
        }
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放在redis中
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48,3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码输出错误："+e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVO> goodsList = goodsService.findGoodsList();
        if(goodsList == null){
            return;
        }
        for(int i = 0; i < goodsList.size(); i++){
            GoodsVO goodsVO = goodsList.get(i);
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVO.getId(),goodsVO.getStockCount());
            isEmpty.put(goodsVO.getId(),false);
        }
    }
}
