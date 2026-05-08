package com.example.Chat.user;

import com.example.Chat.common.BusinessException;
import com.example.Chat.friend.FriendMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements UserDetailsService {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final UserMapper userMapper;
    private final FriendMapper friendMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, FriendMapper friendMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.friendMapper = friendMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userMapper.countByUsername(username) > 0) {
            throw new UserAlreadyExistsException(username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setAvatarUrl("/images/default-avatar.svg");
        user.setEnabled(true);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistsException(username);
        }

        return UserProfileResponse.from(user);
    }

    public MiniProfileResponse miniProfile(Long currentUserId, Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        boolean friend = currentUserId.equals(userId) || friendMapper.countFriendship(currentUserId, userId) > 0;
        return MiniProfileResponse.from(user, friend);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(Long currentUserId, MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择头像文件");
        }
        if (avatar.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "头像文件不能超过 2MB");
        }
        String contentType = avatar.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "只支持 JPG、PNG、GIF 或 WebP 头像");
        }

        String extension = extensionFor(contentType);
        String filename = currentUserId + "-" + UUID.randomUUID() + extension;
        Path avatarDir = Path.of("uploads", "avatars").toAbsolutePath().normalize();
        Path target = avatarDir.resolve(filename).normalize();
        try {
            Files.createDirectories(avatarDir);
            avatar.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "头像保存失败");
        }

        String avatarUrl = "/uploads/avatars/" + filename;
        userMapper.updateAvatarUrl(currentUserId, avatarUrl);
        return UserProfileResponse.from(userMapper.findById(currentUserId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new ChatUserDetails(user);
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
