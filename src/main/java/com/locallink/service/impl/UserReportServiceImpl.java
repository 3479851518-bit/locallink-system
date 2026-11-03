package com.locallink.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.common.WsMessageType;
import com.locallink.entity.ChatMessage;
import com.locallink.entity.UserReport;
import com.locallink.entity.UserInfo;
import com.locallink.enums.NotificationTypeEnum;
import com.locallink.mapper.UserReportMapper;
import com.locallink.service.AdminService;
import com.locallink.service.UserReportService;
import com.locallink.service.UserInfoService;
import com.locallink.service.MessagePollingService;
import com.locallink.service.OnlineStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserReportServiceImpl extends ServiceImpl<UserReportMapper, UserReport> implements UserReportService {
    
    @Autowired(required = false)
    private AdminService adminService;
    
    @Autowired(required = false)
    private UserInfoService userInfoService;
    
    @Autowired(required = false)
    private MessagePollingService messagePollingService;
    
    @Autowired(required = false)
    private OnlineStatusService onlineStatusService;
    
    @Override
    public void submitReport(Long reporterId, Long reportedUserId, Long messageId, Integer reportType,
                            Integer reasonCode, String reason, Integer includeMessages) {
        if (reasonCode == null) {
            throw new BusinessException("请选择举报原因");
        }
        
        // 检查是否举报自己
        if (reportedUserId != null && reportedUserId.equals(reporterId)) {
            throw new BusinessException("不能举报自己");
        }
        
        UserReport report = new UserReport();
        report.setReporterId(reporterId);
        report.setReportedUserId(reportedUserId);
        report.setMessageId(messageId);
        report.setReportType(reportType);
        report.setReasonCode(reasonCode);
        report.setReason(reason);
        report.setIncludeMessages(includeMessages != null ? includeMessages : 0);
        report.setStatus(0);  // 待处理
        report.setCreateTime(LocalDateTime.now());
        
        this.save(report);
        log.info("用户{}提交了举报，类型：{}，原因代码：{}", reporterId, 
                reportType == 1 ? "用户举报" : "消息举报", reasonCode);
    }
    
    @Override
    public List<UserReport> getPendingReports() {
        return this.lambdaQuery()
                .eq(UserReport::getStatus, 0)  // 待处理
                .orderByDesc(UserReport::getCreateTime)
                .list();
    }
    
    @Override
    public List<UserReport> getAllReports() {
        return this.lambdaQuery()
                .orderByDesc(UserReport::getCreateTime)
                .list();
    }
    
    @Override
    public void handleReport(Long reportId, Long handlerId, Integer status, String handleResult) {
        UserReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已处理");
        }
        
        report.setStatus(status);
        report.setHandleResult(handleResult);
        report.setHandlerId(handlerId);
        report.setHandleTime(LocalDateTime.now());
        
        this.updateById(report);
        log.info("管理员{}处理了举报{}，状态：{}", handlerId, reportId, status == 1 ? "已处理" : "已驳回");
    }
    
    @Override
    public void handleReportWithPenalty(Long reportId, Long handlerId, Integer status, String handleResult, Integer penaltyType) {
        UserReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已处理");
        }
        
        // 如果通过举报且选择了处罚类型，则执行相应处罚
        if (status == 1 && penaltyType != null && penaltyType > 0) {
            report.setPenaltyType(penaltyType);
            
            // 根据处罚类型执行相应的操作
            if (report.getReportedUserId() != null) {
                String penaltyText = getPenaltyText(penaltyType);
                
                // 执行处罚
                switch (penaltyType) {
                    case 1: // 警告 - 仅记录，不执行具体操作
                        log.info("对用户{}发出警告", report.getReportedUserId());
                        break;
                    case 2: // 禁言 - 需要调用adminService禁言功能
                        log.info("对用户{}执行禁言处罚", report.getReportedUserId());
                        if (adminService != null) {
                            try {
                                adminService.muteUser(report.getReportedUserId(), handlerId, "因被举报而禁言", null);
                            } catch (Exception e) {
                                log.error("禁言失败", e);
                            }
                        }
                        break;
                    case 3: // 封号 - 需要调用adminService封号功能
                        log.info("对用户{}执行封号处罚", report.getReportedUserId());
                        if (adminService != null) {
                            try {
                                adminService.banUser(report.getReportedUserId(), handlerId, "因被举报而封号");
                            } catch (Exception e) {
                                log.error("封号失败", e);
                            }
                        }
                        break;
                    case 4: // 永久封号 - 需要调用adminService永久封号功能
                        log.info("对用户{}执行永久封号处罚", report.getReportedUserId());
                        if (adminService != null) {
                            try {
                                adminService.banUser(report.getReportedUserId(), handlerId, "因被举报而永久封号");
                            } catch (Exception e) {
                                log.error("永久封号失败", e);
                            }
                        }
                        break;
                    default:
                        break;
                }
                
                // 发送处罚通知给被处罚用户
                sendPenaltyNotification(report.getReportedUserId(), penaltyType, penaltyText, handleResult);
            }
        }
        
        report.setStatus(status);
        report.setHandleResult(handleResult);
        report.setHandlerId(handlerId);
        report.setHandleTime(LocalDateTime.now());
        
        this.updateById(report);
        log.info("管理员{}处理了举报{}，状态：{}，处罚类型：{}", handlerId, reportId, status == 1 ? "已处理" : "已驳回", penaltyType);
    }
    
    /**
     * 获取处罚类型文本
     */
    private String getPenaltyText(Integer penaltyType) {
        switch (penaltyType) {
            case 1: return "警告";
            case 2: return "禁言";
            case 3: return "封号";
            case 4: return "永久封号";
            default: return "未知处罚";
        }
    }
    
    /**
     * 发送处罚通知
     */
    private void sendPenaltyNotification(Long userId, Integer penaltyType, String penaltyText, String reason) {
        try {
            // 构建通知消息
            JSONObject notification = new JSONObject();
            notification.put("type", WsMessageType.NOTIFICATION);
            notification.put("notificationType", NotificationTypeEnum.PENALTY_NOTICE.getCode());
            notification.put("title", "处罚通知");
            notification.put("content", String.format("您因违规行为被处以%s处罚。原因：%s", penaltyText, reason));
            notification.put("penaltyType", penaltyType);
            notification.put("penaltyText", penaltyText);
            notification.put("reason", reason);
            notification.put("canAppeal", true); // 可以申诉
            notification.put("timestamp", LocalDateTime.now().toString());
            
            // 通过HTTP轮询推送通知（如果用户在线）
            if (onlineStatusService.isOnline(userId)) {
                // 创建一个虚拟的ChatMessage来传递通知
                ChatMessage notificationMsg = new ChatMessage();
                notificationMsg.setSenderId(0L); // 系统ID
                notificationMsg.setReceiverId(userId);
                notificationMsg.setContent(notification.toJSONString());
                notificationMsg.setChatType(1); // 私聊类型
                messagePollingService.pushMessage(userId, notificationMsg);
                log.info("已向用户{}发送处罚通知：{}", userId, penaltyText);
            } else {
                log.info("用户{}不在线，通知将在下次登录时显示", userId);
            }
        } catch (Exception e) {
            log.error("发送处罚通知失败", e);
        }
    }
}
