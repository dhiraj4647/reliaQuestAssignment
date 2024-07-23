package com.example.rqchallenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeModel {
    @JsonProperty("id")
    Integer id;
    @JsonProperty("employee_name")
    String name;
    @JsonProperty("employee_salary")
    Integer salary;
    @JsonProperty("employee_age")
    Integer age;
    @JsonProperty("profile_image")
    String profileImage;

    public static Employee convertEmployeeModelToEmployee(EmployeeModel employeeModel) {

        Employee employee = new Employee();
        employee.setId(employeeModel.getId());
        employee.setName(employeeModel.getName());
        employee.setSalary(employeeModel.getSalary());
        employee.setAge(employeeModel.getAge());
        employee.setProfileImage(employeeModel.getProfileImage());

        return employee;
    }
}
