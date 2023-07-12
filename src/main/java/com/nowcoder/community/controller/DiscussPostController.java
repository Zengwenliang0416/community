package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月12日 22:30:09
 * @packageName com.nowcoder.community.controller
 * @className DiscussPostController
 * @describe 处理帖子相关的操作
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        // 发帖的前提是我处于登陆状态
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        }
        // 创建post对象
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        // 报错情况，将来统一处理。
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        // 需要做进一步的处理（用户ID），在页面上我们希望看到的是帖子的作者
        // 有两种办法：
        // 1. 在Mapper中实现查询的时候写一个关联查询，在查到帖子的数据的同时查到作者的数据
        // 优点：效率高   缺点：存在冗余或者耦合，因为在有的功能中并不需要展现用户的这些信息
        // 2. 根据当前我们查到的数据调用userService查到作者的信息，然后通过model将作者数据发送给模版
        // 优点：没有冗余，比较明确 缺点：效率低（可以通过redis解决）
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        return "/site/discuss-detail";
    }
}
