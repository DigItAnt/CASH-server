package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Map;

public class UpdateMetadataRequest extends AddFolderRequest {
    Map<String, Object> metadata;

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    
}
