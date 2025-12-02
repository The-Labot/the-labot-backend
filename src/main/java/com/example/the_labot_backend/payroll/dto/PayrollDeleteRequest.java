package com.example.the_labot_backend.payroll.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PayrollDeleteRequest {
    int year;
    int month;
    private List<Long> workerIds;
}

