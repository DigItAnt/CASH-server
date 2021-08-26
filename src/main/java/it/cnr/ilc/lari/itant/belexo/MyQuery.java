package it.cnr.ilc.lari.itant.belexo;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

public class MyQuery {
    public static void doQuery(String statement) {
        Session session = null;
        try {
            JcrManager.init();
            session = JcrManager.getSession();
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.JCR_SQL2);
            QueryResult result = query.execute();
            String[] columns = result.getColumnNames();

            for (String column: columns)
                System.out.println("COLUMN: " + column);

            RowIterator riter = result.getRows();
            while (riter.hasNext()) {
                Row row = riter.nextRow();
                Node node = row.getNode("p");
                System.out.println("\n\n\nNODEEEEEE \n\n\n" + node.getPath());
                JcrManager.logProperties(node);
            }
       } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (session != null) session.logout();
        }
    }

    public static void main(String[] args) {
        String statement = "SELECT * FROM [nt:base] as p WHERE CONTAINS (p.[jcr:xmlcharacters], 'Oscan 7')";
        statement = "select * from [nt:base] as c INNER JOIN [nt:unstructured] as p on ISDESCENDANTNODE(c,p) WHERE CONTAINS (c.[jcr:xmlcharacters], 'Inscription ItAnt Oscan 7') and p.[mytype] = 'file'";
        //statement = "select * from [nt:base] as c INNER JOIN [nt:base] as p on ISCHILDNODE(c,p) WHERE CONTAINS (c.[jcr:xmlcharacters], 'Inscription ItAnt Oscan 7')";
        doQuery(statement);
    }

}
