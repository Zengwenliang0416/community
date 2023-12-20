package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    private CommunityUtil() {
        throw new IllegalStateException("Utility class");
    }
    //生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // MD5加密 只能加密无法解密，每次加密都是一个值
    // 加盐之后再用MD5加密
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // 利用fastjson开发几个工具方法
    // 服务器返回浏览器的数据需要包含几个部分的内容
    // 1. 编号（code）；2.提示信息（Msg）；3.业务数据

    /**
     * 将三个参数封装成一个JSON对象
     *
     * @param code
     * @param msg
     * @param map
     * @return JSON对象字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (Map.Entry<String,Object> entry : map.entrySet()) {
                json.put(entry.getKey(),entry.getValue());
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }
}
