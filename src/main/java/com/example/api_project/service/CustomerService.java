package com.example.api_project.service;


import com.example.api_project.entity.Customer;
import com.example.api_project.exception.ResourceNotFoundException;
import com.example.api_project.repository.CustomerRepository;
import com.example.api_project.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogger auditLogger;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Transactional
    public Customer create(Customer customer) {
        Customer saved = customerRepository.save(customer);
        auditLogger.log("CREATE", "Customer", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public Customer update(Long id, Customer updated) {
        Customer customer = findById(id);
        customer.setName(updated.getName());
        customer.setPhone(updated.getPhone());
        customer.setEmail(updated.getEmail());
        customer.setAddress(updated.getAddress());
        Customer saved = customerRepository.save(customer);
        auditLogger.log("UPDATE", "Customer", saved.getId(), saved.getName());
        return saved;
    }


    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
        auditLogger.log("DELETE", "Customer", id, customer.getName());
    }
}


