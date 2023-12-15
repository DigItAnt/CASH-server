package it.cnr.ilc.lari.itant.cash.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.cash.om.BiblioFilter;
import it.cnr.ilc.lari.itant.cash.om.BiblioRequest;
import it.cnr.ilc.lari.itant.cash.om.BiblioResponse;

public class ZoteroQueryManager {

    public static PreparedStatement generatePreparedStatement(Connection conn, List<BiblioFilter> filters, int offset, int limit)
            throws SQLException {
        if (filters == null || filters.isEmpty()) {
            throw new IllegalArgumentException("Filter list cannot be null or empty");
        }

        StringBuilder joinClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                joinClause.append(String.format("JOIN zotero AS t%d ON t1.fileid = t%d.fileid \n", i + 1, i + 1));
                whereClause.append(" AND ");
            }

            String alias = "t" + (i + 1);
            whereClause.append(generateCondition(alias, filters.get(i), i)+"\n");
        }

        String sql = "\nSELECT t.fileid, JSON_OBJECTAGG(t.key, t.Value) AS Attributes \n" +
                "FROM zotero AS t \n" +
                "WHERE t.fileid IN (" +
                "SELECT t1.fileid \n" +
                "FROM zotero AS t1 \n" +
                joinClause +
                "WHERE " + whereClause +
                ") \n" +
                "GROUP BY t.fileid\n" +
                "LIMIT " + limit + " OFFSET " + offset + "\n";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        // Set parameters for the prepared statement
        for (int i = 0; i < filters.size(); i++) {
            BiblioFilter filter = filters.get(i);
            pstmt.setString(2 * i + 1, filter.getKey());
            if (filter.getOp().equalsIgnoreCase("in")) {
                pstmt.setString(2 * i + 2, "%" + filter.getValue() + "%");
            } else if (filter.getOp().equalsIgnoreCase("lt")) {
                pstmt.setString(2 * i + 2, filter.getValue());
            } else {
                pstmt.setString(2 * i + 2, filter.getValue());
            }
        }

        return pstmt;
    }

    private static String generateCondition(String alias, BiblioFilter filter, int index) {
        switch (filter.getOp().toLowerCase()) {
            case "eq":
                return String.format("(%s.key = ? AND %s.Value = ?)", alias, alias);
            case "in":
                return String.format("(%s.key = ? AND %s.Value LIKE ?)", alias, alias);
            case "lt":
                return String.format("(%s.key = ? AND CAST(%s.Value AS UNSIGNED) < CAST(? AS UNSIGNED))", alias, alias);
            // Add more cases for other operators
            default:
                throw new IllegalArgumentException("Unsupported operation: " + filter.getOp());
        }
    }

    public static BiblioResponse query(BiblioRequest request, int offset, int limit) throws Exception {
        Connection connection = DBManager.getNewConnection();
        BiblioResponse res = new BiblioResponse();

        try {
            PreparedStatement stmt = generatePreparedStatement(connection, request.getFilters(), offset, limit);

            // log sql
            System.out.println(stmt.toString());

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                String fileid = rs.getString("fileid");
                String attributes = rs.getString("Attributes");

                JSONObject records = new JSONObject(attributes);

                for (String key : records.keySet()) {
                    res.add(fileid, key, records.getString(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidParamException(e.getMessage());
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }

        return res;
    }

    public static void main(String[] args) throws Exception {
        List<BiblioFilter> filters = List.of(
                new BiblioFilter("Author", "Stefania", "in"),
                new BiblioFilter("Date", "1985", "eq")
        // new BiblioFilter("k2", "v2", "in"),
        // new BiblioFilter("k3", "v3", "lt")
        );

        BiblioResponse res = query(new BiblioRequest(filters), 0, 10);
        
        System.out.println(res);
    }

}
