package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownloadFileResponse {
    
    String requestUUID;

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
