package com.example.Chat.friend;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FriendMapper {

    @Select("""
        SELECT
            u.id,
            u.username,
            u.nickname,
            u.avatar_url AS avatarUrl,
            CASE
                WHEN f.id IS NOT NULL THEN 'FRIEND'
                WHEN sent.id IS NOT NULL THEN 'PENDING_SENT'
                WHEN received.id IS NOT NULL THEN 'PENDING_RECEIVED'
                ELSE 'NONE'
            END AS relationshipStatus
        FROM users u
        LEFT JOIN friendships f
            ON f.user_id = #{currentUserId}
            AND f.friend_id = u.id
        LEFT JOIN friend_requests sent
            ON sent.requester_id = #{currentUserId}
            AND sent.receiver_id = u.id
            AND sent.status = 'PENDING'
        LEFT JOIN friend_requests received
            ON received.requester_id = u.id
            AND received.receiver_id = #{currentUserId}
            AND received.status = 'PENDING'
        WHERE u.id <> #{currentUserId}
            AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY u.username
        LIMIT 20
        """)
    List<UserSearchResponse> searchUsers(@Param("currentUserId") Long currentUserId, @Param("keyword") String keyword);

    @Select("""
        SELECT
            u.id,
            u.username,
            u.nickname,
            u.avatar_url AS avatarUrl,
            f.created_at AS friendSince
        FROM friendships f
        JOIN users u ON u.id = f.friend_id
        WHERE f.user_id = #{userId}
        ORDER BY u.nickname, u.username
        """)
    List<FriendResponse> listFriends(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM friendships WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int countFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Insert("INSERT INTO friendships (user_id, friend_id, created_at) VALUES (#{userId}, #{friendId}, CURRENT_TIMESTAMP)")
    int insertFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Delete("""
        DELETE FROM friendships
        WHERE (user_id = #{userId} AND friend_id = #{friendId})
            OR (user_id = #{friendId} AND friend_id = #{userId})
        """)
    int deleteFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("""
        SELECT
            r.id,
            r.requester_id AS requesterId,
            requester.username AS requesterUsername,
            requester.nickname AS requesterNickname,
            requester.avatar_url AS requesterAvatarUrl,
            r.receiver_id AS receiverId,
            receiver.username AS receiverUsername,
            receiver.nickname AS receiverNickname,
            receiver.avatar_url AS receiverAvatarUrl,
            r.status,
            r.message,
            r.created_at AS createdAt,
            r.updated_at AS updatedAt
        FROM friend_requests r
        JOIN users requester ON requester.id = r.requester_id
        JOIN users receiver ON receiver.id = r.receiver_id
        WHERE r.id = #{id}
        """)
    FriendRequestResponse findRequestById(@Param("id") Long id);

    @Select("""
        SELECT
            r.id,
            r.requester_id AS requesterId,
            requester.username AS requesterUsername,
            requester.nickname AS requesterNickname,
            requester.avatar_url AS requesterAvatarUrl,
            r.receiver_id AS receiverId,
            receiver.username AS receiverUsername,
            receiver.nickname AS receiverNickname,
            receiver.avatar_url AS receiverAvatarUrl,
            r.status,
            r.message,
            r.created_at AS createdAt,
            r.updated_at AS updatedAt
        FROM friend_requests r
        JOIN users requester ON requester.id = r.requester_id
        JOIN users receiver ON receiver.id = r.receiver_id
        WHERE r.receiver_id = #{userId}
        ORDER BY r.updated_at DESC
        """)
    List<FriendRequestResponse> listIncomingRequests(@Param("userId") Long userId);

    @Select("""
        SELECT
            r.id,
            r.requester_id AS requesterId,
            requester.username AS requesterUsername,
            requester.nickname AS requesterNickname,
            requester.avatar_url AS requesterAvatarUrl,
            r.receiver_id AS receiverId,
            receiver.username AS receiverUsername,
            receiver.nickname AS receiverNickname,
            receiver.avatar_url AS receiverAvatarUrl,
            r.status,
            r.message,
            r.created_at AS createdAt,
            r.updated_at AS updatedAt
        FROM friend_requests r
        JOIN users requester ON requester.id = r.requester_id
        JOIN users receiver ON receiver.id = r.receiver_id
        WHERE r.requester_id = #{userId}
        ORDER BY r.updated_at DESC
        """)
    List<FriendRequestResponse> listOutgoingRequests(@Param("userId") Long userId);

    @Select("""
        SELECT
            r.id,
            r.requester_id AS requesterId,
            requester.username AS requesterUsername,
            requester.nickname AS requesterNickname,
            requester.avatar_url AS requesterAvatarUrl,
            r.receiver_id AS receiverId,
            receiver.username AS receiverUsername,
            receiver.nickname AS receiverNickname,
            receiver.avatar_url AS receiverAvatarUrl,
            r.status,
            r.message,
            r.created_at AS createdAt,
            r.updated_at AS updatedAt
        FROM friend_requests r
        JOIN users requester ON requester.id = r.requester_id
        JOIN users receiver ON receiver.id = r.receiver_id
        WHERE (r.requester_id = #{userId} AND r.receiver_id = #{otherUserId})
            OR (r.requester_id = #{otherUserId} AND r.receiver_id = #{userId})
        ORDER BY r.updated_at DESC
        LIMIT 1
        """)
    FriendRequestResponse findLatestRequestBetween(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    @Insert("""
        INSERT INTO friend_requests (requester_id, receiver_id, status, message, created_at, updated_at)
        VALUES (#{requesterId}, #{receiverId}, 'PENDING', #{message}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRequest(FriendRequest request);

    @Update("""
        UPDATE friend_requests
        SET requester_id = #{requesterId},
            receiver_id = #{receiverId},
            status = 'PENDING',
            message = #{message},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int resendRequest(FriendRequest request);

    @Update("UPDATE friend_requests SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateRequestStatus(@Param("id") Long id, @Param("status") String status);
}
