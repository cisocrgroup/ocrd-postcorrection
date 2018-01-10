package de.lmu.cis.pocoweb;
import java.io.FileInputStream;
import java.io.File;

class Main {
  public static void main(String[] args) {
    try {
      Client client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                                   "pocoweb123");
      System.out.println("sid: " + client.getSid());
      ProjectData np = client.uploadProject(
          new FileInputStream(new File("testdata/hobbes-ocropus.zip")));
      np.author = "flo";
      np.title = "title";
      client.updateProjectData(np);
      ProjectData[] ps = client.listProjects();
      for (ProjectData p : ps) {
        System.out.println(p.author + " " + p.title + " " + p.projectId);
      }
      ProjectData p = client.getProject(305);
      for (int pid : p.pageIds) {
        System.out.println("book " + 305 + " page id " + pid);
      }
      System.out.println("PID: " + np.projectId);
      client.deleteProject(np.projectId);
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
