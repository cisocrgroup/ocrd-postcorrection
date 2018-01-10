package de.lmu.cis.pocoweb;
import java.io.FileInputStream;
import java.io.File;

class Main {
  public static void main(String[] args) {
    try {
      Client client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                                   "pocoweb123");
      System.out.println("sid: " + client.getSid());
      ProjectData nb = client.uploadProject(
          new FileInputStream(new File("testdata/hobbes-ocropus.zip")));
      nb.author = "flo";
      nb.title = "title";
      client.updateProjectData(nb);
      ProjectData[] bs = client.listProjects();
      for (ProjectData b : bs) {
        System.out.println(b.author + " " + b.title + " " + b.projectId);
      }
      ProjectData b = client.getProject(305);
      for (int p : b.pageIds) {
        System.out.println("book " + 305 + " page " + p);
      }
      System.out.println("PID: " + nb.projectId);
      client.deleteProject(nb.projectId);
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
