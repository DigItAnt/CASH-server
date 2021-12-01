package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EpiDocTextExtractor implements TextExtractorInterface {
    private static final Logger log = LoggerFactory.getLogger(EpiDocTextExtractor.class);
    List<String> tokenList;

    public EpiDocTextExtractor() {
        tokenList = new ArrayList<String>();
    }

    @Override
    public String extract() {
        // Non considera newlines etc.
        return String.join(" ", tokenList);
    }

    @Override
    public List<String> tokens() {
        return tokenList;
    }

    @Override
    public TextExtractorInterface read(InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList list = doc.getElementsByTagName("tei:ab"); // ???
            log.info("NLINES: " + list.getLength());
            for ( int nl = 0; nl < list.getLength(); nl++ ) {
                Node lineNode = list.item(nl);
                NodeList lc = lineNode.getChildNodes();
                for ( int ni = 0; ni < lc.getLength(); ni ++ ) {
                    Node item = lc.item(ni);
                    String name = item.getNodeName();
                    switch ( name ) {
                        case "tei:name":
                        case "tei:w":
                        String token = item.getTextContent();
                        token = token.replaceAll("\\s+", ""); // TODO: NOOOOOOO!!!!
                        log.info("Found token: "+ token);
                        tokenList.add(token);
                    }
                }
            }
            log.info("TOKENS: " + extract());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return this;
    }



    public static void main(String[] args) {
        for (String token: new EpiDocTextExtractor().read(null).tokens() )
            System.out.println(token);
    }
}
