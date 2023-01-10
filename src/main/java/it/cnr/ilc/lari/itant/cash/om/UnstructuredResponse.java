package it.cnr.ilc.lari.itant.cash.om;

import java.util.Map;

public class UnstructuredResponse extends ReqUUIDResponse {
    long nodeid;
    Map<String, Long> unstructuredids;

    public long getNodeid() {
        return nodeid;
    }
    public void setNodeid(long nodeid) {
        this.nodeid = nodeid;
    }
    public Map<String, Long> getUnstructuredids() {
        return unstructuredids;
    }
    public void setUnstructuredids(Map<String, Long> unstructuredids) {
        this.unstructuredids = unstructuredids;
    }
    
}
