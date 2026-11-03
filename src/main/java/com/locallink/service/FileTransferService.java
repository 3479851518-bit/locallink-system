package com.locallink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.locallink.entity.FileTransfer;

public interface FileTransferService extends IService<FileTransfer> {
    
    FileTransfer createTransferTask(Long senderId, Long receiverId, String fileName, Long fileSize);
    
    void updateProgress(String taskId, Long transferredBytes);
    
    void completeTransfer(String taskId);
    
    void cancelTransfer(String taskId);
    
    FileTransfer getTransferProgress(String taskId);
}
