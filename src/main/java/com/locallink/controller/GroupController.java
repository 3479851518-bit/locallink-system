package com.locallink.controller;

import com.locallink.common.Result;
import com.locallink.entity.GroupInfo;
import com.locallink.service.FileService;
import com.locallink.service.GroupInfoService;
import com.locallink.vo.GroupApplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/group")
public class GroupController {
    
    @Autowired
    private GroupInfoService groupInfoService;
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/create")
    public Result<GroupInfo> createGroup(@RequestParam String groupName, @RequestParam Long creatorId) {
        GroupInfo groupInfo = groupInfoService.createGroup(groupName, creatorId);
        return Result.success(groupInfo);
    }
    
    /**
     * 上传群头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadGroupAvatar(@RequestParam("file") MultipartFile file,
                                            @RequestParam String groupId,
                                            @RequestParam Long operatorId) throws IOException {
        try {
            String dbPath = fileService.uploadFile(file, "group-avatars/", groupId + "_");
            groupInfoService.updateGroupInfo(groupId, operatorId, null, dbPath);
            return Result.success(dbPath);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @GetMapping("/join")
    public Result<Void> applyJoinGroup(@RequestParam String groupId, @RequestParam Long userId) {
        groupInfoService.applyJoinGroup(groupId, userId);
        return Result.success();
    }
    
    /**
     * 通过入群申请
     */
    @PostMapping("/accept-apply")
    public Result<Void> acceptApply(@RequestParam Long applyId, @RequestParam Long operatorId) {
        try {
            groupInfoService.acceptApply(applyId, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 拒绝入群申请
     */
    @PostMapping("/reject-apply")
    public Result<Void> rejectApply(@RequestParam Long applyId, @RequestParam Long operatorId) {
        try {
            groupInfoService.rejectApply(applyId, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取待审核申请列表
     */
    @GetMapping("/pending-applies/{groupId}")
    public Result<List<GroupApplyVO>> getPendingApplies(@PathVariable String groupId) {
        List<GroupApplyVO> applies = groupInfoService.getPendingApplies(groupId);
        return Result.success(applies);
    }
    
    @GetMapping("/quit")
    public Result<Void> quitGroup(@RequestParam String groupId, @RequestParam Long userId) {
        groupInfoService.quitGroup(groupId, userId);
        return Result.success();
    }
    
    @GetMapping("/list/{userId}")
    public Result<List<GroupInfo>> getUserGroups(@PathVariable Long userId) {
        log.info("获取用户{}的群组列表", userId);
        try {
            List<GroupInfo> groups = groupInfoService.getUserGroups(userId);
            log.info("返回{}个群组", groups.size());
            return Result.success(groups);
        } catch (Exception e) {
            log.error("获取群组列表失败", e);
            return Result.fail("获取群组列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/info/{groupId}")
    public Result<GroupInfo> getGroupInfo(@PathVariable String groupId) {
        GroupInfo groupInfo = groupInfoService.getGroupInfo(groupId);
        if (groupInfo == null) {
            return Result.fail("群组不存在");
        }
        return Result.success(groupInfo);
    }
    
    @GetMapping("/search")
    public Result<List<GroupInfo>> searchGroups(@RequestParam String keyword) {
        List<GroupInfo> groups = groupInfoService.searchGroups(keyword);
        return Result.success(groups);
    }
    
    /**
     * 获取群成员列表
     */
    @GetMapping("/members/{groupId}")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable String groupId) {
        List<Map<String, Object>> members = groupInfoService.getGroupMembers(groupId);
        return Result.success(members);
    }
    
    /**
     * 移除群成员
     */
    @PostMapping("/remove-member")
    public Result<Void> removeMember(@RequestParam String groupId, 
                                     @RequestParam Long operatorId,
                                     @RequestParam Long targetUserId) {
        try {
            groupInfoService.removeMember(groupId, operatorId, targetUserId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 设置管理员
     */
    @PostMapping("/set-admin")
    public Result<Void> setAdmin(@RequestParam String groupId,
                                 @RequestParam Long operatorId,
                                 @RequestParam Long targetUserId,
                                 @RequestParam boolean isAdmin) {
        try {
            groupInfoService.setAdmin(groupId, operatorId, targetUserId, isAdmin);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 转让群主
     */
    @PostMapping("/transfer-owner")
    public Result<Void> transferOwner(@RequestParam String groupId,
                                      @RequestParam Long currentOwnerId,
                                      @RequestParam Long newOwnerId) {
        try {
            groupInfoService.transferOwner(groupId, currentOwnerId, newOwnerId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 解散群组
     */
    @PostMapping("/dismiss")
    public Result<Void> dismissGroup(@RequestParam String groupId,
                                     @RequestParam Long ownerId) {
        try {
            groupInfoService.dismissGroup(groupId, ownerId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 修改群信息
     */
    @PostMapping("/update")
    public Result<Void> updateGroupInfo(@RequestParam String groupId,
                                        @RequestParam Long operatorId,
                                        @RequestParam(required = false) String groupName,
                                        @RequestParam(required = false) String groupAvatar) {
        try {
            groupInfoService.updateGroupInfo(groupId, operatorId, groupName, groupAvatar);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
