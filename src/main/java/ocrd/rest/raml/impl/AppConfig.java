package ocrd.rest.raml.impl;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
  public AppConfig() {
    register(OcrdResourceImpl.class);
    register(ProjectsResourceImpl.class);
  }
}
