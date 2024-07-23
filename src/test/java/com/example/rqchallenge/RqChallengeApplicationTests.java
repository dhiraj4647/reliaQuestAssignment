package com.example.rqchallenge;

import com.example.rqchallenge.exception.EmployeeDataNotFoundException;
import com.example.rqchallenge.helper.EmployeeHelper;
import com.example.rqchallenge.model.*;
import com.example.rqchallenge.repository.EmployeeRepository;
import com.example.rqchallenge.service.EmployeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.rqchallenge.constant.EmployeeConstant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class RqChallengeApplicationTests {

    private RestTemplate restTemplate;
    private EmployeeRepository employeeRepository;
    private EmployeeHelper employeeHelper;
    private EmployeeService employeeService;

    @BeforeEach
    public void setup() {
        // Create mock objects
        restTemplate = mock(RestTemplate.class);
        employeeRepository = mock(EmployeeRepository.class);

        // Inject mocked RestClientClass into mocked HelperClass
        employeeHelper = new EmployeeHelper(restTemplate,employeeRepository);

        // Inject mocked HelperClass into ServiceClass
        employeeService = new EmployeeService(employeeHelper);
    }

    public List<EmployeeModel> getMockListOfEmp(){
        return Arrays.asList(
            new EmployeeModel(1,"Dhiraj",4500,23,""),
            new EmployeeModel(2,"Suraj",5500,26,""),
            new EmployeeModel(3,"Rajesh",4100,22,""),
            new EmployeeModel(4,"Ramesh",4500,23,""),
            new EmployeeModel(5,"Rajendra",4101,32,""),
            new EmployeeModel(6,"Pavan",6600,31,""),
            new EmployeeModel(7,"Shivam",7700,30,""),
            new EmployeeModel(8,"Shivraj",2700,50,""),
            new EmployeeModel(9,"Viraj",6000,19,""),
            new EmployeeModel(10,"Siraj",6235,20,""),
            new EmployeeModel(11,"Virat",4511,24,""),
            new EmployeeModel(12,"Rohit",8400,23,""),
            new EmployeeModel(13,"Rishabh",900,25,"")
        );
    }



    private HttpEntity getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void testFetchAllEmployeeDataTest() {
        // Mock response for external API call
        List<EmployeeModel> mockEmployees = getMockListOfEmp();

        EmployeeListResponse employeeListResponse = new EmployeeListResponse();
        employeeListResponse.setData(mockEmployees);
        employeeListResponse.setStatus("success");
        ResponseEntity<EmployeeListResponse> mockResponse = ResponseEntity.ok(employeeListResponse);

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class)).thenReturn(mockResponse);
        List<Employee> employeeList =mockEmployees.stream().map(EmployeeModel::convertEmployeeModelToEmployee).collect(Collectors.toList());
        when(employeeRepository.saveAll(Mockito.<List<Employee>>any()))
                .thenReturn(employeeList);

        List<Employee> result = employeeService.getAllEmployees();

        // Verify results
        assertEquals(mockEmployees.size(), result.size());
        assertEquals("success",employeeListResponse.getStatus());
    }

    @Test
    public void testFetchAllEmployeeData_emptyResponse() {
        // Mock empty response from restTemplate
        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Collections.emptyList());
        ResponseEntity<EmployeeListResponse> mockResponseEntity = ResponseEntity.ok(mockResponse);
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        // Call the method under test and expect EmployeeDataNotFoundException
        EmployeeDataNotFoundException exception = assertThrows(EmployeeDataNotFoundException.class,
                () -> employeeService.getAllEmployees());

        // Verify the exception message or handle as needed
        assertEquals("Data Not Found", exception.getMessage());
    }

    @Test
    public void testFetchAllEmployeeData_tooManyRequest() {

        Employee employee = new Employee(1,"Dhiraj",23,4545,"");
        List<Employee> mockedResponse = Collections.singletonList(employee);

        // Mock HttpClientErrorException from restTemplate
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        when(employeeRepository.findAll()).thenReturn(mockedResponse);

        List<Employee> actualResult = employeeService.getAllEmployees();

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals(actualResult.size(),mockedResponse.size());
    }

    public ResponseEntity<EmployeeListResponse> getMockedResponseEntity(List<EmployeeModel> employeeModelList){

        EmployeeListResponse employeeListResponse = new EmployeeListResponse();
        employeeListResponse.setData(employeeModelList);
        return new ResponseEntity<>(employeeListResponse,HttpStatus.OK);
    }
    @Test
    public void filterEmpNameFromSearchStringTest() {
        String searchString = "raj";
        List<EmployeeModel> employeeModelList = getMockListOfEmp();
        ResponseEntity<EmployeeListResponse> mockResponseEntity = getMockedResponseEntity(employeeModelList);
        List<Employee> employeeList = employeeModelList.stream()
                .map(EmployeeModel::convertEmployeeModelToEmployee)
                .collect(Collectors.toList());

        List<Employee> filterListMockResponse = employeeList.stream()
                .filter(employee -> employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        // Mock response for external API call
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        List<Employee> actualResponse = employeeService.filterEmpNameFromSearchString(searchString);

        // Verify results
        assertEquals(filterListMockResponse.size(), actualResponse.size());
    }

    @Test
    public void filterEmpNameFromSearchStringTest_emptyResponse() {

        String searchString = "raj";
        // Mock empty response from restTemplate
        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Collections.emptyList());
        ResponseEntity<EmployeeListResponse> mockResponseEntity = ResponseEntity.ok(mockResponse);
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        // Call the and expect EmployeeDataNotFoundException
        EmployeeDataNotFoundException exception = assertThrows(EmployeeDataNotFoundException.class,
                () -> employeeService.filterEmpNameFromSearchString(searchString));

        // Verify the exception message or handle as needed
        assertEquals("Data Not Found", exception.getMessage());

    }

    @Test
    public void filterEmpNameFromSearchStringTest_GenericException() {

        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.filterEmpNameFromSearchString("raj"));
    }

    @Test
    public void getHighestSalaryOfEmployeesTest() {

        List<EmployeeModel> employeeModelList = getMockListOfEmp();
        ResponseEntity<EmployeeListResponse> mockResponseEntity = getMockedResponseEntity(employeeModelList);
        List<Employee> employeeList = employeeModelList.stream()
                .map(EmployeeModel::convertEmployeeModelToEmployee)
                .collect(Collectors.toList());

        Integer mockHighestSal = employeeList.stream().mapToInt(Employee::getSalary).max().getAsInt();

        // Mock response for external API call
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        Integer actualHighestSal = employeeService.getHighestSalaryOfEmployees();

        // Verify results
        assertEquals(mockHighestSal, actualHighestSal);
    }

    @Test
    public void getHighestSalaryOfEmployeesTest_emptyResponse() {

        // Mock empty response from restTemplate
        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Collections.emptyList());
        ResponseEntity<EmployeeListResponse> mockResponseEntity = ResponseEntity.ok(mockResponse);
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        // Call the method under test and expect EmployeeDataNotFoundException
        EmployeeDataNotFoundException exception = assertThrows(EmployeeDataNotFoundException.class,
                () -> employeeService.getHighestSalaryOfEmployees());

        // Verify the exception message or handle as needed
        assertEquals("Data Not Found", exception.getMessage());

    }

    @Test
    public void getHighestSalaryOfEmployeesTest_GenericException() {

        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.getHighestSalaryOfEmployees());
    }

    @Test
    public void getTopTenHighestEarningEmployeeNamesTest() {

        List<EmployeeModel> employeeModelList = getMockListOfEmp();
        ResponseEntity<EmployeeListResponse> mockResponseEntity = getMockedResponseEntity(employeeModelList);
        List<Employee> employeeList = employeeModelList.stream()
                .map(EmployeeModel::convertEmployeeModelToEmployee)
                .collect(Collectors.toList());


        List<String> mockEmpNameList = employeeList.stream()
                .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());

        // Mock response for external API call
        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);
        List<String> actualEmpNameList = employeeService.getTopTenHighestEarningEmployeeNames();

        // Verify results
        assertEquals(mockEmpNameList, actualEmpNameList);
    }

    @Test
    public void getTopTenHighestEarningEmployeeNamesTest_emptyResponse() {

        // Mock empty response from restTemplate
        EmployeeListResponse mockResponse = new EmployeeListResponse();
        mockResponse.setData(Collections.emptyList());
        ResponseEntity<EmployeeListResponse> mockResponseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenReturn(mockResponseEntity);

        // Call the method under test and expect EmployeeDataNotFoundException
        EmployeeDataNotFoundException exception = assertThrows(EmployeeDataNotFoundException.class,
                () -> employeeService.getTopTenHighestEarningEmployeeNames());

        // Verify the exception message or handle as needed
        assertEquals("Data Not Found", exception.getMessage());
    }

    @Test
    public void getTopTenHighestEarningEmployeeNamesTest_GenericException() {

        when(restTemplate.exchange(BASE_URL+FETCH_ALL_EMPLOYEES, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Test
    public void getEmployeeDetailsByIdTest(){

        EmployeeModel employeeModel = new EmployeeModel(1,"Dhiraj",4512,23,"");
        Employee mockedEmployee = EmployeeModel.convertEmployeeModelToEmployee(employeeModel);
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setData(employeeModel);
        employeeResponse.setStatus("success");
        ResponseEntity<EmployeeResponse> mockedResponse = new ResponseEntity(employeeResponse,HttpStatus.OK);

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+FETCH_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.GET,getEntity(), EmployeeResponse.class))
                .thenReturn(mockedResponse);

        Employee actualEmployee = employeeService.getEmployeeDetailsById("1");
        assertEquals(mockedEmployee,actualEmployee);
        assertEquals("success",employeeResponse.getStatus());
    }

    @Test
    public void getEmployeeDetailsByIdTest_numberFormatException() {

        NumberFormatException exception = assertThrows(NumberFormatException.class,
                () -> employeeService.getEmployeeDetailsById("dhiraj"));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Invalid data provided for id, please provide valid integer",exception.getMessage());
    }

    @Test
    public void getEmployeeDetailsByIdTest_illegalArgumentException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.getEmployeeDetailsById(""));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Data should not empty or null",exception.getMessage());
    }

    @Test
    public void getEmployeeDetailsByIdTest_emptyResponse() {

        ResponseEntity<EmployeeResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+FETCH_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.GET,getEntity(), EmployeeResponse.class))
                .thenReturn(mockedResponse);

        // Call the method under test and expect EmployeeDataNotFoundException
        EmployeeDataNotFoundException exception = assertThrows(EmployeeDataNotFoundException.class,
                () -> employeeService.getEmployeeDetailsById("1"));

        // Verify the exception message or handle as needed
        assertEquals("Data Not Found", exception.getMessage());
    }

    @Test
    public void getEmployeeDetailsByIdTest_tooManyRequest() {

        Employee mockedEmployee = new Employee(1,"Dhiraj",23,4545,"");

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+FETCH_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.GET,getEntity(), EmployeeResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockedEmployee));

        Employee actualResult = employeeService.getEmployeeDetailsById("1");

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals(actualResult,mockedEmployee);
    }

    @Test
    public void getEmployeeDetailsByIdTest_GenericException() {

        when(restTemplate.exchange(BASE_URL+FETCH_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.GET,getEntity(),EmployeeListResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.getEmployeeDetailsById("1"));
    }
    @Test
    public void deleteEmployeeDetailsByIdTest() {

        EmployeeDeleteResponse employeeDeleteResponse = new EmployeeDeleteResponse();
        employeeDeleteResponse.setMessage("successfully! deleted Record");
        employeeDeleteResponse.setStatus("success");
        ResponseEntity<EmployeeDeleteResponse> mockedResponse = new ResponseEntity(employeeDeleteResponse,HttpStatus.OK);

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+DELETE_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.DELETE,getEntity(),EmployeeDeleteResponse.class))
                .thenReturn(mockedResponse);
        String actualResponse = employeeService.deleteEmployeeById("1");

        assertEquals(actualResponse,"successfully! deleted Record");
        assertEquals("success",employeeDeleteResponse.getStatus());
    }

    @Test
    public void deleteEmployeeDetailsByIdTest_tooManyRequest() {

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+DELETE_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.DELETE,getEntity(),EmployeeDeleteResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> employeeService.deleteEmployeeById("1"));

        assertEquals(exception.getClass().getName(),"org.springframework.web.client.HttpClientErrorException");
    }

    @Test
    public void deleteEmployeeDetailsByIdTest_numberFormatException() {

        NumberFormatException exception = assertThrows(NumberFormatException.class,
                () -> employeeService.deleteEmployeeById("dhiraj"));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Invalid data provided for id, please provide valid integer",exception.getMessage());
    }


    @Test
    public void deleteEmployeeDetailsByIdTest_illegalArgumentException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.deleteEmployeeById(""));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Data should not empty or null",exception.getMessage());
    }

    @Test
    public void deleteEmployee_GenericException() {

        when(restTemplate.exchange(BASE_URL+DELETE_EMPLOYEE_DETAILS_BY_ID+1, HttpMethod.DELETE,getEntity(),EmployeeDeleteResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.deleteEmployeeById("1"));
    }

    @Test
    public void createEmployee() {
        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "4512");
            put("age", "23");
        }};
        Employee mockedEmployee = new Employee(1,"Dhiraj",4512,23,"");
        EmployeeCreateResponse employeeCreateResponse = new EmployeeCreateResponse();
        employeeCreateResponse.setData(mockedEmployee);
        employeeCreateResponse.setStatus("success");

        ResponseEntity<EmployeeCreateResponse> mockedResponse = new ResponseEntity(employeeCreateResponse, HttpStatus.OK);

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+CREATE_EMPLOYEE_RECORD, HttpMethod.POST,new HttpEntity<>(data), EmployeeCreateResponse.class))
                .thenReturn(mockedResponse);
        when(employeeRepository.save(mockedEmployee)).thenReturn(mockedEmployee);
        Employee actualEmployee = employeeService.createEmployee(data);

        assertEquals(actualEmployee,mockedEmployee);
        assertEquals("success",employeeCreateResponse.getStatus());

    }

    @Test
    public void createEmployee_illegalArgumentException() {

        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "4512");
        }};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(data));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Incomplete data provided, please provide name, age, salary",exception.getMessage());
    }

    @Test
    public void createEmployee_illegalArgumentException2() {

        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "4512");
            put("age","-45");
        }};
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(data));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Invalid data provided for field age and salary",exception.getMessage());
    }

    @Test
    public void createEmployee_numberFormatException() {

        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "dhiraj");
            put("age", "23");
        }};
        NumberFormatException exception = assertThrows(NumberFormatException.class,
                () -> employeeService.createEmployee(data));

        // Verify the behavior when HttpClientErrorException is thrown
        assertEquals("Invalid data provided for age and salary, please provide valid integer",exception.getMessage());
    }

    @Test
    public void testCreateEmployee_GenericException() {
        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "4545");
            put("age", "23");
        }};

        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+CREATE_EMPLOYEE_RECORD, HttpMethod.POST,new HttpEntity<>(data), EmployeeCreateResponse.class))
                .thenThrow(new RuntimeException("unknown Exception"));

        // Call method and assert
        assertThrows(RuntimeException.class, () -> employeeService.createEmployee(data));
    }

    @Test
    public void createEmployee_tooManyRequest() {

        Map<String, Object> data = new HashMap()
        {{
            put("name", "Dhiraj");
            put("salary", "4512");
            put("age", "23");
        }};
        //Mock the external API call
        when(restTemplate.exchange(BASE_URL+CREATE_EMPLOYEE_RECORD, HttpMethod.POST,new HttpEntity<>(data), EmployeeCreateResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> employeeService.createEmployee(data));

        assertEquals(exception.getClass().getName(),"org.springframework.web.client.HttpClientErrorException");
    }
}
