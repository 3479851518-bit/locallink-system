package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.UserReport;

import java.util.List;

public interface UserReportService extends IService<UserReport> {
    
    /**
     * 提交举报
     */
    void submitReport(Long reporterId, Long reportedUserId, Long messageId, Integer reportType, 
                     Integer reasonCode, String reason, Integer includeMessages);
    
    /**
     * 获取待处理的举报列表
     */
    List<UserReport> getPendingReports();
    
    /**
     * 获取所有举报列表
     */
    List<UserReport> getAllReports();
    
    /**
     * 处理举报
     */
    void handleReport(Long reportId, Long handlerId, Integer status, String handleResult);
    
    /**
     * 处理举报并执行处罚
     */
    void handleReportWithPenalty(Long reportId, Long handlerId, Integer status, String handleResult, Integer penaltyType);
}
