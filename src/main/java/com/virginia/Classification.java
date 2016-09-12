package com.virginia;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

public class Classification extends ClassifiedAnnotation {

  private String sentence;
  private ClassifiedAnnotation medicalHistory;
  private ClassifiedAnnotation familyHistory;

  public Classification(String sentence) {
    super();

    this.sentence = sentence;
    medicalHistory = new ClassifiedAnnotation();
    familyHistory = new ClassifiedAnnotation();
  }

  public String getSentence() {
    return sentence;
  }

  public ClassifiedAnnotation getMedicalHistory() {
    return medicalHistory;
  }

  public ClassifiedAnnotation getFamilyHistory() {
    return familyHistory;
  }

  @Override
  public void addAnnotation(IdentifiedAnnotation annotation) {
    if("patient".equalsIgnoreCase(annotation.getSubject())) {
      if(annotation.getHistoryOf() == 1) {
          medicalHistory.addAnnotation(annotation);
      } else {
          super.addAnnotation(annotation);
      }
    } else {
      if(annotation.getHistoryOf() == 1) {
          familyHistory.addAnnotation(annotation);
      } else {
          super.addAnnotation(annotation);
      }
    }
  }

}
