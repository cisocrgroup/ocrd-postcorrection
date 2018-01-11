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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.io.IOUtils;

public class Client {
  private final String host;
  private String sid;

  public static Client login(String host, String user, String pass)
      throws Exception {
    Client Client = new Client(host);
    return Client.login(user, pass);
  }

  public SuggestionsData getSuggestions(int pid) throws Exception {
    return get("/books/" + pid + "/suggestions", SuggestionsData.class, 200);
  }

  public ProjectData[] listProjects() throws Exception {
    return get("/books", ProjectsData.class, 200).books;
  }

  public ProjectData getProject(int pid) throws Exception {
    return get("/books/" + pid, ProjectData.class, 200);
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

  public ProjectData uploadProject(InputStream in) throws Exception {
    return post("/books", in, ProjectData.class, "application/zip", 200, 201);
  }

  public ProjectData updateProjectData(ProjectData b) throws Exception {
    return post(String.format("/books/%d", b.projectId), b, ProjectData.class,
                200);
  }

  public void deleteProject(int bid) throws Exception {
    delete(String.format("/books/%d", bid), 200);
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
