package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月11日 22:44:52
 * @packageName com.nowcoder.community.controller.interceptor
 * @className LoginRequiredInterceptor
 * @describe 使用拦截器尝试拦截带有注解的方法
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    // 在什么时候去判断登录状态呢？请求最开始的时候

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 首先要判断我们拦截的目标（handler）是不是一个方法，他有可能时静态资源，如果不是方法就不拦截
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 提取method的loginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 如果loginRequired不为空标识该方法需要登陆才能够访问，因此再检查状态
            if (loginRequired != null && hostHolder.getUser()==null) {
                // 没有登陆则重定向到登陆页面
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        // 拦截器开发完之后需要进行配置，指定生效的路径。
        return true;
    }
}
