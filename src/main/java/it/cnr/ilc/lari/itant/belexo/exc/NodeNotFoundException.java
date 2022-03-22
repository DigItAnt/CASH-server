package it.cnr.ilc.lari.itant.belexo.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="The specified node is not found")
public class NodeNotFoundException extends RuntimeException {

}
