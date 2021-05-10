package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;

public class GetDocumentSystemOutput {
    private String requestUUID;
    private Integer results;
    private List<DocumentSystemNode> documentSystem;


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
    public List<DocumentSystemNode> getDocumentSystem() {
        return documentSystem;
    }
    public void setDocumentSystem(List<DocumentSystemNode> documentSystem) {
        this.documentSystem = documentSystem;
    }
}
