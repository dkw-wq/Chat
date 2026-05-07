package com.example.Chat.friend;

import com.example.Chat.common.BusinessException;
import com.example.Chat.user.UserMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendService {

    private static final String PENDING = "PENDING";
    private static final String ACCEPTED = "ACCEPTED";
    private static final String REJECTED = "REJECTED";

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;

    public FriendService(FriendMapper friendMapper, UserMapper userMapper) {
        this.friendMapper = friendMapper;
        this.userMapper = userMapper;
    }

    public List<UserSearchResponse> searchUsers(Long currentUserId, String keyword) {
        return friendMapper.searchUsers(currentUserId, keyword.trim());
    }

    public List<FriendResponse> listFriends(Long currentUserId) {
        return friendMapper.listFriends(currentUserId);
    }

    public List<FriendRequestResponse> listIncomingRequests(Long currentUserId) {
        return friendMapper.listIncomingRequests(currentUserId);
    }

    public List<FriendRequestResponse> listOutgoingRequests(Long currentUserId) {
        return friendMapper.listOutgoingRequests(currentUserId);
    }

    @Transactional
    public FriendRequestResponse sendRequest(Long currentUserId, SendFriendRequest request) {
        Long receiverId = request.receiverId();
        if (currentUserId.equals(receiverId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能添加自己为好友");
        }
        if (userMapper.findById(receiverId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        if (friendMapper.countFriendship(currentUserId, receiverId) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "你们已经是好友");
        }

        FriendRequestResponse latestRequest = friendMapper.findLatestRequestBetween(currentUserId, receiverId);
        if (latestRequest != null && PENDING.equals(latestRequest.status())) {
            throw new BusinessException(HttpStatus.CONFLICT, "已有待处理的好友申请");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequesterId(currentUserId);
        friendRequest.setReceiverId(receiverId);
        friendRequest.setMessage(normalizeMessage(request.message()));

        try {
            if (latestRequest == null) {
                friendMapper.insertRequest(friendRequest);
            } else {
                friendRequest.setId(latestRequest.id());
                friendMapper.resendRequest(friendRequest);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "好友申请已存在");
        }

        return friendMapper.findRequestById(friendRequest.getId());
    }

    @Transactional
    public void acceptRequest(Long currentUserId, Long requestId) {
        FriendRequestResponse request = requireRequestForReceiver(currentUserId, requestId);
        if (!PENDING.equals(request.status())) {
            throw new BusinessException(HttpStatus.CONFLICT, "该申请已处理");
        }

        friendMapper.updateRequestStatus(requestId, ACCEPTED);
        insertFriendshipIfMissing(request.requesterId(), request.receiverId());
        insertFriendshipIfMissing(request.receiverId(), request.requesterId());
    }

    @Transactional
    public void rejectRequest(Long currentUserId, Long requestId) {
        FriendRequestResponse request = requireRequestForReceiver(currentUserId, requestId);
        if (!PENDING.equals(request.status())) {
            throw new BusinessException(HttpStatus.CONFLICT, "该申请已处理");
        }

        friendMapper.updateRequestStatus(requestId, REJECTED);
    }

    @Transactional
    public void deleteFriend(Long currentUserId, Long friendId) {
        if (friendMapper.countFriendship(currentUserId, friendId) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "好友关系不存在");
        }
        friendMapper.deleteFriendship(currentUserId, friendId);
    }

    private FriendRequestResponse requireRequestForReceiver(Long currentUserId, Long requestId) {
        FriendRequestResponse request = friendMapper.findRequestById(requestId);
        if (request == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "好友申请不存在");
        }
        if (!currentUserId.equals(request.receiverId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能处理发送给你的好友申请");
        }
        return request;
    }

    private void insertFriendshipIfMissing(Long userId, Long friendId) {
        if (friendMapper.countFriendship(userId, friendId) == 0) {
            friendMapper.insertFriendship(userId, friendId);
        }
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        return message.trim();
    }
}
