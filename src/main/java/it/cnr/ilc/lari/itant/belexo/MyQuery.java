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
                Node node = row.getNode("s");
                System.out.println("NODEEEEEE " + node.getPath());
                JcrManager.logProperties(node);
            }
       } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (session != null) session.logout();
        }
    }

    public static void main(String[] args) {
        String statement = "SELECT * FROM [nt:base] as s WHERE s.[type]='traditionalID'";
        //statement = "select * from [nt:unstructured] as p INNER JOIN [nt:unstructured] as c on ISCHILDNODE(c,p) WHERE c.[meta_docid]='123'";
        doQuery(statement);
    }

}
