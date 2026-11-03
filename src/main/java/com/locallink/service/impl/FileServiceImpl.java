package com.locallink.service.impl;

import com.locallink.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path:uploads}")
    private String uploadBasePath;

    @Override
    public String uploadFile(MultipartFile file, String subDir, String prefix) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("只能上传图片文件");
        }

        // 检查文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("图片大小不能超过5MB");
        }

        // 获取绝对路径并创建目录
        Path baseDir = Paths.get(uploadBasePath).toAbsolutePath();
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        String dirPath = baseDir + File.separator + subDir;
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = prefix + UUID.randomUUID().toString() + extension;
        Path filePath = dir.resolve(fileName);

        // 保存文件
        file.transferTo(filePath.toFile());

        // 返回数据库存储的相对路径（用于前端访问）
        return "uploads/" + subDir + fileName;
    }
}
