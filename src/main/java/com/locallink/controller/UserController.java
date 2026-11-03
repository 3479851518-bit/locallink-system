package com.locallink.controller;

import com.locallink.common.Result;
import com.locallink.entity.UserInfo;
import com.locallink.service.FileService;
import com.locallink.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@Validated
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
public class UserController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private FileService fileService;
    
    /**
     * 获取服务器配置信息
     */
    @Operation(summary = "获取服务器配置", description = "获取服务器地址、WebSocket地址等配置信息")
    @GetMapping("/config")
    public Result<Map<String, String>> getServerConfig(jakarta.servlet.http.HttpServletRequest request) {
        Map<String, String> config = new HashMap<>();
        
        // 优先从请求头中获取实际的主机信息（支持Nginx反向代理）
        String serverHost = request.getHeader("X-Forwarded-Host");
        String scheme = request.getHeader("X-Forwarded-Proto");
        
        // 如果没有转发头，则使用直接请求的信息
        if (serverHost == null || serverHost.isEmpty()) {
            serverHost = request.getServerName();
        }
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }
        
        // 确定端口
        int serverPort = request.getServerPort();
        boolean isDefaultPort = ("http".equals(scheme) && serverPort == 80) || 
                               ("https".equals(scheme) && serverPort == 443);
        
        // 构建基础URL
        String baseUrl;
        String wsUrl;
        
        if (isDefaultPort) {
            baseUrl = scheme + "://" + serverHost;
            wsUrl = ("https".equals(scheme) ? "wss" : "ws") + "://" + serverHost;
        } else {
            baseUrl = scheme + "://" + serverHost + ":" + serverPort;
            wsUrl = ("https".equals(scheme) ? "wss" : "ws") + "://" + serverHost + ":" + serverPort;
        }
        
        config.put("baseUrl", baseUrl);
        config.put("wsUrl", wsUrl);
        config.put("serverHost", serverHost);
        config.put("serverPort", String.valueOf(serverPort));
        
        return Result.success(config);
    }
    
    @Operation(summary = "用户注册", description = "注册新用户，默认角色为普通用户")
    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserInfo userInfo) {
        // 安全检查：注册用户只能是普通用户（role=0）
        if (userInfo.getRole() != null && userInfo.getRole() == 1) {
            return Result.fail("管理员账号不能通过注册创建，请联系系统管理员添加");
        }
        // 强制设置为普通用户
        userInfo.setRole(0);
        
        UserInfo registeredUser = userInfoService.register(userInfo);
        return Result.success(registeredUser.getId());
    }
    
    @Operation(summary = "用户登录", description = "使用账号和密码登录系统")
    @PostMapping("/login")
    public Result<UserInfo> login(
            @Parameter(description = "账号", required = true) @RequestParam @NotBlank(message = "账号不能为空") String account, 
            @Parameter(description = "密码", required = true) @RequestParam @NotBlank(message = "密码不能为空") String password) {
        UserInfo userInfo = userInfoService.login(account, password);
        userInfo.setPassword(null);
        return Result.success(userInfo);
    }
    
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/info/{userId}")
    public Result<UserInfo> getUserInfo(@Parameter(description = "用户ID", required = true) @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(userInfo);
    }
    
    @Operation(summary = "更新用户状态", description = "更新用户的在线状态")
    @PostMapping("/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId, 
            @Parameter(description = "状态: 0-离线, 1-在线, 2-忙碌", required = true) @RequestParam @NotNull(message = "状态不能为空") Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setStatus(status);
        userInfoService.updateById(userInfo);
        return Result.success();
    }
    
    /**
     * 上传头像
     */
    @Operation(summary = "上传头像", description = "上传用户个人头像，支持jpg/png格式，最大5MB")
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "头像文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId) throws IOException {
        String dbPath = fileService.uploadFile(file, "avatars/", userId + "_");
        userInfoService.updateAvatar(userId, dbPath);
        return Result.success(dbPath);
    }
    
    /**
     * 修改用户昵称
     */
    @Operation(summary = "修改昵称", description = "修改用户昵称")
    @PostMapping("/update-nickname")
    public Result<Void> updateNickname(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId, 
            @Parameter(description = "新昵称", required = true) @RequestParam @NotBlank(message = "昵称不能为空") String nickname) {
        userInfoService.updateNickname(userId, nickname);
        return Result.success();
    }
    
    /**
     * 修改用户头像（URL方式）
     */
    @Operation(summary = "修改头像URL", description = "通过URL方式修改用户头像")
    @PostMapping("/update-avatar")
    public Result<Void> updateAvatar(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId, 
            @Parameter(description = "头像URL", required = true) @RequestParam @NotBlank(message = "头像URL不能为空") String avatar) {
        userInfoService.updateAvatar(userId, avatar);
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @PostMapping("/update-password")
    public Result<Void> updatePassword(
            @Parameter(description = "用户ID", required = true) @RequestParam @NotNull(message = "用户ID不能为空") Long userId, 
            @Parameter(description = "旧密码", required = true) @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam @NotBlank(message = "新密码不能为空") String newPassword) {
        userInfoService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
