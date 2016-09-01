package com.virginia;

public class Tag {

  private final String word;
  private final String annotation;

  public Tag(String word, String annotation) {
    this.word = word;
    this.annotation = annotation;
  }

  public String getWord() { return word; }
  public String getAnnotation() { return annotation; }

}
