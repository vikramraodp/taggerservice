package com.virginia;

import java.io.FileNotFoundException;

import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine;
import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.constituency.parser.ae.ConstituencyParser;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.resource.FileResourceImpl;
import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE;
import org.apache.ctakes.dependency.parser.ae.ClearNLPSemanticRoleLabelerAE;
import org.apache.ctakes.dictionary.lookup2.ae.AbstractJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.temporal.ae.BackwardsTimeAnnotator;
import org.apache.ctakes.temporal.ae.DocTimeRelAnnotator;
import org.apache.ctakes.temporal.ae.EventAnnotator;
import org.apache.ctakes.temporal.ae.EventEventRelationAnnotator;
import org.apache.ctakes.temporal.ae.EventTimeRelationAnnotator;
import org.apache.ctakes.temporal.ae.EventTimeSelfRelationAnnotator;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.CopyNPChunksToLookupWindowAnnotations;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.RemoveEnclosedLookupWindows;
import org.apache.ctakes.temporal.pipelines.FullTemporalExtractionPipeline.CopyPropertiesToTemporalEventAnnotator;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import org.apache.ctakes.assertion.medfacts.cleartk.HistoryCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.ConditionalCleartkAnalysisEngine;

import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.lang.ClassNotFoundException;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.net.URL;
import java.net.URISyntaxException;

public class CTakesPipeline {

	public static AggregateBuilder getAggregateBuilder() throws Exception {
		AggregateBuilder builder = new AggregateBuilder();
		//builder.add(ClinicalPipelineFactory.getFastPipeline());
//	      builder.add( ClinicalPipelineFactory.getTokenProcessingPipeline() );
		  builder.add(AnalysisEngineFactory.createEngineDescription(SimpleSegmentAnnotator.class));
		  builder.add( SentenceDetector.createAnnotatorDescription() );
	      builder.add( TokenizerAnnotatorPTB.createAnnotatorDescription() );
	      builder.add( LvgAnnotator.createAnnotatorDescription() );
	      builder.add( ContextDependentTokenizerAnnotator.createAnnotatorDescription() );
	      builder.add( POSTagger.createAnnotatorDescription() );
	      builder.add( Chunker.createAnnotatorDescription() );
	      builder.add( ClinicalPipelineFactory.getStandardChunkAdjusterAnnotator() );

	      builder.add( AnalysisEngineFactory.createEngineDescription( CopyNPChunksToLookupWindowAnnotations.class ) );
	      builder.add( AnalysisEngineFactory.createEngineDescription( RemoveEnclosedLookupWindows.class ) );
	      builder.add( AnalysisEngineFactory.createEngineDescription( DefaultJCasTermAnnotator.class,
	               AbstractJCasTermAnnotator.PARAM_WINDOW_ANNOT_KEY,
	               "org.apache.ctakes.typesystem.type.textspan.Sentence",
	               JCasTermAnnotator.DICTIONARY_DESCRIPTOR_KEY,
	               "org/apache/ctakes/dictionary/lookup/fast/cTakesHsql.xml")
	      );

        builder.add( AllergyAnnotator.createAnnotatorDescription() );

	      builder.add( ClearNLPDependencyParserAE.createAnnotatorDescription() );
	      builder.add( PolarityCleartkAnalysisEngine.createAnnotatorDescription() );
	      builder.add( UncertaintyCleartkAnalysisEngine.createAnnotatorDescription() );
        builder.add( HistoryCleartkAnalysisEngine.createAnnotatorDescription() );
        builder.add( SubjectCleartkAnalysisEngine.createAnnotatorDescription() );
        builder.add( ConditionalCleartkAnalysisEngine.createAnnotatorDescription() );
	      builder.add( AnalysisEngineFactory.createEngineDescription( ClearNLPSemanticRoleLabelerAE.class ) );
	      builder.add( AnalysisEngineFactory.createEngineDescription( ConstituencyParser.class ) );

			// Add BackwardsTimeAnnotator
			builder.add(BackwardsTimeAnnotator
					.createAnnotatorDescription("/org/apache/ctakes/temporal/ae/timeannotator/model.jar"));
			// Add EventAnnotator
			builder.add(EventAnnotator
					.createAnnotatorDescription("/org/apache/ctakes/temporal/ae/eventannotator/model.jar"));
			builder.add( AnalysisEngineFactory.createEngineDescription( CopyPropertiesToTemporalEventAnnotator.class ) );
			// Add Document Time Relative Annotator
			//link event to eventMention
			builder.add(AnalysisEngineFactory.createEngineDescription(AddEvent.class));
			builder.add(DocTimeRelAnnotator
   					.createAnnotatorDescription("/org/apache/ctakes/temporal/ae/doctimerel/model.jar"));
			// Add Event to Event Relation Annotator
			builder.add(EventTimeSelfRelationAnnotator
					.createEngineDescription("/org/apache/ctakes/temporal/ae/eventtime/20150629/model.jar"));
			// Add Event to Event Relation Annotator
			builder.add(EventEventRelationAnnotator
					.createAnnotatorDescription("/org/apache/ctakes/temporal/ae/eventevent/20150630/model.jar"));

      //builder.add(AnalysisEngineFactory.createEngineDescription(RelationExtractorAnnotator.class));


//	      builder.add( PolarityCleartkAnalysisEngine.createAnnotatorDescription() );
//	      builder.add( UncertaintyCleartkAnalysisEngine.createAnnotatorDescription() );
//	      builder.add( HistoryCleartkAnalysisEngine.createAnnotatorDescription() );
//	      builder.add( ConditionalCleartkAnalysisEngine.createAnnotatorDescription() );
//	      builder.add( GenericCleartkAnalysisEngine.createAnnotatorDescription() );
//	      builder.add( SubjectCleartkAnalysisEngine.createAnnotatorDescription() );

    //builder.add( AnalysisEngineFactory.createEngineDescription( RemoveEnclosedAnnotations.class ) );
    builder.add( AnalysisEngineFactory.createEngineDescription( RemoveExcludedAnnotations.class ) );

		return builder;
	}


	public static class AddEvent extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {
		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			for (EventMention emention : Lists.newArrayList(JCasUtil.select(
					jCas,
					EventMention.class))) {
				EventProperties eventProperties = new org.apache.ctakes.typesystem.type.refsem.EventProperties(jCas);

				// create the event object
				Event event = new Event(jCas);

				// add the links between event, mention and properties
				event.setProperties(eventProperties);
				emention.setEvent(event);

				// add the annotations to the indexes
				eventProperties.addToIndexes();
				event.addToIndexes();
			}
		}
	}

  public static class RemoveExcludedAnnotations extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {

      private static final Logger LOGGER = Logger.getLogger(RemoveExcludedAnnotations.class);
      private static HashMap<String, Object> exclusionLookup = null;

      @Override
      public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
          List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jCas, IdentifiedAnnotation.class));
          for(int i = annotations.size()-2; i >= 0; i--) {
            IdentifiedAnnotation annotation = annotations.get(i+1);
            if(isExcluded(annotation)) {
              annotations.remove(i+1);
              annotation.removeFromIndexes();
            }
          }
          if(annotations.size() > 0) {
            IdentifiedAnnotation annotation = annotations.get(0);
            if(isExcluded(annotation)) {
              annotations.remove(0);
              annotation.removeFromIndexes();
            }
          }
        } catch(IOException ioe ) {
          LOGGER.error("process(): " + ExceptionUtils.getStackTrace(ioe));
        } catch(ClassNotFoundException cnfe) {
          LOGGER.error("process(): " + ExceptionUtils.getStackTrace(cnfe));
        } catch(URISyntaxException use) {
          LOGGER.error("process(): " + ExceptionUtils.getStackTrace(use));
        }
      }

      private boolean isExcluded(IdentifiedAnnotation annotation) throws IOException, ClassNotFoundException, URISyntaxException {
        loadExclusionLookup();

        boolean isExcluded = false;

        String[] tmp = annotation.getClass().getName().split("\\.");
        String semGroup = tmp[tmp.length-1];
        String text = getDisplayText(annotation);

        ArrayList<String> annotationEx = (ArrayList<String>)exclusionLookup.get(text);
        if(annotationEx != null) {
          isExcluded = annotationEx.contains(semGroup);
        }

        return isExcluded;
      }

      private void loadExclusionLookup() throws IOException, ClassNotFoundException, URISyntaxException {
        if(exclusionLookup == null) {
          //URL resourceUrl = URL.class.getResource("/WEB-INF/classes/exclusions");
          URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource("exclusions");
          if(resourceUrl == null) {
            LOGGER.info("loadExclusionLookup(): resourceUrl is null");
          } else {
            LOGGER.info("loadExclusionLookup(): resourceUrl path " + resourceUrl.toURI());
          }
          File file = new File(resourceUrl.toURI());
          FileInputStream f = new FileInputStream(file);
          ObjectInputStream s = new ObjectInputStream(f);

          exclusionLookup = (HashMap<String, Object>) s.readObject();
          s.close();
        }
      }

      private String getDisplayText(IdentifiedAnnotation a) {
        String text = a.getCoveredText();
        FSArray ontologyConcepts = a.getOntologyConceptArr();
        if(ontologyConcepts != null) {
          for (int i = 0; i < ontologyConcepts.size(); ++i) {
            if(ontologyConcepts.get(i) instanceof UmlsConcept) {
              UmlsConcept concept = (UmlsConcept)ontologyConcepts.get(i);
              text = concept.getPreferredText();
              break;
            }
          }
        }
        return text;
      }
  }

  public static class RemoveEnclosedAnnotations extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(RemoveEnclosedAnnotations.class);

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      List<Class<? extends IdentifiedAnnotation>> classes = getAnnotationClasses(jCas);

      for(Class<? extends IdentifiedAnnotation> annClass : classes) {
        String[] tmp = annClass.getName().split("\\.");
        String semGroup = tmp[tmp.length-1];

        List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jCas, annClass));
        for(int i = annotations.size()-2; i >= 0; i--){
          IdentifiedAnnotation ann1 = annotations.get(i);
          IdentifiedAnnotation ann2 = annotations.get(i+1);
          if(ann1.getBegin() <= ann2.getBegin() && ann1.getEnd() >= ann2.getEnd()){
            /// ann1 envelops or encloses ann2
            if(!Character.isUpperCase(getDisplayText(ann2).charAt(0))) {
              LOGGER.info("removed annotation: " + ann2.getCoveredText() +"/" + getDisplayText(ann2) +"(" + semGroup + ")");
              LOGGER.info("keeping annotation: " + ann1.getCoveredText() +"/" + getDisplayText(ann1) +"(" + semGroup + ")");

              annotations.remove(i+1);
              ann2.removeFromIndexes();
            }
          }
        }
      }
    }

    private List<Class<? extends IdentifiedAnnotation>> getAnnotationClasses(JCas jCas) throws AnalysisEngineProcessException {
      List<Class<? extends IdentifiedAnnotation>> classes = new ArrayList<Class<? extends IdentifiedAnnotation>>();
      List<IdentifiedAnnotation> annotations = new ArrayList<>(JCasUtil.select(jCas, IdentifiedAnnotation.class));
      for(IdentifiedAnnotation annotation : annotations) {
        if(!classes.contains(annotation.getClass())) {
          classes.add(annotation.getClass());
        }
      }
      return classes;
    }

    private String getDisplayText(IdentifiedAnnotation a) {
      String text = a.getCoveredText();
      FSArray ontologyConcepts = a.getOntologyConceptArr();
      if(ontologyConcepts != null) {
        for (int i = 0; i < ontologyConcepts.size(); ++i) {
          if(ontologyConcepts.get(i) instanceof UmlsConcept) {
            UmlsConcept concept = (UmlsConcept)ontologyConcepts.get(i);
            text = concept.getPreferredText();
            break;
          }
        }
      }
      return text;
    }

  }
}
