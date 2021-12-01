package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.List;

public interface TextExtractorInterface {
    TextExtractorInterface read(InputStream is);
    List<String> tokens();
    String extract();
}