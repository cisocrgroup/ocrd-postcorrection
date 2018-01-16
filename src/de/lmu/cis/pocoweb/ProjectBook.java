package de.lmu.cis.pocoweb;

import org.raml.jaxrs.example.model.Book;

class ProjectBook extends Book {
  public ProjectBook(Book s) {
    this.author = s.getAuthor();
    this.title = s.getTitle();
    this.projectId = s.getProjectId();
    this.language = s.getLanguage();
    this.profilerUrl = s.getProfilerUrl();
    this.description = s.getDescription();
    this.year = s.getYear();
    this.pages = s.getPages();
    this.isBook = s.getIsBook();
    this.pageIds = s.getPageIds();
  }
  public void setOcrId(int id) {}
  public int getOcrId() { return 0; }

  public void setOcrEngine(String engine) {}
  public String getOcrEngine() { return ""; }
  public void setUser(String user) {}
  public String getUser() { return ""; }
}
