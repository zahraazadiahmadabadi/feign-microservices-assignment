package com.example.profileservice.mapper;

import com.example.profileservice.dto.ProfileRequestDTO;
import com.example.profileservice.dto.ProfileResponseDTO;
import com.example.profileservice.dto.UserDTO;
import com.example.profileservice.dto.UserProfileDTO;
import com.example.profileservice.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-24T12:10:25+0330",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public Profile toEntity(ProfileRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Profile.ProfileBuilder<?, ?> profile = Profile.builder();

        profile.userId( dto.getUserId() );
        profile.bio( dto.getBio() );
        profile.location( dto.getLocation() );
        profile.age( dto.getAge() );

        return profile.build();
    }

    @Override
    public ProfileResponseDTO toDto(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileResponseDTO.ProfileResponseDTOBuilder profileResponseDTO = ProfileResponseDTO.builder();

        profileResponseDTO.id( profile.getId() );
        profileResponseDTO.userId( profile.getUserId() );
        profileResponseDTO.bio( profile.getBio() );
        profileResponseDTO.location( profile.getLocation() );
        profileResponseDTO.age( profile.getAge() );

        return profileResponseDTO.build();
    }

    @Override
    public UserProfileDTO toUserProfileDto(Profile profile, UserDTO user) {
        if ( profile == null && user == null ) {
            return null;
        }

        UserProfileDTO.UserProfileDTOBuilder userProfileDTO = UserProfileDTO.builder();

        if ( profile != null ) {
            userProfileDTO.profileId( profile.getId() );
            userProfileDTO.userId( profile.getUserId() );
            userProfileDTO.bio( profile.getBio() );
            userProfileDTO.location( profile.getLocation() );
            userProfileDTO.age( profile.getAge() );
        }
        userProfileDTO.user( user );

        return userProfileDTO.build();
    }
}
