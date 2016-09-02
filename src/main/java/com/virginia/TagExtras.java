package com.virginia;

public class TagExtras {

  private String preferredText;
  private int historyOf;
  private String pos;
  private String subject;

  public TagExtras(String preferredText, int historyOf) {
    this.preferredText = preferredText;
    this.historyOf = historyOf;
    this.pos = "";
    this.subject = "";
  }

  public TagExtras() {
    this.preferredText = "";
    this.historyOf = 0;
    this.pos = "";
    this.subject = "";
  }

  public String getPreferredText() {
    return preferredText;
  }

  public int getHistoryOf() {
    return historyOf;
  }

  public String getPOS() {
    return pos;
  }

  public String getSubject() {
    return subject;
  }

  public void setPreferredText(String preferredText) {
    this .preferredText = preferredText;
  }

  public void setHistoryOf(int historyOf) {
    this.historyOf = historyOf;
  }

  public void setPOS(String pos) {
    this.pos = pos;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

}
