package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import it.cnr.ilc.lari.itant.belexo.exc.BadFormatException;
import it.cnr.ilc.lari.itant.belexo.om.Annotation;
import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo.TokenType;

public class TxtTextExtractor implements TextExtractorInterface {
    private String text;

    private List<TokenInfo> tokenList;

    public TxtTextExtractor() {
        tokenList = new ArrayList<TokenInfo>();
    }

    @Override
    public String extract() {
        return text;
    }

    @Override
    public List<Annotation> annotations() {
        return new ArrayList<Annotation>();
    }

    @Override
    public List<TokenInfo> tokens() {
        return tokenList;
    }

    @Override
    public Map<String, Object> metadata() {
        return new HashMap<String, Object>();
    }

    protected void populateTokens() {
        boolean inToken = false;
        int tstart = 0;
        for ( int i = 0; i < text.length(); i++ ) {
            if ( Character.isLetterOrDigit(text.charAt(i)) ) {
                if ( !inToken ) {
                    inToken = true;
                    tstart = i;
                }
            } else {
                if ( inToken ) {
                    inToken = false;
                    TokenInfo tinfo = new TokenInfo(text.substring(tstart, i), tstart, i, TokenType.WORD, null);
                    tokenList.add(tinfo);
                }
            }
        }
        if ( inToken ) {
            TokenInfo tinfo = new TokenInfo(text.substring(tstart, text.length()), tstart, text.length(), TokenType.WORD, null);
            tokenList.add(tinfo);
        }
    }

    @Override
    public TextExtractorInterface read(InputStream is) throws BadFormatException {
        try {
            this.text = IOUtils.toString(is, StandardCharsets.UTF_8);
            populateTokens();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new BadFormatException();
        }
        return this;
    }
}
