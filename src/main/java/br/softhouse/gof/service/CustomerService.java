package br.softhouse.gof.service;

import br.softhouse.gof.model.Customer;

import java.util.List;

public interface CustomerService {

    List<Customer> getAll();
    Customer getById(Long id);
    Customer insert(Customer customer);
    Customer update(Long id, Customer customer);
    void delete(Long id);

}
