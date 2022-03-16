package it.cnr.ilc.lari.itant.belexo.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XpathMetadataImporter {
    private static final Logger log = LoggerFactory.getLogger(XpathMetadataImporter.class);

    class FieldDef {
        // either 
        String expression;
        Map<String, FieldDef> subfields;
    }

    Map<String, FieldDef> fields;

    public XpathMetadataImporter(String expressions) {
        fields = new HashMap<String, FieldDef>();
        String[] lines = expressions.split("\n");
        for ( String line: lines ) {
            line = line.strip();
            if ( line.length() == 0 || line.charAt(0) == '#' ) continue;
            String[] columns = line.split("\t");
            if ( columns.length < 2 ) continue;
            FieldDef fdef = new FieldDef();
            fdef.expression = columns[1].strip();
            log.info("Field: " + columns[0] + ":" + fdef.expression);
            fields.put(columns[0].strip(), fdef);
        }
    }
    
    protected Object runXPath(Document doc, String expression, QName what) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        expression = "//tei:title/text()";
        Object ret = xPath.compile(expression).evaluate(doc, what);
        log.info("Extracted: " + ret + " with " + expression);
        return ret;
    }

    protected Object extractField(Document doc, FieldDef fdef) throws Exception {
        Object ret = null;

        if ( fdef.subfields == null || fdef.subfields.size() == 0 ) { // scalar or list
            log.info("Extracting field");
            return runXPath(doc, fdef.expression, XPathConstants.STRING);
        }

        return ret;
    }

    public Map<String, Object> extract(Document doc) {
        Map<String, Object> ret = new HashMap<String, Object>();

        for ( String field: fields.keySet() ) {
            Object value = null;
            try {
                value = extractField(doc, fields.get(field));
                log.info("value for: " + field + " is '" + value + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ( value != null ) ret.put(field, value);
        }

        return ret;
    }
}
