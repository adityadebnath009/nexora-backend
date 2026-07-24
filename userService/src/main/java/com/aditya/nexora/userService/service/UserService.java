package com.aditya.nexora.userService.service;

import com.aditya.nexora.userService.dto.LoginRequestDTO;
import com.aditya.nexora.userService.dto.SignUpRequestDTO;
import com.aditya.nexora.userService.dto.UserDTO;
import com.aditya.nexora.userService.entity.User;
import com.aditya.nexora.userService.exception.BadRequestException;
import com.aditya.nexora.userService.exception.UserAlreadyExistedException;
import com.aditya.nexora.userService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
@Service
public class UserService implements UserServiceImpl{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UsernameGenerator usernameGenerator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, UsernameGenerator usernameGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.usernameGenerator = usernameGenerator;
    }

    @Override
    public UserDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        log.info("User SignUp Request: {}", signUpRequestDTO);
        if(userRepository.existsByEmail(signUpRequestDTO.email())){
            log.error("User with email {} already exists", signUpRequestDTO.email());
            throw new UserAlreadyExistedException("User with email " + signUpRequestDTO.email() + " already exists");
        }

        String username = signUpRequestDTO.userName();
        // 2. If username provided, validate it; if not provided, generate a base one
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
                password(passwordEncoder.encode(signUpRequestDTO.password())).
                build();

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        return "";
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found with email: " + email));
        return mapToDTO(user);
    }

    @Override
    public UserDTO updateProfile(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        user.setName(userDTO.name());
        user.setAbout(userDTO.about());
        user.setHeadline(userDTO.headLine());
        user.setProfilePictureUrl(userDTO.profilePictureUrl());
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
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
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
