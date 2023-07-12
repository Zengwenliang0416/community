package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    // 需要用service查询ticket，因此需要注入
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    // 在请求的一开始就去获取ticket，从而去查找有没有对应的user，如果有就暂存一下
    // 为什么？因为在整个请求的过程当中随时随地要用到user信息

    //该方法是通过接口定义的，该方法的参数不能去随意添加，因此不能用CookieValue注解
    //但是这个方法给了传过来的request，cookie是通过request传过来的，因此可以用request得到cookie
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从request中获取cookie，考虑到后期需要用到别的工具也会用这种方法获取cookie，因此需要进行封装
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 判断凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                // 浏览器访问服务器时是多对一的，一个服务器能够处理多个请求，是并发的，每个浏览器访问服务器时，服务器会创建一个独立的线程
                // 解决这个请求，服务器在处理这个请求时属于一个多线程的环境，因此在存储用户的过程中要考虑到多线程的情况。
                // 如果只是简单地存储在一个工具或者一个容器当中在并发的条件下可能会产生冲突
                // 如果想把数据存储到一个地方让多线程并发访问都没有问题就需要考虑线程的隔离，让每个线程单独存一份，线程与线程之间不相互干扰
                // 因此将user存储到ThreadLocal进行线程隔离
                hostHolder.setUsers(user);
            }
        }
        return true;
    }
    // 什么时候需要用到这个user？
    //  在TemplateEngine之前就要用到，在TemplateEngine被调用之前应该将user存到model里

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }
    // 什么时候将user清理？
    // 在整个请求结束之后

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        hostHolder.clear();
    }
}
