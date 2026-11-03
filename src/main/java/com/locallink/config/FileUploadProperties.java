package com.locallink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {
    
    /**
     * 上传根路径
     */
    private String path = "uploads";
    
    /**
     * 头像配置
     */
    private AvatarConfig avatar = new AvatarConfig();
    
    /**
     * 群头像配置
     */
    private AvatarConfig groupAvatar = new AvatarConfig();
    
    @Data
    public static class AvatarConfig {
        /**
         * 最大文件大小（字节）
         */
        private long maxSize = 5242880; // 5MB
    }
}
