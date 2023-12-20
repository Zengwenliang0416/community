package com.nowcoder.community;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import com.nowcoder.community.entity.TestHutool;
import com.nowcoder.community.util.IPStringToByte;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.apache.commons.lang3.mutable.Mutable;
import org.elasticsearch.common.network.InetAddresses;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年12月05日 22:29:27
 * @packageName com.nowcoder.community
 * @className IPStringToByteTest
 * @describe TODO
 */
public class IPStringToByteTest {
    @Test
    public void test() {
        String ip = "127.0.0.1";
        byte[] bytes = IPStringToByte.ipStringToBytes(ip);
        System.out.println(bytes);
    }

    @Test
    public void systemParmeters() {
        Map<String, String> map = System.getenv();
        if (map != null && map.containsKey("PWD")) {
            System.out.println(map.get("PWD"));
        }
    }

    @Test
    public void hutoolTest() {
        Integer bytes = convertToBytes("23m");
    }

    @Test
    public Integer convertToBytes(String argumentSize) {
        Integer byteSize;
        String unit;
        String[] parts = argumentSize.split("(?i)(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        if (parts.length == 1) {
            // 只有数字，没有单位，默认单位为Byte
            try {
                byteSize = Integer.parseInt(parts[0]);
                return byteSize;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid argument " + parts[0]);
            }
        } else if (parts.length == 2) {
            byteSize = Integer.parseInt(parts[0]);
            unit = parts[1].toLowerCase();

            switch (unit) {
                case "b":
                case "byte":
                    return byteSize;
                case "kb":
                    return byteSize * 1024;
                case "mb":
                    return byteSize * 1024 * 1024;
                case "gb":
                    return byteSize * 1024 * 1024 * 1024;
                default:
                    throw new IllegalArgumentException("Invalid unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("Invalid file size format: " + argumentSize);
        }
    }
}
