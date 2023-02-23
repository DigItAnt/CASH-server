package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class SearchRow {

    long nodeId;
    String nodePath;

    List<TokenRef> tokens;
    public long getNodeId() {
        return nodeId;
    }
    public List<TokenRef> getTokens() {
        return tokens;
    }
    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }
    public void setTokens(List<TokenRef> tokens) {
        this.tokens = tokens;
    }

    public void addTokenRef(TokenRef tokenRef) {
        this.tokens.add(tokenRef);
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }
}
