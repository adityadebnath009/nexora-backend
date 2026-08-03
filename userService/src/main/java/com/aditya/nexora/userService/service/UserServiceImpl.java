package com.aditya.nexora.userService.service;

import com.aditya.nexora.userService.dto.AuthResponseDTO;
import com.aditya.nexora.userService.entity.*;
import com.aditya.nexora.userService.enums.Role;
import com.aditya.nexora.userService.dto.LoginRequestDTO;
import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.exception.BadRequestException;
import com.aditya.nexora.userService.exception.ForbiddenException;
import com.aditya.nexora.userService.exception.UserAlreadyExistedException;
import com.aditya.nexora.userService.repository.EmailVerificationTokenRepository;
import com.aditya.nexora.userService.repository.PasswordResetTokenRepository;
import com.aditya.nexora.userService.repository.RefreshTokenRepository;
import com.aditya.nexora.userService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Ref;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;



@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final UsernameGenerator usernameGenerator;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, JwtService jwtService, UsernameGenerator usernameGenerator, EmailVerificationTokenRepository emailVerificationTokenRepository, EmailService emailService, RefreshTokenRepository refreshTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.usernameGenerator = usernameGenerator;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        log.info("User SignUp Request: {}", signUpRequestDTO);
        if(userRepository.existsByEmail(signUpRequestDTO.email())){
            log.error("User with email {} already exists", signUpRequestDTO.email());
            throw new UserAlreadyExistedException("User with email " + signUpRequestDTO.email() + " already exists");
        }

        String username = signUpRequestDTO.userName();
        if (username != null && !username.isBlank()) {
            username = username.toLowerCase().trim();
            if (userRepository.existsByUsername(username)) {
                List<String> suggestions = usernameGenerator.generateUsernameSuggestions(username);
                throw new BadRequestException("Username is already taken. Suggestions: " + suggestions);
            }
        } else {
            // Generate unique username from name
            String base = usernameGenerator.generateBaseUsername(signUpRequestDTO.name());
            username = base;
            while (userRepository.existsByUsername(username)) {
                int suffix = ThreadLocalRandom.current().nextInt(100, 10000);
                username = base + "-" + suffix;
            }
        }


        User user = User.builder().
                name(signUpRequestDTO.name()).
                email(signUpRequestDTO.email()).
                username(username).
                roles(Collections.singleton(Role.USER)).
                password(passwordEncoder.encode(signUpRequestDTO.password())).
                build();

        User savedUser = userRepository.save(user);


        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder().
                token(token).
                user(user).
                expiryDate(Instant.now().plus(24, ChronoUnit.HOURS)).
                build();

        emailVerificationTokenRepository.save(emailVerificationToken);
        log.info("Email verification token saved: {}", emailVerificationToken);
        log.info("User created successfully: {}", savedUser);
        log.info("Email verification token sent to user: {}", savedUser.getEmail());

        emailService.sendEmailVerification(savedUser.getEmail(), token);


        return mapToDTO(savedUser);
    }

    @Transactional
    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {
        log.info("User Login Request: {}", loginRequestDTO);
        User user = userRepository.findByEmail(loginRequestDTO.email()).orElseThrow(() -> new BadRequestException("User not found with email: " + loginRequestDTO.email()));

        if(!user.isEmailVerified()){
            log.warn("Login attempt for unverified email: {}", user.getEmail());
            throw new ForbiddenException("Please verify your email before login");

        }

        if(!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())){
            log.error("Invalid password or Email");
            throw new BadRequestException("Invalid password or Email");
        }
        log.info("User logged in successfully: {}", user);

        String accessTokenString = jwtService.generateAccessToken(new CustomUserDetails(user));
        String refreshTokenString = jwtService.generateRefreshToken(new CustomUserDetails(user));


        RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenString).
                user(user).
                expiryDate(Instant.now().plus(30, ChronoUnit.DAYS)).
                build();
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponseDTO(accessTokenString, refreshTokenString, mapToDTO(user));

    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found with email: " + email));
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        user.setName(userDTO.name());
        user.setAbout(userDTO.about());
        user.setHeadline(userDTO.headLine());
        user.setRoles(userDTO.roles());
        user.setProfilePictureUrl(userDTO.profilePictureUrl());
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Transactional
    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.
                findByToken(token).orElseThrow(() -> new BadRequestException("Invalid or expired token"));


        log.info("Email verification token: {}", emailVerificationToken);

        if (emailVerificationToken == null) {

            throw new BadRequestException("Invalid or missing verification token");
        }

        if(emailVerificationToken.getExpiryDate().isBefore(Instant.now()))
        {
            log.info("Email verification token expiry date: {}", emailVerificationToken.getExpiryDate());
            emailVerificationTokenRepository.delete(emailVerificationToken);
            throw new BadRequestException("Verification token has expired. Please sign up again.");
        }
        log.info("Email verified successfully for user: {}", emailVerificationToken.getUser().getEmail());

        User user = emailVerificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(emailVerificationToken);


    }

    @Transactional
    @Override
    public AuthResponseDTO refresh(String refreshToken) {

        RefreshToken refreshTokenString = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if(refreshTokenString.getExpiryDate().isBefore(Instant.now()))
        {
            log.info("Refresh token expiry date: {}", refreshTokenString.getExpiryDate());
            refreshTokenRepository.delete(refreshTokenString);
            refreshTokenRepository.flush();
            throw new BadRequestException("Refresh token has expired. Please login again.");
        }
        refreshTokenRepository.delete(refreshTokenString);
        refreshTokenRepository.flush();
        log.info("Refresh token deleted successfully for user: {}", refreshTokenString.getUser().getEmail());

        User user = refreshTokenString.getUser();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        String accessTokenString = jwtService.generateAccessToken(customUserDetails);
        String newRefreshTokenString = jwtService.generateRefreshToken(customUserDetails);

        RefreshToken refreshToken1 = RefreshToken.builder()
                .token(newRefreshTokenString)
                .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken1);


        return new AuthResponseDTO(accessTokenString, newRefreshTokenString, mapToDTO(user));
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        log.info("User logout: {}", refreshToken);
        RefreshToken refreshTokenString = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if(refreshTokenString == null)
        {
            throw new BadRequestException("Invalid refresh token");
        }
        log.info("User logout: {}", refreshToken);
        refreshTokenRepository.delete(refreshTokenString);
        log.info("User logout successfully with email:{}", refreshTokenString.getUser().getEmail());


    }

    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found with email: " + email));
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(passwordResetToken);
        emailService.sendPasswordResetEmail(email, token);


    }


    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new BadRequestException("Invalid or expired token"));
        if(passwordResetToken == null)
        {
            log.error("Invalid or expired token in resetPassword:");
            throw new BadRequestException("Invalid or expired token");
        }
        if(passwordResetToken.getExpiryDate().isBefore(Instant.now()))
        {
            log.error("Password reset token has expired");
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new BadRequestException("Token has expired. Please request a new password reset.");
        }
        User user = passwordResetToken.getUser();
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
        log.info("Password reset successfully for user: {}", user.getEmail());

    }


    @Transactional
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("User change password request: userId: {}, oldPassword: {}, newPassword: {}", userId, oldPassword, newPassword);
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            log.error("Invalid old password");
            throw new BadRequestException("Invalid old password");
        }
        String newPasswordEncoded = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getHeadline(),
                user.getAbout(),
                user.getProfilePictureUrl(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserDTO getUserByUsername(String username) {
       User user = userRepository.
               findByUsername(username.toLowerCase().trim())
               .orElseThrow(() -> new BadRequestException("User not found with username: " + username));
       return mapToDTO(user);
    }
    public UserDTO getByUserId(Long userId) {
        return mapToDTO(userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found with id: " + userId)));
    }


}
