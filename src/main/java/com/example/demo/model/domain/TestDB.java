package com.example.demo.model.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "testdb")
@Data
public class TestDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private Integer age;

    @Column(nullable = true)
    private String gender;
}