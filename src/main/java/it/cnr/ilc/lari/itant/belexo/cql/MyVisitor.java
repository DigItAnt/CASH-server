package it.cnr.ilc.lari.itant.belexo.cql;

import com.evolvedbinary.cql.parser.CorpusQLBaseVisitor;
import com.evolvedbinary.cql.parser.CorpusQLParser.AndContext;
import com.evolvedbinary.cql.parser.CorpusQLParser.AttValuePairEqualsContext;

// https://www.mvndoc.com/c/com.evolvedbinary.cql/corpusql-parser/index.html?overview-tree.html

public class MyVisitor extends CorpusQLBaseVisitor<String> {
    public String res = "";

    @Override
    public String visitAnd(AndContext ctx) {
        System.out.println(" --AND " + ctx.getText());
        res += " E ";
        return " E ";
    }

    @Override
    public String visitAttValuePairEquals(AttValuePairEqualsContext ctx) {
        System.out.println(" -- value " + ctx.propName().getText() + " " + ctx.valuePart().getText());
        res += ctx.propName().getText() + " UGUALE A " + ctx.valuePart().getText();
        return ctx.propName().getText() + " UGUALE A " + ctx.valuePart().getText();
    }

}
