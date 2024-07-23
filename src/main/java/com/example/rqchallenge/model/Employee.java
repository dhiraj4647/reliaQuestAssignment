package com.example.rqchallenge.model;

import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "employee_details")
public class Employee {
    @Id
    Integer id;
    String name;
    Integer salary;
    Integer age;
    String profileImage;
}
