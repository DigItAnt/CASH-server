package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoteroCSVResponse {
    String requestUUID;
    int responseStatus = ResponseStatus.OK; // by default
    int numrecords = 0;

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    @JsonProperty("response-status")
    public int getResponseStatus() {
        return responseStatus;
    }

    @JsonProperty("response-status")
    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    @JsonProperty("num-records")
    public int getNumrecords() {
        return numrecords;
    }

    @JsonProperty("num-records")
    public void setNumrecords(int numrecords) {
        this.numrecords = numrecords;
    }
}
