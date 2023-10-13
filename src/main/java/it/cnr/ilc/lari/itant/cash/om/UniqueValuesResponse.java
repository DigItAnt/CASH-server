package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class UniqueValuesResponse {
    String requestUUID;
    
    List<String> values;
    public String getRequestUUID() {
        return requestUUID;
    }
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }

}