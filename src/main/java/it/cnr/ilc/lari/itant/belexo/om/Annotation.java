package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.DBManager;

public class Annotation {
    private static final Logger log = LoggerFactory.getLogger(Annotation.class);
    long ID = -1;
    String layer;
    String value;
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

    //@JsonSerialize(using = MetadataSerializer.class)
    public Map<String, Object> getAttributes() {
        if ( attributes == null ) {
            // populate it!
            try {
                this.attributes = DBManager.getAnnotationAttributes(this.ID);
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
    
    public List<Span> getSpans() {
        return spans;
    }
    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }
    
    public boolean spansOverlap() {
        return false; // @TODO implement this
    }    
}
