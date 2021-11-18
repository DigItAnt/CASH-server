package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.DBManager;
import it.cnr.ilc.lari.itant.belexo.om.DocumentSystemNode.FileDirectory;

public class FileInfo {
    private static final Logger log = LoggerFactory.getLogger(DocumentSystemNode.class);
    String path;
    String name;
    DocumentSystemNode.FileDirectory type;

    @JsonIgnore
    Date created;
    
    @JsonIgnore
    Date modified;

    @JsonProperty("element-id")
    long elementId;
    Map<String, String> metadata;

    @JsonIgnore
    Map<String, Object> internalProperties;

    @JsonIgnore
    long father = DBManager.NO_FATHER; // elementid of the father node

    public String getPath() {
        log.info("Getting path for " + name);
        try {
            if ( path == null ) {
                this.path = DBManager.getNodePath(this.elementId);
            }
        } catch ( Exception e ) {
            log.error("Error recreating path for file", e);
        }
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

    @JsonIgnore
    public void setType(String type) {
        this.type = type.equals("F")?FileDirectory.file:FileDirectory.directory;
    }

    @JsonIgnore
    public String getTypeS() {
        return this.type==FileDirectory.file?DBManager.TYPE_FILE:DBManager.TYPE_FOLDER;
    }

    public long getElementId() {
        return elementId;
    }
    public void setElementId(long elementId) {
        this.elementId = elementId;
    }

    //@JsonSerialize(using = MetadataSerializer.class)
    public Map<String, String> getMetadata() {
        if ( metadata == null ) {
            // populate it!
            try {
                this.metadata = DBManager.getNodeMetadata(this.elementId);
            } catch (Exception e) {
                log.error("Could not fetch metadata for node " + this.name);
            }
        }
        return metadata;
    }

    //@JsonDeserialize(using = MetadataDeserializer.class)
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @JsonIgnore
    public void setInternalProperties(Map<String, Object> props) {
        this.internalProperties = props;
    }

    @JsonIgnore
    public void setFather(long f) {
        this.father = f;
    }

    @JsonIgnore
    public long getFather() {
        return this.father;
    }

    @JsonIgnore
    public boolean hasAncestor(long nodeId) {
        if ( nodeId == this.elementId ) return true;
        if ( this.father == DBManager.NO_FATHER ) return false;
        if ( nodeId == this.father ) return true;
        try {
            FileInfo fNode = DBManager.getNodeById(father);
            return fNode.hasAncestor(nodeId);
        } catch ( Exception e ) {
            return false;
        }
    }

    @JsonIgnore
    public String printAll() {
        // print this node
        StringBuffer sb = new StringBuffer("");
        sb.append(this.elementId + " "  + this.name + "\n");
        for (String k: this.getMetadata().keySet() ) {
            sb.append(k + ": " + this.getMetadata().get(k) + "\n");
        }
        return sb.toString();
    }
}
