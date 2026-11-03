package com.locallink.service;

import cn.hutool.crypto.digest.BCrypt;
import com.locallink.BaseIntegrationTest;
import com.locallink.common.BusinessException;
import com.locallink.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserInfoService 集成测试
 */
@Transactional  // 每个测试方法执行后自动回滚
@DisplayName("用户服务集成测试")
class UserInfoServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserInfoService userInfoService;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userInfoService.lambdaUpdate().remove();
    }

    @Test
    @DisplayName("用户注册 - 成功场景")
    void testRegister_Success() {
        // Given
        UserInfo newUser = new UserInfo();
        newUser.setAccount("newuser");
        newUser.setPassword("password123");
        newUser.setNickname("新用户");

        // When
        UserInfo result = userInfoService.register(newUser);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getAccount());
        assertNotEquals("password123", result.getPassword()); // 密码应该被加密
        assertTrue(BCrypt.checkpw("password123", result.getPassword()));
    }

    @Test
    @DisplayName("用户注册 - 账号已存在")
    void testRegister_UserAlreadyExists() {
        // Given
        UserInfo existingUser = new UserInfo();
        existingUser.setAccount("existinguser");
        existingUser.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(existingUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            UserInfo newUser = new UserInfo();
            newUser.setAccount("existinguser");
            newUser.setPassword("password123");
            userInfoService.register(newUser);
        });

        assertTrue(exception.getMessage().contains("已存在"));
    }

    @Test
    @DisplayName("用户登录 - 成功场景")
    void testLogin_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoService.save(user);

        // When
        UserInfo result = userInfoService.login("testuser", "password123");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getAccount());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.login("nonexistent", "password123");
        });

        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.login("testuser", "wrongpassword");
        });

        assertTrue(exception.getMessage().contains("密码"));
    }

    @Test
    @DisplayName("用户登录 - 封号用户禁止登录")
    void testLogin_BannedUser() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("banneduser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("被封号用户");
        user.setIsBanned(1);
        user.setBanReason("违反平台规定");
        user.setBanTime(java.time.LocalDateTime.now());
        userInfoService.save(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.login("banneduser", "password123");
        });

        assertTrue(exception.getMessage().contains("封号"));
        assertTrue(exception.getMessage().contains("违反平台规定"));
    }

    @Test
    @DisplayName("更新昵称 - 成功场景")
    void testUpdateNickname_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoService.save(user);

        // When
        userInfoService.updateNickname(user.getId(), "新昵称");

        // Then
        UserInfo updated = userInfoService.getById(user.getId());
        assertEquals("新昵称", updated.getNickname());
    }

    @Test
    @DisplayName("更新昵称 - 用户不存在")
    void testUpdateNickname_UserNotFound() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateNickname(999L, "新昵称");
        });

        assertTrue(exception.getMessage().contains("用户不存在"));
    }

    @Test
    @DisplayName("更新昵称 - 昵称为空")
    void testUpdateNickname_EmptyNickname() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateNickname(user.getId(), "");
        });

        assertTrue(exception.getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("修改密码 - 成功场景")
    void testUpdatePassword_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(user);

        // When
        userInfoService.updatePassword(user.getId(), "password123", "newpassword456");

        // Then
        // 验证可以使用新密码登录
        UserInfo loginResult = userInfoService.login("testuser", "newpassword456");
        assertNotNull(loginResult);
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void testUpdatePassword_WrongOldPassword() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updatePassword(user.getId(), "wrongpassword", "newpassword456");
        });

        assertTrue(exception.getMessage().contains("原密码错误"));
    }

    @Test
    @DisplayName("修改密码 - 新密码长度不足")
    void testUpdatePassword_NewPasswordTooShort() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        userInfoService.save(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updatePassword(user.getId(), "password123", "123");
        });

        assertTrue(exception.getMessage().contains("少于6位"));
    }
}
