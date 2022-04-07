package it.cnr.ilc.lari.itant.cash.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason="The file is not in the proper format")
public class BadFormatException extends RuntimeException {
    
}
