package com.locallink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.locallink.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    
}
