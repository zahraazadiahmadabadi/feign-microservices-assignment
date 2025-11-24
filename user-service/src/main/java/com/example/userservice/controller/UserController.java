package com.example.userservice.controller;

import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // save
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new user",
            description = "Register a new user with name and unique email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public UserResponseDTO save(
            @RequestBody @Valid
            @Parameter(description = "User data for creation")
            UserRequestDTO request
    ) {
        return userService.create(request);
    }

    // getById
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a user by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponseDTO getById(
            @PathVariable("id")
            @Parameter(description = "ID of the user", example = "1")
            Long id
    ) {
        return userService.getById(id);
    }

    // getAll pageable
    @GetMapping
    @Operation(
            summary = "Get all users (paged)",
            description = "Retrieve a paginated list of all users"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paged list of users")
    })
    public Page<UserResponseDTO> getAll(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.ASC
            )
            @Parameter(description = "Pagination and sorting information")
            Pageable pageable
    ) {
        return userService.getAll(pageable);
    }
}
