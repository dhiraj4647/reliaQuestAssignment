package com.example.rqchallenge.service;

import com.example.rqchallenge.exception.EmployeeDataNotFoundException;
import com.example.rqchallenge.helper.EmployeeHelper;
import com.example.rqchallenge.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.rqchallenge.constant.EmployeeConstant.*;

@Slf4j
@Service
public class EmployeeService {

    EmployeeHelper employeeHelper;

    @Autowired
    public EmployeeService(EmployeeHelper employeeHelper) {
        this.employeeHelper = employeeHelper;
    }


    /**
     * Fetches a list of all employees' data from the system.
     *
     * @return A List containing all employees' data.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response or in cache.
     *
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public List<Employee> getAllEmployees() {
        try{
            log.info("Fetching all employees data list");
            return employeeHelper.fetchAllEmployeeData();
        } catch (EmployeeDataNotFoundException ee) {
            throw ee;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Filters the list of employees based on a given search string.
     * <p>
     * This method fetches all employee data and filters the list based on whether
     * the employee's name contains the provided search string (case insensitive).
     *
     * @param searchString The string to search for all the employee list
     * @return A filtered List of Employee objects whose names contain the search string.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response or in cache.
     *
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public List<Employee> filterEmpNameFromSearchString(String searchString) {
        try {
            List<Employee> employeeList = employeeHelper.fetchAllEmployeeData();

            log.info("Filtering the employee names according to search string {}",searchString);
            List<Employee> filterNameEmpList = employeeList.stream()
                    .filter(employee -> employee.getName().toLowerCase().contains(searchString.toLowerCase())
            ).collect(Collectors.toList());
            return filterNameEmpList;
        } catch (EmployeeDataNotFoundException ee) {
            log.error("Data not found in the external API");
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while filtering employee names based on searchString : {}",searchString);
            throw e;
        }
    }

    /**
     * Retrieves the highest salary from all employee list fetched from external api or in cache.
     * <p>
     * This method fetches all employee data and calculates the highest salary
     * from the retrieved list of employees.
     *
     * @return The highest salary among all employees as an Integer.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response or in cache.
     *
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public Integer getHighestSalaryOfEmployees(){
        try {
            List<Employee> employeeList = employeeHelper.fetchAllEmployeeData();
            log.info("Fetching highest salary from employee list");
            return employeeList.stream().mapToInt(Employee::getSalary).max().getAsInt();
        } catch (EmployeeDataNotFoundException ee) {
            log.error("Data not found in the external API");
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while filtering highest salary for emp");
            throw e;
        }
    }

    /**
     * Retrieves the names of the top 10 highest-earning employees.
     * <p>
     * This method fetches all employee data, sorts them based on salary in descending order,
     * and retrieves the names of the top 10 employees with the highest salaries.
     *
     * @return A List of String containing the names of the top 10 highest-earning employees.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response or in cache.
     *
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public List<String> getTopTenHighestEarningEmployeeNames() {
        try {
            List<Employee> employeeList = employeeHelper.fetchAllEmployeeData();

            log.info("Filtering the top-10 highest salary details for employees");
            List<String> empNameList = employeeList.stream()
                    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                    .limit(10)
                    .map(Employee::getName)
                    .collect(Collectors.toList());
            return empNameList;
        } catch (EmployeeDataNotFoundException ee) {
            log.error("Data not found in the external API");
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while retrieving top 10 highest salary name");
            throw e;
        }
    }


    /**
     * Retrieves employee details based on the provided ID.
     * <p>
     * This method validates the ID, fetches employee details from the helper,
     * and handles various exceptions that may occur during the process.
     *
     * @param id The ID of the employee to fetch details for. Must not be empty or null.
     * @return The Employee object containing details for the specified ID.
     * @throws IllegalArgumentException    If the provided ID is empty or null.
     *                                     This exception is thrown to indicate invalid input.
     * @throws NumberFormatException       If the provided ID cannot be parsed as an valid integer.
     *                                     This exception indicates that the ID format is incorrect.
     * @throws EmployeeDataNotFoundException If no employee data is found for the provided ID.
     *                                      This exception indicates that the employee data could not be retrieved.
     * @throws Exception                    If an unexpected error occurs during the operation.
     *
     */
    public Employee getEmployeeDetailsById(String id) {
        try {
            if(id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Data should not empty or null");
            }
            log.info("Fetching the data employee details for id: {}",id);
            return employeeHelper.fetchEmployeeDetailsById(validateIdData(id));
        } catch (NumberFormatException nfe) {
            log.error("Invalid input provided for field id: {}, expecting integer value",id);
            throw nfe;
        } catch (IllegalArgumentException iae) {
            log.error("Invalid data provided",iae);
            throw iae;
        } catch (EmployeeDataNotFoundException ee) {
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while fetching the data for id {}",id);
            throw e;
        }
    }

    /**
     * Deletes an employee record based on the provided ID.
     * <p>
     * This method validates the ID, deletes the employee record using the helper,
     * and handles various exceptions that may occur during the process.
     *
     * @param id The ID of the employee record to delete. Must not be empty or null.
     * @return A message indicating the success status of the delete operation.
     * @throws IllegalArgumentException    If the provided ID is empty or null.
     *                                     This exception is thrown to indicate invalid input.
     * @throws NumberFormatException       If the provided ID cannot be parsed as an integer.
     *                                     This exception indicates that the ID format is incorrect.
     * @throws HttpClientErrorException    If an HTTP client error occurs during the deletion operation.
     *                                     This exception typically indicates issues with the request to the server.
     * @throws Exception                    If an unexpected error occurs during the operation.
     *                                      This could indicate issues with data deletion or underlying systems.
     */
    public String deleteEmployeeById(String id) {
        try {
            if(id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Data should not empty or null");
            }
            return employeeHelper.deleteEmployeeDetailsById(validateIdData(id));
        } catch (NumberFormatException nfe) {
            log.error("Invalid input provided for field id: {}, expecting integer value",id);
            throw nfe;
        } catch (IllegalArgumentException iae) {
            log.error("Invalid data provided",iae);
            throw iae;
        } catch (HttpClientErrorException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error occurred while deleting the data for id {}",id);
            throw e;
        }
    }


    /**
     * Validates and converts the provided ID string to an Integer.
     * <p>
     * This method attempts to parse the provided ID string into an Integer.
     *
     * @param id The ID string to validate and convert to an Integer.
     * @return The validated ID as an Integer.
     * @throws NumberFormatException If the provided ID cannot be parsed as an Integer.
     *                              This typically occurs when the ID format is incorrect or non-numeric.
     */
    private Integer validateIdData(String id) {
        try{
            return Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            log.error("Invalid data provided for id, please provide valid integer",nfe);
            throw new NumberFormatException("Invalid data provided for id, please provide valid integer");
        }
    }


    /**
     * Creates a new employee record based on the provided data.
     *
     * @param data A Map containing the employee data with keys such as "name", "salary", "age", etc.
     *             Must not be null.
     * @return The Employee object representing the newly created employee.
     * @throws NumberFormatException If there is an error converting data to a numeric format.
     *                               This typically occurs when parsing salary or other numeric fields.
     * @throws IllegalArgumentException If the provided data is invalid or missing required fields.
     *                                  This exception indicates issues with data validation.
     * @throws Exception If an unexpected error occurs during the employee creation process.
     *                   This could indicate issues with database operations or underlying systems.
     */
    public Employee createEmployee(Map<String,Object> data) {
        try{
            log.info("Saving the employee details for employee {}",data.get("name"));
            validateData(data);
            return employeeHelper.createEmployee(data);
        } catch (NumberFormatException nfe) {
            throw nfe;
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Validates the provided data map for employee creation.
     * <p>
     * This method checks if the data map contains required fields ('name', 'salary', 'age')
     * and validates their values. It ensures that 'age' and 'salary' are integers and non-negative,
     * and throws exceptions if any validation fails.
     *
     * @param data A Map containing the employee data with keys such as "name", "salary", "age".
     *             Must not be null.
     * @throws IllegalArgumentException If the provided data is incomplete or contains invalid values.
     *                                  This exception indicates issues with data validation.
     * @throws NumberFormatException    If there is an error converting 'age' or 'salary' to integers,
     *
     */
    private void validateData(Map<String, Object> data) {
        try{
            if(data.containsKey(EMP_NAME) && data.containsKey(EMP_SAL) && data.containsKey(EMP_AGE)
                    && data.get(EMP_NAME)!=null && data.get(EMP_SAL)!= null && data.get(EMP_AGE)!= null) {
                try{
                    int age = Integer.parseInt((String)data.get(EMP_AGE));
                    int salary = Integer.parseInt((String)data.get(EMP_SAL));
                    if(age < 0 || salary <0) {
                        throw new IllegalArgumentException("Invalid data provided for field age and salary");
                    }
                } catch (NumberFormatException nfe) {
                    log.error("Invalid data provided for age and salary, please provide valid integer",nfe);
                    throw new NumberFormatException("Invalid data provided for age and salary, please provide valid integer");
                }
            } else {
                throw new IllegalArgumentException("Incomplete data provided, please provide name, age, salary");
            }
        } catch (IllegalArgumentException iae) {
            log.error("Error occurred while validating the data, invalid inputs provided", iae);
            throw iae;
        }
    }

}
