package testpack.custom;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class testClass {


    public static void main(String[] args) throws IOException, UIMAException, SAXException {

        final String ctakesDir = "E:\\apache-ctakes-3.2.2\\"; // set this from somewhere
        final String aeFile = Paths.get(ctakesDir, "desc", "ctakes-clinical-pipeline", "desc", "analysis_engine", "AggregatePlaintextUMLSProcessor.xml").toString();
        XMLInputSource in = new XMLInputSource(aeFile);
        ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

        //create AE here
        AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);

        JCas jcas = ae.newJCas();

        // set text
        jcas.setDocumentText("tylenol gave me a headache");
        ae.process(jcas);

        final boolean printCuis = Arrays.asList( args ).contains( "cuis" );
        final Collection<String> codes = new ArrayList<String>();
        for ( IdentifiedAnnotation entity : JCasUtil.select( jcas, IdentifiedAnnotation.class ) ) {

            System.out.println( "Entity: " + entity.getCoveredText()
                    + " === Polarity: " + entity.getPolarity()
                    + " === Uncertain? " + (entity.getUncertainty() == CONST.NE_UNCERTAINTY_PRESENT)
                    + " === Subject: " + entity.getSubject()
                    + " === Generic? " + (entity.getGeneric() == CONST.NE_GENERIC_TRUE)
                    + " === Conditional? " + (entity.getConditional() == CONST.NE_CONDITIONAL_TRUE)
                    + " === History? " + (entity.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT)
            );

            if ( printCuis ) {
                codes.clear();
                codes.addAll( getCUIs( entity ) );
                for ( String cui : codes ) {
                    System.out.print( cui + " " );
                }
                System.out.println();
            }

        }

        jcas.reset();
        ae.destroy();

        System.out.println("Ended....");
//        if ( args.length > 0 ) {
//            aed.toXML( new FileWriter( args[ 0 ] ) );
//        }

    }

    static private Collection<String> getCUIs( final IdentifiedAnnotation identifiedAnnotation ) {
        final FSArray fsArray = identifiedAnnotation.getOntologyConceptArr();
        if ( fsArray == null ) {
            return Collections.emptySet();
        }
        final FeatureStructure[] featureStructures = fsArray.toArray();
        final Collection<String> cuis = new ArrayList<String>( featureStructures.length );
        for ( FeatureStructure featureStructure : featureStructures ) {
            if ( featureStructure instanceof UmlsConcept) {
                final UmlsConcept umlsConcept = (UmlsConcept)featureStructure;
                final String cui = umlsConcept.getCui();
                final String tui = umlsConcept.getTui();
                if ( tui != null && !tui.isEmpty() ) {
                    cuis.add( cui + "_" + tui );
                } else {
                    cuis.add( cui );
                }
            }
        }
        return cuis;
    }


}
