package it.cnr.ilc.lari.itant.cash.cql;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.evolvedbinary.cql.parser.CorpusQLLexer;
import com.evolvedbinary.cql.parser.CorpusQLParser;

import it.cnr.ilc.lari.itant.cash.DBManager;

/**
 * Example if using the Corpus Query Language Parser
 *
 * You can implement {@link CorpusQLBaseListener} or {@link CorpusQLBaseVisitor}
 * if you want to perform AST transformations
 */
public class TestCQL {

    /**
     * Expects a Corpus Query Language expression as arg[0]
     * and then prints the parse tree
     */
    public static void main(final String args[]) throws Exception {
        DBManager.init();

        String q = "[lemma=\"FAV.*\"] [ ]{2,4} [pos=\"POOO.*\"]"; // args[0]
        q = "[word=\".\" & (pos='asd' | pos=\"www\" ) ]";
        q = "[(word=\".\" & pos='asd') | pos=\"www\" ]";
        q = "[epidoc=\"tei:name\"]";
        //q = "[word=\"statis\"]";
        // cash__author -> metadata author del documento
        // pos__author -> metadata author del pos, pos__author__name -> metadata author.name del pos
        q = "[word=\"statis\" | epidoc=\"tei:name\"]";
        //q = "[word=\"aa\" & pos=\"asd\" & lemma=\"www\"]";
        //q = "[_doc__sub1__f1__f2=\"statis\"]";
        q = "[pos__sub1__f1__f2=\"statis\"]";
        q = "[word=\"_REGEX_statis\"][pos=\"POOO.*\"]";
        //q = "[][][]";

        final CorpusQLLexer lexer = new CorpusQLLexer(CharStreams.fromString(q));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CorpusQLParser parser = new CorpusQLParser(tokens);

        final ParseTree tree = parser.query();

        MyVisitor vis = new MyVisitor();
        vis.visit(tree);
        System.out.println(vis.status.gen(0, 10).toString());
    }
}
