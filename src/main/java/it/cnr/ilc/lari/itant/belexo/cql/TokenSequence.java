package it.cnr.ilc.lari.itant.belexo.cql;

import java.util.ArrayList;
import java.util.List;

public class TokenSequence {
    List<Integer> tokenList = new ArrayList<>();

    public TokenSequence() {
        
    }

    public void addToken(int token) {
        tokenList.add(token);
    }

    public String buildFromString() {
        if (this.tokenList.size() == 0) {
            return "";
        }
        // build "tokFIRSTID.id, tokFIRSTID.begin, tokFIRSTID.end" using String.format
        String toret = String.format("tok%d.id, tok%d.begin, tok%d.end", tokenList.get(0), tokenList.get(0), tokenList.get(0));
        for (int i = 1; i < tokenList.size(); i++)
            toret += String.format(", tok%d.id, tok%d.begin, tok%d.end", tokenList.get(i), tokenList.get(i), tokenList.get(i));
        return toret;
    }

    public String buildChainString() {
        if (this.tokenList.size() < 2) {
            return "";
        }
        // tok2.position = tok1.position + 1  -- for each pair of consecutive tokens
        String toret = "(";
        for (int i = 0; i < tokenList.size() - 1; i++) {
            toret += String.format("tok%d.position = tok%d.position + 1 AND ", tokenList.get(i + 1), tokenList.get(i));
        }
        // remove last AND
        toret = toret.substring(0, toret.length() - 5);
        toret += ")";
        return toret;
    }

}
