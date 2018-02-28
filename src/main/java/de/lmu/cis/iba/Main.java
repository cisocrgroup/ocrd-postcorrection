package de.lmu.cis.iba;

import de.lmu.cis.ocrd.Document;
import de.lmu.cis.ocrd.Document.OCRLine;
import de.lmu.cis.ocrd.Config;
import de.lmu.cis.pocoweb.Client;
import de.lmu.cis.pocoweb.Token;
import com.google.gson.Gson;

// import de.lmu.cis.ocrd.Line;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.lmu.cis.api.model.Page;
import de.lmu.cis.api.model.Project;

import de.lmu.cis.api.model.Book;

class Main {
  private static final int N = 3;

  static ArrayList<Node> sinks = new ArrayList();

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

      LineAlignment l_alignment = new LineAlignment(doc, 3);
      for (ArrayList<OCRLine> aligned_lines : l_alignment) {
        for (OCRLine line : aligned_lines) {
          System.out.println(line);
        }
      }
      for (ArrayList<OCRLine> aligned_lines : l_alignment) {
        OCRLine mocr = null;
        ArrayList<OCRLine> other = new ArrayList<OCRLine>();
        for (OCRLine line : aligned_lines) {
          if (line.isMasterOCR) {
            mocr = line;
          } else {
            other.add(line);
          }
        }
        if (mocr == null) {
          throw new Exception("no master ocr");
        }
        for (OCRLine line : other) {
          ArrayList<String> stringset = new ArrayList<String>();
          stringset.add("#" + mocr.toString() + "$");
          stringset.add("#" + line.toString() + "$");
          Pairwise_LCS_Alignment alignment =
              new Pairwise_LCS_Alignment(stringset);
          alignment.align();
          System.out.println(new Gson().toJson(alignment.getAligmentPairs()));
          // System.out.println(mocr);
          // System.out.println(line);
          // System.out.println(alignment.LCS_to_JSONString());
        }
      }
      //	alignment.align();
      //	String json = alignment.LCS_to_JSONString();
      //	System.out.println(json);

      client.deleteProject(project);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }
}
