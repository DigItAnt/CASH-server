package it.cnr.ilc.lari.itant.belexo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.belexo.exc.NodeNotFoundException;

public class JcrManager {
    public final static String MYID = "myid";
    public final static String MYTYPE = "mytype";
    public final static String TYPE_FOLDER = "folder";
    public final static String TYPE_FILE = "file"; // this node represents a file
    public final static String TYPE_STRUCTURE = "structure"; // under a file node, this is the structure
    public final static String BASE_FOLDER_NAME = "new-folder-";
    public final static String META_PFIX = "meta_";
    public final static String ORIGINAL_CONTENT = "original_content";
    public final static String CONTENT_TYPE = "content_type";
    private final static int ROOT_ID = 0;

    private static final Logger log = LoggerFactory.getLogger(JcrManager.class);
    private static Repository repository;
    private static long startFrom = 1;

    public static void init() throws Exception {
        createRepository();
    }

    public static void createRepository() throws Exception {
		// https://jackrabbit.apache.org/archive/wiki/JCR/RemoteAccess_115513494.html
        String jackrabbitURL = System.getenv("JACKRABBIT_URL");
        if (jackrabbitURL == null)
            jackrabbitURL = "http://localhost:8081/server";
        log.info("JACKRABBIT URL: " + jackrabbitURL);
        repository = JcrUtils.getRepository(jackrabbitURL);
        createRootFolderIfNeeded();
    }

    private static void createRootFolderIfNeeded() throws Exception {
        Session session = null;
        try {
            session = getSession();
            if (getNodeById(session, ROOT_ID) != null) {
                log.info("repo already initialized");
                return;
            }

            log.info("Initializing repo, creating root node /root (0)");
            Node parent = session.getRootNode();
    
            int newid = ROOT_ID;
            String name = "root";
            Node newfolder = parent.addNode(name);
            newfolder.setProperty(MYID, newid);
            newfolder.setProperty(MYTYPE, TYPE_FOLDER);
            session.save();
            log.info("Created /root" + ", id: " + newid);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        } finally {
            if (session != null) session.logout();
        }
    }

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
        return repository;
    }

    public static void stop(Repository repository) {
        ((RepositoryImpl) repository).shutdown();
    }


    public static Session getSession() throws LoginException, RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        return session;
    }

    public static Node getNodeById(long nodeId) throws Exception {
        Session session = getSession();
        return getNodeById(session, nodeId);
    }

    public static Node getNodeById(Session session, long nodeId) throws Exception {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        String querytext = "SELECT * FROM [nt:base] WHERE [nt:base].myid = " + nodeId;
        Query query = queryManager.createQuery(querytext, Query.JCR_SQL2);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes();
        while (iter.hasNext())
            return iter.nextNode();
        return null;
    }

    public static long getNewId(Session session) throws Exception {
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
            node = addNodeInternal(session, parentId, filename, TYPE_FILE);

            // add original content to node
            byte[] contentBytes = contentStream.readAllBytes();
            setFileData(node, contentType, contentBytes);

            session.save();
            if ( filename.endsWith(".xml") ) { // TODO: perhaps do better, here!
                log.info("MYID: " + node.getProperty(MYID).getLong());
                Node structured = addNodeInternal(session, node.getProperty(MYID).getLong(), "structure", TYPE_STRUCTURE, true);
                log.info("Added content under: " + structured.getPath());
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
            Node newfolder = parent.addNode(name);
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
            if (fileExists(parent, name))
                throw new InvalidParamException();
            newNode = parent.addNode(name);
            newNode.setProperty(MYID, newid);
            newNode.setProperty(MYTYPE, nodetype);
            log.info("Created node " + name + ", id: " + newid);
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
            if (elementId == ROOT_ID)
                throw new InvalidParamException();
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
            if ( newname.contains("/") )
                throw new InvalidParamException();
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
            if (!nameOk)
                throw new InvalidParamException();

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
            if (elementId == ROOT_ID)
                throw new InvalidParamException();
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
}
