package it.cnr.ilc.lari.itant.cash.cql;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;

public class GenStatus {
    private static final Logger log = LoggerFactory.getLogger(GenStatus.class);

    public static String DOC_LAYER = "_doc";

    List<String> fromList = new ArrayList<String>();

    // whereLists is a list of whereList, one for each token
    // this is needed to manage "AND" between tokens in where clause generation
    List<List<String>> whereLists = new ArrayList<>();
    List<String> currWhereList = null;

    List<String> paramList = new ArrayList<String>();

    int annotCounter = 0; // current annotation counter

    int currentTokenId = 1;

    TokenSequence seq = new TokenSequence();

    public int getCurrentTokenId() {
        return currentTokenId;
    }

    public void setCurrentTokenId(int currentTokenId) {
        this.currentTokenId = currentTokenId;
        fromList.add(String.format("LEFT JOIN tokens as %s ON %s.node = node.id", getCurrentTokenName(),
                getCurrentTokenName()));
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
        currWhereList = new ArrayList<>();
        currWhereList.add("node.type = 'F'");
        whereLists.add(currWhereList);
    }

    private String clearString(String value) {
        // remove first and last char -- UGLY
        return value.substring(1, value.length() - 1);
    }

    String[] splitOnPipe(String value) {
        value = clearString(value);
        // split on pipe, considering that value could include a pipe escaped with \, in
        // this case the pipe is not a separator
        // e.g. "a|b|c" -> ["a", "b", "c"] , "a\\|b|c" -> ["a\\|b", "c"]
        String[] parts = value.split("(?<!\\\\)\\|");
        // for each part, remove the escape char
        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].replaceAll("\\\\", "");
        return parts;
    }

    // Utility method to generate placeholders for IN clause
    private static String generatePlaceholders(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("?");
            if (i < (count - 1)) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private String adaptOp(String op) {
        if (op.equals("=="))
            return "=";
        return op;
    }

    public void setWordValuePairOp(String value, String op, boolean cast) {
        //op is =, ==, <, >, <=, >=
        String[] choices = splitOnPipe(value);
        if (op.equals("==") && choices.length > 1) {
            _setWordValuePairEqualsMultiValue(choices);
            return;
        }

        boolean regex = false;
        //regex = clearString(value).startsWith(REGEX_PREFIX);
        regex = op.equals("=");

        if (regex) {
            op = "REGEXP";
            if (clearString(value).equals("")) { throw new InvalidParamException("Empty regex not allowed"); }
        }

        op = adaptOp(op);

        if ( cast )
            currWhereList.add("CAST( " + getCurrentTokenName() + ".text AS SIGNED) " + op + " CAST(? AS SIGNED)");
        else
            currWhereList.add(getCurrentTokenName() + ".text " + op + " ?");
        paramList.add(clearString(value));
    }

    private void _setWordValuePairEqualsMultiValue(String[] choices) {
        // REGEX in multivalue options are not managed
        currWhereList.add(getCurrentTokenName() + ".text IN (" + generatePlaceholders(choices.length) + " )");
        for (String choice : choices)
            paramList.add(choice);
    }

    protected String getAnnSpanFrom(int idspan, String annot) {
        String span = "a" + idspan;
        return "JOIN ann_spans as " + span + " ON " + span + ".ann = " + annot + ".id";
    }

    protected String getAnnSpanWhere(int idspan, String tok) {
        String span = "a" + idspan;
        String cond = String.format(
                "(GREATEST(%s.begin, %s.begin) < LEAST(%s.end, %s.end) OR (%s.begin<=%s.begin AND %s.end<=%s.end) OR (%s.begin<=%s.begin AND %s.end<=%s.end) )",
                span, tok, span, tok,
                tok, span, span, tok,
                span, tok, tok, span);
        return cond;
        // return "((" + span + ".`begin`<=" + tok + ".`begin` AND " + span + ".`end`>"
        // + tok +".`begin`) OR (" +
        // span + ".`begin`>" + tok + ".`begin` AND " + tok + ".`end`>" + span +
        // ".`begin`))";
    }

    public void setAttValuePairOp(String att, String value, String op, boolean cast) {
        boolean regex = false;
        //regex = clearString(value).startsWith(REGEX_PREFIX);
        regex = op.equals("=");

        annotCounter++;
        String annot = "ann" + annotCounter;

        fromList.add("LEFT JOIN annotations as " + annot + " on " + annot + ".node = node.id");
        fromList.add(getAnnSpanFrom(annotCounter, annot));

        if (regex) {
            op = "REGEXP";
            if (clearString(value).equals("")) { throw new InvalidParamException("Empty regex not allowed"); }
        }

        String overlapCheck = getAnnSpanWhere(annotCounter, getCurrentTokenName());

        String[] choices = splitOnPipe(value);
        if (!op.equals("==") || choices.length == 1) {
            op = adaptOp(op);
            if (cast)
                currWhereList
                    .add("( " + annot + ".layer = ? AND CAST(" + annot + ".value AS SIGNED) " + op + " CAST(? AS SIGNED) AND " + overlapCheck + " )");
            else
                currWhereList
                    .add("( " + annot + ".layer = ? AND " + annot + ".value " + op + " ? AND " + overlapCheck + " )");

            paramList.add(att);
            paramList.add(clearString(value));
        } else {
            currWhereList
                    .add("( " + annot + ".layer = ? AND " + annot + ".value IN ("+ generatePlaceholders(choices.length) +") AND " + overlapCheck + " )");
            paramList.add(att);
            for (String choice : choices)
                paramList.add(choice);
        }

    }

    public void setMetaValuePairOp(String layer, String field, String[] subfields, String value, String op, boolean cast) {
        boolean regex = false;
        //regex = clearString(value).startsWith(REGEX_PREFIX);
        regex = op.equals("=");

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

            currWhereList.add(ann + ".layer = ? AND");
            paramList.add(layer);
        }

        if (regex) {
            op = "REGEXP";
            if (clearString(value).equals("")) { throw new InvalidParamException("Empty regex not allowed"); }
        }

        if (subfields.length == 0) {
            String[] choices = splitOnPipe(value);
            if (!op.equals("==") || choices.length == 1) {
                op = adaptOp(op);
                if ( cast )
                    currWhereList.add("( " + prop + ".name = ? AND CAST(" + prop + ".value AS SIGNED) " + op + " CAST(? AS SIGNED) )");
                else
                    currWhereList.add("( " + prop + ".name = ? AND " + prop + ".value " + op + " ? )");
                paramList.add(field);
                paramList.add(clearString(value));
            } else {
                currWhereList.add("( " + prop + ".name = ? AND " + prop + ".value IN ("+ generatePlaceholders(choices.length) +") )");
                paramList.add(field);
                for (String choice : choices)
                    paramList.add(choice);
            }
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

            if ( op.contains("<") || op.contains(">") || op.equals("==") || op.equals("REGEXP") ) {
                String[] choices = splitOnPipe(value);
                String subfieldsPath = "";
                for (int i = 0; i < subfields.length; i++)
                    subfieldsPath += subfields[i] + "[*].";
                String propt = "propt" + annotCounter;
                String propv = "propv" + annotCounter;
                subfieldsPath = subfieldsPath.substring(0, subfieldsPath.length() - 1);
                String clause = "( " + prop + ".name=? AND " +
                                "(EXISTS (SELECT 1" +
                                "         FROM JSON_TABLE(" + prop + ".value, '$." + subfieldsPath + "'" +
                                "                         COLUMNS(" + propv + " JSON PATH '$')) as " + propt;

                boolean multiplechoices = false;

                if ( op.equals("==") ) {
                    if (choices.length == 1)
                        clause +=   "         WHERE " + propv + " = ? ) ) = 1)";
                    else {
                        multiplechoices = true;
                        clause +=   "         WHERE " + propv + " IN (" + generatePlaceholders(choices.length) + ") ) ) = 1)";
                    }

                } else {
                    if ( op.equals("REGEXP") )
                        clause +=   "         WHERE " + propv + " " + op + " ? ) ) = 1)";
                    else
                        clause +=   "         WHERE CAST(" + propv + " AS SIGNED) " + op + " CAST(? AS SIGNED) ) ) = 1)";
                }
                paramList.add(field);
                currWhereList.add(clause);
                if (multiplechoices)
                    for (String choice : choices)
                        paramList.add(choice);
                else
                    paramList.add(clearString(value));
            } else {  // TODO remove branch, evaluate IN BOOLEAN MODE
                currWhereList.add("( " + prop + ".name=? AND JSON_CONTAINS(" + prop + ".value, '" + subfieldsJson
                        + "', \"$\") AND MATCH(" + prop + ".value_str) AGAINST(? IN BOOLEAN MODE) )");
                paramList.add(field);
                paramList.add(clearString(value));
            }

        }

        if (!layer.equals(DOC_LAYER))
            currWhereList.add(" AND " + getAnnSpanWhere(spanId, getCurrentTokenName()));

    }

    public void setOperator(String operator) {
        currWhereList.add(operator);
    }

    public PreparedStatement gen(int offset, int limit) throws Exception {
        // String query = String.format("SELECT DISTINCT node.id, %s.id, %s.begin,
        // %s.end ", getCurrentTokenName(), getCurrentTokenName(),
        // getCurrentTokenName());
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
        for (List<String> whereList : whereLists) {
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
        for (String param : paramList) {
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
