package com.locallink.controller;

import com.alibaba.fastjson2.JSONObject;
import com.locallink.common.Result;
import com.locallink.entity.ChatMessage;
import com.locallink.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private com.locallink.service.GroupInfoService groupInfoService;
    
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody ChatMessage message) {
        chatMessageService.sendMessage(message);
        return Result.success();
    }
    
    @GetMapping("/history")
    public Result<List<JSONObject>> getHistory(
            @RequestParam Long userId,
            @RequestParam Long friendId,
            @RequestParam(defaultValue = "50") Integer limit) {
        List<JSONObject> result = chatMessageService.getPrivateHistoryWithUserInfo(userId, friendId, limit);
        return Result.success(result);
    }
    
    @GetMapping("/group/{groupId}")
    public Result<List<JSONObject>> getGroupHistory(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Long userId) {
        // 如果提供了userId，则进行权限验证
        if (userId != null) {
            try {
                // 检查用户是否是群成员（已通过审核）
                com.locallink.entity.GroupMember member = groupInfoService.getGroupMember(groupId, userId);
                if (member == null) {
                    return Result.fail("您不是该群成员");
                }
                // 检查申请状态：null表示旧数据，视为已通过；1表示已通过
                if (member.getApplyStatus() != null && 
                    !member.getApplyStatus().equals(com.locallink.enums.GroupApplyStatusEnum.ACCEPTED.getCode())) {
                    return Result.fail("您的入群申请尚未通过审核");
                }
            } catch (Exception e) {
                return Result.fail(e.getMessage());
            }
        }
        List<JSONObject> result = chatMessageService.getGroupHistoryWithNickname(groupId, limit);
        return Result.success(result);
    }
    
    /**
     * 撤回消息
     */
    @PostMapping("/recall")
    public Result<Void> recallMessage(@RequestParam Long messageId, @RequestParam Long userId) {
        try {
            chatMessageService.recallMessage(messageId, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除私聊消息记录
     */
    @PostMapping("/delete-private")
    public Result<Void> deletePrivateMessages(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            chatMessageService.deletePrivateMessages(userId, friendId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除群聊消息记录
     */
    @PostMapping("/delete-group")
    public Result<Void> deleteGroupMessages(@RequestParam String groupId) {
        try {
            chatMessageService.deleteGroupMessages(groupId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除单条消息
     */
    @PostMapping("/delete")
    public Result<Void> deleteMessage(@RequestParam Long messageId, @RequestParam Long userId) {
        try {
            chatMessageService.deleteMessage(messageId, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
