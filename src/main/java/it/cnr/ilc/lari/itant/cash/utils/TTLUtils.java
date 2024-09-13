package it.cnr.ilc.lari.itant.cash.utils;

import java.io.StringWriter;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.ilc.lari.itant.cash.om.Annotation;
import it.cnr.ilc.lari.itant.cash.om.FileInfo;

public class TTLUtils {
    static final Logger logger = LoggerFactory.getLogger(TTLUtils.class);

    public static String toTTL(FileInfo node, List<Annotation> annotations) {
		StringBuilder sb = new StringBuilder();
		for ( Annotation a: annotations) {
			sb.append(toTTL(node, a) + "\n");
		}
        return sb.toString();
    }


    public static String toTTL(FileInfo node, Annotation annotation) {
        String ex = "http://example.org/";

        // Create IRIs for the resources we want to add.
        IRI picasso = Values.iri(ex, "Picasso");
        IRI artist = Values.iri(ex, "Artist");

        // Create a new, empty Model object.
        Model model = new TreeModel();

        // add our first statement: Picasso is an Artist
        model.add(picasso, RDF.TYPE, artist);

        // second statement: Picasso's first name is "Pablo".
        model.add(picasso, FOAF.FIRST_NAME, Values.literal("Pablo"));

        StringWriter stringWriter = new StringWriter();
        Rio.write(model, stringWriter, RDFFormat.TURTLE);
        return stringWriter.toString();
    }
}
