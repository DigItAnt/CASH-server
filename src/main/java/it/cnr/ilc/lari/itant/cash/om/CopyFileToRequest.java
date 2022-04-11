package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CopyFileToRequest extends RemoveFileRequest {
    @JsonProperty("target-id")
    int targetId;

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
}
