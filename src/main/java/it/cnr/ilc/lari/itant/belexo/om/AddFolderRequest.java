package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddFolderRequest {
    String requestUUID;
    int userId;
    int elementId;

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    @JsonProperty("user-id")
    public int getUserId() {
        return userId;
    }

    @JsonProperty("user-id")
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JsonProperty("element-id")
    public int getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }
    
}
