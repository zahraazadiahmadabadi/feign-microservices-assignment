package com.example.profileservice.webmvc;

import com.example.profileservice.controller.ProfileController;
import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.exception.GlobalExceptionHandler;
import com.example.profileservice.exception.ProfileNotFoundException;
import com.example.profileservice.exception.UserNotFoundException;
import com.example.profileservice.service.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.profileservice.util.FakeEntities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("web-test")
@DisplayName("ProfileController WebMvc Tests")
class ProfileWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /profiles - create profile successfully")
    void shouldCreateProfileSuccessfully() throws Exception {

        ProfileRequestDTO request = profileRequestDTOBuilder().build();

        ProfileResponseDTO expectedResponse = profileResponseDTOBuilder().build();

        when(profileService.create(any(ProfileRequestDTO.class))).thenReturn(expectedResponse);

        String json = mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ProfileResponseDTO actualResponse =
                objectMapper.readValue(json, ProfileResponseDTO.class);

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

        verify(profileService, times(1)).create(any(ProfileRequestDTO.class));
    }

    @Test
    @DisplayName("POST /profiles - should return 400 when userId is null")
    void shouldReturn400WhenUserIdIsNull() throws Exception {

        ProfileRequestDTO invalidRequest = ProfileRequestDTO.builder()
                .userId(null)
                .bio("Some bio")
                .location("Some location")
                .age(25)
                .build();

        String json = mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("status").asInt()).isEqualTo(400);
        assertThat(root.get("error").asText()).isEqualTo("Validation Failed");

        JsonNode firstError = root.get("validationErrors").get(0);
        assertThat(firstError.get("field").asText()).isEqualTo("userId");
        assertThat(firstError.get("message").asText()).isEqualTo("User id is required");

        verify(profileService, never()).create(any(ProfileRequestDTO.class));
    }

    @Test
    @DisplayName("POST /profiles - should return 400 when age is negative")
    void shouldReturn400WhenAgeIsNegative() throws Exception {

        ProfileRequestDTO invalidRequest = ProfileRequestDTO.builder()
                .userId(1L)
                .bio("Some bio")
                .location("Some location")
                .age(-1)
                .build();

        String json = mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("status").asInt()).isEqualTo(400);
        assertThat(root.get("error").asText()).isEqualTo("Validation Failed");

        JsonNode firstError = root.get("validationErrors").get(0);
        assertThat(firstError.get("field").asText()).isEqualTo("age");
        assertThat(firstError.get("message").asText()).isEqualTo("Age must be positive");

        verify(profileService, never()).create(any(ProfileRequestDTO.class));
    }

    @Test
    @DisplayName("POST /profiles - should return 400 when bio exceeds max length")
    void shouldReturn400WhenBioExceedsMaxLength() throws Exception {

        String longBio = "a".repeat(501);
        ProfileRequestDTO invalidRequest = ProfileRequestDTO.builder()
                .userId(1L)
                .bio(longBio)
                .location("Some location")
                .age(25)
                .build();

        String json = mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("status").asInt()).isEqualTo(400);
        assertThat(root.get("error").asText()).isEqualTo("Validation Failed");

        JsonNode firstError = root.get("validationErrors").get(0);
        assertThat(firstError.get("field").asText()).isEqualTo("bio");
        assertThat(firstError.get("message").asText()).isEqualTo("Bio must be at most 500 characters");

        verify(profileService, never()).create(any(ProfileRequestDTO.class));
    }

    @Test
    @DisplayName("POST /profiles - should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        ProfileRequestDTO request = profileRequestDTOBuilder().build();
        Long userId = request.getUserId();

        when(profileService.create(any(ProfileRequestDTO.class)))
                .thenThrow(new UserNotFoundException(userId));

        String json = mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asInt()).isEqualTo(404);
        assertThat(root.get("error").asText()).isEqualTo("Not Found");
        assertThat(root.get("message").asText()).contains("User with id " + userId + " not found");

        verify(profileService, times(1)).create(any(ProfileRequestDTO.class));
    }

    @Test
    @DisplayName("GET /profiles/{id} - get profile with user successfully")
    void shouldGetProfileWithUserSuccessfully() throws Exception {
        UserProfileDTO expected = userProfileDTOBuilder().build();
        Long profileId = expected.getProfileId();

        when(profileService.getUserProfile(profileId)).thenReturn(expected);

        String json = mockMvc.perform(get("/profiles/{id}/with-user", profileId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserProfileDTO actual =
                objectMapper.readValue(json, UserProfileDTO.class);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(profileService, times(1)).getUserProfile(profileId);
    }

    @Test
    @DisplayName("GET /profiles/{id} - should return 404 when profile not found")
    void shouldReturn404WhenProfileNotFound() throws Exception {
        Long id = 999L;
        when(profileService.getUserProfile(id)).thenThrow(new ProfileNotFoundException(id));

        String json = mockMvc.perform(get("/profiles/{id}/with-user", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asInt()).isEqualTo(404);
        assertThat(root.get("error").asText()).isEqualTo("Not Found");
        assertThat(root.get("message").asText()).isEqualTo("Profile with id 999 not found");

        verify(profileService, times(1)).getUserProfile(id);
    }

    @Test
    @DisplayName("GET /profiles/{id} - should return 404 when user not found")
    void shouldReturn404WhenUserNotFoundWhileGettingProfile() throws Exception {
        Long profileId = 1L;
        Long userId = 999L;

        when(profileService.getUserProfile(profileId))
                .thenThrow(new UserNotFoundException(userId));

        String json = mockMvc.perform(get("/profiles/{id}/with-user", profileId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asInt()).isEqualTo(404);
        assertThat(root.get("error").asText()).isEqualTo("Not Found");
        assertThat(root.get("message").asText()).isEqualTo("User with id 999 not found");

        verify(profileService, times(1)).getUserProfile(profileId);
    }
}

