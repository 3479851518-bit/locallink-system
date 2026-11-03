package com.locallink.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.locallink.BaseIntegrationTest;
import com.locallink.entity.UserInfo;
import com.locallink.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 集成测试
 */
@Transactional  // 每个测试方法执行后自动回滚
@DisplayName("用户控制器集成测试")
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserInfoService userInfoService;

    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userInfoService.lambdaUpdate().remove();

        // 创建测试用户
        testUser = new UserInfo();
        testUser.setAccount("testuser");
        testUser.setPassword(BCrypt.hashpw("password123"));
        testUser.setNickname("测试用户");
        testUser.setStatus(0);
        userInfoService.save(testUser);
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() throws Exception {
        String newUserJson = """
                {
                    "account": "newuser",
                    "password": "password123",
                    "nickname": "新用户"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .param("account", "testuser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.account").value("testuser"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .param("account", "testuser")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("密码")));
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .param("account", "nonexistent")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void testGetUserInfo_Success() throws Exception {
        mockMvc.perform(get("/api/user/info/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.account").value("testuser"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    @DisplayName("更新用户状态 - 成功")
    void testUpdateStatus_Success() throws Exception {
        mockMvc.perform(post("/api/user/status")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("修改昵称 - 成功")
    void testUpdateNickname_Success() throws Exception {
        mockMvc.perform(post("/api/user/update-nickname")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("nickname", "新昵称"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证昵称已更新
        UserInfo updatedUser = userInfoService.getById(testUser.getId());
        assert updatedUser != null;
        assert "新昵称".equals(updatedUser.getNickname());
    }

    @Test
    @DisplayName("修改昵称 - 昵称为空")
    void testUpdateNickname_Empty() throws Exception {
        mockMvc.perform(post("/api/user/update-nickname")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("nickname", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("不能为空")));
    }

    @Test
    @DisplayName("修改密码 - 成功")
    void testUpdatePassword_Success() throws Exception {
        mockMvc.perform(post("/api/user/update-password")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("oldPassword", "password123")
                        .param("newPassword", "newpassword456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证可以使用新密码登录
        mockMvc.perform(post("/api/user/login")
                        .param("account", "testuser")
                        .param("password", "newpassword456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void testUpdatePassword_WrongOldPassword() throws Exception {
        mockMvc.perform(post("/api/user/update-password")
                        .param("userId", String.valueOf(testUser.getId()))
                        .param("oldPassword", "wrongpassword")
                        .param("newPassword", "newpassword456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("原密码错误")));
    }
}
