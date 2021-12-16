package br.softhouse.gof.repository;

import br.softhouse.gof.model.CustomerAddress;
import org.springframework.data.repository.CrudRepository;

public interface CustomerAddressRepository extends CrudRepository<CustomerAddress, String> {
}
