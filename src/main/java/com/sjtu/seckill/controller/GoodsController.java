package com.sjtu.seckill.controller;

import com.sjtu.seckill.config.AccessLimit;
import com.sjtu.seckill.pojo.Goods;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.service.IUserService;
import com.sjtu.seckill.vo.DetailVO;
import com.sjtu.seckill.vo.GoodsVO;
import com.sjtu.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @AccessLimit(seconds = 5, maxCounts = 5, needLogin = true)
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,HttpServletRequest request, HttpServletResponse response){
//        if(ticket == null){
//            return "login";
//        }
//        User user = userService.getUserByTicket(ticket, request, response);
////        User user = (User) session.getAttribute(ticket);
//        if(null == user){
//            return "login";
//        }
        //将页面存入redis中
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(html != null){
            return html;
        }
        //如果页面为空则需要手动渲染页面
        model.addAttribute("user",user);
        List<GoodsVO> goodsVOList = goodsService.findGoodsList();
        model.addAttribute("goodsList",goodsVOList);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(html != null){
            valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
        }
        return html;
    }

    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(@PathVariable("goodsId") Long goodsId,
                           Model model, User user,HttpServletRequest request,HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if(html != null){
            return html;
        }
        //如果为空，则需要手动渲染

        GoodsVO goodsVO = goodsService.findGoodsByGoodsId(goodsId);
        Date startDate = goodsVO.getStartDate();
        Date endDate = goodsVO.getEndDate();
        Date nowDate = new Date();
        int secKillStatus = 0;
        int remainSeconds = 0;
        if(nowDate.before(startDate)){
            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
        }else if (nowDate.after(endDate)){
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("user",user);
        model.addAttribute("goods",goodsVO);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(html != null){
            valueOperations.set("goodsDetail:" + goodsId,html,60,TimeUnit.SECONDS);
        }
        return html;
    }

    @RequestMapping("/toDetail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(@PathVariable("goodsId") Long goodsId, User user){


        GoodsVO goodsVO = goodsService.findGoodsByGoodsId(goodsId);
        Date startDate = goodsVO.getStartDate();
        Date endDate = goodsVO.getEndDate();
        Date nowDate = new Date();
        int secKillStatus = 0;
        int remainSeconds = 0;
        if(nowDate.before(startDate)){
            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
        }else if (nowDate.after(endDate)){
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            secKillStatus = 1;
            remainSeconds = 0;
        }

        DetailVO detailVO = new DetailVO();
        detailVO.setUser(user);
        detailVO.setGoodsVO(goodsVO);
        detailVO.setSecKillStatus(secKillStatus);
        detailVO.setRemainSeconds(remainSeconds);

        return RespBean.success(detailVO);
    }
}
