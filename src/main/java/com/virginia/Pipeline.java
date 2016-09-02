package com.virginia;

import java.io.FileNotFoundException;

import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.resource.FileResourceImpl;
import org.apache.ctakes.dictionary.lookup2.ae.AbstractJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ResourceInitializationException;

import org.apache.log4j.Logger;

public class Pipeline {

  private static final Logger LOGGER = Logger.getLogger(Pipeline.class);

	public static AggregateBuilder getAggregateBuilder() {
		AggregateBuilder builder = new AggregateBuilder();
    try {
	      builder.add( ClinicalPipelineFactory.getTokenProcessingPipeline() );
	      builder.add( AnalysisEngineFactory.createEngineDescription( DefaultJCasTermAnnotator.class,
	               AbstractJCasTermAnnotator.PARAM_WINDOW_ANNOT_KEY,
	               "org.apache.ctakes.typesystem.type.textspan.Sentence",
	               JCasTermAnnotator.DICTIONARY_DESCRIPTOR_KEY,
	               "org/apache/ctakes/dictionary/lookup/fast/cTakesHsql.xml" )
	         );
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
		return builder;
	}

}
