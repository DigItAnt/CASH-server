package it.cnr.ilc.lari.itant.cash.om;

import java.util.ArrayList;
import java.util.List;

public class BiblioResult {
    static class BiblioEntry {
        public String key;
        public String value;


    }

    String recordKey;

    List<BiblioEntry> params = new ArrayList<>();

    public BiblioResult(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void add(String key, String value) {
        BiblioEntry entry = new BiblioEntry();
        entry.key = key;
        entry.value = value;
        params.add(entry);
    }

    public List<BiblioEntry> getParams() {
        return params;
    }

}
