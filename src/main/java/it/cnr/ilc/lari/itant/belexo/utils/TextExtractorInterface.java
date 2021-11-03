package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;

public interface TextExtractorInterface {
    List<String> extract(Node node);

    List<String> extract(InputStream content);
}