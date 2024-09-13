package it.cnr.ilc.lari.itant.cash.utils;

import java.io.StringWriter;
import java.sql.Blob;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.Annotation;
import it.cnr.ilc.lari.itant.cash.om.Token;

public class EpiDocXMLExporter {

    private static final Logger log = LoggerFactory.getLogger(EpiDocXMLExporter.class);

    protected static void enrichTokens(Document doc, long id) throws Exception {
        List<Token> tokens = DBManager.getNodeTokens(id, null);
        XPath xpathObj = XPathFactory.newInstance().newXPath();
        xpathObj.setNamespaceContext(new SimpleNamespaceContext(EpiDocTextExtractor.NSPS));
        for (Token token : tokens) {
            log.info("Exporing token: " + token.getXmlid());
            String xmlId = token.getXmlid();
            if ( xmlId == null || xmlId.isEmpty() ) {
                // if the token has no xml:id, skip it
                // normally, see if we can get it from an xpath expression
                continue;
            }
            // find the element with the xml:id attribute
            String xpath = "//*[@xml:id='" + xmlId + "']";
            NodeList nodeList = (NodeList) xpathObj.evaluate(xpath, doc, XPathConstants.NODESET);
            if (nodeList.getLength() == 0) {
                // if the element is not found, skip it
                log.info(token.getXmlid() + " not found in the document?!");
                continue;
            }
            // there's supposed to be ONE such element
            Element element = (Element) nodeList.item(0);
            // add an attribute to the element
        
            // fetch all annotations with the same span as the token
            List<Annotation> annotations = DBManager.getAnnotationsBySpan(id, token.getBegin(), token.getEnd(), null);

            for (Annotation annotation : annotations) {
                log.info(token.getXmlid() + " ANNOTATION " + annotation.toString());
                if ( !annotation.getLayer().equals("attestation") ) continue;
                // add an attribute to the element
                element.setAttribute("sameAs", annotation.getValue());
            }
            
        }
    }

    public static String toXML(long id) throws Exception {
        Blob blob = DBManager.getBlob(id);
        // read the blob as a string
        String xml = new String(blob.getBytes(1, (int) blob.length()));
        // parse the string into a Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(IOUtils.toInputStream(xml, "UTF-8"));

        enrichTokens(doc, id);

        // return the Document as a string
        return toString(doc);

    }

    public static String toString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
    
}
