package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class SearchFilesResponse {
    String requestUUID;
    int results;
    
    List<FileInfo> files;
    public String getRequestUUID() {
        return requestUUID;
    }
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }
    public int getResults() {
        return results;
    }
    public void setResults(int results) {
        this.results = results;
    }
    public List<FileInfo> getFiles() {
        return files;
    }
    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

}