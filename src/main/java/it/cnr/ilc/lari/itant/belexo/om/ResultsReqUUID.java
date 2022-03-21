package it.cnr.ilc.lari.itant.belexo.om;

// Bean di utilità per tutte le volte che serve results e requestUUID
public class ResultsReqUUID {
    String requestUUID;
    
    int results;

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

    
}
