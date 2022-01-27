package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Map;

public class Annotation {
    int ID;
    String layer;
    String value;
    Map<String, String> attributes;
    
    public int getID() {
        return ID;
    }
    public void setID(int iD) {
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
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    
}
