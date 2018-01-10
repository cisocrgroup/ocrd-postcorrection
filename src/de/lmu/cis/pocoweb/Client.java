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

class Client {
  private final String host;
  private String sid;

  public static Client login(String host, String user, String pass)
      throws Exception {
    Client Client = new Client(host);
    return Client.login(user, pass);
  }

  public SuggestionsData getSuggestions(int pid) throws Exception {
    return get("/books/" + pid + "/suggestions", SuggestionsData.class);
  }

  public BooksData getBooks() throws Exception {
    return get("/books", BooksData.class);
  }

  public BookData getBook(int pid) throws Exception {
    return get("/books/" + pid, BookData.class);
  }

  public TokensData getTokens(int bid, int pid) throws Exception {
    return get(String.format("/books/%d/pages/%d", bid, pid), TokensData.class);
  }

  public TokenData getToken(int bid, int pid, int lid, int tid)
      throws Exception {
    return get(String.format("/books/%d/pages/%d/lines/%d/tokens/%d", bid, pid,
                             lid, tid),
               TokenData.class);
  }

  public BookData uploadBook(InputStream in) throws Exception {
    return post("/books", in, BookData.class, "application/zip");
  }

  public BookData updateBookData(BookData b) throws Exception {
    return post(String.format("/books/%d", b.bookId), b, BookData.class);
  }

  public String getHost() { return this.host; }
  public String getSid() { return this.sid; }

  private Client(String host) {
    this.host = host;
    this.sid = null;
  }

  private Client login(String user, String pass) throws Exception {
    SidData sid = post("/login", new LoginData(user, pass), SidData.class);
    this.sid = sid.sid;
    return this;
  }

  private <T> T post(String path, Object data, Class<T> clss) throws Exception {
    return post(path, IOUtils.toInputStream(new Gson().toJson(data), "UTF-8"),
                clss, "application/json; charset=UTF-8");
  }

  private <T> T post(String path, InputStream in, Class<T> clss, String ct)
      throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", ct);
    con.setDoOutput(true);
    try (DataOutputStream out = new DataOutputStream(con.getOutputStream());) {
      IOUtils.copy(in, out);
      out.flush();
    }
    try (InputStream cin = con.getInputStream();) {
      return deserialize(cin, clss);
    }
  }

  private <T> T get(String path, Class<T> clss) throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("GET");
    try (InputStream in = con.getInputStream();) {
      return deserialize(in, clss);
    }
  }

  private static <T> T deserialize(InputStream in, Class<T> clss)
      throws Exception {
    StringWriter out = new StringWriter();
    IOUtils.copy(in, out, Charset.forName("UTF-8"));
    return new Gson().fromJson(out.toString(), clss);
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
