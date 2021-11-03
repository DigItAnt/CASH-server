package it.cnr.ilc.lari.itant.belexo.query;

import java.util.List;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import it.cnr.ilc.lari.itant.belexo.JcrManager;

public class BelexoQuery {    
    public static List<Node> nodesQuerySQL2(String statement, Object ... args) {
        List<Node> ret = null;
	    Session session = null;
	    try {
            JcrManager.init();
            session = JcrManager.getSession();
            ret = nodesQuerySQL2(session, statement, args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( session != null ) session.logout();
        }
        return ret;
    }

    public static List<Node> nodesQuerySQL2(Session session, String statement, Object ... args) {
        /* The query assumes that 'node' is the alias to return */
        List<Node> ret = new ArrayList<Node>();
	    try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.JCR_SQL2);
            QueryResult result = query.execute();
            String[] columns = result.getColumnNames();

            for (String column: columns)
                System.out.println("COLUMN: " + column);

            RowIterator riter = result.getRows();
            while (riter.hasNext()) {
                Row row = riter.nextRow();
                Node node = row.getNode("node");
                ret.add(node);
                System.out.println("\n\n\nNODEEEEEE \n\n\n" + node.getPath());
                //JcrManager.logProperties(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<Node> nodesQuerySQL(Session session, String statement, Object ... args) {
        /* The query assumes that 'node' is the alias to return */
        List<Node> ret = new ArrayList<Node>();
	    try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.SQL);
            QueryResult result = query.execute();
            String[] columns = result.getColumnNames();

            for (String column: columns)
                System.out.println("COLUMN: " + column);

            RowIterator riter = result.getRows();
            while (riter.hasNext()) {
                Row row = riter.nextRow();
                Node node = row.getNode();
                ret.add(node);
                System.out.println("\n\n\nNODEEEEEE \n\n\n" + node.getPath());
                //JcrManager.logProperties(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<Node> nodesQueryXPath(Session session, String statement, Object ... args) {
        /* The query assumes that 'node' is the alias to return */
        List<Node> ret = new ArrayList<Node>();
	    try {
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.XPATH);
            QueryResult result = query.execute();
            String[] columns = result.getColumnNames();

            for (String column: columns)
                System.out.println("COLUMN: " + column);

            RowIterator riter = result.getRows();
            while (riter.hasNext()) {
                Row row = riter.nextRow();
                Node node = row.getNode("node");
                ret.add(node);
                System.out.println("\n\n\nNODEEEEEE \n\n\n" + node.getPath());
                //JcrManager.logProperties(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
