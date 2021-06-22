package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddFolderResponse {
   String requestUUID;
   int responseStatus = ResponseStatus.OK; // by default
   private List<DocumentSystemNode> documentSystem;


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
public List<DocumentSystemNode> getDocumentSystem() {
    return documentSystem;
}
public void setDocumentSystem(List<DocumentSystemNode> documentSystem) {
    this.documentSystem = documentSystem;
}
   
   
}
