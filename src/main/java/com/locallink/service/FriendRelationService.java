package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.FriendRelation;
import com.locallink.vo.FriendVO;

import java.util.List;

public interface FriendRelationService extends IService<FriendRelation> {
    
    void addFriend(Long userId, Long friendId);
    
    void acceptFriend(Long relationId);
    
    void rejectFriend(Long relationId);
    
    List<FriendVO> getFriendList(Long userId);
    
    List<FriendRelation> getPendingRequests(Long userId);
    
    void removeFriend(Long userId, Long friendId);
    
    /**
     * 修改好友备注
     */
    void updateRemark(Long userId, Long friendId, String remark);
    
    /**
     * 获取好友详情
     */
    FriendVO getFriendDetail(Long userId, Long friendId);
    
    /**
     * 拉黑好友
     */
    void blockFriend(Long userId, Long friendId);
    
    /**
     * 取消拉黑
     */
    void unblockFriend(Long userId, Long friendId);
}
