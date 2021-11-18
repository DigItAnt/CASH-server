package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddFolderResponse {
   String requestUUID;
   int responseStatus = ResponseStatus.OK; // by default
   private DocumentSystemNode node;


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
public DocumentSystemNode getNode() {
    return node;
}
public void setNode(DocumentSystemNode documentSystem) {
    this.node = documentSystem;
}
   
   
}
