package it.cnr.ilc.lari.itant.belexo;

import java.io.File;

import javax.jcr.GuestCredentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrManager {
    private static final Logger log = LoggerFactory.getLogger(JcrManager.class);

    static Repository repository;

    public static void start() throws Exception {
        //String xml = "/path/to/repository/configuration.xml";
        String dir = "/tmp/rabbit";
        //repository = new TransientRepository();
        RepositoryConfig config = RepositoryConfig.create(new File(dir));
        //RepositoryConfig config = RepositoryConfig.create(xml, dir);
        repository = RepositoryImpl.create(config);

    }

    public static void stop() {
        ((RepositoryImpl) repository).shutdown();
    }

    public static void test1() throws Exception {
        Session session = repository.login(new GuestCredentials());

        try { 
            String user = session.getUserID(); 
            String name = repository.getDescriptor(Repository.REP_NAME_DESC); 
            System.out.println("Logged in as " + user + " to a " + name + " repository."); 
        } finally { 
            session.logout(); 
        } 
    }

    public static void test2() throws Exception {
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
            root.getNode("hello").remove(); 
            session.save(); 
        } finally { 
            session.logout(); 
        } 
    }

}
