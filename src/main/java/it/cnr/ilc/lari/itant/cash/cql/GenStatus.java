package it.cnr.ilc.lari.itant.cash.cql;

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
    public static String REGEX_PREFIX = "_REGEX_";
    
    List<String> fromList = new ArrayList<String>();

    // whereLists is a list of whereList, one for each token
    // this is needed to manage "AND" between tokens in where clause generation
    List<List<String>> whereLists = new ArrayList<>();
    List<String> currWhereList = null;

    List<String> paramList = new ArrayList<String>();

    int annotCounter = 0;  // current annotation counter

    int currentTokenId = 1;

    TokenSequence seq = new TokenSequence();

    public int getCurrentTokenId() {
        return currentTokenId;
    }

    public void setCurrentTokenId(int currentTokenId) {
        this.currentTokenId = currentTokenId;
        fromList.add(String.format("LEFT JOIN tokens as %s ON %s.node = node.id", getCurrentTokenName(), getCurrentTokenName()));
        seq.addToken(currentTokenId);

        // set whereList for current token
        currWhereList = new ArrayList<>();
        whereLists.add(currWhereList);
    }

    public String getCurrentTokenName() {
        return "tok" + this.currentTokenId;
    }

    public GenStatus() {
        fromList.add("fsnodes as node");
    }

    private String clearString(String value) {
        // remove first and last char -- UGLY
        return value.substring(1, value.length() - 1);
    }

    public void setWordValuePairEquals(String value) {
        if (!clearString(value).startsWith(REGEX_PREFIX))
            currWhereList.add(getCurrentTokenName() + ".text = ?");
        else {
            // remove the prefix
            value = value.substring(REGEX_PREFIX.length());
            currWhereList.add(getCurrentTokenName() + ".text REGEXP ?");
        }
        paramList.add(clearString(value));
    }

    protected String getAnnSpanFrom(int idspan, String annot) {
        String span = "a" + idspan;
        return "JOIN ann_spans as " + span + " ON " + span + ".ann = " + annot + ".id";
    }

    protected String getAnnSpanWhere(int idspan, String tok) {
        String span = "a" + idspan;
        String cond = String.format("(GREATEST(%s.begin, %s.begin) < LEAST(%s.end, %s.end) OR (%s.begin<=%s.begin AND %s.end<=%s.end))", span, tok, span, tok,
                                                                                                                                         tok, span, span, tok);
        return cond;
        //return "((" + span + ".`begin`<=" + tok + ".`begin` AND " + span + ".`end`>" + tok +".`begin`) OR (" + 
        //       span + ".`begin`>" + tok + ".`begin` AND " + tok + ".`end`>" + span + ".`begin`))";
    }

    public void setAttValuePairEquals(String att, String value) {
        annotCounter++;
        String annot = "ann" + annotCounter;

        fromList.add("LEFT JOIN annotations as " + annot + " on " + annot + ".node = node.id");
        fromList.add(getAnnSpanFrom(annotCounter, annot));

        String overlapCheck = getAnnSpanWhere(annotCounter, getCurrentTokenName());
        currWhereList.add("( " + annot + ".layer = ? AND " + annot + ".value = ? AND " + overlapCheck + " )");

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
            currWhereList.add("( " + prop + ".name = ? AND " + prop + ".value = ? )");
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

            currWhereList.add("( " + prop + ".name=? AND JSON_CONTAINS(" + prop + ".value, '" + subfieldsJson + "', \"$\") AND MATCH(" + prop + ".value_str) AGAINST(? IN BOOLEAN MODE) )");
            paramList.add(field);
            paramList.add(clearString(value));


        }

        if (!layer.equals(DOC_LAYER))
            currWhereList.add(" AND " + getAnnSpanWhere(spanId, getCurrentTokenName()));
        
    }

    public void setOperator(String operator) {
        currWhereList.add(operator);
    }

    public PreparedStatement gen(int offset, int limit) throws Exception {
        //String query = String.format("SELECT DISTINCT node.id, %s.id, %s.begin, %s.end ", getCurrentTokenName(), getCurrentTokenName(), getCurrentTokenName());
        String query = "SELECT DISTINCT node.id";
        String fromSeq = seq.buildFromString();
        if (fromSeq.length() > 0)
            query += ", " + fromSeq;

        String chainSeq = seq.buildChainString();
        if (chainSeq.length() > 0) {
            if (currWhereList.size() > 0)
                currWhereList.add(" AND " + chainSeq);
            else
                currWhereList.add(chainSeq);
        }

        // add FROM concatenating fromList with comma
        query += "\nFROM " + String.join("\n  ", fromList);

        // manage whereLists
        boolean whereAdded = false;
        for (List<String> whereList: whereLists) {
            if (whereList.size() > 0) {
                if (!whereAdded) {
                    query += "\nWHERE ";
                    whereAdded = true;
                } else
                    query += "\n  AND ";
                query += String.join(" \n  ", whereList);
            }
        }

        // add offset and limit
        query += "\nLIMIT ?, ?";

        PreparedStatement stmt = DBManager.getNewConnection().prepareStatement(query);

        int i = 1;
        for ( String param: paramList ) {
            stmt.setString(i, param);
            i++;
        }

        // add offset and limit
        stmt.setInt(i, offset);
        i++;
        stmt.setInt(i, limit);


        return stmt;
    }
}
