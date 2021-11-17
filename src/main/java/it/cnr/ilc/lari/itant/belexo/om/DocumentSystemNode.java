package it.cnr.ilc.lari.itant.belexo.om;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.DBManager;

public class DocumentSystemNode {
    private static final Logger log = LoggerFactory.getLogger(DocumentSystemNode.class);
    private String name;
    private String path;
    private List<DocumentSystemNode> children;
    private long elementId;
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
    public long getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(long elementId) {
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

    // Constructor from FileInfo.
    public DocumentSystemNode(FileInfo node, boolean recur) throws Exception {
        this.name = node.getName();
        this.elementId = node.getElementId();
        this.type = node.getType();
        this.children = new ArrayList<DocumentSystemNode>();
        // WTF is path??
        this.path = ""; // TODO
        this.metadata = new HashMap<String, String>();
        this.metadata = node.getMetadata();
        if ( recur ) recurChildren(this, node);
    }

    protected static void recurChildren(DocumentSystemNode dsn, FileInfo node) throws Exception {
        log.info("Recurring over children of " + node.getName());
        for ( FileInfo child: DBManager.getNodeChildren(node.getElementId()) ) {
            log.info("Child node " + child.getName());
            DocumentSystemNode childdsn = new DocumentSystemNode(child, true);
            dsn.children.add(childdsn);
        }
    }

    public static List<DocumentSystemNode> empty() {
        return new ArrayList<DocumentSystemNode>();
    }

    public static List<DocumentSystemNode> populateTree(long root) throws Exception {
        log.info("Populating Tree");
        ArrayList<DocumentSystemNode> toret = new ArrayList<DocumentSystemNode>();
        List<FileInfo> children = DBManager.getNodeChildren(root);
        log.info("Iterating over nodes.");
        for ( FileInfo node: children ) {;
            log.info("Node "  + node.getName());
            DocumentSystemNode dsn = new DocumentSystemNode(node, true);
            log.info("Adding " + dsn.getName());
            toret.add(dsn);
        }
        return toret;
    }

}
