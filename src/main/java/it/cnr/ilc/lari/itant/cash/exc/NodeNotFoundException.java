package it.cnr.ilc.lari.itant.cash.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="The specified node is not found")
public class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException() {
        super("The specified node does not exist");
    }

    public NodeNotFoundException(String message) {
        super(message);
    }
}
