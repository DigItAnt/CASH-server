package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.List;

public interface TextExtractorInterface {
    List<String> extract(InputStream content);
}