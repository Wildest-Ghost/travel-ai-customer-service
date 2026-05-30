package com.fintech.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fintech.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
