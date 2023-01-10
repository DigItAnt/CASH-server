package it.cnr.ilc.lari.itant.cash.om;

public class RemoveFileResponse {
    String requestUUID;
    int responseStatus = ResponseStatus.OK; // by default 

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
