package it.cnr.ilc.lari.itant.cash.om;

public class GetTextResponse extends ReqUUIDResponse {
    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
