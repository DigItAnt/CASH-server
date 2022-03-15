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

import it.cnr.ilc.lari.itant.belexo.om.Annotation;
import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo.TokenType;

public class EpiDocTextExtractor implements TextExtractorInterface {

    private static final Logger log = LoggerFactory.getLogger(EpiDocTextExtractor.class);
    List<TokenInfo> tokenList;
    List<Annotation> annotationList;
    
    public EpiDocTextExtractor() {
        tokenList = new ArrayList<TokenInfo>();
        annotationList = new ArrayList<Annotation>();
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
    public List<Annotation> annotations() {
        return annotationList;
    }

    protected void checkAnnotation(Node annNode, String text, int begin) {
        String nodeName = annNode.getNodeName();
        switch ( nodeName ) {
            case "tei:name":
            case "tei:lb":
            case "tei:gap":
            case "tei:supplied":
            case "tei:unclear":
                break;
            default:
                return;
        }
        // create annotation
        Annotation ann = new Annotation();
        ann.setLayer("epidoc");
        ann.setValue(nodeName);
        NamedNodeMap nmap = annNode.getAttributes();
        ann.attributesFromNodeMap(nmap);
        Annotation.Span span = new Annotation.Span();
        span.setStart(begin);
        span.setEnd(begin+text.length());
        ann.addSpan(span);
        ann.setImported(true);
        log.info("Added annotation: " + ann.toString());
        annotationList.add(ann);
    }

    protected String parseSubItems(Node item, int begin) {
        // This is a token. Get text from his children, and create relative annotations
        String ret = "";

        log.info("Processing node: " + item.getNodeName());
        NodeList lc = item.getChildNodes();
        for ( int ni = 0; ni < lc.getLength(); ni ++ ) {
            Node child = lc.item(ni);
            log.info("Processing child node: " + child.getNodeName());
            String text = child.getTextContent();
            text = text.replaceAll("\\s+", "");
            checkAnnotation(child, text, begin);
            ret += text;
        }

        return ret;

    }

    protected TokenInfo processCandidateToken(Node item, int begin) {
        String name = item.getNodeName();
        TokenType ttype = TokenType.WORD;
        switch ( name ) {
            case "tei:name":
            case "tei:w":
                break;
            case "tei:pc":
                ttype = TokenType.PUNCT;
                break;
            case "tei:lb":
            case "tei:gap":
                checkAnnotation(item, "", begin);
                return null;
            default:
                return null;
        }
        String tokenStr = parseSubItems(item, begin);
        if ( name.equals("tei:name") ) {
            checkAnnotation(item, tokenStr, begin);
        }
        int end = begin + tokenStr.length();
        log.info("Found token: "+ tokenStr);
        NamedNodeMap nmap = item.getAttributes();
        Node xid = nmap.getNamedItem("xml:id");
         String xmlid = null;
        if ( xid != null )
            xmlid = xid.getNodeValue();
        TokenInfo tinfo = new TokenInfo(tokenStr, begin, end, ttype, xmlid);
        tinfo.imported = true;
        tokenList.add(tinfo);
        return tinfo;
    }

    @Override
    public TextExtractorInterface read(InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList list = doc.getElementsByTagName("tei:ab"); // TODO: There's stuff BEFORE ab!!
            log.info("NABS: " + list.getLength());
            int begin = 0;
            for ( int nl = 0; nl < list.getLength(); nl++ ) {
                Node abNode = list.item(nl);
                NodeList lc = abNode.getChildNodes();
                for ( int ni = 0; ni < lc.getLength(); ni ++ ) {
                    Node item = lc.item(ni);
                    TokenInfo tinfo = processCandidateToken(item, begin);
                    if ( tinfo == null ) continue;
                    begin += tinfo.text.length();
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
