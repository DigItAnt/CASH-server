package it.cnr.ilc.lari.itant.cash.utils;

import java.io.StringWriter;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
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
        //IRI picasso = Values.iri(ex, "Picasso");

        ValueFactory vf = Values.getValueFactory();

        ModelBuilder builder = new ModelBuilder();
        builder.setNamespace("frac", "http://www.w3.org/ns/lemon/frac#")
            .setNamespace("powla", "http://purl.org/powla/powla.owl#")
            .setNamespace("ontolex", "http://www.w3.org/ns/lemon/ontolex#")
            .setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
            .setNamespace("lexinfo", "http://www.lexinfo.net/ontology/2.0/lexinfo#")
            .setNamespace("dct", "http://purl.org/dc/terms/")
            .setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
            .setNamespace("oa", "http://www.w3.org/ns/oa#")
            //.setNamespace("", "")
        ;
            
        BNode frac_total = vf.createBNode();

        builder.add(frac_total, RDF.TYPE, "frac:Frequency");
        builder.add(frac_total, Values.iri("frac:measure"), "inscriptions");
        builder.add(frac_total, Values.iri("rdf:value"), "300");

        builder.subject("https://digitant.ilc.cnr.it/data/inscriptions/Oscan/ItAnt_Oscan_Corpus")
            .add(RDF.TYPE, "frac:Corpus")
            .add(RDF.TYPE, "powla:Corpus")
            .add("frac:total", frac_total)
            ;


        //builder.add(picasso, RDF.TYPE, artist);
        //builder.add(picasso, FOAF.FIRST_NAME, Values.literal("Pablo"));

        Model model = builder.build();

        StringWriter stringWriter = new StringWriter();
        Rio.write(model, stringWriter, RDFFormat.TURTLE);
        return stringWriter.toString();
    }
}
