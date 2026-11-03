package com.locallink.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.locallink.common.Result;
import com.locallink.entity.FriendRelation;
import com.locallink.entity.UserInfo;
import com.locallink.service.FriendRelationService;
import com.locallink.service.UserInfoService;
import com.locallink.vo.FriendRequestVO;
import com.locallink.vo.FriendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/friend")
public class FriendController {
    
    @Autowired
    private FriendRelationService friendRelationService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @PostMapping("/add")
    public Result<Void> addFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            friendRelationService.addFriend(userId, friendId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @PostMapping("/accept")
    public Result<Void> acceptFriend(@RequestParam Long relationId) {
        try {
            friendRelationService.acceptFriend(relationId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @PostMapping("/reject")
    public Result<Void> rejectFriend(@RequestParam Long relationId) {
        try {
            friendRelationService.rejectFriend(relationId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @GetMapping("/list/{userId}")
    public Result<List<FriendVO>> getFriendList(@PathVariable Long userId) {
        List<FriendRelation> relations = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getStatus, 1)
                .list();
        
        List<FriendVO> friendList = new ArrayList<>();
        for (FriendRelation relation : relations) {
            UserInfo friend = userInfoService.getById(relation.getFriendId());
            if (friend != null) {
                FriendVO vo = new FriendVO();
                BeanUtils.copyProperties(friend, vo);
                vo.setUserId(friend.getId());
                vo.setRemark(relation.getRemark());
                vo.setIsBlocked(relation.getIsBlocked() != null && relation.getIsBlocked() == 1);
                friendList.add(vo);
            }
        }
        return Result.success(friendList);
    }
    
    @GetMapping("/search")
    public Result<List<UserInfo>> searchUser(@RequestParam String keyword) {
        List<UserInfo> users = userInfoService.lambdaQuery()
                .like(UserInfo::getUsername, keyword)
                .or()
                .like(UserInfo::getAccount, keyword)
                .or()
                .like(UserInfo::getNickname, keyword)
                .list();
        return Result.success(users);
    }
    
    @GetMapping("/pending/{userId}")
    public Result<List<FriendRequestVO>> getPendingRequests(@PathVariable Long userId) {
        log.info("查询用户{}的待处理申请", userId);
        List<FriendRelation> requests = friendRelationService.getPendingRequests(userId);
        log.info("查询到{}条待处理申请", requests.size());
        
        List<FriendRequestVO> result = new ArrayList<>();
        for (FriendRelation request : requests) {
            log.info("处理申请记录: id={}, userId={}, friendId={}", request.getId(), request.getUserId(), request.getFriendId());
            UserInfo applicant = userInfoService.getById(request.getUserId());
            FriendRequestVO vo = new FriendRequestVO();
            vo.setId(request.getId());
            vo.setUserId(request.getUserId());
            vo.setFriendId(request.getFriendId());
            vo.setStatus(request.getStatus());
            vo.setApplyTime(request.getApplyTime());
            
            if (applicant != null) {
                vo.setApplicantName(applicant.getNickname());
                vo.setApplicantAccount(applicant.getAccount());
                vo.setApplicantNickname(applicant.getNickname());
                vo.setApplicantAvatar(applicant.getAvatar());
                log.info("添加申请人: {}", applicant.getNickname());
            } else {
                // 如果申请人信息不存在，使用默认值
                vo.setApplicantName("未知用户");
                vo.setApplicantAccount("user_" + request.getUserId());
                vo.setApplicantNickname("未知用户");
                log.warn("申请人userId={}不存在，使用默认值", request.getUserId());
            }
            result.add(vo);
        }
        
        return Result.success(result);
    }
    
    @PostMapping("/remove")
    public Result<Void> removeFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            friendRelationService.removeFriend(userId, friendId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 修改好友备注
     */
    @PostMapping("/update-remark")
    public Result<Void> updateRemark(@RequestParam Long userId,
                                     @RequestParam Long friendId,
                                     @RequestParam String remark) {
        try {
            friendRelationService.updateRemark(userId, friendId, remark);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取好友详情
     */
    @GetMapping("/detail/{userId}/{friendId}")
    public Result<FriendVO> getFriendDetail(@PathVariable Long userId, @PathVariable Long friendId) {
        try {
            FriendVO friend = friendRelationService.getFriendDetail(userId, friendId);
            return Result.success(friend);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 拉黑好友
     */
    @PostMapping("/block")
    public Result<Void> blockFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            friendRelationService.blockFriend(userId, friendId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 取消拉黑
     */
    @PostMapping("/unblock")
    public Result<Void> unblockFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            friendRelationService.unblockFriend(userId, friendId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
