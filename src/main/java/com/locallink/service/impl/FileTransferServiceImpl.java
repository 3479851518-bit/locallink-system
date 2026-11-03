package com.locallink.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.locallink.common.BusinessException;
import com.locallink.entity.FileTransfer;
import com.locallink.enums.TransferStatusEnum;
import com.locallink.mapper.FileTransferMapper;
// import com.locallink.rabbitmq.MessageSender; // RabbitMQ 已禁用
import com.locallink.service.FileTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class FileTransferServiceImpl extends ServiceImpl<FileTransferMapper, FileTransfer> implements FileTransferService {
    
    // @Autowired(required = false)
    // private MessageSender messageSender; // RabbitMQ 已禁用
    
    @Override
    public FileTransfer createTransferTask(Long senderId, Long receiverId, String fileName, Long fileSize) {
        FileTransfer transfer = new FileTransfer();
        transfer.setTaskId(IdUtil.fastSimpleUUID());
        transfer.setSenderId(senderId);
        transfer.setReceiverId(receiverId);
        transfer.setFileName(fileName);
        transfer.setFileSize(fileSize);
        transfer.setTransferredBytes(0L);
        transfer.setTransferType(1);
        transfer.setStatus(TransferStatusEnum.WAITING.getCode());
        transfer.setStartTime(LocalDateTime.now());
        this.save(transfer);
        
        // RabbitMQ 消息发送已禁用，使用 WebSocket 和 HTTP 轮询
        // if (messageSender != null) {
        //     messageSender.sendFileMessage(transfer);
        // }
        
        log.info("创建文件传输任务: {}, 大小: {}", fileName, fileSize);
        return transfer;
    }
    
    @Override
    public void updateProgress(String taskId, Long transferredBytes) {
        FileTransfer transfer = this.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
        
        if (transfer == null) {
            throw new BusinessException("传输任务不存在");
        }
        
        transfer.setTransferredBytes(transferredBytes);
        if (transfer.getStatus().equals(TransferStatusEnum.WAITING.getCode())) {
            transfer.setStatus(TransferStatusEnum.TRANSFERRING.getCode());
        }
        this.updateById(transfer);
    }
    
    @Override
    public void completeTransfer(String taskId) {
        FileTransfer transfer = this.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
        
        if (transfer == null) {
            throw new BusinessException("传输任务不存在");
        }
        
        transfer.setStatus(TransferStatusEnum.COMPLETED.getCode());
        transfer.setCompleteTime(LocalDateTime.now());
        this.updateById(transfer);
        
        log.info("文件传输完成: {}", taskId);
    }
    
    @Override
    public void cancelTransfer(String taskId) {
        FileTransfer transfer = this.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
        
        if (transfer == null) {
            throw new BusinessException("传输任务不存在");
        }
        
        transfer.setStatus(TransferStatusEnum.CANCELLED.getCode());
        this.updateById(transfer);
        
        log.info("取消文件传输: {}", taskId);
    }
    
    @Override
    public FileTransfer getTransferProgress(String taskId) {
        return this.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
    }
}
