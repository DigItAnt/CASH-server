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
import org.w3c.dom.NodeList;

public class XpathMetadataImporter {
    private static final Logger log = LoggerFactory.getLogger(XpathMetadataImporter.class);

    class FieldDef {
        // either 
        String expression;
        Map<String, FieldDef> subfields = new HashMap<>();
        String postprocess;
        boolean isList = false; // if true, then expression is a list of nodes

        public FieldDef() {}

        public FieldDef(String expr) {
            expression = expr;
            this.postprocess = null;
        }

        public FieldDef(String expr, String postprocess) {
            expression = expr;
            this.postprocess = postprocess;
        }

        public boolean notNull() {
            return postprocess != null && postprocess.contains("notnull");
        }

        public String applyPostprocessing(String value) {
            String ret = value;
            if ( postprocess == null ) return ret.strip();
            // split postprocess on ; and iterate over each resulting token
            if ( !postprocess.contains("nostrip") ) ret = ret.strip();
            for ( String pp: postprocess.split(";") ) {
                // TODO
            }
            return ret;
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

    protected boolean isList(String fname) {
        // check if the last character of fname is '*'
        return (fname.length()>1) && (fname.charAt(fname.length()-1) == '*');
    }

    protected String cleanup(String fname) {
        fname = fname.strip();
        if ( isList(fname) ) return fname.substring(0, fname.length()-1);
        return fname;
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
                fdef.isList = isList(columns[0]);
                String fname = cleanup(columns[0]);
                log.info("Field: " + columns[0] + ":" + fdef.expression + " " + (fdef.isList? "list": "scalar"));
                fields.put(fname, fdef);
            } else if ( !line.startsWith("__") && line.contains(".") ) {
                String[] f_sub = columns[0].split("\\.");
                String field = cleanup(f_sub[0].strip());
                String subfield = cleanup(f_sub[1].strip());
                log.info("SubField: " + subfield);
                FieldDef fdef = fields.get(field);
                if ( fdef == null ) { // create a new one
                    fdef = new FieldDef();
                    fdef.isList = isList(f_sub[0].strip());
                    fields.put(field, fdef);
                    fdef.subfields = new HashMap<String, FieldDef>();
                }
                FieldDef subDef = new FieldDef(columns[1].strip());
                subDef.isList = isList(f_sub[1].strip());
                fdef.subfields.put(subfield, subDef);
            } else if ( line.startsWith("__") ) {
                log.info("Sublist: " + columns[0]);
                li += processSubListDef(fields, lines, li);
                log.info("Sublist processed, li is now: " + li);
            }
        }
    }

    protected int processSubListDef(Map<String, FieldDef> currentFields, String[] lines, int li) {
        return processSubListDef(currentFields, lines, li, 0);
    }

    protected int processSubListDef(Map<String, FieldDef> currentFields, String[] lines, int li, int depth) {
        log.info("Sublist depth: " + depth);
        int toSkip = 0;
        String[] sf = lines[li].split("\t");
        sf[0] = sf[0].substring("__START__".length()).strip(); // field name
        FieldDef fDef = new FieldDef(sf[1].strip());
        currentFields.put(sf[0], fDef);
        toSkip += 1;
        while (!lines[li + toSkip].startsWith("__END__")) {
            String line = lines[li + toSkip];
            toSkip += 1;
            if (line.startsWith("__START__")) { // New nested section
                log.info("Sublist: " + line);
                toSkip += processSubListDef(fDef.subfields, lines, li + toSkip - 1, depth+1); // Process nested, adjust toSkip
                log.info("Sublist processed, li is now: " + (li + toSkip - 1));
            } else {
                String[] columns = line.split("\t");
                if (columns.length < 2) continue;
                log.info("ListSubField: " + columns[0] + ":" + columns[1]);
                String[] f_sub = columns[0].split("\\.");
                String field = f_sub[0].strip();
                String subfield = cleanup(f_sub[1].strip());
                FieldDef subDef = new FieldDef(columns[1].strip());
                subDef.isList = isList(f_sub[1].strip());
                fDef.subfields.put(subfield, subDef);
            }
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

        if ( fdef.subfields == null || fdef.subfields.size() == 0 ) { // scalar or list
            log.info("Extracting scalar field: " + fdef.expression);
            // even for a single field, multiple nodes (e.g., text()) could be returned, so we concatenate them unless it's a list
            try{
                NodeList nlist = (NodeList) runXPath(doc, fdef.expression, XPathConstants.NODESET);
                log.info("Extracted " + nlist.getLength() + " nodes");
                if ( !fdef.isList ) {
                    log.info("Field is not a list");
                    return fdef.applyPostprocessing(nodeListToString(nlist)); //runXPath(doc, fdef.expression, XPathConstants.STRING);
                } else {
                    log.info("Field is a list");
                    // return a list of Strings
                    List<String> lst = new ArrayList<String>();
                    for ( int ni = 0; ni < nlist.getLength(); ni++ ) {
                        Node node = nlist.item(ni);
                        lst.add(fdef.applyPostprocessing(node.getTextContent()));
                    }
                    return lst;
                }
            } catch (XPathException e) {
                log.info("Expression does not appear to return a NodeSet, trying string");
                return fdef.applyPostprocessing((String)runXPath(doc, fdef.expression, XPathConstants.STRING));
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
