package de.lmu.cis.pocoweb;
import java.io.FileInputStream;
import java.io.File;
import java.util.List;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Page;
import org.raml.jaxrs.example.model.Line;

class Main {
  public static void main(String[] args) {
    try {
      Client client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                                   "pocoweb123");
      System.out.println("sid: " + client.getSid());
      Project np = client.uploadProject(
          new FileInputStream(new File("testdata/hobbes-ocropus.zip")));
      np.setAuthor("flo");
      np.setTitle("foo");
      client.updateProjectData(np);
      List<Project> ps = client.listProjects();
      for (Project p : ps) {
        System.out.println(p.getAuthor() + " " + p.getTitle() + " " +
                           p.getProjectId());
      }
      Project p = client.getProject(305);
      for (Integer pid : p.getPageIds()) {
        System.out.println("book " + 305 + " page id " + pid);
        Page page = client.getPage(p.getProjectId(), pid);
        for (Line line : page.getLines()) {
          System.out.println(line.getCor());
          System.out.println(line.getOcr());
        }
      }
      System.out.println("PID: " + np.getProjectId());
      client.deleteProject(np.getProjectId());
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
