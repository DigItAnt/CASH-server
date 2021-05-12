package it.cnr.ilc.lari.itant.belexo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.belexo.om.GetDocumentSystemResponse;
import it.cnr.ilc.lari.itant.belexo.om.GetUsersResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@RestController
public class BootstrapController {
    
    @GetMapping("/rest/ping")
	String ping() {
		return "pong";
	}

	@GetMapping("/api/getDocumentSystem")
	public GetDocumentSystemResponse getDocumentSystem(@RequestParam String requestUUID) {
		PodamFactory factory = new PodamFactoryImpl();
		GetDocumentSystemResponse toret = factory.manufacturePojo(GetDocumentSystemResponse.class);
		toret.setRequestUUID(requestUUID);
		toret.setResults(toret.getDocumentSystem().size());
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
