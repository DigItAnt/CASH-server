package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddFolderRequest extends UserReqUUID {
    long elementId;

    @JsonProperty("element-id")
    public long getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(long elementId) {
        this.elementId = elementId;
    }
    
}
