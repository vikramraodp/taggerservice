package com.virginia;

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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.commons.lang.exception.ExceptionUtils;

@RestController
public class ClinicalTagger {

    private static final Logger LOGGER = Logger.getLogger(ClinicalTagger.class);
    static AnalysisEngine pipeline;

    @PostConstruct
    public void init() {
      LOGGER.info("init(): called...");
      AggregateBuilder aggregateBuilder;
      try {
        aggregateBuilder = Pipeline.getAggregateBuilder();
        pipeline = aggregateBuilder.createAggregate();
        if(pipeline == null) {
            LOGGER.info("init(): pipeline is null");
        }
      } catch (Exception e) {
          LOGGER.error("init(): " + e.getMessage());
      }
    }

    @RequestMapping(value = "/clinical", method = RequestMethod.POST, consumes="application/json")
    public List<Tag> tag(@RequestBody Monologue im) {
      String sent = im.getMonologue();
      LOGGER.info("tag(): " + sent);
      List<Tag> result = new ArrayList<Tag>();
      try {
        JCas jcas = pipeline.newJCas();
        jcas.setDocumentText(sent);
        pipeline.process(jcas);
        result = formatResults(jcas);
        jcas.reset();
      } catch (Exception e) {
        LOGGER.error("tag(): " + ExceptionUtils.getStackTrace(e));
      }

      return result;
    }

    private List<Tag> formatResults(JCas jcas) {
      List<Tag> tags = new ArrayList<Tag>();
      Collection<IdentifiedAnnotation> annotations = JCasUtil.select(jcas,IdentifiedAnnotation.class);
      for (IdentifiedAnnotation a : annotations) {
        String annotationName = a.getType().getShortName();
        annotationName = annotationName.replace("Mention","");
        annotationName = annotationName.replace("Annotation","");

        Tag tag = new Tag(a.getCoveredText(),annotationName);
        tags.add(tag);

        FSArray ontologyConcepts = a.getOntologyConceptArr();
        if(ontologyConcepts != null) {
          for (int i = 0; i < ontologyConcepts.size(); ++i) {
            if(ontologyConcepts.get(i) instanceof UmlsConcept) {
              UmlsConcept concept = (UmlsConcept)ontologyConcepts.get(i);
              tag.getExtras().setPreferredText(concept.getPreferredText());
              break;
            }
          }
        }
      }
      return tags;
    }

    private String getPOS(FeatureStructure fs) {
      String pos = "";
      List<?> plist = fs.getType().getFeatures();
      for (Object obj : plist) {
        if (obj instanceof Feature) {
          Feature feature = (Feature) obj;
          if (feature.getRange().isPrimitive()) {
            if(feature.getName().equalsIgnoreCase("partOfSpeech")) {
              pos = fs.getFeatureValueAsString(feature);
              break;
            }
          } else if (feature.getRange().isArray()) {
              FeatureStructure featval = fs.getFeatureValue(feature);
              FSArray valarray = (FSArray) featval;
              if(valarray != null) {
                for (int i = 0; i < valarray.size(); ++i) {
                  FeatureStructure temp = valarray.get(i);
                  pos = getPOS(temp);
                  if(pos.length() > 0) {
                    break;
                  }
                }
              }
          }
        }
      }
      return pos;
    }
}
