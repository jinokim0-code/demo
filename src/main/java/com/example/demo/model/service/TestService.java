package com.example.demo.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.TestDB;
import com.example.demo.model.repository.TestRepository;

@Service
public class TestService {
    @Autowired
    private TestRepository testRepository;

    // public TestDB findByName(String name) {
    // return (TestDB) testRepository.findByName(name);
    // }

    public List<TestDB> findAll() {
        return testRepository.findAll();
    }
}