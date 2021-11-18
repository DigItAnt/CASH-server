package it.cnr.ilc.lari.itant.belexo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import it.cnr.ilc.lari.itant.belexo.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.belexo.exc.NodeNotFoundException;
import it.cnr.ilc.lari.itant.belexo.om.FileInfo;
import it.cnr.ilc.lari.itant.belexo.om.DocumentSystemNode.FileDirectory;
import it.cnr.ilc.lari.itant.belexo.utils.FakeTextExtractor;
import it.cnr.ilc.lari.itant.belexo.utils.StringUtils;
import it.cnr.ilc.lari.itant.belexo.utils.TextExtractorInterface;

public class DBManager {
    public final static String TYPE_FOLDER = "D";
    public final static String TYPE_FILE = "F"; // this node represents a file
    public final static String TEXT_PROPERTY = "text"; // property of usntructured holding the extracted text
    public final static String BASE_FOLDER_NAME = "new-folder-";
    public final static String ORIGINAL_CONTENT = "original_content";
    public final static String CONTENT_TYPE = "content_type";
    public final static String TOKEN_POSITION_PROPERTY = "token_position"; // ordinal position of the token in the text
    public final static long NO_FATHER = -1;

    private static final Logger log = LoggerFactory.getLogger(DBManager.class);
    private static Connection connection;

    @Autowired
    static JdbcTemplate jdbcTemplate;

    public static void init() throws Exception {
        if ( jdbcTemplate == null ) {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/belexo", "test", "test");
        } else {
            connection = jdbcTemplate.getDataSource().getConnection();
        }
        createRootNodeIfNeeded();
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

    private synchronized static void replaceNodeMetadata(long elementId, Map<String, String> metadata) throws Exception {
        connection.setAutoCommit(false);
        try {
            // remove the old metadata first, then update it. This should be done in a 'softer' way
            PreparedStatement stmt = connection.prepareStatement("delete from str_fs_props where node=? and meta=1");
            stmt.setLong(1, elementId);
            stmt.execute();
            stmt = connection.prepareStatement("INSERT INTO str_fs_props (name, value, node, meta) values " + StringUtils.n(metadata.size(), "(?,?,?,1)", ","));
            int i = 1;
            for ( String k: metadata.keySet() ) {
                stmt.setString(i++, k);
                stmt.setString(i++, metadata.get(k));
                stmt.setLong(i++, elementId);
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
    
    public static void updateNodeMetadata(long elementId, Map<String, String> props) throws Exception {
        try {
            FileInfo node = getNodeById(connection, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            Map<String, String> metadata = getNodeMetadata(elementId);

            log.info("Setting node metadata for " + elementId);
            for (Map.Entry<String, String> prop: props.entrySet()) {
                metadata.put(prop.getKey(), prop.getValue());
            }
            replaceNodeMetadata(elementId, metadata);

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
            ret.setElementId((int) res.getLong("id"));
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
            ret.setElementId((int) res.getLong("id"));
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
            stmt.setString(1, getNewFolderName(parent));
            stmt.setLong(2, parent);
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

    public static Map<String, String> getNodeMetadata(long nodeId) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("SELECT name, value from str_fs_props where node=? and meta=1");
        stmt.setLong(1, nodeId);
        ResultSet rs = stmt.executeQuery();
        Map<String, String> ret = new HashMap<String, String>();
        while ( rs.next() ) {
            ret.put(rs.getString("name"), rs.getString("value"));
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

    private static void insertTextEntry(long nodeId, String text) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO unstructured (text,node) values (?,?)");
        stmt.setString(1, text);
        stmt.setLong(2, nodeId);
        stmt.execute();
    }

    private static int insertTokenNode(long nodeId, String token, int position, int begin, int end) throws Exception {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO tokens (text,node,position,begin,end) values (?,?,?,?,?)");
        stmt.setString(1, token);
        stmt.setLong(2, nodeId);
        stmt.setInt(3, position);
        stmt.setInt(4, begin);
        stmt.setInt(5, end);
        int ret = stmt.executeUpdate();
        return ret;
    }


    public synchronized static long addFile(long parentId, String filename, InputStream contentStream, String contentType) throws Exception {
        log.info("Creating file under parent " + parentId);
        FileInfo node = null;
        connection.setAutoCommit(false);
        try {
            if ( fileExists(parentId, filename) ) {
                log.error("A file with the same name already exists in this directory");
                throw new InvalidParamException();
            }
            //byte[] contentBytes = contentStream.readAllBytes();
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
                TextExtractorInterface extractor = new FakeTextExtractor(); // TODO: replace with actual extractor
                String text = String.join(" ", extractor.extract((InputStream) null)); // must read the bytes here...
                insertTextEntry(nid, text);
                log.info("Added text");
                int ti = 1;
                int begin = 0;
                int end = -1;
                for (String token: text.split(" ")) {
                    begin = end + 1; // TODO This assumes a space
                    end = begin + token.length();
                    long tid = insertTokenNode(nid, token, ti++, begin, end);
                    log.info("Added token node " + tid);
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

}
