package com.nowcoder.community.util;

import jdk.nashorn.internal.ir.IfNode;

import java.util.Date;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月15日 15:03:29
 * @packageName com.nowcoder.community.util
 * @className RedisKeyUtil
 * @describe 生成RedisKey
 */
public class RedisKeyUtil {
    // 声明Key的一部分的常量
    private static final String SPLIT = ":";
    // 存储帖子和评论的赞，这些实体的赞通过一个前缀开头
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 存储喜欢的用户收到的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注的目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 目标的关注者
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码的Key
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登陆凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户缓存
    private static final String PREFIX_USER = "user";
    // 独立访客
    private static final String PREFIX_UV = "uv";
    // 日活跃用户
    private static final String PREFIX_DAU = "dau";
    // 帖子的热度
    private static final String PREFIX_POST = "post";


    // 通过一个方法获得传入redis中的key
    // like:entity:entityType:entityId -> set(userId)需要知道有哪些人给帖子或者评论点了赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + entityId;
    }

    /*
    某个用户的赞，传入的是用户的ID
    like:user:userId -> int
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /*
    某个用户的关注的实体
    followee:userId:entityType -> zset(entityId,now)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /*
    某个实体拥有的粉丝
    follower:entityType:entityId -> zset(userId,now)
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登陆凭证
     *
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户缓存数据的Key
     *
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 独立访客的key
     *
     * @param date
     * @return
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间UV的key
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日DAU的key
     *
     * @param date
     * @return
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间DAU的key
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 帖子的热度
     * 产生变化的post是多个而不是某一个，因此不用传条件进来
     * @return
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }


}
