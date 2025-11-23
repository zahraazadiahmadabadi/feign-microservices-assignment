package com.example.profileservice.service;

import com.example.profileservice.client.UserClient;
import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.entity.Profile;
import com.example.profileservice.exception.ProfileNotFoundException;
import com.example.profileservice.exception.UserNotFoundException;
import com.example.profileservice.mapper.ProfileMapper;
import com.example.profileservice.repository.ProfileRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final UserClient userClient;

    @Transactional
    public ProfileResponseDTO create(ProfileRequestDTO dto) {
        Long userId = dto.getUserId();

        try {
            userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(userId);
        }

        Profile profile = profileMapper.toEntity(dto);
        Profile saved = profileRepository.save(profile);
        return profileMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(profileId));

        UserDTO user;
        try {
            user = userClient.getUserById(profile.getUserId());
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(profile.getUserId());
        }

        return profileMapper.toUserProfileDto(profile, user);
    }
}
