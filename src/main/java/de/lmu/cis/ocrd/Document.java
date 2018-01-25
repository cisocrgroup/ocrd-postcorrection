package de.lmu.cis.ocrd;

import de.lmu.cis.pocoweb.Client;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.raml.jaxrs.example.model.Book;
import org.raml.jaxrs.example.model.Page;
import org.raml.jaxrs.example.model.Project;

public class Document {
  private final Project project;
  private final Client client;
  public Document(Project project, Client client) {
    this.client = client;
    this.project = project;
  }
  public void eachLine(Visitor v) throws Exception {
    List<LineTriple> lines = new ArrayList<LineTriple>();
    System.out.println("size: " + project.getBooks().size());
    for (Book book : project.getBooks()) {
      System.out.println("engine: " + book.getOcrEngine() + ", size: " +
                         book.getPageIds().size() + ",user: " +
                         book.getOcrUser());
      int pseq = 1;
      for (int pageId : book.getPageIds()) {
        Page page = client.getPage(book.getProjectId(), pageId);
        for (org.raml.jaxrs.example.model.Line line : page.getLines()) {
          lines.add(new LineTriple(book.getOcrEngine(), new Line(client, line),
                                   pseq));
        }
        pseq++;
      }
    }
    Collections.sort(lines);
    for (LineTriple line : lines) {
      v.visit(line);
    }
  }

  public class LineTriple implements Comparable<LineTriple> {
    public LineTriple(String ocrEngine, Line line, int pageSeq) {
      this.ocrEngine = ocrEngine;
      this.line = line;
      this.pageSeq = pageSeq;
    }

    public int compareTo(LineTriple other) {
      if (this.pageSeq < other.pageSeq) {
        return -1;
      }
      if (this.pageSeq > other.pageSeq) {
        return 1;
      }
      if (this.line.getLineId() < other.line.getLineId()) {
        return -1;
      }
      if (this.line.getLineId() > other.line.getLineId()) {
        return 1;
      }
      return this.ocrEngine.compareTo(other.ocrEngine);
    }
    public String ocrEngine;
    public Line line;
    public int pageSeq;
  }

  public interface Visitor { void visit(LineTriple t) throws Exception; }
}
