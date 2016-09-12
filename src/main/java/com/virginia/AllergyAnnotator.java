package com.virginia;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author SPF , chip-nlp
 * @version %I%
 * @since 7/10/2015
 */
final public class AllergyAnnotator extends JCasAnnotator_ImplBase {

   static private final Logger LOGGER = Logger.getLogger( "AllergyAnnotator" );


   static private enum AllergyPreExpression {
      COLON_LIST( "\\ballergies:\\s++[a-z,'\"\\t ]*" ),
      ALLERGIC_TO( "\\ballergic( reaction)? to:?\\s++[a-z\\,'\"\\t ]*" );
      final private Pattern __pattern;
      private AllergyPreExpression( final String regex ) {
         __pattern = Pattern.compile( regex );
      }
      private Matcher getMatcher( final CharSequence windowText ) {
         return __pattern.matcher( windowText );
      }
   }

   static private enum AllergyPostExpression {
      ALLERGY( "[a-z]* allergy"),
      HYPERSENSITIVITY( "[a-z]* (hyper)?sensitivity");
      final private Pattern __pattern;
      private AllergyPostExpression( final String regex ) {
         __pattern = Pattern.compile( regex );
      }
      private Matcher getMatcher( final CharSequence windowText ) {
         return __pattern.matcher( windowText );
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void process( final JCas jcas ) throws AnalysisEngineProcessException {
      LOGGER.info( "Starting processing" );

      final String docText = jcas.getDocumentText();
      final Collection<MedicationMention> medications = JCasUtil.select( jcas, MedicationMention.class );

      for ( MedicationMention medication : medications ) {
         final int windowBegin = Math.max( 0, medication.getBegin() - 40 );
         final String preWindowText = docText.substring( windowBegin, medication.getEnd() ).toLowerCase();
         for ( AllergyPreExpression preExpression : AllergyPreExpression.values() ) {
            final Matcher matcher = preExpression.getMatcher( preWindowText );
            while ( matcher.find() ) {
               storeAllergy( jcas, windowBegin + matcher.start(), medication.getEnd() );
               // could break from loop but there may be a wider context
            }
         }
         final int windowEnd = Math.min( docText.length(), medication.getEnd() + 20 );
         final String postWindowText = docText.substring( medication.getBegin(), windowEnd ).toLowerCase();
         for ( AllergyPostExpression postExpression : AllergyPostExpression.values() ) {
            final Matcher matcher = postExpression.getMatcher( postWindowText );
            while ( matcher.find() ) {
               storeAllergy( jcas, medication.getBegin(), windowBegin + matcher.end() );
               // could break from loop but there may be a wider context
            }
         }
      }
      LOGGER.info( "Finished processing" );
   }


   static private void storeAllergy( final JCas jcas, final int matchBegin, final int matchEnd ) {
      final UmlsConcept umlsConcept = new UmlsConcept( jcas );
      umlsConcept.setCodingScheme( "AllergyPrototype" );
      // C0020517 is a generic CUI for hypersensitivity / allergy
      umlsConcept.setCui( "C0020517" );
      umlsConcept.setTui( "T046" );
      umlsConcept.setPreferredText( "Hypersensitivity" );
      final FSArray conceptArr = new FSArray( jcas, 1 );
      conceptArr.set( 0, umlsConcept );

      final IdentifiedAnnotation annotation = new SignSymptomMention( jcas );
      annotation.setTypeID( CONST.NE_TYPE_ID_FINDING );
      annotation.setBegin( matchBegin );
      annotation.setEnd( matchEnd );
      annotation.setOntologyConceptArr( conceptArr );
//            annotation.setDiscoveryTechnique( CONST.NE_DISCOVERY_TECH_DICT_LOOKUP );
      annotation.addToIndexes();
   }



   static public AnalysisEngineDescription createAnnotatorDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createEngineDescription( AllergyAnnotator.class );
   }

}
