package com.virginia;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;
import java.util.ArrayList;

@RestController
public class POSTagger {

    static MaxentTagger tagger;

    static{
        tagger =  new MaxentTagger("models/english-bidirectional-distsim.tagger");
    }

    @RequestMapping(value = "/pos", method = RequestMethod.POST, consumes="application/json")
    public List<Tag> tag(@RequestBody Monologue im) {
        
        String sent = im.getMonologue();
        String[] parts = sent.split("\\s+");
        List<HasWord> wdList = new ArrayList<HasWord>(parts.length);
        for(String w : parts) {
            wdList.add(new WordToken(w));
        }
        List<TaggedWord> taggedSentence = tagger.tagSentence(wdList);
        List<Tag> tags = new ArrayList<Tag>();
        for (TaggedWord word : taggedSentence) {
            tags.add(new Tag(word.word(),word.tag()));
        }
        return tags;
        //return tagger.tagString(im.getMonologue());
    }
}
