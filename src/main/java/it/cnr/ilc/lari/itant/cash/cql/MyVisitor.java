package it.cnr.ilc.lari.itant.cash.cql;

import java.util.Arrays;

import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.evolvedbinary.cql.parser.CorpusQLBaseVisitor;
import com.evolvedbinary.cql.parser.CorpusQLParser.AndContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairEqualsContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairEqualsREContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairLessContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairNotEqualsContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.ComplexQueryContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.OrContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.QueryContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.SequenceContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.SequencePartContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.SimpleQueryContext;

// https://www.mvndoc.com/c/com.evolvedbinary.cql/corpusql-parser/index.html?overview-tree.html

public class MyVisitor extends CorpusQLBaseVisitor<GenStatus> {
    private static final Logger log = LoggerFactory.getLogger(MyVisitor.class);

    GenStatus status = new GenStatus();

    public static String customSep = "__";

    public int queryPartId = 0;

    @Override
    public GenStatus visitAnd(AndContext ctx) {
        System.out.println(" --AND " + ctx.getText());
        status.setOperator("AND");
        return status;
    }

    @Override
    public GenStatus visitOr(OrContext ctx) {
        System.out.println(" --OR " + ctx.getText());
        status.setOperator("OR");
        return status;
    }
    
    @Override
    public GenStatus visitTerminal(TerminalNode node) {
        if ( "()".contains(node.getText()) )
            status.setOperator(node.getText());

        return status;
    }

    public GenStatus visitAttValuePairOp(String propName, String valuePart, String op, boolean cast) {
        System.out.println(" -- value " + propName + " " + valuePart + " op: " + op);

        if (propName.contains(customSep))
            return visitCustomAttValuePairOp(propName, valuePart, op, cast);

        if (propName.equals("word"))
            status.setWordValuePairOp(valuePart, op, cast);
        else
            status.setAttValuePairOp(propName, valuePart, op, cast);
        return status;
    }

    private GenStatus visitCustomAttValuePairOp(String propName, String valuePart, String op, boolean cast) {
        String[] parts = propName.split(customSep);
        String layer = parts[0];
        String field = parts[1];
        String[] subfields = new String[]{};
        if (parts.length > 2)
            subfields = Arrays.copyOfRange(parts, 2, parts.length);

        // log field and subfields
        log.info("layer: {}, field: {}, subfields: {}", layer, field, subfields);

        status.setMetaValuePairOp(layer, field, subfields, valuePart, op, cast);
        return status;
    }

    public GenStatus getStatus() {
        return status;
    }

    @Override
    public GenStatus visitSequencePart(SequencePartContext ctx) {
        queryPartId++;
        status.setCurrentTokenId(queryPartId);
        return super.visitSequencePart(ctx);
    }

    @Override
    public GenStatus visitAttValuePairEqualsRE(AttValuePairEqualsREContext ctx) {
        String op = "=";
        return visitAttValuePairOp(ctx.propName().getText(), ctx.valuePart().getText(), op, false);        
    }

    @Override
    public GenStatus visitAttValuePairEquals(AttValuePairEqualsContext ctx) {
        String op = "==";
        return visitAttValuePairOp(ctx.propName().getText(), ctx.valuePart().getText(), op, false);
    }

    @Override
    public GenStatus visitAttValuePairLess(AttValuePairLessContext ctx) {
        String op = "<";
        return visitAttValuePairOp(ctx.propName().getText(), ctx.valuePart().getText(), op, true);
    }


}
