package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DocumentSystemNode {
    private String name;
    private String path;
    private List<DocumentSystemNode> children;
    private int elementId;
    private Map<String, String> metadata;
    // private String key_meta;  // TODO ripensare con value_meta, vedi doc

    static public enum FileDirectory {
        file, directory
    }

    private FileDirectory type;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public FileDirectory getType() {
        return type;
    }
    public void setType(FileDirectory type) {
        this.type = type;
    }
    public List<DocumentSystemNode> getChildren() {
        return children;
    }
    public void setChildren(List<DocumentSystemNode> children) {
        this.children = children;
    }

    @JsonProperty("element-id")
    public int getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(int elementId) {
        this.elementId = elementId;
    }
    
    //@JsonSerialize(using = MetadataSerializer.class)
    public Map<String, String> getMetadata() {
        return metadata;
    }

    //@JsonDeserialize(using = MetadataDeserializer.class)
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    
}
