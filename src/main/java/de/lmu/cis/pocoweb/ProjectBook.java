package de.lmu.cis.pocoweb;

import com.google.gson.Gson;
import java.util.List;
import org.raml.jaxrs.example.model.Book;
import org.raml.jaxrs.example.model.Project;

public class ProjectBook {
  public String author, title, language, description, profilerUrl;
  public int projectId, year, pages;
  public List<Integer> pageIds;
  public boolean isBook;

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
    this.setOcrData(new OcrData());
    this.setOcrEngine(s.getOcrEngine());
    this.setOcrUser(s.getOcrUser());
    // System.out.println("NEW PROJCET BOOK DESCRIPTION: " + description);
  }

  public ProjectBook withOcrId(int id) {
    setOcrId(id);
    return this;
  }

  public void setOcrId(int id) {
    OcrData data = loadOcrData();
    data.ocrId = id;
    setOcrData(data);
  }

  public int getOcrId() {
    int ocrId = loadOcrData().ocrId;
    return ocrId == 0 ? projectId : ocrId;
  }

  public ProjectBook withOcrEngine(String engine) {
    setOcrEngine(engine);
    return this;
  }

  public void setOcrEngine(String engine) {
    OcrData data = loadOcrData();
    data.ocrEngine = engine;
    setOcrData(data);
  }
  public String getOcrEngine() { return loadOcrData().ocrEngine; }

  public ProjectBook withOcrUser(String user) {
    setOcrUser(user);
    return this;
  }

  public void setOcrUser(String user) {
    OcrData data = loadOcrData();
    data.ocrUser = user;
    setOcrData(data);
  }
  public String getOcrUser() { return loadOcrData().ocrUser; }

  public Book newBook() {
    return new Book()
        .withAuthor(author)
        .withTitle(title)
        .withOcrUser(getOcrUser())
        .withLanguage(language)
        .withProfilerUrl(profilerUrl)
        .withOcrEngine(getOcrEngine())
        .withProjectId(projectId)
        .withYear(year)
        .withPages(pages)
        .withPageIds(pageIds)
        .withDescription(description);
  }

  public Project newProject() {
    Project project = new Project()
                          .withAuthor(author)
                          .withTitle(title)
                          .withLanguage(language)
                          .withProfilerUrl(profilerUrl)
                          .withYear(year)
                          .withUser(getOcrUser())
                          .withProjectId(getOcrId());
    project.getBooks().add(this.newBook());
    return project;
  }

  private class OcrData {
    public String ocrUser = "";
    public String ocrEngine = "";
    public int ocrId = 0;
  }

  private OcrData loadOcrData() {
    OcrData data = new Gson().fromJson(description, OcrData.class);
    if (data == null) {
      return new OcrData();
    }
    // System.out.println("LOAD OCR DATA: " + description);
    return data;
  }
  private void setOcrData(OcrData data) {
    // System.out.println("SET OCR DATA BEFORE: " + description);
    if (data == null) {
      data = new OcrData();
    }
    description = new Gson().toJson(data);
    // System.out.println("SET OCR DATA AFTER: " + description);
  }
}
