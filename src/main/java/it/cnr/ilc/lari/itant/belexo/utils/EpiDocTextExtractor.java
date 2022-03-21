package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.cnr.ilc.lari.itant.belexo.exc.BadFormatException;
import it.cnr.ilc.lari.itant.belexo.om.Annotation;
import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo.TokenType;

public class EpiDocTextExtractor implements TextExtractorInterface {

    private static final String MDATAPATH = "/importer/epidoc.tsv";
    private static final String MDATA = "fieldID\t//tei:TEI/@xml:id\n" +
    "trismegistos.trismegistosID\t//tei:altIdentifier[@type='trismegistos']/tei:idno/text()\n" +
    "trismegistos.trismegistosID_url\t//tei:altIdentifier[@type='trismegistos']/tei:idno/@source\n"+
    "__START__traditionalID\t//tei:altIdentifier[@type='traditionalID']/tei:idno\n" +
    "traditionalID.traditionalID\ttext()\n" +
    "traditionalID.traditionalID_url\t@source\n" +
    "__END__";
    private static final Map<String, String> NSPS = new HashMap<String, String>() {{
        put("dcr", "http://www.isocat.org/ns/dcr");
        put("tei", "http://www.tei-c.org/ns/1.0");
    }};

    private static final Logger log = LoggerFactory.getLogger(EpiDocTextExtractor.class);
    List<TokenInfo> tokenList;
    List<Annotation> annotationList;
    Map<String, Object> mdata;
    XpathMetadataImporter mimporter;
    private static final String LAYER = "epidoc";

    public EpiDocTextExtractor() {
        tokenList = new ArrayList<TokenInfo>();
        annotationList = new ArrayList<Annotation>();
        mdata = null;
        ClassPathResource cpr = new ClassPathResource(MDATAPATH);
        try {
            String mdata = new String(cpr.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            mdata = mdata.substring(mdata.indexOf('\n'));
            mimporter = new XpathMetadataImporter(mdata);
            mimporter.setContext(new SimpleNamespaceContext(NSPS));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Cannot read importer: ", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> metadata() { return mdata; }

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
            case "tei:pc":
            case "tei:lb":
            case "tei:gap":
            case "tei:supplied":
            case "tei:unclear":
            case "tei:ex":
            case "tei:abbr":
            case "tei:del":
            case "tei:expan":
                break;
            default:
                return;
        }
        // create annotation
        Annotation ann = new Annotation();
        ann.setLayer(LAYER);
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
            text = text.replaceAll("\\s+", ""); // TODO: this probably shouldn't exist
            checkAnnotation(child, text, begin+ret.length());
            parseSubItems(child, begin+ret.length());
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
        if ( name.equals("tei:name") || name.equals("tei:pc") ) {
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

    private Node getDivInBody(Document doc, String type) {
        NodeList bodys = doc.getElementsByTagName("tei:body");
        Node body = bodys.item(0);
        // Inside the body, look for 'div type="edition"'
        if ( body == null )
            return null;
        NodeList divs = body.getChildNodes();
        for ( int nl = 0; nl < divs.getLength(); nl++ ) {
            Node div = divs.item(nl);
            NamedNodeMap attrs = div.getAttributes();
            if ( attrs == null ) continue;
            Node xid = attrs.getNamedItem("type");
            if ( xid == null ) continue;
            if ( xid.getNodeValue().equals(type) ) return div;
        }
        
        return null;
    }

    protected List<Node> getParts(Node edition) {
        List<Node> ret = new ArrayList<Node>();
        NodeList divs = edition.getChildNodes();
        for ( int nl = 0; nl < divs.getLength(); nl++ ) {
            Node div = divs.item(nl);
            if ( !div.getNodeName().equals("tei:div") ) continue;
            NamedNodeMap attrs = div.getAttributes();
            Node xid = attrs.getNamedItem("type");
            if ( xid == null || (!xid.getNodeValue().equals("textpart")) ) continue;
            ret.add(div);
        }

        return ret;
    }

    protected Node getChildByName(Node node, String name) {
        NodeList children = node.getChildNodes();
        for ( int nl = 0; nl < children.getLength(); nl++ ) {
            Node child = children.item(nl);
            if ( child.getNodeName().equals(name) ) return child;
        }

        return null;
    }

    protected int processPart(Node part, int begin) throws BadFormatException {
        int offset = begin;
        Node abNode = getChildByName(part, "tei:ab");
        if ( abNode == null ) throw new BadFormatException();
        NodeList lc = abNode.getChildNodes();
        for ( int ni = 0; ni < lc.getLength(); ni ++ ) {
            Node item = lc.item(ni);
            TokenInfo tinfo = processCandidateToken(item, begin);
            if ( tinfo == null ) continue;
            begin += tinfo.text.length();
        }

        return begin-offset;
    }

    @Override
    public TextExtractorInterface read(InputStream is) throws BadFormatException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            // metadata
            mdata = mimporter.extract(doc);

            // text, tokens and annotations
            Node div = getDivInBody(doc, "edition");
            if ( div == null ) throw new BadFormatException();
            List<Node> parts = getParts(div);
            int begin = 0;
            for ( Node part: parts ) {
                int length = processPart(part, begin);
                // annotate part
                Annotation ann = new Annotation();
                ann.setLayer(LAYER);
                ann.setValue(part.getNodeName());
                NamedNodeMap nmap = part.getAttributes();
                ann.attributesFromNodeMap(nmap);
                Annotation.Span span = new Annotation.Span();
                span.setStart(begin);
                span.setEnd(begin+length);
                ann.addSpan(span);
                ann.setImported(true);
                log.info("Added annotation: " + ann.toString());
                annotationList.add(ann);
                
                begin += length;
            }

            log.info("TOKENS: " + extract());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return this;
    }



    public static void main(String[] args) throws Exception {
        for (TokenInfo token: new EpiDocTextExtractor().read(null).tokens() )
            System.out.println(token.text);
    }
}
