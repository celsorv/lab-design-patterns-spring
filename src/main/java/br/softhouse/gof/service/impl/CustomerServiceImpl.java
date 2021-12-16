package br.softhouse.gof.service.impl;

import br.softhouse.gof.exception.CustomerNotFoundException;
import br.softhouse.gof.exception.EntityInUseException;
import br.softhouse.gof.model.Customer;
import br.softhouse.gof.model.CustomerAddress;
import br.softhouse.gof.repository.CustomerAddressRepository;
import br.softhouse.gof.repository.CustomerRepository;
import br.softhouse.gof.service.CustomerService;
import br.softhouse.gof.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String MSG_ENTITY_IN_USE = "Customer id %d in use, cannot be removed";

    // TODO Singleton: inject Spring components with @Autowired.

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Autowired
    private ViaCepService viaCepService;

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional
    public Customer insert(Customer customer) {
        return saveCustomerWithCep(customer);
    }

    @Override
    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer customerDB = getById(id);
        return saveCustomerWithCep(customer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            customerRepository.deleteById(id);

        } catch (EmptyResultDataAccessException e) {
            throw new CustomerNotFoundException(id);

        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException(String.format(MSG_ENTITY_IN_USE, id));

        }
    }

    private Customer saveCustomerWithCep(Customer customer) {

        // check if the customer's address already exists (by zip code)
        final String cep = customer.getCustomerAddress().getCep();
        CustomerAddress address = addressRepository.findById(cep)
                .orElseGet(() -> {

                    // persists the address from ViaCep data
                    CustomerAddress newAddress = viaCepService.queryCep(cep);
                    return addressRepository.save(newAddress);

                });

        // include customer with address data
        customer.setCustomerAddress(address);
        return customerRepository.save(customer);
    }

}
