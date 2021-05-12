package it.cnr.ilc.lari.itant.belexo.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RenameFileRequest extends RemoveFileRequest {
    @JsonProperty("rename-string")
    String renameString;

    public String getRenameString() {
        return renameString;
    }

    public void setRenameString(String renameString) {
        this.renameString = renameString;
    }

    
}
