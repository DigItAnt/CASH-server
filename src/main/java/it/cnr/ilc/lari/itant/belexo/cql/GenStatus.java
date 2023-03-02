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
        fromList.add("LEFT JOIN tokens as tok1 ON tok1.node = node.id");
    }

    private String clearString(String value) {
        // remove first and last char -- UGLY
        return value.substring(1, value.length() - 1);
    }

    public void setWordValuePairEquals(String value) {
        whereList.add("tok1.text = ?");
        paramList.add(clearString(value));
    }

    public void setAttValuePairEquals(String att, String value) {
        annotCounter++;
        String annot = "ann" + annotCounter;
        String span = "a" + annotCounter;
        fromList.add("LEFT JOIN annotations as " + annot + " on " + annot + ".node = node.id");
        fromList.add("JOIN ann_spans as " + span + " ON " + span + ".ann = " + annot + ".id");

        String overlapCheck = "((" + span + ".`begin`<=tok1.`begin` AND " + span + ".`end`>=tok1.`begin`) OR (" + 
                                span + ".`begin`>tok1.`begin` AND tok1.`end`>=" + span + ".`begin`))";
        whereList.add("( " + annot + ".layer = ? AND " + annot + ".value = ? AND " + overlapCheck + " )");

        paramList.add(att);
        paramList.add(clearString(value));
        
    }

    public void setOperator(String operator) {
        whereList.add(operator);
    }

    public PreparedStatement gen() throws Exception {
        String query = "SELECT DISTINCT node.id, tok1.id, tok1.begin, tok1.end ";
        // add FROM concatenating fromList with comma
        query += "\nFROM " + String.join("\n  ", fromList);
        // add WHERE concatenating whereList with AND
        //query += "\nWHERE " + String.join(" AND\n  ", whereList);
        query += "\nWHERE " + String.join(" \n  ", whereList);

        PreparedStatement stmt = DBManager.getNewConnection().prepareStatement(query);

        int i = 1;
        for ( String param: paramList ) {
            stmt.setString(i, param);
            i++;
        }

        return stmt;
    }
}
