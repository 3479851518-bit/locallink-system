package com.locallink.controller;

import com.locallink.common.Result;
import com.locallink.entity.FileTransfer;
import com.locallink.service.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private static final String UPLOAD_DIR = "uploads/";
    
    @Autowired
    private FileTransferService fileTransferService;
    
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam Long senderId,
                                     @RequestParam Long receiverId) throws IOException {
        String fileName = file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String filePath = UPLOAD_DIR + System.currentTimeMillis() + "_" + fileName;
        file.transferTo(new File(filePath));
        
        FileTransfer transfer = new FileTransfer();
        transfer.setSenderId(senderId);
        transfer.setReceiverId(receiverId);
        transfer.setFileName(fileName);
        transfer.setFileSize(file.getSize());
        transfer.setFilePath(filePath);
        transfer.setStatus(1);
        fileTransferService.save(transfer);
        
        return Result.success(filePath);
    }
    
    @GetMapping("/download/{taskId}")
    public void downloadFile(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        FileTransfer transfer = fileTransferService.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
        
        if (transfer != null && transfer.getFilePath() != null) {
            File file = new File(transfer.getFilePath());
            if (file.exists()) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + transfer.getFileName());
                
                try (InputStream in = new FileInputStream(file);
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            }
        }
    }
    
    @GetMapping("/progress/{taskId}")
    public Result<FileTransfer> getProgress(@PathVariable String taskId) {
        FileTransfer transfer = fileTransferService.lambdaQuery()
                .eq(FileTransfer::getTaskId, taskId)
                .one();
        return Result.success(transfer);
    }
}
