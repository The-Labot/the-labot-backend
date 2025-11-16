package com.example.the_labot_backend.headoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HeadOfficeCheckResponse { 
    private boolean exists;
    private String name;    // 본사명 (존재할 때만)
}
