package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月15日 15:21:46
 * @packageName com.nowcoder.community.controller
 * @className LikeController
 * @describe TODO
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    处理异步请求的方法
     */
    @LoginRequired
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 需要用拦截器判断用户登陆状态
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态呢
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 传给页面
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        if (entityType == ENTITY_TYPE_POST) {
            // 给帖子初始分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            // 把帖子id放到redis中（存到set中）
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
