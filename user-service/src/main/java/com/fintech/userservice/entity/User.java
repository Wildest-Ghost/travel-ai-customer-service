package com.fintech.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")   // user 是 PostgreSQL 保留字，显式指定表名
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
}
