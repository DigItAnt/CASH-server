package it.cnr.ilc.lari.itant.cash.cql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import com.evolvedbinary.cql.parser.CorpusQLLexer;
import com.evolvedbinary.cql.parser.CorpusQLParser;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.InvalidParamException;

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
        q = "[worda__gg__aa=\"no\\|word|statis|aaa|dd\"]";
        //q = "[worda\"statis\"]";
        //q = "[][][]";

        // read q from standard input
        //q = new BufferedReader(new InputStreamReader(System.in)).readLine();
        
        final CorpusQLLexer lexer = new CorpusQLLexer(CharStreams.fromString(q));
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
        System.out.println(vis.status.gen(0, 10).toString());
    }
}
