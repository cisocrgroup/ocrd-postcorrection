package de.lmu.cis.iba;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;

// import de.lmu.cis.ocrd.Line;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import de.lmu.cis.api.model.Page;
import de.lmu.cis.api.model.Project;

import de.lmu.cis.api.model.Book;

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
    try (Client client = Client.login(Config.getInstance().getPocowebURL(),
                                      Config.getInstance().getPocowebUser(),
                                      Config.getInstance().getPocowebPass());) {
      Book book = new Book()
                      .withOcrEngine("abbyy")
                      .withOcrUser("test-ocr-user")
                      .withAuthor("Grenzboten")
                      .withTitle("Die Grenzboten")
                      .withYear(1841);
      Project project;
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-abbyy-small.zip");) {
        project = client.newProject(book, is);
      }
      book = new Book()
                 .withOcrEngine("tesseract")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (
          InputStream is = new FileInputStream(
              "src/test/resources/1841-DieGrenzboten-tesseract-small-with-error.zip");) {
        project = client.addBook(project, book, is);
      }
      book = new Book()
                 .withOcrEngine("ocropus")
                 .withOcrUser("test-ocr-user")
                 .withAuthor("Grenzboten")
                 .withTitle("Die Grenzboten")
                 .withYear(1841);
      try (InputStream is = new FileInputStream(
               "src/test/resources/1841-DieGrenzboten-ocropus-small.zip");) {
        project = client.addBook(project, book, is);
      }
      Document doc = new Document(project, client);

      ArrayList<String> stringset = new ArrayList<String>();

      doc.eachLine(new Document.Visitor() {
        public void visit(Document.LineTriple t) throws Exception {
          System.out.println(String.format("[%9s,%1d,%2d] %s", t.ocrEngine,
                                           t.pageSeq, t.line.getLineId(),
                                           t.line.getNormalized()));
          for (Token token : t.line.getTokens()) {
            System.out.println(String.format(
                "[token %2d] %s", token.getTokenId(), token.getCor()));
          }
          stringset.add("#" + t.line.getNormalized() + "$");
        }
      });

      for (int i = 0; i < stringset.size(); i++) {
        System.out.println(stringset.get(i));
      }

<<<<<<< HEAD
      Online_CDAWG_sym scdawg = new Online_CDAWG_sym(stringset, true);
      scdawg.determineAlphabet(true);
      scdawg.build_cdawg();
      // scdawg.print_automaton("svgs/scdawg");

      ArrayList test_result = new ArrayList();

      HashMap longest_quasi_nodes = new HashMap();

      for (int j = 0; j < scdawg.sinks.size(); j++) {
        for (int i = 0; i < scdawg.all_nodes.size(); i++) {
          Node node = scdawg.all_nodes.get(i);

          Iterator it = node.children.entrySet().iterator();

          while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node child = (Node)pair.getValue();

            if (scdawg.sinks.get(j) == child) {  // wenn rechtsübergang auf sink
              test_result.add(scdawg.sinks.get(j).stringnr + " " +
                              scdawg.get_node_label(node));

              if (!longest_quasi_nodes.containsKey(
                      scdawg.sinks.get(j).stringnr)) {
                longest_quasi_nodes.put(scdawg.sinks.get(j).stringnr,
                                        scdawg.get_node_label(node));

              } else {
                if (scdawg.get_node_length(node) >
                    longest_quasi_nodes.get(scdawg.sinks.get(j).stringnr)
                        .toString()
                        .length()) {
                  longest_quasi_nodes.put(scdawg.sinks.get(j).stringnr,
                                          scdawg.get_node_label(node));
                }
              }

              Iterator it2 = node.children_left.entrySet().iterator();

              while (it2.hasNext()) {
                Map.Entry pair_left = (Map.Entry)it2.next();
                Node child_left = (Node)pair_left.getValue();

                if (scdawg.sinks.get(j) ==
                    child_left) {  // wenn linksübergang auf sink
                }
              }
            }
          }
        }
      }

      ArrayList<ArrayList> all_aligned_lines = new ArrayList<ArrayList>();

      Iterator it = longest_quasi_nodes.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();

        int key1 = (int)pair.getKey();
        String value1 = (String)pair.getValue();
        System.out.println(key1 + " " + value1);

        if (value1 == null) continue;

        ArrayList<Integer> aligned_lines = new ArrayList();
        aligned_lines.add(key1);

        Iterator it2 = longest_quasi_nodes.entrySet().iterator();

        //			  while (it2.hasNext()) {
        //			      Map.Entry pair2 = (Map.Entry)it2.next();
        //
        //			      int key2 = (int) pair2.getKey();
        //				  String value2 =  (String)
        //pair2.getValue();
        //
        //			      if (key1!=key2&&value1.equals(value2)) {
        //
        //			    	  aligned_lines.add(key2);
        //
        //			    	  longest_quasi_nodes.put(key2, null);
        //
        //			      }
        //			  }
        //
        //			  all_aligned_lines.add(aligned_lines);
      }

      for (ArrayList aligned_lines : all_aligned_lines) {
        for (int i = 0; i < aligned_lines.size(); i++) {
          System.out.println(aligned_lines.get(i));
        }
        System.out.println("-------------------");
      }
=======
      ArrayList test = new ArrayList();

      test.add(
          "#Brüssel Wenige Städte in Europa bieten gleiche Vortheile der$");
      test.add("#Bruſſel Wenige Stͤdte in Europa bieten gleiche Vortheiſe der$");
      test.add(
          "#Brüssel Wenige Städte in Europa bieten gleiche Vortheile der$");

      Online_CDAWG_sym scdawg = new Online_CDAWG_sym(test, true);
      scdawg.determineAlphabet(true);
      scdawg.build_cdawg();

      ArrayList test_result = new ArrayList();

      for (int j = 0; j < scdawg.sinks.size(); j++) {
        for (int i = 0; i < scdawg.all_nodes.size(); i++) {
          Node node = scdawg.all_nodes.get(i);

          // all nodes except first and last alignment part

          Iterator it = node.children.entrySet().iterator();

          while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node child = (Node)pair.getValue();

            int letter = (int)pair.getKey();

            if (scdawg.sinks.get(j) == child) {  // wenn rechtsübergang auf sink
              test_result.add(scdawg.sinks.get(j).stringnr + " " +
                              scdawg.get_node_label(node));
            }
          }

          Iterator it2 = node.children_left.entrySet().iterator();

          while (it2.hasNext()) {
            Map.Entry pair_left = (Map.Entry)it2.next();
            Node child_left = (Node)pair_left.getValue();
            System.out.println(child_left.stringnr);

            if (scdawg.sinks.get(j) == child_left) {
              test_result.add(scdawg.sinks.get(j).stringnr + " " +
                              scdawg.get_node_label(node));
            }
          }
        }
      }

      for (int i = 0; i < test_result.size(); i++) {
        //			   System.out.println(test_result.get(i));
      }
>>>>>>> new-order

      client.deleteProject(project);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }
}
