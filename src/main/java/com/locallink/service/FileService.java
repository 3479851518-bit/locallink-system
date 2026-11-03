package com.locallink.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    /**
     * 上传文件并返回存储路径
     * @param file 文件对象
     * @param subDir 子目录（如 avatars/, group-avatars/）
     * @param prefix 文件名前缀（如 userId_）
     * @return 数据库存储的相对路径
     */
    String uploadFile(MultipartFile file, String subDir, String prefix) throws IOException;
}
