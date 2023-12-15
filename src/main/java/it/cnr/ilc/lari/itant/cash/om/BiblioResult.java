package it.cnr.ilc.lari.itant.cash.om;

import java.util.HashMap;
import java.util.Map;

public class BiblioResult {

    String recordKey;

    Map<String, String> params = new HashMap<>();

    public BiblioResult(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void add(String key, String value) {
        params.put(key, value);
    }

    public Map<String, String> getParams() {
        return params;
    }

}
