package it.cnr.ilc.lari.itant.cash.controller;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evolvedbinary.cql.parser.CorpusQLLexer;
import com.evolvedbinary.cql.parser.CorpusQLParser;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.cql.MyVisitor;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;
import it.cnr.ilc.lari.itant.cash.om.SearchFilesRequest;
import it.cnr.ilc.lari.itant.cash.om.SearchFilesResponse;
import it.cnr.ilc.lari.itant.cash.om.SearchResponse;
import it.cnr.ilc.lari.itant.cash.om.SearchRow;
import it.cnr.ilc.lari.itant.cash.om.TestSearchResponse;
import it.cnr.ilc.lari.itant.cash.utils.LogUtils;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class SearchController {
	private static final Logger log = LoggerFactory.getLogger(SearchController.class);

	@PostMapping("/api/public/searchFiles")
	public SearchFilesResponse searchFiles(@RequestBody SearchFilesRequest request, Principal principal) {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		PodamFactory factory = new PodamFactoryImpl();
		SearchFilesResponse toret = factory.manufacturePojo(SearchFilesResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		toret.setResults(toret.getFiles().size());
		return toret;
	}

	@PostMapping("/api/public/testSearch")
	public TestSearchResponse testSearch(@RequestParam String query, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		TestSearchResponse res = new TestSearchResponse();

		List<Long> ids = DBManager.findNodesByTextQuery(query);
		ArrayList<String> paths = new ArrayList<>();
		for ( Long nid: ids ) {
			paths.add(DBManager.getNodePath(nid));
		}
		res.setPaths(paths);
		return res;
	}

	@PostMapping("/api/public/search")
	public SearchResponse search(@RequestParam String query,
	                             @RequestParam(value="limit", defaultValue = "10") int limit,
								 @RequestParam(value="offset", defaultValue = "0") int offset,
	                             Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

        final CorpusQLLexer lexer = new CorpusQLLexer(CharStreams.fromString(query));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CorpusQLParser parser = new CorpusQLParser(tokens);

		parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer,
                                    final Object offendingSymbol,
                                    final int line,
                                    final int charPositionInLine,
                                    final String msg,
                                    final RecognitionException e) {
                throw new InvalidParamException("failed to parse at line " + line + " due to " + msg);
            }
        });

        final ParseTree tree = parser.query();

        MyVisitor vis = new MyVisitor();
        vis.visit(tree);
		PreparedStatement stmt = vis.getStatus().gen(offset, limit);
		String qsql = stmt.toString();
		log.info("From {} to {}", query, qsql);

		SearchResponse res = new SearchResponse();

		List<SearchRow> srs = DBManager.findNodesBySQLQuery(stmt);
		Map<Long, String> paths = new HashMap<>();

		for ( SearchRow sr: srs ) {
			if ( !paths.containsKey(sr.getNodeId()) ) {
				paths.put(sr.getNodeId(), DBManager.getNodePath(sr.getNodeId()));
			}
			sr.setNodePath(paths.get(sr.getNodeId()));
		}
		res.setRows(srs);
		return res;
	}


}
