package com.example.profileservice.service;

import com.example.profileservice.client.UserClient;
import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.entity.Profile;
import com.example.profileservice.exception.ProfileNotFoundException;
import com.example.profileservice.exception.UserNotFoundException;
import com.example.profileservice.repository.ProfileRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static com.example.profileservice.util.FakeEntities.profileRequestDTOBuilder;
import static com.example.profileservice.util.FakeEntities.userDTOBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileService Integration Tests")
class ProfileServiceIntegrationTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    @MockBean
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        reset(userClient);
    }

    @Test
    @DisplayName("Should Create profile successfully ")
    void shouldCreateProfileSuccessfully() {
        UserDTO user = userDTOBuilder().build();
        ProfileRequestDTO request = profileRequestDTOBuilder()
                .userId(user.getId())
                .build();

        when(userClient.getUserById(user.getId())).thenReturn(user);

        ProfileResponseDTO response = profileService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(request.getUserId());
        assertThat(response.getBio()).isEqualTo(request.getBio());
        assertThat(response.getLocation()).isEqualTo(request.getLocation());
        assertThat(response.getAge()).isEqualTo(request.getAge());

        assertThat(profileRepository.existsById(response.getId())).isTrue();

        Profile savedProfile = profileRepository.findById(response.getId())
                .orElseThrow();

        assertThat(savedProfile.getUserId()).isEqualTo(request.getUserId());
        assertThat(savedProfile.getBio()).isEqualTo(request.getBio());
        assertThat(savedProfile.getLocation()).isEqualTo(request.getLocation());
        assertThat(savedProfile.getAge()).isEqualTo(request.getAge());

        verify(userClient, times(1)).getUserById(user.getId());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException ")
    void shouldThrowExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;
        ProfileRequestDTO request = profileRequestDTOBuilder()
                .userId(nonExistentUserId)
                .build();

        when(userClient.getUserById(nonExistentUserId))
                .thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> profileService.create(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("not found");

        assertThat(profileRepository.count()).isEqualTo(0);
        verify(userClient, times(1)).getUserById(nonExistentUserId);
    }

    @Test
    @DisplayName("Should retrieve user profile by id successfully when profile and user exist")
    void shouldGetUserProfileByIdSuccessfully() {
        UserDTO user = userDTOBuilder().build();
        ProfileRequestDTO request = profileRequestDTOBuilder()
                .userId(user.getId())
                .build();

        when(userClient.getUserById(user.getId())).thenReturn(user);

        ProfileResponseDTO savedProfile = profileService.create(request);
        Long profileId = savedProfile.getId();

        when(userClient.getUserById(user.getId())).thenReturn(user);

        UserProfileDTO retrievedProfile = profileService.getUserProfile(profileId);

        assertThat(retrievedProfile).isNotNull();
        assertThat(retrievedProfile.getProfileId()).isEqualTo(savedProfile.getId());
        assertThat(retrievedProfile.getUserId()).isEqualTo(savedProfile.getUserId());
        assertThat(retrievedProfile.getBio()).isEqualTo(savedProfile.getBio());
        assertThat(retrievedProfile.getLocation()).isEqualTo(savedProfile.getLocation());
        assertThat(retrievedProfile.getAge()).isEqualTo(savedProfile.getAge());
        assertThat(retrievedProfile.getUser()).isNotNull();
        assertThat(retrievedProfile.getUser().getId()).isEqualTo(user.getId());
        assertThat(retrievedProfile.getUser().getName()).isEqualTo(user.getName());
        assertThat(retrievedProfile.getUser().getEmail()).isEqualTo(user.getEmail());

        verify(userClient, times(2)).getUserById(user.getId());
    }

    @Test
    @DisplayName("Should throw ProfileNotFoundException when profile id does not exist")
    void shouldThrowExceptionWhenProfileNotFound() {
        Long nonExistentProfileId = 999L;

        assertThatThrownBy(() -> profileService.getUserProfile(nonExistentProfileId))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("not found");

        verify(userClient, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist while retrieving profile")
    void shouldThrowExceptionWhenUserNotFoundWhileRetrievingProfile() {
        UserDTO user = userDTOBuilder().build();
        ProfileRequestDTO request = profileRequestDTOBuilder()
                .userId(user.getId())
                .build();

        when(userClient.getUserById(user.getId())).thenReturn(user);

        ProfileResponseDTO savedProfile = profileService.create(request);
        Long profileId = savedProfile.getId();

        when(userClient.getUserById(user.getId()))
                .thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> profileService.getUserProfile(profileId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(user.getId()))
                .hasMessageContaining("not found");

        verify(userClient, times(2)).getUserById(user.getId());
    }
}

