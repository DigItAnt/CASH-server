package it.cnr.ilc.lari.itant.belexo.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class BadFormatException extends RuntimeException {
    
}
