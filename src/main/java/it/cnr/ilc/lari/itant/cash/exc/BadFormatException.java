package it.cnr.ilc.lari.itant.cash.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class BadFormatException extends RuntimeException {
    public BadFormatException() {
        super("Invalid format");
    }

    public BadFormatException(String message) {
        super(message);
    }
}
