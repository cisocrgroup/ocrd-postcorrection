package de.lmu.cis.ocrd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class Config {
  private Properties properties;
  private static final Config instance = new Config();

  public static Config getInstance() {
    return instance;
  }

  private Config() {
    this.properties = new Properties();
    this.properties.setProperty("pocowebURL", "http://pocoweb.cis.lmu.de/rest");
    this.properties.setProperty("pocowebUser", "pocoweb");
    this.properties.setProperty("pocowebPass", "pocoweb123");
  }

  public String getPocowebURL() {
    return properties.getProperty("pocowebURL");
  }

  public String getPocowebUser() {
    return this.properties.getProperty("pocowebUser");
  }

  public String getPocowebPass() {
    return this.properties.getProperty("pocowebPass");
  }

  public void load(String path) throws IOException {
    // use default values
    Properties newProperties = new Properties(this.properties);
    try (InputStream is = new FileInputStream(path);) {
      newProperties.load(is);
    }
    this.properties = newProperties;
  }
}
