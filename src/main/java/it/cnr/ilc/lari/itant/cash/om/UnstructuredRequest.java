package it.cnr.ilc.lari.itant.cash.om;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnstructuredRequest {
    Map<String, String> unstructured;
    long elementId;

    @JsonProperty("element-id")
    public long getElementId() {
        return elementId;
    }

    @JsonProperty("element-id")
    public void setElementId(long elementId) {
        this.elementId = elementId;
    }
    
    public Map<String, String> getUnstructured() {
        return unstructured;
    }

    public void setUnstructured(Map<String, String> unstructured) {
        this.unstructured = unstructured;
    }
}
