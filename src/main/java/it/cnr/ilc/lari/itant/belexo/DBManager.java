package it.cnr.ilc.lari.itant.belexo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import it.cnr.ilc.lari.itant.belexo.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.belexo.exc.NodeNotFoundException;
import it.cnr.ilc.lari.itant.belexo.om.FileInfo;
import it.cnr.ilc.lari.itant.belexo.om.DocumentSystemNode.FileDirectory;
import it.cnr.ilc.lari.itant.belexo.utils.FakeTextExtractor;
import it.cnr.ilc.lari.itant.belexo.utils.NodeTypeRegister;
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
    private static long startFrom = 1;

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



    /*

    public static Repository getRepository() {
        return repository;
    }

    public static Repository start() throws Exception {
        Repository repository;

        //String xml = "/path/to/repository/configuration.xml";
        String dir = "/tmp/rabbit";
        //repository = new TransientRepository();
        RepositoryConfig config = RepositoryConfig.create(new File(dir));
        //RepositoryConfig config = RepositoryConfig.create(xml, dir);
        repository = RepositoryImpl.create(config);
        log.warn("Registering node types");
        NodeTypeRegister.registerTypes(getSession());
        return repository;
    }

    public static void stop(Repository repository) {
        ((RepositoryImpl) repository).shutdown();
    }


    public static Session getSession() throws LoginException, RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        return session;
    }

    public static FileInfo getNodeById(long nodeId) throws Exception {
        return getNodeById(connection, nodeId);
    }

    public static synchronized long getNewId(Session session) throws Exception {
        while ( true ) {
            if (getNodeById(session, startFrom) == null)
                break;
            startFrom += 1;
        }
        return startFrom;
    }

    public static boolean isDirectory(Session session, long nodeId) throws Exception {
        //if (nodeId == ROOT_ID) return true;
        Node node = getNodeById(session, nodeId);
        if (node == null) return false;
        return ( node.getProperty(MYTYPE).getString().equals(TYPE_FOLDER) );
    }

    public static boolean fileExists(Node parent, String filename) throws Exception {
        try {
            parent.getNode(filename);
        } catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    public static String getNewFolderName(Node parent) throws Exception {
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

    public synchronized static int addFolder(long parentId) throws Exception {
        log.info("Creating folder under parent " + parentId);
        return addNode(parentId, null, TYPE_FOLDER);
    }

    protected static void setFileData(Node node, String contentType, byte[] content) throws Exception {
        node.setProperty(ORIGINAL_CONTENT, new BinaryImpl(content));
        node.setProperty(CONTENT_TYPE, contentType);
    }

    public synchronized static int addFile(long parentId, String filename, InputStream contentStream, String contentType) throws Exception {
        log.info("Creating file under parent " + parentId);
        // The file has been added as a node. We should now fill its content and import it.
        Session session = null;
        Node node = null;

        try {
            session = getSession();
            byte[] contentBytes = contentStream.readAllBytes();
            node = addNodeInternal(session, parentId, filename, TYPE_FILE);

            // add original content to node
            setFileData(node, contentType, contentBytes);

            session.save();
            long nid = node.getProperty(MYID).getLong();
            if ( filename.endsWith(".xml") ) { // TODO: perhaps do better, here!
                log.info("MYID: " + nid);
                Node structured = addInternalNode(session, node, "structure", TYPE_STRUCTURE);
                structured.setProperty(FILEREF, nid);
                log.info("Added content under: " + structured.getPath());
                Node unstructured = addInternalNode(session, node, "unstructured",
                                                    TYPE_UNSTRUCTURED);
                unstructured.setProperty(FILEREF, nid);
                TextExtractorInterface extractor = new FakeTextExtractor(); // TODO: replace with actual extractor
                String text = String.join(" ", extractor.extract(unstructured));
                unstructured.setProperty(TEXT_PROPERTY, text);
                log.info("Added text under: " + unstructured.getPath());
                int ti = 1;
                for (String token: text.split(" ")) {
                    Node tokenNode = unstructured.addNode("token" + ti, PTYPE_TOKEN);
                    tokenNode.setProperty(FILEREF, nid);
                    tokenNode.setProperty(TEXT_PROPERTY, token);
                    tokenNode.setProperty(TOKEN_POSITION_PROPERTY, ti++);
                    log.info("Added token node " + tokenNode.getPath());
                }
                session.save();                
                session.importXML(structured.getPath(), new ByteArrayInputStream(contentBytes), ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);                
            }

            session.save();
        } catch (Exception e) {
            if (node != null)
                removeNode(node.getProperty(MYID).getLong());
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }

        return 0;
    }

    // TODO: this one should wrap addNodeInternal instead
    public synchronized static int addNode(long parentId, String nodename, String nodetype) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Creating node under parent " + parentId);
            Node parent = getNodeById(session, parentId);
            if (parent == null)
                throw new NodeNotFoundException();

            if (!isDirectory(session, parentId)) {
                log.error("destination is not a directory");
                throw new InvalidParamException();
            }
            
            long newid = getNewId(session);
            String name = nodename;
            if (name == null)
                name = getNewFolderName(parent);
            if (fileExists(parent, name))
                throw new InvalidParamException();

            String ptype = typeToPtype(nodetype);

            Node newfolder = (ptype == null)?parent.addNode(name):parent.addNode(name, ptype);
            newfolder.setProperty(MYID, newid);
            newfolder.setProperty(MYTYPE, nodetype);
            session.save();
            log.info("Created node " + name + ", id: " + newid);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
        return 0;
    }

    protected synchronized static Node addNodeInternal(Session session, long parentId, String nodename, String nodetype) throws Exception {
        return addNodeInternal(session, parentId, nodename, nodetype, false);
    }

    protected synchronized static Node addNodeInternal(Session session, long parentId, String nodename, String nodetype,
                                                       boolean skipCheckDir) throws Exception {
        Node newNode = null;
        try {
            log.info("Creating node under parent " + parentId);
            Node parent = getNodeById(session, parentId);
            if (parent == null)
                throw new NodeNotFoundException();

            if (!skipCheckDir && !isDirectory(session, parentId)) {
                log.error("destination is not a directory");
                throw new InvalidParamException();
            }
    
            long newid = getNewId(session);
            String name = nodename;
            if (name == null)
                name = getNewFolderName(parent);
            if (fileExists(parent, name)) {
                log.error("A file of the same name already exists in parent: " + parent.getPath() + " " + name);
                throw new InvalidParamException();
            }
            String ptype = typeToPtype(nodetype);

            newNode = (ptype == null)?parent.addNode(name):parent.addNode(name, ptype);
            newNode.setProperty(MYID, newid);
            newNode.setProperty(MYTYPE, nodetype);
            log.info("Created node " + name + ", id: " + newid);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
        return newNode;
    }

    protected synchronized static Node addInternalNode(Session session, Node parent, String nodename, String nodetype) throws Exception {
        Node newNode = null;
        try {
            log.info("Creating node under parent " + (parent != null?parent.toString():"<NULL>"));
            if (parent == null)
                throw new NodeNotFoundException();
    
            String name = nodename;
            String ptype = typeToPtype(nodetype);

            newNode = (ptype == null)?parent.addNode(name):parent.addNode(name, ptype);
            newNode.setProperty(MYTYPE, nodetype);
            log.info("Created node " + name);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
        return newNode;
    }


    public synchronized static void removeNode(long elementId) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Removing node " + elementId);
            if (elementId == ROOT_ID) {
                log.error("Cannot remove root node!");
                throw new InvalidParamException();
            }
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();
            String name = node.getName();
            node.remove();
            session.save();
            log.info("Removed node " + name + ", id: " + elementId);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

    public synchronized static void renameNode(long elementId, String newname) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Renaming name " + elementId + " with name " + newname);
            if ( newname.contains("/") ) {
                log.error("Node name cannot contain '/'");
                throw new InvalidParamException();
            }
            
            //if (elementId == ROOT_ID)
            //    throw new ForbiddenException();
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();
            String newpath = node.getParent().getPath();
            if (!newpath.endsWith("/"))
                newpath += "/";
            newpath += newname;
            
            boolean nameOk = false;
            try {
                node.getParent().getNode(newname);
            } catch(PathNotFoundException e) {
                nameOk = true;
            }
            if (!nameOk) {
                log.error("The name " + newname + " is not valid");
                throw new InvalidParamException();
            }
            node.getSession().move(node.getPath(), newpath);
        
            session.save();
            log.info("Renamed node " + newname + ", id: " + elementId);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

    public synchronized static void moveNode(long elementId, int destId) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Moving node " + elementId + " under " + destId);
            if (elementId == ROOT_ID) {
                log.error("You cannot move the root node!");
                throw new InvalidParamException();
            }
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            Node parent = getNodeById(session, destId);
            if (parent == null) {
                log.error("cannot move file: parent not found");
                throw new NodeNotFoundException();
            }

            if (!isDirectory(session, destId)) {
                log.error("destination is not a directory");
                throw new InvalidParamException();
            }

            String newpath = parent.getPath();
            if (!newpath.endsWith("/"))
                newpath += "/";
            newpath += node.getName();

            boolean nameOk = false;
            try {
                parent.getNode(node.getName());
            } catch (PathNotFoundException e) {
                nameOk = true;
            }
            if (!nameOk) {
                log.error("cannot move file: name already present");
                throw new InvalidParamException();
            }

            try {
                node.getSession().move(node.getPath(), newpath);
            } catch (RepositoryException e) {
                throw new InvalidParamException();
            }

            session.save();
            log.info("Moved node " + node.getName() + ", id: " + elementId);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

    public synchronized static void copyNode(long elementId, long destId) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Copying node " + elementId + " under " + destId);
            if (elementId == ROOT_ID)
                throw new InvalidParamException();
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            if (isDirectory(session, elementId)) {
                log.error("node is not a file: " + elementId);
                throw new InvalidParamException();
            }

            Node root = session.getRootNode();
            Node parent = getNodeById(session, destId);
            if (parent == null)
                throw new NodeNotFoundException();

            if (!isDirectory(session, destId)) {
                log.error("destination is not a directory");
                throw new InvalidParamException();
            }

            String newpath = parent.getPath();
            if (!newpath.endsWith("/"))
                newpath += "/";
            newpath += node.getName();

            boolean nameOk = false;
            try {
                parent.getNode(node.getName());
            } catch (PathNotFoundException e) {
                nameOk = true;
            }
            if (!nameOk) {
                log.error("cannot move file: name already present");
                throw new InvalidParamException();
            }


            Workspace workspace = session.getWorkspace(); 

            try {
                workspace.copy(node.getPath(), newpath);
                String relpath = newpath.substring(1); // remove /
                root.getNode(relpath).setProperty(MYID, getNewId(session));
            } catch (RepositoryException e) {
                throw new InvalidParamException();
            }

            session.save();
            log.info("Copied node " + node.getName() + ", id: " + elementId);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }


    public synchronized static void updateNodeMetadata(long elementId, Map<String, String> props) throws Exception {
        Session session = null;
        try {
            session = getSession();

            // TODO occhio injection
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            log.info("Setting node metadata for " + elementId);
            for (Map.Entry<String, String> prop: props.entrySet()) {
                String internalName = META_PFIX + prop.getKey();
                node.setProperty(internalName, prop.getValue());
            }
            session.save();
            log.info("Properties set on node with id: " + elementId);
            //logProperties(node);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

    public synchronized static void deleteNodeMetadata(long elementId) throws Exception {
        Session session = null;
        try {
            session = getSession();

            log.info("Deleting node metadata for " + elementId);
            Node node = getNodeById(session, elementId);
            if (node == null)
                throw new NodeNotFoundException();

            PropertyIterator pit = node.getProperties(META_PFIX + "*");
            while (pit.hasNext()) {
                pit.nextProperty().remove();
            }

            session.save();
            log.info("Deleted metadata for node with id: " + elementId);
            //logProperties(node);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

    public static void logProperties(Node node) {
        try {
            PropertyIterator pit = node.getProperties();
            while ( pit.hasNext() ) {
                Property p = pit.nextProperty();
                try {
                    if (p.getName().equals(ORIGINAL_CONTENT)) continue;
                    log.info("P " + p.getName() + ": " + p.getString());
                } catch ( Exception e ) {
                    log.info("Cannot log " + p.toString());
                }
            }
        } catch ( Exception me ) {
            log.info("Cannot access properties of " + node.toString());
        }
    }

    protected static void logTree(Node node) throws Exception {
        // recursively log all nodes in this three, properties included
        log.info("START " + node.getPath());
        logProperties(node);
        NodeIterator nit = node.getNodes("*"); // child nodes
        while ( nit.hasNext() ) {
            logTree(nit.nextNode());
        }
        log.info("END " + node.getPath());
    }

    public static void logFileNode(Node node) throws Exception {
        if ( !node.getProperty(MYTYPE).getString().equals(TYPE_FILE) ) {
            log.info("Node has type: " + node.getProperty(MYTYPE).getString());
            log.warn("Node " + node.getPath() + " is not of file type; ");
            return;
        }
        logTree(node);
    }

    */
}
