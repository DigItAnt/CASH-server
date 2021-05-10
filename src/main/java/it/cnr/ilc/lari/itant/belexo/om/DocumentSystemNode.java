package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;

import javax.validation.constraints.Pattern;

public class DocumentSystemNode {
    private String name;
    private String path;
    private List<DocumentSystemNode> children;
    private int element_id;

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

    
}
