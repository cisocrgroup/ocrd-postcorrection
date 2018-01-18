package de.lmu.cis.pocoweb;

import com.google.gson.Gson;
import java.util.List;
import org.raml.jaxrs.example.model.Book;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.ProjectEntry;

public class ProjectBook extends Book {
  public ProjectBook() {
    super();
    this.description = "{}";
  }
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

  public ProjectBook withOcrEngine(String engine) {
    setOcrEngine(engine);
    return this;
  }

  public ProjectBook withOcrUser(String user) {
    setOcrUser(user);
    return this;
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

  public Project newProjectFromThis() {
    int ocrId = getOcrId();
    // if the book does not have a valid ocrId,
    // use the book's projectId instead.
    if (ocrId == 0) {
      ocrId = projectId;
    }
    Project project = new Project()
                          .withAuthor(author)
                          .withTitle(title)
                          .withLanguage(language)
                          .withProfilerUrl(profilerUrl)
                          .withYear(year)
                          .withUser(getOcrUser())
                          .withProjectId(ocrId);
    project.getBooks().add(
        new ProjectEntry().withOcrEngine(getOcrEngine()).withBook(this));
    return project;
  }

  public void addThisToProject(Project project, String ocrEngine) {
    // ocrId
    List<ProjectEntry> list = project.getBooks();
    if (list.isEmpty()) {
      project.setProjectId(this.projectId);
    }
    // update book data
    this.author = project.getAuthor();
    this.title = project.getTitle();
    this.language = project.getLanguage();
    this.profilerUrl = project.getProfilerUrl();
    this.year = project.getYear();
    this.setOcrEngine(ocrEngine);
    this.setOcrUser(project.getUser());
    this.setOcrId(project.getProjectId());
    // insert this book into project's list of books
    list.add(new ProjectEntry().withOcrEngine(ocrEngine).withBook(this));
    project.setBooks(list);
  }

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
