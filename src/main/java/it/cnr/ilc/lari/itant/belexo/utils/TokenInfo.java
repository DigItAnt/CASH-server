package it.cnr.ilc.lari.itant.belexo.utils;

import java.util.ArrayList;
import java.util.List;

public class TokenInfo {
    public enum TokenType { WORD, PUNCT };

    public String text;
    public TokenType tokenType;
    public int begin;
    public int end;
    public String xmlid;

    public TokenInfo(String t, int b, int e, TokenType ttype, String xmlid) {
        text = t;
        begin = b;
        end = e;
        tokenType = ttype;
        this.xmlid = xmlid;
    }

    public TokenInfo(String t) {
        text = t;
        begin = 0;
        end = 0;
        tokenType = TokenType.WORD;
    }

    public static List<String> allTokens(List<TokenInfo> tokenList) {
        List<String> ret = new ArrayList<>();
        for ( TokenInfo tinfo: tokenList ) ret.add(tinfo.text);
        return ret;
    }

    public static List<String> allWordTokens(List<TokenInfo> tokenList) {
        List<String> ret = new ArrayList<>();
        for ( TokenInfo tinfo: tokenList )
            if ( tinfo.tokenType == TokenType.WORD ) ret.add(tinfo.text);
        return ret;
    }

};
