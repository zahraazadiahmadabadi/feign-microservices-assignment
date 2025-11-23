package com.example.userservice.service;

import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import com.example.userservice.entity.User;
import com.example.userservice.exception.EmailAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static com.example.userservice.uril.FakeEntities.userRequestDTOBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save user successfully when email is unique")
    void shouldSaveUserSuccessfully() {
        UserRequestDTO request = userRequestDTOBuilder().build();

        UserResponseDTO response = userService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());

        assertThat(userRepository.existsByEmail(request.getEmail())).isTrue();

        User savedUser = userRepository.findById(response.getId())
                .orElseThrow();

        assertThat(savedUser.getName()).isEqualTo(request.getName());
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserRequestDTO firstRequest = userRequestDTOBuilder().build();
        UserResponseDTO firstSaved = userService.create(firstRequest);

        String duplicatedEmail = firstRequest.getEmail();
        UserRequestDTO duplicateRequest = userRequestDTOBuilder()
                .email(duplicatedEmail)
                .build();

        assertThatThrownBy(() -> userService.create(duplicateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(duplicatedEmail)
                .hasMessageContaining("already in use");

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.existsByEmail(duplicatedEmail)).isTrue();
        assertThat(firstSaved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve user by id successfully when user exists")
    void shouldGetUserByIdSuccessfully() {
        UserRequestDTO request = userRequestDTOBuilder().build();
        UserResponseDTO savedUser = userService.create(request);
        Long userId = savedUser.getId();

        UserResponseDTO retrievedUser = userService.getById(userId);

        assertThat(retrievedUser)
                .usingRecursiveComparison()
                .isEqualTo(savedUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user id does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> userService.getById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should retrieve all users with pagination")
    void shouldGetAllUsersWithPagination() {
        IntStream.range(0, 3).forEach(i -> userService.create(userRequestDTOBuilder().build()));

        Pageable pageable = PageRequest.of(0, 2);

        Page<UserResponseDTO> page = userService.getAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should retrieve empty page when no users exist")
    void shouldGetEmptyPageWhenNoUsersExist() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponseDTO> page = userService.getAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }
}
