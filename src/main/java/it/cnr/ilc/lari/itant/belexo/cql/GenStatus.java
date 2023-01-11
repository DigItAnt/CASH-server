package it.cnr.ilc.lari.itant.belexo.cql;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import it.cnr.ilc.lari.itant.cash.DBManager;

public class GenStatus {
    List<String> fromList = new ArrayList<String>();
    List<String> whereList = new ArrayList<String>();
    List<String> paramList = new ArrayList<String>();

    int annotCounter = 0;  // current annotation counter

    public GenStatus() {
        fromList.add("fsnodes as node");
        fromList.add("LEFT JOIN tokens as tok ON tok.node = node.id");
    }

    private String clearString(String value) {
        // remove first and last char -- UGLY
        return value.substring(1, value.length() - 1);
    }

    public void setWordValuePairEquals(String value) {
        whereList.add("tok.text = ?");
        paramList.add(clearString(value));
    }

    public void setAttValuePairEquals(String att, String value) {
        annotCounter++;
        String annot = "ann" + annotCounter;
        fromList.add("LEFT JOIN annotations as " + annot + " on " + annot + ".node = node.id");

        whereList.add("( TokenMatch(" + annot + ".id, tok.id) AND " + annot + ".layer = ? AND " + annot + ".value = ? )");

        paramList.add(att);
        paramList.add(clearString(value));
        
    }

    public PreparedStatement gen() throws Exception {
        String query = "SELECT DISTINCT node.id ";
        // add FROM concatenating fromList with comma
        query += "\nFROM " + String.join("\n  ", fromList);
        // add WHERE concatenating whereList with AND
        query += "\nWHERE " + String.join(" AND\n  ", whereList);

        PreparedStatement stmt = DBManager.getConnection().prepareStatement(query);

        int i = 1;
        for ( String param: paramList ) {
            stmt.setString(i, param);
            i++;
        }

        return stmt;
    }
}
