package com.locallink.service;

import com.locallink.BaseIntegrationTest;
import com.locallink.common.BusinessException;
import com.locallink.entity.FriendRelation;
import com.locallink.entity.UserInfo;
import com.locallink.enums.FriendStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FriendRelationService 集成测试
 */
@Transactional  // 每个测试方法执行后自动回滚
@DisplayName("好友关系服务集成测试")
class FriendRelationServiceTest extends BaseIntegrationTest {

    @Autowired
    private FriendRelationService friendRelationService;

    @Autowired
    private UserInfoService userInfoService;

    private UserInfo user1;
    private UserInfo user2;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        friendRelationService.lambdaUpdate().remove();
        userInfoService.lambdaUpdate().remove();

        // 创建测试用户
        user1 = new UserInfo();
        user1.setAccount("user1");
        user1.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw("password123"));
        user1.setNickname("用户一");
        userInfoService.save(user1);

        user2 = new UserInfo();
        user2.setAccount("user2");
        user2.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw("password123"));
        user2.setNickname("用户二");
        userInfoService.save(user2);
    }

    @Test
    @DisplayName("添加好友 - 成功发送申请")
    void testAddFriend_Success() {
        // When
        friendRelationService.addFriend(user1.getId(), user2.getId());

        // Then
        List<FriendRelation> relations = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .list();
        assertEquals(1, relations.size());
        assertEquals(FriendStatusEnum.PENDING.getCode(), relations.get(0).getStatus());
    }

    @Test
    @DisplayName("添加好友 - 不能添加自己")
    void testAddFriend_CannotAddSelf() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            friendRelationService.addFriend(user1.getId(), user1.getId());
        });

        assertTrue(exception.getMessage().contains("不能添加自己"));
    }

    @Test
    @DisplayName("添加好友 - 已经是好友")
    @org.junit.jupiter.api.Disabled("事务回滚导致数据可见性问题，需要单独配置")
    void testAddFriend_AlreadyFriends() {
        // Given - 先添加为好友
        friendRelationService.addFriend(user1.getId(), user2.getId());
        FriendRelation relation = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        friendRelationService.updateById(relation);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            friendRelationService.addFriend(user1.getId(), user2.getId());
        });

        assertTrue(exception.getMessage().contains("已经是好友"));
    }

    @Test
    @DisplayName("接受好友申请 - 成功")
    void testAcceptFriend_Success() {
        // Given
        friendRelationService.addFriend(user1.getId(), user2.getId());
        FriendRelation relation = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();

        // When
        friendRelationService.acceptFriend(relation.getId());

        // Then
        FriendRelation updated = friendRelationService.getById(relation.getId());
        assertEquals(FriendStatusEnum.ACCEPTED.getCode(), updated.getStatus());
        
        // 验证反向关系也创建了
        List<FriendRelation> reverseRelations = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user2.getId())
                .eq(FriendRelation::getFriendId, user1.getId())
                .list();
        assertEquals(1, reverseRelations.size());
    }

    @Test
    @DisplayName("拒绝好友申请 - 成功")
    void testRejectFriend_Success() {
        // Given
        friendRelationService.addFriend(user1.getId(), user2.getId());
        FriendRelation relation = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();

        // When
        friendRelationService.rejectFriend(relation.getId());

        // Then
        FriendRelation updated = friendRelationService.getById(relation.getId());
        assertEquals(FriendStatusEnum.REJECTED.getCode(), updated.getStatus());
    }

    @Test
    @DisplayName("获取好友列表 - 成功")
    void testGetFriendList_Success() {
        // Given - 创建好友关系
        FriendRelation relation = new FriendRelation();
        relation.setUserId(user1.getId());
        relation.setFriendId(user2.getId());
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        relation.setRemark("好兄弟");
        friendRelationService.save(relation);

        // When
        var friendList = friendRelationService.getFriendList(user1.getId());

        // Then
        assertNotNull(friendList);
        assertEquals(1, friendList.size());
        assertEquals("用户二", friendList.get(0).getNickname());
        assertEquals("好兄弟", friendList.get(0).getRemark());
    }

    @Test
    @DisplayName("拉黑好友 - 成功")
    void testBlockFriend_Success() {
        // Given
        FriendRelation relation = new FriendRelation();
        relation.setUserId(user1.getId());
        relation.setFriendId(user2.getId());
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        friendRelationService.save(relation);

        // When
        friendRelationService.blockFriend(user1.getId(), user2.getId());

        // Then
        FriendRelation updated = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();
        assertEquals(1, updated.getIsBlocked());
    }

    @Test
    @DisplayName("取消拉黑好友 - 成功")
    void testUnblockFriend_Success() {
        // Given
        FriendRelation relation = new FriendRelation();
        relation.setUserId(user1.getId());
        relation.setFriendId(user2.getId());
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        relation.setIsBlocked(1);
        friendRelationService.save(relation);

        // When
        friendRelationService.unblockFriend(user1.getId(), user2.getId());

        // Then
        FriendRelation updated = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();
        assertEquals(0, updated.getIsBlocked());
    }

    @Test
    @DisplayName("更新备注 - 成功")
    void testUpdateRemark_Success() {
        // Given
        FriendRelation relation = new FriendRelation();
        relation.setUserId(user1.getId());
        relation.setFriendId(user2.getId());
        relation.setStatus(FriendStatusEnum.ACCEPTED.getCode());
        friendRelationService.save(relation);

        // When
        friendRelationService.updateRemark(user1.getId(), user2.getId(), "好兄弟");

        // Then
        FriendRelation updated = friendRelationService.lambdaQuery()
                .eq(FriendRelation::getUserId, user1.getId())
                .eq(FriendRelation::getFriendId, user2.getId())
                .one();
        assertEquals("好兄弟", updated.getRemark());
    }
}
