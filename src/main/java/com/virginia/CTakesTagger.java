package com.virginia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.StringUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;

//import org.apache.ctakes.core.cc.pretty.plaintext.PrettyTextWriter;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.ctakes.core.cc.pretty.plaintext.PrettyTextWriter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.commons.lang.exception.ExceptionUtils;

@RestController
public class CTakesTagger {

    private static final Logger LOGGER = Logger.getLogger(CTakesTagger.class);
    static AnalysisEngine pipeline;

    @PostConstruct
    public void init() {
      LOGGER.info("init(): called...");
      AggregateBuilder aggregateBuilder;
      try {
        aggregateBuilder = CTakesPipeline.getAggregateBuilder();
        pipeline = aggregateBuilder.createAggregate();
        if(pipeline == null) {
            LOGGER.info("init(): pipeline is null");
        }
      } catch (Exception e) {
          LOGGER.error("init(): " + e.getMessage());
      }
    }

    @RequestMapping(value = "/clinicalv2", method = RequestMethod.POST, consumes="application/json",produces="application/json" )
    public List<Classification> tag(@RequestBody Monologue im) {
      String sent = im.getMonologue();
      LOGGER.info("tag(): " + sent);
      List<Classification> result = new ArrayList<Classification>();
      try {
        JCas jcas = pipeline.newJCas();
        jcas.setDocumentText(sent);
        pipeline.process(jcas);
        result = extractFeatures(jcas);
        jcas.reset();
      } catch (Exception e) {
        LOGGER.error("tag(): " + ExceptionUtils.getStackTrace(e));
      }

      return result;
    }

    private List<Classification> extractFeatures(JCas jcas) {
      List<Classification> tags = new ArrayList<Classification>();

      Collection<Sentence> sentences = JCasUtil.select(jcas,Sentence.class);
      for (Sentence sentence : sentences) {
        Classification classification = new Classification(sentence.getCoveredText());
        tags.add(classification);
        Collection<IdentifiedAnnotation> identifiedAnnotations
            = JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, sentence );
        for ( IdentifiedAnnotation annotation : identifiedAnnotations ) {
            classification.addAnnotation(annotation);
        }
      }

      return tags;
    }

}
