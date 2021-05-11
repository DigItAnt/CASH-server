package it.cnr.ilc.lari.itant.belexo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.belexo.om.GetDocumentSystemOutput;
import it.cnr.ilc.lari.itant.belexo.om.GetUsersOutput;
import it.cnr.ilc.lari.itant.belexo.om.SearchFilesRequest;
import it.cnr.ilc.lari.itant.belexo.om.SearchFilesResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@RestController
public class BootstrapController {
    
    @GetMapping("/rest/ping")
	String ping() {
		return "pong";
	}

	@GetMapping("/api/getDocumentSystem")
	public GetDocumentSystemOutput getDocumentSystem(@RequestParam String requestUUID) {
		PodamFactory factory = new PodamFactoryImpl();
		GetDocumentSystemOutput toret = factory.manufacturePojo(GetDocumentSystemOutput.class);
		toret.setRequestUUID(requestUUID);
		toret.setResults(toret.getDocumentSystem().size());
		return toret;
	}

	@GetMapping("/api/getUsers")
	public GetUsersOutput getUsers(@RequestParam String requestUUID) {
		PodamFactory factory = new PodamFactoryImpl();
		GetUsersOutput toret = factory.manufacturePojo(GetUsersOutput.class);
		toret.setRequestUUID(requestUUID);
		toret.setResults(toret.getUsers().size());
		return toret;
	}

	@PostMapping("/api/searchFiles")
	public SearchFilesResponse searchFiles(@RequestBody SearchFilesRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		SearchFilesResponse toret = factory.manufacturePojo(SearchFilesResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		toret.setResults(toret.getFiles().size());
		return toret;
	}
}
