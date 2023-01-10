package it.cnr.ilc.lari.itant.cash.importers;


/**
 * This class implements the importing of epidoc. It provides methods to deal with the extra
 * info (metadata, annotations) to be extracted from the uploadd 
 */
public class EpiDoc {
    public String getText() {
        return "this is the text";
    }

    public static void main(String[] args) {
        System.out.println("\n\n\n\n\n\n\nTesting EpiDoc importer class");
        EpiDoc doc = new EpiDoc();
        System.out.println("Text: " + doc.getText());

    }
}
