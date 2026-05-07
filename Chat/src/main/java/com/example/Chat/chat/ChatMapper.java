package com.example.Chat.chat;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatMapper {

    @Insert("""
        INSERT INTO chat_messages (sender_id, receiver_id, content, created_at)
        VALUES (#{senderId}, #{receiverId}, #{content}, CURRENT_TIMESTAMP)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Select("""
        SELECT
            m.id,
            m.sender_id AS senderId,
            sender.username AS senderUsername,
            sender.nickname AS senderNickname,
            m.receiver_id AS receiverId,
            receiver.username AS receiverUsername,
            receiver.nickname AS receiverNickname,
            m.content,
            m.created_at AS createdAt
        FROM chat_messages m
        JOIN users sender ON sender.id = m.sender_id
        JOIN users receiver ON receiver.id = m.receiver_id
        WHERE m.id = #{id}
        """)
    ChatMessageResponse findById(@Param("id") Long id);

    @Select("""
        SELECT *
        FROM (
            SELECT
                m.id,
                m.sender_id AS senderId,
                sender.username AS senderUsername,
                sender.nickname AS senderNickname,
                m.receiver_id AS receiverId,
                receiver.username AS receiverUsername,
                receiver.nickname AS receiverNickname,
                m.content,
                m.created_at AS createdAt
            FROM chat_messages m
            JOIN users sender ON sender.id = m.sender_id
            JOIN users receiver ON receiver.id = m.receiver_id
            WHERE (m.sender_id = #{currentUserId} AND m.receiver_id = #{friendId})
                OR (m.sender_id = #{friendId} AND m.receiver_id = #{currentUserId})
            ORDER BY m.created_at DESC, m.id DESC
            LIMIT #{limit}
        ) recent_messages
        ORDER BY createdAt ASC, id ASC
        """)
    List<ChatMessageResponse> listConversation(
        @Param("currentUserId") Long currentUserId,
        @Param("friendId") Long friendId,
        @Param("limit") int limit
    );
}
