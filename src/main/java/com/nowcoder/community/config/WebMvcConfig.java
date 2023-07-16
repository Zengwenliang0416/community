package com.nowcoder.community.config;

import com.nowcoder.community.controller.MessageInterceptor;
import com.nowcoder.community.controller.interceptor.AlphaInterceptor;
import com.nowcoder.community.controller.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置文件
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //在这个配置中主要配置拦截类，因此需要将拦截器注入进来
    @Autowired
    private AlphaInterceptor alphaInterceptor;
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    private MessageInterceptor messageInterceptor;

    // 注册接口，spring在调用的时候会将registry对象传进来，根据传进来的对象注册intercepter
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(alphaInterceptor);//拦截一切请求
        registry.addInterceptor(alphaInterceptor)// 设置不需要拦截的路径，在一个项目当中，静态页面往往是不需要进行拦截的。
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpeg","/**/*.png","/**/*.jpg" ) //添加不需要被拦截的路径
                .addPathPatterns("/register","/login");//添加明确需要拦截的路径

        registry.addInterceptor(loginTicketInterceptor)// 设置不需要拦截的路径，在一个项目当中，静态页面往往是不需要进行拦截的。
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpeg","/**/*.png","/**/*.jpg" ); //添加不需要被拦截的路径

        registry.addInterceptor(loginRequiredInterceptor)// 设置不需要拦截的路径，在一个项目当中，静态页面往往是不需要进行拦截的。
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpeg","/**/*.png","/**/*.jpg" ); //添加不需要被拦截的路径
        registry.addInterceptor(messageInterceptor)// 设置不需要拦截的路径，在一个项目当中，静态页面往往是不需要进行拦截的。
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.jpeg","/**/*.png","/**/*.jpg" ); //添加不需要被拦截的路径
    }
}
