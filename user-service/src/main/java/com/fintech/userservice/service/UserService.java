package com.fintech.userservice.service;

import com.fintech.userservice.dto.UserDTO;
import com.fintech.userservice.dto.UserVO;
import com.fintech.userservice.entity.User;

public interface UserService {

    void register(UserDTO user);

    String login(UserDTO user);

    UserVO findById(Long id);
}
