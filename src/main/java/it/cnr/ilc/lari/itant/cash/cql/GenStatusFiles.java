package it.cnr.ilc.lari.itant.cash.cql;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.cash.DBManager;

public class GenStatusFiles extends GenStatus {
    private static final Logger log = LoggerFactory.getLogger(GenStatusFiles.class);

    private boolean justCount;

    public GenStatusFiles(boolean justCount) {
        super();
        this.justCount = justCount;
    }

    public PreparedStatement gen(int offset, int limit) throws Exception {
        //String query = String.format("SELECT DISTINCT node.id, %s.id, %s.begin, %s.end ", getCurrentTokenName(), getCurrentTokenName(), getCurrentTokenName());
        String query = justCount?"SELECT COUNT(DISTINCT node.id)":"SELECT DISTINCT node.id";
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

        if (offset >= 0)
            // add offset and limit
            query += "\nLIMIT ?, ?";

        PreparedStatement stmt = DBManager.getNewConnection().prepareStatement(query);

        int i = 1;
        for ( String param: paramList ) {
            stmt.setString(i, param);
            i++;
        }

        if (offset >= 0) {
            // add offset and limit
            stmt.setInt(i, offset);
            i++;
            stmt.setInt(i, limit);
        }

        return stmt;
    }
}
