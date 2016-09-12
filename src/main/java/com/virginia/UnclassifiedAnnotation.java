package com.virginia;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;

public class UnclassifiedAnnotation {

  private String annotationType;
  private String coveredText;
  private String preferredText;
  private int historyOf;
  private String subject;
  private boolean conditional;
  private float confidence;
  private boolean generic;
  private int dicoveryTechnique;
  private int polarity;
  private int uncertainity;

  public UnclassifiedAnnotation(IdentifiedAnnotation annotation) {
    coveredText = annotation.getCoveredText();
    preferredText = getDisplayText(annotation);
    historyOf = annotation.getHistoryOf();
    subject = annotation.getSubject();
    conditional = annotation.getConditional();
    confidence = annotation.getConfidence();
    generic = annotation.getGeneric();
    dicoveryTechnique = annotation.getDiscoveryTechnique();
    polarity = annotation.getPolarity();
    uncertainity = annotation.getUncertainty();

    String[] tmp = annotation.getClass().getName().split("\\.");
    annotationType = tmp[tmp.length-1];

  }

  public String getAnnotationType() {
    return annotationType;
  }

  public String getCoveredText() {
    return coveredText;
  }

  public String getPreferredText() {
    return preferredText;
  }

  public int getHistoryOf() {
    return historyOf;
  }

  public String getSubject() {
    return subject;
  }

  public boolean getConditional() {
    return conditional;
  }

  public float getConfidence() {
    return confidence;
  }

  public boolean getGeneric() {
    return generic;
  }

  public int getDiscoveryTechnique() {
    return dicoveryTechnique;
  }

  public int getPolarity() {
    return polarity;
  }

  public int getUncertainty() {
    return uncertainity;
  }

  private String getDisplayText(IdentifiedAnnotation a) {
    String text = a.getCoveredText();
    FSArray ontologyConcepts = a.getOntologyConceptArr();
    if(ontologyConcepts != null) {
      for (int i = 0; i < ontologyConcepts.size(); ++i) {
        if(ontologyConcepts.get(i) instanceof UmlsConcept) {
          UmlsConcept concept = (UmlsConcept)ontologyConcepts.get(i);
          text = concept.getPreferredText();
          break;
        }
      }
    }
    return text;
  }
}
