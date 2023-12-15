package it.cnr.ilc.lari.itant.cash.om;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiblioResponse extends ReqUUIDResponse {
    List<BiblioResult> results = new java.util.ArrayList<>();
    BiblioResult current;
    Set<String> recordKeys = new HashSet<>();

    public BiblioResponse() {    
    }

    public void add(String fileid, String key, String value) {
        if (!this.recordKeys.contains(fileid)) {
            current = new BiblioResult(fileid);
            results.add(current);
            this.recordKeys.add(fileid);
        }
        current.add(key, value);
    }

    public List<BiblioResult> getResults() {
        return results;
    }
}
