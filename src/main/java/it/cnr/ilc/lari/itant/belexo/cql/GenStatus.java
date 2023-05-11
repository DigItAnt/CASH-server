package it.cnr.ilc.lari.itant.belexo.cql;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.cnr.ilc.lari.itant.cash.DBManager;

public class GenStatus {
    private static final Logger log = LoggerFactory.getLogger(GenStatus.class);

    public static String DOC_LAYER = "_doc";
    
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

    protected String getAnnSpanFrom(int idspan, String annot) {
        String span = "a" + idspan;
        return "JOIN ann_spans as " + span + " ON " + span + ".ann = " + annot + ".id";
    }

    protected String getAnnSpanWhere(int idspan, String tok) {
        String span = "a" + idspan;
        String cond = String.format("(GREATEST(%s.begin, %s.begin) < LEAST(%s.end, %s.end))", span, tok, span, tok);
        return cond;
        //return "((" + span + ".`begin`<=" + tok + ".`begin` AND " + span + ".`end`>" + tok +".`begin`) OR (" + 
        //       span + ".`begin`>" + tok + ".`begin` AND " + tok + ".`end`>" + span + ".`begin`))";
    }

    public void setAttValuePairEquals(String att, String value) {
        annotCounter++;
        String annot = "ann" + annotCounter;

        fromList.add("LEFT JOIN annotations as " + annot + " on " + annot + ".node = node.id");
        fromList.add(getAnnSpanFrom(annotCounter, annot));

        String overlapCheck = getAnnSpanWhere(annotCounter, "tok1");
        whereList.add("( " + annot + ".layer = ? AND " + annot + ".value = ? AND " + overlapCheck + " )");

        paramList.add(att);
        paramList.add(clearString(value));
        
    }

    public void setMetaValuePairEquals(String layer, String field, String[] subfields, String value) {
        annotCounter++;
        String prop = "prop" + annotCounter;
        int spanId = 0;
        if (layer.equals(DOC_LAYER))
            fromList.add("JOIN str_fs_props as " + prop + " on " + prop + ".node = node.id");
        else {
            annotCounter++;
            String ann = "ann" + annotCounter;
            fromList.add("LEFT JOIN annotations as " + ann + " ON " + ann + ".node = node.id");
            fromList.add("JOIN str_ann_props as " + prop + " on " + prop + ".ann = " + ann + ".id");
            spanId = annotCounter;
            fromList.add(getAnnSpanFrom(annotCounter, ann));
        }

        if (subfields.length == 0) {
            whereList.add("( " + prop + ".name = ? AND " + prop + ".value = ? )");
            paramList.add(field);
            paramList.add(clearString(value));
        } else {
            Map<String, Object> subfieldsMap = new HashMap<>();
            Map<String, Object> currentMap = subfieldsMap;
            for (int i = 0; i < subfields.length - 1; i++) {
                Map<String, Object> newMap = new HashMap<>();
                currentMap.put(subfields[i], newMap);
                currentMap = newMap;
            }
            currentMap.put(subfields[subfields.length - 1], clearString(value));

            String subfieldsJson = "";
            try {
                subfieldsJson = new ObjectMapper().writeValueAsString(subfieldsMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            log.info("subfieldsJson: " + subfieldsJson);

            whereList.add("( " + prop + ".name=? AND JSON_CONTAINS(" + prop + ".value, '" + subfieldsJson + "', \"$\") AND MATCH(" + prop + ".value_str) AGAINST(? IN BOOLEAN MODE) )");
            paramList.add(field);
            paramList.add(clearString(value));


        }

        if (!layer.equals(DOC_LAYER))
            whereList.add(" AND " + getAnnSpanWhere(spanId, "tok1"));
        
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
        if (whereList.size() > 0)
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
