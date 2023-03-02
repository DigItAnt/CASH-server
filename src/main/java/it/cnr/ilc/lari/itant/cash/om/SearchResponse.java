package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class SearchResponse {
    String requestUUID;
    
    List<SearchRow> rows;
    public String getRequestUUID() {
        return requestUUID;
    }
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public List<SearchRow> getRows() {
        return rows;
    }

    public void setRows(List<SearchRow> rows) {
        this.rows = rows;
    }
    
    public void addRow(SearchRow row) {
        this.rows.add(row);
    }
}