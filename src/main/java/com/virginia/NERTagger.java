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

@RestController
public class NERTagger {

    static AbstractSequenceClassifier classifier;

    static{
        String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
        classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
    }

    @RequestMapping(value  = "/ner", method = RequestMethod.POST, consumes="application/json")
    public List<Tag> tag(@RequestBody Monologue im) {        
        String sent = im.getMonologue();
        String[] parts = sent.split("\\s+");
        List<HasWord> wdList = new ArrayList<HasWord>(parts.length);
        for(String w : parts) {
            wdList.add(new WordToken(w));
        }
        List<CoreMap> tagging = classifier.classifySentence(wdList);
        List<Tag> tags = new ArrayList<Tag>();
        String wd, annot;
        for(CoreMap item : tagging) {
            wd = item.get(CoreAnnotations.TextAnnotation.class);
            annot = item.get(CoreAnnotations.AnswerAnnotation.class);
            tags.add(new Tag(wd,annot));
        }
        return tags;
        //return classifier.classifyToString(im.getMonologue());
    }
}
