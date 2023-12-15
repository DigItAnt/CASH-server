package it.cnr.ilc.lari.itant.cash.om;

public class BiblioFilter {
    String key;
    String value;
    String op;

    public BiblioFilter(String key, String value, String op) {
        this.key = key;
        this.value = value;
        this.op = op;
    }
    
    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getOp() {
        return op;
    }

}
