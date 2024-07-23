package com.example.rqchallenge.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponse {
    private String status;
    private EmployeeModel data;
}
