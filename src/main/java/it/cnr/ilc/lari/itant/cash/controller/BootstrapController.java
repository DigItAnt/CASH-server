package it.cnr.ilc.lari.itant.cash.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.DocumentSystemNode;
import it.cnr.ilc.lari.itant.cash.om.GetDocumentSystemResponse;
import it.cnr.ilc.lari.itant.cash.om.GetUsersResponse;
import it.cnr.ilc.lari.itant.cash.utils.LogUtils;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class BootstrapController {
    private static final Logger log = LoggerFactory.getLogger(BootstrapController.class);

    @GetMapping("/rest/ping")
	String ping() {
		return "pong";
	}

	@GetMapping("/api/public/getDocumentSystem")
	public GetDocumentSystemResponse getDocumentSystem(@RequestParam String requestUUID, @RequestParam(name="element-id", required = false, defaultValue = "0") long id, Principal principal) {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

		PodamFactory factory = new PodamFactoryImpl();
		GetDocumentSystemResponse toret = factory.manufacturePojo(GetDocumentSystemResponse.class);
		try {
			long root = id == 0 ? DBManager.getRootNodeId() : id;
			toret.setDocumentSystem(DocumentSystemNode.populateTree(root));
			toret.setRequestUUID(requestUUID);
			toret.setResults(toret.getDocumentSystem().size());
		} catch (Exception e) {
			toret.setResults(0);
		}
		return toret;
	}

	@GetMapping("/api/getUsers")
	public GetUsersResponse getUsers(@RequestParam String requestUUID, Principal principal) {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal), requestUUID);

		PodamFactory factory = new PodamFactoryImpl();
		GetUsersResponse toret = factory.manufacturePojo(GetUsersResponse.class);
		toret.setRequestUUID(requestUUID);
		toret.setResults(toret.getUsers().size());
		return toret;
	}

}
