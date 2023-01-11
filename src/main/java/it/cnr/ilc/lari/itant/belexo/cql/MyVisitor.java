package it.cnr.ilc.lari.itant.belexo.cql;

import com.evolvedbinary.cql.parser.CorpusQLBaseVisitor;
import com.evolvedbinary.cql.parser.CorpusQLParser.AndContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairEqualsContext;

// https://www.mvndoc.com/c/com.evolvedbinary.cql/corpusql-parser/index.html?overview-tree.html

public class MyVisitor extends CorpusQLBaseVisitor<GenStatus> {
    GenStatus status = new GenStatus();

    @Override
    public GenStatus visitAnd(AndContext ctx) {
        System.out.println(" --AND " + ctx.getText());
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
