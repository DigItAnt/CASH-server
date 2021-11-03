package it.cnr.ilc.lari.itant.belexo.utils;

import javax.jcr.Session;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import java.io.StringReader;

public class NodeTypeRegister {
    protected static final String tokenNodeCND =
        "<ns = 'http://ilc.cnr.ir/belexo/ns'>\n" +
        "[ns:TokenNode] > nt:unstructured orderable";

    protected static final String annotationNodeCND =
        "<ns = 'http://ilc.cnr.ir/belexo/ns'>\n" +
        "[ns:AnnotationNode] > nt:unstructured orderable";

    public static void RegisterCustomNodeTypes(Session session, String cnd) throws Exception {
        CndImporter.registerNodeTypes(new StringReader(cnd), session, true);
    }

    public static void registerTypes(Session session) throws Exception {
        RegisterCustomNodeTypes(session, tokenNodeCND);
        RegisterCustomNodeTypes(session, annotationNodeCND);
    }
}
