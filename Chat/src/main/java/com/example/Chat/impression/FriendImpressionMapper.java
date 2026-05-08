package com.example.Chat.impression;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FriendImpressionMapper {

    @Insert("""
        INSERT INTO friend_impressions (author_id, target_id, content, created_at)
        VALUES (#{authorId}, #{targetId}, #{content}, CURRENT_TIMESTAMP)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FriendImpression impression);

    @Select("""
        SELECT
            i.id,
            i.author_id AS authorId,
            author.username AS authorUsername,
            author.nickname AS authorNickname,
            i.target_id AS targetId,
            target.username AS targetUsername,
            target.nickname AS targetNickname,
            i.content,
            i.created_at AS createdAt
        FROM friend_impressions i
        JOIN users author ON author.id = i.author_id
        JOIN users target ON target.id = i.target_id
        WHERE i.id = #{id}
        """)
    FriendImpressionResponse findById(@Param("id") Long id);

    @Select("""
        SELECT
            i.id,
            i.author_id AS authorId,
            author.username AS authorUsername,
            author.nickname AS authorNickname,
            i.target_id AS targetId,
            target.username AS targetUsername,
            target.nickname AS targetNickname,
            i.content,
            i.created_at AS createdAt
        FROM friend_impressions i
        JOIN users author ON author.id = i.author_id
        JOIN users target ON target.id = i.target_id
        WHERE i.target_id = #{userId}
        ORDER BY i.created_at DESC, i.id DESC
        """)
    List<FriendImpressionResponse> listReceived(@Param("userId") Long userId);

    @Select("""
        SELECT
            i.id,
            i.author_id AS authorId,
            author.username AS authorUsername,
            author.nickname AS authorNickname,
            i.target_id AS targetId,
            target.username AS targetUsername,
            target.nickname AS targetNickname,
            i.content,
            i.created_at AS createdAt
        FROM friend_impressions i
        JOIN users author ON author.id = i.author_id
        JOIN users target ON target.id = i.target_id
        WHERE i.author_id = #{userId}
        ORDER BY i.created_at DESC, i.id DESC
        """)
    List<FriendImpressionResponse> listGiven(@Param("userId") Long userId);
}
