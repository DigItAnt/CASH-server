package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class CountFilesResponse {
    String requestUUID;
    int results;
    
    public int getResults() {
        return results;
    }
    public void setResults(int results) {
        this.results = results;
    }
}