package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;

public class GetDocumentSystemResponse extends ResultsReqUUID {
    private List<DocumentSystemNode> documentSystem;

    public List<DocumentSystemNode> getDocumentSystem() {
        return documentSystem;
    }
    public void setDocumentSystem(List<DocumentSystemNode> documentSystem) {
        this.documentSystem = documentSystem;
    }
}
