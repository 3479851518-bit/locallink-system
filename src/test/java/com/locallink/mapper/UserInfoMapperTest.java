package com.locallink.mapper;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.locallink.BaseIntegrationTest;
import com.locallink.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserInfoMapper 测试
 */
@Transactional  // 每个测试方法执行后自动回滚
@DisplayName("用户Mapper测试")
class UserInfoMapperTest extends BaseIntegrationTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userInfoMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("插入用户 - 成功")
    void testInsert_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        user.setStatus(0);

        // When
        int result = userInfoMapper.insert(user);

        // Then
        assertEquals(1, result);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void testSelectById_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoMapper.insert(user);

        // When
        UserInfo found = userInfoMapper.selectById(user.getId());

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getAccount());
        assertEquals("测试用户", found.getNickname());
    }

    @Test
    @DisplayName("根据ID查询用户 - 不存在")
    void testSelectById_NotFound() {
        // When
        UserInfo found = userInfoMapper.selectById(999L);

        // Then
        assertNull(found);
    }

    @Test
    @DisplayName("根据账号查询用户 - 成功")
    void testSelectByAccount_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoMapper.insert(user);

        // When
        UserInfo found = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getAccount, "testuser")
        );

        // Then
        assertNotNull(found);
        assertEquals("testuser", found.getAccount());
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdate_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoMapper.insert(user);

        // When
        user.setNickname("新昵称");
        int result = userInfoMapper.updateById(user);

        // Then
        assertEquals(1, result);
        
        UserInfo updated = userInfoMapper.selectById(user.getId());
        assertEquals("新昵称", updated.getNickname());
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void testDelete_Success() {
        // Given
        UserInfo user = new UserInfo();
        user.setAccount("testuser");
        user.setPassword(BCrypt.hashpw("password123"));
        user.setNickname("测试用户");
        userInfoMapper.insert(user);

        // When
        int result = userInfoMapper.deleteById(user.getId());

        // Then
        assertEquals(1, result);
        
        UserInfo deleted = userInfoMapper.selectById(user.getId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("查询所有用户 - 成功")
    void testSelectAll_Success() {
        // Given
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setAccount("user" + i);
            user.setPassword(BCrypt.hashpw("password123"));
            user.setNickname("用户" + i);
            userInfoMapper.insert(user);
        }

        // When
        List<UserInfo> users = userInfoMapper.selectList(null);

        // Then
        assertNotNull(users);
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("条件查询 - 成功")
    void testSelectByCondition_Success() {
        // Given
        UserInfo user1 = new UserInfo();
        user1.setAccount("user1");
        user1.setPassword(BCrypt.hashpw("password123"));
        user1.setNickname("张三");
        user1.setStatus(1);
        userInfoMapper.insert(user1);

        UserInfo user2 = new UserInfo();
        user2.setAccount("user2");
        user2.setPassword(BCrypt.hashpw("password123"));
        user2.setNickname("李四");
        user2.setStatus(0);
        userInfoMapper.insert(user2);

        // When
        List<UserInfo> onlineUsers = userInfoMapper.selectList(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getStatus, 1)
        );

        // Then
        assertNotNull(onlineUsers);
        assertEquals(1, onlineUsers.size());
        assertEquals("张三", onlineUsers.get(0).getNickname());
    }

    @Test
    @DisplayName("模糊查询 - 成功")
    void testSelectByLike_Success() {
        // Given
        UserInfo user1 = new UserInfo();
        user1.setAccount("zhangsan");
        user1.setPassword(BCrypt.hashpw("password123"));
        user1.setNickname("张三");
        userInfoMapper.insert(user1);

        UserInfo user2 = new UserInfo();
        user2.setAccount("lisi");
        user2.setPassword(BCrypt.hashpw("password123"));
        user2.setNickname("李四");
        userInfoMapper.insert(user2);

        // When
        List<UserInfo> users = userInfoMapper.selectList(
                new LambdaQueryWrapper<UserInfo>()
                        .like(UserInfo::getNickname, "张")
        );

        // Then
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("张三", users.get(0).getNickname());
    }
}
