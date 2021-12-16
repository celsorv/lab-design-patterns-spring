package br.softhouse.gof.exception;

public class EntityInUseException extends BusinessException {

    public EntityInUseException(String mensagem) {
        super(mensagem);
    }
}
