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
import org.raml.jaxrs.example.model.Book;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ClientTest {
  private Client client;
  private Project project;
  private static final String resourceAbbyyZip =
      "src/test/resources/1841-DieGrenzboten-abbyy-small.zip";
  private static final String resourceTesseractZip =
      "src/test/resources/1841-DieGrenzboten-tesseract-small.zip";
  private static final String resourceOcropusZip =
      "src/test/resources/1841-DieGrenzboten-ocropus-small.zip";

  @Before
  public void login() throws Exception {
    this.client =
        Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb", "pocoweb123");
  }
  @After
  public void logout() throws Exception {
    if (project != null) {
      client.deleteProject(project);
    }
    this.client.close();
  }

  @Test
  public void TestGetProjects() throws Exception {
    Projects ps = this.client.listProjects();
    assertNotNull(ps);
  }

  @Test
  public void TestCreateNewProject() throws Exception {
    Book book = new Book()
                    .withOcrEngine("abbyy")
                    .withOcrUser("test-ocr-user")
                    .withAuthor("Grenzboten")
                    .withTitle("Die Grenzboten")
                    .withYear(1841);
    try (InputStream is = new FileInputStream(resourceAbbyyZip);) {
      this.project = client.newProject(book, is);
    }
    assertThat(project.getUser(), is("test-ocr-user"));
    assertThat(project.getYear(), is(1841));
    assertThat(project.getAuthor(), is("Grenzboten"));
    assertThat(project.getTitle(), is("Die Grenzboten"));
    assertThat(project.getProjectId(), is(not(0)));
    assertThat(project.getBooks().size(), is(1));
    assertThat(project.getBooks().get(0).getOcrEngine(), is("abbyy"));
    assertThat(project.getProjectId(),
               is(project.getBooks().get(0).getProjectId()));
    assertThat(project.getUser(), is(project.getBooks().get(0).getOcrUser()));
    assertThat(project.getAuthor(), is(project.getBooks().get(0).getAuthor()));
    assertThat(project.getTitle(), is(project.getBooks().get(0).getTitle()));
    assertThat(project.getYear(), is(project.getBooks().get(0).getYear()));
    // tesseract
    book = new Book()
               .withOcrEngine("tesseract")
               .withOcrUser("test-ocr-user")
               .withAuthor("Grenzboten")
               .withTitle("Die Grenzboten")
               .withYear(1841);
    try (InputStream is = new FileInputStream(resourceTesseractZip);) {
      project = client.addBook(project, book, is);
    }
    assertThat(project.getBooks().size(), is(2));
    assertThat(project.getBooks().get(1).getOcrEngine(), is("tesseract"));
    assertThat(project.getBooks().get(1).getProjectId(), is(not(0)));
    assertThat(book.getProjectId(),
               is(not(project.getBooks().get(1).getProjectId())));
    assertThat(project.getProjectId(),
               is(not(project.getBooks().get(1).getProjectId())));
    assertThat(project.getUser(), is(project.getBooks().get(1).getOcrUser()));
    assertThat(project.getAuthor(), is(project.getBooks().get(1).getAuthor()));
    assertThat(project.getTitle(), is(project.getBooks().get(1).getTitle()));
    assertThat(project.getYear(), is(project.getBooks().get(1).getYear()));
    // ocropus
    book = new Book()
               .withOcrEngine("ocropus")
               .withOcrUser("test-ocr-user")
               .withAuthor("Grenzboten")
               .withTitle("Die Grenzboten")
               .withYear(1841);
    try (InputStream is = new FileInputStream(resourceOcropusZip);) {
      project = client.addBook(project, book, is);
    }
    assertThat(project.getBooks().size(), is(3));
    assertThat(project.getBooks().get(2).getOcrEngine(), is("ocropus"));
    assertThat(project.getBooks().get(2).getProjectId(), is(not(0)));
    assertThat(book.getProjectId(),
               is(not(project.getBooks().get(2).getProjectId())));
    assertThat(project.getProjectId(),
               is(not(project.getBooks().get(2).getProjectId())));
    assertThat(project.getUser(), is(project.getBooks().get(2).getOcrUser()));
    assertThat(project.getAuthor(), is(project.getBooks().get(2).getAuthor()));
    assertThat(project.getTitle(), is(project.getBooks().get(2).getTitle()));
    assertThat(project.getYear(), is(project.getBooks().get(2).getYear()));
  }
}
