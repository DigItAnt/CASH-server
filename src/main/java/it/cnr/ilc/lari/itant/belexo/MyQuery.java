package it.cnr.ilc.lari.itant.belexo;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import it.cnr.ilc.lari.itant.belexo.utils.NodeTypeRegister;

public class MyQuery {
    public static void doQuery(String statement) {
        Session session = null;
        try {
            JcrManager.init();
            session = JcrManager.getSession();
            NodeTypeRegister.registerTypes(session);
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query query = qm.createQuery(statement, Query.JCR_SQL2);
            QueryResult result = query.execute();
            String[] columns = result.getColumnNames();

            for (String column: columns)
                System.out.println("COLUMN: " + column);

            int nres = 0;
            RowIterator riter = result.getRows();
            while (riter.hasNext()) {
                nres++;
                Row row = riter.nextRow();
                Node node = row.getNode("p");
                System.out.println("\n\n\nNODEEEEEE " + node.getPath() + "\n\n\n");
                JcrManager.logProperties(node);
            }
            System.out.println("NRES -> " + nres);
       } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (session != null) session.logout();
        }
    }

    public static void main(String[] args) {
        String statement = "SELECT * FROM [nt:base] as p WHERE p.mytype='file'";
        //statement = "select * from [nt:base] as c INNER JOIN [nt:unstructured] as p on ISDESCENDANTNODE(c,p) WHERE CONTAINS (c.[jcr:xmlcharacters], 'Inscription ItAnt Oscan 7') and p.[mytype] = 'file'";
        //statement = "select * from [nt:base] as c INNER JOIN [nt:base] as p on ISCHILDNODE(c,p) WHERE CONTAINS (c.[jcr:xmlcharacters], 'Inscription ItAnt Oscan 7')";
        statement = "SELECT * FROM [ns:FileNode] as p " + "INNER JOIN [ns:TokenNode] as t on ISDESCENDANTNODE(t, p) " + " WHERE p.mytype='file'";
        statement = "SELECT * FROM [ns:FileNode] as p " + "INNER JOIN [ns:TokenNode] as t on t.fileref=p.myid " + " WHERE p.mytype='file'";
        doQuery(statement);
    }

}
