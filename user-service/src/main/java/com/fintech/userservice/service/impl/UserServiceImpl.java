package com.fintech.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fintech.security.JwtUtil;
import com.fintech.userservice.dto.UserDTO;
import com.fintech.userservice.dto.UserVO;
import com.fintech.userservice.entity.User;
import com.fintech.userservice.mapper.UserMapper;
import com.fintech.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void register(UserDTO user) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户已存在");
        }
        //密码加密
        String encodePassword = passwordEncoder.encode(user.getPassword());
        //入库
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(encodePassword);
        userMapper.insert(newUser);

    }

    @Override
    public String login(UserDTO user) {
        User newUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername())
        );
        if (newUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(user.getPassword(), newUser.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtUtil.generateToken(newUser.getId(), newUser.getUsername());
        return token;
    }
    @Override
    public UserVO findById(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return null;
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        return vo;
    }
}
