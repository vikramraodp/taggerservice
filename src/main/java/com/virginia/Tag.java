package com.virginia;

public class Tag {

  private String word;
  private String annotation;
  private TagExtras extras;

  public Tag(String word, String annotation) {
    this.word = word;
    this.annotation = annotation;
    this.extras = new TagExtras();
  }

  public Tag(String word, String annotation, TagExtras extras) {
    this.word = word;
    this.annotation = annotation;
    this.extras = extras;
  }

  public String getWord() { return word; }
  public String getAnnotation() { return annotation; }
  public TagExtras getExtras() { return extras; }

}
