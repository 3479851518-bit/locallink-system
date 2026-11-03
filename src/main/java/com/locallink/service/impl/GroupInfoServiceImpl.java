package com.locallink.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.common.ResultCode;
import com.locallink.entity.GroupInfo;
import com.locallink.entity.GroupMember;
import com.locallink.entity.UserInfo;
import com.locallink.enums.GroupApplyStatusEnum;
import com.locallink.enums.GroupRoleEnum;
import com.locallink.mapper.GroupInfoMapper;
import com.locallink.mapper.GroupMemberMapper;
import com.locallink.service.GroupInfoService;
import com.locallink.service.UserInfoService;
import com.locallink.vo.GroupApplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService {
    
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private com.locallink.service.ChatMessageService chatMessageService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupInfo createGroup(String groupName, Long creatorId) {
        log.info("开始创建群组: {}, 创建者: {}", groupName, creatorId);
        
        // 创建群组信息
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(IdUtil.fastSimpleUUID());
        groupInfo.setGroupName(groupName);
        groupInfo.setCreatorId(creatorId);
        groupInfo.setCreateTime(LocalDateTime.now());
        this.save(groupInfo);
        
        log.info("群组信息保存成功，group_id: {}", groupInfo.getGroupId());
        
        // 创建群主成员记录（自动通过审核）
        GroupMember member = new GroupMember();
        member.setGroupId(groupInfo.getGroupId());
        member.setUserId(creatorId);
        member.setRole(GroupRoleEnum.OWNER.getCode());
        member.setApplyStatus(GroupApplyStatusEnum.ACCEPTED.getCode()); // 设置为已通过
        member.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);
        
        log.info("群主成员记录保存成功，apply_status: {}", member.getApplyStatus());
        log.info("群组创建完成: {}, 创建者: {}", groupName, creatorId);
        return groupInfo;
    }
    
    @Override
    public void applyJoinGroup(String groupId, Long userId) {
        // 检查群组是否存在
        GroupInfo groupInfo = this.lambdaQuery()
                .eq(GroupInfo::getGroupId, groupId)
                .one();
        
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 检查用户是否已经是群成员
        GroupMember existMember = groupMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("user_id", userId)
        );
        
        if (existMember != null) {
            if (existMember.getApplyStatus() != null && existMember.getApplyStatus().equals(GroupApplyStatusEnum.ACCEPTED.getCode())) {
                throw new BusinessException("您已经是该群成员");
            } else if (existMember.getApplyStatus() != null && existMember.getApplyStatus().equals(GroupApplyStatusEnum.PENDING.getCode())) {
                throw new BusinessException("您的申请正在审核中，请耐心等待");
            }
            // 如果被拒绝过，可以重新申请，更新申请状态
            existMember.setApplyStatus(GroupApplyStatusEnum.PENDING.getCode());
            existMember.setApplyTime(LocalDateTime.now());
            existMember.setAuditTime(null);
            groupMemberMapper.updateById(existMember);
        } else {
            // 创建新的申请记录
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(GroupRoleEnum.MEMBER.getCode());
            member.setApplyStatus(GroupApplyStatusEnum.PENDING.getCode());
            member.setApplyTime(LocalDateTime.now());
            groupMemberMapper.insert(member);
        }
        
        log.info("用户{}申请加入群聊{}", userId, groupId);
    }
    
    @Override
    public void acceptApply(Long applyId, Long operatorId) {
        GroupMember apply = groupMemberMapper.selectById(applyId);
        
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        
        if (!apply.getApplyStatus().equals(GroupApplyStatusEnum.PENDING.getCode())) {
            throw new BusinessException("该申请已处理");
        }
        
        // 验证操作权限（群主或管理员）
        checkGroupPermission(apply.getGroupId(), operatorId, true);
        
        // 通过申请
        apply.setApplyStatus(GroupApplyStatusEnum.ACCEPTED.getCode());
        apply.setAuditTime(LocalDateTime.now());
        apply.setJoinTime(LocalDateTime.now());
        groupMemberMapper.updateById(apply);
        
        log.info("用户{}通过了用户{}的入群申请", operatorId, apply.getUserId());
    }
    
    @Override
    public void rejectApply(Long applyId, Long operatorId) {
        GroupMember apply = groupMemberMapper.selectById(applyId);
        
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        
        if (!apply.getApplyStatus().equals(GroupApplyStatusEnum.PENDING.getCode())) {
            throw new BusinessException("该申请已处理");
        }
        
        // 验证操作权限（群主或管理员）
        checkGroupPermission(apply.getGroupId(), operatorId, true);
        
        // 拒绝申请
        apply.setApplyStatus(GroupApplyStatusEnum.REJECTED.getCode());
        apply.setAuditTime(LocalDateTime.now());
        groupMemberMapper.updateById(apply);
        
        log.info("用户{}拒绝了用户{}的入群申请", operatorId, apply.getUserId());
    }
    
    @Override
    public List<GroupApplyVO> getPendingApplies(String groupId) {
        List<GroupMember> applies = groupMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("apply_status", GroupApplyStatusEnum.PENDING.getCode())
                        .orderByDesc("apply_time")
        );
        
        List<GroupApplyVO> result = new ArrayList<>();
        GroupInfo groupInfo = this.lambdaQuery()
                .eq(GroupInfo::getGroupId, groupId)
                .one();
        
        for (GroupMember apply : applies) {
            GroupApplyVO vo = new GroupApplyVO();
            vo.setId(apply.getId());
            vo.setGroupId(apply.getGroupId());
            vo.setGroupName(groupInfo != null ? groupInfo.getGroupName() : "未知群组");
            vo.setUserId(apply.getUserId());
            vo.setApplyStatus(apply.getApplyStatus());
            vo.setApplyTime(apply.getApplyTime());
            vo.setAuditTime(apply.getAuditTime());
            
            // 获取申请人信息
            UserInfo applicant = userInfoService.getById(apply.getUserId());
            if (applicant != null) {
                vo.setApplicantName(applicant.getNickname());
                vo.setApplicantAccount(applicant.getAccount());
                vo.setApplicantNickname(applicant.getNickname());
                vo.setApplicantAvatar(applicant.getAvatar());
            } else {
                vo.setApplicantName("未知用户");
                vo.setApplicantAccount("user_" + apply.getUserId());
                vo.setApplicantNickname("未知用户");
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public void quitGroup(String groupId, Long userId) {
        GroupInfo groupInfo = this.lambdaQuery()
                .eq(GroupInfo::getGroupId, groupId)
                .one();
        
        if (groupInfo.getCreatorId().equals(userId)) {
            throw new BusinessException("群主不能退出群聊");
        }
        
        groupMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("user_id", userId)
        );
        
        log.info("用户{}退出群聊{}", userId, groupId);
    }
    
    @Override
    public List<GroupInfo> getUserGroups(Long userId) {
        log.info("查询用户{}的群组列表", userId);
        
        // 只返回已通过审核的群组
        List<GroupMember> members = groupMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("user_id", userId)
                        .eq("apply_status", GroupApplyStatusEnum.ACCEPTED.getCode())
        );
        
        log.info("找到{}条群成员记录", members.size());
        
        if (members.isEmpty()) {
            log.info("用户没有加入任何群组");
            return new ArrayList<>();
        }
        
        // 提取所有groupId
        List<String> groupIds = members.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toList());
        
        log.info("查询群组IDs: {}", groupIds);
        
        // 批量查询群组信息
        List<GroupInfo> groups = this.lambdaQuery()
                .in(GroupInfo::getGroupId, groupIds)
                .list();
        
        log.info("返回{}个群组", groups.size());
        return groups;
    }
    
    @Override
    public GroupInfo getGroupInfo(String groupId) {
        return this.lambdaQuery()
                .eq(GroupInfo::getGroupId, groupId)
                .one();
    }
    
    @Override
    public List<GroupInfo> searchGroups(String keyword) {
        return this.lambdaQuery()
                .like(GroupInfo::getGroupName, keyword)
                .list();
    }
    
    @Override
    public List<Map<String, Object>> getGroupMembers(String groupId) {
        log.info("获取群{}的成员列表", groupId);
        
        // 只返回已通过审核的成员
        List<GroupMember> members = groupMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("apply_status", GroupApplyStatusEnum.ACCEPTED.getCode())
        );
        
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量获取所有成员ID
        List<Long> memberIds = members.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toList());
        
        // 一次性查询所有用户信息，避免N+1问题
        List<UserInfo> users = userInfoService.listByIds(memberIds);
        
        // 构建userId到UserInfo的映射
        Map<Long, UserInfo> userMap = users.stream()
                .collect(Collectors.toMap(UserInfo::getId, u -> u));
        
        // 构建userId到GroupMember的映射
        Map<Long, GroupMember> memberMap = members.stream()
                .collect(Collectors.toMap(GroupMember::getUserId, m -> m));
        
        List<Map<String, Object>> memberList = new ArrayList<>();
        for (Long memberId : memberIds) {
            UserInfo user = userMap.get(memberId);
            GroupMember member = memberMap.get(memberId);
            
            if (user != null && member != null) {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("userId", user.getId());
                memberInfo.put("username", user.getUsername());
                memberInfo.put("nickname", user.getNickname());
                memberInfo.put("account", user.getAccount());
                memberInfo.put("avatar", user.getAvatar());
                memberInfo.put("status", user.getStatus()); // 添加在线状态
                memberInfo.put("role", member.getRole());
                memberInfo.put("roleName", getRoleName(member.getRole()));
                memberInfo.put("joinTime", member.getJoinTime());
                memberList.add(memberInfo);
            }
        }
        
        log.info("返回{}个群成员", memberList.size());
        return memberList;
    }
    
    @Override
    public void removeMember(String groupId, Long operatorId, Long targetUserId) {
        log.info("操作员{}尝试从群{}中移除成员{}", operatorId, groupId, targetUserId);
        
        // 检查群组是否存在
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 检查操作员权限
        checkGroupPermission(groupId, operatorId, true);
        
        // 不能移除群主
        GroupMember target = getGroupMember(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException("目标用户不是该群成员");
        }
        
        if (target.getRole().equals(GroupRoleEnum.OWNER.getCode())) {
            throw new BusinessException("不能移除群主");
        }
        
        // 移除成员
        groupMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("user_id", targetUserId)
        );
        
        log.info("成功从群{}中移除成员{}", groupId, targetUserId);
    }
    
    @Override
    public void setAdmin(String groupId, Long operatorId, Long targetUserId, boolean isAdmin) {
        log.info("操作员{}尝试{}群{}中用户{}的管理员身份", operatorId, isAdmin ? "设置" : "取消", groupId, targetUserId);
        
        // 检查群组是否存在
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 只有群主能设置管理员
        if (!groupInfo.getCreatorId().equals(operatorId)) {
            throw new BusinessException("只有群主才能设置管理员");
        }
        
        // 检查目标用户是否是群成员
        GroupMember target = getGroupMember(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException("目标用户不是该群成员");
        }
        
        // 不能对群主操作
        if (target.getRole().equals(GroupRoleEnum.OWNER.getCode())) {
            throw new BusinessException("不能修改群主的角色");
        }
        
        // 设置或取消管理员
        target.setRole(isAdmin ? GroupRoleEnum.ADMIN.getCode() : GroupRoleEnum.MEMBER.getCode());
        groupMemberMapper.updateById(target);
        
        log.info("成功{}用户{}的管理员身份", isAdmin ? "设置" : "取消", targetUserId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwner(String groupId, Long currentOwnerId, Long newOwnerId) {
        log.info("群{}的群主{}尝试转让群主给用户{}", groupId, currentOwnerId, newOwnerId);
        
        // 检查群组是否存在
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 验证当前操作者是否为群主
        if (!groupInfo.getCreatorId().equals(currentOwnerId)) {
            throw new BusinessException("只有群主才能转让群主");
        }
        
        // 检查新群主是否是群成员
        GroupMember newOwner = getGroupMember(groupId, newOwnerId);
        if (newOwner == null) {
            throw new BusinessException("新用户不是该群成员");
        }
        
        // 更新原群主为普通成员
        GroupMember oldOwner = getGroupMember(groupId, currentOwnerId);
        oldOwner.setRole(GroupRoleEnum.MEMBER.getCode());
        groupMemberMapper.updateById(oldOwner);
        
        // 更新新群主
        newOwner.setRole(GroupRoleEnum.OWNER.getCode());
        groupMemberMapper.updateById(newOwner);
        
        // 更新群组创建者
        groupInfo.setCreatorId(newOwnerId);
        this.updateById(groupInfo);
        
        log.info("成功将群{}的群主转让给{}", groupId, newOwnerId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dismissGroup(String groupId, Long ownerId) {
        log.info("群主{}尝试解散群{}", ownerId, groupId);
        
        // 检查群组是否存在
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 验证操作者是否为群主
        if (!groupInfo.getCreatorId().equals(ownerId)) {
            throw new BusinessException("只有群主才能解散群组");
        }
        
        // 删除所有群成员
        groupMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
        );
        
        // 删除群组聊天记录
        try {
            chatMessageService.deleteGroupMessages(groupId);
            log.info("已删除群组{}的聊天记录", groupId);
        } catch (Exception e) {
            log.error("删除群组{}聊天记录失败", groupId, e);
        }
        
        // 删除群组
        this.removeById(groupInfo.getId());
        
        log.info("成功解散群{}", groupId);
    }
    
    @Override
    public void updateGroupInfo(String groupId, Long operatorId, String groupName, String groupAvatar) {
        log.info("操作员{}尝试修改群{}的信息", operatorId, groupId);
        
        // 检查群组是否存在
        GroupInfo groupInfo = getGroupInfo(groupId);
        if (groupInfo == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 检查操作员权限（需要是群主或管理员）
        checkGroupPermission(groupId, operatorId, true);
        
        // 更新群信息
        if (groupName != null && !groupName.trim().isEmpty()) {
            groupInfo.setGroupName(groupName);
        }
        if (groupAvatar != null) {
            groupInfo.setGroupAvatar(groupAvatar);
        }
        this.updateById(groupInfo);
        
        log.info("成功修改群{}的信息", groupId);
    }
    
    /**
     * 获取角色名称
     */
    private String getRoleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        for (GroupRoleEnum roleEnum : GroupRoleEnum.values()) {
            if (roleEnum.getCode().equals(role)) {
                return roleEnum.getDesc();
            }
        }
        return "未知";
    }
    
    /**
     * 检查群组权限
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param requireAdmin 是否需要管理员或群主权限
     * @return 群成员信息
     */
    private GroupMember checkGroupPermission(String groupId, Long userId, boolean requireAdmin) {
        GroupMember member = getGroupMember(groupId, userId);
        
        if (member == null) {
            throw new BusinessException("您不是该群成员");
        }
        
        if (requireAdmin && !member.getRole().equals(GroupRoleEnum.OWNER.getCode()) 
                && !member.getRole().equals(GroupRoleEnum.ADMIN.getCode())) {
            throw new BusinessException("只有群主或管理员才能执行此操作");
        }
        
        return member;
    }
    
    /**
     * 获取群成员信息
     */
    @Override
    public GroupMember getGroupMember(String groupId, Long userId) {
        return groupMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GroupMember>()
                        .eq("group_id", groupId)
                        .eq("user_id", userId)
        );
    }
}