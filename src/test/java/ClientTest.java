package test;

import de.lmu.cis.pocoweb.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.raml.jaxrs.example.model.Projects;

public class ClientTest {
  private Client client;

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
}
