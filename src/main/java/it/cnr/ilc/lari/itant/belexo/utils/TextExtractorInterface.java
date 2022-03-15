package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.List;

import it.cnr.ilc.lari.itant.belexo.om.Annotation;

public interface TextExtractorInterface {
    TextExtractorInterface read(InputStream is);
    List<TokenInfo> tokens();
    List<Annotation> annotations();
    String extract();
}