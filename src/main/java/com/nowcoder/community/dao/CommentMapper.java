package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年07月13日 11:48:10
 * @packageName com.nowcoder.community.dao
 * @className CommentMapper
 * @describe 评论的数据库操作
 * 查询评论需要支持分页，需要提供两个方法：某一页有多少条数据，计算总的页数
 */

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    /**
     * 添加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);
}
