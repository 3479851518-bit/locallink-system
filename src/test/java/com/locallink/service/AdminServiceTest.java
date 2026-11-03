package com.locallink.service;

import com.locallink.BaseIntegrationTest;
import com.locallink.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdminService 集成测试
 */
@Transactional
@DisplayName("管理员服务集成测试")
class AdminServiceTest extends BaseIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserInfoService userInfoService;

    private Long adminId;
    private Long userId;

    @BeforeEach
    void setUp() {
        // 创建管理员用户
        UserInfo admin = new UserInfo();
        admin.setAccount("admin_test");
        admin.setPassword("password123");
        admin.setNickname("管理员");
        admin.setRole(1); // 管理员角色
        userInfoService.save(admin);
        adminId = admin.getId();

        // 创建普通用户
        UserInfo user = new UserInfo();
        user.setAccount("user_test");
        user.setPassword("password123");
        user.setNickname("普通用户");
        user.setRole(0); // 普通用户角色
        userInfoService.save(user);
        userId = user.getId();
    }

    @Test
    @DisplayName("封号用户 - 成功场景")
    void testBanUser_Success() {
        // Given
        UserInfo user = userInfoService.getById(userId);
        assertEquals(0, user.getIsBanned());

        // When
        adminService.banUser(userId, adminId, "违反平台规定");

        // Then
        UserInfo bannedUser = userInfoService.getById(userId);
        assertEquals(1, bannedUser.getIsBanned());
        assertEquals("违反平台规定", bannedUser.getBanReason());
        assertNotNull(bannedUser.getBanTime());
    }

    @Test
    @DisplayName("封号用户 - 不能封号自己")
    void testBanUser_CannotBanSelf() {
        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.banUser(adminId, adminId, "违反规定");
        });
    }

    @Test
    @DisplayName("封号用户 - 不能封号管理员")
    void testBanUser_CannotBanAdmin() {
        // Given - 创建另一个管理员
        UserInfo anotherAdmin = new UserInfo();
        anotherAdmin.setAccount("admin_test2");
        anotherAdmin.setPassword("password123");
        anotherAdmin.setNickname("另一个管理员");
        anotherAdmin.setRole(1);
        userInfoService.save(anotherAdmin);

        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.banUser(anotherAdmin.getId(), adminId, "违反规定");
        });
    }

    @Test
    @DisplayName("解封用户 - 成功场景")
    void testUnbanUser_Success() {
        // Given - 先封号
        adminService.banUser(userId, adminId, "违反平台规定");
        UserInfo bannedUser = userInfoService.getById(userId);
        assertEquals(1, bannedUser.getIsBanned());

        // When
        adminService.unbanUser(userId, adminId);

        // Then
        UserInfo unbannedUser = userInfoService.getById(userId);
        assertEquals(0, unbannedUser.getIsBanned());
        assertNull(unbannedUser.getBanReason());
        assertNull(unbannedUser.getBanTime());
    }

    @Test
    @DisplayName("禁言用户 - 定时禁言")
    void testMuteUser_WithDuration() {
        // Given
        UserInfo user = userInfoService.getById(userId);
        assertEquals(0, user.getIsMuted());

        // When - 禁言60分钟
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(60);
        adminService.muteUser(userId, adminId, "发布不当内容", expireTime);

        // Then
        UserInfo mutedUser = userInfoService.getById(userId);
        assertEquals(1, mutedUser.getIsMuted());
        assertEquals("发布不当内容", mutedUser.getMuteReason());
        assertNotNull(mutedUser.getMuteExpireTime());
    }

    @Test
    @DisplayName("禁言用户 - 永久禁言")
    void testMuteUser_Permanent() {
        // When - 不传过期时间，表示永久禁言
        adminService.muteUser(userId, adminId, "严重违规", null);

        // Then
        UserInfo mutedUser = userInfoService.getById(userId);
        assertEquals(1, mutedUser.getIsMuted());
        assertEquals("严重违规", mutedUser.getMuteReason());
        assertNull(mutedUser.getMuteExpireTime());
    }

    @Test
    @DisplayName("禁言用户 - 不能禁言自己")
    void testMuteUser_CannotMuteSelf() {
        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.muteUser(adminId, adminId, "违规", null);
        });
    }

    @Test
    @DisplayName("禁言用户 - 不能禁言管理员")
    void testMuteUser_CannotMuteAdmin() {
        // Given - 创建另一个管理员
        UserInfo anotherAdmin = new UserInfo();
        anotherAdmin.setAccount("admin_test2");
        anotherAdmin.setPassword("password123");
        anotherAdmin.setNickname("另一个管理员");
        anotherAdmin.setRole(1);
        userInfoService.save(anotherAdmin);

        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.muteUser(anotherAdmin.getId(), adminId, "违规", null);
        });
    }

    @Test
    @DisplayName("解除禁言 - 成功场景")
    void testUnmuteUser_Success() {
        // Given - 先禁言
        adminService.muteUser(userId, adminId, "违规", null);
        UserInfo mutedUser = userInfoService.getById(userId);
        assertEquals(1, mutedUser.getIsMuted());

        // When
        adminService.unmuteUser(userId, adminId);

        // Then
        UserInfo unmutedUser = userInfoService.getById(userId);
        assertEquals(0, unmutedUser.getIsMuted());
        assertNull(unmutedUser.getMuteReason());
        assertNull(unmutedUser.getMuteTime());
        assertNull(unmutedUser.getMuteExpireTime());
    }

    @Test
    @DisplayName("设置用户角色 - 设置为管理员")
    void testSetUserRole_ToAdmin() {
        // Given
        UserInfo user = userInfoService.getById(userId);
        assertEquals(0, user.getRole());

        // When
        adminService.setUserRole(userId, adminId, 1);

        // Then
        UserInfo updatedUser = userInfoService.getById(userId);
        assertEquals(1, updatedUser.getRole());
    }

    @Test
    @DisplayName("设置用户角色 - 设置为普通用户")
    void testSetUserRole_ToNormal() {
        // Given - 先设置为管理员
        adminService.setUserRole(userId, adminId, 1);

        // When - 再设置为普通用户
        adminService.setUserRole(userId, adminId, 0);

        // Then
        UserInfo updatedUser = userInfoService.getById(userId);
        assertEquals(0, updatedUser.getRole());
    }

    @Test
    @DisplayName("设置用户角色 - 不能修改自己的角色")
    void testSetUserRole_CannotModifySelf() {
        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.setUserRole(adminId, adminId, 0);
        });
    }

    @Test
    @DisplayName("设置用户角色 - 无效的角色值")
    void testSetUserRole_InvalidRole() {
        // When & Then
        assertThrows(com.locallink.common.BusinessException.class, () -> {
            adminService.setUserRole(userId, adminId, 99);
        });
    }

    @Test
    @DisplayName("获取所有用户列表")
    void testGetAllUsers() {
        // When
        var users = adminService.getAllUsers();

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2); // 至少有管理员和普通用户
    }

    @Test
    @DisplayName("提交申诉 - 成功场景")
    void testSubmitAppeal_Success() {
        // When
        adminService.submitAppeal(userId, "我对处罚有异议");

        // Then - 验证申诉消息已创建
        var appeals = adminService.getPendingAppeals();
        assertNotNull(appeals);
        assertFalse(appeals.isEmpty());
    }

    @Test
    @DisplayName("获取待处理申诉列表")
    void testGetPendingAppeals() {
        // Given - 提交申诉
        adminService.submitAppeal(userId, "申诉内容");

        // When
        var appeals = adminService.getPendingAppeals();

        // Then
        assertNotNull(appeals);
        assertFalse(appeals.isEmpty());
    }
}
