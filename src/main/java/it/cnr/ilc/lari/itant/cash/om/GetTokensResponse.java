package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class GetTokensResponse extends ReqUUIDResponse {
    List<Token> tokens;

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }
    

}
