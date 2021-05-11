package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;
import java.util.Map;

public class DocumentSystemNode {
    private String name;
    private String path;
    private List<DocumentSystemNode> children;
    private int element_id;
    private Map<String, String> metadata;
    // private String key_meta;  // TODO ripensare con value_meta, vedi doc

    static public enum FileFolder {
        file, folder
    }

    private FileFolder type;
    
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

    public FileFolder getType() {
        return type;
    }
    public void setType(FileFolder type) {
        this.type = type;
    }
    public List<DocumentSystemNode> getChildren() {
        return children;
    }
    public void setChildren(List<DocumentSystemNode> children) {
        this.children = children;
    }

    public int getElement_id() {
        return element_id;
    }
    public void setElement_id(int element_id) {
        this.element_id = element_id;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    
}
