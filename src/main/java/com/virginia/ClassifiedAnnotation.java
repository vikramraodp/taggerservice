package com.virginia;

import java.util.List;
import java.util.ArrayList;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

import org.apache.ctakes.typesystem.type.textsem.ContextAnnotation;
import org.apache.ctakes.typesystem.type.textsem.DateAnnotation;

import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;

import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.BodyLateralityModifier;
import org.apache.ctakes.typesystem.type.textsem.BodySideModifier;
import org.apache.ctakes.typesystem.type.textsem.ConditionalModifier;
import org.apache.ctakes.typesystem.type.textsem.CourseModifier;
import org.apache.ctakes.typesystem.type.textsem.GenericModifier;
import org.apache.ctakes.typesystem.type.textsem.HistoryOfModifier;
import org.apache.ctakes.typesystem.type.textsem.LabDeltaFlagModifier;
import org.apache.ctakes.typesystem.type.textsem.LabEstimatedModifier;
import org.apache.ctakes.typesystem.type.textsem.LabInterpretationModifier;
import org.apache.ctakes.typesystem.type.textsem.LabReferenceRangeModifier;
import org.apache.ctakes.typesystem.type.textsem.LabValueModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationAllergyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDosageModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDurationModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFormModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFrequencyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationRouteModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStatusChangeModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStrengthModifier;
import org.apache.ctakes.typesystem.type.textsem.PolarityModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureDeviceModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMethodModifier;
import org.apache.ctakes.typesystem.type.textsem.SeverityModifier;
import org.apache.ctakes.typesystem.type.textsem.SubjectModifier;
import org.apache.ctakes.typesystem.type.textsem.UncertaintyModifier;

import org.apache.ctakes.smokingstatus.type.NonSmokerNamedEntityAnnotation;
//import org.apache.ctakes.padtermspotter.type.PADHit;
//import org.apache.ctakes.padtermspotter.type.PADLocation;
import org.apache.ctakes.typesystem.type.textsem.PersonTitleAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RangeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RomanNumeralAnnotation;
import org.apache.ctakes.smokingstatus.type.SmokerNamedEntityAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.smokingstatus.type.UnknownSmokerNamedEntityAnnotation;
import org.apache.ctakes.typesystem.type.textsem.FractionAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MeasurementAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.relation.TemporalTextRelation;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;

import org.apache.commons.lang3.StringUtils;

public class ClassifiedAnnotation {

  protected String signsAndSymptoms;
  protected String findings;
  protected String labEvents;
  protected String medications;
  protected String procedures;
  protected List<UnclassifiedAnnotation> unclassifedAnnotations;

  public ClassifiedAnnotation() {
    signsAndSymptoms = "";
    findings = "";
    labEvents = "";
    medications = "";
    procedures = "";
    unclassifedAnnotations = new ArrayList<UnclassifiedAnnotation>();
  }

  public String getSignsAndSymptoms() {
    return signsAndSymptoms;
  }

  public String getFindings() {
    return findings;
  }

  public String getLabEvents() {
    return labEvents;
  }

  public String getMedications() {
    return medications;
  }

  public String getProcedures() {
    return procedures;
  }

  public List<UnclassifiedAnnotation> getUnclassifiedAnnotations() {
    return unclassifedAnnotations;
  }

  public void addAnnotation(IdentifiedAnnotation annotation) {
    String text = getDisplayText(annotation);
    if(text == null || text.length() == 0) {
      unclassifedAnnotations.add(new UnclassifiedAnnotation(annotation));
      return;
    }

    if(annotation.getPolarity() < 0) {
      if(!StringUtils.startsWithIgnoreCase(text,"No")) {
        text = "No " + text;
      }
    }
    if(annotation instanceof SignSymptomMention &&
          !(StringUtils.containsIgnoreCase(text,"history") ||
                StringUtils.containsIgnoreCase(text,"relatives"))) {
        signsAndSymptoms =  addAnnotationInternal(signsAndSymptoms, text);
    } else if (annotation instanceof DiseaseDisorderMention) {
        findings = addAnnotationInternal(findings, text);
    } else if(annotation instanceof LabMention) {
        labEvents = addAnnotationInternal(labEvents,text);
    } else if(annotation instanceof  MedicationEventMention || annotation instanceof  MedicationMention) {
        medications = addAnnotationInternal(medications, text);
    } else if(annotation instanceof  ProcedureMention) {
        procedures = addAnnotationInternal(procedures, text);
    } else {
        unclassifedAnnotations.add(new UnclassifiedAnnotation(annotation));
    }
  }

  protected String addAnnotationInternal(String baseAnnotation, String annotation) {
    if(StringUtils.containsIgnoreCase(baseAnnotation, annotation)) {
      return baseAnnotation;
    }

    StringBuffer newAnnotation = new StringBuffer();
    newAnnotation.append(baseAnnotation);
    if(newAnnotation.length() > 0) {
      newAnnotation.append(", ");
    }
    newAnnotation.append(annotation);
    return newAnnotation.toString();
  }

  protected String getDisplayText(IdentifiedAnnotation a) {
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
