package it.cnr.ilc.lari.itant.belexo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import it.cnr.ilc.lari.itant.belexo.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.belexo.exc.NodeNotFoundException;
import it.cnr.ilc.lari.itant.belexo.om.Annotation;
import it.cnr.ilc.lari.itant.belexo.om.FileInfo;
import it.cnr.ilc.lari.itant.belexo.om.Token;
import it.cnr.ilc.lari.itant.belexo.om.Annotation.Span;
import it.cnr.ilc.lari.itant.belexo.om.DocumentSystemNode.FileDirectory;
import it.cnr.ilc.lari.itant.belexo.utils.EpiDocTextExtractor;
import it.cnr.ilc.lari.itant.belexo.utils.StringUtils;
import it.cnr.ilc.lari.itant.belexo.utils.TextExtractorInterface;
import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo;
import it.cnr.ilc.lari.itant.belexo.utils.TokenInfo.TokenType;

public class DBManager {
    static ObjectMapper mapper = new ObjectMapper();
    public final static String TYPE_FOLDER = "D";
    public final static String TYPE_FILE = "F"; // this node represents a file
    public final static String TEXT_PROPERTY = "text"; // property of usntructured holding the extracted text
    public final static String BASE_FOLDER_NAME = "new-folder-";
    public final static String ORIGINAL_CONTENT = "original_content";
    public final static String CONTENT_TYPE = "content_type";
    public final static String TOKEN_POSITION_PROPERTY = "token_position"; // ordinal position of the token in the text
    public final static long NO_FATHER = -1;

    public final static String ENV_MYSQL_URL = "MYSQL_URL";
    public final static String ENV_MYSQL_USER = "MYSQL_USER";
    public final static String ENV_MYSQL_PASS = "MYSQL_PASSWORD";

    public final static String DB_SCHEMA_PATH = "/model/schema.sql";

    private static final Logger log = LoggerFactory.getLogger(DBManager.class);
    private static Connection connection;

    @Autowired
    static JdbcTemplate jdbcTemplate;

    public static void init() throws Exception {
        if ( jdbcTemplate == null ) {
            String mysqlUrl = "jdbc:mysql://localhost:3306/belexo";
            String mysqlUser = "test";
            String mysqlPassword = "test";
            if (System.getenv(ENV_MYSQL_URL) != null)
                mysqlUrl = System.getenv(ENV_MYSQL_URL);
            if (System.getenv(ENV_MYSQL_USER) != null)
                mysqlUser = System.getenv(ENV_MYSQL_USER);
            if (System.getenv(ENV_MYSQL_PASS) != null)
                mysqlPassword = System.getenv(ENV_MYSQL_PASS);
            log.info("Connecting to DB {}, user {}", mysqlUrl, mysqlUser);
            connection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
            log.info("connected to DB");
        } else {
            connection = jdbcTemplate.getDataSource().getConnection();
        }

        checkDBInitialized(connection);

        createRootNodeIfNeeded();
    }

    private static int getNumTablesInDB(Connection connection) throws Exception {
        String dbname = connection.getCatalog();
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(DISTINCT `TABLE_NAME`) AS numtables FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `table_schema` = ?");
        stmt.setString(1, dbname);
        ResultSet res = stmt.executeQuery();
        res.next();
        int numtables = res.getInt("numtables");
        return numtables;
    }

    private static void checkDBInitialized(Connection connection) throws Exception {
        if (getNumTablesInDB(connection) > 0) {
            log.info("DB already initialized");
            return;
        }
        log.info("DB is empty, need to be initialized");

        ScriptUtils.executeSqlScript(connection, new ClassPathResource(DB_SCHEMA_PATH));

        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("/model/functions.sql"));
        rdp.setSeparator(";;");
        rdp.populate(connection);

    }

    private synchronized static long insertFileInfo(FileInfo f) throws Exception {
        connection.setAutoCommit(false);
        long ret = 0;
        try {
            ret = insertFileInfoInternal(f);
        } catch ( Exception e ) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
        return ret;
    }

    private synchronized static long insertFileInfoInternal(FileInfo f) throws Exception {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO fsnodes (name, type, father) values (?,?,?);",
                                                                  Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, f.getName());
            stmt.setString(2, f.getTypeS());
            if ( f.getFather() < 0 ) {
                stmt.setNull(3, Types.NULL);
            } else stmt.setLong(3, f.getFather());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();  
            long ret = rs.next() ? rs.getLong(1) : 0;
            connection.commit();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public synchronized static void renameNode(long nodeId, String newName) throws Exception {
        connection.setAutoCommit(false);
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE fsnodes set name=? WHERE id=?");
            stmt.setString(1, newName);
            stmt.setLong(2, nodeId);
            stmt.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        
    }

    public synchronized static void removeNode(long nodeId) throws Exception {
        connection.setAutoCommit(false);
        try {
            FileInfo node = getNodeById(connection, nodeId);
            if ( node == null ) {
                throw new NodeNotFoundException();
            }
            if ( node.getFather() == NO_FATHER ) {
                log.error("Cannot remove root node!");
                throw new InvalidParamException();
            }
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM fsnodes WHERE id=?");
            stmt.setLong(1, nodeId);
            stmt.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        
    }

    public synchronized static void moveNode(long nodeId, long destination) throws Exception {
        connection.setAutoCommit(false);
        try {
            FileInfo node = getNodeById(connection, nodeId);
            if ( node == null ) {
                log.error("Cannot move non-existent node " + nodeId);
                throw new NodeNotFoundException();
            }
            if ( node.getFather() == NO_FATHER ) {
                log.error("Cannot move root node!");
                throw new InvalidParamException();
            }
            FileInfo dnode = getNodeById(connection, destination);
            if ( dnode == null ) {
                log.error("Cannot move to a non-existent node " + destination);
                throw new NodeNotFoundException();
            }
            if ( dnode.getType() != FileDirectory.directory ) {
                log.error("Destination node " + destination + " is not a directory!");
                throw new InvalidParamException();
            }
            if ( dnode.hasAncestor(nodeId) ) {
                log.error("Cannot move unto a descendant node!");
                throw new InvalidParamException();
            }
            if ( fileExists(destination, node.getName()) ) {
                log.error("A node of the same name already exists into the target!");
                throw new InvalidParamException();
            }
            
            PreparedStatement stmt = connection.prepareStatement("UPDATE fsnodes set father=? WHERE id=?");
            stmt.setLong(1, destination);
            stmt.setLong(2, nodeId);
            stmt.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        
    }

    public synchronized static FileInfo copyNode(long nodeId, long destination) throws Exception {
        try {
            FileInfo node = getNodeById(connection, nodeId);
            if ( node == null ) {
                log.error("Cannot copy non-existent node " + nodeId);
                throw new NodeNotFoundException();
            }
            if ( node.getType() != FileDirectory.file ) {
                log.error("Cannot copy a directory!");
                throw new InvalidParamException();
            }
            FileInfo dnode = getNodeById(connection, destination);
            if ( dnode == null ) {
                log.error("Cannot move to a non-existent node " + destination);
                throw new NodeNotFoundException();
            }
            if ( dnode.getType() != FileDirectory.directory ) {
                log.error("Destination node " + destination + " is not a directory!");
                throw new InvalidParamException();
            }
            if ( fileExists(destination, node.getName()) ) {
                log.error("A node of the same name already exists into the target!");
                throw new InvalidParamException();
            }
        
            node.setFather(destination);
            long newId = insertFileInfo(node);
            node.setElementId(newId);
            return node;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static int nValuesInMetadata(Map<String, Object> metadata) {
        int i = 0;
        for ( String k: metadata.keySet() ) {
            Object v = metadata.get(k);
            if ( v instanceof List ) i+=((List) v).size();
            else i++;
        }
        return i;
    }

    private synchronized static void OLD_replaceNodeMetadata(long elementId, Map<String, Object> metadata) throws Exception {
        connection.setAutoCommit(false);
        try {
            // remove the old metadata first, then update it. This should be done in a 'softer' way
            PreparedStatement stmt = connection.prepareStatement("delete from str_fs_props where node=? and meta=1");
            stmt.setLong(1, elementId);
            stmt.execute();
            stmt = connection.prepareStatement("INSERT INTO str_fs_props (name, value, node, meta) values " +
                                               StringUtils.n(nValuesInMetadata(metadata), "(?,?,?,1)", ","));
            int i = 1;
            for ( String k: metadata.keySet() ) {
                Object v = metadata.get(k);
                for ( Object value: (v instanceof List)?((List) v):Arrays.asList(new Object[]{v}) ) {
                    String toWrite = mapper.writeValueAsString(value);
                    stmt.setString(i++, k);
                    stmt.setString(i++, toWrite);
                    stmt.setLong(i++, elementId);
                }
            }
            stmt.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private synchronized static void replaceRowAttributes(long id, Map<String, Object> attributes,
                                                          String deleteQuery, String insertQuery, boolean inheritTransaction) throws Exception {
        if ( !inheritTransaction ) connection.setAutoCommit(false);
        try {
            // remove the old ATTRIBUTES first, then update it. This should be done in a 'softer' way
            PreparedStatement stmt = connection.prepareStatement(deleteQuery);
            stmt.setLong(1, id);
            stmt.execute();
            stmt = connection.prepareStatement(insertQuery);
            int i = 1;
            for ( String k: attributes.keySet() ) {
                Object v = attributes.get(k);
                for ( Object value: (v instanceof List)?((List) v):Arrays.asList(new Object[]{v}) ) {
                    String toWrite = mapper.writeValueAsString(value);
                    stmt.setString(i++, k);
                    stmt.setString(i++, toWrite);
                    stmt.setLong(i++, id);
                }
            }
            if ( i > 1 ) stmt.executeUpdate(); // otherwise where was nothing to insert!
            if (!inheritTransaction) connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (!inheritTransaction) connection.rollback();
        } finally {
            if ( !inheritTransaction) connection.setAutoCommit(true);
        }
    }

    public synchronized static void deleteNodeMetadata(long elementId) throws Exception {
        connection.setAutoCommit(false);
        try {
            // remove the old metadata first, then update it. This should be done in a 'softer' way
            PreparedStatement stmt = connection.prepareStatement("delete from str_fs_props where node=? and meta=1");
            stmt.setLong(1, elementId);
            stmt.execute();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    } 

    private static List mergeAttributesEntry(Object m1, Object m2) {
        // 4 cases: OO, OL, LO, LL: strings or objects we do not merge but make into a list
        if ( !(m1 instanceof List) && !(m2 instanceof List) )
        return new ArrayList(Arrays.asList(new Object[]{m1, m2}));
        if ( !(m2 instanceof List) ) { // m1 must be List
            ((List) m1).add(m2);
            return (List) m1;
        }
        if ( !(m1 instanceof List) ) { // m2 must be List
            List ret = new ArrayList(Arrays.asList(new Object[]{m1}));
            ret.addAll((List) m2);
            return ret;
        }
        // both lists
        ((List) m1).addAll((List) m2);
        return (List) m1;
    }
 
    public synchronized static void deleteRowAttributes(long id, String table) throws Exception {
        deleteRowAttributes(id, table, true);
    }

    public synchronized static void deleteRowAttributes(long id, String query, boolean autocommit) throws Exception {
        if ( !autocommit ) connection.setAutoCommit(false);
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setLong(1, id);
            stmt.execute();
            if ( !autocommit ) connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if ( !autocommit ) connection.rollback();
        } finally {
            if ( !autocommit ) connection.setAutoCommit(true);
        }
    } 

    protected static void updateRowAttributes(long rowId, Map<String, Object> attributes, Map<String, Object> newAttrs,
                                              String deleteQuery, String insertQuery,
                                              String extras, boolean inheritTransaction) throws Exception {
        try {
            log.info("Setting attributes for " + rowId);
            for (Map.Entry<String, Object> prop: newAttrs.entrySet()) {
                if ( !attributes.containsKey(prop.getKey()) )
                    attributes.put(prop.getKey(), prop.getValue());
                else { // must merge... huge PITA
                    attributes.put(prop.getKey(), mergeAttributesEntry(attributes.get(prop.getKey()), prop.getValue()));
                }
            }
            String iq = insertQuery + " values " + StringUtils.n(nValuesInMetadata(attributes), "(?,?,?" + extras + ")", ",");
            replaceRowAttributes(rowId, attributes, deleteQuery, iq, inheritTransaction);
            //logProperties(node);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }

    public static void updateNodeMetadata(long elementId, Map<String, Object> props) throws Exception {
        try {
            FileInfo node = getNodeById(connection, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            Map<String, Object> metadata = getNodeMetadata(elementId);

            updateRowAttributes(elementId, metadata, props, "delete from str_fs_props where node=? and meta=1",
                                                            "INSERT INTO str_fs_props (name, value, node, meta)", ",1", false);

            log.info("Properties set on node with id: " + elementId);
            //logProperties(node);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }

    public static FileInfo getRootNode() throws Exception {
        log.info("Trying to get root node ");
        PreparedStatement stmt = connection.prepareStatement("SELECT id, name, type from fsnodes where father IS NULL");
        ResultSet res = stmt.executeQuery();
        FileInfo ret = null;
        while ( res.next() ) {
            log.info("Found.");
            ret = new FileInfo();
            ret.setElementId(res.getLong("id"));
            ret.setName(res.getString("name"));
            ret.setType(res.getString("type"));
        }
        return ret;
    }

    public static long getRootNodeId() throws Exception {
        return getRootNode().getElementId();
    }

    public static FileInfo getNodeById(long nodeId) throws Exception {
        return getNodeById(connection, nodeId);
    }

    protected static FileInfo getNodeById(Connection connection, long nodeId) throws Exception {
        log.info("Trying to get node " + nodeId);
        PreparedStatement stmt = connection.prepareStatement("SELECT id, name, type, father from fsnodes where id=?");
        stmt.setLong(1, nodeId);
        ResultSet res = stmt.executeQuery();
        FileInfo ret = null;
        while ( res.next() ) {
            log.info("Found.");
            ret = new FileInfo();
            ret.setElementId(res.getLong("id"));
            ret.setName(res.getString("name"));
            ret.setType(res.getString("type"));
            long father = res.getLong("father");
            ret.setFather(res.wasNull()?NO_FATHER:father);
        }
        return ret;
    }
 
    public static List<FileInfo> getNodeChildren(long nodeId) throws Exception {
	    PreparedStatement stmt = connection.prepareStatement("SELECT id, name, type from fsnodes where father=?");
	    stmt.setLong(1, nodeId);
	    ResultSet res = stmt.executeQuery();
	    List<FileInfo> ret = new java.util.ArrayList<FileInfo>();
	    while ( res.next() ) {
	        FileInfo fi = new FileInfo();
	        fi.setElementId(res.getLong("id"));
	        fi.setName(res.getString("name"));
            fi.setType(res.getString("type"));
            fi.setFather(nodeId);
	        ret.add(fi);
        }
        return ret;
    }

    private synchronized static void createRootNodeIfNeeded() throws Exception {
        try {
            if (getRootNode() != null) {
                log.info("repo already initialized");
                return;
            }

            String name = "root";
            FileInfo newfolder = new FileInfo();
            newfolder.setName(name);
            newfolder.setPath("/root");
            newfolder.setFather(NO_FATHER);
            newfolder.setType(FileDirectory.directory);
            long newid = insertFileInfo(newfolder);
            log.info("Created /root" + ", id: " + newid);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static boolean fileExists(long parent, String name) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("SELECT * from fsnodes where father=? and name=? LIMIT 1");
        stmt.setLong(1, parent);
        stmt.setString(2, name);
        //log.info("PS: " + stmt.toString());
        ResultSet rSet = stmt.executeQuery();
        if ( rSet.next() ) return true;
        return false;
    }

    public static String getNewFolderName(long parent) throws Exception {
        int tmp = 1;
        String name = null;
        while ( true ) {
            name = BASE_FOLDER_NAME + tmp;
            if (!fileExists(parent, name))
                break;
            tmp += 1;
        }
        return name;
    }

    public synchronized static long addFolder(long parent) throws Exception {
        connection.setAutoCommit(false);
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO fsnodes (name, father, type) values (?,?,?)",
                                                                  Statement.RETURN_GENERATED_KEYS);
            if ( parent == 0 ) parent = getRootNodeId();
            stmt.setString(1, getNewFolderName(parent));
            stmt.setLong(2, parent); // it's a first level folder
            stmt.setString(3, TYPE_FOLDER);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();  
            long ret = rs.next() ? rs.getLong(1) : 0;            
            connection.commit();
            return ret;
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static Map<String, Object> OLD__getNodeMetadata(long nodeId) throws Exception {
        log.info("Getting metadata for " + nodeId);
        PreparedStatement stmt = connection.prepareStatement("SELECT name, value from str_fs_props where node=? and meta=1");
        stmt.setLong(1, nodeId);
        ResultSet rs = stmt.executeQuery();
        Map<String, Object> ret = new HashMap<String, Object>();
        while ( rs.next() ) {
            String key = rs.getString("name");
            Object value = mapper.readValue(rs.getString("value"), Object.class); 
            log.info("Type of value " + value.getClass().getName());
            //String value = rs.getString("value");
            if ( !ret.containsKey(key) )
                ret.put(key, value);
            else { // we aleady have a value. A string or a List<String> already?
                Object entry = ret.get(key);
                log.info("Type of entry " + entry.getClass().getName());
                if ( !(entry instanceof List) ) { // create a List
                    log.info("Generating new list");
                    ret.put(key, new ArrayList<Object>(Arrays.asList(new Object[]{entry, value})));
                } else {// add to the list
                    log.info("Adding to the list");
                    ((ArrayList<Object>) entry).add(value);
                }
            }
        }
        log.info("Metadata found: " + ret.keySet().toString());
        return ret;
    }

    public static Map<String, Object> getNodeMetadata(long nodeId) throws Exception {
        return getRowAttributes(nodeId, "SELECT name, value from str_fs_props where node=? and meta=1");
    }

    private static Map<String, Object> getRowAttributes(long id, final String query) throws Exception {
        log.info("Getting metadata for " + id + " in " + query);
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();
        Map<String, Object> ret = new HashMap<String, Object>();
        while ( rs.next() ) {
            String key = rs.getString("name");
            Object value = mapper.readValue(rs.getString("value"), Object.class); 
            log.info("Type of value " + ((value == null)?"null":value.getClass().getName()));
            //String value = rs.getString("value");
            if ( !ret.containsKey(key) )
                ret.put(key, value);
            else { // we aleady have a value. A string or a List<String> already?
                Object entry = ret.get(key);
                log.info("Type of entry " + entry.getClass().getName());
                if ( !(entry instanceof List) ) { // create a List
                    log.info("Generating new list");
                    ret.put(key, new ArrayList<Object>(Arrays.asList(new Object[]{entry, value})));
                } else {// add to the list
                    log.info("Adding to the list");
                    ((ArrayList<Object>) entry).add(value);
                }
            }
        }
        log.info("Metadata found: " + ret.keySet().toString());
        return ret;
    }

    public static Map<String, Object> getAnnotationAttributes(long annId) throws Exception {
        return getRowAttributes(annId, "select name, value from str_ann_props where ann=?");
    }

    public static List<Annotation.Span> getAnnotationSpans(long annid) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("SELECT  begin,end from ann_spans where ann=?");
        stmt.setLong(1, annid);
        ResultSet rs = stmt.executeQuery();
        List<Annotation.Span> ret = new ArrayList<Annotation.Span>();
        while ( rs.next() ) {
            Annotation.Span span = new Annotation.Span();
            span.setStart(rs.getInt("begin"));
            span.setEnd(rs.getInt("end"));
            ret.add(span);
        }
        return ret;
    }

    private static void saveFileData(FileInfo node, String contentType, InputStream contentStream) throws Exception {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO blob_fs_props (name,value,content_type,node) values (?,?,?,?)");
            stmt.setString(1, ORIGINAL_CONTENT);
            stmt.setBlob(2, contentStream);
            stmt.setString(3, contentType);
            stmt.setLong(4, node.getElementId());
            stmt.execute();
    }

    private static long insertTextEntry(long nodeId, String text, String tType) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO unstructured (text,node,type) values (?,?,?)",
                                                             Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, text);
        stmt.setLong(2, nodeId);
        stmt.setString(3, tType);
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        return rs.next()?rs.getLong(1):0;
    }

    private static long insertTokenNode(long nodeId, long srcTxt,
                                       String token, int position, int begin, int end,
                                       String xmlid, boolean imported) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO tokens (text,node,srctxt,position,begin,end,xmlid,imported) values (?,?,?,?,?,?,?,?)",
                                                             Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, token);
        stmt.setLong(2, nodeId);
        stmt.setLong(3, srcTxt);
        stmt.setInt(4, position);
        stmt.setInt(5, begin);
        stmt.setInt(6, end);
        if ( xmlid != null ) stmt.setString(7, xmlid);
        else stmt.setNull(7, Types.VARCHAR);
        stmt.setBoolean(8, imported);
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        return rs.next()?rs.getLong(1):0;
    }

    @Transactional
    public synchronized static long addFile(long parentId, String filename, InputStream contentStream, String contentType) throws Exception {
        log.info("Creating file under parent " + parentId);
        FileInfo node = null;
        connection.setAutoCommit(false);
        try {
            if ( parentId == 0 ) parentId = getRootNodeId();
            if ( fileExists(parentId, filename) ) {
                log.error("A file with the same name already exists in this directory");
                throw new InvalidParamException();
            }
            if ( parentId > 0 && getNodeById(parentId) == null ) {
                log.error("Specified parent directory does not exist");
                throw new InvalidParamException();
            }
            byte[] contentBytes = contentStream.readAllBytes();
            node = new FileInfo();
            node.setFather(parentId);
            node.setName(filename);
            node.setType(FileDirectory.file);
            long nid = insertFileInfoInternal(node);
            node.setElementId(nid);

            // add original content to node
            saveFileData(node, contentType, contentStream);

            if ( filename.endsWith(".xml") ) { // TODO: perhaps do better, here!
                log.info("MYID: " + nid);
                ByteArrayInputStream bais = new ByteArrayInputStream(contentBytes);
                TextExtractorInterface extractor = new EpiDocTextExtractor();
                String text = extractor.read(bais).extract(); // must read the bytes here...
                long srcTxt = insertTextEntry(nid, text, "interpretative");
                log.info("Added text");
                int ti = 1;
                for (TokenInfo token: extractor.tokens() ) { // adds tokens
                    if ( token.tokenType != TokenType.WORD ) continue;
                    long tid = insertTokenNode(nid, srcTxt, token.text, ti++, token.begin, token.end, token.xmlid, token.imported);
                    log.info("Added token node " + tid);
                }
                for (Annotation ann: extractor.annotations()) { // adds annotations
                    long aid = insertAnnotationInternal(nid, ann);
                    ann.setID(aid);
                    // save spans
                    addAnnotationSpans(aid, ann.getSpans());
                    // save attributes
                    addAnnotationAttributes(aid, ann.getAttributes());
                    log.info("Added annotation " + aid);
                }
            }

            connection.commit();
            return nid;
        } catch (Exception e) {
            connection.rollback();
            log.error(e.toString());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static String printNode(long nodeId) throws Exception {
        FileInfo node = getNodeById(nodeId);
        return node.printAll();
    }

    public static String getNodePath(long nodeId) throws Exception {
        List<String> path = new ArrayList<String>();
        boolean toRoot = false;
        PreparedStatement stmt = connection.prepareStatement("select name, father from fsnodes where id=?");
        while ( !toRoot ) {
            stmt.setLong(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                path.add(rs.getString("name"));
                nodeId = rs.getLong("father");
                toRoot = rs.wasNull();
            } else toRoot = true; 
        }
        Collections.reverse(path);
        return "/" + String.join("/", path);
    }

    public static FileInfo findByAbsolutePath(String path) throws Exception {
        String[] elements = path.strip().split("/");
        if ( !elements[0].equals("root") ) throw new InvalidParamException();
        long nodeId = getRootNodeId();
        int i = 1;
        while ( i < elements.length ) {
            PreparedStatement stmt = connection.prepareStatement("SELECT id from fsnodes where father=? and name=?");
            stmt.setLong(1, nodeId);
            stmt.setString(2, elements[i++]);
            ResultSet rs = stmt.executeQuery();
            if ( !rs.next() ) throw new NodeNotFoundException();
            nodeId = rs.getLong("id");
        }
        return getNodeById(nodeId);
    }

    /**
     * This is mostly for testing a query...
     * @param query
     * @return
     */
    public static List<Long> findNodesByTextQuery(String query) throws Exception {
        String[] tokens = query.split(" ");
        int ntokens = tokens.length;

        String sql = "SELECT DISTINCT n.id from fsnodes n";
        for ( int ti = 1; ti <= ntokens; ti++ ) {
            sql += ", tokens t" + ti + " ";
        }
        sql += "WHERE ";
        for (int ti = 1; ti <= ntokens; ti++ ) {
            String t = "t" + ti;
            if ( ti > 1 ) sql += " AND ";
            sql += t + ".node=n.id AND ";
            sql += t + ".text='" + StringUtils.sqlEscapeString(tokens[ti-1]) + "'";
            if ( ti > 1 )
                sql += " AND " + t + ".position=t" + (ti-1) + ".position+1";
        }
        log.info("Search query: " + sql);
        PreparedStatement stmt = connection.prepareStatement(sql); // actually no params
        ResultSet rs = stmt.executeQuery();
        ArrayList<Long> ret = new ArrayList<Long>();
        while ( rs.next() )
            ret.add(rs.getLong("n.id"));
        return ret;
    }

    // @TODO: there are missing columns here
    protected static Annotation getAnnotationById(Connection connection, long annId) throws Exception {
        log.info("Trying to get annotation " + annId);
        PreparedStatement stmt = connection.prepareStatement("SELECT id, layer, value, imported FROM annotations where id=?");
        stmt.setLong(1, annId);
        ResultSet res = stmt.executeQuery();
        Annotation ret = null;
        while ( res.next() ) {
            log.info("Found.");
            ret = new Annotation();
            ret.setID(res.getLong("id"));
            ret.setLayer(res.getString("layer"));
            ret.setValue(res.getString("value"));
            ret.setImported(res.getBoolean("imported"));
        }
        return ret;
    }

    private static String TOKENS_QUERY = "" + 
    "SELECT t.id, t.text, position, begin, end, xmlid, u.type, imported " +
    "FROM tokens t, unstructured u WHERE t.srctxt=u.id and t.node=? ORDER BY t.position";

    public static List<Token> getNodeTokens(long nodeId) throws Exception {
        log.info("Trying to get tokens of node " + nodeId);
        PreparedStatement stmt = connection.prepareStatement(TOKENS_QUERY);
        stmt.setLong(1, nodeId);
        ResultSet res = stmt.executeQuery();
        List<Token> ret = new ArrayList<Token>();
        while ( res.next() ) {
            log.info("Found.");
            Token tok = new Token();
            tok.setID(res.getLong("t.id"));
            tok.setNode(nodeId);
            tok.setText(res.getString("t.text"));
            tok.setPosition(res.getInt("position"));
            tok.setBegin(res.getInt("begin"));
            tok.setEnd(res.getInt("end"));
            tok.setXmlid(res.getString("xmlid"));
            tok.setSource(res.getString("u.type"));
            tok.setImported(res.getBoolean("imported"));
            ret.add(tok);
        }
        return ret;
    }

    // @TODO: there are missing columns here
    public static List<Annotation> getNodeAnnotations(long nodeId) throws Exception {
        log.info("Trying to get annotations of node " + nodeId);
        PreparedStatement stmt = connection.prepareStatement("SELECT id, layer, value, imported FROM annotations where node=?");
        stmt.setLong(1, nodeId);
        ResultSet res = stmt.executeQuery();
        List<Annotation> ret = new ArrayList<Annotation>();
        while ( res.next() ) {
            log.info("Found.");
            Annotation ann = new Annotation();
            ann.setID(res.getLong("id"));
            ann.setLayer(res.getString("layer"));
            ann.setValue(res.getString("value"));
            ann.setImported(res.getBoolean("imported"));
            ret.add(ann);
        }

        return ret;
    }

    public static Annotation getAnnotationById(long annId) throws Exception {
        return getAnnotationById(connection, annId);
    }

    // @TODO: Should not return just the first one
    public static String getNodeText(long nodeId) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("SELECT text FROM unstructured where node=? limit 1");
        stmt.setLong(1, nodeId);
        ResultSet res = stmt.executeQuery();
        while ( res.next() )
            return res.getString(1);
        return null;
    }

    /*
    private synchronized static long insertAnnotaton(long nodeId, Annotation ann) throws Exception {
        connection.setAutoCommit(false);
        long ret = 0;
        try {
            ret = insertAnnotationInternal(nodeId, ann);
        } catch ( Exception e ) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
        return ret;
    }
    */

    private synchronized static long insertAnnotationInternal(long nodeId, Annotation ann) throws Exception {
        try {
            PreparedStatement stmt;
            int coli = 1;
            if ( ann.getID() <= 0 ) {
                stmt = connection.prepareStatement("INSERT INTO annotations (layer, value, node, created, imported) values (?,?,?,?,?);",
                                                                    Statement.RETURN_GENERATED_KEYS);
            } else {
                stmt = connection.prepareStatement("INSERT INTO annotations (id,layer, value, node, created, imported) values (?,?,?,?,?,?);");
                stmt.setLong(coli++, ann.getID());
            }
            stmt.setString(coli++, ann.getLayer());
            stmt.setString(coli++, ann.getValue());
            stmt.setLong(coli++, nodeId);
            java.util.Date date = new java.util.Date();
            stmt.setTimestamp(coli++, new java.sql.Timestamp(date.getTime()));
            stmt.setBoolean(coli++, ann.getImported());
            stmt.executeUpdate();
            long ret = ann.getID();
            if ( ann.getID() <= 0 ) {
                ResultSet rs = stmt.getGeneratedKeys();  
                ret = rs.next() ? rs.getLong(1) : 0;
            }
            connection.commit();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected synchronized static void addAnnotationSpans(long annid, List<Annotation.Span> spans) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO ann_spans (ann, begin, end) values " +
                                                             StringUtils.n(spans.size(), "(?,?,?)", ","));
        int i = 1;
        for ( Annotation.Span span: spans ) {
            stmt.setLong(i++, annid);
            stmt.setInt(i++, span.getStart());
            stmt.setInt(i++, span.getEnd());
        }
        stmt.execute();
    }

    protected synchronized static void addAnnotationAttributes(long annid, Map<String, Object> attributes) throws Exception {
        try {
            updateRowAttributes(annid, new HashMap<String, Object>(),
                                attributes, "delete from str_ann_props where ann=?",
                                            "INSERT INTO str_ann_props (name, value, ann)", "", true);

            log.info("Properties set on annotation with id: " + annid);
            //logProperties(node);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }


    @Transactional
    public synchronized static Annotation addAnnotation(long nodeId, Annotation ann) throws Exception {
        log.info("Creating annotation for node " + nodeId);
        FileInfo node = getNodeById(nodeId);
        if ( node == null ) {
            log.error("Invalid node " + nodeId);
            throw new InvalidParamException();
        }
        if ( ann.spansOverlap() ) {
            log.error("Spans overlap!" + Strings.join(ann.getSpans(), ';'));
            throw new InvalidParamException();
        }
        connection.setAutoCommit(false);
        try {
            long annid = insertAnnotationInternal(nodeId, ann);
            ann.setID(annid);
            log.info("ANN1");
            // save spans
            addAnnotationSpans(annid, ann.getSpans());
            log.info("ANN2");

            // save attributes
            addAnnotationAttributes(annid, ann.getAttributes());
            log.info("ANN3");

            connection.commit();
            log.info("ANN4");

            return ann;
        } catch (Exception e) {
            log.error(e.toString());
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public synchronized static void deleteAnnotation(long annid) throws Exception {
        log.info("Deleting annotation " + annid);
        getAnnotationById(annid);
        PreparedStatement stmt = connection.prepareStatement("delete from annotations where id=?");
        stmt.setLong(1, annid);
        stmt.execute();
    }

    public static long getAnnotationNodeId(long annid) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("select node from annotations where id=?");
        stmt.setLong(1, annid);
        ResultSet res = stmt.executeQuery();
        while ( res.next() )
            return res.getLong(1);
        log.error("Annotation " + annid + " not found");
        throw new InvalidParamException();
    }
}
