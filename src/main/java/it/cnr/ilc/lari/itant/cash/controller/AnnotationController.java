package it.cnr.ilc.lari.itant.cash.controller;

import java.security.Principal;
import java.util.HashMap;

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

import io.swagger.v3.oas.annotations.Operation;
import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.cash.om.Annotation;
import it.cnr.ilc.lari.itant.cash.om.CreateAnnotationResponse;
import it.cnr.ilc.lari.itant.cash.om.CreateTokenResponse;
import it.cnr.ilc.lari.itant.cash.om.DeleteTokenResponse;
import it.cnr.ilc.lari.itant.cash.om.GetAnnotationResponse;
import it.cnr.ilc.lari.itant.cash.om.GetAnnotationsResponse;
import it.cnr.ilc.lari.itant.cash.om.GetRawContent;
import it.cnr.ilc.lari.itant.cash.om.GetTextResponse;
import it.cnr.ilc.lari.itant.cash.om.GetTokensResponse;
import it.cnr.ilc.lari.itant.cash.om.ModifyAnnotationResponse;
import it.cnr.ilc.lari.itant.cash.om.Token;
import it.cnr.ilc.lari.itant.cash.om.UnstructuredRequest;
import it.cnr.ilc.lari.itant.cash.om.UnstructuredResponse;
import it.cnr.ilc.lari.itant.cash.utils.LogUtils;

@CrossOrigin
@RestController
public class AnnotationController {
    private static final Logger log = LoggerFactory.getLogger(AnnotationController.class);

    @GetMapping(value="/api/public/gettext")
    @Operation(summary = "Get text", description = "Get the text associated to nodeid")
    public GetTextResponse getText(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);
        
        GetTextResponse resp = new GetTextResponse();
        resp.setRequestUUID(requestUUID);
        resp.setText(DBManager.getNodeText(nodeid, null));
        return resp;
    }

    @GetMapping(value="/api/public/getcontent")
    @Operation(summary = "Get raw content", description = "Get the original nodeid content (e.g. original XML)")
    public GetRawContent getContent(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        GetRawContent resp = new GetRawContent();
        resp.setRequestUUID(requestUUID);
        resp.setText(DBManager.getRawContent(nodeid, null));
        return resp;
    }

    @GetMapping(value="/api/public/annotation")
    @Operation(summary = "Get annotations", description = "Get annotations associated to nodeid")
    public GetAnnotationsResponse getAnnotations(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        GetAnnotationsResponse resp = new GetAnnotationsResponse();
        resp.setRequestUUID(requestUUID);
        resp.setAnnotations(DBManager.getNodeAnnotations(nodeid, null));
        return resp;
    }

    @GetMapping(value="/api/public/token")
    @Operation(summary = "Get tokens", description = "Get tokens associated to nodeid")
    public GetTokensResponse getTokens(@RequestParam String requestUUID, @RequestParam long nodeid, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        GetTokensResponse resp = new GetTokensResponse();
        resp.setRequestUUID(requestUUID);
        resp.setTokens(DBManager.getNodeTokens(nodeid, null));
        return resp;
    }

    @PostMapping(value="/api/token")
    @Operation(summary = "Create a token", description = "Create a token associated to nodeid")
    public CreateTokenResponse createToken(@RequestParam String requestUUID, @RequestParam long nodeid, @RequestBody Token token, Principal principal) throws Exception {
		if ( principal != null ) log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        CreateTokenResponse resp = new CreateTokenResponse();
        token.setID(-1);

        resp.setRequestUUID(requestUUID);
        Long srcid = DBManager.getNodeTextId(nodeid, token.getSource(), null);
        long id = DBManager.insertTokenNode(nodeid, srcid, token.getText(), token.getPosition(), token.getBegin(),
                                            token.getEnd(), token.getXmlid(), token.isImported(), null);
        token.setID(id);
        resp.setToken(token);
        return resp;
    }

    @DeleteMapping(value="/api/token")
    @Operation(summary = "Delete a token", description = "Delete a token identified by tokenid")
    public DeleteTokenResponse deleteToken(@RequestParam String requestUUID, @RequestParam long tokenid, Principal principal) throws Exception {
		if ( principal != null ) log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        DeleteTokenResponse resp = new DeleteTokenResponse();

        resp.setRequestUUID(requestUUID);
        DBManager.deleteToken(tokenid, null);
        return resp;
    }


    @PostMapping(value="/api/annotation")
    @Operation(summary = "Create an annotation", description = "Create an annotation on document nodeid.")
    public CreateAnnotationResponse createAnnotation(@RequestParam String requestUUID, @RequestParam long nodeid, @RequestBody Annotation annotation, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        CreateAnnotationResponse resp = new CreateAnnotationResponse();
        annotation.setID(-1);

        resp.setRequestUUID(requestUUID);
        resp.setAnnotation(DBManager.addAnnotation(nodeid, annotation));
        return resp;
    }

    @DeleteMapping(value="/api/annotate")
    @Operation(summary = "Delete an annotation", description = "Delete the annotation given its annotationID.")
    public void deleteAnnotation(@RequestParam String requestUUID, @RequestParam long annotationID, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID, "delete annotation " + annotationID);
        DBManager.deleteAnnotation(annotationID, null);
    }

    @PutMapping(value="/api/annotation")
    @Operation(summary = "Modify an annotation", description = "Modify the annotation identified by annotation.id")
    public ModifyAnnotationResponse modifyAnnotation(@RequestParam String requestUUID, @RequestBody Annotation annotation, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        ModifyAnnotationResponse resp = new ModifyAnnotationResponse();

        if ( annotation.getSpans().isEmpty() ) {
            log.error("Trying to modify an annotation by removing spans");
            throw new InvalidParamException();
        }

        long nid = DBManager.getAnnotationNodeId(annotation.getID(), null);
        DBManager.deleteAnnotation(annotation.getID(), null);
        DBManager.addAnnotation(nid, annotation);

        resp.setRequestUUID(requestUUID);
        resp.setAnnotation(annotation);
        return resp;
    }

    /** Not ready yet
    @GetMapping(value="/api/annotation")
    public GetAnnotationResponse getAnnotation(@RequestParam String requestUUID, @RequestParam long annotationId, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

        GetAnnotationResponse resp = new GetAnnotationResponse();

        Annotation annotation = DBManager.getAnnotationById(annotationId, null);
        resp.setRequestUUID(requestUUID);
        resp.setAnnotation(annotation);
        return resp;
    }
     */

    // CHECK
    @DeleteMapping(value="/api/annotationbyvalue")
    @Operation(summary = "Delete annotations by value", description = "Delete the annotations with value 'value'")
    public void deleteByValue(@RequestParam String requestUUID, @RequestParam String value, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID, "delete annotation by value " + value);

        DBManager.deleteAnnotationByValue(value, null);
    }

    // CHECK
    @GetMapping(value="/api/annotationbyvalue")
    @Operation(summary = "Get annotations by value", description = "Get the annotations with value 'value'")
    public GetAnnotationsResponse getByValue(@RequestParam String requestUUID, @RequestParam String value, Principal principal) throws Exception {
        GetAnnotationsResponse resp = new GetAnnotationsResponse();
        resp.setRequestUUID(requestUUID);
        resp.setAnnotations(DBManager.getAnnotationsByValue(value, null));
        return resp;
        
    }

    // CHECK
    @PostMapping(value="/api/unstructured")
    @Operation(summary = "Add a new node with ...", description = "...")
    public UnstructuredResponse addUnastructured(@RequestParam String requestUUID, @RequestParam long nodeid, @RequestBody UnstructuredRequest request, Principal principal) throws Exception {
        if ( principal != null ) log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID, "unstructured annotation for " + nodeid);
        UnstructuredResponse resp = new UnstructuredResponse();
        resp.setUnstructuredids(new HashMap<String, Long>());

        for ( String type : request.getUnstructured().keySet() ) {
            String value = request.getUnstructured().get(type);
            long id = DBManager.insertTextEntry(nodeid, value, type, null);
            resp.getUnstructuredids().put(type, id);
        }
        
        resp.setNodeid(nodeid);

        return resp;
    }

    
}
