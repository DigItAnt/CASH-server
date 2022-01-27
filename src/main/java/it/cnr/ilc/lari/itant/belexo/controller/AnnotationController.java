package it.cnr.ilc.lari.itant.belexo.controller;

import java.util.ArrayList;
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

import it.cnr.ilc.lari.itant.belexo.om.Annotation;

@CrossOrigin
@RestController
public class AnnotationController {
    private static final Logger log = LoggerFactory.getLogger(AnnotationController.class);

    @GetMapping(value="/api/v1/gettext")
    public String getText(@RequestParam String nodeid) {

        return "Lorem ipsum dolor sit amet consectetur adipiscing elit";
    }

    @GetMapping(value="/api/v1/annotation")
    public List<Annotation> getAnnotations(@RequestParam String nodeid) {

        return new ArrayList<Annotation>();
    }

    @PostMapping(value="/api/v1/annotation")
    public Annotation createAnnotation(@RequestParam String nodeid, @RequestBody Annotation annotation) {
        return annotation;
    }

    @DeleteMapping(value="/api/v1/annotate")
    public void deleteAnnotation(@RequestParam int annotationID) {
        log.info("delete annotation " + annotationID);
    }

    @PutMapping(value="/api/v1/annotation")
    public Annotation modifyAnnotation(@RequestBody Annotation annotation) {
        return annotation;
    }


}
