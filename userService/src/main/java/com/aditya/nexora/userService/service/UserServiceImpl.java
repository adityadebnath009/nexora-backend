package com.aditya.nexora.userService.service;

import com.aditya.nexora.userService.dto.LoginRequestDTO;
import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.entity.User;

public interface UserServiceImpl {

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO);

    public String login(LoginRequestDTO loginRequestDTO);

    public UserDTO getUserByEmail(String email);

    public UserDTO updateProfile(Long userId, UserDTO userDTO);

}
