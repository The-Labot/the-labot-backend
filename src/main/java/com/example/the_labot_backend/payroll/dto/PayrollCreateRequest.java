package com.example.the_labot_backend.payroll.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PayrollCreateRequest {
    int year;
    int month;
    private List<Long> workerIds;
}
