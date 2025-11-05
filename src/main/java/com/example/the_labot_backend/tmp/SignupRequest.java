package com.example.the_labot_backend.tmp;


import com.example.the_labot_backend.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String phoneNumber;
    private String password;
    private String name;
    private Role role; // ex) ROLE_WORKER, ROLE_ADMIN
}
