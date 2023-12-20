package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 处理数据
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月26日 21:46:00
 * @packageName com.nowcoder.community.service
 * @className DataService
 * @describe 处理用户登陆数据
 */
@Service
public class DataService {
    private final RedisTemplate redisTemplate;
    // 需要用到日期，提前格式化
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    public DataService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将指定的ip计入UV
     * @param ip
     */
    public void recordUV(String ip){
        // 生成key再往里存
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        // 得到Key之后往redis中记录
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    /**
     * 统计指定日期范围内的UV
     * @param start
     * @param end
     * @return
     */
    public long calculateUV(Date start, Date end){
        // 首先判断参数
        if (start==null||end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 整理该日期范围内的Key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        // 合并参数
        String redisKey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());
        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * 将指定用户计入DAU
     * @param userId
     */
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    /**
     * 统计指定日期范围内的DAU
     * @param start
     * @param end
     * @return
     */
    public long calculateDAU(Date start, Date end){
        // 首先判断参数
        if (start==null||end==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 整理该日期范围内的Key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        // 进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
