package de.lmu.cis.pocoweb;

import java.io.FileInputStream;
import java.io.InputStream;

import de.lmu.cis.api.model.Book;
import de.lmu.cis.api.model.Project;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.ocrd.Document.Visitor;
import de.lmu.cis.ocrd.OCRLine;
import de.lmu.cis.ocrd.PocowebDocument;

class Main {

  public static void main(String[] args) {
    try (Client client = Client.login(Config.getInstance().getPocowebURL(),
                                      Config.getInstance().getPocowebUser(),
                                      Config.getInstance().getPocowebPass());) {
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
      PocowebDocument doc = new PocowebDocument(project, client);
      doc.eachLine(new Visitor() {
        @Override
		public void visit(OCRLine t) throws Exception {
          // System.out.println(String.format("[%9s,%1d,%2d] %s", t.ocrEngine,
          //                                  t.pageSeq, t.line.getLineId(),
          //                                  t.line.getNormalized()));
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
