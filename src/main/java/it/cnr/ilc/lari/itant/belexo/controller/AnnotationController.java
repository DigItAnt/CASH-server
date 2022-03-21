package it.cnr.ilc.lari.itant.belexo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.belexo.DBManager;
import it.cnr.ilc.lari.itant.belexo.om.Annotation;
import it.cnr.ilc.lari.itant.belexo.om.Token;

@CrossOrigin
@RestController
public class AnnotationController {
    private static final Logger log = LoggerFactory.getLogger(AnnotationController.class);

    @GetMapping(value="/api/v1/gettext")
    public String getText(@RequestParam String requestUUID, @RequestParam long nodeid) throws Exception {
        return DBManager.getNodeText(nodeid);
    }

    @GetMapping(value="/api/v1/annotation")
    public List<Annotation> getAnnotations(@RequestParam String requestUUID, @RequestParam long nodeid) throws Exception {
        return DBManager.getNodeAnnotations(nodeid);
    }

    @GetMapping(value="/api/v1/token")
    public List<Token> getTokens(@RequestParam String requestUUID, @RequestParam long nodeid) throws Exception {
        return DBManager.getNodeTokens(nodeid);
    }

    @PostMapping(value="/api/v1/annotation")
    public Annotation createAnnotation(@RequestParam String requestUUID, @RequestParam long nodeid, @RequestBody Annotation annotation) throws Exception {
        annotation.setID(-1);
        return DBManager.addAnnotation(nodeid, annotation);
    }

    @DeleteMapping(value="/api/v1/annotate")
    public void deleteAnnotation(@RequestParam String requestUUID, @RequestParam long annotationID) throws Exception {
        log.info("delete annotation " + annotationID);
        DBManager.deleteAnnotation(annotationID);
    }

    @PutMapping(value="/api/v1/annotation")
    public Annotation modifyAnnotation(@RequestParam String requestUUID, @RequestBody Annotation annotation) throws Exception {
        long nid = DBManager.getAnnotationNodeId(annotation.getID());
        DBManager.deleteAnnotation(annotation.getID());
        DBManager.addAnnotation(nid, annotation);
        return annotation;
    }


}
