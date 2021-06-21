package it.cnr.ilc.lari.itant.belexo;

import java.io.File;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrManager {
    public final static String MYID = "myid";
    public final static String MYTYPE = "mytype";
    public final static String TYPE_FOLDER = "folder";
    public final static String TYPE_FILE = "file";
    public final static String BASE_FOLDER_NAME = "new-folder-";
    private final static int ROOT_ID = 0;

    private static final Logger log = LoggerFactory.getLogger(JcrManager.class);
    private static Repository repository;
    private static int startFrom = 1;

    public static void init() throws Exception {
        createRepository();
    }

    public static void createRepository() throws Exception {
		// https://jackrabbit.apache.org/archive/wiki/JCR/RemoteAccess_115513494.html
        repository = JcrUtils.getRepository("http://localhost:8081/server");
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

    public static Node getNodeById(Session session, int nodeId) throws Exception {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        String querytext = "SELECT * FROM [nt:base] WHERE [nt:base].myid = " + nodeId;
        Query query = queryManager.createQuery(querytext, Query.JCR_SQL2);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes();
        while (iter.hasNext())
            return iter.nextNode();
        return null;
    }

    public static int getNewId(Session session) throws Exception {
        while ( true ) {
            if (getNodeById(session, startFrom) == null)
                break;
            startFrom += 1;
        }
        return startFrom;
    }

    public static String getNewFolderName(Node parent) throws Exception {
        int tmp = 1;
        String name = null;
        while ( true ) {
            name = BASE_FOLDER_NAME + tmp;
            try {
                parent.getNode(name);
            } catch (PathNotFoundException e) {
                break;
            }
            tmp += 1;
        }
        return name;
    }

    public synchronized static int addFolder(int parentId) {
        Session session = null;
        try {
            session = getSession();

            Node root = session.getRootNode();
            Node parent = root;
            if (parentId != ROOT_ID)
                parent = getNodeById(session, parentId);
            if (parent == null) // TODO custom exception
                throw new Exception("NOT FOUND");
            int newid = getNewId(session);
            String name = getNewFolderName(parent);
            Node newfolder = parent.addNode(name);
            newfolder.setProperty(MYID, newid);
            newfolder.setProperty(MYTYPE, TYPE_FOLDER);
            session.save();
            log.info("Created folder " + name + ", id: " + newid);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.logout();
        }
        return 0;
    }

    public synchronized static int removeFolder(int elementId) {
        Session session = null;
        try {
            session = getSession();

            if (elementId == ROOT_ID) // TODO custom exception
                throw new Exception("CANNOT REMOVE ROOT");
            Node node = getNodeById(session, elementId);
            if (node == null) // TODO custom exception
                throw new Exception("NOT FOUND");
            String name = node.getName();
            node.remove();
            session.save();
            log.info("Removed node " + name + ", id: " + elementId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.logout();
        }
        return 0;
    }


    public static void test2(Repository repository) throws Exception {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

        try { 
            Node root = session.getRootNode(); 

            // Store content 
            Node hello = root.addNode("hello"); 
            Node world = hello.addNode("world"); 
            world.setProperty("message", "Hello, World!"); 
            session.save(); 

            // Retrieve content 
            Node node = root.getNode("hello/world"); 
            System.out.println(node.getPath()); 
            System.out.println(node.getProperty("message").getString()); 

            // Remove content 
            //root.getNode("hello").remove(); 
            session.save(); 
        } finally { 
            session.logout(); 
        } 
    }

}
