package test;

import java.io.InputStream;
import java.io.FileInputStream;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.ProjectBook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Projects;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ClientTest {
  private Client client;
  private static final String resourceAbbyyZip =
      "src/test/resources/1841-DieGrenzboten-abbyy.zip";
  private static final String resourceTesseractZip =
      "src/test/resources/1841-DieGrenzboten-tesseract.zip";
  private static final String resourceOcropusZip =
      "src/test/resources/1841-DieGrenzboten-ocropus.zip";

  @Before
  public void login() throws Exception {
    this.client =
        Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb", "pocoweb123");
  }
  @After
  public void logout() throws Exception {
    this.client.close();
  }

  @Test
  public void TestGetProjects() throws Exception {
    Projects ps = this.client.listProjects();
    assertNotNull(ps);
  }

  @Test
  public void TestCreateNewProject() throws Exception {
    ProjectBook book =
        new ProjectBook().withOcrEngine("abbyy").withOcrUser("test-ocr-user");
    book.withAuthor("Grenzboten").withTitle("Die Grenzboten").withYear(1841);
    uploadBook(book, resourceAbbyyZip);
    Project project = book.newProjectFromThis();
    assertThat(project.getProjectId(), is(book.getProjectId()));
    assertThat(project.getUser(), is(book.getOcrUser()));
    assertThat(project.getAuthor(), is(book.getAuthor()));
    assertThat(project.getTitle(), is(book.getTitle()));
    assertThat(project.getYear(), is(book.getYear()));
    assertThat(project.getBooks().size(), is(1));
    book = new ProjectBook()
               .withOcrEngine("tesseract")
               .withOcrUser("test-ocr-user");
    book.addThisToProject(project);
    uploadBook(book, resourceTesseractZip);
    assertThat(project.getProjectId(), is(not(book.getProjectId())));
    assertThat(project.getUser(), is(book.getOcrUser()));
    assertThat(project.getAuthor(), is(book.getAuthor()));
    assertThat(project.getTitle(), is(book.getTitle()));
    assertThat(project.getYear(), is(book.getYear()));
    assertThat(project.getBooks().size(), is(2));
    book =
        new ProjectBook().withOcrEngine("ocropus").withOcrUser("test-ocr-user");
    book.addThisToProject(project);
    uploadBook(book, resourceOcropusZip);
    assertThat(project.getProjectId(), is(not(book.getProjectId())));
    assertThat(project.getUser(), is(book.getOcrUser()));
    assertThat(project.getAuthor(), is(book.getAuthor()));
    assertThat(project.getTitle(), is(book.getTitle()));
    assertThat(project.getYear(), is(book.getYear()));
    assertThat(project.getBooks().size(), is(3));
    Project otherProject = client.getProject(project.getProjectId());
    assertThat(project.getProjectId(), is(otherProject.getProjectId()));
    assertThat(project.getUser(), is(otherProject.getUser()));
    assertThat(project.getAuthor(), is(otherProject.getAuthor()));
    assertThat(project.getTitle(), is(otherProject.getTitle()));
    assertThat(project.getYear(), is(otherProject.getYear()));
    assertThat(project.getBooks().size(), is(otherProject.getBooks().size()));
    client.deleteProject(project);
  }

  private void uploadBook(ProjectBook book, String resource) throws Exception {
    try (InputStream is = new FileInputStream(resource);) {
      client.uploadBook(book, is);
      assertThat(book.getProjectId(), is(not(0)));
    }
  }
}
