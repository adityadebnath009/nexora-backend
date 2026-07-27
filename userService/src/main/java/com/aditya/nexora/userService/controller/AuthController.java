package com.aditya.nexora.userService.controller;


import com.aditya.nexora.userService.dto.*;
import com.aditya.nexora.userService.service.UserService;
import com.aditya.nexora.userService.service.UsernameGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final UsernameGenerator usernameGenerator;

    public AuthController(UserService userService, UsernameGenerator usernameGenerator) {
        this.userService = userService;
        this.usernameGenerator = usernameGenerator;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@RequestBody @Valid SignUpRequestDTO signUpRequestDTO) {

        return ResponseEntity.status(HttpStatus.OK).body(userService.signUp(signUpRequestDTO));

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(loginRequestDTO));

    }

    @GetMapping("/check-username")
    public ResponseEntity<UsernameCheckResponseDTO> checkUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(usernameGenerator.checkUsernameAvailability(username));
    }


    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token")String token)
    {
        userService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body("Email verified successfully! You can now log in to Nexora.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(userService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO request) {
        userService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
        userService.forgotPassword(request.email());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }







}
