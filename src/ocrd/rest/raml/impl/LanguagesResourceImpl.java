package ocrd.rest.raml.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.raml.jaxrs.example.model.Languages;
import org.raml.jaxrs.example.resource.LanguagesResource;
import de.lmu.cis.pocoweb.Client;

public class LanguagesResourceImpl implements LanguagesResource {
  @Override
  public GetLanguagesResponse getLanguages() throws Exception {
    try (Client client = newClient();) {
      return GetLanguagesResponse.withJsonOK(
          client.getLocalProfilerLanguages());
    }
  }

  @Override
  public GetLanguagesByProfilerUrlResponse
  getLanguagesByProfilerUrl(String profilerUrl) throws Exception {
    try (Client client = newClient();) {
      return GetLanguagesByProfilerUrlResponse.withJsonOK(
          client.getProfilerLanguages(profilerUrl));
    }
  }

  private static Client newClient() throws Exception {
    return Client.login("http://pocoweb.cis.lmu.de/rest", "pocoweb",
                        "pocoweb123");
  }
}
