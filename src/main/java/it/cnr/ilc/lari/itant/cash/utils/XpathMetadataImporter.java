package it.cnr.ilc.lari.itant.cash.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XpathMetadataImporter {
    private static final Logger log = LoggerFactory.getLogger(XpathMetadataImporter.class);

    class FieldDef {
        // either 
        String expression;
        Map<String, FieldDef> subfields;

        public FieldDef() {}

        public FieldDef(String expr) {
            expression = expr;
        }
    }

    Map<String, FieldDef> fields;
    NamespaceContext context;

    public NamespaceContext getContext() {
        return context;
    }

    public void setContext(NamespaceContext context) {
        this.context = context;
    }

    public String nodeListToString(NodeList nodes) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            builder.append(node.getNodeValue());
        }

        return builder.toString();
    }

    public XpathMetadataImporter(String expressions) {
        fields = new HashMap<String, FieldDef>();
        String[] lines = expressions.split("\n");
        boolean inSub = false;
        for ( int li = 0; li < lines.length; li++ ) {
            String line = lines[li].strip();
            if ( line.length() == 0 || line.charAt(0) == '#' ) continue;
            String[] columns = line.split("\t");
            if ( columns.length < 2 ) continue;
            if ( !line.startsWith("__") && !line.contains(".") ) { // single value
                FieldDef fdef = new FieldDef(columns[1].strip());
                log.info("Field: " + columns[0] + ":" + fdef.expression);
                fields.put(columns[0].strip(), fdef);
            } else if ( !line.startsWith("__") && line.contains(".") ) {
                String[] f_sub = columns[0].split("\\.");
                String field = f_sub[0].strip();
                String subfield = f_sub[1].strip();
                FieldDef fdef = fields.get(field);
                if ( fdef == null ) { // create a new one
                    fdef = new FieldDef();
                    fields.put(field, fdef);
                    fdef.subfields = new HashMap<String, FieldDef>();
                }
                fdef.subfields.put(subfield, new FieldDef(columns[1].strip()));
            } else if ( line.startsWith("__") ) {
                li += processSubListDef(lines, li);
                // TODO: process until line.startWith("__END__");
            }
        }
    }

    protected int processSubListDef(String[] lines, int li) {
        int toSkip = 0;
        String[] sf = lines[li].split("\t");
        sf[0] = sf[0].substring("__START__".length()).strip(); // field name
        FieldDef fDef = new FieldDef(sf[1].strip());
        fields.put(sf[0], fDef);
        fDef.subfields = new HashMap<String, FieldDef>();
        toSkip += 1;
        while ( !lines[li+toSkip].startsWith("__END__") ) {
            String[] columns = lines[li+toSkip].split("\t");
            toSkip += 1;
            if ( columns.length < 2 ) continue;
            String[] f_sub = columns[0].split("\\.");
            String field = f_sub[0].strip();
            String subfield = f_sub[1].strip();
            fDef.subfields.put(subfield, new FieldDef(columns[1].strip()));
        }

        return toSkip;
    }


    protected Object runXPath(Object doc, String expression, QName what) throws Exception {
        // TODO: FIX NAMESPACE!!!!
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(context);
        //expression = "//tei:TEI";
        Object ret = xPath.compile(expression).evaluate(doc, what);
        if ( ret instanceof String ) ret = ((String)ret).strip(); // strip the string
        log.info("Extracted: " + ret + " with " + expression);
        return ret;
    }

    protected Object extractField(Object doc, FieldDef fdef) throws Exception {
        Object ret = null;

        if ( fdef.subfields == null || fdef.subfields.size() == 0 ) { // scalar or list (list unsupported)
            log.info("Extracting scalar field: " + fdef.expression);
            // even for a single field, multiple nodes (e.g., text()) could be returned, so we concatenate them
            try{
                NodeList nlist = (NodeList) runXPath(doc, fdef.expression, XPathConstants.NODESET);
                log.info("Extracted " + nlist.getLength() + " nodes");
                return  nodeListToString(nlist); //runXPath(doc, fdef.expression, XPathConstants.STRING);
            } catch (XPathException e) {
                log.info("Expression does not appear to return a NodeSet, trying string");
                return runXPath(doc, fdef.expression, XPathConstants.STRING);
            }
        } else { // it's a structure
            if ( fdef.expression != null ) { // it's a list
                List<Object> lst = new ArrayList<Object>();
                // first get a list of nodes
                NodeList nlist = (NodeList) runXPath(doc, fdef.expression, XPathConstants.NODESET);
                for ( int ni = 0; ni < nlist.getLength(); ni++ ) {
                    Node node = nlist.item(ni);
                    lst.add(extractFields(node, fdef.subfields));
                }
                ret = lst;
            } else { // it's a single object.
                ret = extractFields(doc, fdef.subfields);
            }
        }

        return ret;
    }

    protected Map<String, Object> extractFields(Object doc, Map<String, FieldDef> fields) {
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

    public Map<String, Object> extract(Document doc) {
        return extractFields(doc, fields);
    }
}
