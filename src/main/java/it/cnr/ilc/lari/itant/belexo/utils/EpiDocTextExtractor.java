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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo.TokenType;

public class EpiDocTextExtractor implements TextExtractorInterface {

    private static final Logger log = LoggerFactory.getLogger(EpiDocTextExtractor.class);
    List<TokenInfo> tokenList;

    public EpiDocTextExtractor() {
        tokenList = new ArrayList<TokenInfo>();
    }

    @Override
    public String extract() {
        // Non considera newlines etc.
        return String.join("", TokenInfo.allTokens(tokenList));
    }

    @Override
    public List<TokenInfo> tokens() {
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
            int begin = 0;
            for ( int nl = 0; nl < list.getLength(); nl++ ) {
                Node lineNode = list.item(nl);
                NodeList lc = lineNode.getChildNodes();
                for ( int ni = 0; ni < lc.getLength(); ni ++ ) {
                    Node item = lc.item(ni);
                    String name = item.getNodeName();
                    String tokenStr = item.getTextContent();
                    tokenStr = tokenStr.replaceAll("\\s+", ""); // TODO: NOOOOOOO!!!!
                    TokenType ttype = TokenType.WORD;
                    int end = begin + tokenStr.length() - 1;
                    if ( end < begin ) continue;
                    switch ( name ) {
                        case "tei:name":
                        case "tei:w":
                        case "tei:pc":
                        log.info("Found token: "+ tokenStr);
                        if ( name.equals("tei:pc") ) ttype = TokenType.PUNCT;
                        NamedNodeMap nmap = item.getAttributes();
                        Node xid = nmap.getNamedItem("xml:id");
                        String xmlid = null;
                        if ( xid != null )
                            xmlid = xid.getNodeValue();
                        tokenList.add(new TokenInfo(tokenStr, begin, end, ttype, xmlid));
                        begin = end + 1;
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
        for (TokenInfo token: new EpiDocTextExtractor().read(null).tokens() )
            System.out.println(token.text);
    }
}
