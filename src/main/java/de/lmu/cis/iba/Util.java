package de.lmu.cis.iba;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Util {
  public static String parseFilename(String filename) {
    String[] array = filename.split("\\\\");
    String[] array2 = array[array.length - 1].split("\\.");

    return array2[0];
  }
  public static <K extends Comparable, V> Map<K, V> sortByKeys(Map<K, V> map) {
    return new TreeMap<>(map);
  }

  public static HashMap sortByValues(HashMap map, String mode) {
    List list = new LinkedList(map.entrySet());
    // Defined Custom Comparator here

    if (mode.equals("ASC")) {
      Collections.sort(list, new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((Comparable)((Map.Entry)(o1)).getValue())
              .compareTo(((Map.Entry)(o2)).getValue());
        }
      });

    }

    else {
      Collections.sort(list, new Comparator() {
        public int compare(Object o2, Object o1) {
          return ((Comparable)((Map.Entry)(o1)).getValue())
              .compareTo(((Map.Entry)(o2)).getValue());
        }
      });
    }

    HashMap sortedHashMap = new LinkedHashMap();
    for (Iterator it = list.iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      sortedHashMap.put(entry.getKey(), entry.getValue());
    }
    return sortedHashMap;
  }

  public static ArrayList readInputFile(String filename) throws IOException {
    ArrayList result = new ArrayList();

    BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(filename), StandardCharsets.UTF_8));
    try {
      File f = new File(filename);
      byte[] fileBytes = readBytes(f);

      String str = new String(fileBytes, StandardCharsets.UTF_8);

      String strarray[] = str.split("\\r?\\n");

      for (int i = strarray.length - 1; i >= 0; i--) {
        result.add(strarray[i].trim());
      }

    } finally {
      br.close();
    }

    // for(int i=0;i<result.size();i++) System.out.println(" x"+result.get(i));

    return result;
  }

  public static byte[] readBytes(File file) {
    FileInputStream fis = null;
    byte[] b = null;
    try {
      fis = new FileInputStream(file);
      b = readBytesFromStream(fis);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // close(fis);
    }
    return b;
  }

  public static byte[] readBytesFromStream(InputStream readStream)
      throws IOException {
    ByteArrayOutputStream writeStream = null;
    byte[] byteArr = null;
    writeStream = new ByteArrayOutputStream();
    try {
      copy(readStream, writeStream);
      writeStream.flush();
      byteArr = writeStream.toByteArray();
    } finally {
      close(writeStream);
    }
    return byteArr;
  }

  public static void close(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      writer = null;
    }
  }

  public static void close(InputStream inStream) {
    try {
      inStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    inStream = null;
  }

  public static void close(OutputStream outStream) {
    try {
      outStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    outStream = null;
  }

  public static long copy(InputStream readStream, OutputStream writeStream)
      throws IOException {
    int bytesread = -1;
    byte[] b = new byte[4096];  // 4096 is default cluster size in Windows for <
                                // 2TB NTFS partitions
    long count = 0;
    bytesread = readStream.read(b);
    while (bytesread != -1) {
      writeStream.write(b, 0, bytesread);
      count += bytesread;
      bytesread = readStream.read(b);
    }
    return count;
  }

  public static void writeFile(String filename, String content) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename));
      writer.write(content);

    } catch (IOException e) {
    } finally {
      try {
        if (writer != null) writer.close();
      } catch (IOException e) {
      }
    }
  }

  public static void printHelp() {
    printFrame(new ArrayList() {
      {
        add("Usage:");
        add("indexstructure: -s <'suffixtrie,suffixtree,dawg,cdawg'>");
        add("modes: -i <console input> -d  -f <read file>  <read directory>");
      }
    });
  }

  // ++++++++++++++++++++++++++++++++++++++++++++++++ //
  // +++++++++++++ Print-Methoden +++++++++++++++++++ //
  // ++++++++++++++++++++++++++++++++++++++++++++++++ //

  // =======================================
  // printFrame
  // =======================================
  public static void printFrame(String text) {
    System.out.println("");
    System.out.print(" +-");
    for (int i = 0; i < text.length(); i++) System.out.print("-");
    System.out.println("-+");

    System.out.println(" | " + text + " |");
    System.out.print(" +-");
    for (int i = 0; i < text.length(); i++) System.out.print("-");
    System.out.println("-+");
    System.out.println("");

  }  // printFrame

  // =======================================
  // printFrame
  // =======================================

  public static void printFrame(ArrayList textList) {
    String s;
    int maxLength = 0;

    for (int i = 0; i < textList.size(); i++) {
      s = (String)textList.get(i);
      if (maxLength < s.length()) maxLength = s.length();
    }

    System.out.println("");
    System.out.print(" +-");
    for (int i = 0; i < maxLength; i++) System.out.print("-");
    System.out.println("-+");

    for (int i = 0; i < textList.size(); i++) {
      s = " | " + textList.get(i);

      for (int j = s.length(); j <= maxLength + 3; j++) s += " ";

      s += "|";

      System.out.println(s);
    }
    System.out.print(" +-");
    for (int i = 0; i < maxLength; i++) System.out.print("-");
    System.out.println("-+");
    System.out.println("");

  }  // printFrame

  // =======================================
  // printArray
  // =======================================
  public static void printArray(ArrayList list) {
    // System.out.print("\n");
    for (int i = 0; i < list.size(); i++) {
      System.out.println("   - " + list.get(i));
    }
    // System.out.print("\n");
  }  // printArray

  // =======================================
  // printTreeMap
  // =======================================

  public static String printTreeMap(TreeMap trmp) {
    String s1 = "\n";
    Iterator it = trmp.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry me = (Map.Entry)it.next();
      System.out.println(me);
    }
    // System.out.println(s1);

    return s1;
  }
}
