package ocrd.rest.raml.handler;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.lmu.cis.pocoweb.Client;

import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Book;
import org.raml.jaxrs.example.model.Projects;

public class ProjectsHandler {

  private static Client client;

  public ProjectsHandler() {

    try {

      client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                            "pocoweb123");

      //			      System.out.println("sid: " +
      // client.getSid());
      //			      ProjectData np = client.uploadProject(
      //			          new FileInputStream(new
      // File("testdata/hobbes-ocropus.zip")));
      //			      np.author = "flo";
      //			      np.title = "title";
      //			      client.updateProjectData(np);
      //
      //			      ProjectData p = client.getProject(305);
      //			      for (int pid : p.pageIds) {
      //			        System.out.println("book " + 305 + "
      // page id " + pid);
      //			      }
      //			      System.out.println("PID: " +
      // np.projectId);
      //			      client.deleteProject(np.projectId);
    } catch (Exception e) {
      System.out.println("error: " + e);
    }

  } // ProjectsHandler()

  public Projects listProjects() {
    return null;
    //   try {
    //     Book ps = new Book();
    //     List<Book> pps = client.listBooks();
    //     System.out.println("len projects: " + pps.size());
    //     ps.setBooks(pps);
    //     // ps.setProjects(client.listProjects());
    //     return ps;
    //   } catch (Exception e) {
    //     e.printStackTrace();
    //   }
    //   return null;
  }
}
