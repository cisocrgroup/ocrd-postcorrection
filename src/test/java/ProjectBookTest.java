package test;

import de.lmu.cis.pocoweb.ProjectBook;
import org.junit.Test;
import static org.junit.Assert.*;
import org.raml.jaxrs.example.model.Projects;

public class ProjectBookTest {
  private static final String testOcrUser = "test-ocr-user";
  private static final String testOcrEngine = "test-ocr-engine";
  private static final int testOcrId = 27;

  @Test
  public void testWithOcrUser() throws Exception {
    ProjectBook book = new ProjectBook().withOcrUser(testOcrUser);
    assertEquals(book.getOcrUser(), testOcrUser);
  }
  @Test
  public void testWithOcrId() throws Exception {
    ProjectBook book = new ProjectBook().withOcrId(testOcrId);
    assertEquals(book.getOcrId(), testOcrId);
  }
  @Test
  public void testWithOcrEngine() throws Exception {
    ProjectBook book = new ProjectBook().withOcrEngine(testOcrEngine);
    assertEquals(book.getOcrEngine(), testOcrEngine);
  }
  @Test
  public void testWithAll() throws Exception {
    ProjectBook book = new ProjectBook()
                           .withOcrUser(testOcrUser)
                           .withOcrId(testOcrId)
                           .withOcrEngine(testOcrEngine);
    assertEquals(book.getOcrUser(), testOcrUser);
    assertEquals(book.getOcrId(), testOcrId);
    assertEquals(book.getOcrEngine(), testOcrEngine);
  }
}
