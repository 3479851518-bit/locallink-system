package com.locallink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.common.ResultCode;
import com.locallink.entity.FriendRelation;
import com.locallink.entity.UserInfo;
import com.locallink.enums.FriendStatusEnum;
import com.locallink.mapper.FriendRelationMapper;
import com.locallink.service.FriendRelationService;
import com.locallink.service.UserInfoService;
import com.locallink.vo.FriendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FriendRelationServiceImpl extends ServiceImpl<FriendRelationMapper, FriendRelation> implements FriendRelationService {
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private com.locallink.service.ChatMessageService chatMessageService;
    
    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException("不能添加自己为好友");
        }
        
        // 检查是否已经是好友（双向检查）
        FriendRelation exist1 = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .one();
        
        if (exist1 != null && exist1.getStatus().equals(FriendStatusEnum.ACCEPTED.getCode())) {
            throw new BusinessException(ResultCode.FRIEND_ALREADY_EXIST);
        }
        
        // 检查对方是否已经添加了自己
        FriendRelation exist2 = this.lambdaQuery()
                .eq(FriendRelation::getUserId, friendId)
                .eq(FriendRelation::getFriendId, userId)
                .one();
        
        if (exist2 != null && exist2.getStatus().equals(FriendStatusEnum.ACCEPTED.getCode())) {
            throw new BusinessException(ResultCode.FRIEND_ALREADY_EXIST);
        }
        
        // 如果已有待处理的申请
        if (exist1 != null && exist1.getStatus().equals(FriendStatusEnum.PENDING.getCode())) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_PENDING);
        }
        
        // 如果之前被拒绝过，可以重新发送申请
        if (exist1 != null && exist1.getStatus().equals(FriendStatusEnum.REJECTED.getCode())) {
            exist1.setStatus(FriendStatusEnum.PENDING.getCode());
            this.updateById(exist1);
            log.info("用户{}重新向用户{}发送好友申请", userId, friendId);
            return;
        }
        
        // 创建新的的好友申请
        if (exist1 == null) {
            FriendRelation relation = new FriendRelation();
            relation.setUserId(userId);
            relation.setFriendId(friendId);
            relation.setStatus(FriendStatusEnum.PENDING.getCode());
            this.save(relation);
        }
        
        log.info("用户{}向用户{}发送好友申请", userId, friendId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptFriend(Long relationId) {
        FriendRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_EXIST);
        }
        
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        this.updateById(relation);
        
        FriendRelation reverseRelation = new FriendRelation();
        reverseRelation.setUserId(relation.getFriendId());
        reverseRelation.setFriendId(relation.getUserId());
        reverseRelation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        this.save(reverseRelation);
        
        log.info("用户{}接受了好友申请", relation.getUserId());
    }
    
    @Override
    public void rejectFriend(Long relationId) {
        FriendRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new BusinessException(ResultCode.FRIEND_NOT_EXIST);
        }
        
        relation.setStatus(FriendStatusEnum.REJECTED.getCode());
        this.updateById(relation);
        
        log.info("用户{}拒绝了好友申请", relation.getUserId());
    }
    
    @Override
    public List<FriendVO> getFriendList(Long userId) {
        List<FriendRelation> relations = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getStatus, FriendStatusEnum.ACCEPTED.getCode())
                .list();
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量获取所有好友ID
        List<Long> friendIds = relations.stream()
                .map(FriendRelation::getFriendId)
                .collect(java.util.stream.Collectors.toList());
        
        // 一次性查询所有好友信息，避免N+1问题
        List<UserInfo> friends = userInfoService.listByIds(friendIds);
        
        // 构建userId到UserInfo的映射
        java.util.Map<Long, UserInfo> friendMap = friends.stream()
                .collect(java.util.stream.Collectors.toMap(UserInfo::getId, f -> f));
        
        // 构建friendId到remark的映射
        java.util.Map<Long, String> remarkMap = relations.stream()
                .collect(java.util.stream.Collectors.toMap(
                    FriendRelation::getFriendId,
                    r -> r.getRemark() != null ? r.getRemark() : "",
                    (existing, replacement) -> existing
                ));
        
        // 组装结果
        List<FriendVO> friendList = new ArrayList<>();
        for (Long friendId : friendIds) {
            UserInfo friend = friendMap.get(friendId);
            if (friend != null) {
                FriendVO vo = new FriendVO();
                BeanUtils.copyProperties(friend, vo);
                vo.setUserId(friend.getId());
                vo.setRemark(remarkMap.get(friendId));
                friendList.add(vo);
            }
        }
        
        return friendList;
    }
    
    @Override
    public List<FriendRelation> getPendingRequests(Long userId) {
        log.info("查询待处理申请: friendId={}, status={}", userId, FriendStatusEnum.PENDING.getCode());
        // 兼容status为NULL的情况，将NULL也视为待验证状态
        List<FriendRelation> list = this.lambdaQuery()
                .eq(FriendRelation::getFriendId, userId)
                .and(wrapper -> wrapper
                    .eq(FriendRelation::getStatus, FriendStatusEnum.PENDING.getCode())
                    .or()
                    .isNull(FriendRelation::getStatus)
                )
                .list();
        log.info("查询结果数量: {}", list.size());
        return list;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFriend(Long userId, Long friendId) {
        // 先删除聊天记录
        try {
            chatMessageService.deletePrivateMessages(userId, friendId);
            log.info("已删除用户{}和{}之间的聊天记录", userId, friendId);
        } catch (Exception e) {
            log.error("删除用户{}和{}之间的聊天记录失败", userId, friendId, e);
        }
        
        // 再删除好友关系
        this.lambdaUpdate()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .remove();
        
        this.lambdaUpdate()
                .eq(FriendRelation::getUserId, friendId)
                .eq(FriendRelation::getFriendId, userId)
                .remove();
        
        log.info("用户{}删除了好友{}", userId, friendId);
    }
    
    @Override
    public void updateRemark(Long userId, Long friendId, String remark) {
        FriendRelation relation = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .one();
        
        if (relation == null) {
            throw new BusinessException("好友关系不存在");
        }
        
        relation.setRemark(remark);
        this.updateById(relation);
        
        log.info("用户{}修改好友{}的备注为{}", userId, friendId, remark);
    }
    
    @Override
    public FriendVO getFriendDetail(Long userId, Long friendId) {
        FriendRelation relation = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .one();
        
        if (relation == null) {
            throw new BusinessException("好友关系不存在");
        }
        
        UserInfo friend = userInfoService.getById(friendId);
        if (friend == null) {
            throw new BusinessException("好友信息不存在");
        }
        
        FriendVO vo = new FriendVO();
        BeanUtils.copyProperties(friend, vo);
        vo.setUserId(friend.getId());
        vo.setRemark(relation.getRemark());
        vo.setIsBlocked(relation.getIsBlocked() != null && relation.getIsBlocked() == 1);
        
        return vo;
    }
    
    @Override
    public void blockFriend(Long userId, Long friendId) {
        FriendRelation relation = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .one();
        
        if (relation == null) {
            throw new BusinessException("好友关系不存在");
        }
        
        relation.setIsBlocked(1);
        this.updateById(relation);
        
        log.info("用户{}拉黑了好友{}", userId, friendId);
    }
    
    @Override
    public void unblockFriend(Long userId, Long friendId) {
        FriendRelation relation = this.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .one();
        
        if (relation == null) {
            throw new BusinessException("好友关系不存在");
        }
        
        relation.setIsBlocked(0);
        this.updateById(relation);
        
        log.info("用户{}取消拉黑好友{}", userId, friendId);
    }
}