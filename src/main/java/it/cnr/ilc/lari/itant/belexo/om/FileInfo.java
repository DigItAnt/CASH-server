package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileInfo {
    String path;
    String name;
    DocumentSystemNode.FileDirectory type;
    @JsonProperty("element-id")
    int elementId;
    Map<String, String> metadata;

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DocumentSystemNode.FileDirectory getType() {
        return type;
    }
    public void setType(DocumentSystemNode.FileDirectory type) {
        this.type = type;
    }
    public int getElementId() {
        return elementId;
    }
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }



}
