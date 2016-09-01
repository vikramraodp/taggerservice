package com.virginia;

import edu.stanford.nlp.ling.HasWord; 

class WordToken implements HasWord {

    private String wd = null;

    public WordToken(String wd) {
        this.wd = wd;
    }

    public String word() {
        return wd;
    }

    public void setWord(String string) {
        this.wd = string;
    }

}
