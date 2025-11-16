package com.example.the_labot_backend.headoffice.dto;

import com.example.the_labot_backend.headoffice.HeadOffice;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadOfficeResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String representative;
    private String secretCode;

    public static HeadOfficeResponse from(HeadOffice office) {
        return HeadOfficeResponse.builder()
                .id(office.getId())
                .name(office.getName())
                .address(office.getAddress())
                .phoneNumber(office.getPhoneNumber())
                .representative(office.getRepresentative())
                .secretCode(office.getSecretCode())
                .build();
    }
}
