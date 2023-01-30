package it.cnr.ilc.lari.itant.belexo.cql;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.evolvedbinary.cql.parser.CorpusQLBaseVisitor;
import com.evolvedbinary.cql.parser.CorpusQLParser.AndContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairEqualsContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.OrContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.ValuePartParenthesisedContext;

// https://www.mvndoc.com/c/com.evolvedbinary.cql/corpusql-parser/index.html?overview-tree.html

public class MyVisitor extends CorpusQLBaseVisitor<GenStatus> {
    GenStatus status = new GenStatus();

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


    @Override
    public GenStatus visitAttValuePairEquals(AttValuePairEqualsContext ctx) {
        System.out.println(" -- value " + ctx.propName().getText() + " " + ctx.valuePart().getText());
        if (ctx.propName().getText().equals("word"))
            status.setWordValuePairEquals(ctx.valuePart().getText());
        else
            status.setAttValuePairEquals(ctx.propName().getText(), ctx.valuePart().getText());
        return status;
    }

    public GenStatus getStatus() {
        return status;
    }

}
