package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.GroupInfo;
import com.locallink.entity.GroupMember;
import com.locallink.entity.UserInfo;
import com.locallink.vo.GroupApplyVO;

import java.util.List;
import java.util.Map;

public interface GroupInfoService extends IService<GroupInfo> {
    
    GroupInfo createGroup(String groupName, Long creatorId);
    
    void applyJoinGroup(String groupId, Long userId);
    
    void acceptApply(Long applyId, Long operatorId);
    
    void rejectApply(Long applyId, Long operatorId);
    
    List<GroupApplyVO> getPendingApplies(String groupId);
    
    void quitGroup(String groupId, Long userId);
    
    List<GroupInfo> getUserGroups(Long userId);
    
    GroupInfo getGroupInfo(String groupId);
    
    List<GroupInfo> searchGroups(String keyword);
    
    /**
     * 获取群成员列表
     */
    List<Map<String, Object>> getGroupMembers(String groupId);
    
    /**
     * 移除群成员
     */
    void removeMember(String groupId, Long operatorId, Long targetUserId);
    
    /**
     * 设置管理员
     */
    void setAdmin(String groupId, Long operatorId, Long targetUserId, boolean isAdmin);
    
    /**
     * 转让群主
     */
    void transferOwner(String groupId, Long currentOwnerId, Long newOwnerId);
    
    /**
     * 解散群组
     */
    void dismissGroup(String groupId, Long ownerId);
    
    /**
     * 修改群信息
     */
    void updateGroupInfo(String groupId, Long operatorId, String groupName, String groupAvatar);
    
    /**
     * 获取群成员信息（用于权限检查）
     */
    GroupMember getGroupMember(String groupId, Long userId);
}
