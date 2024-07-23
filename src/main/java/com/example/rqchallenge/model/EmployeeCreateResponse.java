package com.example.rqchallenge.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateResponse {
    private String status;
    private Employee data;
    private String message;
}
