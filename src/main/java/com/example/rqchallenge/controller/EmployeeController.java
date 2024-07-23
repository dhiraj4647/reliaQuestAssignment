package com.example.rqchallenge.controller;

import com.example.rqchallenge.employees.IEmployeeController;
import com.example.rqchallenge.model.Employee;
import com.example.rqchallenge.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@RestController
public class EmployeeController implements IEmployeeController {

    @Autowired
    public EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {

        List<Employee> employeeList = employeeService.getAllEmployees();
        return new ResponseEntity<>(employeeList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> employeeList = employeeService.filterEmpNameFromSearchString(searchString);
        return new ResponseEntity<>(employeeList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        Employee employee = employeeService.getEmployeeDetailsById(id);
        return new ResponseEntity<>(employee,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
        return new ResponseEntity<>(highestSalary,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> empNameList = employeeService.getTopTenHighestEarningEmployeeNames();
        return new ResponseEntity<>(empNameList,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(Map<String, Object> employeeInput) {
        Employee employee = employeeService.createEmployee(employeeInput);
        return new ResponseEntity<>(employee,HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        String response = employeeService.deleteEmployeeById(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
