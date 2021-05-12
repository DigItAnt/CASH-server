package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadFileRequest extends UserReqUUID {
    String path;
    
    @JsonProperty("file-name")
    String fileName;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
