package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import it.cnr.ilc.lari.itant.belexo.om.Annotation;

public interface TextExtractorInterface {
    TextExtractorInterface read(InputStream is) throws BadFormatException;
    List<TokenInfo> tokens();
    List<Annotation> annotations();
    Map<String, Object> metadata();
    String extract();
}