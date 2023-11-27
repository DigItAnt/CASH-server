package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataRefreshStatus {

    public class Status {
        public long elementId;
        public String status; // OK or KO
    }

    private String requestUUID;
    private Integer results;
    private List<Status> statuses;

    public Integer getResults() {
        return results;
    }
    public void setResults(Integer results) {
        this.results = results;
    }

    public String getRequestUUID() {
        return requestUUID;
    }
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public List<Status> getStatuses() {
        return statuses;
    }
}
