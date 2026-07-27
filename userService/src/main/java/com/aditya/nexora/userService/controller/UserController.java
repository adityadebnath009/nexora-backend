package com.aditya.nexora.userService.controller;

import com.aditya.nexora.userService.dto.ChangePasswordRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.service.UserService;
import com.aditya.nexora.userService.service.UserServiceImpl;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {

    private final UserServiceImpl userService;
    private final ModelMapper modelMapper;

    public UserController(UserServiceImpl userService, ModelMapper modelMapper) {
        this.userService = (UserServiceImpl) userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(@RequestHeader("X-User-Id") Long userId) {
        UserDTO userDTO = userService.getByUserId(userId);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(userId, userDTO));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid ChangePasswordRequestDTO request) {
        userService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }



}
