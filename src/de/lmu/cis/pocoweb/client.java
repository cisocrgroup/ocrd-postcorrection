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
  private String host;
  private String sid;

  public static Client login(String host, String user, String pass)
      throws Exception {
    Client client = new Client(host);
    return client.login(user, pass);
  }

  public Suggestions getSuggestions(int pid) throws Exception {
    return get("/books/" + pid + "/suggestions", Suggestions.class);
  }

  private Client(String host) {
    this.host = host;
    this.sid = null;
  }

  private Client login(String user, String pass) throws Exception {
    SID sid = post("/login", new Login(user, pass), SID.class);
    this.sid = sid.sid;
    return this;
  }

  private <T> T post(String path, Object data, Class<T> clss) throws Exception {
    HttpURLConnection con = getConnection(path);
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    try (DataOutputStream out = new DataOutputStream(con.getOutputStream());) {
      out.writeBytes(new Gson().toJson(data));
      out.flush();
    }
    try (InputStream in = con.getInputStream();) {
      return deserialize(in, clss);
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

  public static void main(String[] args) {
    try {
      Client client = Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                                   "pocoweb123");
      System.out.println("sid: " + client.sid);
      Suggestions s = client.getSuggestions(305);
      System.out.println(new Gson().toJson(s));
    } catch (Exception e) {
      System.out.println("error: " + e);
    }
  }
}
