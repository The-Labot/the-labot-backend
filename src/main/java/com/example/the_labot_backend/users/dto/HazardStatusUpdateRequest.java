package com.example.the_labot_backend.users.dto;

import com.example.the_labot_backend.hazards.entity.HazardStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HazardStatusUpdateRequest {
    private HazardStatus status;
}
