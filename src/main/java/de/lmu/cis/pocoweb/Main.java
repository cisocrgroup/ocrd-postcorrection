package de.lmu.cis.pocoweb;

import de.lmu.cis.ocrd.Document;
// import de.lmu.cis.ocrd.Line;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.raml.jaxrs.example.model.Page;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Book;

class Main {
  private static String patternsToString(String[] patterns) {
    String prefix = "[";
    String res = "";
    if (patterns == null || patterns.length == 0) {
      res += "[]";
    } else {
      for (String p : patterns) {
        res += prefix + p;
        prefix = ",";
      }
      res += "]";
    }
    return res;
  }

  public static void main(String[] args) {
    try (Client client = Client.login("http://pocoweb.cis.lmu.de/rest",
                                      "pocoweb", "pocoweb123");) {
      Book book = new Book()
                      .withOcrEngine("abbyy")
                      .withOcrUser("test-ocr-user")
                      .withAuthor("Grenzboten")
                      .withTitle("Die Grenzboten")
                      .withYear(1841);
      Project project;
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-abbyy-small.zip");) {
        project = client.newProject(book, is);
      }
      book = new Book()
                 .withOcrEngine("tesseract")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (
          InputStream is = new FileInputStream(
              "src/test/resources/1841-DieGrenzboten-tesseract-small-with-error.zip");) {
        project = client.addBook(project, book, is);
      }
      book = new Book()
                 .withOcrEngine("ocropus")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-ocropus-small.zip");) {
        project = client.addBook(project, book, is);
      }
      Document doc = new Document(project, client);
      doc.eachLine(new Document.Visitor() {
        public void visit(Document.LineTriple t) throws Exception {
          System.out.println(String.format("[%9s,%1d,%2d] %s", t.ocrEngine,
                                           t.pageSeq, t.line.getLineId(),
                                           t.line.getNormalized()));
          // for (Token token : t.line.getTokens()) {
          //   System.out.println(String.format(
          //       "[token %2d] %s", token.getTokenId(), token.getCor()));
          // }
        }
      });
      client.deleteProject(project);
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
