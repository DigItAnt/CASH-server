package it.cnr.ilc.lari.itant.cash.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.FileInfo;
import it.cnr.ilc.lari.itant.cash.om.MetadataRefreshStatus;
import it.cnr.ilc.lari.itant.cash.om.DocumentSystemNode.FileDirectory;

public class MetadataRefresher {

    private static final Logger log = LoggerFactory.getLogger(MetadataRefresher.class);

    public static List<Long> getChildrenIDs(long elementID) {
        List<Long> toret = new ArrayList<>();
        FileInfo node = null;
        try {
            node = DBManager.getNodeById(elementID);
        } catch (Exception e) {
            log.error("Error getting node " + elementID, e);
            return toret;
        }
        if ( node.getType() == FileDirectory.file ) {
            if ( "text/xml".equals(DBManager.getContentType(elementID)) ) {
                toret.add(elementID);
            }
        } else {
            List<FileInfo> children = null;
            try {
                children = DBManager.getNodeChildren(elementID);
            } catch (Exception e) {
                log.error("Error getting children of node " + elementID, e);
                return toret;
            }
            for (FileInfo child : children) {
                if ( child.getType() == FileDirectory.file ) {
                    toret.add(child.getElementId());
                } else {
                    toret.addAll(getChildrenIDs(child.getElementId()));
                }
            }
        }
        return toret;
    }



    public static MetadataRefreshStatus run(long elementID) {
        MetadataRefreshStatus toret = new MetadataRefreshStatus();
        toret.setStatuses(new ArrayList<MetadataRefreshStatus.Status>());

        log.info("Updating metadata staring from note {}", elementID);

        if ( elementID == 0 )
            try {
                elementID = DBManager.getRootNodeId();
            } catch (Exception e) {
                log.error("Error getting root node", e);
                MetadataRefreshStatus.Status status = toret.new Status();
                status.elementId = elementID;
                status.status = "KO";
                toret.getStatuses().add(status);
                return toret;
            }
        // first, get all id's of file elements that are children of elementID
        List<Long> childrenIDs = getChildrenIDs(elementID);

        log.info("Running metadata refresh for {} files", childrenIDs.size());

        ClassPathResource cpr = new ClassPathResource(EpiDocTextExtractor.MDATAPATH);
        XpathMetadataImporter importer = null;
        try {
            importer = new XpathMetadataImporter(new String(cpr.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error reading metadata definition file", e);
            MetadataRefreshStatus.Status status = toret.new Status();
            status.elementId = elementID;
            status.status = "KO";
            toret.getStatuses().add(status);
            return toret;
        }
        importer.setContext(new SimpleNamespaceContext(EpiDocTextExtractor.NSPS));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        for ( long id : childrenIDs ) {
            MetadataRefreshStatus.Status status = toret.new Status();
            status.elementId = id;
            FileInfo node = null;
            try {
                node = DBManager.getNodeById(id);
            } catch (Exception e) {
                log.error("Error getting node " + id, e);
                status.status = "KO";
                toret.getStatuses().add(status);
                continue;
            }
            Map<String, Object> oldMetadata = node.getMetadata();
            
            Map<String, Object> newMetadata = null;
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new ByteArrayInputStream(DBManager.getRawContent(id, null).getBytes()));

                // metadata
                newMetadata = importer.extract(doc);
            } catch (Exception e) {
                log.error("Error parsing file " + node.getName(), e);
                continue;
            }

            // compare old and new metadata
            // overwrite old metadata with new metadata keys. Keep old metadata keys that are not in new metadata
            // if a key is in both, overwrite the value
            for ( String key : newMetadata.keySet() )
                oldMetadata.put(key, newMetadata.get(key));

            // save metadata
            try {
                DBManager.replaceNodeMetadata(id, oldMetadata);
            } catch (Exception e) {
                log.error("Error saving metadata for file " + node.getName(), e);
                continue;
            }
            status.status = "OK";
            toret.getStatuses().add(status);
        }
        return toret;
    }
}