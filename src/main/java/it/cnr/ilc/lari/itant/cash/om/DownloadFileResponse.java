package it.cnr.ilc.lari.itant.cash.om;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownloadFileResponse extends ResponseEntity<byte []> {
    
    String requestUUID;

    public DownloadFileResponse(byte[] content, HttpHeaders headers) {
        super(content, headers, HttpStatus.OK);
    }

    @JsonProperty("response-status")
    int responseStatus;

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    

}
