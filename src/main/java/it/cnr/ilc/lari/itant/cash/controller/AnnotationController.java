package it.cnr.ilc.lari.itant.cash.controller;

import java.security.Principal;

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

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.Annotation;
import it.cnr.ilc.lari.itant.cash.om.CreateAnnotationResponse;
import it.cnr.ilc.lari.itant.cash.om.GetAnnotationsResponse;
import it.cnr.ilc.lari.itant.cash.om.GetRawContent;
import it.cnr.ilc.lari.itant.cash.om.GetTextResponse;
import it.cnr.ilc.lari.itant.cash.om.GetTokensResponse;
import it.cnr.ilc.lari.itant.cash.om.ModifyAnnotationResponse;
import it.cnr.ilc.lari.itant.cash.utils.LogUtils;

@CrossOrigin
@RestController
public class AnnotationController {
    private static final Logger log = LoggerFactory.getLogger(AnnotationController.class);

    @GetMapping(value="/api/v1/gettext")
    public GetTextResponse getText(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);
        
        GetTextResponse resp = new GetTextResponse();
        resp.setRequestUUID(requestUUID);
        resp.setText(DBManager.getNodeText(nodeid));
        return resp;
    }

    @GetMapping(value="/api/v1/getcontent")
    public GetRawContent getContent(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);

        GetRawContent resp = new GetRawContent();
        resp.setRequestUUID(requestUUID);
        resp.setText(DBManager.getRawContent(nodeid));
        return resp;
    }

    @GetMapping(value="/api/v1/annotation")
    public GetAnnotationsResponse getAnnotations(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);

        GetAnnotationsResponse resp = new GetAnnotationsResponse();
        resp.setRequestUUID(requestUUID);
        resp.setAnnotations(DBManager.getNodeAnnotations(nodeid));
        return resp;
    }

    @GetMapping(value="/api/v1/token")
    public GetTokensResponse getTokens(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);

        GetTokensResponse resp = new GetTokensResponse();
        resp.setRequestUUID(requestUUID);
        resp.setTokens(DBManager.getNodeTokens(nodeid));
        return resp;
    }

    @PostMapping(value="/api/v1/annotation")
    public CreateAnnotationResponse createAnnotation(@RequestParam String requestUUID, @RequestParam long nodeid, @RequestBody Annotation annotation, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);

        CreateAnnotationResponse resp = new CreateAnnotationResponse();
        annotation.setID(-1);

        resp.setRequestUUID(requestUUID);
        resp.setAnnotation(DBManager.addAnnotation(nodeid, annotation));
        return resp;
    }

    @DeleteMapping(value="/api/v1/annotate")
    public void deleteAnnotation(@RequestParam String requestUUID, @RequestParam long annotationID, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID, "delete annotation " + annotationID);
        DBManager.deleteAnnotation(annotationID);
    }

    @PutMapping(value="/api/v1/annotation")
    public ModifyAnnotationResponse modifyAnnotation(@RequestParam String requestUUID, @RequestBody Annotation annotation, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID);

        ModifyAnnotationResponse resp = new ModifyAnnotationResponse();

        long nid = DBManager.getAnnotationNodeId(annotation.getID());
        DBManager.deleteAnnotation(annotation.getID());
        DBManager.addAnnotation(nid, annotation);

        resp.setRequestUUID(requestUUID);
        resp.setAnnotation(annotation);
        return resp;
    }

    @DeleteMapping(value="/api/v1/annotationbyvalue")
    public void deleteByValue(@RequestParam String requestUUID, @RequestParam String value, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, principal.getName(), requestUUID, "delete annotation by value " + value);

        DBManager.deleteAnnotationByValue(value);
    }

}
