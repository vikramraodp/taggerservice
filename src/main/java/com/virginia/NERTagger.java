package com.virginia;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.HasWord;
import java.util.List;
import java.util.ArrayList;

import edu.stanford.nlp.ie.ClassifierCombiner;
import org.apache.log4j.Logger;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.stanford.nlp.ie.NERClassifierCombiner;

import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import java.util.Collection;

@RestController
public class NERTagger {

    private static final Logger LOGGER = Logger.getLogger(NERTagger.class);
    static AbstractSequenceClassifier classifier;
    static AnalysisEngine pipeline;

    static{
        String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
        String trainedClassifier = "classifiers/demographics-ner-model.ser.gz";
        //classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        try {
          //classifier = new ClassifierCombiner(serializedClassifier,trainedClassifier);
          classifier = new NERClassifierCombiner(false, false, serializedClassifier, trainedClassifier);
        } catch(FileNotFoundException fnf) {
          LOGGER.error("cctor(): " + ExceptionUtils.getStackTrace(fnf));
          classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        } catch(IOException ioe) {
          LOGGER.error("cctor(): " + ExceptionUtils.getStackTrace(ioe));
          classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
        }

        AggregateBuilder aggregateBuilder;
        try {
          aggregateBuilder = SentenceDetectorPipeline.getAggregateBuilder();
          pipeline = aggregateBuilder.createAggregate();
          if(pipeline == null) {
              LOGGER.info("init(): pipeline is null");
          }
        } catch (Exception e) {
            LOGGER.error("init(): " + e.getMessage());
        }
    }

    @RequestMapping(value  = "/ner", method = RequestMethod.POST, consumes="application/json")
    public List<Tag> tag(@RequestBody Monologue im) {
        String sent = im.getMonologue();
        List<Tag> tags = new ArrayList<Tag>();

        try {
          JCas jcas = pipeline.newJCas();
          jcas.setDocumentText(sent);
          pipeline.process(jcas);
          Collection<Sentence> sentences = JCasUtil.select(jcas,Sentence.class);
          for (Sentence sentence : sentences) {
            LOGGER.info("tag(): " + sentence.getCoveredText());
            Collection<BaseToken> tokens = JCasUtil.selectCovered(BaseToken.class, sentence);
            List<HasWord> wdList = new ArrayList<HasWord>(tokens.size());
            for(BaseToken token : tokens){
              String word = token.getCoveredText();
              wdList.add(new WordToken(word));
            }
            List<CoreMap> tagging = classifier.classifySentence(wdList);
            String wd, annot;
            for(CoreMap item : tagging) {
                wd = item.get(CoreAnnotations.TextAnnotation.class);
                annot = item.get(CoreAnnotations.AnswerAnnotation.class);
                tags.add(new Tag(wd,annot));
            }
          }
          jcas.reset();
        } catch (Exception e) {
          LOGGER.error("tag(): " + ExceptionUtils.getStackTrace(e));
        }

        return tags;
        //return classifier.classifyToString(im.getMonologue());
    }
}
