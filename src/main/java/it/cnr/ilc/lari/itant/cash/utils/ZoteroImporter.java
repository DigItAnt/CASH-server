package it.cnr.ilc.lari.itant.cash.utils;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;

public class ZoteroImporter {

    private static final Logger log = LoggerFactory.getLogger(ZoteroImporter.class);

    public int importCSV(Reader in) throws Exception {
        Iterable<CSVRecord> records;

        Connection connection = DBManager.getNewConnection();
        connection.setAutoCommit(false);
        int numrecords = 0;

        try {

            connection.prepareStatement("DELETE FROM zotero").execute();

            // TODO read from conf file
            List<String> fieldsToSplit = List.of("Author", "Manual Tags");

            try {
                records = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withDelimiter(',')
                        .withQuote('"')
                        .parse(in);

                for (CSVRecord record : records) {
                    String fileid = record.get("Key").trim();

                    if (fileid.isEmpty())
                        continue;

                    numrecords++;

                    if ( numrecords % 1000 == 0 )
                        log.info("Zotero: imported {} records", numrecords);

                    for (Map.Entry<String, String> entry : record.toMap().entrySet()) {
                        String[] values;
                        String key = entry.getKey().trim();

                        if (fieldsToSplit.contains(entry.getKey()))
                            values = Arrays.stream(entry.getValue().split(";"))
                                        .map(String::trim)
                                        .toArray(String[]::new);
                        else
                            values = new String[] { entry.getValue().trim() };
                        for (String value : values) {
                            if (value.isEmpty())
                                continue;

                            PreparedStatement stmt = connection
                                    .prepareStatement("INSERT INTO zotero (fileid, `key`, value) VALUES (?, ?, ?)");
                            stmt.setString(1, fileid);
                            stmt.setString(2, key);
                            stmt.setString(3, value);
                            stmt.execute();

                        }
                    }
                }

            connection.commit();

            return numrecords;
            
            } catch (Exception e) {
                log.error("Error parsing CSV file", e);
                throw new InvalidParamException("cannot parse CSV file");
            }

        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            throw new InvalidParamException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }

    }



}
