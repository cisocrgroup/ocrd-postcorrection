package de.lmu.cis.pocoweb;
import java.io.FileInputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.List;
import org.raml.jaxrs.example.model.Page;
import org.raml.jaxrs.example.model.Line;

class Main {
  private static String patternsToString(String[] patterns) {
    String prefix = "[";
    String res = "";
    if (patterns == null || patterns.length == 0) {
      res += "[]";
    } else {
      for (String p : patterns) {
        res += prefix + p;
        prefix = ",";
      }
      res += "]";
    }
    return res;
  }

  public static void main(String[] args) {
    try (Client client = Client.login("http://pocoweb.cis.lmu.de/rest",
                                      "pocoweb", "pocoweb123");) {
      // System.out.println("sid: " + client.getSid());
      // Project np = client.uploadProject(
      //     new FileInputStream(new File("testdata/hobbes-ocropus.zip")));
      // np.setAuthor("flo");
      // np.setTitle("foo");
      // client.updateProjectData(np);
      List<ProjectBook> bs = client.listBooks();
      System.out.println("len bs: " + bs.size());
      int i = 0;
      for (ProjectBook b : bs) {
        System.out.println(b.getAuthor() + " " + b.getTitle() + " " +
                           b.getProjectId());
        b.setOcrId(++i);
        b.setOcrEngine("ocropus");
        b.setOcrUser("user");
        System.out.println(b.getDescription());
      }
      // client.orderProfile(201);
      // while (client.getProfilingStatus(201) != 200) {
      //   System.out.println("waiting");
      //   TimeUnit.SECONDS.sleep(10);
      // }
      client.orderProfile(201);
      TimeUnit.SECONDS.sleep(20);
      for (SuggestionData s : client.getSuggestions(201).suggestions) {
        System.out.print(String.format(
            "%d %d %d %d %s %s %s %s (%f)", s.projectId, s.pageId, s.lineId,
            s.tokenId, s.token, s.suggestion, patternsToString(s.ocrPatterns),
            patternsToString(s.histPatterns), s.weight));
        System.out.println();
      }

      // Project p = client.getProject(201);
      // for (Integer pid : p.getPageIds()) {
      //   System.out.println("book " + 201 + " page id " + pid);
      //   Page page = client.getPage(p.getProjectId(), pid);
      //   for (Line line : page.getLines()) {
      //     System.out.println(line.getCor());
      //     System.out.println(line.getOcr());
      //   }
      // }
      // System.out.println("PID: " + np.getProjectId());
      // client.deleteProject(np.getProjectId());
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
