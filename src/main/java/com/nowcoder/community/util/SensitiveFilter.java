package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月12日 14:35:20
 * @packageName com.nowcoder.community.util
 * @className SensitiveFilter
 * @describe TODO
 */
// 为了方便复用，将这个类托管到容器，由容器来管理
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 设置敏感词替换符号
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode rootNode = new TrieNode();

    // 根据敏感词数据文件构造前缀树，使用方法去构造树，且这个树应该在程序初次调用这个工具的时候初始化即可
    // 使用@PostCostruct注解实现，该注解表示这是一个初始化方法，当容器实例化这个bean之后这个方法会被自动调用，这个bean在服务器启动时被初始化
    @PostConstruct
    public void init() {
        // 读取文件中的字符，使用类加载器，类加载器从类路径中器加载资源
        try (
                // 字节流需要关闭，因此需要放在try中
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 从字节流中去读取这个文件不太方便，需要转换成字符流，直接使用reader不是很方便，最好采用缓冲流，可以提高效率
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            // 通过reader去读取每一个敏感词
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addkeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词失败：" + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     *
     * @param keyword
     */
    private void addkeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1
        TrieNode temNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 记录最终的结果
        StringBuilder result = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号比如？嫖？？娼？？
            if (isSymbol(c)) {
                // 如果指针1属于根节点，将此符号计入结果，让指针2往下走
                if (temNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            // 检查下级节点
            temNode = temNode.getSubNode(c);
            if (temNode == null) {
                // 以begin为开头的字符串不是敏感词，存入结果
                result.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                temNode = rootNode;
            } else if (temNode.isKeyWordEnd()) {
                // 发现敏感词，将begin开头，position结尾的字符串替换
                result.append(REPLACEMENT);
                begin = ++position;
                // 重新指向根节点
                temNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }
        // 将最后一批字符计入结果
        result.append(text.substring(begin));
        return String.valueOf(result);
    }

    /**
     * 判断字符是否为特殊符号
     *
     * @param c 字符
     * @return 是或者否
     */
    private boolean isSymbol(Character c) {
        // 0x2e80-0x9FFF为东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2e80 || c > 0x9FFF);
    }

    /**
     * 构建敏感词前缀树
     */
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeyWordEnd = false;
        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
