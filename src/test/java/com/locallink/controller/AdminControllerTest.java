package com.locallink.controller;

import com.locallink.BaseIntegrationTest;
import com.locallink.entity.UserInfo;
import com.locallink.service.AdminService;
import com.locallink.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController 集成测试
 */
@Transactional
@DisplayName("管理员控制器集成测试")
class AdminControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AdminService adminService;

    private Long adminId;
    private Long userId;

    @BeforeEach
    void setUp() {
        // 创建管理员用户
        UserInfo admin = new UserInfo();
        admin.setAccount("admin_test");
        admin.setPassword("password123");
        admin.setNickname("管理员");
        admin.setRole(1);
        userInfoService.save(admin);
        adminId = admin.getId();

        // 创建普通用户
        UserInfo user = new UserInfo();
        user.setAccount("user_test");
        user.setPassword("password123");
        user.setNickname("普通用户");
        user.setRole(0);
        userInfoService.save(user);
        userId = user.getId();
    }

    @Test
    @DisplayName("获取所有用户列表 - 成功")
    void testGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("封号用户 - 成功")
    void testBanUser_Success() throws Exception {
        mockMvc.perform(post("/api/admin/ban-user")
                        .param("userId", userId.toString())
                        .param("adminId", adminId.toString())
                        .param("reason", "违反平台规定"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("封号用户 - 不能封号自己")
    void testBanUser_CannotBanSelf() throws Exception {
        mockMvc.perform(post("/api/admin/ban-user")
                        .param("userId", adminId.toString())
                        .param("adminId", adminId.toString())
                        .param("reason", "违反规定"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("解封用户 - 成功")
    void testUnbanUser_Success() throws Exception {
        // 先封号
        adminService.banUser(userId, adminId, "违反规定");

        mockMvc.perform(post("/api/admin/unban-user")
                        .param("userId", userId.toString())
                        .param("adminId", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("禁言用户 - 成功")
    void testMuteUser_Success() throws Exception {
        mockMvc.perform(post("/api/admin/mute-user")
                        .param("userId", userId.toString())
                        .param("adminId", adminId.toString())
                        .param("reason", "发布不当内容")
                        .param("durationMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("解除禁言 - 成功")
    void testUnmuteUser_Success() throws Exception {
        // 先禁言
        adminService.muteUser(userId, adminId, "违规", null);

        mockMvc.perform(post("/api/admin/unmute-user")
                        .param("userId", userId.toString())
                        .param("adminId", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("设置用户角色 - 成功")
    void testSetUserRole_Success() throws Exception {
        mockMvc.perform(post("/api/admin/set-role")
                        .param("userId", userId.toString())
                        .param("adminId", adminId.toString())
                        .param("role", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交申诉 - 成功")
    void testSubmitAppeal_Success() throws Exception {
        mockMvc.perform(post("/api/admin/appeal")
                        .param("userId", userId.toString())
                        .param("content", "我对处罚有异议"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取待处理申诉列表 - 成功")
    void testGetPendingAppeals_Success() throws Exception {
        mockMvc.perform(get("/api/admin/appeals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
