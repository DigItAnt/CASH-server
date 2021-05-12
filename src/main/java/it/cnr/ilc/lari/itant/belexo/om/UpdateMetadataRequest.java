package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Map;

public class UpdateMetadataRequest extends AddFolderRequest {
    Map<String, String> metadata;

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    
}
