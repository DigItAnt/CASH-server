package it.cnr.ilc.lari.itant.cash.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.om.SearchFilesRequest;
import it.cnr.ilc.lari.itant.cash.om.SearchFilesResponse;
import it.cnr.ilc.lari.itant.cash.om.TestSearchResponse;
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

	@PostMapping("/api/testSearch")
	public TestSearchResponse testSearch(@RequestParam String query) throws Exception {
		TestSearchResponse res = new TestSearchResponse();

		List<Long> ids = DBManager.findNodesByTextQuery(query);
		ArrayList<String> paths = new ArrayList<>();
		for ( Long nid: ids ) {
			paths.add(DBManager.getNodePath(nid));
		}
		res.setPaths(paths);
		return res;
	}
}
