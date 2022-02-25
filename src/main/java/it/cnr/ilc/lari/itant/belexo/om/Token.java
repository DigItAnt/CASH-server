package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.belexo.DBManager;

public class Token {
    public static final String TOKENIZATION_LAYER = "tokenization";
    private static final Logger log = LoggerFactory.getLogger(Annotation.class);


    long ID = -1;
    String text;
    String xmlid;
    int position;
    int begin;
    int end;
    long node;
    String source;
    boolean imported;

    public long getID() {
        return ID;
    }
    public void setID(long iD) {
        ID = iD;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getXmlid() {
        return xmlid;
    }
    public void setXmlid(String xmlid) {
        this.xmlid = xmlid;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getBegin() {
        return begin;
    }
    public void setBegin(int begin) {
        this.begin = begin;
    }
    public int getEnd() {
        return end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public long getNode() {
        return node;
    }
    public void setNode(long node) {
        this.node = node;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public boolean isImported() {
        return imported;
    }
    public void setImported(boolean imported) {
        this.imported = imported;
    }
}
