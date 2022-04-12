package it.cnr.ilc.lari.itant.cash.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import it.cnr.ilc.lari.itant.cash.exc.BadFormatException;
import it.cnr.ilc.lari.itant.cash.om.Annotation;

public class NullTextExtractor implements TextExtractorInterface {
    public NullTextExtractor() {
    }

    public String getTextType() { return "plain"; }

    @Override
    public String extract() {
        return "";
    }

    @Override
    public List<Annotation> annotations() {
        return new ArrayList<Annotation>();
    }

    @Override
    public List<TokenInfo> tokens() {
        return new ArrayList<TokenInfo>();
    }

    @Override
    public Map<String, Object> metadata() {
        return new HashMap<String, Object>();
    }

    @Override
    public TextExtractorInterface read(InputStream is) throws BadFormatException {
        return this;
    }
}
