package com.example.Chat.impression;

import com.example.Chat.common.BusinessException;
import com.example.Chat.friend.FriendMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendImpressionService {

    private final FriendImpressionMapper friendImpressionMapper;
    private final FriendMapper friendMapper;

    public FriendImpressionService(FriendImpressionMapper friendImpressionMapper, FriendMapper friendMapper) {
        this.friendImpressionMapper = friendImpressionMapper;
        this.friendMapper = friendMapper;
    }

    public List<FriendImpressionResponse> listReceived(Long currentUserId) {
        return friendImpressionMapper.listReceived(currentUserId);
    }

    public List<FriendImpressionResponse> listGiven(Long currentUserId) {
        return friendImpressionMapper.listGiven(currentUserId);
    }

    @Transactional
    public FriendImpressionResponse add(Long currentUserId, AddImpressionRequest request) {
        if (currentUserId.equals(request.targetId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能给自己添加好友印象");
        }
        if (friendMapper.countFriendship(currentUserId, request.targetId()) == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能给好友添加印象");
        }

        FriendImpression impression = new FriendImpression();
        impression.setAuthorId(currentUserId);
        impression.setTargetId(request.targetId());
        impression.setContent(request.content().trim());
        friendImpressionMapper.insert(impression);

        return friendImpressionMapper.findById(impression.getId());
    }
}
