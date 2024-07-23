package com.example.rqchallenge.helper;

import com.example.rqchallenge.exception.EmployeeDataNotFoundException;
import com.example.rqchallenge.model.*;
import com.example.rqchallenge.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.rqchallenge.constant.EmployeeConstant.*;

@Component
@Slf4j
public class EmployeeHelper {

    RestTemplate restTemplate;

    EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeHelper(RestTemplate restTemplate, EmployeeRepository employeeRepository) {
        this.restTemplate = restTemplate;
        this.employeeRepository = employeeRepository;
    }

    @PostConstruct
    public void init(){
        fetchAllEmployeeData();
    }


    private HttpEntity getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }


    /**
     * Fetches the list of all employees from an external API.
     * <p>
     * This method sends a GET request to the external API, retrieves the list of employees,
     * converts them to Employee entities, and saves them into a cache database.
     * If the API response is empty or null, an EmployeeDataNotFoundException is thrown.
     * If there's an HTTP client or server error, the method falls back to fetching data from the cache database.
     *
     * @return A List of Employee objects containing details of all employees fetched from the external API or cache.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response.
     *                                      This typically occurs when the API response is empty or null.
     * @throws HttpClientErrorException      If an HTTP client error occurs during the request to the external API.
     *                                      This typically indicates issues with the request itself.
     * @throws HttpServerErrorException      If an HTTP server error occurs during the request to the external API.
     *                                      This typically indicates issues on the server side.
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public List<Employee> fetchAllEmployeeData() {
        try {
            log.info("Fetching the employee list from the external API and saving the response in cache db");
            String url = BASE_URL+FETCH_ALL_EMPLOYEES;
            ResponseEntity<EmployeeListResponse> response = restTemplate
                    .exchange(url, HttpMethod.GET,getEntity(), EmployeeListResponse.class);
            EmployeeListResponse employeeListResponse = response.getBody();
            if(employeeListResponse ==null || CollectionUtils.isEmpty(employeeListResponse.getData())) {
                throw new EmployeeDataNotFoundException("Data Not Found");
            }
            log.info("converting employee model class to employee entity class");
            List<Employee> employeeList = employeeListResponse.getData().stream()
                    .map(EmployeeModel::convertEmployeeModelToEmployee)
                    .collect(Collectors.toList());
            saveEmployeeListIntoCacheDb(employeeList);
            return employeeList;
        } catch (HttpClientErrorException | HttpServerErrorException he) {
            log.error("Error occurred while fetching employee list from the external API, so now fetching from in-cache db ",he);
            return fetchEmployeeListFromInCacheDb();
        } catch (EmployeeDataNotFoundException ee) {
            log.error("Employee details not found",ee);
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while fetching data from the external API",e);
            throw e;
        }
    }

    private void saveEmployeeListIntoCacheDb(List<Employee> employeeList) {
        try{
            log.info("Saving the employee list in in-cache db");
            employeeRepository.saveAll(employeeList);
        } catch (Exception e) {
            log.error("Error occurred while saving the employee list into in-cache db ",e);
        }
    }

    private List<Employee> fetchEmployeeListFromInCacheDb() {
        try{
            log.info("Fetching all employee list from the in-cache db");
            return employeeRepository.findAll();
        } catch (Exception e) {
            log.error("Error occurred while fetching all employee list from the in-cache db ",e);
            throw e;
        }
    }

    /**
     * Fetches the details of an employee by their ID from an external API.
     * <p>
     * This method sends a GET request to the external API with the provided employee ID,
     * retrieves the employee details, saves them into a cache database. If the API response is empty or null,
     * an EmployeeDataNotFoundException is thrown. If there's an HTTP client or server error,
     * the method falls back to fetching data from the cache database.
     *
     * @param id The ID of the employee whose details are to be fetched. Must not be null.
     * @return The Employee object containing details of the employee fetched from the external API or cache.
     * @throws EmployeeDataNotFoundException If no employee data is found in the external API response.
     *                                      This typically occurs when the API response is empty or null.
     * @throws HttpClientErrorException      If an HTTP client error occurs during the request to the external API.
     *                                      This typically indicates issues with the request itself.
     * @throws HttpServerErrorException      If an HTTP server error occurs during the request to the external API.
     *                                      This typically indicates issues on the server side.
     * @throws Exception                     If an unexpected error occurs during the operation.
     *
     */
    public Employee fetchEmployeeDetailsById(Integer id) {
        try{
            String url =BASE_URL+FETCH_EMPLOYEE_DETAILS_BY_ID+id;
            ResponseEntity<EmployeeResponse> response = restTemplate
                    .exchange(url, HttpMethod.GET,getEntity(), EmployeeResponse.class);
            EmployeeResponse employeeResponse = response.getBody();
            if(employeeResponse == null || employeeResponse.getData()==null) {
                throw new EmployeeDataNotFoundException("Data Not Found");
            }
            log.info("Data for id {} found on external API",id);
            Employee employee = EmployeeModel.convertEmployeeModelToEmployee(employeeResponse.getData());
            saveEmployeeIntoCacheDb(employee);
            return employee;
        } catch (HttpClientErrorException | HttpServerErrorException he) {
            log.error("Error occurred while fetching employee details from the external API for Id {}, " +
                    "so now fetching from in-cache db: {} ",id,he);
            return fetchEmployeeDetailsFromInCacheDb(id);
        } catch (EmployeeDataNotFoundException ee) {
            log.error("Employee details for Id {} not found {}",id,ee);
            throw ee;
        } catch (Exception e) {
            log.error("Error occurred while fetching employee details for Id {} {} ",id,e);
            throw e;
        }
    }

    private Employee fetchEmployeeDetailsFromInCacheDb(Integer id) {
        log.info("Retrieving employee details for employee Id {} from cache db", id);
        return employeeRepository.findById(id).orElseThrow(()->new EmployeeDataNotFoundException("Data Not Found"));
    }

    private void saveEmployeeIntoCacheDb(Employee employee) {
        try{
            employeeRepository.save(employee);
        } catch(Exception e) {
            log.error("Error occurred while saving the employee details for id into the in-cache db",e);
        }
    }


    /**
     * Deletes employee details by ID from an external API.
     * <p>
     * This method sends a DELETE request to the external API with the provided employee ID,
     * deletes the employee details. It also deletes the employee details
     * from the cache database.
     *
     * @param id The ID of the employee whose details are to be deleted. Must not be null.
     * @return A message confirming the deletion of employee details.
     * @throws HttpClientErrorException If an HTTP client error occurs during the request to the external API.
     *                                  This typically indicates issues with the request itself.
     * @throws Exception                If an unexpected error occurs during the operation.
     *                                  This could indicate issues with data processing or underlying systems.
     */
    public String deleteEmployeeDetailsById(Integer id) {
        try {
            String url = BASE_URL+DELETE_EMPLOYEE_DETAILS_BY_ID+id;
            ResponseEntity<EmployeeDeleteResponse> response = restTemplate
                    .exchange(url, HttpMethod.DELETE,getEntity(), EmployeeDeleteResponse.class);
            EmployeeDeleteResponse employeeDeleteResponse = response.getBody();
            deleteEmployeeDetailsFromCacheDb(id);
            log.info("Employee details for id {} deleted successfully.",id);
            return employeeDeleteResponse.getMessage();
        } catch (HttpClientErrorException ex) {
            log.error("Error occurred while connecting to API ",ex);
            throw ex;
        } catch (Exception e) {
            log.error("Employee details not deleted for id {}",id,e);
            throw e;
        }
    }

    private void deleteEmployeeDetailsFromCacheDb(Integer id) {
        log.info("Retrieving employee details for employee Id {} from in-cache db", id);
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if(employeeOptional.isPresent()) {
            employeeRepository.delete(employeeOptional.get());
        } else {
            log.info("Data not found in-memory cache for id {}",id);
        }
    }


    /**
     * Creates a new employee record using data provided in a map.
     * <p>
     * This method sends a POST request to an external API endpoint with employee data,
     * retrieves the created employee details from the API response, saves them into a cache database,
     * and returns the created Employee object.
     *
     * @param data A Map containing employee data with keys such as "name", "salary", "age". Must not be null.
     * @return The Employee object created from the external API response and saved in the cache database.
     * @throws HttpClientErrorException If an HTTP client error occurs during the request to the external API.
     *                                  This typically indicates issues with the request itself.
     * @throws Exception                If an unexpected error occurs during the operation.
     *                                  This could indicate issues with data processing or underlying systems.
     */
    public Employee createEmployee(Map<String,Object> data) {
        try{
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL+CREATE_EMPLOYEE_RECORD);
            ResponseEntity<EmployeeCreateResponse> response = restTemplate
                    .exchange(BASE_URL+CREATE_EMPLOYEE_RECORD, HttpMethod.POST,new HttpEntity<>(data),EmployeeCreateResponse.class);
            EmployeeCreateResponse employeeCreateResponse = response.getBody();
            Employee employee = employeeCreateResponse.getData();
            saveEmployeeIntoCacheDb(employee);
            log.info("Employee record for Id {} create successfully ..!",employee.getId());
            return employee;
        } catch (HttpClientErrorException ex) {
            log.error("Error occurred while connecting to API ",ex);
            throw ex;
        } catch (Exception e) {
            log.error("Error occurred while storing the employee details into db");
            throw e;
        }
    }

}
