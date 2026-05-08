package com.example.Chat.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("""
        SELECT
            id,
            username,
            password_hash AS passwordHash,
            nickname,
            avatar_url AS avatarUrl,
            enabled,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM users
        WHERE username = #{username}
        """)
    User findByUsername(@Param("username") String username);

    @Select("""
        SELECT
            id,
            username,
            password_hash AS passwordHash,
            nickname,
            avatar_url AS avatarUrl,
            enabled,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM users
        WHERE id = #{id}
        """)
    User findById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Insert("""
        INSERT INTO users (username, password_hash, nickname, avatar_url, enabled, created_at, updated_at)
        VALUES (#{username}, #{passwordHash}, #{nickname}, #{avatarUrl}, #{enabled}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET avatar_url = #{avatarUrl}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAvatarUrl(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);
}
