package com.example.userservice.webmvc;

import com.example.userservice.controller.UserController;
import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import com.example.userservice.exception.GlobalExceptionHandler;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.IntStream;

import static com.example.userservice.uril.FakeEntities.userRequestDTOBuilder;
import static com.example.userservice.uril.FakeEntities.userResponseDTOBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("web-test")
@DisplayName("UserController WebMvc Tests")
class UserWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /users - create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {

        UserRequestDTO request = userRequestDTOBuilder().build();

        UserResponseDTO expectedResponse = userResponseDTOBuilder().build();

        when(userService.create(any(UserRequestDTO.class))).thenReturn(expectedResponse);

        String json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserResponseDTO actualResponse =
                objectMapper.readValue(json, UserResponseDTO.class);

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);

        verify(userService, times(1)).create(any(UserRequestDTO.class));
    }

    @Test
    @DisplayName("POST /users - should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {

        UserRequestDTO invalidRequest = UserRequestDTO.builder()
                .name("")
                .email("valid.email@test.com")
                .build();

        String json = mockMvc.perform(post("/users")
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
        assertThat(firstError.get("field").asText()).isEqualTo("name");
        assertThat(firstError.get("message").asText()).isEqualTo("Name is required");

        verify(userService, never()).create(any(UserRequestDTO.class));
    }

    @Test
    @DisplayName("POST /users - should return 400 when email is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        UserRequestDTO invalidRequest = UserRequestDTO.builder()
                .name("Some Name")
                .email("not-an-email")
                .build();

        String json = mockMvc.perform(post("/users")
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
        assertThat(firstError.get("field").asText()).isEqualTo("email");
        assertThat(firstError.get("message").asText()).isEqualTo("Email must be valid");

        verify(userService, never()).create(any(UserRequestDTO.class));
    }

    @Test
    @DisplayName("GET /users/{id} - get user by id successfully (recursive compare)")
    void shouldGetUserByIdSuccessfully() throws Exception {
        UserResponseDTO expected = userResponseDTOBuilder().build();
        Long userId = expected.getId();

        when(userService.getById(userId)).thenReturn(expected);


        String json = mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();


        UserResponseDTO actual =
                objectMapper.readValue(json, UserResponseDTO.class);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(userService, times(1)).getById(userId);
    }

    @Test
    @DisplayName("GET /users/{id} - should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        Long id = 999L;
        when(userService.getById(id)).thenThrow(new UserNotFoundException(id));

        String json = mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asInt()).isEqualTo(404);
        assertThat(root.get("error").asText()).isEqualTo("Not Found");
        assertThat(root.get("message").asText()).isEqualTo("User with id 999 not found");

        verify(userService, times(1)).getById(id);
    }

    @Test
    @DisplayName("GET /users - get paginated users successfully (recursive list compare)")
    void shouldGetAllUsersSuccessfully() throws Exception {
        int pageNumber = 0;
        int pageSize = 5;

        List<UserResponseDTO> expectedList = IntStream.range(0, pageSize)
                .mapToObj(i -> userResponseDTOBuilder().build())
                .toList();

        Page<UserResponseDTO> page =
                new PageImpl<>(expectedList, PageRequest.of(pageNumber, pageSize), expectedList.size());

        when(userService.getAll(any(Pageable.class))).thenReturn(page);

        String json = mockMvc.perform(get("/users")
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("number").asInt()).isEqualTo(pageNumber);
        assertThat(root.get("size").asInt()).isEqualTo(pageSize);
        assertThat(root.get("totalElements").asInt()).isEqualTo(expectedList.size());

        JsonNode contentNode = root.get("content");
        List<UserResponseDTO> actualList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<>() {
                });

        assertThat(actualList)
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(userService, times(1)).getAll(any(Pageable.class));
    }
}
