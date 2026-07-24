package com.aditya.nexora.userService.controller;


import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.dto.UsernameCheckResponseDTO;
import com.aditya.nexora.userService.service.UserService;
import com.aditya.nexora.userService.service.UserServiceImpl;
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

    @GetMapping("/check-username")
    public ResponseEntity<UsernameCheckResponseDTO> checkUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(usernameGenerator.checkUsernameAvailability(username));
    }


}
