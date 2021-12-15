package com.sjtu.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.seckill.pojo.User;
import com.sjtu.seckill.service.IUserService;
import com.sjtu.seckill.utils.CookieUtil;
import com.sjtu.seckill.vo.RespBean;
import com.sjtu.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            User user = getUser(request,response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null){
                return true; //没有注解直接跳过
            }
            int seconds = accessLimit.seconds();
            int maxCounts = accessLimit.maxCounts();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin){
                if(user == null){
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                key = key +":"+ user.getId();
            }
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Integer counts = (Integer) valueOperations.get(key);
            if(counts == null){
                valueOperations.set(key,1,seconds, TimeUnit.SECONDS);
            }else if(counts < maxCounts){
                valueOperations.increment(key);
            }else {
                render(response,RespBeanEnum.ACCESS_LIMIT_ERROR);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, RespBeanEnum sessionError) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        RespBean respBean = RespBean.error(sessionError);
        out.write(new ObjectMapper().writeValueAsString(respBean));
        out.flush();
        out.close();

    }

    public User getUser(HttpServletRequest request, HttpServletResponse response) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if(userTicket == null){
            return null;
        }
        return userService.getUserByTicket(userTicket,request,response);
    }
}
