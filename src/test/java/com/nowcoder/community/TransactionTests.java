package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月13日 11:22:01
 * @packageName com.nowcoder.community
 * @className TransactionTests
 * @describe 测试事务管理
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {
    @Autowired
    AlphaService alphaService;
    @Test
    public void testSave1(){
        Object obj = alphaService.save1();
        System.out.println(obj);
    }
    @Test
    public void testSave2(){
        Object obj = alphaService.save2();
        System.out.println(obj);
    }
}
