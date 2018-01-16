package de.lmu.cis.pocoweb;

import com.google.gson.Gson;
import org.raml.jaxrs.example.model.Book;

class ProjectBook extends Book {
  public ProjectBook(Book s) {
    this.author = s.getAuthor();
    this.title = s.getTitle();
    this.projectId = s.getProjectId();
    this.language = s.getLanguage();
    this.profilerUrl = s.getProfilerUrl();
    this.year = s.getYear();
    this.pages = s.getPages();
    this.isBook = s.getIsBook();
    this.pageIds = s.getPageIds();
    this.description = s.getDescription();
    if (description == null || description.isEmpty()) {
      this.description = "{}";
    }
  }
  public void setOcrId(int id) {
    OcrData data = loadOcrData();
    data.ocrId = id;
    setOcrData(data);
  }
  public int getOcrId() { return loadOcrData().ocrId; }

  public void setOcrEngine(String engine) {
    OcrData data = loadOcrData();
    data.ocrEngine = engine;
    setOcrData(data);
  }
  public String getOcrEngine() { return loadOcrData().ocrEngine; }

  public void setOcrUser(String user) {
    OcrData data = loadOcrData();
    data.ocrUser = user;
    setOcrData(data);
  }
  public String getOcrUser() { return loadOcrData().ocrUser; }

  private class OcrData {
    public String ocrUser, ocrEngine;
    public int ocrId;
  }

  private OcrData loadOcrData() {
    return new Gson().fromJson(description, OcrData.class);
  }
  private void setOcrData(OcrData data) {
    description = new Gson().toJson(data);
  }
}
