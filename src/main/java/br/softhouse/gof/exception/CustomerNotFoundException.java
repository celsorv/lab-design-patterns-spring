package br.softhouse.gof.exception;

public class CustomerNotFoundException extends EntityNotFoundException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Long id) {
        this(String.format("There is no customer with id %d", id));
    }

}
