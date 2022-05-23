package it.cnr.ilc.lari.itant.cash.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.DocumentSystemNode;
import it.cnr.ilc.lari.itant.cash.om.GetDocumentSystemResponse;
import it.cnr.ilc.lari.itant.cash.om.GetUsersResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class BootstrapController {
    
    @GetMapping("/rest/ping")
	String ping() {
		return "pong";
	}

	@GetMapping("/api/getDocumentSystem")
	public GetDocumentSystemResponse getDocumentSystem(@RequestParam String requestUUID, Principal principal) {
		//System.out.println("User: " + principal.getName());
		PodamFactory factory = new PodamFactoryImpl();
		GetDocumentSystemResponse toret = factory.manufacturePojo(GetDocumentSystemResponse.class);
		try {
			toret.setDocumentSystem(DocumentSystemNode.populateTree(DBManager.getRootNodeId()));
			toret.setRequestUUID(requestUUID);
			toret.setResults(toret.getDocumentSystem().size());
		} catch (Exception e) {
			toret.setResults(0);
		}
		return toret;
	}

	@GetMapping("/api/getUsers")
	public GetUsersResponse getUsers(@RequestParam String requestUUID) {
		PodamFactory factory = new PodamFactoryImpl();
		GetUsersResponse toret = factory.manufacturePojo(GetUsersResponse.class);
		toret.setRequestUUID(requestUUID);
		toret.setResults(toret.getUsers().size());
		return toret;
	}

}
