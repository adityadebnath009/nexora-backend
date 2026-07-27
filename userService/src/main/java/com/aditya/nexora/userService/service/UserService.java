package com.aditya.nexora.userService.service;

import com.aditya.nexora.userService.dto.AuthResponseDTO;
import com.aditya.nexora.userService.dto.LoginRequestDTO;
import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.entity.User;

public interface UserService {

    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO);

    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO);

    public UserDTO getUserByEmail(String email);

    public UserDTO updateProfile(Long userId, UserDTO userDTO);

    public void verifyEmail(String token);
    public AuthResponseDTO refresh(String refreshToken);

    public void logout(String refreshToken);

    public void forgotPassword(String email);
    public void resetPassword(String token, String newPassword);
    public void changePassword(Long userId, String oldPassword, String newPassword);


}
