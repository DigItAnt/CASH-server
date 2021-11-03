package it.cnr.ilc.lari.itant.belexo.utils;

import java.util.List;

import javax.jcr.Node;

public interface TextExtractorInterface {
    List<String> extract(Node node);
}
