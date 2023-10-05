package it.cnr.ilc.lari.itant.cash.cql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// https://www.mvndoc.com/c/com.evolvedbinary.cql/corpusql-parser/index.html?overview-tree.html

public class MyVisitorFiles extends MyVisitor {
    private static final Logger log = LoggerFactory.getLogger(MyVisitor.class);

    public MyVisitorFiles() {
        super();

        this.status = new GenStatusFiles();
    }
}
