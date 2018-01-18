package test;

import de.lmu.cis.pocoweb.ProjectBook;
import org.junit.Test;
import org.raml.jaxrs.example.model.Projects;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ProjectBookTest {
  private static final String testResource = "test-resource";
  private static final String testOcrUser = "test-ocr-user";
  private static final String testOcrEngine = "test-ocr-engine";
  private static final int testOcrId = 27;

  @Test
  public void testWithOcrUser() throws Exception {
    ProjectBook book = new ProjectBook().withOcrUser(testOcrUser);
    assertThat(book.getOcrUser(), is(testOcrUser));
  }
  @Test
  public void testWithOcrId() throws Exception {
    ProjectBook book = new ProjectBook().withOcrId(testOcrId);
    assertThat(book.getOcrId(), is(testOcrId));
  }
  @Test
  public void testWithOcrEngine() throws Exception {
    ProjectBook book = new ProjectBook().withOcrEngine(testOcrEngine);
    assertThat(book.getOcrEngine(), is(testOcrEngine));
  }
  @Test
  public void testWithAll() throws Exception {
    ProjectBook book = new ProjectBook()
                           .withOcrUser(testOcrUser)
                           .withOcrId(testOcrId)
                           .withOcrEngine(testOcrEngine);
    assertThat(book.getOcrUser(), is(testOcrUser));
    assertThat(book.getOcrId(), is(testOcrId));
    assertThat(book.getOcrEngine(), is(testOcrEngine));
  }
}
