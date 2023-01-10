package it.cnr.ilc.lari.itant.cash.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import it.cnr.ilc.lari.itant.cash.exc.BadFormatException;
import it.cnr.ilc.lari.itant.cash.om.Annotation;

public interface TextExtractorInterface {
    TextExtractorInterface read(InputStream is) throws BadFormatException;
    List<TokenInfo> tokens();
    List<Annotation> annotations();
    Map<String, Object> metadata();
    String extract();
    String getTextType();
}