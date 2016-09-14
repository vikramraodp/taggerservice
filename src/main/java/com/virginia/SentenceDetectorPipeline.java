package com.virginia;

import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.dictionary.lookup2.ae.AbstractJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.CopyNPChunksToLookupWindowAnnotations;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.RemoveEnclosedLookupWindows;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

public class SentenceDetectorPipeline {

	public static AggregateBuilder getAggregateBuilder() throws Exception {
		AggregateBuilder builder = new AggregateBuilder();
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

    return builder;
	}

}
