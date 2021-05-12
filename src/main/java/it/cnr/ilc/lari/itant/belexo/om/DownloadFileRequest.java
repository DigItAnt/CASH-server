package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownloadFileRequest extends UserReqUUID {
    
    @JsonProperty("full-path")
    String fullPath;

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

}
