package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.security.util.Password;

import java.util.*;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    // 注册过程中需要发送邮件，因此需要把发送邮件的客户端和邮件模版注入进来
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    // 发送邮件时需要激活码，激活码中需要包含域名和项目名，因此需要把项目名和域名也注入进来
    // 注入固定的值用@Value
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //应该返回的是包含多组信息的值，字典最合适
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        // 验证账号
        User exitUser = userMapper.selectByName(user.getUsername());
        if (exitUser != null) {
            map.put("usernameMsg", "该账号已存在，请前往登陆界面！");
            return map;
        }
        // 验证邮箱
        exitUser = userMapper.selectByEmail(user.getEmail());
        if (exitUser != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //注册使用随机图像
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空！");
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号未注册！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确，请重新输入！");
            return map;
        }
        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }
    public void updateHeader(int userId, String headerUrl){
        userMapper.updateHeader(userId, headerUrl);
    }
    public Map<String, Object> updatePassword(int userId, String oldPassword,String newPassword,String confirmPassword) {
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword)){
            map.put("updateOldMsg","请输入原密码！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("updateNewMsg","请输入新密码！");
            return map;
        }
        if (StringUtils.isBlank(confirmPassword)){
            map.put("updateConfirmMsg","请确认密码！");
            return map;
        }
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        if (!oldPassword.equals(user.getPassword())) {
            map.put("updateOldMsg", "该密码与原始密码不符合!");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        if (newPassword.equals(oldPassword)) {
            map.put("updateNewMsg", "新密码与旧密码相同，请重新输入！");
            return map;
        }
        confirmPassword = CommunityUtil.md5(confirmPassword+user.getSalt());
        if (!newPassword.equals(confirmPassword)) {
            map.put("updateConfirmMsg", "确认密码与新密码不一致，请重新输入！");
            return map;
        }
        userMapper.updatePassword(userId,newPassword);
        return map;
    }
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }
}
