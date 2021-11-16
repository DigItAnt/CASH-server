package it.cnr.ilc.lari.itant.belexo.om;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.JcrManager;

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

    public DocumentSystemNode(Node node) throws Exception {
        this(node, false);
    }

    // Constructor from node.
    public DocumentSystemNode(Node node, boolean recur) throws Exception {
        this.name = node.getName();
        this.elementId = node.getProperty(JcrManager.MYID).getLong();
        this.type = node.getProperty(JcrManager.MYTYPE).getString().equals(JcrManager.TYPE_FILE)?FileDirectory.file:FileDirectory.directory;
        this.children = new ArrayList<DocumentSystemNode>();
        // WTF is path??
        this.path = ""; // TODO
        this.metadata = new HashMap<String, String>();
        PropertyIterator pit = node.getProperties(JcrManager.META_PFIX + "*");
        while ( pit.hasNext() ) {
            Property p = pit.nextProperty();
            this.metadata.put(p.getName().substring(JcrManager.META_PFIX.length()), p.getString());
        }
        if ( recur ) recurChildren(this, node);
    }

    protected static void recurChildren(DocumentSystemNode dsn, Node node) throws Exception {
        log.info("Recurring over children of " + node.getName());
        NodeIterator nit = node.getNodes();
        while ( nit.hasNext() ) {
            Node child = nit.nextNode();
            log.info("Child node " + child.getName());
            if ( !child.hasProperty(JcrManager.MYTYPE) ) continue;
            String nodeType = child.getProperty(JcrManager.MYTYPE).getString();
            if (!nodeType.equals(JcrManager.TYPE_FILE) && !nodeType.equals(JcrManager.TYPE_FOLDER)) continue;
            DocumentSystemNode childdsn = new DocumentSystemNode(child, true);
            dsn.children.add(childdsn);
        }
    }

    public static List<DocumentSystemNode> empty() {
        return new ArrayList<DocumentSystemNode>();
    }

    public static List<DocumentSystemNode> populateTree() throws Exception {
        log.info("Populating Tree");
        ArrayList<DocumentSystemNode> toret = new ArrayList<DocumentSystemNode>();
        Session session = JcrManager.getSession();
        Node root = session.getRootNode();
        NodeIterator nit = root.getNodes();
        log.info("Iterating over nodes.");
        while ( nit.hasNext() ) {
            Node node = nit.nextNode();
            log.info("Node "  + node.getPath());
            //JcrManager.logProperties(node);
            if ( !node.hasProperty(JcrManager.MYTYPE) ) continue;
            String nodeType = node.getProperty(JcrManager.MYTYPE).getString();
            if (!nodeType.equals(JcrManager.TYPE_FILE) && !nodeType.equals(JcrManager.TYPE_FOLDER)) continue;
            DocumentSystemNode dsn = new DocumentSystemNode(node, true);
            log.info("Adding " + dsn.getName());
            toret.add(dsn);
        }
        return toret;
    }
}
