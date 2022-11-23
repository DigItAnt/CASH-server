package it.cnr.ilc.lari.itant.cash.om;

public class CreateTokenResponse extends ReqUUIDResponse {
    Token token;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
