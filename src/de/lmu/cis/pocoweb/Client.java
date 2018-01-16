package de.lmu.cis.pocoweb;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.io.IOUtils;
import org.raml.jaxrs.example.model.Book;
import org.raml.jaxrs.example.model.Page;
import org.raml.jaxrs.example.model.Project;
import org.raml.jaxrs.example.model.Projects;

public class Client {
  private final String host;
  private String sid;

  public static Client login(String host, String user, String pass)
      throws Exception {
    Client Client = new Client(host);
    return Client.login(user, pass);
  }

  private class Books { public Book[] books; }
  public List<ProjectBook> listBooks() throws Exception {
    Book[] books = get("/books", Books.class, 200).books;
    List<ProjectBook> list = new ArrayList(books.length);
    for (Book b : books) {
      list.add(new ProjectBook(b));
    }
    return list;
  }

  public Projects listProjects() throws Exception {
    List<ProjectBook> projectBooks = listBooks();
    Map<Integer, Project> map = new HashMap();
    for (ProjectBook book : projectBooks) {
      Integer ocrId = book.getOcrId();
      if (!map.containsKey(ocrId)) {
        map.put(ocrId, book.newProjectFromThis());
      } else {
        book.addThisToProject(map.get(ocrId), book.getOcrEngine());
      }
    }
    List<Project> projects = new ArrayList(map.size());
    projects.addAll(map.values());
    return new Projects().withProjects(projects);
  }

  public Project getProject(int pid) throws Exception {
    for (Project p : listProjects().getProjects()) {
      if (p.getProjectId() == pid) {
        return p;
      }
    }
    throw new Exception("no such project: " + pid);
  }

  // public ProjectData getProject(int pid) throws Exception {
  public ProjectBook getBook(int pid) throws Exception {
    return new ProjectBook(get("/books/" + pid, Book.class, 200));
  }

  public ProjectBook uploadBook(InputStream in) throws Exception {
    return new ProjectBook(
        post("/books", in, Book.class, "application/zip", 200, 201));
  }

  public ProjectBook updateBookData(ProjectBook p) throws Exception {
    return new ProjectBook(
        post(String.format("/books/%d", p.getProjectId()), p, Book.class, 200));
  }

  public void deleteBook(int bid) throws Exception {
    delete(String.format("/books/%d", bid), 200);
  }

  public Page getPage(int bid, int pid) throws Exception {
    return get(String.format("/books/%d/pages/%d", bid, pid), Page.class, 200);
  }

  public TokensData getTokens(int bid, int pid) throws Exception {
    return get(String.format("/books/%d/pages/%d", bid, pid), TokensData.class,
               200);
  }

  public TokenData getToken(int bid, int pid, int lid, int tid)
      throws Exception {
    return get(String.format("/books/%d/pages/%d/lines/%d/tokens/%d", bid, pid,
                             lid, tid),
               TokenData.class, 200);
  }

  public SuggestionsData getSuggestions(int pid) throws Exception {
    return get("/books/" + pid + "/suggestions", SuggestionsData.class, 200);
  }

  public String getHost() { return this.host; }
  public String getSid() { return this.sid; }

  private Client(String host) {
    this.host = host;
    this.sid = null;
  }

  private Client login(String user, String pass) throws Exception {
    SidData sid = post("/login", new LoginData(user, pass), SidData.class, 200);
    this.sid = sid.sid;
    return this;
  }

  private <T> T post(String path, Object data, Class<T> clss, int... codes)
      throws Exception {
    return post(path, IOUtils.toInputStream(new Gson().toJson(data), "UTF-8"),
                clss, "application/json; charset=UTF-8", codes);
  }

  private <T> T post(String path, InputStream in, Class<T> clss, String ct,
                     int... codes) throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", ct);
    con.setDoOutput(true);
    try (DataOutputStream out = new DataOutputStream(con.getOutputStream());) {
      IOUtils.copy(in, out);
      out.flush();
    }
    validateResponseCode(con.getResponseCode(), codes);
    try (InputStream cin = con.getInputStream();) {
      return deserialize(cin, clss);
    }
  }

  private <T> T get(String path, Class<T> clss, int... codes) throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("GET");
    validateResponseCode(con.getResponseCode(), codes);
    try (InputStream in = con.getInputStream();) {
      return deserialize(in, clss);
    }
  }

  private int delete(String path, int... codes)throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("DELETE");
    validateResponseCode(con.getResponseCode(), codes);
    return con.getResponseCode();
  }

  private static <T> T deserialize(InputStream in, Class<T> clss)
      throws Exception {
    StringWriter out = new StringWriter();
    IOUtils.copy(in, out, Charset.forName("UTF-8"));
    // System.out.println("json: " + out.toString());
    return new Gson().fromJson(out.toString(), clss);
  }

  private static void validateResponseCode(int got, int... codes)
      throws Exception {
    for (int want : codes) {
      if (want == got) {
        return;
      }
    }
    throw new Exception("Invalid return code: " + got);
  }

  private HttpURLConnection getConnection(String path) throws Exception {
    HttpURLConnection con =
        (HttpURLConnection) new URL(this.host + path).openConnection();
    if (this.sid != null) {
      con.setRequestProperty("Authorization", "Pocoweb " + sid);
    }
    return con;
  }
}
