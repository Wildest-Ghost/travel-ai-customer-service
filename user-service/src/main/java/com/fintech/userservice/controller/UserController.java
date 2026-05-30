package com.fintech.userservice.controller;

import com.fintech.common.Result;
import com.fintech.userservice.dto.UserDTO;
import com.fintech.userservice.dto.UserVO;
import com.fintech.userservice.entity.User;
import com.fintech.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserDTO user) {
        userService.register(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserDTO user) {
        String token = userService.login(user);
        return Result.success(token);
    }

    @GetMapping("/{id}")
    public Result<UserVO> findById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }
}
