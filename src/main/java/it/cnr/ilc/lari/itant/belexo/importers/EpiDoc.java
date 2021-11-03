package it.cnr.ilc.lari.itant.belexo.importers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import it.cnr.ilc.lari.itant.belexo.JcrManager;
import it.cnr.ilc.lari.itant.belexo.query.BelexoQuery;

/**
 * This class implements the importing of epidoc. It provides methods to deal with the extra
 * info (metadata, annotations) to be extracted from the uploadd 
 */
public class EpiDoc {
    Node rootNode;
    public EpiDoc(Node docNode) {
        rootNode = docNode;
    }
    
    public String getText() {
        /* extracts the text */
        StringBuffer buf = new StringBuffer("");
        Session session = null;
        try {
            JcrManager.init();
            session = JcrManager.getSession();            
            String rootPath = "/root/testfolder/prova8.xml" /*rootNode.getPath()*/ + "/structure/tei:TEI";

            String statement = "select * from nt:base WHERE " +
                                "ISCHILDNODE('" + rootPath + "')" + " and " + 
                                "jcr:path LIKE '%tei:text%'" //+ " and " + //node.[jcr:path] LIKE '%tei:div%' and " +
                                //"node.[jcr:xmlcharacters] is NOT NULL"
                                ;
                                //"node.[jcr:title] LIKE '%tei:text%'";
            //statement = "SELECT * FROM [nt:base] as node WHERE CONTAINS (node.[jcr:xmlcharacters], 'Oscan 2')";
            statement = "select * from nt:base WHERE jcr:path LIKE '%/tei:text'";

            //statement = "/root/testfolder/prova8.xml//tei:text//tei:div//*";
            System.out.println("\n\n\nQUERY: " + statement);
            List<Node> results = BelexoQuery.nodesQuerySQL(session, statement);
            for (Node node: results) {
                System.out.println("Node: " + node.getPath());
                JcrManager.logProperties(node);
                //buf.append(node.getProperty("jcr:xmlcharacters").getString());
            }
        } catch ( Exception e ) {
            System.out.println("\n\nERRRRRORORRRRRR" + e.toString());
            e.printStackTrace();
        } finally {
            if ( session != null ) session.logout();
        }

        return buf.toString();
    }

    public static void main(String[] args) {
        System.out.println("\n\n\n\n\n\n\nTesting EpiDoc importer class");
        EpiDoc doc = new EpiDoc(null);
        System.out.println("Text: " + doc.getText());

    }
}
