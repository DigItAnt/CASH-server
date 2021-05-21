package it.cnr.ilc.lari.itant.belexo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.belexo.om.SearchFilesRequest;
import it.cnr.ilc.lari.itant.belexo.om.SearchFilesResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class SearchController {
	@PostMapping("/api/searchFiles")
	public SearchFilesResponse searchFiles(@RequestBody SearchFilesRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		SearchFilesResponse toret = factory.manufacturePojo(SearchFilesResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		toret.setResults(toret.getFiles().size());
		return toret;
	}
}
