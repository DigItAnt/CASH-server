package it.cnr.ilc.lari.itant.cash.customparsers;

import java.sql.PreparedStatement;
import java.util.Arrays;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.cql.GenStatus;
import it.cnr.ilc.lari.itant.cash.exc.BadFormatException;

public class MetadataToSQL {
    public static final String FIELD_SEPARATOR = "__";

    // this function splits fields on FIELD_SEPARATOR
    static String[] splitFields(String fields) {
        return fields.substring(FIELD_SEPARATOR.length()).split(FIELD_SEPARATOR);
    }

    static String[] getFields(String query) throws BadFormatException {
        if (!query.startsWith(GenStatus.DOC_LAYER))
            throw new BadFormatException("Query must start with " + GenStatus.DOC_LAYER);
        
        return splitFields(query.substring(GenStatus.DOC_LAYER.length()));
    }

    public static PreparedStatement getPreparedStatement(String query) throws Exception {
        String[] fields = getFields(query);


        if ( fields.length == 1 ) {
            String sql = "SELECT DISTINCT value_str as v FROM str_fs_props WHERE name = ?";
            PreparedStatement ps = DBManager.getConnection().prepareStatement(sql);
            ps.setString(1, fields[0]);
            return ps;
        }

        String sql = "SELECT DISTINCT JSON_EXTRACT(value, ?) as v FROM str_fs_props WHERE name = ?";
        PreparedStatement ps = DBManager.getConnection().prepareStatement(sql);
        String[] rest = Arrays.copyOfRange(fields, 1, fields.length);
        String jsonPath = "$." + String.join(".", rest);
        ps.setString(1, jsonPath);
        ps.setString(2, fields[0]);
        return ps;
    }
    
}
