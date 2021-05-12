package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddFolderRequest extends UserReqUUID {
    int elementId;

    @JsonProperty("element-id")
    public int getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }
    
}
