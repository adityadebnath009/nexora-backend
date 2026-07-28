package com.aditya.nexora.userService.controller;

import com.aditya.nexora.userService.dto.AuthResponseDTO;
import com.aditya.nexora.userService.dto.LoginRequestDTO;
import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.dto.UsernameCheckResponseDTO;
import com.aditya.nexora.userService.enums.Role;
import com.aditya.nexora.userService.exception.GlobalHandleApiException;
import com.aditya.nexora.userService.exception.GlobalHandleApiResponse;
import com.aditya.nexora.userService.service.UserService;
import com.aditya.nexora.userService.service.UserServiceImpl;
import com.aditya.nexora.userService.service.UsernameGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserServiceApiEndpointsWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final FakeUserService fakeUserService = new FakeUserService();
    private final FakeUsernameGenerator fakeUsernameGenerator = new FakeUsernameGenerator();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(fakeUserService, fakeUsernameGenerator);
        UserController userController = new UserController(fakeUserService, new ModelMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(authController, userController)
                .setControllerAdvice(new GlobalHandleApiException(), new GlobalHandleApiResponse())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void signupEndpointReturnsUserPayload() throws Exception {
        fakeUserService.signUpResponse = sampleUser(1L, "aditya", "aditya@example.com");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "aditya@example.com",
                                  "password": "secret123",
                                  "name": "Aditya",
                                  "userName": "aditya"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("aditya@example.com"))
                .andExpect(jsonPath("$.data.username").value("aditya"));
    }

    @Test
    void loginEndpointReturnsTokens() throws Exception {
        fakeUserService.loginResponse = new AuthResponseDTO("access-token", "refresh-token", sampleUser(1L, "aditya", "aditya@example.com"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "aditya@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.email").value("aditya@example.com"));
    }

    @Test
    void checkUsernameEndpointReturnsAvailability() throws Exception {
        fakeUsernameGenerator.availabilityResponse = new UsernameCheckResponseDTO(true, List.of());

        mockMvc.perform(get("/auth/check-username").param("username", "aditya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isAvailable").value(true));
    }

    @Test
    void verifyEmailEndpointReturnsSuccessMessage() throws Exception {
        mockMvc.perform(get("/auth/verify-email").param("token", "verify-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        Assertions.assertEquals("verify-token", fakeUserService.verifyEmailToken);
    }

    @Test
    void refreshEndpointReturnsNewTokens() throws Exception {
        fakeUserService.refreshResponse = new AuthResponseDTO("new-access", "new-refresh", sampleUser(1L, "aditya", "aditya@example.com"));

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
    }

    @Test
    void logoutEndpointReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh-token"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Assertions.assertEquals("refresh-token", fakeUserService.logoutToken);
    }

    @Test
    void forgotPasswordEndpointReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"aditya@example.com"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Assertions.assertEquals("aditya@example.com", fakeUserService.forgotPasswordEmail);
    }

    @Test
    void resetPasswordEndpointReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"reset-token","newPassword":"new-secret"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Assertions.assertEquals("reset-token", fakeUserService.resetPasswordToken);
        Assertions.assertEquals("new-secret", fakeUserService.resetPasswordNewPassword);
    }

    @Test
    void getMeEndpointReturnsUserProfile() throws Exception {
        fakeUserService.getByUserIdResponse = sampleUser(42L, "aditya", "aditya@example.com");

        mockMvc.perform(get("/user/me")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.email").value("aditya@example.com"));
    }

    @Test
    void getUserByUsernameEndpointReturnsUserProfile() throws Exception {
        fakeUserService.getUserByUsernameResponse = sampleUser(1L, "aditya", "aditya@example.com");

        mockMvc.perform(get("/user/username/aditya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("aditya"));
    }

    @Test
    void getUserByEmailEndpointReturnsUserProfile() throws Exception {
        fakeUserService.getUserByEmailResponse = sampleUser(1L, "aditya", "aditya@example.com");

        mockMvc.perform(get("/user/email/aditya@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("aditya@example.com"));
    }

    @Test
    void updateProfileEndpointReturnsUpdatedProfile() throws Exception {
        fakeUserService.updateProfileResponse = sampleUser(42L, "aditya", "aditya@example.com");

        mockMvc.perform(put("/user")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aditya",
                                  "about": "About me",
                                  "headLine": "Engineer",
                                  "profilePictureUrl": "https://example.com/pic.png",
                                  "roles": ["USER"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.name").value("Aditya"));
    }

    @Test
    void changePasswordEndpointReturnsNoContent() throws Exception {
        mockMvc.perform(post("/user/change-password")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"old-secret","newPassword":"new-secret"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Assertions.assertEquals(42L, fakeUserService.changePasswordUserId);
        Assertions.assertEquals("old-secret", fakeUserService.changePasswordOldPassword);
        Assertions.assertEquals("new-secret", fakeUserService.changePasswordNewPassword);
    }

    private static UserDTO sampleUser(Long id, String username, String email) {
        return new UserDTO(
                id,
                username,
                email,
                "Aditya",
                "Engineer",
                "About me",
                "https://example.com/pic.png",
                Set.of(Role.USER),
                null,
                null
        );
    }

    private static final class FakeUserService extends UserServiceImpl implements UserService {
        private UserDTO signUpResponse;
        private AuthResponseDTO loginResponse;
        private UserDTO getByUserIdResponse;
        private UserDTO getUserByUsernameResponse;
        private UserDTO getUserByEmailResponse;
        private UserDTO updateProfileResponse;
        private AuthResponseDTO refreshResponse;
        private String verifyEmailToken;
        private String logoutToken;
        private String forgotPasswordEmail;
        private String resetPasswordToken;
        private String resetPasswordNewPassword;
        private Long changePasswordUserId;
        private String changePasswordOldPassword;
        private String changePasswordNewPassword;

        private FakeUserService() {
            super(null, null, null, null, null, null, null, null, null);
        }

        @Override
        public UserDTO signUp(SignUpRequestDTO signUpRequestDTO) {
            return signUpResponse;
        }

        @Override
        public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
            return loginResponse;
        }

        @Override
        public UserDTO getUserByEmail(String email) {
            return getUserByEmailResponse;
        }

        @Override
        public UserDTO updateProfile(Long userId, UserDTO userDTO) {
            return updateProfileResponse;
        }

        @Override
        public void verifyEmail(String token) {
            this.verifyEmailToken = token;
        }

        @Override
        public AuthResponseDTO refresh(String refreshToken) {
            return refreshResponse;
        }

        @Override
        public void logout(String refreshToken) {
            this.logoutToken = refreshToken;
        }

        @Override
        public void forgotPassword(String email) {
            this.forgotPasswordEmail = email;
        }

        @Override
        public void resetPassword(String token, String newPassword) {
            this.resetPasswordToken = token;
            this.resetPasswordNewPassword = newPassword;
        }

        @Override
        public void changePassword(Long userId, String oldPassword, String newPassword) {
            this.changePasswordUserId = userId;
            this.changePasswordOldPassword = oldPassword;
            this.changePasswordNewPassword = newPassword;
        }

        @Override
        public UserDTO getUserByUsername(String username) {
            return getUserByUsernameResponse;
        }

        @Override
        public UserDTO getByUserId(Long userId) {
            return getByUserIdResponse;
        }
    }

    private static final class FakeUsernameGenerator extends UsernameGenerator {
        private UsernameCheckResponseDTO availabilityResponse;

        private FakeUsernameGenerator() {
            super(null);
        }

        @Override
        public UsernameCheckResponseDTO checkUsernameAvailability(String username) {
            return availabilityResponse;
        }
    }
}
