package it.cnr.ilc.lari.itant.cash.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class InvalidParamException extends RuntimeException {
    public InvalidParamException() {
        super("One of the parameters is invalid");
    }
    
    public InvalidParamException(String message) {
        super(message);
    }

}
