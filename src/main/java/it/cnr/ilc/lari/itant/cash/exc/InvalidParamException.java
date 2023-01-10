package it.cnr.ilc.lari.itant.cash.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason="One of the parameters is invalid")
public class InvalidParamException extends RuntimeException {
    
}
