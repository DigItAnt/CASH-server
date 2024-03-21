package it.cnr.ilc.lari.itant.cash.om;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import it.cnr.ilc.lari.itant.cash.DBManager;

public class Annotation {
    private static final Logger log = LoggerFactory.getLogger(Annotation.class);
    long ID = -1;
    String layer;
    String value;
    long node;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    boolean imported = false;
    Map<String, Object> attributes;

    public static class Span {
        int start;
        int end;
        public int getStart() {
            return start;
        }
        public void setStart(int start) {
            this.start = start;
        }
        public int getEnd() {
            return end;
        }
        public void setEnd(int end) {
            this.end = end;
        }
        
    }
    
    public long getNode() {
        return node;
    }
    public void setNode(long node) {
        this.node = node;
    }

    List<Span> spans;
    
    public long getID() {
        return ID;
    }
    public void setID(long iD) {
        ID = iD;
    }

    public String getLayer() {
        return layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public void setImported(boolean imp) {
        imported = imp;
    }

    public boolean getImported() { return imported; }

    //@JsonSerialize(using = MetadataSerializer.class)
    public Map<String, Object> getAttributes() {
        if ( attributes == null ) {
            // populate it!
            try {
                this.attributes = DBManager.getAnnotationAttributes(this.ID, null);
            } catch (Exception e) {
                log.error("Could not fetch attributes for annotation " + this.ID, e);
            }
        }
        return attributes;
    }

    //@JsonDeserialize(using = MetadataDeserializer.class)
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public void attributesFromNodeMap(NamedNodeMap nmap) {
        this.attributes = new HashMap<String, Object>();
        for ( int i=0; i<nmap.getLength(); i++ ) {
            Node attr = nmap.item(i);
            this.attributes.put(attr.getNodeName(), attr.getNodeValue());
        }
    }

    public List<Span> getSpans() {
        if ( spans == null ) {
            // populate it!
            try {
                this.spans = DBManager.getAnnotationSpans(this.ID, null);
            } catch (Exception e) {
                log.error("Could not fetch spans for annotation " + this.ID, e);
            }
        }
        return spans;
    }
    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }
    
    public void addSpan(Span span) {
        if ( spans == null ) {
            spans = new ArrayList<Span>();
        }

        spans.add(span);
    }

    public boolean spansOverlap() {
        return false; // @TODO implement this
    } 

    protected Span firstSpan() {
        if ( spans.size() > 0 ) return spans.get(0);
        Span ret = new Span();
        ret.setStart(-1);
        ret.setEnd(-1);
        return ret;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer("ANN: ");
        ret.append(layer).append("#").append(value);
        if ( this.spans != null )
            ret.append("(@").append(firstSpan().start).append("-").append(firstSpan().end).append(",");
        for (String k: getAttributes().keySet()) {
            ret.append(k).append("=").append(getAttributes().get(k)).append(",");
        }
        if ( this.spans != null ) ret.append(")");
        return ret.toString();
    }
}
